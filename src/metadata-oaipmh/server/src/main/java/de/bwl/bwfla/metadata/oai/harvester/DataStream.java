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

package de.bwl.bwfla.metadata.oai.harvester;

import de.bwl.bwfla.common.services.guacplay.util.StopWatch;
import de.bwl.bwfla.common.services.security.MachineTokenProvider;
import de.bwl.bwfla.metadata.oai.harvester.config.BackendConfig;
import de.bwl.bwfla.metadata.oai.harvester.config.HarvesterConfig;
import de.bwl.bwfla.metadata.repository.api.ItemDescription;
import de.bwl.bwfla.metadata.repository.api.ItemIdentifierDescription;
import de.bwl.bwfla.metadata.repository.client.MetaDataRepository;
import org.dspace.xoai.model.oaipmh.Granularity;
import org.dspace.xoai.model.oaipmh.Header;
import org.dspace.xoai.model.oaipmh.MetadataFormat;
import org.dspace.xoai.model.oaipmh.Record;
import org.dspace.xoai.serviceprovider.ServiceProvider;
import org.dspace.xoai.serviceprovider.client.HttpOAIClient;
import org.dspace.xoai.serviceprovider.exceptions.BadArgumentException;
import org.dspace.xoai.serviceprovider.exceptions.HarvestException;
import org.dspace.xoai.serviceprovider.exceptions.IdDoesNotExistException;
import org.dspace.xoai.serviceprovider.exceptions.InternalHarvestException;
import org.dspace.xoai.serviceprovider.exceptions.NoMetadataFormatsException;
import org.dspace.xoai.serviceprovider.model.Context;
import org.dspace.xoai.serviceprovider.parameters.ListRecordsParameters;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.transform.Transformer;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class DataStream
{
	private final Logger log;
	private final BackendConfig.StreamConfig config;
	private final MetaDataRepository mdrepo;
	private final ServiceProvider service;
	private final String secret;

	public DataStream(BackendConfig.StreamConfig config, Client http, String secret, Logger log)
	{
		this.log = log;
		this.config = config;

		final WebTarget endpoint = http.target(config.getSinkConfig().getBaseUrl());
		this.secret = secret;
		this.mdrepo = new MetaDataRepository(endpoint, secret);
		this.service = new ServiceProvider(newContext(config.getSourceConfig()));
	}

	public HarvestingResult execute() throws HarvestException
	{
		return this.execute(DataStream.getDefaultFromTimestamp());
	}

	public HarvestingResult execute(Instant fromts) throws HarvestException
	{
		return this.execute(fromts, DataStream.getDefaultUntilTimestamp());
	}

	public synchronized HarvestingResult execute(Instant fromts, Instant untilts) throws HarvestException
	{
		final StopWatch stopwatch = new StopWatch();
		final Instant startTimestamp = Instant.now();

		final BackendConfig.SourceConfig source = config.getSourceConfig();
		final BackendConfig.SinkConfig sink = config.getSinkConfig();
		final String mdprefix = HarvesterConfig.getMetaDataFormat();

		log.info("Starting matadata-harvesting from remote repository: " + source.getUrl());
		log.info("Using timestamp-range: " + fromts.toString() + " -- " + untilts.toString());

		log.info("Checking supported metadata-formats...");
		if (!this.checkMetaDataFormat(mdprefix))
			throw new NoMetadataFormatsException("No supported metadata-formats found!");

		log.info("Using metadata-format: " + mdprefix);

		final HarvestingResult result = new HarvestingResult(startTimestamp);

		// Download and forward all records to the sink endpoint
		log.info("Forwarding records to destination: " + sink.getBaseUrl());
		final Iterator<Record> records = this.listRecords(mdprefix, fromts, untilts);
		final Stream<ItemDescription> items = DataStream.toItemStream(records)
				.peek((item) -> {
					result.onRecordDownloaded();

					// Update latest item's timestamp
					final Instant curts = Instant.parse(item.getIdentifier().getTimestamp());
					if (curts.isAfter(config.getLatestItemTimestamp()))
						config.setLatestItemTimestamp(curts);
				});

		// Prepare forwarding request
		final MetaDataRepository.Items.Insert request = mdrepo.items()
				.insert(items);

		// Stream records to the sink endpoint
		try (final Response response = request.execute()) {
			if (response.getStatusInfo() == Response.Status.OK) {
				final long durms = stopwatch.timems();
				final long dursec = TimeUnit.MILLISECONDS.toSeconds(durms);
				final String message = result.getNumRecordsDownloaded() + " record(s) forwarded in "
						+ ((dursec != 0) ? (dursec + " second(s)") : (durms + " msec(s)"));

				log.info(message);

				result.setDurationInSeconds(dursec);
			}
			else {
				final String reason = response.getStatusInfo().getReasonPhrase();
				throw new InternalHarvestException("Forwarding records failed with: " + reason);
			}
		}

		return result;
	}

	public BackendConfig.StreamConfig getConfig()
	{
		return config;
	}

	public Instant getLatestItemTimestamp()
	{
		return config.getLatestItemTimestamp();
	}

	public static Instant getDefaultFromTimestamp()
	{
		return Instant.ofEpochMilli(0L);
	}

	public static Instant getDefaultUntilTimestamp()
	{
		return Instant.now();
	}


	// ========== Internal Helpers ==============================

	private static Context newContext(BackendConfig.SourceConfig config)
	{
		final String baseurl = config.getUrl();
		final String format = HarvesterConfig.getMetaDataFormat();
		final Transformer transformer = HarvesterConfig.getMetaDataTransformer();

		String token = null;
		if(config.getSecret() != null)
			token = MachineTokenProvider.getJwt(config.getSecret());
		return new Context()
				.withBaseUrl(baseurl)
				.withGranularity(Granularity.Second)
				.withOAIClient(new HttpOAIClient(baseurl, token))
				.withMetadataTransformer(format, transformer);
	}

	private boolean checkMetaDataFormat(String mdprefix) throws IdDoesNotExistException
	{
		for (final Iterator<MetadataFormat> iter = service.listMetadataFormats(); iter.hasNext();) {
			final MetadataFormat format = iter.next();
			if (format.getMetadataPrefix().contentEquals(mdprefix))
				return true;
		}

		return false;
	}

	private Iterator<Record> listRecords(String mdprefix, Instant fromts, Instant untilts) throws BadArgumentException
	{
		final ListRecordsParameters params = new ListRecordsParameters()
				.withMetadataPrefix(mdprefix)
				.withFrom(new Date(fromts.toEpochMilli()))
				.withUntil(new Date(untilts.toEpochMilli()));

		return service.listRecords(params);
	}

	private static Stream<ItemDescription> toItemStream(Iterator<Record> records)
	{
		final Function<Record, ItemDescription> mapper = (record) -> {
			final Header header = record.getHeader();
			final ItemIdentifierDescription id = new ItemIdentifierDescription(header.getIdentifier())
					.setTimestamp(header.getDatestamp())
					.setDeleted(header.isDeleted())
					.setSets(header.getSetSpecs());

			return new ItemDescription(id)
					.setMetaData(record.getMetadata().getValueString());
		};

		final Spliterator<Record> spliterator = Spliterators.spliteratorUnknownSize(records, 0);
		return StreamSupport.stream(spliterator, false)
				.map(mapper);
	}
}
