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
import de.bwl.bwfla.emucomp.api.FileSystemType;

import java.nio.file.Path;
import java.util.logging.Logger;


public class FileSystemMakerFAT implements IFileSystemMaker
{
	private final FileSystemType fstype;

	public FileSystemMakerFAT(FileSystemType fstype)
	{
		switch (fstype) {
			case VFAT:
			case FAT16:
			case FAT32:
				this.fstype = fstype;
				break;

			default:
				throw new IllegalArgumentException("Specified filesystem type is not a variant of FAT!");
		}
	}

	public void execute(Path device, Logger log) throws BWFLAException
	{
		execute(device, null, log);
	}

	@Override
	public void execute(Path device, String label, Logger log) throws BWFLAException {
		if(label != null && label.length() > 11)
			label = label.substring(0, Math.min(label.length(), 11));

		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("mkfs.fat");
		if(label != null)
			process.addArguments("-n", label);
		switch (fstype) {
			case FAT16:
				process.addArguments("-F", "16");
				break;

			case FAT32:
				process.addArguments("-F", "32");
				break;

			case VFAT:
				// Use auto-detection!
				break;
		}

		process.addArgument(device.toString());
		if (!process.execute())
			throw new BWFLAException("Creating " + fstype.toString() + "-filesystem failed!");
	}
}
