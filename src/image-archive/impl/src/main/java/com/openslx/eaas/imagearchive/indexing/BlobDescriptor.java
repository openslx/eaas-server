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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BlobDescriptor
{
	private String name;
	private String location;
	private String etag;
	private long mtime;

	/** Inlined aliases */
	private Set<String> aliases;


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

	@JsonSetter(Fields.ETAG)
	public void setEtag(String etag)
	{
		this.etag = etag;
	}

	@JsonGetter(Fields.ETAG)
	public String etag()
	{
		return etag;
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

	@JsonSetter(Fields.ALIASES)
	public void setAliases(Set<String> aliases)
	{
		this.aliases = aliases;
	}

	@JsonGetter(Fields.ALIASES)
	public Set<String> aliases()
	{
		if (aliases == null)
			aliases = new HashSet<>();

		return aliases;
	}

	public static DocumentCollection.Filter filter(String name)
	{
		final var f1 = DocumentCollection.filter()
				.eq(Fields.NAME, name);

		final var f2 = DocumentCollection.filter()
				.eq(Fields.ALIASES, name);

		return DocumentCollection.Filter.or(f1, f2);
	}

	public static DocumentCollection.Filter filter(FilterOptions options)
	{
		final var filter = DocumentCollection.filter();
		if (options.location() != null)
			filter.eq(Fields.LOCATION, options.location());

		if (options.from() > 0L)
			filter.gte(Fields.MTIME, options.from());

		if (options.until() > 0L)
			filter.lt(Fields.MTIME, options.until());

		return filter;
	}

	public static <T extends BlobDescriptor> void index(DocumentCollection<T> entries)
			throws BWFLAException
	{
		// NOTE: we want to look up entries by 'name ' and 'location'
		entries.index(Fields.NAME);
		entries.index(Fields.LOCATION);

		// entries should be queryable by their aliases too
		entries.index(Fields.ALIASES);
	}


	// ===== Internal Helpers ==============================

	public static final class Fields
	{
		public static final String NAME     = "nam";
		public static final String LOCATION = "loc";
		public static final String ETAG     = "etg";
		public static final String MTIME    = "mts";
		public static final String DATA     = "dat";
		public static final String ALIASES  = "als";
	}
}
