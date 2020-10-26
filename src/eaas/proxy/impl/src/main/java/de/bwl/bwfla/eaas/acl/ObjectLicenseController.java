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
import de.bwl.bwfla.emucomp.api.EmulationEnvironmentHelper;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class ObjectLicenseController extends AbstractLicenseController
{
	private final ObjectArchiveHelper objectArchiveHelper;

	public ObjectLicenseController(int priority, Logger log) throws BWFLAException
	{
		super(priority, log);

		final Configuration config = ConfigurationProvider.getConfiguration();
		final String objectArchiveUrl = config.get("ws.objectarchive");
		if (objectArchiveUrl == null)
		    throw new BWFLAException("Initializing object-license-controller failed!");

		this.objectArchiveHelper = new ObjectArchiveHelper(objectArchiveUrl);
	}

	@Override
	public void gain(UUID session, ComponentConfiguration config) throws BWFLAException
	{
		if (!(config instanceof MachineConfiguration))
			return;

		final Stream<ResourceHandle> resources = EmulationEnvironmentHelper.getObjects((MachineConfiguration)config)
				.stream()
				.map((object) -> {
					final String id = object.getArchive() + "/" + object.getObjectId();
					final int seats = this.getMaxNumSeats(object.getArchive(), object.getObjectId());
					return new ResourceHandle(id, seats);
				});

		super.allocate(session, resources);
	}

	@Override
	public void drop(UUID session)
	{
		super.release(session);
	}

	private int getMaxNumSeats(String archive, String id) throws RuntimeException
	{
		try {
			final int numseats = objectArchiveHelper.getNumObjectSeats(archive, id);
			return (numseats < 0) ? Integer.MAX_VALUE : numseats;
		}
		catch (Exception error) {
			throw new RuntimeException(error);
		}
	}
}
