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

package de.bwl.bwfla.emil;

import com.openslx.eaas.common.databind.Streamable;
import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.ImageArchiveMappers;
import com.openslx.eaas.imagearchive.api.v2.databind.MetaDataKindV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.util.EmulatorMetaHelperV2;
import com.openslx.eaas.imagearchive.databind.EmulatorMetaData;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;


@ApplicationScoped
@Path("/emulator-repository")
public class EmulatorRepository extends EmilRest
{
	@Inject
	private EmilEnvironmentRepository emilEnvRepo = null;

	private ImageArchiveClient imagearchive = null;
	private EmulatorMetaHelperV2 emuMetaHelper = null;


	// ===== Public API ==============================

	@Path("/emulators")
	public Emulators emulators()
	{
		return new Emulators();
	}


	// ===== Subresources ==============================

	public class Emulators
	{
		/** List all available emulators */
		@GET
		@Secured(roles={Role.PUBLIC})
		@Produces(MediaType.APPLICATION_JSON)
		@TypeHint(EmulatorMetaData[].class)
		public Streamable<EmulatorMetaData> list() throws BWFLAException
		{
			return imagearchive.api()
					.v2()
					.metadata(MetaDataKindV2.EMULATORS)
					.fetch(ImageArchiveMappers.JSON_TREE_TO_EMULATOR_METADATA);
		}

		/** Mark emulator version as default */
		@POST
		@Path("/{emuid}/default")
		@Secured(roles={Role.ADMIN})
		public void makrAsDefault(@PathParam("emuid") String emuid) throws BWFLAException
		{
			emuMetaHelper.markAsDefault(emuid);
		}
	}


	// ===== Internal Helpers ==============================

	@PostConstruct
	private void initialize()
	{
		try {
			imagearchive = emilEnvRepo.getImageArchive();
			emuMetaHelper = new EmulatorMetaHelperV2(imagearchive, LOG);
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Initializing emulator-repository failed!", error);
		}
	}
}
