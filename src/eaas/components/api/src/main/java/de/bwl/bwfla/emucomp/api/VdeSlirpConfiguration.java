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

    @XmlElement(required = false, nillable = false, defaultValue = "10.0.2.2")
    private String ip4Address = "10.0.2.2";
    
    @XmlElement(required = false, nillable = false, defaultValue = "24")
    private Integer netmask = 24;
    
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

    public void setIp4Address(String ip4Address) {
        this.ip4Address = ip4Address;
    }

    public String getIp4Address() {
        return ip4Address;
    }
    
    public void setNetmask(int netmask) {
        this.netmask = netmask;
    }

    public int getNetmask() {
        return netmask;
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
