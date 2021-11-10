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

package de.bwl.bwfla.emil;

import com.openslx.eaas.common.event.EventTrigger;
import com.openslx.eaas.common.event.ServerStartupEvent;
import com.openslx.eaas.migration.MigrationManager;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
class ServerLifecycleHooks
{
	@Resource(lookup = "java:jboss/ee/concurrency/executor/io")
	private ExecutorService executor;


	// ===== Internal Helpers ====================

	protected ServerLifecycleHooks()
	{
		// Empty!
	}

	private void onStartup(@Observes @Initialized(ApplicationScoped.class) Object unused)
			throws Exception
	{
		EventTrigger.fire(new ServerStartupEvent());

		final var logger = Logger.getLogger("SERVER-LIFECYCLE-HOOKS");
		executor.execute(() -> {
			try {
				// execute migrations...
				MigrationManager.instance()
						.execute();

				logger.info("Application started!");
			}
			catch (Exception error) {
				logger.log(Level.SEVERE, "Starting application failed!", error);
			}
		});
	}
}
