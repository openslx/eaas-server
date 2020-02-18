package de.bwl.bwfla.emucomp.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class NetworkConfiguration extends JaxbType {

    @XmlElement
    private String network;

    @XmlElement
    private String gateway;

    @XmlElement
    private String upstream_dns;

    @XmlElement
    private String archived_internet_date;

    public String getArchived_internet_date() {
        return archived_internet_date;
    }

    public void setArchived_internet_date(String archived_internet_date) {
        this.archived_internet_date = archived_internet_date;
    }

    @XmlElement
    private DHCPConfiguration dhcp;

    @XmlElement
    private List<EnvironmentNetworkConfiguration> environments;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public DHCPConfiguration getDhcp() {
        return dhcp;
    }

    public void setDhcp(DHCPConfiguration dhcp) {
        this.dhcp = dhcp;
    }

    public List<EnvironmentNetworkConfiguration> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<EnvironmentNetworkConfiguration> environments) {
        this.environments = environments;
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

    @XmlRootElement
    public static class EnvironmentNetworkConfiguration
    {
        @XmlElement
        private String mac;
        @XmlElement
        private String ip;

        @XmlElement
        private boolean wildcard;

        @XmlElement
        private List<String> hostnames;

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public boolean isWildcard() {
            return wildcard;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setWildcard(boolean wildcard) {
            this.wildcard = wildcard;
        }

        public List<String> getHostnames() {
            if(hostnames == null)
                hostnames = new ArrayList<>();
            return hostnames;
        }
    }

    @XmlRootElement
    public static class DHCPConfiguration
    {
        @XmlElement
        private String ip;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }


}
