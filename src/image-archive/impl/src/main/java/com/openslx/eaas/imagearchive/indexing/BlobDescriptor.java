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

package com.openslx.eaas.imagearchive.indexing;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.time.ZonedDateTime;


public class BlobDescriptor
{
	private String name;
	private String location;
	private long mtime;


	@JsonSetter(Fields.NAME)
	public void setName(String name)
	{
		this.name = name;
	}

	@JsonGetter(Fields.NAME)
	public String name()
	{
		return name;
	}

	@JsonSetter(Fields.LOCATION)
	public void setLocation(String location)
	{
		this.location = location;
	}

	@JsonGetter(Fields.LOCATION)
	public String location()
	{
		return location;
	}

	@JsonSetter(Fields.MTIME)
	public void setModTime(long timestamp)
	{
		this.mtime = timestamp;
	}

	@JsonIgnore
	public void setModTime(ZonedDateTime time)
	{
		final var timestamp = time.toInstant()
				.toEpochMilli();

		this.setModTime(timestamp);
	}

	@JsonGetter(Fields.MTIME)
	public long mtime()
	{
		return mtime;
	}

	public static DocumentCollection.Filter filter(String name)
	{
		return DocumentCollection.filter()
				.eq(Fields.NAME, name);
	}

	public static <T extends BlobDescriptor> void index(DocumentCollection<T> entries)
			throws BWFLAException
	{
		// NOTE: we want to look up entries by 'name ' and 'location'
		entries.index(Fields.NAME);
		entries.index(Fields.LOCATION);
	}


	// ===== Internal Helpers ==============================

	protected static final class Fields
	{
		public static final String NAME     = "nam";
		public static final String LOCATION = "loc";
		public static final String MTIME    = "mts";
		public static final String DATA     = "dat";
	}
}
