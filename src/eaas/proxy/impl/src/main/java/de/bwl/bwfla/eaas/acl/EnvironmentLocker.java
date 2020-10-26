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

import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class EnvironmentLocker extends AbstractAccessController
{
	/** Mapping: environment ID -> session ID */
	private final ConcurrentHashMap<String, UUID> environments = new ConcurrentHashMap<>();

	/** Mapping: session ID -> environment ID */
	private final ConcurrentHashMap<UUID, String> sessions = new ConcurrentHashMap<>();

	public EnvironmentLocker(int priority, Logger log)
	{
		super(priority, log);
	}

	@Override
	public void gain(UUID session, ComponentConfiguration config) throws AccessDeniedException
	{
		final String envId = ((MachineConfiguration) config).getId();
		if (environments.putIfAbsent(envId, session) != null)
			throw new AccessDeniedException("Environment already locked!");

		sessions.put(session, envId);
		log.info("Environment '" + envId + "' locked by session " + session);
	}

	@Override
	public void drop(UUID session)
	{
		final String envId = sessions.remove(session);
		if (envId != null) {
			environments.remove(envId);
			log.info("Environment '" + envId + "' unlocked by session " + session);
		}
	}
}
