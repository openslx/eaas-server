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

package de.bwl.bwfla.imagebuilder;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;

import java.nio.file.Path;
import java.util.logging.Logger;


public class PartitionTableMakerMBR implements IPartitionTableMaker
{
	public void execute(Path device, int partStartOffset, String fsType, Logger log) throws BWFLAException
	{
		if(fsType.equalsIgnoreCase("vfat"))
			fsType = "fat32";
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("parted");
		process.addArgument("--script");
		process.addArguments("--align", "optimal");
		process.addArguments(device.toString(), "mklabel", "msdos");
		process.addArguments("mkpart", "primary", fsType, partStartOffset + "B", "100%");
		if (!process.execute())
			throw new BWFLAException("Partitioning '" + device.toString() + "' failed!");
	}
}
