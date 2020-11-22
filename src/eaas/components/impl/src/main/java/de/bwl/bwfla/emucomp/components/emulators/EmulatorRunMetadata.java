package de.bwl.bwfla.emucomp.components.emulators;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Nic;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class EmulatorRunMetadata extends JaxbType {
    @XmlElement
    private String startTime;

    @XmlElement
    private List<DriveBinding> drives;

    @XmlElement
    private List<NicBinding> nics;

    @XmlElement
    MachineConfiguration machineConfig;

    public String isStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public List<DriveBinding> getDrives() {
        if(drives == null)
            drives = new ArrayList<>();

        return drives;
    }

    public List<NicBinding> getNics() {
        if(nics == null)
            nics = new ArrayList<>();

        return nics;
    }


    @XmlAccessorType(XmlAccessType.NONE)
    @XmlRootElement
    public static class NicBinding extends JaxbType {
        @XmlElement
        public Nic nic;

        @XmlElement
        public String path;
    }

    @XmlAccessorType(XmlAccessType.NONE)
    @XmlRootElement
    public static class DriveBinding extends JaxbType {
        @XmlElement
        public Drive drive;
        @XmlElement
        public String path;
    }
}
