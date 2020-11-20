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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import de.bwl.bwfla.common.services.guacplay.util.StopWatch;
import de.bwl.bwfla.common.services.security.SecuredInternal;
import de.bwl.bwfla.digpubsharing.api.DigitalPublication;
import de.bwl.bwfla.digpubsharing.api.ImportSummary;
import de.bwl.bwfla.digpubsharing.api.DigPubRecord;
import de.bwl.bwfla.digpubsharing.api.DigPubStatus;
import de.bwl.bwfla.digpubsharing.api.Settings;
import de.bwl.bwfla.digpubsharing.impl.PersistentDigPubRecord;
import de.bwl.bwfla.digpubsharing.impl.PersistentSettings;
import de.bwl.bwfla.objectarchive.api.SeatDescription;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
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
	private DocumentCollection<PersistentDigPubRecord> records;

	private ObjectArchiveHelper objects;
	private String objectArchiveId;

	private static final String MEDIATYPE_CSV = "text/csv";


	// ===== Inventory API ====================

	/** Add (or update) digital-publications to inventory (internal API) */
	@PUT
	@SecuredInternal
	@Path("/inventory")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addDigitalPublications(@TypeHint(DigitalPublication[].class) InputStream istream)
	{
		final int BATCH_SIZE = 512;

		final ObjectReader reader = new ObjectMapper()
				.readerFor(DigitalPublication.class);

		int counter = 0;

		LOG.info("Adding publication(s) to inventory...");
		try (istream; MappingIterator<DigitalPublication> iter = reader.readValues(istream)) {
			final StopWatch stopwatch = new StopWatch();
			final var batch = inventory.batch(BATCH_SIZE);
			while (iter.hasNext()) {
				final DigitalPublication entry = iter.next();
				final var filter = DigitalPublication.filter(entry.getExternalId());
				batch.replace(filter, entry);
				if (batch.size() == BATCH_SIZE)
					batch.execute(false);

				++counter;
			}

			// execute last batch
			if (batch.size() > 0)
				batch.execute(false);

			LOG.info(counter + " publication(s) added to inventory (took " + stopwatch.timems() + " msecs)");
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
	public void removeDigitalPublications(@TypeHint(String[].class) InputStream istream)
	{
		final int BATCH_SIZE = 1024;

		final ObjectReader reader = new ObjectMapper()
				.readerFor(String.class);

		int counter = 0;

		LOG.info("Removing publication(s) from inventory...");
		try (istream; MappingIterator<String> ids = reader.readValues(istream)) {
			final StopWatch stopwatch = new StopWatch();
			final var batch = inventory.batch();
			while (ids.hasNext()) {
				batch.delete(DigitalPublication.filter(ids.next()));
				if (batch.size() == BATCH_SIZE)
					batch.execute(false);

				++counter;
			}

			// execute last batch
			if (batch.size() > 0)
				batch.execute(false);

			LOG.info(counter + " publication(s) removed (took " + stopwatch.timems() + " msecs)");
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
			final StopWatch stopwatch = new StopWatch();
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

			LOG.info(counter + " publication(s) read (took " + stopwatch.timems() + " msecs)");
		};

		return Response.ok()
				.entity(streamer)
				.build();
	}


	// ===== Records API ====================

	/** Download sample .csv in a form suitable for importing */
	@GET
	@SecuredInternal
	@Path("/import-sample")
	@Produces(MEDIATYPE_CSV)
	public Response getImportSample()
	{
		final String sample = "# example csv-file for importing\n"
				+ "# into dig-pub-sharing service\n\n"
				+ "# id,seats\n"
				+ "id-01,2\n"
				+ "id-02,1\n"
				+ "id-03,5\n";

		return Response.ok()
				.entity(sample)
				.build();
	}

	/** Import digital-publication records from .csv */
	@POST
	@SecuredInternal
	@Path("/import")
	@Consumes(MEDIATYPE_CSV)
	@Produces(MediaType.APPLICATION_JSON)
	@TypeHint(ImportSummary.class)
	public Response importPublicationRecords(InputStream istream)
	{
		final String tenant = this.getTenantId();
		final String date = this.date();

		LOG.info("Importing publication(s) for tenant '" + tenant + "'...");

		try (istream; BufferedReader reader = new BufferedReader(new InputStreamReader(istream))) {
			final StopWatch stopwatch = new StopWatch();
			final var importer = new DigPubRecordImporter(tenant, date);
			reader.lines().forEach(importer);
			importer.finish();

			LOG.info(importer.getNumRecordsImported() + " publication(s) imported (took " + stopwatch.timems() + " msecs)");

			final var summary = new ImportSummary()
					.setNumRecordsIndexed((int) inventory.count())
					.setNumRecordsImported(importer.getNumRecordsImported())
					.setNumRecordsMatched(importer.getNumRecordsMatched());

			return Response.ok()
					.entity(summary)
					.build();
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Importing publication(s) failed!", error);
			throw new InternalServerErrorException(error);
		}
	}

	/** Export digital-publication records as .csv */
	@POST
	@SecuredInternal
	@Path("/export")
	@Produces(MEDIATYPE_CSV)
	@TypeHint(String.class)
	public Response exportPublicationRecords()
	{
		final int BATCH_SIZE = 512;

		final String tenant = this.getTenantId();

		LOG.info("Exporting publications for tenant '" + tenant + "'...");

		final StreamingOutput streamer = (OutputStream ostream) -> {
			int counter = 0;
			final StopWatch stopwatch = new StopWatch();
			final var batch = records.batch(BATCH_SIZE);
			try (var writer = new OutputStreamWriter(ostream); var entries = records.find(PersistentDigPubRecord.filter(tenant))) {
				for (PersistentDigPubRecord record : entries) {
					final var extid = record.getExternalId();

					// write record as csv...
					writer.write(extid);
					writer.write("\n");
					++counter;

					// update record's status
					final var status = record.getStatus();
					if (status.isNew()) {
						status.setNewFlag(false);
						batch.replace(PersistentDigPubRecord.filter(tenant, extid), record);
						if (batch.size() == BATCH_SIZE)
							batch.execute(false);
					}
				}

				// execute last batch
				if (batch.size() > 0)
					batch.execute(false);
			}
			catch (Exception error) {
				LOG.log(Level.WARNING, "Exporting publications failed!", error);
				throw new InternalServerErrorException(error);
			}

			LOG.info(counter + " publication(s) exported (took " + stopwatch.timems() + " msecs)");
		};

		return Response.ok()
				.entity(streamer)
				.build();
	}

	/** Count all digital-publication records */
	@POST
	@SecuredInternal
	@Path("/count-records")
	@Produces(MediaType.APPLICATION_JSON)
	@TypeHint(Integer.class)
	public Response countPublicationRecords()
	{
		final String tenant = this.getTenantId();

		try {
			final var filter = PersistentDigPubRecord.filter(tenant);
			return Response.ok()
					.entity(records.count(filter))
					.build();
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Counting publications failed!", error);
			throw new InternalServerErrorException(error);
		}
	}

	/**
	 * List all digital-publication records
	 *
	 * @param skip Skip given number of records from listing (server-side pagination)
	 * @param limit Max. number of records to return (server-side pagination)
	 */
	@GET
	@SecuredInternal
	@Path("/records")
	@Produces(MediaType.APPLICATION_JSON)
	@TypeHint(DigPubRecord[].class)
	public Response listPublicationRecords(@QueryParam("skip") @DefaultValue("0") int skip,
										   @QueryParam("limit") @DefaultValue("-1") int limit)
	{
		final String tenant = this.getTenantId();

		LOG.info("Listing publications for tenant '" + tenant + "'...");

		final StreamingOutput streamer = (OutputStream ostream) -> {
			final StopWatch stopwatch = new StopWatch();
			final SequenceWriter writer = new ObjectMapper()
					.writerFor(DigPubRecord.class)
					.withDefaultPrettyPrinter()
					.writeValuesAsArray(ostream);

			int counter = 0;

			try (writer; var entries = records.find(PersistentDigPubRecord.filter(tenant))) {
				// apply server-side pagination
				entries.skip(skip);
				if (limit > 0)
					entries.limit(limit);

				// write records...
				for (PersistentDigPubRecord entry : entries) {
					// TODO: construct valid access-link!
					final String link = "https://localhost/dig-pub-sharing/viewer?extid=" + entry.getExternalId();

					final var record = new DigPubRecord()
							.setExternalId(entry.getExternalId())
							.setStatus(entry.getStatus())
							.setLink(link);

					writer.write(record);
					++counter;
				}
			}
			catch (Exception error) {
				LOG.log(Level.WARNING, "Listing publications failed!", error);
				throw new InternalServerErrorException(error);
			}

			LOG.info(counter + " publication(s) read (took " + stopwatch.timems() + " msecs)");
		};

		return Response.ok()
				.entity(streamer)
				.build();
	}

	/**
	 * Remove digital-publication records matching specified external IDs
	 *
	 * @requestExample application/json ["id-1", "id-2", "id-5"]
	 */
	@DELETE
	@SecuredInternal
	@Path("/records")
	@Consumes(MediaType.APPLICATION_JSON)
	public void removePublicationRecords(@TypeHint(String[].class) InputStream istream)
	{
		final String tenant = this.getTenantId();
		final ObjectReader reader = new ObjectMapper()
				.readerFor(String.class);

		LOG.info("Removing publications for tenant '" + tenant + "'...");
		try (istream; MappingIterator<String> ids = reader.readValues(istream)) {
			final StopWatch stopwatch = new StopWatch();
			final var deleter = new DigPubRecordDeleter(tenant);
			ids.forEachRemaining(deleter);
			deleter.finish();

			LOG.info(deleter.getNumRecordsDeleted() + " publication(s) removed (took " + stopwatch.timems() + " msecs)");
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Removing publication(s) failed!", error);
			throw new InternalServerErrorException(error);
		}
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

			this.records = ServiceAPI.getDbCollection("records", PersistentDigPubRecord.class);
			PersistentDigPubRecord.index(records);

			this.setupObjectArchiveClient();
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

	private String date()
	{
		return LocalDate.now()
				.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private void setupObjectArchiveClient()
	{
		final var config = ConfigurationProvider.getConfiguration();
		this.objects = new ObjectArchiveHelper(config.get("ws.objectarchive"));
		this.objectArchiveId = config.get("objectarchive.default_archive");
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


	private abstract class DigPubRecordProcessor<I,O> implements Consumer<I>
	{
		protected final DocumentCollection<PersistentDigPubRecord>.Batch rbatch;
		protected final List<O> obatch;
		protected final String tenant;

		protected static final int RECORDS_BATCH_SIZE = 512;
		protected static final int OBJECTS_BATCH_SIZE = 512;

		protected DigPubRecordProcessor(String tenant)
		{
			this.rbatch = records.batch(RECORDS_BATCH_SIZE);
			this.obatch = new ArrayList<>(OBJECTS_BATCH_SIZE);
			this.tenant = tenant;
		}

		public void finish() throws BWFLAException
		{
			this.rflush(1);
			this.oflush(1);
		}

		protected void rflush(int minsize) throws BWFLAException
		{
			if (rbatch.size() >= minsize)
				rbatch.execute(false);
		}

		protected abstract void oflush(int minsize) throws BWFLAException;
	}

	private class DigPubRecordImporter extends DigPubRecordProcessor<String, SeatDescription>
	{
		private final String date;
		private int numRecordsImported;
		private int numRecordsMatched;

		public DigPubRecordImporter(String tenant, String date)
		{
			super(tenant);

			this.date = date;
			this.numRecordsImported = 0;
			this.numRecordsMatched = 0;
		}

		public int getNumRecordsImported()
		{
			return numRecordsImported;
		}

		public int getNumRecordsMatched()
		{
			return numRecordsMatched;
		}

		@Override
		public void accept(String line)
		{
			// skip comments
			if (line.startsWith("#") || line.isEmpty())
				return;

			// parse current line...
			final String[] fields = line.split(",");
			if (fields.length != 2) {
				LOG.warning("Invalid input submitted for import!");
				throw new BadRequestException();
			}

			final String extid = fields[0].strip();
			final int numseats = Integer.parseInt(fields[1].strip());
			try {
				this.process(extid, numseats);
			}
			catch (Exception error) {
				throw new RuntimeException(error);
			}
		}

		@Override
		protected void oflush(int minsize) throws BWFLAException
		{
			if (obatch.size() >= minsize) {
				objects.setNumObjectSeatsForTenant(objectArchiveId, obatch, tenant);
				obatch.clear();
			}
		}

		private void process(String extid, int numseats) throws BWFLAException
		{
			// look up matching publication...
			final DigitalPublication publication = inventory.lookup(DigitalPublication.filter(extid));

			// look up matching record...
			final var filter = PersistentDigPubRecord.filter(tenant, extid);
			PersistentDigPubRecord record = records.lookup(filter);
			if (record == null) {
				// not found, initialize new record
				record = new PersistentDigPubRecord()
						.setTenantId(tenant)
						.setExternalId(extid);
			}

			// update record's fields...
			record.setNumSeats(numseats);
			if (publication != null) {
				// matching publication exists!
				final DigPubStatus status = record.getStatus();
				if (!status.isMatched()) {
					status.setMatchedOnDate(date)
							.setNewFlag(true);
				}

				// update corresponding entry in object-archive
				final String oid = publication.getObjectId();
				obatch.add(new SeatDescription(oid, numseats));
				this.oflush(OBJECTS_BATCH_SIZE);

				++numRecordsMatched;
			}

			// make all changes persistent...
			rbatch.replace(filter, record);
			this.rflush(RECORDS_BATCH_SIZE);

			++numRecordsImported;
		}
	}

	private class DigPubRecordDeleter extends DigPubRecordProcessor<String, String>
	{
		private int numRecordsDeleted;

		public DigPubRecordDeleter(String tenant)
		{
			super(tenant);

			this.numRecordsDeleted = 0;
		}

		public int getNumRecordsDeleted()
		{
			return numRecordsDeleted;
		}

		@Override
		public void accept(String extid)
		{
			try {
				this.process(extid);
			}
			catch (Exception error) {
				throw new RuntimeException(error);
			}
		}

		@Override
		protected void oflush(int minsize) throws BWFLAException
		{
			if (obatch.size() >= minsize) {
				objects.resetNumObjectSeatsForTenant(objectArchiveId, obatch, tenant);
				obatch.clear();
			}
		}

		private void process(String extid) throws BWFLAException
		{
			// delete records in batches...
			rbatch.delete(PersistentDigPubRecord.filter(tenant, extid));
			this.rflush(RECORDS_BATCH_SIZE);

			// look up matching publication's object-id...
			final DigitalPublication publication = inventory.lookup(DigitalPublication.filter(extid));
			if (publication != null) {
				// remove corresponding entries from object-archive
				obatch.add(publication.getObjectId());
				this.oflush(OBJECTS_BATCH_SIZE);
			}

			++numRecordsDeleted;
		}
	}
}
