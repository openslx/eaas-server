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

package de.bwl.bwfla.eaas.acl;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.ComponentConfiguration;

import java.util.UUID;
import java.util.logging.Logger;


public abstract class AbstractAccessController
{
	protected final Logger log;
	private final int priority;


	protected AbstractAccessController(int priority, Logger log)
	{
		this.priority = priority;
		this.log = log;
	}

	public int priority()
	{
		return priority;
	}

	/** Try to gain access to resources associated with session. */
	public abstract void gain(UUID session, ComponentConfiguration config) throws BWFLAException;

	/** Drop access to resources associated with session. */
	public abstract void drop(UUID session);
}
