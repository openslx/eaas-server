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

package de.bwl.bwfla.metadata.repository.json;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;


public class JsonUtils
{
	public static void expect(JsonParser json, String key)
			throws JsonParsingException
	{
		JsonUtils.expect(json, json.next(), JsonParser.Event.KEY_NAME);
		if (!key.contentEquals(json.getString()))
			throw JsonUtils.fail("Field name expected: " + key, json);
	}

	public static void expect(JsonParser json, JsonParser.Event expevent)
			throws JsonParsingException
	{
		JsonUtils.expect(json, json.next(), expevent);
	}

	public static void expect(JsonParser json, JsonParser.Event curevent, JsonParser.Event expevent)
			throws JsonParsingException
	{
		if (curevent != expevent) {
			final String message = "Expected " + expevent.toString() + ", but found " + curevent.toString();
			throw JsonUtils.fail(message, json);
		}
	}

	public static JsonParsingException fail(String message, JsonParser json)
	{
		return new JsonParsingException(message, json.getLocation());
	}
}
