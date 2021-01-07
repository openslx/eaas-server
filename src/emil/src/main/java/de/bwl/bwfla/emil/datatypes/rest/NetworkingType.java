package de.bwl.bwfla.emil.datatypes.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkingType {

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

    public boolean isConnectEnvs() {
        return connectEnvs;
    }

    public void setConnectEnvs(boolean connectEnvs) {
        this.connectEnvs = connectEnvs;
    }
}
