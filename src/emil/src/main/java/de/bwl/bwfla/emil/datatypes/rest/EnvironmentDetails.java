package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.emil.datatypes.EmilContainerEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EnvironmentDetails {

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
    private boolean serverMode;

    @XmlElement
    private boolean localServerMode;

    @XmlElement
    private boolean enableSocks;

    @XmlElement
    private String serverPort;

    @XmlElement
    private String serverIp;

    @XmlElement
    private String gwPrivateIp;

    @XmlElement
    private String gwPrivateMask;

    @XmlElement
    private boolean enableInternet;

    @XmlElement
    private boolean connectEnvs;

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
    private List<String> installedSoftwareIds;

    @XmlElement
    private String userTag;

    @XmlElement
    private String os;

    @XmlElement
    private String nativeConfig;

    @XmlElement
    private boolean useXpra;

    @XmlElement private String containerName;
    @XmlElement private String containerVersion;

    @XmlElement private List<Drive> drives;

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

    EnvironmentDetails() {}

    public EnvironmentDetails(EmilEnvironment emilenv, MachineConfiguration machineConf, List<EmilEnvironment> parents) {

        this.envType = "base";
        this.parentEnvId = emilenv.getParentEnvId();
        this.envId = emilenv.getEnvId();
        this.title = emilenv.getTitle();
        this.description =  emilenv.getDescription();
        this.version = emilenv.getVersion();
        this.emulator = emilenv.getEmulator();
        this.helpText = emilenv.getHelpText();
        this.enableRelativeMouse = emilenv.isEnableRelativeMouse();
        this.enablePrinting = emilenv.isEnablePrinting();
        this.shutdownByOs = emilenv.isShutdownByOs();
        this.timeContext = emilenv.getTimeContext();
        this.serverMode = emilenv.isServerMode();
        this.localServerMode = emilenv.isLocalServerMode();
        this.enableSocks = emilenv.isEnableSocks();
        this.serverPort = emilenv.getServerPort();
        this.serverIp = emilenv.getServerIp();
        this.gwPrivateIp = emilenv.getGwPrivateIp();
        this.gwPrivateMask = emilenv.getGwPrivateMask();
        this.enableInternet = emilenv.isEnableInternet();
        this.connectEnvs = emilenv.isConnectEnvs();
        this.author = emilenv.getAuthor();
        this.canProcessAdditionalFiles = emilenv.isCanProcessAdditionalFiles();
        this.archive = emilenv.getArchive();
        this.xpraEncoding = emilenv.getXpraEncoding();

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
            this.installedSoftwareIds = machineConf.getInstalledSoftwareIds();
            this.userTag = machineConf.getUserTag();
            this.os = machineConf.getOperatingSystemId();

            if(machineConf.getNativeConfig() != null)
                this.nativeConfig = machineConf.getNativeConfig().getValue();

            if(machineConf.getUiOptions() != null && machineConf.getUiOptions().getForwarding_system() != null)
            {
                if (machineConf.getUiOptions().getForwarding_system().equalsIgnoreCase("xpra"))
                    this.useXpra = true;
            }
            else
                this.useXpra = false;

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
}
