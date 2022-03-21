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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractMetaData
{
	private String kind;

	protected AbstractMetaData(String kind)
	{
		this.setKind(kind);
	}

	protected AbstractMetaData(String type, String version)
	{
		this.kind = AbstractMetaData.kind(type, version);
	}

	@JsonSetter(Fields.KIND)
	protected void setKind(String kind)
	{
		if (kind == null || kind.isEmpty())
			throw new IllegalArgumentException();

		this.kind = kind;
	}

	@JsonGetter(Fields.KIND)
	public String kind()
	{
		return kind;
	}


	public static String kind(String type, String version)
	{
		if (type == null || type.isEmpty())
			throw new IllegalArgumentException();

		if (version == null || version.isEmpty())
			throw new IllegalArgumentException();

		return type + "/" + version;
	}


	public static class Fields
	{
		private static final String KIND = "kind";
	}
}
