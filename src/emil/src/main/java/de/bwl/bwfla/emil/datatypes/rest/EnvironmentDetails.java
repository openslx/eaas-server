package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilContainerEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EnvironmentDetails {

    private static final Logger LOG = Logger.getLogger(EmilEnvironmentRepository.class.getName());

    @XmlElement
    private NetworkingType networking;
    @XmlElement
    private String parentEnvId;

    @XmlElement
    private String envId;

    @XmlElement
    private String title;

    @XmlElement
    private String description;

    @XmlElement
    private String version;

    @XmlElement
    private String emulator;

    @XmlElement
    private String helpText;

    @XmlElement
    private boolean enableRelativeMouse;

    @XmlElement
    private boolean enablePrinting;

    @XmlElement
    private boolean shutdownByOs;

    @XmlElement
    private String timeContext;

    @XmlElement
    private String author;

    @XmlElement
    private boolean canProcessAdditionalFiles;

    @XmlElement
    private String archive;

    @XmlElement
    private String xpraEncoding;

    @XmlElement
    private String owner;

    @XmlElement
    private String envType;

    @XmlElement
    private List<ParentEnvironment> revisions;

    @XmlElement
    private List<SoftwareInfo> installedSoftwareIds;

    @XmlElement
    private String userTag;

    @XmlElement
    private String os;

    @XmlElement
    private String nativeConfig;

    @XmlElement(defaultValue = "false")
    private boolean useXpra = false;

    @XmlElement(defaultValue = "false")
    private boolean useWebRTC = false;

    @XmlElement private String containerName;
    @XmlElement private String containerVersion;

    @XmlElement private List<Drive> drives;

    @XmlElement private String timestamp;

    /* Object Environments */

    @XmlElement
    private String objectId;

    @XmlElement
    private String objectArchive;

    /* container */

    @XmlElement
    private String input;

    @XmlElement
    private String output;

    @XmlElement
    private List<String> processArgs;

    @XmlElement
    private List<String> processEnvs;

    @XmlElement
    private String runtimeId;

    @XmlElement
    private boolean isLinuxRuntime;

    EnvironmentDetails() {}


    public EnvironmentDetails(EmilEnvironment emilenv,
                              MachineConfiguration machineConf,
                              List<EmilEnvironment> parents,
                              SoftwareArchiveHelper swHelper) {



        this.envType = "base";
        this.parentEnvId = emilenv.getParentEnvId();
        this.envId = emilenv.getEnvId();
        this.title = emilenv.getTitle();
        this.description =  emilenv.getDescription();
        this.version = emilenv.getVersion();
        this.emulator = emilenv.getEmulator();
        this.enableRelativeMouse = emilenv.isEnableRelativeMouse();
        this.enablePrinting = emilenv.isEnablePrinting();
        this.shutdownByOs = emilenv.isShutdownByOs();
        this.timeContext = emilenv.getTimeContext();
        this.networking = emilenv.getNetworking();

        this.author = emilenv.getAuthor();
        this.canProcessAdditionalFiles = emilenv.isCanProcessAdditionalFiles();
        this.archive = emilenv.getArchive();
        this.xpraEncoding = emilenv.getXpraEncoding();
        this.isLinuxRuntime = emilenv.isLinuxRuntime();
        this.helpText = emilenv.getHelpText();
        this.timestamp = emilenv.getTimestamp();

        if(machineConf != null)
            this.drives = machineConf.getDrive();
        else
            this.drives = new ArrayList<>();

        if( emilenv.getOwner() != null)
            this.owner = emilenv.getOwner().getUsername();
        else
           this.owner = "shared";

        if(emilenv instanceof EmilObjectEnvironment)
        {
            EmilObjectEnvironment emilObjEnv = (EmilObjectEnvironment) emilenv;
            this.objectId = emilObjEnv.getObjectId();
            this.objectArchive = emilObjEnv.getObjectArchiveId();
            envType = "object";
        }

        if(emilenv instanceof EmilContainerEnvironment)
        {
            envType = "container";
            EmilContainerEnvironment cEnv = (EmilContainerEnvironment) emilenv;
            this.input = cEnv.getInput();
            this.output = cEnv.getOutput();
            this.runtimeId = cEnv.getRuntimeId();

            this.processArgs = cEnv.getArgs();
            this.processEnvs = cEnv.getEnv();
        }

        this.revisions = new ArrayList<>();
        for(EmilEnvironment parentEnv : parents)
        {
            ParentEnvironment p = new ParentEnvironment();
            p.id = parentEnv.getEnvId();
            p.text = parentEnv.getDescription();
            p.archive = parentEnv.getArchive();
            revisions.add(p);
        }

        if (machineConf != null) {
            List<String> swIds = machineConf.getInstalledSoftwareIds();
            installedSoftwareIds = new ArrayList<>();
            for(String swId : swIds)
            {
                try {
                    SoftwarePackage software = swHelper.getSoftwarePackageById(swId);
                    if(software == null)
                    {
                        LOG.severe("Software: " + swId + " not found. Probably software archive is not configured.");
                        continue;
                    }
                    SoftwareInfo info = new SoftwareInfo();
                    info.id = swId;
                    info.label = software.getName();
                    info.archive = software.getArchive();
                    installedSoftwareIds.add(info);
                } catch (BWFLAException e) {
                    LOG.log(Level.WARNING, "Looking up software-package failed: " + swId, e);
                }
            }

            this.userTag = machineConf.getUserTag();
            this.os = machineConf.getOperatingSystemId();

            if(machineConf.getNativeConfig() != null)
                this.nativeConfig = machineConf.getNativeConfig().getValue();

            if(machineConf.getUiOptions() != null )
            {
                if (machineConf.getUiOptions().getForwarding_system() != null &&
                        machineConf.getUiOptions().getForwarding_system().equalsIgnoreCase("xpra"))
                    this.useXpra = true;

                if (machineConf.getUiOptions().getAudio_system() != null &&
                        machineConf.getUiOptions().getAudio_system().equalsIgnoreCase("webRTC"))
                    this.useWebRTC = true;
            }

            if (machineConf.getEmulator().getBean() != null) {
                this.emulator = machineConf.getEmulator().getBean();
            }

            if (machineConf.getEmulator().getContainerName() != null) {
                this.containerName = machineConf.getEmulator().getContainerName();
            }

            if (machineConf.getEmulator().getContainerVersion() != null) {
                this.containerVersion = machineConf.getEmulator().getContainerVersion();
            }
        }
    }

    @XmlRootElement
    public static class ParentEnvironment {
        @XmlElement
        String id;

        @XmlElement
        String text;

        @XmlElement
        String archive;
    }

    @XmlRootElement
    public static class SoftwareInfo {
        @XmlElement
        String id;

        @XmlElement
        String label;

        @XmlElement
        String archive;
    }
}
