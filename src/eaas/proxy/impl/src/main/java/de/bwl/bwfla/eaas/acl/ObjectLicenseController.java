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
import de.bwl.bwfla.emucomp.api.ObjectArchiveBinding;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ObjectLicenseController extends AbstractLicenseController
{
	private final ObjectArchiveHelper objectArchiveHelper;

	public ObjectLicenseController(int priority) throws BWFLAException
	{
		super(priority);

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

		final List<String> allocatedObjectIds = allocations.computeIfAbsent(session, ctx -> new ArrayList<String>());
		final List<ObjectArchiveBinding> objects = EmulationEnvironmentHelper.getObjects((MachineConfiguration)config);
		try {
			for (ObjectArchiveBinding o: objects) {
				int max = objectArchiveHelper.getNumObjectSeats(o.getArchive(), o.getObjectId());
				if(max < 0)
					max = Integer.MAX_VALUE;
				allocate(o.getObjectId() + o.getArchive(), max);
				allocatedObjectIds.add(o.getObjectId() + o.getArchive());
			}
		} catch (Throwable t) {
			// TODO: implement some retry logic!

			// Not all seats could be allocated, clean up
			for (String softwareId : allocatedObjectIds)
				releaseSeat(softwareId);
			allocatedObjectIds.clear();

			throw t;
		}
	}

	@Override
	public void drop(UUID session)
	{
		final List<String> allocatedObjects = allocations.get(session);
		if (allocatedObjects == null)
			return;  // No allocations made for this UUID

		for (String obj : allocatedObjects)
			releaseSeat(obj);
		allocatedObjects.clear();
	}
}
