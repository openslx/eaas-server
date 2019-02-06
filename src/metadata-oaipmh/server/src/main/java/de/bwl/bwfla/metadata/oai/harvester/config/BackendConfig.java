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

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.metadata.oai.common.config.BaseConfig;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;


public class BackendConfig extends BaseConfig
{
	private String name;

	private SourceConfig source = new SourceConfig();
	private SinkConfig sink = new SinkConfig();


	// ========== Getters and Setters ==============================

	@JsonProperty(Fields.NAME)
	public String getName()
	{
		return name;
	}

	@Config(Fields.NAME)
	public void setName(String name)
	{
		ConfigHelpers.check(name, "Name is invalid!");
		this.name = name;
	}

	@JsonProperty(Fields.SINK)
	public SinkConfig getSinkConfig()
	{
		return sink;
	}

	public void setSinkConfig(SinkConfig sink)
	{
		this.sink = sink;
	}

	@JsonProperty(Fields.SOURCE)
	public SourceConfig getSourceConfig()
	{
		return source;
	}

	public void setSourceConfig(SourceConfig source)
	{
		this.source = source;
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
			this.url = url;
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
			this.baseurl = url;
		}
	}


	// ========== Initialization ==============================

	public void load(Configuration config)
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);
		ConfigHelpers.configure(source, ConfigHelpers.filter(config, "source."));
		ConfigHelpers.configure(sink, ConfigHelpers.filter(config, "sink."));
	}


	// ========== Internal Helpers ==============================

	private static class Fields
	{
		private static final String NAME     = "name";
		private static final String SOURCE   = "source";
		private static final String SINK     = "sink";
		private static final String URL      = "url";
		private static final String BASE_URL = "base_url";
	}
}
