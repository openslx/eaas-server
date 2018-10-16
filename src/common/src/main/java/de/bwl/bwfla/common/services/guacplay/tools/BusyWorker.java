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

package de.bwl.bwfla.common.services.guacplay.tools;


public final class BusyWorker implements Runnable
{
	private final int id;
	private volatile boolean exitflag;
	
	
	public BusyWorker(int id)
	{
		this.id = id;
		this.exitflag = false;
	}
	
	@Override
	public void run()
	{
		System.out.println("Start worker with ID-" + id);
		
		@SuppressWarnings("unused")
		int counter = 0;
		
		while (!exitflag) {
			for (int i = 0; i < 1000; ++i)
				counter += i;
		}
		
		System.out.println("Stop worker with ID-" + id);
	}

	public void exit()
	{
		exitflag = true;
	}
}
