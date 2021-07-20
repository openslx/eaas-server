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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

	@JsonGetter("start_timestamp")
	public String getStartTimestamp()
	{
		return startTimestamp;
	}

	@JsonGetter("num_records_downloaded")
	public int getNumRecordsDownloaded()
	{
		return numRecordsDownloaded;
	}

	@JsonGetter("duration_seconds")
	public int getDurationInSeconds()
	{
		return durationInSeconds;
	}

	@JsonIgnore
	public HarvestingResult onRecordDownloaded()
	{
		++numRecordsDownloaded;
		return this;
	}

	@JsonIgnore
	public HarvestingResult onRecordsDownloaded(int number)
	{
		numRecordsDownloaded += number;
		return this;
	}

	@JsonIgnore
	public HarvestingResult setDurationInSeconds(long duration)
	{
		this.durationInSeconds = (int) duration;
		return this;
	}
}
