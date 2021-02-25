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


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportTask
{
	private String description;
	private ImportSource source;
	private ImportTarget target;

	@JsonSetter(Fields.DESCRIPTION)
	public ImportTask setDescription(String description)
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
	public ImportTask setSource(ImportSource source)
	{
		this.source = source;
		return this;
	}

	@JsonGetter(Fields.SOURCE)
	public ImportSource source()
	{
		if (source == null)
			source = new ImportSource();

		return source;
	}

	@JsonSetter(Fields.TARGET)
	public ImportTask setTarget(ImportTarget target)
	{
		this.target = target;
		return this;
	}

	@JsonGetter(Fields.TARGET)
	public ImportTarget target()
	{
		if (target == null)
			target = new ImportTarget();

		return target;
	}

	@JsonIgnore
	public void validate() throws IllegalArgumentException
	{
		if (source == null)
			throw new IllegalArgumentException("Import source is invalid!");

		if (target == null)
			throw new IllegalArgumentException("Import target is invalid!");

		source.validate();
		target.validate();
	}


	private static final class Fields
	{
		public static final String DESCRIPTION = "dsc";
		public static final String SOURCE      = "src";
		public static final String TARGET      = "tgt";
	}
}
