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

import de.bwl.bwfla.common.utils.ConfigHelpers;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class MigrationManagerConfig extends BaseConfig<MigrationManagerConfig>
{
	private List<MigrationConfig> migrations = new ArrayList<>();
	private Path statedir;


	// ===== Getters and Setters ====================

	public void setMigrationConfigs(List<MigrationConfig> migrations)
	{
		ConfigHelpers.check(migrations, "List of migrations is invalid!");
		this.migrations = migrations;
	}

	public List<MigrationConfig> getMigrationConfigs()
	{
		return migrations;
	}

	public void setStateDirectory(Path path)
	{
		ConfigHelpers.check(path, "State directory is invalid!");
		this.statedir = path;
	}

	public Path getStateDirectory()
	{
		return statedir;
	}


	// ===== Initialization ====================

	public static MigrationManagerConfig create(Logger log)
	{
		return new MigrationManagerConfig()
				.load(log);
	}


	// ===== Internal Helpers ====================

	@Override
	protected MigrationManagerConfig load(Configuration config, Logger log) throws ConfigException
	{
		log.info("Loading migration-manager's configuration...");

		super.load(config, log);

		// Configure state directory
		{
			final var datadir = config.get("commonconf.serverdatadir");
			this.setStateDirectory(Path.of(datadir, "migrations"));
		}

		// Configure migrations
		{
			migrations.clear();

			while (true) {
				final var prefix = ConfigHelpers.toListKey("migrations", migrations.size(), ".");
				final var properties = ConfigHelpers.filter(config, prefix);
				if (properties.get("name") == null)
					break;  // No more migrations found!

				final var migration = new MigrationConfig()
						.load(properties, log);

				migrations.add(migration);
			}

			log.info("Loaded " + migrations.size() + " migration(s)");
		}

		return this;
	}
}
