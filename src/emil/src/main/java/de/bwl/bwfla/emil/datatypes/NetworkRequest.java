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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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

    @XmlType
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ComponentSpec {
        @XmlElement(required = true)
        private String componentId;
        
        @XmlElement(required = false, defaultValue = "auto")
        private String hwAddress = "auto";

        public String getComponentId() {
            return componentId;
        }

        public void setComponentId(String componentId) {
            this.componentId = componentId;
        }

        public String getHwAddress() {
            return hwAddress;
        }

        public void setHwAddress(String hwAddress) {
            this.hwAddress = hwAddress;
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

    @XmlElement(name = "hasInternet", required = false, defaultValue = "false")
    private boolean internet = false;

    @XmlElement(name = "enableDhcp", required = false, defaultValue = "false")
    private boolean dhcp = false;

    @XmlElement(name= "hasTcpGateway", required = false, defaultValue = "false")
    private boolean tcpGateway = false;

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

    public void setTcpGatewayConfig(TcpGatewayConfig tcpGatewayConfig) {
        this.tcpGatewayConfig = tcpGatewayConfig;
    }
}
