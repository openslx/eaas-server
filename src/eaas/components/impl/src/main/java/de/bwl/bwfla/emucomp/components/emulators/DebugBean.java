package de.bwl.bwfla.emucomp.components.emulators;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Nic;
import org.apache.tamaya.inject.api.Config;

import javax.inject.Inject;


public class DebugBean extends EmulatorBean
{
	@Inject
	@Config("emucomp.debug_bean_enabled")
	public boolean isEnabled;

	@Override
	protected void prepareEmulatorRunner() throws BWFLAException
	{
		if(isEnabled) {
			String nativeConfig = this.getNativeConfig();
			emuRunner.setCommand(nativeConfig);
		}
	}

	@Override
	protected boolean addDrive(Drive drive)
	{
		return false;
	}

	@Override
	protected boolean connectDrive(Drive drive, boolean attach)
	{
		return false;
	}

	@Override
	protected boolean addNic(Nic nic)
	{
		return false;
	}
}
