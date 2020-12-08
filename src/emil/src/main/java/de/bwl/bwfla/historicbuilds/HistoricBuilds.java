package de.bwl.bwfla.historicbuilds;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.common.taskmanager.TaskManager;
import de.bwl.bwfla.emil.Components;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.EnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.rest.EnvironmentDetails;
import de.bwl.bwfla.envproposer.EnvironmentProposer;
import de.bwl.bwfla.envproposer.api.ProposalResponse;
import de.bwl.bwfla.envproposer.impl.UserData;
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
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/historic-builds/api/v1")
public class HistoricBuilds {

    private static final Logger LOG = Logger.getLogger("HISTORIC-BUILDS");

    //TODO inject TaskManager?
    private final TaskManager taskmgr;

    @Inject
    private Components components = null;

    @Inject
    private EmilEnvironmentRepository emilEnvRepo = null;

    @Inject
    EnvironmentRepository environmentRepo = null;

    //TODO is this needed here?
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
    public Response postBuild(HistoricRequest request, @Context UriInfo uri) {

        LOG.info("Someone sent a build request to the historic build API.");

        SoftwareHeritageRequest swhRequest = request.getSwhRequest();
        BuildToolchainRequest buildToolchainRequest = request.getBuildToolchainRequest();

        EmilEnvironment emilEnv = emilEnvRepo.getEmilEnvironmentById(buildToolchainRequest.getEnvironmentID());
        String envType;
        try {
            EnvironmentDetails envDetails = environmentRepo.environments().addEnvironmentDetails(emilEnv);
            envType = envDetails.getEnvType();
        } catch (BWFLAException e) {
            return ResponseUtils.createInternalErrorResponse(e);
        }

        final String swhTaskID;
        final String btcTaskID;
        try {
            swhTaskID = taskmgr.submit(new SoftwareHeritageTask(swhRequest));
            btcTaskID = taskmgr.submit(new BuildToolchainTask(buildToolchainRequest, components, swhTaskID, taskmgr, envType));
            //TODO nicht beide Tasks gleichzeitig starten, sondern swhTask started btc, wenn fertig?
        } catch (Throwable throwable) {
            LOG.log(Level.WARNING, "Starting a Task failed!", throwable);
            return ResponseUtils.createInternalErrorResponse(throwable);
        }

        final String swhWaitLocation = HistoricBuilds.getLocationUrl(uri, "waitqueue", swhTaskID);
        final String swhResultLocation = HistoricBuilds.getLocationUrl(uri, "buildresult", swhTaskID);
        final TaskInfo<Object> swhInfo = taskmgr.lookup(swhTaskID);
        swhInfo.setUserData(new UserData(swhWaitLocation, swhResultLocation));

        final String btcWaitLocation = HistoricBuilds.getLocationUrl(uri, "waitqueue", btcTaskID);
        final String btcResultLocation = HistoricBuilds.getLocationUrl(uri, "buildresult", btcTaskID);
        final TaskInfo<Object> btcInfo = taskmgr.lookup(btcTaskID);
        btcInfo.setUserData(new UserData(btcWaitLocation, btcResultLocation));

        final HistoricResponse response = new HistoricResponse();
        response.setMessage("Started 2 Tasks successfully.");
        response.setSwhTaskId(swhTaskID);
        response.setBuildToolchainTaskId(btcTaskID);

        return ResponseUtils.createLocationResponse(Status.ACCEPTED, btcWaitLocation, response);
    }

    @GET
    @Path("/waitqueue/{id}")
    @Secured(roles = {Role.PUBLIC})
    @Produces(MediaType.APPLICATION_JSON)
    public Response poll(@PathParam("id") String id) {
        try {
            final TaskInfo<Object> info = taskmgr.lookup(id);
            if (info == null) {
                String message = "Passed ID is invalid: " + id;
                return ResponseUtils.createMessageResponse(Status.NOT_FOUND, message);
            }

            Status status = null;
            String location = null;

            final UserData userdata = info.userdata(UserData.class);
            if (info.result().isDone()) {
                // Result is available!
                status = Status.SEE_OTHER;
                location = userdata.getResultLocation();
            } else {
                // Result is not yet available!
                status = Status.OK;
                location = userdata.getWaitLocation();
            }

            return ResponseUtils.createLocationResponse(status, location, null);
        } catch (Throwable throwable) {
            return ResponseUtils.createInternalErrorResponse(throwable);
        }
    }

    @GET
    @Path("/buildresult/{id}")
    @Secured(roles = {Role.PUBLIC})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildResult(@PathParam("id") String id) {
        try {
            if (id == null || id.isEmpty()) {
                String message = "ID was not specified or is invalid!";
                return ResponseUtils.createMessageResponse(Status.BAD_REQUEST, message);
            }

            final TaskInfo<Object> info = taskmgr.lookup(id);
            if (info == null || !info.result().isDone()) {
                String message = "Passed ID is invalid: " + id;
                return ResponseUtils.createMessageResponse(Status.NOT_FOUND, message);
            }

            try {
                // Result is available!
                final Future<Object> future = info.result();
                return ResponseUtils.createResponse(Status.OK, future.get());
            } finally {
                taskmgr.remove(id);
            }
        } catch (Throwable throwable) {
            return ResponseUtils.createInternalErrorResponse(throwable);
        }
    }

    // ========== Internal Helpers ====================

    private static String getLocationUrl(UriInfo uri, String subres, String id) {
        return ResponseUtils.getLocationUrl(HistoricBuilds.class, uri, subres, id);
    }

    private static class TaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Object> {
        public TaskManager() throws NamingException {
            //TODO is this fine?
            super("HISTORIC-BUILDS-TASKS", InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
        }
    }

}
