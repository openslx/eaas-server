package de.bwl.bwfla.objectarchive.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class TaskState extends JaxbType{

    @XmlElement
    private String taskId;

    @XmlElement(required = true, defaultValue = "false")
    private boolean done;

    public TaskState()
    {
        taskId = null;
        done = false;
    }

    public TaskState(String id)
    {
        taskId = id;
        done = false;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
