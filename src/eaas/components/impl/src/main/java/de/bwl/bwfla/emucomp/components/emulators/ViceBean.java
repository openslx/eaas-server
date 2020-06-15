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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Binding;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.Nic;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;
import de.bwl.bwfla.emucomp.api.EmulatorUtils.XmountOutputFormat;
import org.apache.tamaya.inject.api.Config;

import javax.inject.Inject;


public abstract class ViceBean extends EmulatorBean
{
	private static final int FLOPPY_DRIVES_NUM = 4;
	private static final int FLOPPY_DRIVE_BASE_ID     = 8;
	
	private int numAttachedDrives = 0;

	@Inject
	@Config("components.vice_defaults_dir")
	protected String defaultsDirectory;

	@Override
	protected String getEmuContainerName(MachineConfiguration env)
	{
		return "vice-sdl";
	}

	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		emuConfig.setHardTermination(true);

		Path images = null;
		try {
			images = this.getDataDir().resolve("images");
			Files.createDirectory(images);
			
			// Create local alias-links for image bindings,
			// to be used with emulator's attach/detach GUI 
			for (Map.Entry<String, Binding> entry : bindings.entries().entrySet()) {
				final String name = entry.getKey();
				final Binding binding = entry.getValue();
				LOG.info("Creating alias-link for binding: " + name);
				try {
					Path imgpath = Paths.get(this.lookupResource("binding://" + name, XmountOutputFormat.RAW));
					String imgname = binding.getLocalAlias();
					if (imgname == null || imgname.isEmpty()) {
						LOG.info("No alias set, skipping: " + name);
						continue;
					}

					Path imglink = images.resolve(imgname);
					imgpath = images.relativize(imgpath);
					Files.createSymbolicLink(imglink, imgpath);
				} catch (IOException exception) {
					LOG.warning("Creating alias-link for binding '" + name + "' failed!");
					LOG.log(Level.SEVERE, exception.getMessage(), exception);
				}
			}
		}
		catch (IOException exception) {
			LOG.warning("Creating images directory failed!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
		}
		
		// Common setup for all VICE emulators.
		// Expected to be called by subclasses!
		
		emuRunner.setWorkingDirectory(this.getDataDir());

		// Set defaults, if available
		try {
			final Path target = Paths.get(defaultsDirectory);
			if (Files.exists(target)) {
				final Path basedir = this.getDataDir().resolve("vice-defaults");
				Files.createSymbolicLink(basedir, target);

				final Path vicerc = basedir.resolve("sdl-vicerc");
				if (Files.exists(vicerc))
					emuRunner.addArguments("-config", vicerc.toString());
			}
		}
		catch (Exception error) {
			throw new BWFLAException("Preparing vice-defaults directory failed!", error);
		}

		emuRunner.addArgument("-verbose");
		emuRunner.addArgument("-sound");
		emuRunner.addArguments("-VICIIborders", "3");
		emuRunner.addArguments("-VICIIfilter", "0");
		emuRunner.addArguments("-refresh", "10");

		if (this.isLocalModeEnabled()) {
			LOG.warning("Local-mode is currently not supported for VICE!");;
		}
		else if (this.isSdlBackendEnabled()) {
			// Set same audio parameters as libsdl, or else
			// VICE refuses to initialize sound device!
			emuRunner.addArguments("-sounddev", "sdl");
			emuRunner.addArguments("-soundrate", "22050");
			emuRunner.addArguments("-soundoutput", "2");
			emuRunner.addArguments("-soundoutput", "2");
			
			// Disable hardware-scaling (OpenGL)!
			emuRunner.addArgument("+VICIIhwscale");
		}
		
		emuRunner.addArguments("-chdir", images.toString());
		
		String config = this.getNativeConfig();
		if (config != null && !config.isEmpty()) {
			String[] tokens = config.trim().split("\\s+");
			for (String token : tokens) {
				if (token.isEmpty())
					continue;
				emuRunner.addArgument(token.trim());
			}
		}
	}

	@Override
	public boolean addDrive(Drive drive)
	{
		if (drive == null || (drive.getData() == null)) {
            LOG.warning("Drive doesn't contain an image, attach canceled.");
            return false;
        }

        try {
        	final String binding = this.getDriveBinding(drive);
	        if (binding == null || binding.isEmpty())
	        	return true;  // No disk-image to inject!
	        
	        // VICE supports:
	        //     => Floppy images in D64 and X64 formats, using devices 8-11
	   	    //     => Tape images in T64 format, using device 1

	        if (drive.getType() == DriveType.FLOPPY) {
	        	if (numAttachedDrives >= FLOPPY_DRIVES_NUM)
	        		return false;  // All devices occupied!
	        	
	        	String dnum = Integer.toString(FLOPPY_DRIVE_BASE_ID + numAttachedDrives);
	        	emuRunner.addArgument("-", dnum);
	        	emuRunner.addArgument(binding);
	        	
	        	if (drive.isBoot()) {
	        		// VICE uses always device 8 for autostart!
	        		emuRunner.addArguments("-autostart", binding);
	        	}
	        	
	        	++numAttachedDrives;
	        }
	        // TODO: add proper tape support!
	        //else if (drive.getType() == DriveType.TAPE) {
	        //	emuRunner.addArguments("-1", binding);
	        //}
	        else {
	        	throw new IllegalArgumentException("Unsupported drive type specified: " + drive.getType());
	        }
        }
        catch (Exception exception) {
        	LOG.warning("Adding drive failed!");
			LOG.log(Level.WARNING, exception.getMessage(), exception);
        	return false;
        }
        
		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean connect) throws BWFLAException
	{
		throw this.newNotSupportedException();
	}


	protected boolean addNic(Nic nic) throws BWFLAException
	{
		throw this.newNotSupportedException();
	}
	
	
	/* ==================== Internal Helpers ==================== */
	
	private String getDriveBinding(Drive drive) throws BWFLAException, IOException
	{
		return this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType()));
	}
}