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
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class SoftwareLicenseController extends AbstractLicenseController
{
	private final SoftwareArchiveHelper softwareArchiveHelper;

	public SoftwareLicenseController(int priority) throws BWFLAException
    {
        super(priority);

		final Configuration config = ConfigurationProvider.getConfiguration();
		final String softwareArchiveUrl = config.get("ws.softwarearchive");
		if (softwareArchiveUrl == null)
            throw new BWFLAException("Initializing software-license-controller failed!");

		this.softwareArchiveHelper = new SoftwareArchiveHelper(softwareArchiveUrl);
	}

	@Override
	public void gain(UUID session, ComponentConfiguration config) throws BWFLAException
    {
		if (!(config instanceof MachineConfiguration))
			return;

		final List<String> allocatedSoftwareIds = allocations.computeIfAbsent(session, ctx -> new ArrayList<String>());
		final List<String> installedSoftwareIds = ((MachineConfiguration)config).getInstalledSoftwareIds();
		// Allocate software seats...
		try {
			for (String softwareId : installedSoftwareIds) {
				int max = softwareArchiveHelper.getNumSoftwareSeatsById(softwareId);
				if(max < 0)
					max = Integer.MAX_VALUE;
				allocate(softwareId, max);
				allocatedSoftwareIds.add(softwareId);
			}
		} catch (Throwable t) {
			// TODO: implement some retry logic!

			// Not all seats could be allocated, clean up
			for (String softwareId : allocatedSoftwareIds)
				releaseSeat(softwareId);
			allocatedSoftwareIds.clear();

			throw t;
		}
	}

	@Override
	public void drop(UUID session)
    {
		final List<String> allocatedSoftwareIds = allocations.get(session);
		if (allocatedSoftwareIds == null)
			return;  // No allocations made for this UUID

		for (String softwareId : allocatedSoftwareIds)
			releaseSeat(softwareId);
		allocatedSoftwareIds.clear();
	}
}
