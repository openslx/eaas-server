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

package de.bwl.bwfla.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Priority;

import org.apache.tamaya.yaml.YAMLFormat;


@Priority(100)
public class CustomYamlFormat extends YAMLFormat
{
	@Override
	public void mapYamlIntoProperties(String prefix, Object config, HashMap<String, String> values)
	{
		if (config instanceof List) {
			this.process(prefix, (List<?>) config, values);
		}
		else if (config instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) config;
			this.process(prefix, map, values);
		}
		else {
			// It's a primitive type
			String value = this.mapValueToString(config);
			values.put(prefix, value);
		}
	}
	
	private void process(String prefix, List<?> list, HashMap<String, String> values)
	{
		if (list.isEmpty())
			throw new IllegalStateException("Empty configuration values are not supported!");

		// Check types of list's elements
		final Class<?> type = list.get(0).getClass();
		final Consumer<Object> typecheck = (value) -> {
			if (value.getClass() != type)
				throw new IllegalStateException("All values in a list must be of the same type!");
		};
		
		list.forEach(typecheck);

		// Process the elements recursively...
		int counter = 0;
		for (Object value : list) {
			String newPrefix = prefix + "[" + counter + "]";
			this.mapYamlIntoProperties(newPrefix, value , values);
			++counter;
		}
	}
	
	private void process(String prefix, Map<String, Object> map, HashMap<String, String> values)
	{
		if (map.isEmpty())
			throw new IllegalStateException("Empty configuration values are not supported!");
		
		// Process the elements recursively...
		final BiConsumer<String, Object> action = (key, value) -> {
			String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
			this.mapYamlIntoProperties(newPrefix, value, values);
		};
		
		map.forEach(action);
	}
}
