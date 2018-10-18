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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Nic;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.MessageType;


public abstract class PceBean extends EmulatorBean
{
	// Member fields
	private final Map<String, Drive> deviceToDriveMapping = new TreeMap<String, Drive>();
	private final Map<Drive, String> driveToDeviceMapping = new TreeMap<Drive, String>(new DriveComparator());
	
	@SuppressWarnings("unchecked")
	private final Deque<String>[] freeDeviceIds = (Deque<String>[]) new Deque[3];
	
	
	public PceBean()
	{
		super();


		final int[] drivesNumber = this.getDrivesNumber();
		final int[] drivesBaseIds = this.getDrivesBaseIds();
		
		// Initialize the device IDs
		for (int i = 0; i < 2; ++i) {
			final int baseid = drivesBaseIds[i];
			final int numDrives = drivesNumber[i];
			freeDeviceIds[i] = new ArrayDeque<String>(numDrives);

			for (int drive = 0; drive < numDrives; ++drive)
				freeDeviceIds[i].add(Integer.toString(baseid + drive));
		}
	}

	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		emuRunner.setCommand(getExecPath());
		emuRunner.setWorkingDirectory(this.getDataDir());
		String config = this.getNativeConfig();
		if (config != null && !config.isEmpty())
			LOG.warning("PCE emulators currently do not support native configuration!");

		// Use SDL devices
		emuRunner.addArguments("--terminal" , "sdl");
		emuRunner.addArgument("--no-monitor");
		emuRunner.addArgument("--verbose");
		emuRunner.addArgument("--run");

		if (this.isLocalModeEnabled())
			LOG.warning("Local-mode is currently not supported for PCE!");;

		deviceToDriveMapping.clear();
		driveToDeviceMapping.clear();
	}

	@Override
	public void finishRuntimeConfiguration() throws BWFLAException
	{
		// At this point we should have all runtime information
		
		try {
			// Write the local config file
			final File configFile = new File(this.getDataDir().toFile(), "pce.cfg");
			List<String> configTemplate = this.getConfigTemplate();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
				for (String line : configTemplate)
					this.writeln(writer, line);

				// Write the disk blocks
				
				this.writeln(writer, 1);
				this.writeln(writer, "# Disk blocks added by PceBean");
				this.writeln(writer, 1);
				
				for (Map.Entry<String, Drive> entry : deviceToDriveMapping.entrySet()) {
					final String image = this.getImagePath(entry.getValue());
					this.writeln(writer, "disk {");
					this.writeln(writer, "drive", entry.getKey(), false);
					this.writeln(writer, "optional", "1", false);
					this.writeln(writer, "type", "auto");
					this.writeln(writer, "file", image);
					this.writeln(writer, "}");
					this.writeln(writer, 1);
				}
				
				writer.flush();
				writer.close();
			}
			
			emuRunner.addArguments("--config", configFile.getAbsolutePath());
		}
		catch (IOException exception) {
			throw new BWFLAException("Writing pce.cfg file failed!", exception);
		}
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
				this.attachDrive(0, drive);
				break;
				
			case DISK:
				this.attachDrive(1, drive);
				break;
				
			default:
				LOG.warning("PCE does not support " + drive.getType() + " drives!");
		}

		return false;
	}
	
	@Override
	public boolean connectDrive(Drive drive, boolean connect)
	{
		final int tid = (drive.getType() == DriveType.FLOPPY) ? 0 : 1;
		String device = driveToDeviceMapping.get(drive);
		if (device != null) {
			// Detach this drive
			deviceToDriveMapping.remove(device);
			driveToDeviceMapping.remove(drive);
			
			// Mark device ID as free
			freeDeviceIds[tid].push(device);
			
			try {
				// Eject this drive from PCE emulator
				ctlMsgWriter.begin(MessageType.EMULATOR_COMMAND);
				ctlMsgWriter.write("m emu.disk.eject " + device, false);
				ctlMsgWriter.send(emuCtlSocketName, true);
			}
			catch (IOException exception) {
				LOG.warning("Sending eject-message to emulator failed!");
				LOG.log(Level.WARNING, exception.getMessage(), exception);
			}
		}
		
		if (!connect)
			return true;  // Drive detached!
		
		this.attachDrive(tid, drive);
		
		try {
			final String path = this.getImagePath(drive);
			device = driveToDeviceMapping.get(drive);
			
			// Insert this drive to PCE emulator
			ctlMsgWriter.begin(MessageType.EMULATOR_COMMAND);
			ctlMsgWriter.write("m emu.disk.insert " + device + ":" + path, false);
			ctlMsgWriter.send(emuCtlSocketName, true);
		}
		catch (IOException exception) {
			LOG.warning("Sending insert-message to emulator failed!");
			exception.printStackTrace();
		}
		
		return true;
	}

	protected boolean addNic(Nic nic)
	{
		LOG.severe("operation unsupported yet: " + this.getClass().getEnclosingMethod().getName());
		return false;
	}

	
	protected abstract int[] getDrivesNumber();
	protected abstract int[] getDrivesBaseIds();
	protected abstract String getConfigTemplatePath();

	
	/* =============== Internal Helpers =============== */
	
	private String getImagePath(Drive drive)
	{
		try {
			return this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType()));
		} catch (Exception exception) {
			LOG.warning("Drive doesn't reference a valid binding, attach canceled.");
			exception.printStackTrace();
			return null;
		}
	}

	private List<String> getConfigTemplate() throws IOException
	{
		ClassLoader cloader = this.getClass().getClassLoader();
		String name = this.getConfigTemplatePath();
		
		LOG.info("Using config template: " + name);
		
		// Read the contents of the template file
		InputStream instream = cloader.getResourceAsStream(name);
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
		List<String> template = new ArrayList<String>(512);
		while (reader.ready())
			template.add(reader.readLine());
		
		return template;
	}

	private boolean attachDrive(int tid, Drive drive)
	{
		final String device = freeDeviceIds[tid].poll();
		if (device == null) {
			LOG.warning("Attempt to attach more than " + this.getDrivesNumber()[tid] + " drives! Aborting.");
			return false;
		}

		// Update drive configuration
		deviceToDriveMapping.put(device, drive);
		driveToDeviceMapping.put(drive, device);
		
		return true;
	}
	
	private void writeln(BufferedWriter writer, int numNewLines) throws IOException
	{
		while (numNewLines > 0) {
			writer.newLine();
			--numNewLines;
		}
	}
	
	private void writeln(BufferedWriter writer, String value) throws IOException
	{
		writer.write(value);
		writer.newLine();
	}
	
	private void writeln(BufferedWriter writer, String key, String value) throws IOException
	{
		this.writeln(writer, key, value, true);
	}
	
	private void writeln(BufferedWriter writer, String key, String value, boolean quote) throws IOException
	{
		writer.write("    ");
		writer.write(key);
		writer.write(" = ");
		
		if (quote)
			writer.write("\"");
		
		writer.write(value);
		
		if (quote)
			writer.write("\"");
		
		writer.newLine();
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
	 abstract protected String getExecPath();
}