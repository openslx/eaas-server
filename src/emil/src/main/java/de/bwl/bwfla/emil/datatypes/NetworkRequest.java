/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.emil.datatypes;

import java.util.List;

import javax.xml.bind.annotation.*;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NetworkRequest extends JaxbType {

    public boolean isDhcp() {
        return dhcp;
    }

    public void setDhcp(boolean dhcp) {
        this.dhcp = dhcp;
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

    @XmlType
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ComponentSpec {
        @XmlElement(required = true)
        private String componentId;

        @XmlElement(required = false)
        private String networkLabel;

        @XmlElement(required = false)
        private List<Short> serverPorts;

        @XmlElement(required = false)
        private String serverIp;

        @XmlElement
        private String fqdn;

        @XmlElement(required = false, defaultValue = "auto")
        private String hwAddress = "auto";


        public String getComponentId() {
            return componentId;
        }

        public void setComponentId(String componentId) {
            this.componentId = componentId;
        }

        public String getNetworkLabel() {
            return networkLabel;
        }


        public void setNetworkLabel(String networkLabel) {
            this.networkLabel = networkLabel;
        }

        public List<Short> getServerPorts() {
            return serverPorts;
        }

        public void setServerPorts(List<Short> serverPorts) {
            this.serverPorts = serverPorts;
        }

        public String getServerIp() {
            return serverIp;
        }

        public void setServerIp(String serverIp) {
            this.serverIp = serverIp;
        }

        public String getHwAddress() {
            return hwAddress;
        }

        public void setHwAddress(String hwAddress) {
            this.hwAddress = hwAddress;
        }

        public String getFqdn() {
            return fqdn;
        }

        public void setFqdn(String fqdn) {
            this.fqdn = fqdn;
        }
    }

    @XmlType
    @XmlAccessorType(XmlAccessType.NONE)
    public static class TcpGatewayConfig {

        @XmlElement
        private boolean socks;

        @XmlElement
        private String serverPort;

        @XmlElement
        private String serverIp;

        public boolean isSocks() {
            return socks;
        }

        public void setSocks(boolean socks) {
            this.socks = socks;
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
    }
    
    @XmlElement(required = true)
    private List<ComponentSpec> components;

    @XmlElement
    private String networkEnvironmentId;

    @XmlElement(name = "hasInternet", required = false, defaultValue = "false")
    private boolean internet = false;

    @XmlElement(name = "enableDhcp", required = false, defaultValue = "false")
    private boolean dhcp = false;

    @XmlElement
    private String dhcpNetworkAddress;

    @XmlElement
    private String dhcpNetworkMask;

    @XmlElement(name= "hasTcpGateway", required = false, defaultValue = "false")
    private boolean tcpGateway = false;

    @XmlElement
    private String gateway;

    @XmlElement
    private String network;

    public String getNetwork() {
        return network;
    }

    public String getGateway() {
        return gateway;
    }

    @XmlElement
    private TcpGatewayConfig tcpGatewayConfig;

    public List<ComponentSpec> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentSpec> components) {
        this.components = components;
    }

    public boolean hasInternet() {
        return internet;
    }

    public void setInternet(boolean internet) {
        this.internet = internet;
    }

    public boolean isTcpGateway() {
        return tcpGateway;
    }

    public void setTcpGateway(boolean tcpGateway) {
        this.tcpGateway = tcpGateway;
    }

    public TcpGatewayConfig getTcpGatewayConfig() {
        return tcpGatewayConfig;
    }

    public String getNetworkEnvironmentId() {
        return networkEnvironmentId;
    }

    public void setTcpGatewayConfig(TcpGatewayConfig tcpGatewayConfig) {
        this.tcpGatewayConfig = tcpGatewayConfig;
    }
}
