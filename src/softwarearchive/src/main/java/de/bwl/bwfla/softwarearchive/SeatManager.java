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

package de.bwl.bwfla.softwarearchive;

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
	private static final Logger LOG = Logger.getLogger("SOFTWARE-SEAT-MANAGER");
	private DocumentCollection<SeatRecord> entries;


	public int getNumSeats(String tenant, String resource) throws BWFLAException
	{
		final String[] filter = SeatRecord.filter(tenant, resource);
		try {
			return entries.lookup(filter)
				.getNumSeats();
		}
		catch (NoSuchElementException error) {
			return -1;
		}
	}

	public void setNumSeats(String tenant, String resource, int seats) throws BWFLAException
	{
		LOG.info("Storing " + seats + " software seat(s) for '" + resource + " (" + tenant + ")'...");

		final String[] filter = SeatRecord.filter(tenant, resource);
		entries.save(new SeatRecord(tenant, resource, seats), filter);
	}

	public void resetNumSeats(String tenant, String resource)
	{
		LOG.info("Resetting software seats for '" + resource + " (" + tenant + ")'...");

		entries.delete(SeatRecord.filter(tenant, resource));
	}

	public void resetNumSeats(String tenant)
	{
		LOG.info("Resetting software seats for tenant '" + tenant + "'...");

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

		final String cname = "swseats";

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
		private String resource;
		private int seats;

		protected SeatRecord()
		{
			// Empty!
		}

		public SeatRecord(String tenant, String resource, int seats)
		{
			this.tenant = tenant;
			this.resource = resource;
			this.seats = seats;
		}

		@JsonProperty(Fields.TENANT)
		public String getTenant()
		{
			return tenant;
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

		public static String[] filter(String tenant, String resource)
		{
			return new String[] {
					SeatRecord.Fields.TENANT, tenant,
					SeatRecord.Fields.RESOURCE, resource
				};
		}

		public static String[] filter(String tenant)
		{
			return new String[] {
					SeatRecord.Fields.TENANT, tenant
				};
		}

		public static String[] getIndexFields()
		{
			return new String[] {
					Fields.TENANT,
					Fields.RESOURCE
				};
		}

		private static final class Fields
		{
			public static final String TENANT    = "tenant";
			public static final String RESOURCE  = "resource";
			public static final String SEATS     = "seats";
		}
	}
}
