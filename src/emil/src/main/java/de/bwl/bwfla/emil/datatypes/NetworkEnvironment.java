package de.bwl.bwfla.emil.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.rest.NetworkEnvironmentNetworkingType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NetworkEnvironment extends JaxbType {

    @XmlElement(required = true)
    private ArrayList<NetworkEnvironmentElement> emilEnvironments;

    @XmlElement(required = true)
    private String title;

    @XmlElement(required = true)
    private String envId;

    @XmlElement(required = true)
    private String description;

    @XmlElement(required = true)
    private String network;

    @XmlElement(required = true)
    private String gateway;

    @XmlElement(required = true)
    private String upstream_dns;

    @XmlElement(required = false)
    private String dnsServiceEnvId;

    @XmlElement(required = false)
    private String smbServiceEnvId;

    @XmlElement(required = false)
    private String linuxArchiveProxyEnvId;

    @XmlElement(required = false)
    private String startupEnvId;

    @XmlElement(required = false)
    private NetworkEnvironmentNetworkingType networking;

    public ArrayList<NetworkEnvironmentElement> getEmilEnvironments() {
        return emilEnvironments;
    }

    public void setEmilEnvironments(ArrayList<NetworkEnvironmentElement> emilEnvironments) {
        this.emilEnvironments = emilEnvironments;
    }

    public NetworkEnvironmentNetworkingType getNetworking() {
        return this.networking;
    }

    public void setNetworking(NetworkEnvironmentNetworkingType networking) {
        this.networking = networking;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEnvId() {
        return envId;
    }

    @JsonIgnore
    public String getIdDBkey() {
        return idDBkey; // example: "envId" : "ad14d2bc-9ace-48df-b473-397dac19b2e915",
    }

    @JsonIgnore
    private static final String idDBkey = "envId";

    @XmlElement(required = false)
    private String type = getClass().getCanonicalName();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }


    public String getUpstream_dns() {
        return upstream_dns;
    }

    public void setUpstream_dns(String upstream_dns) {
        this.upstream_dns = upstream_dns;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDnsServiceEnvId() {
        return dnsServiceEnvId;
    }

    public String getStartupEnvId() {
        return startupEnvId;
    }
<<<<<<< Updated upstream
=======

    public void setDnsServiceEnvId(String dnsServiceEnvId) {
        this.dnsServiceEnvId = dnsServiceEnvId;
    }

    public String getSmbServiceEnvId() {
        return smbServiceEnvId;
    }

    public void setSmbServiceEnvId(String smbServiceEnvId) {
        this.smbServiceEnvId = smbServiceEnvId;
    }

    public String getLinuxArchiveProxyEnvId() {
        return linuxArchiveProxyEnvId;
    }

    public void setLinuxArchiveProxyEnvId(String linuxArchiveProxyEnvId) {
        this.linuxArchiveProxyEnvId = linuxArchiveProxyEnvId;
    }
>>>>>>> Stashed changes
}
