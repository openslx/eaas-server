package de.bwl.bwfla.emil;


import java.util.*;
import java.util.logging.Logger;


import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import de.bwl.bwfla.api.imagearchive.ImageArchiveMetadata;
import de.bwl.bwfla.api.imagearchive.ImageType;
import de.bwl.bwfla.common.datatypes.EnvironmentDescription;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.emil.datatypes.security.Role;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.utils.ContainerUtil;
import de.bwl.bwfla.emil.utils.TaskManager;
import de.bwl.bwfla.emil.tasks.ImportContainerTask;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;

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
    private TaskManager taskManager;

    @Inject
    private EmilEnvironmentRepository emilEnvRepo;

    private ObjectArchiveHelper objHelper;

    protected static final Logger LOG = Logger.getLogger("eaas/containerData");

    private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

    @PostConstruct
    private void initialize() {
        objHelper = new ObjectArchiveHelper(objectArchive);
    }

    /**
     * Get List of available Runtimes
     * 
     * @return List of Container Runtimes
     */
    @Secured({Role.PUBLIC})
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

    @Secured({Role.RESTRCITED})
    @POST
    @Path("/updateContainer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateContainer(UpdateContainerRequest req) {
        boolean imported = false;
        final String curEnvId = req.getId();
        if (curEnvId == null)
            return Emil.errorMessageResponse("Id was null");

        EmilEnvironment newEmilEnv = emilEnvRepo.getEmilEnvironmentById(curEnvId);
        if (newEmilEnv == null) {
            return Emil.errorMessageResponse("No environment found with ID: " + curEnvId);
        }

        assert (newEmilEnv instanceof EmilContainerEnvironment);

        EmilContainerEnvironment env = (EmilContainerEnvironment) newEmilEnv;

        OciContainerConfiguration containerConf;
        try {
            containerConf = (OciContainerConfiguration) envHelper.getEnvironmentById(newEmilEnv.getArchive(), req.getId());
            EnvironmentDescription description = containerConf.getDescription();
            description.setTitle(req.getTitle());
            containerConf.setDescription(description);

            OciContainerConfiguration.Process process = new OciContainerConfiguration.Process();
            if (req.getProcessEnvs() != null && req.getProcessEnvs().size() > 0)
                process.setEnvironmentVariables(req.getProcessEnvs());
            process.setArguments(req.getProcessArgs());
            containerConf.setProcess(process);

            containerConf.setOutputPath(req.getOutputFolder());
            EmilContainerEnvironment newEnv = new EmilContainerEnvironment();
            if (!env.getArchive().equals("default")) {
                // we need to import / duplicate the env
                ImageArchiveMetadata md = new ImageArchiveMetadata();
                md.setType(ImageType.USER);
                newEnv.setArchive("default");
                String id = envHelper.importMetadata("default", containerConf, md, false);
                newEnv.setEnvId(id);
                newEnv.setParentEnvId(env.getEnvId());
                env.addChildEnvId(newEnv.getEnvId());
                imported = true;
            } else {
                envHelper.updateMetadata(env.getArchive(), containerConf);
                newEnv = env;
            }


            newEnv.setTitle(req.getTitle());
            newEnv.setInput(req.getInputFolder());
            newEnv.setDescription(req.getDescription());
            newEnv.setOutput(req.getOutputFolder());
            newEnv.setArgs(req.getProcessArgs());
            newEnv.setEnv(req.getProcessEnvs());
            newEnv.setAuthor(req.getAuthor());
            newEnv.setRuntimeId(req.getContainerRuntimeId());
            newEnv.setNetworking(req.getNetworking());


            if(imported) {
                // emilEnvRepo.save(currentEnv, false);
                emilEnvRepo.save(newEnv, true);
            }
            else
                emilEnvRepo.save(newEnv, false);

        } catch (BWFLAException e) {
            e.printStackTrace();
            return Emil.internalErrorResponse(e);
        }

        return Emil.successMessageResponse("update successful");
    }

    @Secured({Role.RESTRCITED})
    @POST
    @Path("/importContainer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse saveContainerImage(ImportContainerRequest req) {
        return new TaskStateResponse(taskManager.submitTask(new ImportContainerTask(req, containerUtil, envHelper)));
    }

    @Secured({Role.RESTRCITED})
    @POST
    @Path("/importEmulator")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse importEmulator(ImportEmulatorRequest req) {
        return new TaskStateResponse(taskManager.submitTask(new ImportContainerTask(req, containerUtil, envHelper)));
    }

    @Secured({Role.RESTRCITED})
    @POST
    @Path("/updateLatestEmulator")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateLatestEmulator(UpdateLatestEmulatorRequest request) throws BWFLAException {
        envHelper.updateLatestEmulator(getEmulatorArchive(), request.getEmulatorName(), request.getVersion());
    }

    @Secured({Role.RESTRCITED})
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

    @Secured({Role.RESTRCITED})
    @POST
    @Path("/saveImportedContainer")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveImportedContainer(SaveImportedContainerRequest saveImpContainerReq) {
        try {
            emilEnvRepo.saveImportedContainer(saveImpContainerReq);
        } catch (BWFLAException e1) {
            return Emil.internalErrorResponse(e1);
        }
        return Emil.successMessageResponse("save success!");
    }

    private String getEmulatorArchive() {
        String archive = ConfigurationProvider.getConfiguration().get("emucomp.emulator_archive");
        if(archive == null || archive.isEmpty())
            return EMULATOR_DEFAULT_ARCHIVE;
        return archive;
    }

}
