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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NetworkResponse extends JaxbType {
    @XmlElement(required = true)
    private String id;

    @XmlElement(required = true)
    private boolean isLocalMode = false;

    @XmlElement
    private Map<String, URI> networkUrls ;



    public NetworkResponse(String id) {
        super();
        this.id = id;
        networkUrls = new HashMap<>();
    }

    public boolean isLocalMode() {
        return isLocalMode;
    }

    public void setLocalMode(boolean localMode) {
        isLocalMode = localMode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addUrl(String key, URI uri)
    {
        networkUrls.put(key, uri);
    }

    public Map<String, URI> getNetworkUrls() {
        return networkUrls;
    }

    public void setNetworkUrls(Map<String, URI> networkUrls) {
        this.networkUrls = networkUrls;
    }
}
