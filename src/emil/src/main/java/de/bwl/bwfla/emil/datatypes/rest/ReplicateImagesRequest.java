package de.bwl.bwfla.emil.datatypes.rest;

import java.util.List;

public class ReplicateImagesRequest extends EmilRequestType{
    private List<String> replicateList;
    private String destArchive;

    public List<String> getReplicateList() {
        return replicateList;
    }

    public void setReplicateList(List<String> replicateList) {
        this.replicateList = replicateList;
    }

    public String getDestArchive() {
        return destArchive;
    }

    public void setDestArchive(String destArchive) {
        this.destArchive = destArchive;
    }
}
