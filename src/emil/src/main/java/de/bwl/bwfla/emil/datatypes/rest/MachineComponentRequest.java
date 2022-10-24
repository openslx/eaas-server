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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emucomp.api.MediumType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
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

    @XmlElement(required = false, defaultValue = "false")
    private boolean headless;

    @XmlElement(required = false, defaultValue = "120")
    private int sessionLifetime;

    @XmlElement(required = false)
    private LinuxRuntimeContainerReq linuxRuntimeData;

    @XmlElement(required = false, defaultValue = "false")
    private boolean hasOutput;

    @XmlElement(required = false, defaultValue = "boot")
    private String outputDriveId;

    @XmlElement
    private ArrayList<UserMedium> userMedia;

    @XmlElement
    private ArrayList<Drive> drives;

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

    public boolean isHeadless() {
        return headless;
    }

    public int getSessionLifetime() {
        return sessionLifetime;
    }

    public boolean hasOutput() {
        return hasOutput;
    }

    public void setHasOutput(boolean hasOutput) {
        this.hasOutput = hasOutput;
    }

    public String getOutputDriveId() {
        return outputDriveId;
    }

    public void setOutputDriveId(String outputDriveId) {
        this.outputDriveId = outputDriveId;
    }

    public ArrayList<Drive> getDrives() {
        if (drives == null)
            drives = new ArrayList<>();
        return drives;
    }


    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "kind")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ImageDataSource.class, name = "image"),
            @JsonSubTypes.Type(value = ObjectDataSource.class, name = "object"),
            @JsonSubTypes.Type(value = SoftwareDataSource.class, name = "software"),
            @JsonSubTypes.Type(value = UserMedium.class, name = "user-medium")
    })
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    @XmlSeeAlso({
            ObjectDataSource.class,
            SoftwareDataSource.class,
            ImageDataSource.class,
            UserMedium.class
    })
    public static class DriveDataSource extends JaxbType
    {
        @XmlElement(required = true)
        private String kind;

        public String getKind() {
            return kind;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ImageDataSource extends DriveDataSource
    {
        @XmlElement(required = true)
        private String id;

        public String getId() {
            return id;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ObjectDataSource extends DriveDataSource
    {
        @XmlElement(required = true)
        private String id;

        @XmlElement(required = true)
        private String archive;

        public String getId() {
            return id;
        }

        public String getArchive() {
            return archive;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class SoftwareDataSource extends DriveDataSource
    {
        @XmlElement(required = true)
        private String id;

        public String getId() {
            return id;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class UserMedium extends DriveDataSource
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

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Drive extends JaxbType
    {
        @XmlElement(required = true)
        private String id;

        @XmlElement(required = true)
        private DriveDataSource data;

        @XmlElement
        private boolean bootable;

        public String getId() {
            return id;
        }

        public DriveDataSource getData() {
            return data;
        }

        public boolean isBootable() {
            return bootable;
        }
    }
}