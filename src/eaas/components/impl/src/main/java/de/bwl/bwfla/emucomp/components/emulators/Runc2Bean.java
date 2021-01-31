package de.bwl.bwfla.emucomp.components.emulators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.components.emulators.EmulatorRunMetadata;
import de.bwl.bwfla.emucomp.components.emulators.EmulatorRunMetadata.DriveBinding;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;
import de.bwl.bwfla.emucomp.api.Nic;

public class Runc2Bean extends EmulatorBean
{
	EmulatorRunMetadata config = new EmulatorRunMetadata();

	@Override
	protected String getEmuContainerName(MachineConfiguration machineConfiguration)
	{
		// currently metadata contains a emucon-rootfs/ prefix.
		// this is deprecated, add compat

		String containerName = machineConfiguration.getEmulator().getContainerName();
		if(containerName == null)
			throw new IllegalArgumentException();

		int lastIndex = containerName.lastIndexOf('/');
		if(lastIndex > 0)
			containerName = containerName.substring(lastIndex + 1);

		return containerName;
	}

	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		// TODO: Get this from the imported emulator image
		emuRunner.setCommand("/eaas-run");

		config.machineConfig = emuEnvironment;

	}

	@Override
	public boolean addDrive(Drive drive) {
		if (drive == null || (drive.getData() == null)) {
			LOG.warning("Drive doesn't contain an image, attach canceled.");
			return false;
		}

		Path imagePath = null;
		try {
			imagePath = Paths.get(this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType())));
		} catch (Exception e) {
			LOG.warning("Drive doesn't reference a valid binding, attach canceled.");
			e.printStackTrace();
			return false;
		}

		final DriveBinding binding = new DriveBinding();
		binding.drive = drive;
		binding.path = imagePath.toAbsolutePath().toString();
		config.getDrives().add(binding);

		return true;
	}

	@Override
	protected boolean connectDrive(Drive drive, boolean attach) throws BWFLAException {
		return false;
	}

	protected boolean addNic(Nic nic) {
		if (nic == null) {
			LOG.warning("NIC is null, attach canceled.");
			return false;
		}

		final String nicPath = this.getNetworksDir().resolve("nic_" + nic.getHwaddress()).toString();
		final EmulatorRunMetadata.NicBinding binding = new EmulatorRunMetadata.NicBinding();
		binding.nic = nic;
		binding.path = nicPath;
		config.getNics().add(binding);

		return true;
	}

	private String fmtDate(long epoch)
	{
		Date d = new Date(epoch);
		DateFormat format = new SimpleDateFormat("YYYY-MM-dd'T'hh:mm:ss"); // 2006-06-17T16:01:21
		String formatted = format.format(d);
		return formatted;
	}

	protected void setEmulatorTime(long epoch)
	{
		config.setStartTime(fmtDate(epoch));
	}

	@Override
	public void start() throws BWFLAException {
		emuRunner.addEnvVariable("EAAS_CONFIG", config.jsonValueWithoutRoot(false));
		super.start();
	}

	@Override
	public String stop() throws BWFLAException {
		return super.stop();
	}

	@Override
	public void destroy(){
		super.destroy();
	}
}
