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
import de.bwl.bwfla.common.database.DocumentCollection;
import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import java.util.NoSuchElementException;
import java.util.logging.Logger;


@ApplicationScoped
public class SeatManager
{
	private static final Logger LOG = Logger.getLogger("OBJECT-SEAT-MANAGER");
	private DocumentCollection<SeatRecord> entries;


	public int getNumSeats(String tenant, String archive, String resource) throws BWFLAException
	{
		final String[] filter = SeatRecord.filter(tenant, archive, resource);
		try {
			return entries.lookup(filter)
				.getNumSeats();
		}
		catch (NoSuchElementException error) {
			return -1;
		}
	}

	public void setNumSeats(String tenant, String archive, String resource, int seats) throws BWFLAException
	{
		LOG.info("Storing " + seats + " object seat(s) for '" + archive + "/" + resource + " (" + tenant + ")'...");

		final String[] filter = SeatRecord.filter(tenant, archive, resource);
		entries.save(new SeatRecord(tenant, archive, resource, seats), filter);
	}

	public void resetNumSeats(String tenant, String archive, String resource)
	{
		LOG.info("Resetting object seats for '" + archive + "/" + resource + " (" + tenant + ")'...");

		entries.delete(SeatRecord.filter(tenant, archive, resource));
	}

	public void resetNumSeats(String tenant)
	{
		LOG.info("Resetting object seats for tenant '" + tenant + "'...");

		entries.delete(SeatRecord.filter(tenant));
	}


	// ===== Internal Methods ===============

	@PostConstruct
	private void initialize()
	{
		this.entries = SeatManager.getSeatCollection();
	}

	private static DocumentCollection<SeatRecord> getSeatCollection()
	{
		final String dbname = ConfigurationProvider.getConfiguration()
				.get("commonconf.mongodb.dbname");

		final String cname = "objseats";

		LOG.info("Initializing collection: " + cname + " (" + dbname + ")");
		try {
			final MongodbEaasConnector dbcon = CDI.current()
					.select(MongodbEaasConnector.class)
					.get();

			final DocumentCollection<SeatRecord> entries = dbcon.getInstance(dbname)
					.getCollection(cname, SeatRecord.class);

			entries.index(SeatRecord.getIndexFields());
			return entries;
		}
		catch (Exception error) {
			throw new RuntimeException(error);
		}
	}

	private static class SeatRecord extends JaxbType
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

		public static String[] filter(String tenant, String archive, String resource)
		{
			return new String[] {
					Fields.TENANT, tenant,
					Fields.ARCHIVE, archive,
					Fields.RESOURCE, resource
				};
		}

		public static String[] filter(String tenant)
		{
			return new String[] {
					Fields.TENANT, tenant
				};
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
