package de.bwl.bwfla.historicbuilds.impl;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.ContainerComponentRequest;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.historicbuilds.api.*;
import org.apache.tamaya.ConfigurationProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HistoricBuildTask extends BlockingTask<Object> {

    //SWH variables
    private final String revisionId;
    private final String directoryId;
    private final Boolean shouldExtract;
    private final String scriptLocation;
    private final Path workingDir;

    //BTC variables
    private final String environmentID;
    private final String inputDirectory;
    private final String outputDirectory;
    private final String recipeContent;
    private final String[] prerequisites;
    private final String mail;
    private final String mode;

    private final String cronUser;
    private final Boolean autoStart;
    private final String recipeLocation;
    private final String recipeName;

    private final String recipeFullPath;

    private final String envType;
    private final DatabaseEnvironmentsAdapter environmentsAdapter;

    private static final String blobStoreAddressSoap = ConfigurationProvider.getConfiguration().get("emucomp.blobstore_soap");
    private static final String blobStoreRestAddress = ConfigurationProvider.getConfiguration().get("rest.blobstore");

    private static final Logger LOG = Logger.getLogger("SWH-TASK");

    public HistoricBuildTask(HistoricRequest request, String envType, DatabaseEnvironmentsAdapter environmentsAdapter) throws BWFLAException {

        SoftwareHeritageRequest swhRequest = request.getSwhRequest();
        BuildToolchainRequest btcRequest = request.getBuildToolchainRequest();

        this.environmentID = btcRequest.getEnvironmentID();
        this.inputDirectory = btcRequest.getInputDirectory();
        this.outputDirectory = btcRequest.getOutputDirectory();
        this.recipeContent = btcRequest.getRecipe();
        this.prerequisites = btcRequest.getPrerequisites();
        this.mail = btcRequest.getMail();
        this.mode = btcRequest.getMode();

        //TODO incorporate these in execute()
        this.cronUser = btcRequest.getCronUser();
        this.autoStart = btcRequest.getAutoStart();
        this.recipeLocation = btcRequest.getRecipeLocation();
        this.recipeName = btcRequest.getRecipeName();

        this.recipeFullPath = Paths.get(this.inputDirectory).resolve(recipeName).toString();


        this.envType = envType;
        this.environmentsAdapter = environmentsAdapter;

        this.revisionId = swhRequest.getRevisionId();
        this.directoryId = swhRequest.getDirectoryId();
        this.shouldExtract = swhRequest.isExtract(); //TODO change getter for extract
        this.scriptLocation = swhRequest.getScriptLocation();
        this.workingDir = createWorkingDir();

    }

    @Override
    protected HistoricResponse execute() throws Exception {


        URL swhDataLocation = downloadAndStoreFromSoftwareHeritage();
        URL recipeLocation = publishRecipeData();
        URL cronLocation = publishCronTab();

        return prepareEnvironment(swhDataLocation, recipeLocation, cronLocation);
    }


    private HistoricResponse prepareEnvironment(URL dataLocation, URL recipeLocation, URL cronLocation) throws BWFLAException {

        if (envType.equals("base")) {

            //TODO check if error should be thrown when extract is true and envType is machine
            //TODO create Condition and pass it to injectData

            List<ImageModificationRequest> requestList = new ArrayList<>();

            ImageModificationRequest request = new ImageModificationRequest();
            ImageModificationCondition imageModificationCondition = new ImageModificationCondition();
            imageModificationCondition.getPaths().add(inputDirectory);
            request.setCondition(imageModificationCondition);
            request.setDataUrl(dataLocation.toString());
            request.setAction(ImageModificationAction.EXTRACT_TAR);
            request.setDestination(inputDirectory);

            requestList.add(request);

            request = new ImageModificationRequest();
            request.setCondition(imageModificationCondition);
            request.setDataUrl(recipeLocation.toString());
            request.setAction(ImageModificationAction.COPY);
            request.setDestination(this.recipeFullPath);

            requestList.add(request);

            if (this.autoStart) {
                request = new ImageModificationRequest();
                imageModificationCondition = new ImageModificationCondition();
                imageModificationCondition.getPaths().add("/etc/");
                request.setCondition(imageModificationCondition);
                request.setDataUrl(cronLocation.toString());
                request.setAction(ImageModificationAction.COPY);
                request.setDestination("/etc/crontab");

                requestList.add(request);
            }

            String envId = injectDataIntoImage(environmentID, requestList);

            HistoricResponse response = new HistoricResponse();
            response.setEnvironmentId(envId);
            return response;

        } else if (envType.equals("container")) {

            ContainerComponentRequest componentRequest = new ContainerComponentRequest();
            componentRequest.setEnvironment(environmentID);
            //TODO create mapping from swhPath -> inputFolder
            return null;
        } else {
            LOG.warning("Got unsupported environment type."); //TODO throw exception
            throw new BWFLAException("Got unsupported environment type.");
        }
    }

    private String injectDataIntoImage(String environmentId, List<ImageModificationRequest> requestList) throws BWFLAException {
        // in case we want to inject data into the file system
        // 1. we need to find the image (-> boot drive)
        // 2. modify the image
        // 3. import temp. environment
        // 4. call components with newEnvId
        // TODO: delete tmp environment if necessary
        // Ideally split this into a separate function / task
        String archive = "default"; // todo: request.getArchive();

        Environment chosenEnv = environmentsAdapter.getEnvironmentById(archive, environmentId);
        AbstractDataResource r = EmulationEnvironmentHelper.getBootDriveResource((MachineConfiguration) chosenEnv);
        if (!(r instanceof ImageArchiveBinding)) {
            throw new BWFLAException("Resource was not of type ImageArchiveBinding, can't inject data.");
        }

        String imageId = ((ImageArchiveBinding) r).getImageId();
        String newImageId = environmentsAdapter.injectData(imageId, requestList);
        ((ImageArchiveBinding) r).setImageId(newImageId);

        ImageArchiveMetadata md = new ImageArchiveMetadata();
        md.setType(ImageType.USER);
        return environmentsAdapter.importMetadata(archive, chosenEnv, md, false);
    }

    private URL downloadAndStoreFromSoftwareHeritage() throws Exception {
        try {

            ArrayList<String> arguments = new ArrayList<String>();
            arguments.add("/usr/bin/python3");
            arguments.add(scriptLocation); //TODO check if this needs to be configurable or hardcoded location
            String idToBeUsed;

            if (revisionId == null && directoryId == null) {
                throw new BWFLAException("Can't download without id (no revisionId and not directoryId given!");
            } else if (revisionId != null) {
                arguments.add(revisionId);
                idToBeUsed = revisionId;
            } else {
                arguments.add(directoryId);
                arguments.add("--dir");
                idToBeUsed = directoryId;
            }

            if (shouldExtract) {
                arguments.add("--extract");
            }

            final Path outputFolder = workingDir.resolve("pythonOutput");
            Files.createDirectory(outputFolder);

            arguments.add("-o");
            arguments.add(outputFolder.toString());

            String[] pythonCmds = arguments.toArray(String[]::new);

            LOG.info("Starting python script with args:");
            LOG.info(Arrays.toString(pythonCmds));
            ProcessBuilder processBuilder = new ProcessBuilder(pythonCmds);
            Process process = processBuilder.start();

            process.waitFor();

            if (process.exitValue() == 0) {
                if (shouldExtract) {
                    return null; //TODO this
                } else {
                    LOG.info("Storing file at " + outputFolder.toString());
                    //TODO delete file?
                    return publishSWHData(outputFolder);

                }

            } else {
                LOG.info("Some error occurred, trying to read stderr from python script...");
                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(process.getErrorStream()));
                String s;
                LOG.severe("Error in python script while downloading SWH Data: Printing python stderr:");
                while ((s = stdError.readLine()) != null) {
                    LOG.severe(s);
                }

                throw new BWFLAException("Could not download from SWH, exitValue was not 0.");
            }

        } catch (Exception error) {
            log.log(Level.WARNING, "Downloading Software from SWH failed!", error);
            throw error;
        }
    }

    private static URL publishSWHData(Path outputFolder) throws Exception {

        String swhDataFilePath = Objects.requireNonNull(outputFolder.toFile().list())[0];

        String fileName = swhDataFilePath.substring(0, swhDataFilePath.indexOf("."));
        LOG.info("Path" + swhDataFilePath);
        LOG.info("filename" + fileName);

        final BlobDescription blob = new BlobDescription()
                .setDescription("Software Heritage Code Archive")
                .setNamespace("swh-data")
                .setDataFromFile(outputFolder.resolve(swhDataFilePath))
                .setType(".tar.gz")
                .setName(fileName);

        URL blobURL = storeDataInBlobstore(blob);
        LOG.info("Stored SWH Data at:" + blobURL.toString());
        return blobURL;
    }

    private URL publishRecipeData() throws Exception {

        File recipe = workingDir.resolve(this.recipeName).toFile();
        FileWriter fileWriter = new FileWriter(recipe);
        fileWriter.write(this.recipeContent);
        fileWriter.close();

        final BlobDescription blob = new BlobDescription()
                .setDescription("Historic Builds Recipe")
                .setNamespace("historic-recipe")
                .setDataFromFile(workingDir.resolve(this.recipeName));

        if (this.recipeName.contains(".")) {
            String[] recipes = this.recipeName.split("\\.");
            String recipeName = recipes[0];
            String recipeType = recipes[1];
            LOG.info("RecipeName " + recipeName + " RecipeType " + recipeType);
            blob.setName(recipeName);
            blob.setType("." + recipeType);
        } else {
            blob.setName(this.recipeName);
        }

        URL blobURL = storeDataInBlobstore(blob);
        LOG.info("Stored recipe at:" + blobURL.toString());
        return blobURL;
    }

    private URL publishCronTab() throws Exception {

        File crontab = workingDir.resolve("crontab").toFile();
        FileWriter fileWriter = new FileWriter(crontab);
        fileWriter.append("SHELL=/bin/sh").append(System.getProperty("line.separator"))
                .append("PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin").append(System.getProperty("line.separator"))
                .append("@reboot root /bin/sh ").append(this.recipeFullPath).append(System.getProperty("line.separator")); //TODO always root?
        fileWriter.close();

        final BlobDescription blob = new BlobDescription()
                .setDescription("Historic Builds Crontab")
                .setNamespace("historic-crontab")
                .setDataFromFile(workingDir.resolve("crontab"))
                .setName(this.cronUser);

        URL blobURL = storeDataInBlobstore(blob);
        LOG.info("Stored crontab at:" + blobURL.toString());
        return blobURL;
    }

    private static URL storeDataInBlobstore(BlobDescription blob) throws Exception {
        BlobHandle handle = BlobStoreClient.get().getBlobStorePort(blobStoreAddressSoap).put(blob);
        return new URL(handle.toRestUrl(blobStoreRestAddress));
    }


    private static Path createWorkingDir() throws BWFLAException {
        try {
            return EaasFileUtils.createTempDirectory(Paths.get("/tmp-storage"), "historic-");
        } catch (Exception error) {
            throw new BWFLAException("Creating working directory failed!", error);
        }
    }
}
