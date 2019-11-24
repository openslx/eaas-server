package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.emil.datatypes.EmilContainerEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import riotcmd.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EnvironmentListItem {

    EnvironmentListItem() {}

    @XmlElement
    private String envId;
    
    @XmlElement
    private String title;
    
    @XmlElement
    private String archive;

    @XmlElement
    private String owner;

    @XmlElement
    private String objectId;

    @XmlElement
    private String objectArchive;

    @XmlElement
    private String envType;

    @XmlElement
    private String operatingSystem;

    @XmlElement
    private String timestamp;

    @XmlElement
    private String description;

    @XmlElement
    private boolean isLinuxRuntime;

    @XmlElement
    private boolean networkEnabled;

    public EnvironmentListItem(EmilEnvironment emilenv) {

        this.envId =  emilenv.getEnvId();
        this.title = emilenv.getTitle();
        this.archive = emilenv.getArchive();
        this.envType = "base";
        this.isLinuxRuntime = emilenv.isLinuxRuntime();
        this.timestamp = emilenv.getTimestamp();
        this.operatingSystem = emilenv.getOs();
        this.description = emilenv.getDescription();
        
        if (emilenv.getNetworking() != null)
            this.networkEnabled = (emilenv.getNetworking().isConnectEnvs() || emilenv.getNetworking().isEnableInternet() || emilenv.getNetworking().isServerMode() || emilenv.getNetworking().isLocalServerMode());
        else this.networkEnabled = false;

        if( emilenv.getOwner() != null)
           this.owner = emilenv.getOwner().getUsername();
        else
            this.owner = "shared";

        if(emilenv instanceof EmilObjectEnvironment)
        {
            EmilObjectEnvironment emilObjEnv = (EmilObjectEnvironment) emilenv;
            this.objectId = emilObjEnv.getObjectId();
            this.objectArchive = emilObjEnv.getObjectArchiveId();
            this.envType = "object";
        }

        if(emilenv instanceof EmilContainerEnvironment)
        {
            this.envType = "container";
        }
    }

    public boolean isLinuxRuntime() {
        return isLinuxRuntime;
    }

    public void setLinuxRuntime(boolean linuxRuntime) {
        isLinuxRuntime = linuxRuntime;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectArchive() {
        return objectArchive;
    }

    public void setObjectArchive(String objectArchive) {
        this.objectArchive = objectArchive;
    }

    public String getEnvType() {
        return envType;
    }

    public void setEnvType(String envType) {
        this.envType = envType;
    }

    public boolean isNetworkEnabled() {
        return networkEnabled;
    }

    public void setNetworkEnabled(boolean networkEnabled) {
        this.networkEnabled = networkEnabled;
    }
}
