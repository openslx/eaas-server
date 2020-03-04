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


public class FileSystemMakerNTFS implements IFileSystemMaker
{
	@Override
	public void execute(Path device, Logger log) throws BWFLAException
	{
		execute(device,null, log);
	}

	@Override
	public void execute(Path device, String label, Logger log) throws BWFLAException {
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("mkfs.ntfs");
		if(label != null)
			process.addArguments("-L", label);
		process.addArgument("--force");
		process.addArgument(device.toString());
		if (!process.execute())
			throw new BWFLAException("Creating NTFS-filesystem failed!");
	}
}
