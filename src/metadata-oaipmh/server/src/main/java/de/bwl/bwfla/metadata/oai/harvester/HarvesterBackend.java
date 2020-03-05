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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.tamaya.ConfigurationProvider;
import org.dspace.xoai.serviceprovider.exceptions.HarvestException;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.logging.PrefixLoggerContext;
import de.bwl.bwfla.common.services.guacplay.util.StopWatch;
import de.bwl.bwfla.metadata.oai.harvester.config.BackendConfig;

import javax.ws.rs.client.Client;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class HarvesterBackend
{
	private final Logger log;
	private final StateDescription state;
	private final Collection<DataStream> streams;


	public HarvesterBackend(StateDescription state, Client http)
	{
		final BackendConfig config = state.getBackendConfig();
		final PrefixLoggerContext logctx = new PrefixLoggerContext()
				.add(config.getName());

		String secret = ConfigurationProvider.getConfiguration().get("rest.apiSecret");

		this.log = new PrefixLogger(this.getClass().getName(), logctx);
		this.state = state;
		this.streams = new ArrayList<>(config.getStreamConfigs().size());

		for (BackendConfig.StreamConfig sc : config.getStreamConfigs())
			streams.add(new DataStream(sc, http, secret, log));
	}

	public HarvesterBackend(BackendConfig config, Client http)
	{
		this(new StateDescription(config), http);
	}

	public HarvestingResult execute() throws HarvestException
	{
		return this.execute(null, null);
	}

	public HarvestingResult execute(Instant fromts, Instant untilts) throws HarvestException
	{
		final StopWatch stopwatch = new StopWatch();
		final Instant startTimestamp = Instant.now();
		final HarvestingResult result = new HarvestingResult(startTimestamp);

		log.info("Starting " + streams.size() + " harvesting-stream(s)...");

		// Execute all streams sequentially, since their order may be important!
		for (DataStream stream : streams) {
			final Instant fts = (fromts != null) ? fromts : stream.getLatestItemTimestamp();
			final Instant uts = (untilts != null) ? untilts : DataStream.getDefaultUntilTimestamp();
			final HarvestingResult sr = stream.execute(fts, uts);
			result.onRecordsDownloaded(sr.getNumRecordsDownloaded());
		}

		final long durms = stopwatch.timems();
		final long dursec = TimeUnit.MILLISECONDS.toSeconds(durms);
		final String message = streams.size() + " harvesting-stream(s) finished in "
				+ ((dursec != 0) ? (dursec + " second(s)") : (durms + " msec(s)"));

		log.info(message);

		state.setLastRunTimestamp(Instant.now());
		result.setDurationInSeconds(dursec);
		return result;
	}

	public String getName()
	{
		return this.getConfig().getName();
	}

	public BackendConfig getConfig()
	{
		return state.getBackendConfig();
	}

	public StateDescription getStateDescription()
	{
		return state;
	}

	public HarvesterStatus getStatus()
	{
		return new HarvesterStatus(this);
	}


	public static class StateDescription
	{
		private BackendConfig config;
		private Instant lastRunTimestamp;


		private StateDescription()
		{
			// Default constructor
		}

		public StateDescription(BackendConfig config)
		{
			this.config = config;
			this.lastRunTimestamp = Instant.EPOCH;
		}

		@JsonProperty("config")
		public BackendConfig getBackendConfig()
		{
			return config;
		}

		public void setBackendConfig(BackendConfig config)
		{
			this.config = config;
		}

		@JsonIgnore
		public Instant getLastRunTimestamp()
		{
			return lastRunTimestamp;
		}

		public void setLastRunTimestamp(Instant timestamp)
		{
			this.lastRunTimestamp = timestamp;
		}

		@JsonProperty("last_run_timestamp")
		public String getLastRunTimestampAsString()
		{
			return lastRunTimestamp.toString();
		}

		@JsonSetter("last_run_timestamp")
		public void setLastRunTimestamp(String timestamp)
		{
			this.lastRunTimestamp = Instant.parse(timestamp);
		}

	}
}
