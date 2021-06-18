package de.bwl.bwfla.prov.api;


import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.rest.ComponentWithExternalFilesRequest;
import de.bwl.bwfla.emil.datatypes.rest.LinuxRuntimeContainerReq;
import de.bwl.bwfla.emucomp.api.MediumType;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlType(name = "machine")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MachineComponentRequestWithInput extends ComponentWithExternalFilesRequest {
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

    @XmlElement
    private ArrayList<UserMedium> userMedia;

    @XmlElement(name = "input_data")
    private ArrayList<Input> input;


    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    public void setKeyboardLayout(String keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }

    public String getKeyboardModel() {
        return keyboardModel;
    }

    public void setKeyboardModel(String keyboardModel) {
        this.keyboardModel = keyboardModel;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public boolean isLockEnvironment() {
        return lockEnvironment;
    }

    public void setLockEnvironment(boolean lockEnvironment) {
        this.lockEnvironment = lockEnvironment;
    }

    public String getEmulatorVersion() {
        return emulatorVersion;
    }

    public void setEmulatorVersion(String emulatorVersion) {
        this.emulatorVersion = emulatorVersion;
    }

    public String getObjectArchive() {
        return objectArchive;
    }

    public void setObjectArchive(String objectArchive) {
        this.objectArchive = objectArchive;
    }

    public LinuxRuntimeContainerReq getLinuxRuntimeData() {
        return linuxRuntimeData;
    }

    public void setLinuxRuntimeData(LinuxRuntimeContainerReq linuxRuntimeData) {
        this.linuxRuntimeData = linuxRuntimeData;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public ArrayList<UserMedium> getUserMedia() {
        if (userMedia == null)
            userMedia = new ArrayList<>();
        return userMedia;
    }

    public ArrayList<Input> getInput() {
        if (input == null)
            input = new ArrayList<>();
        return input;
    }

    public void setInput(ArrayList<Input> input) {
        this.input = input;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public void setSessionLifetime(int sessionLifetime) {
        this.sessionLifetime = sessionLifetime;
    }

    public boolean isHeadless() {
        return headless;
    }

    public int getSessionLifetime() {
        return sessionLifetime;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class UserMedium extends JaxbType {
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

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Input extends JaxbType {

        @XmlElement(name = "content")
        private ArrayList<InputData> content;

        @XmlElement(name = "destination")
        private String destination;

        @XmlElement(name = "size_mb")
        private int size;

        public ArrayList<InputData> getContent() {
            return content;
        }

        public void setContent(ArrayList<InputData> content) {
            this.content = content;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }


    }


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class InputData extends JaxbType {
        @XmlElement(name = "action")
        private String action;

        @XmlElement(name = "url")
        private String url;

        @XmlElement(name = "name")
        private String name;

        @XmlElement(name = "compression_format")
        private String compressionFormat;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCompressionFormat() {
            return compressionFormat;
        }

        public void setCompressionFormat(String compressionFormat) {
            this.compressionFormat = compressionFormat;
        }


    }

}