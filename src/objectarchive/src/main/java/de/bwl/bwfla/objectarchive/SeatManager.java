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

package de.bwl.bwfla.objectarchive;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.database.document.DocumentDatabaseConnector;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.objectarchive.api.SeatDescription;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.logging.Logger;


@ApplicationScoped
public class SeatManager
{
	private static final Logger LOG = Logger.getLogger("OBJECT-SEAT-MANAGER");
	private DocumentCollection<SeatRecord> entries;
	private boolean verbose;


	public int getNumSeats(String tenant, String archive, String resource) throws BWFLAException
	{
		final var filter = SeatRecord.filter(tenant, archive, resource);
		final SeatRecord record = entries.lookup(filter);
		if (record == null)
			return -1;

		return record.getNumSeats();
	}

	public void setNumSeats(String tenant, String archive, String resource, int seats) throws BWFLAException
	{
		if (verbose)
			LOG.info("Storing " + seats + " object seat(s) for '" + archive + "/" + resource + " (" + tenant + ")'...");

		final var filter = SeatRecord.filter(tenant, archive, resource);
		entries.replace(filter, new SeatRecord(tenant, archive, resource, seats));
	}

	public void setNumSeats(String tenant, String archive, List<SeatDescription> resources) throws BWFLAException
	{
		final int BATCH_SIZE = 256;

		final var batch = entries.batch(BATCH_SIZE);
		for (SeatDescription description : resources) {
			final var resource = description.getResource();
			final var seats = description.getNumSeats();
			if (verbose)
				LOG.info("Storing " + seats + " object seat(s) for '" + archive + "/" + resource + " (" + tenant + ")'...");

			final var filter = SeatRecord.filter(tenant, archive, resource);
			batch.replace(filter, new SeatRecord(tenant, archive, resource, seats));
			if (batch.size() == BATCH_SIZE)
				batch.execute(false);
		}

		if (batch.size() > 0)
			batch.execute(false);
	}

	public void resetNumSeats(String tenant, String archive, String resource) throws BWFLAException
	{
		if (verbose)
			LOG.info("Resetting object seats for '" + archive + "/" + resource + " (" + tenant + ")'...");

		entries.delete(SeatRecord.filter(tenant, archive, resource));
	}

	public void resetNumSeats(String tenant, String archive, List<String> resources) throws BWFLAException
	{
		final int BATCH_SIZE = 512;

		final var batch = entries.batch(BATCH_SIZE);
		for (String resource : resources) {
			if (verbose)
				LOG.info("Resetting object seats for '" + archive + "/" + resource + " (" + tenant + ")'...");

			batch.delete(SeatRecord.filter(tenant, archive, resource));
			if (batch.size() == BATCH_SIZE)
				batch.execute(false);
		}

		if (batch.size() > 0)
			batch.execute(false);
	}

	public void resetNumSeats(String tenant) throws BWFLAException
	{
		if (verbose)
			LOG.info("Resetting object seats for tenant '" + tenant + "'...");

		entries.delete(SeatRecord.filter(tenant));
	}


	// ===== Internal Methods ===============

	@PostConstruct
	private void initialize()
	{
		this.entries = SeatManager.getSeatCollection();

		this.verbose = ConfigurationProvider.getConfiguration()
				.getOrDefault("objectarchive.verbose_logging", Boolean.class, false);
	}

	private static DocumentCollection<SeatRecord> getSeatCollection()
	{
		final String dbname = ConfigurationProvider.getConfiguration()
				.get("commonconf.mongodb.dbname");

		final String cname = "objseats";

		LOG.info("Initializing collection: " + cname + " (" + dbname + ")");
		try {
			final DocumentCollection<SeatRecord> entries = DocumentDatabaseConnector.instance()
					.database(dbname)
					.collection(cname, SeatRecord.class);

			entries.index(SeatRecord.getIndexFields());
			return entries;
		}
		catch (Exception error) {
			throw new RuntimeException(error);
		}
	}

	private static class SeatRecord
	{
		private String tenant;
		private String archive;
		private String resource;
		private int seats;

		protected SeatRecord()
		{
			// Empty!
		}

		public SeatRecord(String tenant, String archive, String resource, int seats)
		{
			this.tenant = tenant;
			this.archive = archive;
			this.resource = resource;
			this.seats = seats;
		}

		@JsonProperty(Fields.TENANT)
		public String getTenant()
		{
			return tenant;
		}

		@JsonProperty(Fields.ARCHIVE)
		public String getArchive()
		{
			return archive;
		}

		@JsonProperty(Fields.RESOURCE)
		public String getResource()
		{
			return resource;
		}

		@JsonProperty(Fields.SEATS)
		public int getNumSeats()
		{
			return seats;
		}

		public static DocumentCollection.Filter filter(String tenant, String archive, String resource)
		{
			return DocumentCollection.filter()
					.eq(Fields.TENANT, tenant)
					.eq(Fields.ARCHIVE, archive)
					.eq(Fields.RESOURCE, resource);
		}

		public static DocumentCollection.Filter filter(String tenant)
		{
			return DocumentCollection.filter()
					.eq(Fields.TENANT, tenant);
		}

		public static String[] getIndexFields()
		{
			return new String[] {
					Fields.TENANT,
					Fields.ARCHIVE,
					Fields.RESOURCE
				};
		}

		private static final class Fields
		{
			public static final String TENANT    = "tenant";
			public static final String ARCHIVE   = "archive";
			public static final String RESOURCE  = "resource";
			public static final String SEATS     = "seats";
		}
	}
}
