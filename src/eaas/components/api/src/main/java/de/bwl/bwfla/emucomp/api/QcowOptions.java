package de.bwl.bwfla.emucomp.api;

public class QcowOptions {
    private String backingFile;
    private String size;
    private String auth;

    public String getBackingFile() {
        if(auth == null)
            return backingFile;
        return backingFile.replace("://", "://" + auth + "@");
    }

    public void setBackingFile(String backingFile) {
        this.backingFile = backingFile;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setProxyAuth(String auth)
    {
        this.auth = auth;
    }
}
