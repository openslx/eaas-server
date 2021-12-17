package de.bwl.bwfla.emil;


import java.util.*;
import java.util.logging.Logger;


import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.util.EmulatorMetaHelperV2;
import de.bwl.bwfla.common.datatypes.EnvironmentDescription;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.rest.ErrorInformation;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.emil.tasks.BuildContainerImageTask;
import de.bwl.bwfla.emil.tasks.ImportEmulatorTask;
import de.bwl.bwfla.emil.utils.TaskManager;
import de.bwl.bwfla.emil.tasks.ImportContainerTask;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.inject.api.Config;

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
    private TaskManager taskManager;

    @Inject
    private EmilEnvironmentRepository emilEnvRepo;

    @Inject
	@AuthenticatedUser
	private UserContext authenticatedUser = null;

    private ObjectArchiveHelper objHelper;

    private EmulatorMetaHelperV2 emuMetaHelper;

    protected static final Logger LOG = Logger.getLogger("eaas/containerData");

    @PostConstruct
    private void initialize() {
        objHelper = new ObjectArchiveHelper(objectArchive);
        emuMetaHelper = new EmulatorMetaHelperV2(emilEnvRepo.getImageArchive(), LOG);
    }

    /**
     * Get List of available Runtimes
     * 
     * @return List of Container Runtimes
     */
    @Secured(roles={Role.PUBLIC})
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

    @Secured(roles={Role.RESTRICTED})
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
            containerConf = (OciContainerConfiguration) emilEnvRepo.getImageArchive()
                    .api()
                    .v2()
                    .containers()
                    .fetch(req.getId());

            EnvironmentDescription description = containerConf.getDescription();
            description.setTitle(req.getTitle());
            containerConf.setDescription(description);

            OciContainerConfiguration.Process process = new OciContainerConfiguration.Process();
            if (req.getProcessEnvs() != null && req.getProcessEnvs().size() > 0)
                process.setEnvironmentVariables(req.getProcessEnvs());
            process.getArguments().addAll(req.getProcessArgs());
            containerConf.setProcess(process);

            containerConf.setOutputPath(req.getOutputFolder());
            containerConf.setInputPath(req.getInputFolder());
            
            EmilContainerEnvironment newEnv = new EmilContainerEnvironment();
            if (!env.getArchive().equals("default")) {
                // we need to import / duplicate the env
                final var id = emilEnvRepo.getImageArchive()
                        .api()
                        .v2()
                        .containers()
                        .insert(containerConf);

                newEnv.setArchive("default");
                newEnv.setEnvId(id);
                newEnv.setParentEnvId(env.getEnvId());
                env.addChildEnvId(newEnv.getEnvId());
                newEnv.setDigest(env.getDigest());
                imported = true;
            } else {
                final var options = new ReplaceOptionsV2()
                        .setLocation(env.getArchive());

                emilEnvRepo.getImageArchive()
                        .api()
                        .v2()
                        .containers()
                        .replace(containerConf.getId(), containerConf, options);

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

    @Secured(roles={Role.RESTRICTED})
    @POST
    @Path("/buildContainerImage")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse saveContainerImage(CreateContainerImageRequest req) {
        return new TaskStateResponse(taskManager.submitTask(new BuildContainerImageTask(req, emilEnvRepo)));

    }

    @Deprecated
    @Secured(roles={Role.RESTRICTED})
    @POST
    @Path("/updateLatestEmulator")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateLatestEmulator(UpdateLatestEmulatorRequest request) throws BWFLAException {
        emuMetaHelper.markAsDefault(request.getEmulatorName(), request.getVersion());
    }

    @Secured(roles={Role.RESTRICTED})
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
            e1.printStackTrace();
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

    @Secured(roles={Role.RESTRICTED})
    @POST
    @Path("/importContainer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse importContainer(ImportContainerRequest req) {

        final var userCtx = (authenticatedUser != null) ? authenticatedUser.clone() : new UserContext();
        return new TaskStateResponse(taskManager.submitTask(new ImportContainerTask(req, emilEnvRepo, userCtx)));
    }

    @Secured(roles={Role.RESTRICTED})
    @POST
    @Path("/importEmulator")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse importEmulator(ImportEmulatorRequest req) {
        return new TaskStateResponse(taskManager.submitTask(new ImportEmulatorTask(req, emuMetaHelper, envHelper)));
    }
}
