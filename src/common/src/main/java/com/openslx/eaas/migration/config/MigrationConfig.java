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

package com.openslx.eaas.migration.config;

import com.openslx.eaas.migration.MigrationManager;
import de.bwl.bwfla.common.utils.ConfigHelpers;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import java.util.logging.Logger;


public class MigrationConfig extends BaseConfig<MigrationConfig>
{
	private String name = null;
	private boolean force = false;
	private Configuration args = null;


	// ===== Getters and Setters ====================

	@Config("name")
	public void setName(String name)
	{
		ConfigHelpers.check(name, "Name is invalid!");
		MigrationManager.validate(name);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Config(value = "force", defaultValue = "false")
	public void setForceFlag(boolean force)
	{
		this.force = force;
	}

	public boolean getForceFlag()
	{
		return force;
	}

	public void setArguments(Configuration args)
	{
		ConfigHelpers.check(args, "Arguments are invalid!");
		this.args = args;
	}

	public Configuration getArguments()
	{
		return args;
	}


	// ===== Internal Helpers ====================

	@Override
	protected MigrationConfig load(Configuration config, Logger log) throws ConfigException
	{
		this.setArguments(ConfigHelpers.filter(config,"args."));
		return super.load(config, log);
	}
}
