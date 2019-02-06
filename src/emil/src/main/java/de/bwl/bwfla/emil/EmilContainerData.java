package de.bwl.bwfla.emil;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.api.imagebuilder.DockerImport;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderMetadata;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderResult;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.datatypes.EnvironmentDescription;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.common.utils.InputStreamDataSource;
import de.bwl.bwfla.configuration.converters.DurationPropertyConverter;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.WithPropertyConverter;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.naming.InitialContext;
import javax.xml.bind.JAXBException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

@Path("EmilContainerData")
@ApplicationScoped
public class EmilContainerData extends EmilRest {

    @Inject
    private DatabaseEnvironmentsAdapter envHelper;

    private List<DataSource> handlers = new ArrayList<>();


    @Inject
    @Config(value = "rest.blobstore")
    String blobStoreRestAddress;

    @Inject
    @Config(value = "ws.imagebuilder")
    String imageBuilderAddress;

    @Inject
    @Config(value = "ws.blobstore")
    private String blobStoreWsAddress;

    @Inject
    @Config("emil.inputpathtodelete")
    private String inputPathToDelete = null;

    @Inject
    @Config("emil.dockerTmpBuildFiles")
    private String dockerTmpBuildFiles = null;

    @Inject
    @Config(value = "emil.containerdata.imagebuilder.delay")
    @WithPropertyConverter(DurationPropertyConverter.class)
    private Duration imageBuilderDelay = null;

    @Inject
    @Config(value = "emil.containerdata.imagebuilder.timeout")
    @WithPropertyConverter(DurationPropertyConverter.class)
    private Duration imageBuilderTimeout = null;

    @Inject
    private EmilEnvironmentRepository emilEnvRepo;

    private ObjectArchiveHelper objHelper;

    private AsyncIoTaskManager taskManager;

    protected static final Logger LOG = Logger.getLogger("eaas/containerData");
    
    @PostConstruct
    private void initialize() {
        objHelper = new ObjectArchiveHelper(objectArchive);
        try {
            taskManager = new AsyncIoTaskManager();
        } catch (NamingException e) {
            throw new IllegalStateException("failed to create AsyncIoTaskManager");
        }
    }

    /**
     * Get List of available Runtimes
     * 
     * @return List of Container Runtimes
     */
    @Secured
    @GET
    @Path("/getOriginRuntimeList")
    @Produces(MediaType.APPLICATION_JSON)
    public RuntimeListResponse getOriginRuntimeList() {

        RuntimeListResponse resp = new RuntimeListResponse();       

        try {
            //TODO: archive backend, Containerhelper?
            ArrayList<RuntimeListItem> runtimes = new ArrayList<>();

            RuntimeListItem runc = new RuntimeListItem("0", "runc");
            RuntimeListItem docker = new RuntimeListItem("1", "docker");
            RuntimeListItem singularity = new RuntimeListItem("2", "singularity");
            runtimes.add(runc);
            runtimes.add(docker);
            runtimes.add(singularity);
            resp.setRuntimes(runtimes);

            return resp;
        } catch (Exception e) {
            throw new BadRequestException(Response
					.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorInformation(e.getMessage()))
					.build());
        }
    }

    @Secured
    @POST
    @Path("/updateContainer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateContainer(UpdateContainerRequest req)
    {

        final String curEnvId = req.getId();
        if(curEnvId == null)
            return Emil.errorMessageResponse("Id was null");

        EmilEnvironment newEmilEnv = emilEnvRepo.getEmilEnvironmentById(curEnvId);
        if(newEmilEnv == null) {
            return Emil.errorMessageResponse("No environment found with ID: " + curEnvId);
        }

        assert (newEmilEnv instanceof EmilContainerEnvironment);

        EmilContainerEnvironment env = (EmilContainerEnvironment)newEmilEnv;

        OciContainerConfiguration containerConf;
        try {
            containerConf = (OciContainerConfiguration) envHelper.getEnvironmentById(newEmilEnv.getArchive(), req.getId());
        } catch (BWFLAException e) {
            return Emil.internalErrorResponse(e);
        }

        EnvironmentDescription description = containerConf.getDescription();
        description.setTitle(req.getTitle());
        containerConf.setDescription(description);

        OciContainerConfiguration.Process process = new OciContainerConfiguration.Process();
        if(req.getProcessEnvs() != null && req.getProcessEnvs().size() > 0)
            process.setEnvironmentVariables(req.getProcessEnvs());
        process.setArguments(req.getProcessArgs());
        containerConf.setProcess(process);

        containerConf.setOutputPath(req.getOutputFolder());

        env.setTitle(req.getTitle());
        env.setInput(req.getInputFolder());
        env.setDescription(req.getDescription());
        env.setOutput(req.getOutputFolder());
        env.setArgs(req.getProcessArgs());
        env.setEnv(req.getProcessEnvs());
        env.setAuthor(req.getAuthor());

        try {
            emilEnvRepo.save(env, false);
            envHelper.updateMetadata(env.getArchive(), containerConf);
        } catch (BWFLAException e) {
            e.printStackTrace();
            return Emil.internalErrorResponse(e);
        }

        return Emil.successMessageResponse("update successful");
    }

    @Secured
    @POST
    @Path("/importContainer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse saveContainerImage(ImportContainerRequest req) {
        try {
            return new TaskStateResponse(taskManager.submitTask(new ImportContainerTask(req)));
        } catch (BWFLAException e) {
            return new TaskStateResponse(e);
        }
    }

    @Secured
    @POST
    @Path("/importEmulator")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse importEmulator(ImportEmulatorRequest req) {
        try {
            return new TaskStateResponse(taskManager.submitTask(new ImportContainerTask(req)));
        } catch (BWFLAException e) {
            return new TaskStateResponse(e);
        }
    }

    @Secured
    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(EnvironmentDeleteRequest desc) {
        if (desc.getEnvId() == null || desc.getEnvId().trim().isEmpty()) {
            return Emil.errorMessageResponse("Emil environment ID is null or empty");
        }

        try {
            emilEnvRepo.delete(desc.getEnvId(), desc.getDeleteMetaData(), desc.getDeleteImage());
        } catch ( JAXBException | BWFLAException e1) {
            return Emil.internalErrorResponse(e1);
        }

//        OciContainerConfiguration containerConf;
//        try {
//            containerConf = (OciContainerConfiguration) envHelper.getEnvironmentById(desc.getEnvId());
//        } catch (BWFLAException e) {
//            return Emil.internalErrorResponse(e);
//        }
//
//        try {
//            if (desc.getDeleteMetaData()) {
//                envHelper.deleteMetaData(desc.getEnvId());
//            }
//
//            if (desc.getDeleteImage()) {
//                for (AbstractDataResource b : containerConf.getDataResources()) {
//                    if (b instanceof ImageArchiveBinding && b.getId().equals("rootfs")) {
//                        ImageArchiveBinding iab = (ImageArchiveBinding) b;
//                        LOG.info("delete image: " + iab.getImageId());
//                        envHelper.deleteImage(iab.getImageId(), iab.getType());
//                    }
//                }
//            }
//        } catch (BWFLAException e) {
//            return Emil.internalErrorResponse(e);
//        }
        return Emil.successMessageResponse("delete success!");
    }


    @Secured
    @GET
    @Path("/taskState")
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse taskState(@QueryParam("taskId") String taskId) {
        final TaskInfo<Object> info = taskManager.getTaskInfo(taskId);
        if (info == null)
            return new TaskStateResponse(new BWFLAException("task not found"));

        if (!info.result().isDone())
            return new TaskStateResponse(taskId);

        try {
            Object o = info.result().get();
            TaskStateResponse response = new TaskStateResponse(taskId, true);

            if(o != null) {
                if(o instanceof BWFLAException)
                    return new TaskStateResponse((BWFLAException)o);
                if(o instanceof Map)
                    response.setUserData((Map<String,String>)o);
            }
            return response;

        } catch (InterruptedException | ExecutionException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return new TaskStateResponse(new BWFLAException(e));
        }
    }

    @Secured
    @POST
    @Path("/saveImportedContainer")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveImportedContainer(SaveImportedContainerRequest saveImpContainerReq) {
        try {
            emilEnvRepo.saveImportedContainer(saveImpContainerReq.getId(),
                    saveImpContainerReq.getTitle(),
                    saveImpContainerReq.getDescription(),
                    saveImpContainerReq.getAuthor());
        } catch (BWFLAException e1) {
            return Emil.internalErrorResponse(e1);
        }
        return Emil.successMessageResponse("save success!");
    }

    class AsyncIoTaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Object> {
        AsyncIoTaskManager() throws NamingException {
            super(InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
        }
    }

    class ImportContainerTask extends AbstractTask<Object>
    {
        private final ImportContainerRequest containerRequest;
        final ImageBuilder imagebuilder = ImageBuilderClient.get().getImageBuilderPort(imageBuilderAddress);
        private final ImageDescription description = new ImageDescription()
                .setMediumType(MediumType.HDD)
                .setPartitionTableType(PartitionTableType.NONE)
                .setFileSystemType(FileSystemType.EXT4)
                .setSizeInMb(1024 * 10); // 10 Gb virtual size

        ImportContainerTask(ImportContainerRequest containerRequest) throws BWFLAException {
            this.containerRequest = containerRequest;
        }

        ImageContentDescription getImageEntryFromUrlStr(String urlString) throws BWFLAException
        {
            ImageContentDescription entry;
            entry = new ImageContentDescription();
            try {
                entry.setURL(new URL(urlString));
            } catch (MalformedURLException e) {
                final String filename = urlString;
                if (filename.contains("/")) {
                    throw new BWFLAException("filename must not be null/empty or contain '/' characters:" + filename);
                }
                File archiveFile = new File("/eaas/import/", filename);
                if(!archiveFile.exists()) {
                    throw new BWFLAException("file " + filename + " not found in input folder");
                }
                entry.setDataFromFile(archiveFile.toPath());
            }
            return entry;
        }

        private ImageBuilderResult createImagefromArchiveFile(String srcUrlString) throws BWFLAException {

            ImageContentDescription entry = getImageEntryFromUrlStr(srcUrlString);

            entry.setAction(ImageContentDescription.Action.EXTRACT)
                    .setArchiveFormat(ImageContentDescription.ArchiveFormat.TAR);

            description.addContentEntry(entry);
            return this.createImageFromDescription(description);
        }

        private ImageBuilderResult createImagefromSingularityImg(String srcUrlString) throws BWFLAException
        {
            ImageContentDescription entry = getImageEntryFromUrlStr(srcUrlString);

            entry.setAction(ImageContentDescription.Action.EXTRACT)
                    .setArchiveFormat(ImageContentDescription.ArchiveFormat.SIMG);

            description.addContentEntry(entry);

            return this.createImageFromDescription(description);
        }

        private ImageBuilderResult createImageFromDockerHub(String dockerName, String tag) throws BWFLAException {
            ImageContentDescription entry = new ImageContentDescription();
            entry.setAction(ImageContentDescription.Action.RSYNC);
            ImageContentDescription.DockerDataSource dockerDataSource
                    = new ImageContentDescription.DockerDataSource(dockerName, tag);
            dockerDataSource.imageArchiveHost = envHelper.getImageArchiveHost();
            entry.setDataFromDockerSource(dockerDataSource);
            description.addContentEntry(entry);
            ImageBuilderResult result = this.createImageFromDescription(description);

            return result;
        }

        private ImageBuilderResult createImageFromDescription(ImageDescription description) throws BWFLAException
        {
            return ImageBuilderClient.build(imagebuilder, description, imageBuilderTimeout, imageBuilderDelay);
        }

        @Override
        protected Object execute() throws Exception {
            if(containerRequest.getUrlString() == null && !(containerRequest instanceof ImportEmulatorRequest)) {
                return new BWFLAException("invalid url: " + containerRequest.getUrlString());
            }
            if((containerRequest.getProcessArgs() == null || containerRequest.getProcessArgs().size() == 0) && !(containerRequest instanceof ImportEmulatorRequest)) {
                return new BWFLAException("missing process args");
            }

            if(containerRequest.getImageType() == null)
                return new BWFLAException("missing image type");

            ImageBuilderResult result = null;
            URL imageUrl = null;

            try {
                switch(containerRequest.getImageType())
                {
                    case ROOTFS:
                        result = createImagefromArchiveFile(containerRequest.getUrlString());
                        break;
                    case SIMG:
                        result = createImagefromSingularityImg(containerRequest.getUrlString());
                        break;
                    case DOCKERHUB:
                        result = createImageFromDockerHub(containerRequest.getUrlString(), containerRequest.getTag());
                        break;
                    case READYMADE:
                        imageUrl = new URL(containerRequest.getUrlString());
                        break;

                    default:
                        throw new BWFLAException("unkonwn imageType " + containerRequest.getImageType());
                }

            } catch (BWFLAException e) {
                e.printStackTrace();
                return e;
            }

            ImageArchiveMetadata meta = new ImageArchiveMetadata();
            if(containerRequest instanceof ImportEmulatorRequest)
                meta.setType(ImageType.BASE);
            else
                meta.setType(ImageType.TMP);

            if (imageUrl == null)
                if(result.getBlobHandle() == null)
                    throw new BWFLAException("Image blob unavailable");
                try {
                    imageUrl = new URL(result.getBlobHandle().toRestUrl(blobStoreRestAddress, false));
                } catch (MalformedURLException e) {
                    return new BWFLAException(e);
                }

            EnvironmentsAdapter.ImportImageHandle importState = null;
            ImageArchiveBinding binding;
            try {
                if (containerRequest instanceof ImportEmulatorRequest)
                    importState = envHelper.importImage("emulators", imageUrl, meta, true);
                else
                    importState = envHelper.importImage("default", imageUrl, meta, true);

                binding = importState.getBinding(60 * 60 * 60); //wait an hour
            }
            catch (BWFLAException e) {
                e.printStackTrace();
                return e;
            }

            if (containerRequest instanceof ImportEmulatorRequest) {
                ImportEmulatorRequest emulatorRequest = (ImportEmulatorRequest) containerRequest;

                de.bwl.bwfla.api.imagearchive.ImageDescription iD = new de.bwl.bwfla.api.imagearchive.ImageDescription();
                iD.setType(meta.getType().value());
                iD.setId(binding.getImageId());
                iD.setFstype(emulatorRequest.getFstype());

                Entry entry = new Entry();
                entry.setName(emulatorRequest.getEmulatorType());
                entry.setVersion(emulatorRequest.getVersion());
                entry.setImage(iD);

                Alias alias = new Alias();
                alias.setName(emulatorRequest.getEmulatorType());
                alias.setVersion(emulatorRequest.getVersion());
                alias.setAlias(emulatorRequest.getAlias());

                if(result.getMetadata() != null)
                {
                    ImageBuilderMetadata md = result.getMetadata();
                    if(md instanceof DockerImport)
                    {
                        DockerImport dockerMd = (DockerImport)md;
                        Provenance pMd = new Provenance();
                        pMd.getLayers().addAll(dockerMd.getLayers());
                        pMd.setOciSourceUrl("docker://" + dockerMd.getImageRef());
                        pMd.setVersionTag(dockerMd.getTag());
                        entry.setProvenance(pMd);
                    }
                }

                envHelper.addNameIndexesEntry("emulators", entry, alias);
                return new HashMap<>();
            }
            binding.setId("rootfs");
            binding.setFileSystemType("ext4");

            OciContainerConfiguration config = new OciContainerConfiguration();
            EnvironmentDescription description = new EnvironmentDescription();
            description.setTitle(containerRequest.getName());
            config.setDescription(description);
            config.getDataResources().add(binding);

            config.setGui(containerRequest.guiRequired());

            config.setOutputPath(containerRequest.getOutputFolder());
            config.setInputPath(containerRequest.getInputFolder());
            config.setRootFilesystem("binding://rootfs");

            OciContainerConfiguration.Process process = new OciContainerConfiguration.Process();
            if(containerRequest.getProcessEnvs() != null && containerRequest.getProcessEnvs().size() > 0)
                process.setEnvironmentVariables(containerRequest.getProcessEnvs());
            process.setArguments(containerRequest.getProcessArgs());
            config.setProcess(process);

            config.setId("dummy");

            log.warning(config.toString());
            String newEnvironmentId = null;
            try {
                LOG.info("importing config: " + config.toString());
                newEnvironmentId = envHelper.importMetadata("default", config, meta, false);
            } catch (BWFLAException e) {
                e.printStackTrace();
                return e;
            }
            
            Map<String, String> userData = new HashMap<>();

            userData.put("environmentId", newEnvironmentId);
            return userData;
        }
    }

    @Secured
    @POST
    @Path("/uploadUserInput")
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public UploadFileResponse upload(MultipartFormDataInput input) {
        String fileName = null;
        String objectId = null;
        String mediaType = null;
        InputStream inputStream = null;

        try {
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            List<InputPart> inputPartsFiles = uploadForm.get("file");
            List<InputPart> metadataPart = uploadForm.get("mediaType");
            List<InputPart> objectIdPart = uploadForm.get("objectId");

            if (inputPartsFiles == null)
                return new UploadFileResponse(new BWFLAException("invalid form data"));

            if (inputPartsFiles.size() > 1) {
                return new UploadFileResponse(new BWFLAException("we currently support only one file at the time"));
            }

            for (InputPart inputPart : inputPartsFiles) {
                try {
                    MultivaluedMap<String, String> header = inputPart.getHeaders();
                    fileName = getFileName(header);
                    inputStream = inputPart.getBody(InputStream.class, null);
                } catch (IOException e) {
                    return new UploadFileResponse(new BWFLAException(e));
                }
            }

            final BlobDescription blob = new BlobDescription()
                    .setDescription("ImageBuilder image")
                    .setNamespace("imagebuilder-outputs")
                    .setData(new DataHandler(new InputStreamDataSource(inputStream)))
                    .setName("userTempFile")
                    .setType(".temp");


            BlobHandle handle = BlobStoreClient.get()
                    .getBlobStorePort(blobStoreWsAddress)
                    .put(blob);


            UploadFileResponse response = new UploadFileResponse();
            response.setUserDataUrl(handle.toRestUrl(blobStoreRestAddress));
            return response;
        } catch (BWFLAException e) {
            e.printStackTrace();
            return new UploadFileResponse(new BWFLAException(e));
        }

    }

    private static String partToString(List<InputPart> partList) throws BWFLAException, IOException {
        if(partList.size() == 0)
            throw new BWFLAException("partList empty");

        InputPart part = partList.get(0);
        return part.getBodyAsString();

    }

    private String getFileName(MultivaluedMap<String, String> header) {

        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

}
