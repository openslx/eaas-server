package de.bwl.bwfla.historicbuilds.impl;

import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.ComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.ContainerComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.MachineComponentRequest;
import de.bwl.bwfla.historicbuilds.api.*;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import org.apache.jena.atlas.logging.Log;
import org.apache.tamaya.ConfigurationProvider;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

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
    private final String execFile;
    private final String[] prerequisites;
    private final String mail;
    private final String mode;

    private final String envType;
    private final DatabaseEnvironmentsAdapter environmentsAdapter;

    private static final Logger LOG = Logger.getLogger("SWH-TASK");

    public HistoricBuildTask(HistoricRequest request, String envType, DatabaseEnvironmentsAdapter environmentsAdapter) throws BWFLAException {

        SoftwareHeritageRequest swhRequest = request.getSwhRequest();
        BuildToolchainRequest btcRequest = request.getBuildToolchainRequest();

        this.environmentID = btcRequest.getEnvironmentID();
        this.inputDirectory = btcRequest.getInputDirectory();
        this.outputDirectory = btcRequest.getOutputDirectory();
        this.execFile = btcRequest.getExecFile();
        this.prerequisites = btcRequest.getPrerequisites();
        this.mail = btcRequest.getMail();
        this.mode = btcRequest.getMode();

        this.envType = envType;

        this.environmentsAdapter = environmentsAdapter;


        this.revisionId = swhRequest.getRevisionId();
        this.directoryId = swhRequest.getDirectoryId();
        this.shouldExtract = swhRequest.isExtract(); //TODO change getter for extract
        this.scriptLocation = swhRequest.getScriptLocation();
        this.workingDir = createWorkingDir();

    }

    //FIXME Use other class or make response class common
    @Override
    protected ComponentRequest execute() throws Exception {
        URL swhDataLocation = downloadAndStoreFromSoftwareHeritage();
        return prepareEnvironment(swhDataLocation);
    }

    private ComponentRequest prepareEnvironment(URL swhDataLocation) throws BWFLAException {

        if (envType.equals("base")) {
            MachineComponentRequest componentRequest = new MachineComponentRequest();


            //TODO check if error should be thrown when extract is true and envType is machine

            //TODO create Condition and pass it to injectData

            String environmentWithInjectID = environmentsAdapter.injectData(environmentID, null, swhDataLocation.toString());
            componentRequest.setEnvironment(environmentWithInjectID);
            return componentRequest;

        } else if (envType.equals("container")) {

            ContainerComponentRequest componentRequest = new ContainerComponentRequest();
            componentRequest.setEnvironment(environmentID);
            //TODO create mapping from swhPath -> inputFolder
            return componentRequest;
        } else {
            LOG.warning("Got unsupported environment type."); //TODO throw exception
            throw new BWFLAException("Got unsupported environment type.");
        }
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

            } else { //TODO give better information (access python script output?)
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

        BlobHandle handle = null;

        String blobStoreAddressSoap = ConfigurationProvider.getConfiguration().get("emucomp.blobstore_soap");
        String blobStoreRestAddress = ConfigurationProvider.getConfiguration().get("rest.blobstore");

        final BlobDescription blob = new BlobDescription()
                .setDescription("Software Heritage Code Archive")
                .setNamespace("swh-data")
                .setDataFromFile(outputFolder.resolve(swhDataFilePath))
                .setType(".tar.gz")
                .setName(fileName);
        //.setName(Paths.get(swhDataFilePath).getFileName().toString());
        handle = BlobStoreClient.get().getBlobStorePort(blobStoreAddressSoap).put(blob);
        URL blobURL = new URL(handle.toRestUrl(blobStoreRestAddress));
        LOG.info("Stored SWH Data at:" + blobURL.toString());
        return blobURL;
    }


    private static Path createWorkingDir() throws BWFLAException {
        try {
            return EaasFileUtils.createTempDirectory(Paths.get("/tmp-storage"), "historic-");
        } catch (Exception error) {
            throw new BWFLAException("Creating working directory failed!", error);
        }
    }
}
