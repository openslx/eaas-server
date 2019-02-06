package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.List;

public class ReplicateImagesResponse extends EmilResponseType {
    private List<String> taskList;

    public ReplicateImagesResponse() {}

    public ReplicateImagesResponse(BWFLAException e)
    {
        super(e);
    }

    public List<String> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<String> taskList) {
        this.taskList = taskList;
    }
}
