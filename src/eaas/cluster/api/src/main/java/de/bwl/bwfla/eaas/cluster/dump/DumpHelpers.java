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

package de.bwl.bwfla.eaas.cluster.dump;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.NotFoundException;

import de.bwl.bwfla.eaas.cluster.ResourceSpec;


public final class DumpHelpers
{
	public static JsonGenerator writeResourceTimestamp(JsonGenerator json)
	{
		return json.write("__timestamp", LocalDateTime.now().toString());
	}

	public static JsonObjectBuilder addResourceTimestamp(JsonObjectBuilder json)
	{
		return json.add("__timestamp", LocalDateTime.now().toString());
	}

	public static JsonGenerator writeResourceType(JsonGenerator json, String type)
	{
		return json.write("__resource_type", type);
	}

	public static JsonObjectBuilder addResourceType(JsonObjectBuilder json, String type)
	{
		return json.add("__resource_type", type);
	}

	public static JsonGenerator writeResourceType(JsonGenerator json, Class<?> clazz)
	{
		return DumpHelpers.writeResourceType(json, clazz.getSimpleName());
	}

	public static JsonObjectBuilder addResourceType(JsonObjectBuilder json, Class<?> clazz)
	{
		return DumpHelpers.addResourceType(json, clazz.getSimpleName());
	}

	public static JsonGenerator write(JsonGenerator json, ResourceSpec spec)
	{
		json.write("cpu", spec.cpu() + "m")
			.write("memory", spec.memory() + "MB");

		return json;
	}

	public static JsonObjectBuilder add(JsonObjectBuilder json, ResourceSpec spec)
	{
		json.add("cpu", spec.cpu() + "m")
			.add("memory", spec.memory() + "MB");

		return json;
	}

	public static JsonGenerator write(JsonGenerator json, String name, ResourceSpec spec)
	{
		json.writeStartObject(name);
		DumpHelpers.write(json, spec);
		json.writeEnd();
		return json;
	}

	public static JsonObjectBuilder add(JsonObjectBuilder json, String name, ResourceSpec spec)
	{
		return json.add(name, DumpHelpers.add(Json.createObjectBuilder(), spec));
	}

	public static JsonObject merge(JsonObject obj1, JsonObject obj2)
	{
		final JsonObjectBuilder json = Json.createObjectBuilder();

		final BiConsumer<String, JsonValue> addAndMergeObj2 = (key, value) -> {
			if (!obj2.containsKey(key)) {
				json.add(key, value);
				return;
			}

			switch (value.getValueType())
			{
				case OBJECT: {
					final JsonObjectBuilder merged = Json.createObjectBuilder();
					final BiConsumer<String, JsonValue> adder = (k,v) -> merged.add(k,v);
					((JsonObject) value).forEach(adder);
					((JsonObject) obj2.get(key)).forEach(adder);
					json.add(key, merged);
					break;
				}

				default:
					final String message = "Merging JSON objects on key '" + key + "' failed!\n"
							+ "Object-1:  " + obj1.toString() + "\n"
							+ "Object-2:  " + obj2.toString();

					throw new IllegalStateException(message);
			}
		};

		final BiConsumer<String, JsonValue> mergeMissing = (key, value) -> {
			if (obj1.containsKey(key))
				return;

			json.add(key, value);
		};

		obj1.forEach(addAndMergeObj2);
		obj2.forEach(mergeMissing);
		return json.build();
	}

	public static JsonObject toJsonObject(ResourceSpec spec)
	{
		return DumpHelpers.add(Json.createObjectBuilder(), spec).build();
	}

	public static String toDurationString(long durationInMsec)
	{
		return Duration.ofMillis(durationInMsec).toString();
	}

	public static void notfound(String resource) throws NotFoundException
	{
		throw new NotFoundException("Resource '" + resource + "' was not found!");
	}
}
