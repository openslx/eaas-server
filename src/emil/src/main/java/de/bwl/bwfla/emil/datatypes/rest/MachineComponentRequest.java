/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * emulatorVersion 3 of the License, or (at your option) any later emulatorVersion.
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

package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emucomp.api.Binding;
import de.bwl.bwfla.emucomp.api.MediumType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;


@XmlType(name = "machine")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MachineComponentRequest extends ComponentWithExternalFilesRequest {
    @XmlElement(required = true)
    private String environment;

    @XmlElement(defaultValue = "us")
    private String keyboardLayout = "us";

    @XmlElement(defaultValue = "pc105")
    private String keyboardModel = "pc105";

    @XmlElement(required = false)
    private String object;
    
    @XmlElement(required = false, defaultValue = "default")
    private String archive = "default";

    @XmlElement(required = false, defaultValue = "default")
    private String objectArchive = "default";
    
    @XmlElement(required = false)
    private String software;

    @XmlElement(required = false, defaultValue = "false")
    private boolean lockEnvironment;

    @XmlElement(required = false, defaultValue = "latest")
    private String emulatorVersion = "latest";

    @XmlElement(required = false)
    private String nic;

    @XmlElement(required = false)
    private LinuxRuntimeContainerReq linuxRuntimeData;

    @XmlElement
    private ArrayList<UserMedium> userMedia;

    public String getEnvironment() {
        return environment;
    }

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    public String getKeyboardModel() {
        return keyboardModel;
    }

    public String getObject() {
        return object;
    }

    public String getSoftware() {
        return software;
    }

    public String getArchive() {
        return archive;
    }

    public boolean isLockEnvironment() {
        return lockEnvironment;
    }


    public String getEmulatorVersion() {
        return emulatorVersion;
    }

    public String getObjectArchive() {
        return objectArchive;
    }

    public LinuxRuntimeContainerReq getLinuxRuntimeData() {
        return linuxRuntimeData;
    }

    public String getNic() {
        return nic;
    }

    public ArrayList<UserMedium> getUserMedia() {
        if(userMedia == null)
            userMedia = new ArrayList<>();
        return userMedia;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class UserMedium extends JaxbType
    {
        @XmlElement(name = "mediumType")
        private MediumType mediumType;

        @XmlElement(name = "url")
        private String url;

        @XmlElement(required = false)
        private String name;

        public MediumType getMediumType() {
            return mediumType;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }
    }
}