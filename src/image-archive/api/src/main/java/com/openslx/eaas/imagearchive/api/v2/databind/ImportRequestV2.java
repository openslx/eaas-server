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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportRequestV2
{
	private String description;
	private ImportSourceV2 source;
	private ImportTargetV2 target;

	@JsonSetter(Fields.DESCRIPTION)
	public ImportRequestV2 setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@JsonGetter(Fields.DESCRIPTION)
	public String description()
	{
		return description;
	}

	@JsonSetter(Fields.SOURCE)
	public ImportRequestV2 setSource(ImportSourceV2 source)
	{
		this.source = source;
		return this;
	}

	@JsonGetter(Fields.SOURCE)
	public ImportSourceV2 source()
	{
		if (source == null)
			source = new ImportSourceV2();

		return source;
	}

	@JsonSetter(Fields.TARGET)
	public ImportRequestV2 setTarget(ImportTargetV2 target)
	{
		this.target = target;
		return this;
	}

	@JsonGetter(Fields.TARGET)
	public ImportTargetV2 target()
	{
		if (target == null)
			target = new ImportTargetV2();

		return target;
	}


	private static final class Fields
	{
		public static final String DESCRIPTION = "description";
		public static final String SOURCE      = "source";
		public static final String TARGET      = "target";
	}
}
