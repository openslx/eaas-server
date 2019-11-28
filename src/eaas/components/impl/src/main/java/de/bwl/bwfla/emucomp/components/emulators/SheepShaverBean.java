package de.bwl.bwfla.emucomp.components.emulators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import javax.inject.Inject;

import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Nic;

/**
 * @author iv1004
 * 
 */
public class SheepShaverBean extends EmulatorBean
{
    @Inject
    @Config("components.binary.sheepshaver")
    private String sheepShaverBean;

	@Override
	protected String getEmuContainerName(MachineConfiguration env)
	{
		return "sheepshaver";
	}

	@Override
	public void prepareEmulatorRunner()
	{
		emuConfig.setHardTermination(true);

		// Initialize the process-runner
		emuRunner.setCommand(sheepShaverBean);

		HashMap<String, String>	conf = prepareConfig(this.getNativeConfig());
		for(String key : conf.keySet())
		{
			emuRunner.addArgument("--" + key.trim());
			if (conf.get(key) != null)
					emuRunner.addArgument(conf.get(key).trim());

		}
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
            return false;
        }

		switch (drive.getType()) {
		case FLOPPY:
            try {
                final Path link = this.getDataDir()
						.resolve("floppy-" + drive.getBus() + "-" + drive.getUnit() + ".img");

                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
                imagePath = link;
            } catch (IOException e) {
                LOG.warning("Cannot create generic symlink for floppy image, attach cancelled.");
                return false;
            }
			emuRunner.addArgument("--disk");
			emuRunner.addArgument(imagePath.toString());
			break;

		case DISK:
			emuRunner.addArgument("--disk");
			emuRunner.addArgument(imagePath.toString());
			break;

		case CDROM:
            try {
                final Path link = this.getDataDir()
						.resolve("cdrom-" + drive.getBus() + "-" + drive.getUnit() + ".iso");

                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
                imagePath = link;
            } catch (IOException e) {
                LOG.warning("Cannot create generic symlink for cdrom image, attach cancelled.");
                return false;
            }
			emuRunner.addArgument("--cdrom");
			emuRunner.addArgument(imagePath.toString());
			break;

		default:
			LOG.severe("Device type '" + drive.getType() + "' not supported yet.");
			return false;
		}

		return true;
	}

    @Override
    public int changeMedium(int containerId, String objReference)
            throws BWFLAException {
        throw new BWFLAException("Hotplug is not supported by this emulator");
    }

    @Override
    public boolean connectDrive(Drive drive, boolean attach) {
        // This method should never be called.
        LOG.severe("Hotplug is not supported by this emulator");
        LOG.info("The previous message cannot appear. Please verify that changeMedium is correctly overridden in BasiliskIIBean.");
        return false;

        // This code WOULD implement hotswapping media IF BasiliskII would allow
        // it
        /*
        if (drive == null) {
            LOG.warning("Drive is null, (de-)attach cancelled.");
            return false;
        }

        Path imagePath = Paths.get(this.lookupResource(drive.getData()));
        if (attach) {
            if (imagePath == null || !Files.exists(imagePath)) {
                LOG.warning("Drive doesn't reference a valid binding, attach cancelled.");
                return false;
            }
        } else {
            imagePath = Paths.get("/dev/null");
        }

        switch (drive.getType()) {
        case FLOPPY:
            try {
                Path link = this.tempDir.toPath().resolve(
                        Paths.get("floppy-" + drive.getBus() + "-"
                                + drive.getUnit() + ".img"));
                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
            } catch (IOException e) {
                LOG.severe("Could not remove symbolic link to floppy. Detach cancelled.");
                return false;
            }
            break;

        case DISK:
            LOG.warning("Hotplug for disk drives is not supported by this emulator.");
            return false;

        case CDROM:
            try {
                Path link = this.tempDir.toPath().resolve(
                        Paths.get("cdrom-" + drive.getBus() + "-"
                                + drive.getUnit() + ".iso"));
                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
            } catch (IOException e) {
                LOG.severe("Could not remove symbolic link to cdrom. Detach cancelled.");
                return false;
            }
            break;

        default:
            LOG.severe("Device type '" + drive.getType()
                    + "' not supported yet.");
            return false;
        }
        return true;
        */
    }

	@Override
	protected boolean addNic(Nic nic) {
		emuRunner.addArguments("--switch", this.getNetworksDir().resolve("nic_" + nic.getHwaddress()).toString());
		return true;
	}

	private HashMap prepareConfig(String config)
	{
		HashMap<String,String> confValues = loadDefaults();

		if(config == null)
			return confValues;

		String[] tokens = config.trim().split("\n");
		for (String token : tokens) {
			String[] args = token.trim().split("\\s+");
			if (args.length < 1 || args.length > 2) {
				LOG.warning("check your native config file, some 'param-value' pairs are malformed");
				continue;
			}

			confValues.put(args[0], args[1]);
			EmulatorBeanMode mode = this.getEmuBeanMode();
			if(mode == EmulatorBeanMode.XPRA)
			{
				String screenConf = confValues.get("screen");
				if(screenConf != null)
					screenConf = screenConf.replace("dga", "win");
				confValues.put("screen", screenConf);
			}
		}
		return confValues;
	}

	private HashMap loadDefaults()
	{
		HashMap<String, String> defValues = new HashMap<String,String>();
		final String defaultsName = "sheepshaver.defaults";
		ClassLoader cloader = this.getClass().getClassLoader();

		try (InputStream instream = cloader.getResourceAsStream(defaultsName);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(instream)))
		{
			String line = reader.readLine();
			while (line != null)
			{
				String[] args = line.trim().split("\\s+");
				if (args.length < 1 || args.length > 2) {
					LOG.warning("check your defaults file, some 'param-value' pairs are malformed");
					continue;
				}

				if(args.length == 2)
					defValues.put(args[0], args[1]);
				else
					defValues.put(args[0], null);
				line = reader.readLine();
			}

		} catch (IOException e) {
			LOG.info(e.getMessage());
		}

		return defValues;
	}
}
