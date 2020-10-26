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
import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.eaas.EaasWS;
import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class AccessControlManager
{
	private final Logger log;
	private final List<AbstractAccessController> controllers;

	public AccessControlManager()
	{
		this.log = Logger.getLogger("ACCESS-CONTROLLERS");
		this.controllers = new ArrayList<>(4);
	}

	public void gain(UUID session, EaasWS.SessionOptions options, ComponentConfiguration config) throws BWFLAException
	{
		log.info("Allocating access-control leases for session '" + session + "'...");
		for (AbstractAccessController controller: controllers) {
			if ((controller instanceof EnvironmentLocker) && !options.isLockEnvironment())
				continue;  // Skip environment-locking, if not requested!

			controller.gain(session, config);
		}
	}

	public void drop(UUID session)
	{
		log.info("Dropping access-control leases for session '" + session + "'...");
		for (AbstractAccessController controller: controllers) {
			try {
				controller.drop(session);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Dropping access-control lease failed!", error);
			}
		}
	}


	// ===== Internal Helpers ====================

	@PostConstruct
	private void initialize()
	{
		log.info("Initializing access-controllers...");

		final Configuration config = ConfigurationProvider.getConfiguration();
		for (int index = 0; true; ++index) {
			// Parse next entry...
			final String prefix = ConfigHelpers.toListKey("acl", index, ".");
			final Configuration subconfig = ConfigHelpers.filter(config, prefix);
			if (ConfigHelpers.isEmpty(subconfig))
				break;  // No more entries found!

			final String type = subconfig.get("type");
			final int order = Integer.parseInt(subconfig.get("order"));
			this.add(type, order, log);
		}

		this.add(Controllers.ENVIRONMENT_LOCK, 0, log);

		// sort controllers according to their priorities...
		controllers.sort(Comparator.comparingInt(AbstractAccessController::priority));

		log.info(controllers.size() + " access-controller(s) initialized");
	}


	private void add(String type, int priority, Logger log)
	{
		try {
			controllers.add(AccessControlManager.create(type, priority, log));
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Initializing ACL-controller failed!", error);
		}
	}

	private static AbstractAccessController create(String type, int priority, Logger log) throws BWFLAException
	{
		log.info("Initializing '" + type + "' access-controller...");
		switch (type) {
			case Controllers.OBJECT_LICENSE:
			case Controllers.LEGACY_OBJECT_LICENSE:
				return new ObjectLicenseController(priority, log);

			case Controllers.SOFTWARE_LICENSE:
			case Controllers.LEGACY_SOFTWARE_LICENSE:
				return new SoftwareLicenseController(priority, log);

			case Controllers.ENVIRONMENT_LOCK:
				return new EnvironmentLocker(priority, log);

			default:
				throw new BWFLAException("Invalid access-controller type: " + type);
		}
	}

	private static final class Controllers
	{
		public static final String OBJECT_LICENSE = "object-license";
		public static final String SOFTWARE_LICENSE = "software-license";
		public static final String ENVIRONMENT_LOCK = "environment-lock";

		public static final String LEGACY_OBJECT_LICENSE = "ObjectLicenseManager";
		public static final String LEGACY_SOFTWARE_LICENSE = "SoftwareLicenseManager";
	}
}
