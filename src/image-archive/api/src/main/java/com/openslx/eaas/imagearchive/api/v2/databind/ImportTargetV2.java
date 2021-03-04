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

package com.openslx.eaas.imagearchive.api.v2.databind;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportTargetV2
{
	private Kind kind;
	private String name;
	private String location;

	@JsonSetter(Fields.KIND)
	public ImportTargetV2 setKind(Kind kind)
	{
		this.kind = kind;
		return this;
	}

	@JsonGetter(Fields.KIND)
	public Kind kind()
	{
		return kind;
	}

	@JsonSetter(Fields.NAME)
	public ImportTargetV2 setName(String name)
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
	public ImportTargetV2 setLocation(String location)
	{
		this.location = location;
		return this;
	}

	@JsonGetter(Fields.LOCATION)
	public String location()
	{
		return location;
	}


	public enum Kind
	{
		CHECKPOINT,
		IMAGE;

		@JsonValue
		public String value()
		{
			return this.name()
					.toLowerCase();
		}

		@JsonCreator
		public static Kind from(String kind)
		{
			switch (kind) {
				case "checkpoint":
					return CHECKPOINT;
				case "image":
					return IMAGE;
				default:
					throw new IllegalArgumentException();
			}
		}
	}


	private static final class Fields
	{
		public static final String KIND     = "kind";
		public static final String NAME     = "name";
		public static final String LOCATION = "location";
	}
}
