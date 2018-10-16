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

package de.bwl.bwfla.eaas.cluster.dump;


public class DumpTrigger implements Runnable
{
	private final DumpConfig dconf;
	private Runnable subResourceDumpHandler;
	private Runnable resourceDumpHandler;
	
	public DumpTrigger(DumpConfig dconf)
	{
		this.dconf = dconf;
		this.subResourceDumpHandler = null;
		this.resourceDumpHandler = null;
	}
	
	public void setResourceDumpHandler(Runnable handler)
	{
		this.resourceDumpHandler = handler;
	}
	
	public void setSubResourceDumpHandler(Runnable handler)
	{
		this.subResourceDumpHandler = handler;
	}

	public void run()
	{
		dconf.begin();
		try {
			if (dconf.hasMoreUrlSegments()) {
				if (subResourceDumpHandler != null)
					subResourceDumpHandler.run();
				else DumpHelpers.notfound(dconf.nextUrlSegment());
			}
			else {
				resourceDumpHandler.run();
			}
		}
		finally {
			dconf.end();
		}
	}
}
