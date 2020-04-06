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

package de.bwl.bwfla.emucomp.components.emulators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Level;

import javax.inject.Inject;

import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Nic;


public class KegsBean extends EmulatorBean
{
    @Inject
    @Config("components.binary.kegs")
    public String kegsBean;
    
	/** Virtual device ID of the drive with support for autostarting. */
	private static final String AUTOSTART_DEVICE_ID = "s7d1";
	
	/** Max. number of supported drives per slot. */
	private static final int[] MAX_DRIVES_NUMBER = { 2, 2, 32 };

	/** ID of the first usable slot. */
	private static final int BASE_SLOT_ID = 5;

	// Member fields
	private final Map<String, Drive> deviceToDriveMapping = new TreeMap<String, Drive>();
	private final Map<Drive, String> driveToDeviceMapping = new TreeMap<Drive, String>(new DriveComparator());
	
	@SuppressWarnings("unchecked")
	private final Deque<String>[] freeDeviceIds = (Deque<String>[]) new Deque[3];
	
	
	public KegsBean()
	{
		super();

		final StringBuilder sbuilder = new StringBuilder(32);
		
		// Initialize the device IDs
		for (int i = 0; i < 3; ++i) {
			final int slot = BASE_SLOT_ID + i;
			int numDrives = MAX_DRIVES_NUMBER[i];
			freeDeviceIds[i] = new ArrayDeque<String>(numDrives);
			
			// Device prefix
			sbuilder.setLength(0);
			sbuilder.append('s');
			sbuilder.append(slot);
			sbuilder.append('d');

			final String prefix = sbuilder.toString();
			for (int drive = (slot != 7) ? 1 : 2; drive <= numDrives; ++drive)
				freeDeviceIds[i].add(prefix + drive);
		}
	}

	@Override
	protected String getEmuContainerName(MachineConfiguration env)
	{
		return "kegs-sdl";
	}

	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		emuRunner.setCommand(kegsBean);
		emuRunner.setWorkingDirectory(this.getDataDir());
		String config = this.getNativeConfig();
		if (config != null && !config.isEmpty()) {
			String[] tokens = config.trim().split("\\s+");
			for (String token : tokens)
			{
				if(token.isEmpty())
					continue;
				emuRunner.addArgument(token.trim());
			}
		}

		// Use SDL devices
		emuRunner.addArguments("--skip" , "0");
		emuRunner.addArguments("--audio", "2");
		emuRunner.addArguments("--video", "3");

		deviceToDriveMapping.clear();
		driveToDeviceMapping.clear();
	}

	@Override
	public void finishRuntimeConfiguration() throws BWFLAException
	{
		// Kegs expects a ROM image in its working directory
		try {
			final String romPath = this.getRomPath();
			if (romPath == null || romPath.isEmpty())
				throw new BWFLAException("No binding for ROM image specified!");

			LOG.info("Creating a link to the ROM file...");
			Path link = this.getDataDir().resolve("ROM");
			Path target = Paths.get(romPath);
			Files.createSymbolicLink(link, target);
		}
		catch (IOException exception) {
			throw new BWFLAException(exception);
		}
		
		this.updateDiskConfFile();
	}

	@Override
	public boolean addDrive(Drive drive)
	{
		if (drive == null || (drive.getData() == null)) {
			LOG.warning("Drive doesn't contain an image, attach canceled.");
			return false;
		}

		final String image = this.getImagePath(drive);
		if (image == null)
			return false;

		switch (drive.getType())
		{
			case FLOPPY:
			case DISK:
				
				if (drive.isBoot()) {
					// Autostarting is only supported using slot 7, device 1
					if (deviceToDriveMapping.containsKey(AUTOSTART_DEVICE_ID))
						LOG.warning("Drive " + AUTOSTART_DEVICE_ID + " already contains an image! Replacing images.");
					
					// Replace the old mapping
					deviceToDriveMapping.put(AUTOSTART_DEVICE_ID, drive);
					driveToDeviceMapping.put(drive, AUTOSTART_DEVICE_ID);
					return true;
				}
				
				if (image.contains(".dsk") || image.contains(".do")) {
					// We have a 140K 5.25" floppy, in DOS 3.3 format.
					// It can be loaded into slot 6, drives 1 and 2.
					return this.attachDriveIntoSlot(6, drive);
				}
				else if (image.contains(".po")) {
					// We have a 800K 3.5" floppy, in ProDOS format.
					// It can be loaded into slot 5, drives 1 and 2.
					return this.attachDriveIntoSlot(5, drive);
				}
				else {
					// Unknown image type, try to load it into slot 7 (drives 1 to 32).
					return this.attachDriveIntoSlot(7, drive);
				}
				
			default:
				LOG.warning("KEGS does not support " + drive.getType() + " drives!");
		}

		return false;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean connect)
	{
		final String device = driveToDeviceMapping.get(drive);
		if (device != null) {
			// Detach this drive
			deviceToDriveMapping.remove(device);
			driveToDeviceMapping.remove(drive);
			
			// Mark device ID as free
			final int slot = Character.digit(device.charAt(1), 10);
			final int sidx = slot - BASE_SLOT_ID;
			freeDeviceIds[sidx].push(device);
		}
		
		if (!connect)
			return true;  // Drive detached!
		
		final boolean result = this.addDrive(drive);
		try {
			this.updateDiskConfFile();
		}
		catch (BWFLAException exception) {
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
		}
		
		return result;
	}


	protected boolean addNic(Nic nic) throws BWFLAException
	{
		throw this.newNotSupportedException();
	}

	private String getImagePath(Drive drive)
	{
		try {
			return this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType()));
		} catch (Exception exception) {
			LOG.warning("Drive doesn't reference a valid binding, attach cancelled.");
			LOG.log(Level.WARNING, exception.getMessage(), exception);
			return null;
		}
	}

	private String getRomPath() throws BWFLAException, IOException
	{
		return this.lookupResource("binding://rom", this.getImageFormatForDriveType(Drive.DriveType.DISK));
	}
	
	private boolean attachDriveIntoSlot(int slot, Drive drive)
	{
		final int sidx = slot - BASE_SLOT_ID;
		final String device = freeDeviceIds[sidx].poll();
		if (device == null) {
			LOG.warning("Attempt to attach more than " + MAX_DRIVES_NUMBER[sidx] + " drives to slot " + slot + "! Aborting.");
			return false;
		}

		// Update drive configuration
		deviceToDriveMapping.put(device, drive);
		driveToDeviceMapping.put(drive, device);
		
		return true;
	}
	
	public void updateDiskConfFile() throws BWFLAException
	{
		final Function<String, String> hostPathReplacer = this.getContainerHostPathReplacer();

		// Compose the disks configuration
		File diskConfFile = new File(this.getDataDir().toFile(), "disk_conf");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(diskConfFile))) {
			for (Map.Entry<String, Drive> entry : deviceToDriveMapping.entrySet()) {
				Drive drive = entry.getValue();
				String image = this.getImagePath(drive);
				if (image == null)
					continue;  // No media to inject!

				if (this.isContainerModeEnabled())
					image = hostPathReplacer.apply(image);

				writer.write(entry.getKey());
				writer.write(" = ");
				writer.write(image);
				writer.newLine();
			}
		} catch (IOException exception) {
			throw new BWFLAException("Writing disk_conf file failed!", exception);
		}
	}
	
	
	private static class DriveComparator implements Comparator<Drive>
	{
		@Override
		public int compare(Drive d1, Drive d2)
		{
			if (d1 == d2)
				return 0;
			
			if (d1.hashCode() < d2.hashCode())
				return -1;
			
			return 1;
		}
	}
}