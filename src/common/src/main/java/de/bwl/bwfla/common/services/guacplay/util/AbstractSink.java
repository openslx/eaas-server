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

package de.bwl.bwfla.common.services.guacplay.util;

import java.util.ArrayList;


/** Base class, that represents a collection of data consumers. */
public abstract class AbstractSink<T>
{
	protected final ArrayList<T> consumers;

	
	/** Constructor */
	protected AbstractSink(int capacity)
	{
		this.consumers = new ArrayList<T>(capacity);
	}
	
	/** Add a new consumer. */
	public void addConsumer(T consumer)
	{
		consumers.add(consumer);
	}
	
	/** Remove the first occurence of the specified consumer. */
	public boolean removeConsumer(T consumer)
	{
		return consumers.remove(consumer);
	}
	
	// The derived classes should implement the
	// following method to consume the data:
	//
	//     public void consume(<args>);
}
