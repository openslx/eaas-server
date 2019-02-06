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

import java.time.Instant;


public class HarvesterStatus
{
	private Instant lastRunTimestamp;
	private Instant latestItemTimestamp;


	private HarvesterStatus()
	{
		// Default constructor
	}

	public HarvesterStatus(Instant timestamp)
	{
		this.lastRunTimestamp = Instant.from(timestamp);
		this.latestItemTimestamp = timestamp;
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
}
