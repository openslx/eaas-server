package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlElement;

public class NetworkEnvironmentNetworkingType extends ContainerNetworkingType {

    @XmlElement(required = false)
    boolean isArchivedInternetEnabled;

    @XmlElement(required = false)
    String archiveInternetDate;

    @XmlElement(required = false)
    boolean allowExternalConnections;

    @XmlElement(required = false)
    String dhcpNetworkAddress;

    @XmlElement(required = false)
    String dhcpNetworkMask;

    public String getArchiveInternetDate() {
        return archiveInternetDate;
    }

    public void setArchiveInternetDate(String archiveInternetDate) {
        this.archiveInternetDate = archiveInternetDate;
    }

    public String getDhcpNetworkAddress() {
        return dhcpNetworkAddress;
    }

    public void setDhcpNetworkAddress(String dhcpNetworkAddress) {
        this.dhcpNetworkAddress = dhcpNetworkAddress;
    }

    public String getDhcpNetworkMask() {
        return dhcpNetworkMask;
    }

    public void setDhcpNetworkMask(String dhcpNetworkMask) {
        this.dhcpNetworkMask = dhcpNetworkMask;
    }

    public boolean isArchivedInternetEnabled() {
        return isArchivedInternetEnabled;
    }

    public void setArchivedInternetEnabled(boolean archivedInternetEnabled) {
        this.isArchivedInternetEnabled = archivedInternetEnabled;
    }

    public boolean isAllowExternalConnections() {
        return allowExternalConnections;
    }

    public void setAllowExternalConnections(boolean allowExternalConnections) {
        this.allowExternalConnections = allowExternalConnections;
    }
}
