package de.bwl.bwfla.emil.utils;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("tasks")
@ApplicationScoped
public class TaskManager {

    private Map<String, TaskStateResponse> responses;
    private AsyncIoTaskManager taskManager;

    private static class AsyncIoTaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Object> {
        AsyncIoTaskManager() throws NamingException {
            super("EMIL-TASKS", InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"), true);
        }
    }

    protected static final Logger LOG = Logger.getLogger("EaaS TaskManager");

    @PostConstruct
    void init()
    {
        this.responses = new ConcurrentHashMap<>();

        try {
            this.taskManager = new AsyncIoTaskManager();
        } catch (NamingException e) {
            final String message = "Initializing task manager failed!";
            LOG.log(Level.SEVERE, message, e);
            throw new IllegalStateException(message, e);
        }
    }

    public String submitTask(AbstractTask<Object> task)
    {
        return taskManager.submit(task);
    }

    @Secured(roles = {Role.PUBLIC})
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse lookup(@PathParam("id") String taskId, @QueryParam("cleanup") @DefaultValue("true") boolean cleanup)
    {
        // Lookup cached response (and remove if requested)
        TaskStateResponse response = (cleanup) ? responses.remove(taskId) : responses.get(taskId);
        if (response != null)
            return response;

        // No response found, lookup task's result...
        final TaskInfo<Object> info = taskManager.lookup(taskId);
        if (info == null)
            return new TaskStateResponse(new BWFLAException("task not found"));

        if (!info.result().isDone())
            return new TaskStateResponse(taskId);

        try {
            final Object result = info.result().get();
            response = new TaskStateResponse(taskId, true);
            if (result != null) {
                if (result instanceof BWFLAException)
                    response = new TaskStateResponse((BWFLAException) result);
                if (result instanceof ClassificationResult)
                    response.setObject((ClassificationResult) result);
                if (result instanceof CreateContainerImageResult)
                    response.setObject((CreateContainerImageResult)result);
                if (result instanceof ComponentResponse)
                    response.setObject((ComponentResponse) result);
                if (result instanceof Map)
                    response.setUserData((Map<String,String>) result);
                if(result instanceof SnapshotResponse)
                    response.setObject((SnapshotResponse)result);
            }

            // Cache response
            if (!cleanup) {
                LOG.info("Caching task result " + taskId);
                responses.put(taskId, response);
            }

            return response;

        } catch (InterruptedException | ExecutionException e) {
            LOG.log(Level.SEVERE, "Retrieving task result failed!", e);
            return new TaskStateResponse(new BWFLAException(e));
        }
        finally {
            taskManager.remove(taskId);
        }
    }

    @DELETE
    @Path("/{id}")
    @Secured(roles = {Role.PUBLIC})
    public void remove(@PathParam("id") String taskId)
    {
        final TaskInfo<Object> task = taskManager.lookup(taskId);
        if (task != null) {
            // Cancel and remove task
            taskManager.remove(taskId);
            task.result().cancel(true);
        }

        final TaskStateResponse response = responses.remove(taskId);
        if (response != null)
            LOG.info("Cached task result " + taskId + " removed");

        if (task == null && response == null)
            throw new NotFoundException("Task " + taskId + " was not found!");
    }
}
