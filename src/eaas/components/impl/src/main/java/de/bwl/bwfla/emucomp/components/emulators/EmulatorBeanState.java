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

import de.bwl.bwfla.common.datatypes.EmuCompState;


/** This class represents internal state of an EmulatorBean. */
public final class EmulatorBeanState
{
	private EmuCompState state;
	
	/** Constructor */
	public EmulatorBeanState(EmuCompState state)
	{
		this.state = state;
	}
	
	
	/* ===== Unsynchronized API ===== */

	public EmuCompState get()
	{
		return this.state;
	}
	
	public void set(EmuCompState newstate)
	{
		this.state = newstate;
	}
	
	
	/* ===== Synchronized API ===== */
	
	public synchronized EmuCompState fetch()
	{
		return this.get();
	}
	
	public synchronized void update(EmuCompState newstate)
	{
		this.set(newstate);
	}
}
