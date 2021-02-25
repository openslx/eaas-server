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

package com.openslx.eaas.imagearchive.config;

import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.configuration.converters.DurationPropertyConverter;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.WithPropertyConverter;

import java.nio.file.Path;
import java.time.Duration;


public class ImporterConfig extends BaseConfig<ImporterConfig>
{
	private int numWorkers;
	private Duration gcInterval;
	private Duration maxRecordAge;
	private Path basedir;


	// ===== Getters and Setters ====================

	@Config("num_workers")
	public void setNumWorkers(int num)
	{
		ConfigHelpers.check(num, 1, 256, "Number of workers is invalid!");
		this.numWorkers = num;
	}

	public int getNumWorkers()
	{
		return numWorkers;
	}

	@Config("gc_interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	public void setGcInterval(Duration interval)
	{
		ConfigHelpers.check(interval, "GC interval is invalid!");
		this.gcInterval = interval;
	}

	public Duration getGcInterval()
	{
		return gcInterval;
	}

	@Config("max_record_age")
	@WithPropertyConverter(DurationPropertyConverter.class)
	public void setMaxRecordAge(Duration age)
	{
		ConfigHelpers.check(age, "Max. record age is invalid!");
		this.maxRecordAge = age;
	}

	public Duration getMaxRecordAge()
	{
		return maxRecordAge;
	}

	@Config("base_directory")
	public void setBaseDirectory(Path dir)
	{
		ConfigHelpers.check(dir, "Base directory is invalid!");
		this.basedir = dir;
	}

	public Path getBaseDirectory()
	{
		return basedir;
	}
}
