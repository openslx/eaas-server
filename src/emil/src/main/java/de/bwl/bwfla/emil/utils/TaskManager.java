package de.bwl.bwfla.emil.utils;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.emil.datatypes.rest.ClassificationResult;
import de.bwl.bwfla.emil.datatypes.rest.TaskStateResponse;
import de.bwl.bwfla.emil.datatypes.security.Role;
import de.bwl.bwfla.emil.datatypes.security.Secured;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("tasks")
@ApplicationScoped
public class TaskManager {

    private static AsyncIoTaskManager taskManager;

    static class AsyncIoTaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Object> {
        AsyncIoTaskManager() throws NamingException {
            super(InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
        }
    }

    protected static final Logger LOG = Logger.getLogger("EaaS TaskManager");

    @PostConstruct
    void init()
    {
        try {
            taskManager = new AsyncIoTaskManager();
            LOG.warning(taskManager.toString());
        } catch (NamingException e) {
            e.printStackTrace();
            throw new IllegalStateException("could not initialize task manager");
        }
    }

    public String submitTask(AbstractTask<Object> task)
    {
        return taskManager.submitTask(task);
    }

    @Secured({Role.PUBLIC})
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse taskState(@PathParam("id") String taskId) {
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
                if(o instanceof ClassificationResult)
                    response.setObject((ClassificationResult)o);
                if(o instanceof Map)
                    response.setUserData((Map<String,String>)o);
            }
            return response;

        } catch (InterruptedException | ExecutionException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return new TaskStateResponse(new BWFLAException(e));
        }
        finally {
            taskManager.removeTaskInfo(taskId);
        }
    }
}
