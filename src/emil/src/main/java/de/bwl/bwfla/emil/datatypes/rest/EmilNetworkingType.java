package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmilNetworkingType {

    @XmlElement(required = false)
    private boolean enableInternet;
    @XmlElement(required = false)
    private boolean serverMode;
    @XmlElement(required = false)
    private boolean localServerMode;
    @XmlElement(required = false)
    private boolean enableSocks;
    @XmlElement(required = false)
    private String serverPort;
    @XmlElement(required = false)
    private String serverIp;
    @XmlElement(required = false)
    private String gwPrivateIp;
    @XmlElement(required = false)
    private String gwPrivateMask;
    @XmlElement(required = false)
    private boolean connectEnvs = false;
    @XmlElement(required = false)
    private String helpText;

    public boolean isEnableInternet() {
        return enableInternet;
    }

    public void setEnableInternet(boolean enableInternet) {
        this.enableInternet = enableInternet;
    }

    public boolean isServerMode() {
        return serverMode;
    }

    public void setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
    }

    public boolean isLocalServerMode() {
        return localServerMode;
    }

    public void setLocalServerMode(boolean localServerMode) {
        this.localServerMode = localServerMode;
    }

    public boolean isEnableSocks() {
        return enableSocks;
    }

    public void setEnableSocks(boolean enableSocks) {
        this.enableSocks = enableSocks;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getGwPrivateIp() {
        return gwPrivateIp;
    }

    public void setGwPrivateIp(String gwPrivateIp) {
        this.gwPrivateIp = gwPrivateIp;
    }

    public String getGwPrivateMask() {
        return gwPrivateMask;
    }

    public void setGwPrivateMask(String gwPrivateMask) {
        this.gwPrivateMask = gwPrivateMask;
    }

    public boolean isConnectEnvs() {
        return connectEnvs;
    }

    public void setConnectEnvs(boolean connectEnvs) {
        this.connectEnvs = connectEnvs;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }
}
