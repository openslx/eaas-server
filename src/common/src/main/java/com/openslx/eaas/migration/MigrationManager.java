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

package com.openslx.eaas.migration;

import com.openslx.eaas.common.databind.DataUtils;
import com.openslx.eaas.migration.config.MigrationConfig;
import com.openslx.eaas.migration.config.MigrationManagerConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.CDI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.regex.Pattern;


@ApplicationScoped
public class MigrationManager
{
	private static final Logger LOG = Logger.getLogger("MIGRATIONS");

	/** Expected valid migration names */
	private static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

	private MigrationManagerConfig config;


	/** Immediately execute given migration, if enabled */
	public void execute(String name, IMigration migration) throws Exception
	{
		this.execute(name, migration, false);
	}

	/** Immediately (force-) execute given migration, if enabled */
	public void execute(String name, IMigration migration, boolean force) throws Exception
	{
		MigrationManager.validate(name);

		final var mc = this.find(name);
		if (mc == null) {
			LOG.info("Skipping disabled migration '" + name + "'!");
			return;
		}

		final var outpath = config.getStateDirectory()
				.resolve(name + ".json");

		force = force || mc.getForceFlag();
		if (!force && Files.exists(outpath)) {
			LOG.info("Skipping executed migration '" + name + "'!");
			return;
		}

		final var suffix = (force) ? " (forced)" : "";
		LOG.info("Running migration '" + name + "'" + suffix + "...");
		final var result = new MigrationResult(name);
		try {
			migration.execute(mc);
		}
		catch (Exception error) {
			LOG.warning("Running migration '" + name + "' failed!");
			throw error;
		}

		result.finish(MigrationState.EXECUTED);
		this.store(outpath, result);

		LOG.warning("Finished migration '" + name + "'");
	}

	/** Return global instance */
	public static MigrationManager instance()
	{
		return CDI.current()
				.select(MigrationManager.class)
				.get();
	}


	// ===== Internal Helpers ==============================

	protected MigrationManager()
	{
		// Empty!
	}

	private void initialize(@Observes @Initialized(ApplicationScoped.class) Object unused)
	{
		try {
			this.config = MigrationManagerConfig.create(LOG);
			Files.createDirectories(config.getStateDirectory());
		}
		catch (Exception error) {
			throw new IllegalStateException("Initializing migration-manager failed!", error);
		}
	}

	private MigrationConfig find(String name)
	{
		for (var mc : config.getMigrationConfigs()) {
			if (name.equals(mc.getName()))
				return mc;
		}

		return null;
	}

	private void store(Path outpath, MigrationResult result) throws IOException
	{
		try (var output = Files.newBufferedWriter(outpath)) {
			DataUtils.json()
					.writer(true)
					.writeValue(output, result);
		}
	}

	private static void validate(String name) throws IllegalArgumentException
	{
		final var matcher = NAME_PATTERN.matcher(name);
		if (!matcher.matches())
			throw new IllegalArgumentException("Invalid name: " + name);
	}
}
