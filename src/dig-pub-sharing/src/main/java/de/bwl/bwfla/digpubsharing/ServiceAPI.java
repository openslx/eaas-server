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

package de.bwl.bwfla.digpubsharing;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.database.document.DocumentDatabaseConnector;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.SecuredInternal;
import de.bwl.bwfla.digpubsharing.api.DigitalPublication;
import de.bwl.bwfla.digpubsharing.api.Settings;
import de.bwl.bwfla.digpubsharing.impl.PersistentSettings;
import org.apache.tamaya.ConfigurationProvider;


/** Service for sharing digital-publications */
@ResourceLabel("dig-pub-sharing")
@Path("api/v1")
@ApplicationScoped
public class ServiceAPI
{
	private static final Logger LOG = Logger.getLogger("DIG-PUB-SHARING");

	private DocumentCollection<DigitalPublication> inventory;
	private DocumentCollection<PersistentSettings> settings;


	// ===== Inventory API ====================

	/** Add (or update) digital-publications to inventory (internal API) */
	@PUT
	@SecuredInternal
	@Path("/inventory")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addDigitalPublications(@TypeHint(DigitalPublication[].class) InputStream istream)
	{
		final ObjectReader reader = new ObjectMapper()
				.readerFor(DigitalPublication.class);

		int counter = 0;

		LOG.info("Adding publication(s) to inventory...");
		try (istream; MappingIterator<DigitalPublication> iter = reader.readValues(istream)) {
			while (iter.hasNext()) {
				final DigitalPublication entry = iter.next();
				final var filter = DigitalPublication.filter(entry.getExternalId());
				inventory.replace(filter, entry);
				++counter;
			}

			LOG.info(counter + " publication(s) added to inventory");
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Adding publication(s) failed!", error);
			throw new InternalServerErrorException(error);
		}
	}

	/**
	 * Remove digital-publications with specified external IDs from inventory (internal API)
	 *
	 * @requestExample application/json ["id-1", "id-2", "id-5"]
	 */
	@DELETE
	@SecuredInternal
	@Path("/inventory")
	@Consumes(MediaType.APPLICATION_JSON)
	public void removeDigitalPublications(@TypeHint(String[].class) List<String> ids)
	{
		try {
			for (String id : ids) {
				inventory.delete(DigitalPublication.filter(id));
				LOG.info("Publication with ID '" + id + "' removed from inventory");
			}
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Removing publication(s) failed!", error);
			throw new InternalServerErrorException(error);
		}
	}

	/** List digital-publication inventory (internal API) */
	@GET
	@SecuredInternal
	@Path("/inventory")
	@Produces(MediaType.APPLICATION_JSON)
	@TypeHint(DigitalPublication[].class)
	public Response listDigitalPublications()
	{
		LOG.info("Listing publication inventory...");

		final StreamingOutput streamer = (OutputStream ostream) -> {
			final SequenceWriter writer = new ObjectMapper()
					.writerFor(DigitalPublication.class)
					.withDefaultPrettyPrinter()
					.writeValuesAsArray(ostream);

			int counter = 0;

			try (writer; var entries = inventory.list()) {
				for (DigitalPublication entry : entries) {
					writer.write(entry);
					++counter;
				}
			}
			catch (Exception error) {
				LOG.log(Level.WARNING, "Listing publications failed!", error);
				throw new InternalServerErrorException(error);
			}

			LOG.info(counter + " publication(s) read");
		};

		return Response.ok()
				.entity(streamer)
				.build();
	}


	// ===== Settings API ====================

	/** Update tenant's site-settings */
	@PUT
	@SecuredInternal
	@Path("/settings")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateSiteSettings(Settings config)
	{
		final String tenant = this.getTenantId();

		try {
			final var filter = PersistentSettings.filter(tenant);
			final var data = new PersistentSettings()
					.setTenantId(tenant)
					.setSettings(config);

			settings.replace(filter, data);

			LOG.info("Settings for tenant '" + tenant + "' updated");
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Updating site-settings failed!", error);
			throw new InternalServerErrorException(error);
		}
	}

	/** Look up tenant's site-settings */
	@GET
	@SecuredInternal
	@Path("/settings")
	@Produces(MediaType.APPLICATION_JSON)
	@TypeHint(Settings.class)
	public Response getSiteSettings()
	{
		final String tenant = this.getTenantId();

		try {
			final var filter = PersistentSettings.filter(tenant);
			final PersistentSettings data = settings.lookup(filter);
			if (data == null)
				throw new NotFoundException();

			LOG.info("Settings for tenant '" + tenant + "' read");

			return Response.ok()
					.entity(data.getSettings())
					.build();
		}
		catch (BWFLAException error) {
			LOG.log(Level.WARNING, "Looking up site-settings failed!", error);
			throw new InternalServerErrorException(error);
		}
	}


	// ===== Internal Helpers ====================

	@PostConstruct
	private void initialize()
	{
		try {
			this.inventory = ServiceAPI.getDbCollection("inventory", DigitalPublication.class);
			DigitalPublication.index(inventory);

			this.settings = ServiceAPI.getDbCollection("settings", PersistentSettings.class);
			PersistentSettings.index(settings);
		}
		catch (Exception error) {
			throw new RuntimeException("Initializing dig-pub-sharing service failed!", error);
		}
	}

	private String getTenantId()
	{
		// TODO: get tenant from user-context!
		return "dummy";
	}

	private static <T> DocumentCollection<T> getDbCollection(String cname, Class<T> clazz)
	{
		final String dbname = ConfigurationProvider.getConfiguration()
				.get("digpub_sharing.dbname");

		LOG.info("Initializing collection: " + cname + " (" + dbname + ")");
		return DocumentDatabaseConnector.instance()
				.database(dbname)
				.collection(cname, clazz);
	}
}
