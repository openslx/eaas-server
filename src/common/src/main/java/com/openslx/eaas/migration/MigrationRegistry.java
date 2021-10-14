/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.migration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


public class MigrationRegistry
{
	private final Map<String, IMigration> entries;
	private final Logger log;


	public MigrationRegistry(Logger log)
	{
		this.entries = new LinkedHashMap<>();
		this.log = log;
	}

	/** Register given migration for later execution */
	public void register(String name, IMigration migration) throws Exception
	{
		MigrationManager.validate(name);
		entries.put(name, migration);

		log.info("Registered migration '" + name + "'");
	}

	/** Remove named migration */
	public void remove(String name)
	{
		if (entries.remove(name) != null)
			log.info("Removed migration '" + name + "'");
	}

	/** Look up named migration */
	public IMigration lookup(String name)
	{
		return entries.get(name);
	}

	/** Returns true if registry is empty, else false */
	public boolean isEmpty()
	{
		return entries.isEmpty();
	}

	/** Return number of registered migrations */
	public int size()
	{
		return entries.size();
	}

	Map<String, IMigration> entries()
	{
		return entries;
	}

	Set<String> names()
	{
		return entries.keySet();
	}
}
