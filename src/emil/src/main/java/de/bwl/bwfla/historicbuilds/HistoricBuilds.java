package de.bwl.bwfla.historicbuilds;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.common.taskmanager.TaskManager;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.envproposer.EnvironmentProposer;
import de.bwl.bwfla.envproposer.api.ProposalRequest;
import de.bwl.bwfla.historicbuilds.api.BuildToolchainRequest;
import de.bwl.bwfla.historicbuilds.api.HistoricRequest;
import de.bwl.bwfla.historicbuilds.api.HistoricResponse;
import de.bwl.bwfla.historicbuilds.api.SoftwareHeritageRequest;
import de.bwl.bwfla.historicbuilds.impl.BuildToolchainTask;
import de.bwl.bwfla.historicbuilds.impl.SoftwareHeritageTask;
import de.bwl.bwfla.restutils.ResponseUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/historic-builds/api/v1")
public class HistoricBuilds {

    private static final Logger LOG = Logger.getLogger("HISTORIC-BUILDS");

    //TODO inject TaskManager?
    private final TaskManager taskmgr;


    @Inject
    private EmilEnvironmentRepository emilEnvRepo = null;

    //TODO is this needed?
    @Inject
    @AuthenticatedUser
    private UserContext userCtx;

    public HistoricBuilds() throws BWFLAException {
        try {
            this.taskmgr = new TaskManager();
        } catch (Exception error) {
            throw new BWFLAException("Initializing HistoricBuilds failed!", error);
        }
    }

    @GET
    @Path("/hello")
    @Secured(roles = {Role.PUBLIC})
    @Produces(MediaType.TEXT_PLAIN)
    public Response historicHello() {
        LOG.info("Someone sent a hello request to the historic build API.");
        return ResponseUtils.createResponse(Status.OK, "Hello from the historic builds API!");
    }

    @POST
    @Path("/build")
    @Secured(roles = {Role.PUBLIC})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postBuild(HistoricRequest request) {

        SoftwareHeritageRequest swhRequest = request.getSwhRequest();
        BuildToolchainRequest buildToolchainRequest = request.getBuildToolchainRequest();


        //final String swhTaskID = taskmgr.submit(new SoftwareHeritageTask(swhRequest));
        //final String buildTCTaskID = taskmgr.submit(new BuildToolchainTask(buildToolchainRequest, userCtx.getUserId()));

        LOG.info("Someone sent a build request to the historic build API: returning incoming json!");
        if (swhRequest != null) {
            LOG.info("Revision ID:" + swhRequest.getRevisionId());
        }

        EmilEnvironment emilEnv = emilEnvRepo.getEmilEnvironmentById(buildToolchainRequest.getEmulatorID());

        return ResponseUtils.createResponse(Status.OK, emilEnv);
    }

    private static class TaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Object> {
        public TaskManager() throws NamingException {
            //TODO is this fine?
            super("HISTORIC-BUILDS-TASKS", InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
        }
    }

}
