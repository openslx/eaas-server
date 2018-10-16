package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.api.objectarchive.TaskState;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class TaskStateResponse extends EmilResponseType{

    @XmlElement(required = true)
    private String taskId;

    @XmlElement(required = true, defaultValue = "false")
    private boolean isDone;

    @XmlElement(required = false)
    private Map<String, String> userData;

    public TaskStateResponse(TaskState objArchiveTaskState)
    {
        taskId = objArchiveTaskState.getTaskId();
        isDone = objArchiveTaskState.isDone();
    }

    public TaskStateResponse(String taskId)
    {
        this.taskId = taskId;
        this.isDone = false;
    }

    public TaskStateResponse(String taskId, boolean isDone)
    {
        this.taskId = taskId;
        this.isDone = true;
    }

    public TaskStateResponse(BWFLAException e) {
        super(e);
    }

    TaskStateResponse() {}

    public String getTaskId() {
        return taskId;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public Map<String, String> getUserData() {
        return userData;
    }

    public void setUserData(Map<String, String> userData) {
        this.userData = userData;
    }
}
