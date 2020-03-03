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

package de.bwl.bwfla.metadata.oai.harvester.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.metadata.oai.common.config.BaseConfig;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;


public class BackendConfig extends BaseConfig
{
	private String name;

	private String secret = null;

	private Collection<StreamConfig> streams = new ArrayList<>();


	// ========== Getters and Setters ==============================

	@JsonProperty(Fields.NAME)
	public String getName()
	{
		return name;
	}

	@JsonProperty(Fields.SECRET)
	public String getSecret()
	{
		return secret;
	}

	@Config(Fields.NAME)
	public void setName(String name)
	{
		ConfigHelpers.check(name, "Name is invalid!");
		this.name = name;
	}

	@Config(value = Fields.SECRET, required = false)
	public void setSecret(String secret)
	{
		if(!secret.isEmpty())
			this.secret = secret;
	}

	@JsonProperty(Fields.STREAMS)
	public Collection<StreamConfig> getStreamConfigs()
	{
		return streams;
	}

	public void setStreamConfigs(Collection<StreamConfig> streams)
	{
		this.streams = streams;
	}


	public static class SourceConfig
	{
		private String url;

		@JsonProperty(Fields.URL)
		public String getUrl()
		{
			return url;
		}

		@Config(Fields.URL)
		public void setUrl(String url)
		{
			ConfigHelpers.check(url, "URL is invalid!");
			this.url = BackendConfig.sanitize(url);
		}
	}

	public static class SinkConfig
	{
		private String baseurl;

		@JsonProperty(Fields.BASE_URL)
		public String getBaseUrl()
		{
			return baseurl;
		}

		@Config(Fields.BASE_URL)
		public void setBaseUrl(String url)
		{
			ConfigHelpers.check(url, "Base URL is invalid!");
			this.baseurl = BackendConfig.sanitize(url);
		}
	}

	public static class StreamConfig
	{
		private Instant latestItemTimestamp = Instant.EPOCH;
		private SourceConfig source = new SourceConfig();
		private SinkConfig sink = new SinkConfig();

		@JsonIgnore
		public Instant getLatestItemTimestamp()
		{
			return latestItemTimestamp;
		}

		public void setLatestItemTimestamp(Instant timestamp)
		{
			this.latestItemTimestamp = timestamp;
		}

		@JsonProperty("latest_item_timestamp")
		public String getLatestItemTimestampAsString()
		{
			return latestItemTimestamp.toString();
		}

		@JsonSetter("latest_item_timestamp")
		public void setLatestItemTimestamp(String timestamp)
		{
			this.latestItemTimestamp = Instant.parse(timestamp);
		}

		@JsonProperty(Fields.SOURCE)
		public SourceConfig getSourceConfig()
		{
			return source;
		}

		public void setSourceConfig(SourceConfig source)
		{
			ConfigHelpers.check(source, "Source config is invalid!");
			this.source = source;
		}

		@JsonProperty(Fields.SINK)
		public SinkConfig getSinkConfig()
		{
			return sink;
		}

		public void setSinkConfig(SinkConfig sink)
		{
			ConfigHelpers.check(sink, "Sink config is invalid!");
			this.sink = sink;
		}

		public void load(Configuration config)
		{
			// Configure annotated members of this instance
			ConfigHelpers.configure(source, ConfigHelpers.filter(config, Fields.SOURCE + "."));
			ConfigHelpers.configure(sink, ConfigHelpers.filter(config, Fields.SINK + "."));
		}
	}


	// ========== Initialization ==============================

	public void load(Configuration config)
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);

		// Configure streams for this instance
		{
			streams.clear();

			while (true) {
				final String prefix = ConfigHelpers.toListKey(Fields.STREAMS, streams.size(), ".");
				final Configuration sconfig = ConfigHelpers.filter(config, prefix);
				if (ConfigHelpers.isEmpty(sconfig))
					break;  // No more streams found!

				StreamConfig stream = new StreamConfig();
				stream.load(sconfig);
				streams.add(stream);
			}
		}
	}


	// ========== Internal Helpers ==============================

	private static class Fields
	{
		private static final String NAME     = "name";
		private static final String STREAMS  = "streams";
		private static final String SOURCE   = "source";
		private static final String SINK     = "sink";
		private static final String URL      = "url";
		private static final String BASE_URL = "base_url";
		private static final String SECRET   = "secret";
	}

	private static String sanitize(String url)
	{
		// Fix URLs containing empty paths:
		// http://host.org/a//b -> http://host.org/a/b

		final String separator = "://";
		final String[] parts = url.split(separator);
		if (parts.length != 2)
			throw new IllegalArgumentException();

		final String protocol = parts[0];
		final String path = parts[1].replaceAll("//", "/");
		return protocol + separator + path;
	}
}
