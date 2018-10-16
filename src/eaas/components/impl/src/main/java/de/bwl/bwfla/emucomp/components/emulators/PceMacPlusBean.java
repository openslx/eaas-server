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

import javax.inject.Inject;

import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import org.apache.tamaya.inject.api.Config;



public class PceMacPlusBean extends PceBean
{
    @Inject
    @Config("components.binary.pce.macplus")
    protected String emuExecPath;

	/** Max. number of supported drives per type. */
	private static final int[] DRIVES_NUMBER = { 2, 4 };

	/** ID of the first usable slot. */
	private static final int[] DRIVES_BASE_IDS = { 0, 128 };
	
	
	public PceMacPlusBean()
	{
		super();
	}

	@Override
	protected String getEmuContainerName(MachineConfiguration env)
	{
		return "pce-macplus";
	}

	@Override
	protected int[] getDrivesNumber()
	{
		return DRIVES_NUMBER;
	}

	@Override
	protected int[] getDrivesBaseIds()
	{
		return DRIVES_BASE_IDS;
	}
	
	@Override
	protected String getConfigTemplatePath()
	{
		String model = emuEnvironment.getModel();
		if (model == null || model.isEmpty()) {
			model = "classic";
			LOG.warning("Emulator's model was not set! Using default '" + model + "'.");
		}
		
		String arch = emuEnvironment.getArch();
		if (arch == null || arch.isEmpty()) {
			arch = "68000";
			LOG.warning("Emulator's architectrue was not set! Using default '" + arch + "'.");
		}
		
		return "pce/macplus/macplus-" + model + "-" + arch + ".cfg";
	}

	protected String getExecPath()
	{
		return emuExecPath;
	}
}