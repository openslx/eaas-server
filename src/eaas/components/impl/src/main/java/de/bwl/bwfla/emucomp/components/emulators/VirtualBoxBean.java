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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.inject.Inject;

import de.bwl.bwfla.emucomp.api.*;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.IllegalEmulatorStateException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;
import de.bwl.bwfla.emucomp.api.EmulatorUtils.XmountOutputFormat;


/**
 * 
 * @author Johann Latocha <johann.latocha@rz.uni-freiburg.de>
 * @author Dennis Wehrle <dennis.wehrle@rz.uni-freiburg.de>
 * 
 */
public class VirtualBoxBean extends EmulatorBean
{
	private String vboxmanage_bin = null;
	private File vboxhome;
	
	private String vmname = null;
	
    @Inject
    @Config("components.binary.vbox.headlessexec")
    private String vboxHeadlessExec;
    
    @Inject
    @Config("components.binary.vbox.manageexec")
    private String vboxManageExec;

	@Override
	protected String getEmuContainerName(MachineConfiguration env)
	{
		return "virtualbox";
	}

	@Override
	protected XmountOutputFormat getImageFormatForDriveType(DriveType driveType) {
        switch (driveType) {
        case DISK:
            return XmountOutputFormat.VDI;
        default:
            return XmountOutputFormat.RAW;
        }
	}
	
	@Override
	public Set<String> getHotplugableDrives()
	{
		HashSet<String> set = new HashSet<String>();
		set.add(DriveType.CDROM.name());
		set.add(DriveType.DISK.name());
		set.add(DriveType.FLOPPY.name());
		return set;
	}
	
	@Override
	protected void prepareEmulatorRunner() throws BWFLAException {
		// setup bean prerequisites
		File file = new File(vboxManageExec);
		if (file == null || !file.exists())
			throw new BWFLAException(
					"VBoxManage executable not found! Make sure you have specified a valid path to your "
							+ "executable in the corresponding 'properties' file");
		vboxmanage_bin = file.getAbsolutePath();
		vboxhome = new File(System.getProperty("java.io.tmpdir") + File.separator + "vbox");
		if (!vboxhome.exists()) {
			if (!vboxhome.mkdirs()) {
				throw new BWFLAException("Failed to create temporary vboxhome directory: " + vboxhome.toString());
			}
		}
		
		// create and register new VM
		vmname = this.getWorkingDir().getFileName().toString();
		vboxmanage(false, // don't wait for lock!
		           "createvm", "--name", vmname,
				               "--basefolder", this.getDataDir().toString(),
				               "--register");
        // set boot order
        vboxmanage("modifyvm", vmname, "--boot1", "disk",
                                       "--boot2", "dvd",
                                       "--boot3", "floppy",
                                       "--boot4", "net");

		if (this.emuEnvironment.getEmulator() != null &&
		    this.emuEnvironment.getEmulator().getMachine() != null &&
		    this.emuEnvironment.getEmulator().getMachine().getValue() != null) {
			String machDef = this.emuEnvironment.getEmulator().getMachine().getValue();
			for (String parameter : machDef.split("(\r?\n)+")) {
				if (parameter.trim().isEmpty()) {
					continue;
				}
				String[] args = parameter.trim().split("\\s+", 2);
				if (args.length > 1) {
					if (args[0].startsWith("--")) {
						vboxmanage("modifyvm", vmname, args[0], args[1]);
					} else {
						vboxmanage("modifyvm", vmname, "--" + args[0], args[1]);
					}
				} else if (args.length == 1) {
					vboxmanage("modifyvm", vmname, args[0], "");
				}
			}
		} else {
			// default vm configuration
			vboxmanage("modifyvm", vmname,  "--hwvirtex", "on",
											"--memory", "1024",
											"--ioapic", "on",
											"--mouse", "usbtablet",
											"--audio", "alsa",
											"--audiocontroller", "ac97");
		}
		vboxmanage("storagectl", vmname, "--name", "ide_1",
										 "--add", "ide",
										 "--bootable", "on");
		vboxmanage("storagectl", vmname, "--name", "fdc_1",
									     "--add", "floppy",
									     "--bootable", "on");

		// apply native configuration
		String nativeConfig = this.getNativeConfig();
		if (nativeConfig != null && !nativeConfig.isEmpty()) {
			String[] tokens = nativeConfig.trim().split("\n");
			for (String token : tokens)
			{
				token = token.trim();
				if(token.isEmpty())
						continue;
				vboxmanage(token.replace("$VMUUID", vmname).split("\\s+"));
			}
		}
		
		// setup emulator runner for the new VM
		// get VBoxHeadless executable
		File vboxheadless = new File(vboxHeadlessExec);
		if (vboxheadless == null || !vboxheadless.exists()) {
			throw new BWFLAException("VBoxHeadless executable not found! Make sure you have specified "
							+ "a valid path to your executable in the corresponding 'properties' file");
		}
		
		emuRunner.setCommand(vboxheadless.getAbsolutePath());
		emuRunner.addArgument("--startvm");
		emuRunner.addArgument(vmname);
		if (this.isLocalModeEnabled()) {
			emuRunner.addArgument("--fullscreen");
			emuRunner.addArgument("--fullscreenresize");
		}

		// HINT: if not running vboxsdl, configure and use the
		// 		 VRDE/RDP remote desktop protocol

		this.emuRunner.addEnvVariable("VBOX_USER_HOME", vboxhome.getAbsolutePath());
	}
	
	@Override
	public void destroy() {
		vboxmanage("unregistervm", vmname);
		super.destroy();
	}

    protected void vboxmanageWaitForLock() {
        while (true) {
            DeprecatedProcessRunner proc = new DeprecatedProcessRunner("listvms.sh");
            try {
                proc.addEnvVariable("VBOX_USER_HOME", vboxhome.getAbsolutePath());
                proc.addArguments(this.vmname);
                proc.start();
                proc.waitUntilFinished();
                String out = proc.getStdOutString();
                if (out.contains("1")) {
                    break;
                }
                proc.cleanup();
                Thread.sleep(1000);
            } catch(Exception e) {
                LOG.severe("Could not wait for vboxmanage to be ready. Following calls to vboxmanage will probably fail.");
                return;
            } finally {
            }
        }
    }
	
	protected void vboxmanage(boolean waitForLock, List<String> args) {
	    if (waitForLock) {
	       //  this.vboxmanageWaitForLock();
	    }
		DeprecatedProcessRunner proc = new DeprecatedProcessRunner(this.vboxmanage_bin);
		proc.addArguments(args);
		proc.addEnvVariable("VBOX_USER_HOME", vboxhome.getAbsolutePath());
		proc.execute();
	}
	
	protected void vboxmanage(List<String> args) {
	    vboxmanage(true, args);
	}
	
	protected void vboxmanage(boolean waitForLock, String...args) {
		vboxmanage(waitForLock, new ArrayList<String>(Arrays.asList(args)));
	}
	
	protected void vboxmanage(String...args) {
	    vboxmanage(true, args);
	}
	
	protected boolean mediumHasUuid(String type, Path path) {
	    boolean result = false;
	    DeprecatedProcessRunner proc = null;
        
        try {
    	    proc = new DeprecatedProcessRunner(this.vboxmanage_bin);
    	    proc.addArguments("list", type);
    	    proc.start();
    	    proc.waitUntilFinished();
    	    String stdout = proc.getStdOutString();
            result = stdout.contains(path.toString());
        } catch (IOException e) {
            return false;
        } finally {
            proc.cleanup();
        }
        return result;
	}
	
    @Override
    public DataHandler detachMedium(int containerId) throws BWFLAException {
        /*
         * VirtualBox resources are all mounted as VDI image, the
         * VolatileResource's resourcePath is therefore also a VDI which we
         * cannot handle when downloading attached files. Therefore, we re-mount
         * the image here (which is fine as the emulator is already stopped) as
         * RAW and set the resourcePath to the new RAW path.
         */

        synchronized (emuBeanState) {
            final EmuCompState curstate = emuBeanState.get();
            if (curstate != EmuCompState.EMULATOR_READY
                    && curstate != EmuCompState.EMULATOR_RUNNING
                    && curstate != EmuCompState.EMULATOR_STOPPED) {
                String message = "Cannot detach medium from emulator!";
                throw new IllegalEmulatorStateException(message, curstate);
            }
        }

        List<AbstractDataResource> bindings = this.emuEnvironment
                .getAbstractDataResource();

        if (bindings != null) {
            for (AbstractDataResource aBinding : bindings) {
                if (!(aBinding instanceof VolatileResource))
                    continue;

                VolatileResource binding = (VolatileResource) aBinding;
                String id = "attached_container_" + containerId;
                String bindingId = binding.getId();

                if (id.equalsIgnoreCase(bindingId)) {
                    try {
                        // remount the image as raw and set resourcePath
                        Path resourceDir = this.getBindingsDir();
                        Path cowPath = resourceDir
                                .resolve(binding.getId() + ".cow");
                        Path fuseMountpoint = cowPath.resolveSibling(
                                cowPath.getFileName() + ".dd.fuse");
                        String resourcePath;
                        resourcePath = EmulatorUtils.mountCowFile(cowPath,
                                fuseMountpoint)
                                .toString();
                        binding.setResourcePath(resourcePath);
                    } catch (IllegalArgumentException | IOException e) {
                        throw new BWFLAException(
                                "Could not remount image file as RAW.", e);
                    }

                    break;
                }
            }
        }
        return super.detachMedium(containerId);
    }
	
    @Override
    protected boolean addDrive(Drive drive) {
    	if (drive == null || (drive.getData() == null)) {
            LOG.warning("Drive doesn't contain an image, attach canceled.");
            return false;
        }
        
        Path imagePath = null;
        try {
            imagePath = Paths.get(this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType())));
        } catch (Exception e) {
            LOG.warning("Drive doesn't reference a valid binding, attach cancelled.");
            return false;
        }

        List<String> args = new ArrayList<String>();
        args.add("storageattach");
        args.add(vmname);

        switch (drive.getType()) {
        case FLOPPY:
            try {
                Path link = this.getDataDir().resolve(imagePath.getFileName().toString() + ".img");
                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
                imagePath = link;
            } catch (IOException e) {
                LOG.warning("Cannot create .img link for vbox, attach cancelled.");
                return false;
            }
            args.add("--storagectl");
            args.add("fdc_1");

            args.add("--type");
            args.add("fdd");

            break;

        case DISK:
            args.add("--storagectl");
            args.add("ide_1");

            args.add("--type");
            args.add("hdd");

            args.add("--setuuid");
            args.add("");

            break;

        case CDROM:
            try {
                Path link = this.getDataDir().resolve(imagePath.getFileName().toString() + ".iso");
                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
                imagePath = link;
            } catch (IOException e) {
                LOG.warning("Cannot create .iso link for vbox, attach cancelled.");
                return false;
            }
            args.add("--storagectl");
            args.add("ide_1");

            args.add("--type");
            args.add("dvddrive");

            if (!this.mediumHasUuid("dvds", imagePath)) {
                args.add("--setuuid");
                args.add("");
            }

            break;

        default:
            LOG.severe("Device type '" + drive.getType()
                    + "' not supported yet.");
            return false;
        }
        args.addAll(Arrays.asList("--port", drive.getBus(), "--device",
                drive.getUnit(), "--medium", imagePath.toString()));

        vboxmanage(args);
        return true;
    }

	@Override
    protected boolean connectDrive(Drive drive, boolean attach) {
        if (!attach) {
            if (drive == null) {
                LOG.warning("Drive is null, de-attach cancelled.");
                return false;
            }
            String imagePath = "emptydrive";

            List<String> args = new ArrayList<String>();
            args.add("storageattach");
            args.add(vmname);

            List<String> closeMediumArgs = new ArrayList<String>();

            switch (drive.getType()) {
            case FLOPPY:
                args.add("--storagectl");
                args.add("fdc_1");

                args.add("--type");
                args.add("fdd");

                closeMediumArgs.add("floppy");

                break;

            case DISK:
                args.add("--storagectl");
                args.add("ide_1");

                args.add("--type");
                args.add("hdd");

                closeMediumArgs.add("disk");

                break;

            case CDROM:
                args.add("--storagectl");
                args.add("ide_1");

                args.add("--type");
                args.add("dvddrive");

                closeMediumArgs.add("dvd");

                break;
            default:
                LOG.severe("Device type '" + drive.getType()
                        + "' not supported yet.");
                return false;
            }
            args.addAll(Arrays.asList("--port", drive.getBus(),
                    "--device", drive.getUnit(), "--medium", imagePath));

            vboxmanage(args);

            return true;
        } else {
            return this.addDrive(drive);
        }
    }
	
	@Override
	protected boolean addNic(Nic nic) {
		if (nic == null) {
			LOG.warning("NIC is null, attach canceled.");
			return false;
		}
		
		vboxmanage("modifyvm", vmname, "--nic1", "generic");
		vboxmanage("modifyvm", vmname, "--nicgenericdrv1", "VDE");
		vboxmanage("modifyvm", vmname, "--nicproperty1", "network=" + this.getNetworksDir().resolve("nic_" + nic.getHwaddress()));
		vboxmanage("modifyvm", vmname, "--macaddress1", nic.getHwaddress().replace(":", ""));

		return true;
	}
}
