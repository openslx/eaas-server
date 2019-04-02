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
import de.bwl.bwfla.emil.utils.ContainerUtil;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.ConfigurationProvider;
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

    @Inject
    private ContainerUtil containerUtil;

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
    private EmilEnvironmentRepository emilEnvRepo;

    private ObjectArchiveHelper objHelper;

    private AsyncIoTaskManager taskManager;

    protected static final Logger LOG = Logger.getLogger("eaas/containerData");

    private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

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
    @Path("/updateLatestEmulator")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateLatestEmulator(UpdateLatestEmulatorRequest request) throws BWFLAException {
        envHelper.updateLatestEmulator(getEmulatorArchive(), request.getEmulatorName(), request.getVersion());
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
        } catch (BWFLAException e1) {
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


        ImportContainerTask(ImportContainerRequest containerRequest) throws BWFLAException {
            this.containerRequest = containerRequest;
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


            if (containerRequest instanceof ImportEmulatorRequest) {

                ImportEmulatorRequest request = (ImportEmulatorRequest)containerRequest;
                containerUtil.importEmulator(request);
                return new HashMap<>();
            }
            else
            {
                String newEnvironmentId = containerUtil.importContainer(containerRequest);
                Map<String, String> userData = new HashMap<>();
                envHelper.sync();
                userData.put("environmentId", newEnvironmentId);
                return userData;
            }
        }
    }

    private String getEmulatorArchive() {
        String archive = ConfigurationProvider.getConfiguration().get("emucomp.emulator_archive");
        if(archive == null || archive.isEmpty())
            return EMULATOR_DEFAULT_ARCHIVE;
        return archive;
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
