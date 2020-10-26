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

import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class SoftwareLicenseController extends AbstractLicenseController
{
	private final SoftwareArchiveHelper softwareArchiveHelper;

	public SoftwareLicenseController(int priority, Logger log) throws BWFLAException
    {
        super(priority, log);

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

		final Stream<ResourceHandle> resources = ((MachineConfiguration) config).getInstalledSoftwareIds()
				.stream()
				.map((id) -> new ResourceHandle(id, this.getMaxNumSeats(id)));

		super.allocate(session, resources);
	}

	@Override
	public void drop(UUID session)
    {
		super.release(session);
	}

	private int getMaxNumSeats(String id) throws RuntimeException
	{
		try {
			final int numseats = softwareArchiveHelper.getNumSoftwareSeatsById(id);
			return (numseats < 0) ? Integer.MAX_VALUE : numseats;
		}
		catch (Exception error) {
			throw new RuntimeException(error);
		}
	}
}
