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

import javax.json.Json;
import javax.json.JsonObject;
import java.time.Instant;


public class HarvestingResult
{
	private final String startTimestamp;
	private int numRecordsDownloaded;
	private int durationInSeconds;

	public HarvestingResult(Instant startts)
	{
		this.startTimestamp = startts.toString();
		this.numRecordsDownloaded = 0;
		this.durationInSeconds = 0;
	}

	public int getNumRecordsDownloaded()
	{
		return numRecordsDownloaded;
	}

	public int getDurationInSeconds()
	{
		return durationInSeconds;
	}

	public HarvestingResult onRecordDownloaded()
	{
		++numRecordsDownloaded;
		return this;
	}

	public HarvestingResult onRecordsDownloaded(int number)
	{
		numRecordsDownloaded += number;
		return this;
	}

	public HarvestingResult setDurationInSeconds(long duration)
	{
		this.durationInSeconds = (int) duration;
		return this;
	}

	public String toJsonString()
	{
		final JsonObject json = Json.createObjectBuilder()
				.add("num_records_downloaded", numRecordsDownloaded)
				.add("duration_seconds", durationInSeconds)
				.add("start_timestamp", startTimestamp)
				.build();

		return json.toString();
	}
}
