package de.bwl.bwfla.emil.datatypes;

import javax.xml.bind.annotation.*;


import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.security.EmilEnvironmentOwner;
import de.bwl.bwfla.emil.datatypes.security.EmilEnvironmentPermissions;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@XmlSeeAlso({
	EmilObjectEnvironment.class,
	EmilContainerEnvironment.class,
	EmilSessionEnvironment.class
})
@XmlAccessorType(XmlAccessType.NONE)
public class EmilEnvironment extends JaxbType implements Comparable<EmilEnvironment> {
	@XmlElement(required = false)
	private String parentEnvId;
	@XmlElement(required = true)
	protected String envId;
	@XmlElement(required = false, defaultValue = "default")
	protected String archive = "default";
	@XmlElement(required = false)
	private String os;
	@XmlElement(required = false)
	private String title;
	@XmlElement(required = false)
	private String description;
	@XmlElement(required = false)
	private String version;
	@XmlElement(required = false)
	private String status;
	@XmlElement(required = false)
	private String emulator;
	@XmlElement(required = false)
	private String helpText;
	@XmlElement(required = false)
	private String timeContext;
	@XmlElement(required = false)
	private String author;

	@XmlElement(required = false)
	private String type = getClass().getCanonicalName();

	@XmlElement(required = false)
	private Set<String> childrenEnvIds = new HashSet<>();

	@XmlElement(required = false)
	private Set<String> branches = new HashSet<>();

	@XmlElement(required = false)
	private boolean visible = true;
	@XmlElement(required = false)
	private boolean enableRelativeMouse = false;
	@XmlElement(required = false)
	private boolean enablePrinting = false;
	@XmlElement(required = false)
	private boolean shutdownByOs = false;

	@XmlElement(required = false)
	private boolean connectEnvs = false;

	@XmlElement(required = false)
	private boolean enableInternet;

	@XmlElement(required = false)
	private boolean serverMode;
	@XmlElement(required = false)
	private boolean localServerMode;
	@XmlElement(required = false)
	private boolean enableSocks;

	@XmlElement(required = false)
	private String serverPort;
	@XmlElement(required = false)
	private String serverIp;
	@XmlElement(required = false)
	private String gwPrivateIp;
	@XmlElement(required = false)
	private String gwPrivateMask;

	@XmlElement(required = false)
	private EmilEnvironmentOwner owner;

	@XmlElement(required = false)
	private EmilEnvironmentPermissions permissions;

	public boolean isConnectEnvs() {
		return connectEnvs;
	}

	public void setConnectEnvs(boolean connectEnvs) {
		this.connectEnvs = connectEnvs;
	}

	public boolean isCanProcessAdditionalFiles() {
		return canProcessAdditionalFiles;
	}

	public void setCanProcessAdditionalFiles(boolean canProcessAdditionalFiles) {
		this.canProcessAdditionalFiles = canProcessAdditionalFiles;
	}

	@XmlElement(required = false)
	private boolean canProcessAdditionalFiles;

	@XmlElement
	private String timestamp = Instant.now().toString();

	public EmilEnvironment() {}

	public EmilEnvironment(EmilEnvironment template)
	{
		envId = template.envId;
		archive = template.archive;
		os = template.os;
		title = template.title;
		description = template.description;
		version = template.version;
		status = template.status;
		emulator = template.emulator;
		helpText = template.helpText;
		timeContext = template.timeContext;
		visible = template.visible;
		enableRelativeMouse = template.enableRelativeMouse;
		enablePrinting = template.enablePrinting;
		shutdownByOs = template.shutdownByOs;
		enableInternet = template.enableInternet;
		serverMode = template.serverMode;
		localServerMode = template.localServerMode;
		enableSocks = template.enableSocks;
		serverPort = template.serverPort;
		serverIp = template.serverIp;
		gwPrivateIp = template.gwPrivateIp;
		gwPrivateMask = template.gwPrivateMask;
		owner = template.owner;
		permissions = template.permissions;
		connectEnvs = template.connectEnvs;
		canProcessAdditionalFiles = template.canProcessAdditionalFiles;
	}

	public String getArchive() {
		return archive;
	}

	public void setArchive(String archive) {
		this.archive = archive;
	}

	public String getTimeContext() {
		return timeContext;
	}
	public void setTimeContext(String timeContext) {
		this.timeContext = timeContext;
	}
	public boolean isEnableRelativeMouse() {
		return enableRelativeMouse;
	}
	public void setEnableRelativeMouse(boolean enableRelativeMouse) {
		this.enableRelativeMouse = enableRelativeMouse;
	}
	public String getParentEnvId() {
		return parentEnvId;
	}
	public void setParentEnvId(String envId) {
		this.parentEnvId = envId;
	}
	public String getEnvId() {
		return envId;
	}
	public void setEnvId(String envId) {
		this.envId = envId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public String getEmulator() {
		return emulator;
	}
	public void setEmulator(String emulator) {
		this.emulator = emulator;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isEnablePrinting() {
		return enablePrinting;
	}

	public boolean isShutdownByOs() {
		return shutdownByOs;
	}

	public void setShutdownByOs(boolean shutdownByOs) {
		this.shutdownByOs = shutdownByOs;
	}

	public void setEnablePrinting(boolean enablePrinting) {
		this.enablePrinting = enablePrinting;
	}

	@Override
	public int compareTo(EmilEnvironment other) {
		if(title == null || other == null || other.title == null)
			return -1;
		return title.compareTo(other.title);
	}
	public String getHelpText() {
		return helpText;
	}
	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}
	
	public boolean isVisible() {
		if(childrenEnvIds == null || childrenEnvIds.size() == 0)
			return true;
		return false;
	}

	public Set<String> getChildrenEnvIds() {
		return childrenEnvIds;
	}

	public void addChildEnvId(String envId) {
		this.childrenEnvIds.add(envId);
	}
	public void removeChildEnvId(String envId) {
		this.childrenEnvIds.remove(envId);
	}

	public Set<String> getBranches() {
		return branches;
	}

	public void addBranchId(String envId) {
		this.branches.add(envId);
	}

	public void removeBranchEnvId(String envId) {
		this.branches.remove(envId);
	}

	@JsonIgnore
	public String getIdDBkey() {
		return idDBkey; // example: "envId" : "ad14d2bc-9ace-48df-b473-397dac19b2e915",
	}

    @JsonIgnore
	private static final String idDBkey = "envId";

	public boolean isEnableInternet() {
		return enableInternet;
	}

	public void setEnableInternet(boolean enableInternet) {
		this.enableInternet = enableInternet;
	}

	public boolean isServerMode() {
		return serverMode;
	}

	public boolean isLocalServerMode() {
		return localServerMode;
	}

	public void setLocalServerMode(boolean localServerMode) {
		this.localServerMode = localServerMode;
	}

	public void setServerMode(boolean serverMode) {
		this.serverMode = serverMode;
	}

	public boolean isEnableSocks() {
		return enableSocks;
	}

	public void setEnableSocks(boolean enableSocks) {
		this.enableSocks = enableSocks;
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

	public String getGwPrivateIp() {
		return gwPrivateIp;
	}

	public void setGwPrivateIp(String gwPrivateIp) {
		this.gwPrivateIp = gwPrivateIp;
	}

	public String getGwPrivateMask() {
		return gwPrivateMask;
	}

	public void setGwPrivateMask(String gwPrivateMask) {
		this.gwPrivateMask = gwPrivateMask;
	}

	public EmilEnvironmentPermissions getPermissions() {
		return permissions;
	}

	public void setPermissions(EmilEnvironmentPermissions permissions) {
		this.permissions = permissions;
	}

	public EmilEnvironmentOwner getOwner() {
		return owner;
	}

	public void setOwner(EmilEnvironmentOwner owner) {
		this.owner = owner;
	}



	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}


	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
