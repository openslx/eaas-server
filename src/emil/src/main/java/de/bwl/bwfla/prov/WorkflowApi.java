package de.bwl.bwfla.prov;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.EnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.rest.EnvironmentDetails;
import de.bwl.bwfla.envproposer.impl.UserData;
import de.bwl.bwfla.prov.api.WorkflowRequest;
import de.bwl.bwfla.prov.impl.ExecuteEnvForWorkflowTask;
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
@Path("/workflow/api/v1")
public class WorkflowApi {


    private static final Logger LOG = Logger.getLogger("HISTORIC-BUILDS");

    //TODO inject TaskManager?
    private final TaskManager taskmgr;

    @Inject
    private EmilEnvironmentRepository emilEnvRepo = null;

    @Inject
    EnvironmentRepository environmentRepo = null;

    @Inject
    private DatabaseEnvironmentsAdapter environmentsAdapter;

    public WorkflowApi() throws BWFLAException {
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
        LOG.info("Someone sent a hello request to the  workflow API!");
        return ResponseUtils.createResponse(Status.OK, "Hello from the workflow API!");
    }


    @POST
    @Path("/workflow")
    @Secured(roles = {Role.PUBLIC})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postBuild(WorkflowRequest request, @Context UriInfo uri) {

        LOG.info("Someone sent a build request to the historic build API.");

        String envId = request.getEnvironmentId();
        String[] urls = request.getInputFiles();

//        EmilEnvironment emilEnv = emilEnvRepo.getEmilEnvironmentById(envId);

        final String taskID;
        try {
            taskID = taskmgr.submit(new ExecuteEnvForWorkflowTask(envId, urls, environmentsAdapter));
        } catch (Throwable throwable) {
            LOG.log(Level.WARNING, "Starting the Task failed!", throwable);
            return ResponseUtils.createInternalErrorResponse(throwable);
        }

        final String waitLocation = WorkflowApi.getLocationUrl(uri, "waitqueue", taskID);
        final String resultLocation = WorkflowApi.getLocationUrl(uri, "buildresult", taskID);
        final TaskInfo<Object> swhInfo = taskmgr.lookup(taskID);
        swhInfo.setUserData(new UserData(waitLocation, resultLocation));

        final HistoricBuildStartedResponse response = new HistoricBuildStartedResponse();
        response.setTaskId(taskID);
        response.setWaitQueueUrl(waitLocation);

        return ResponseUtils.createLocationResponse(Status.ACCEPTED, waitLocation, response);
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

            Status status = Status.OK;
            final UserData userdata = info.userdata(UserData.class);

            HistoricWaitqueueResponse response = new HistoricWaitqueueResponse();
            response.setId(id);
            response.setResultUrl(userdata.getResultLocation());

            if (info.result().isDone()) {
                // Result is available!
                response.setStatus("Done"); //TODO remove deprecated string
                response.setDone(true);
            } else {
                // Result is not yet available!
                response.setStatus("Processing"); //TODO remove deprecated string
                response.setDone(false);
            }
            //TODO check for errors and pass error flag to response

            return ResponseUtils.createResponse(status, response);
        } catch (Throwable throwable) {
            return ResponseUtils.createInternalErrorResponse(throwable);
        }
    }

    @GET
    @Path("/workflowresult/{id}")
    @Secured(roles = {Role.PUBLIC})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildResult(@PathParam("id") String id) {
        try {
            if (id == null || id.isEmpty()) {
                String message = "ID was not specified or is invalid!";
                return ResponseUtils.createMessageResponse(Status.BAD_REQUEST, message);
            }

            final TaskInfo<Object> info = taskmgr.lookup(id);
            if (info == null) {
                String message = "Passed ID is invalid: " + id;
                return ResponseUtils.createMessageResponse(Status.NOT_FOUND, message);
            }

            if (!info.result().isDone()) {
                String message = "This task with id " + id + " is still being processed.";
                return ResponseUtils.createMessageResponse(Status.OK, message);
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




















    private static String getLocationUrl(UriInfo uri, String subres, String id) {
        return ResponseUtils.getLocationUrl(WorkflowApi.class, uri, subres, id);
    }

    private static class TaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Object> {
        public TaskManager() throws NamingException {
            super("WORKFLOW-TASKS", InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
        }
    }


}