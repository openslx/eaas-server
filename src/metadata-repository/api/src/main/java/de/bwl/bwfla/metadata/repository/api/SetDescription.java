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

package de.bwl.bwfla.metadata.repository.api;

import de.bwl.bwfla.metadata.repository.json.IJsonStreamable;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;


public class SetDescription implements IJsonStreamable
{
	private String spec;
	private String name;

	public static final char SPEC_SEPARATOR = ':';


	public SetDescription(String spec)
	{
		this.spec = spec;
		this.name = null;
	}

	public SetDescription(String... specparts)
	{
		this(SetDescription.toSpecString(specparts));
	}

	public String getSpec()
	{
		return spec;
	}

	public String getName()
	{
		return name;
	}

	public SetDescription setName(String name)
	{
		this.name = name;
		return this;
	}

	public SetDescription setSpec(String... parts)
	{
		this.spec = SetDescription.toSpecString(parts);
		return this;
	}

	public void write(JsonGenerator json, String objname) throws JsonException
	{
		if (objname != null)
			json.writeStartObject(objname);
		else json.writeStartObject();

		json.write(Fields.NAME, this.getName());
		json.write(Fields.SPEC, this.getSpec());
		json.writeEnd();
	}

	public static SetDescription from(JsonObject json) throws JsonException
	{
		try {
			return new SetDescription(json.getString(Fields.SPEC))
					.setName(json.getString(Fields.NAME, ""));
		}
		catch (Exception error) {
			throw new JsonException("Constructing object failed!", error);
		}
	}

	public static String toSpecString(String... parts)
	{
		if (parts.length < 1)
			return "";

		final StringBuilder sb = new StringBuilder(128);
		for (String part : parts)
			sb.append(part).append(SPEC_SEPARATOR);

		sb.setLength(sb.length() - 1);
		return sb.toString();
	}


	// ========== Internal Helpers ==============================

	private static final class Fields
	{
		static final String NAME = "name";
		static final String SPEC = "spec";
	}
}
