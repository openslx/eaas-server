package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.bwl.bwfla.common.utils.NetworkUtils;


@XmlAccessorType(XmlAccessType.NONE)
@XmlType(namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlRootElement(name="vdeslirp", namespace = "http://bwfla.bwl.de/common/datatypes")
public class VdeSlirpConfiguration extends ComponentConfiguration {
    @XmlElement(required = false, nillable = false)
    private String hwAddress;

    @XmlElement(required = false, nillable = false, defaultValue = "10.0.2.0")
    private String network = "10.0.2.0";

    @XmlElement(required = false, nillable = false, defaultValue = "10.0.2.1")
    private String gateway = "10.0.2.1";

    @XmlElement(required = false, nillable = false, defaultValue = "255.255.255.0")
    private String netmask = "255.255.255.0";
    
    @XmlElement(required = false, nillable = false, defaultValue = "true")
    private boolean dhcp = true;
    
    @XmlElement(required = false, nillable = false)
    private String dnsServer;

    public VdeSlirpConfiguration() {
        this.hwAddress = NetworkUtils.getRandomHWAddress();
    }
    
    public String getHwAddress() {
        return hwAddress;
    }

    public void setHwAddress(String hwAddress) {
        this.hwAddress = hwAddress;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getNetwork() {
        return network;
    }
    
    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public String getNetmask() {
        return netmask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public boolean isDhcpEnabled() {
        return dhcp;
    }

    public void setDhcpEnabled(boolean dhcpEnabled) {
        this.dhcp = dhcpEnabled;
    }

    public String getDnsServer() {
        return dnsServer;
    }

    public void setDnsServer(String dnsAddress) {
        this.dnsServer = dnsAddress;
    }
}
