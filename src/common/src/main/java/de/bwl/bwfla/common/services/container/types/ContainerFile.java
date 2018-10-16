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

package de.bwl.bwfla.common.services.container.types;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


public class ContainerFile extends File
{
	private static final long serialVersionUID = 8390719991154743221L;

	/** Logger instance. */
	private final Logger log = Logger.getLogger("ContainerFile");
	
	private final AtomicInteger numrefs = new AtomicInteger(0);
	private final AtomicBoolean isUsable = new AtomicBoolean(true);

	private static final int MAX_NUM_RETRIES  = 25;
	private static final int RETRY_TIMEOUT    = 1000;  // in ms
	
	
	public ContainerFile(String pathname)
	{
		super(pathname);
	}

	public ContainerFile(File file)
	{
		super(file.getAbsolutePath());
	}
	
	@Override
	public boolean delete()
	{
		isUsable.set(false);

		int numRetriesLeft = MAX_NUM_RETRIES;
		
		// Wait until all references are released
		while (numrefs.get() > 0) {
			try {
				Thread.sleep(RETRY_TIMEOUT);
			}
			catch (InterruptedException e) {
				// Ignore it!
			}
			
			if (--numRetriesLeft == 0)
				break;
		}
		
		final int refsleft = numrefs.get();
		if (refsleft > 0) {
			String message = "The file " + this.getAbsolutePath() + " has still " + refsleft + " reference(s) after timeout! Deleting it will probably fail.";
			log.warning(message);
		}
		
		final boolean success = super.delete();
		if (!success)
			log.warning(this.getAbsolutePath() + " could not be deleted!");
		
		return success;
	}
	
	public void addReference()
	{
		numrefs.incrementAndGet();
	}
	
	public void removeReference()
	{
		numrefs.decrementAndGet();
	}
	
	public boolean isUsable()
	{
		return isUsable.get();
	}
}
