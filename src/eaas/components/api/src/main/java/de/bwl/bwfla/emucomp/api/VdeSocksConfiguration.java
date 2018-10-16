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

package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.bwl.bwfla.common.utils.NetworkUtils;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlRootElement(name = "vdesocks", namespace = "http://bwfla.bwl.de/common/datatypes")
public class VdeSocksConfiguration extends ComponentConfiguration {
    @XmlElement(required = false, nillable = false)
    private String hwAddress;

    @XmlElement(required = false, nillable = false, defaultValue = "10.0.2.4")
    private String ip4Address = "10.0.2.4";

    @XmlElement(required = false, nillable = false, defaultValue = "24")
    private Integer netmask = 24;

    public VdeSocksConfiguration() {
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
}
