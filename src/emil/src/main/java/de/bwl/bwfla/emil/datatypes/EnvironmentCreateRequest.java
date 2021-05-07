package de.bwl.bwfla.emil.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.UiOptions;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EnvironmentCreateRequest extends JaxbType{
	private String templateId;
	private String label;
	private String nativeConfig;
	private List<DriveSetting> driveSettings;

	private String romId;
	private String romLabel;
	
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getNativeConfig() {
		return nativeConfig;
	}
	public void setNativeConfig(String nativeConfig) {
		this.nativeConfig = nativeConfig;
	}

	private boolean enablePrinting;
	private boolean enableRelativeMouse;
	private boolean useWebRTC;
	private boolean useXpra;
	private String xpraEncoding;
	private boolean shutdownByOs;
	private String operatingSystemId;
	private boolean enableNetwork;


	public List<DriveSetting> getDriveSettings() {
		return driveSettings;
	}

	public void setDriveSettings(List<DriveSetting> driveSettings) {
		this.driveSettings = driveSettings;
	}

	public boolean isEnablePrinting() {
		return enablePrinting;
	}

	public void setEnablePrinting(boolean enablePrinting) {
		this.enablePrinting = enablePrinting;
	}

	public boolean isEnableRelativeMouse() {
		return enableRelativeMouse;
	}

	public void setEnableRelativeMouse(boolean enableRelativeMouse) {
		this.enableRelativeMouse = enableRelativeMouse;
	}

	public boolean isUseWebRTC() {
		return useWebRTC;
	}

	public void setUseWebRTC(boolean useWebRTC) {
		this.useWebRTC = useWebRTC;
	}

	public boolean isUseXpra() {
		return useXpra;
	}

	public void setUseXpra(boolean useXpra) {
		this.useXpra = useXpra;
	}

	public String getXpraEncoding() {
		return xpraEncoding;
	}

	public void setXpraEncoding(String xpraEncoding) {
		this.xpraEncoding = xpraEncoding;
	}

	public boolean isShutdownByOs() {
		return shutdownByOs;
	}

	public void setShutdownByOs(boolean shutdownByOs) {
		this.shutdownByOs = shutdownByOs;
	}

	public String getOperatingSystemId() {
		return operatingSystemId;
	}

	public void setOperatingSystemId(String operatingSystemId) {
		this.operatingSystemId = operatingSystemId;
	}

	public String getRomId() {
		return romId;
	}

	public void setRomId(String romId) {
		this.romId = romId;
	}

	public String getRomLabel() {
		return romLabel;
	}

	public void setRomLabel(String romLabel) {
		this.romLabel = romLabel;
	}

	public boolean isEnableNetwork() {
		return enableNetwork;
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class DriveSetting extends JaxbType
	{
		public Drive getDrive() {
			return drive;
		}

		public void setDrive(Drive drive) {
			this.drive = drive;
		}

		public String getImageId() {
			return imageId;
		}

		public void setImageId(String imageId) {
			this.imageId = imageId;
		}

		public String getImageArchive() {
			return imageArchive;
		}

		public void setImageArchive(String imageArchive) {
			this.imageArchive = imageArchive;
		}

		public String getObjectId() {
			return objectId;
		}

		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}

		public String getObjectArchive() {
			return objectArchive;
		}

		public void setObjectArchive(String objectArchive) {
			this.objectArchive = objectArchive;
		}

		private Drive drive;
		private int driveIndex;
		private String operatingSystem;

		private String imageId;
		private String imageArchive;

		private String objectId;
		private String objectArchive;



		public int getDriveIndex() {
			return driveIndex;
		}

		public void setDriveIndex(int driveIndex) {
			this.driveIndex = driveIndex;
		}

		public String getOperatingSystem() {
			return operatingSystem;
		}

		public void setOperatingSystem(String operatingSystem) {
			this.operatingSystem = operatingSystem;
		}
	}
}
