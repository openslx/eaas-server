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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A generic helper class for maintaining a mapping between
 * some IDs and the corresponding registered object instances.
 */
public class ObjectRegistry<K,V>
{
	/** Logger instance. */
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	/** A mapping from IDs to the objects. */
	protected final ConcurrentMap<K,V> entries;
	
	
	/** Constructor. */
	public ObjectRegistry()
	{
		entries = new ConcurrentHashMap<K,V>();
	}
	
	/**
	 * Register an object instance for the specified ID.
	 * @param id The object's ID.
	 * @param object The object instance to register.
	 * @return Previously registered object or null.
	 */
	public V set(K id, V object)
	{
		if (object != null)
			log.info("Added entry:  {} <-> {}", id, object.getClass().getName());
		
		return entries.put(id, object);
	}
	
	/**
	 * Retrieve the registered object for the specified ID.
	 * @param id The object's ID to look up.
	 * @return The registered object or null.
	 */
	public V get(K id)
	{
		return entries.get(id);
	}
	
	/**
	 * Remove the entry for the specified ID from the registry.
	 * @param id The ID, for which the entry should be removed.
	 * @return The registered object or null.
	 */
	public V remove(K id)
	{
		final V object = entries.remove(id);
		if (object != null)
			log.info("Removed entry:  {} <-> {}", id, object.getClass().getName());
		
		return object;
	}

	@Override
	public String toString()
	{
		return this.entries.toString();
	}
}
