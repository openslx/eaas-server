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

package com.openslx.eaas.imagearchive.databind;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.openslx.eaas.imagearchive.BlobKind;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportTarget
{
	private BlobKind kind;
	private String name;
	private String location;

	@JsonSetter(Fields.KIND)
	public ImportTarget setKind(String kind)
	{
		this.kind = BlobKind.from(kind);
		return this;
	}

	@JsonGetter(Fields.KIND)
	public String kindstr()
	{
		return kind.value();
	}

	@JsonIgnore
	public ImportTarget setKind(BlobKind kind)
	{
		this.kind = kind;
		return this;
	}

	@JsonIgnore
	public BlobKind kind()
	{
		return kind;
	}

	@JsonSetter(Fields.NAME)
	public ImportTarget setName(String name)
	{
		this.name = name;
		return this;
	}

	@JsonGetter(Fields.NAME)
	public String name()
	{
		return name;
	}

	@JsonSetter(Fields.LOCATION)
	public ImportTarget setLocation(String location)
	{
		this.location = location;
		return this;
	}

	@JsonGetter(Fields.LOCATION)
	public String location()
	{
		return location;
	}

	@JsonIgnore
	public void validate() throws IllegalArgumentException
	{
		if (kind == null)
			throw new IllegalArgumentException("Blob kind is invalid!");
	}


	private static final class Fields
	{
		public static final String KIND     = "knd";
		public static final String NAME     = "nam";
		public static final String LOCATION = "loc";
	}
}
