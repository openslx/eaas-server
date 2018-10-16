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

package de.bwl.bwfla.common.utils;


import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.inject.ConfigurationInjection;

import java.util.ArrayList;
import java.util.Collection;


public class ConfigHelpers
{
	/** Replace all chars from value with a replacement char */
	public static String anonymize(String value, char replacement)
	{
		int length = value.length();
		final StringBuilder sb = new StringBuilder(length);
		while (length > 0) {
			sb.append(replacement);
			--length;
		}
		
		return sb.toString();
	}
	
	/** Returns the key for a list element at specified index */
	public static String toListKey(String key, int index)
	{
		return (key + '[' + index + ']');
	}
	
	/** Returns the key for a list element at specified index */
	public static String toListKey(String key, int index, String suffix)
	{
		return (ConfigHelpers.toListKey(key, index) + suffix);
	}

	/** Looks up a list of configuration values and returns it as ArrayList */
	public static ArrayList<String> getAsList(Configuration config, String key)
	{
		final ArrayList<String> entries = new ArrayList<String>();
		while (true) {
			final String newkey = ConfigHelpers.toListKey(key, entries.size());
			final String value = config.get(newkey);
			if (value == null)
				break;

			entries.add(value);
		}

		return entries;
	}

	/** Returns a configuration containing entries with the specified prefix */
	public static Configuration filter(Configuration config, String prefix)
	{
		return config.with(ConfigurationFunctions.section(prefix, true));
		
//		// Workaround for a tamaya bug!
//		final KeyMapper mapper = (key) -> {
//			if (key.startsWith("_")) {
//				// We have a special key, perform remapping
//				return ("_" + prefix + key.substring(1));
//			}
//			else {
//				// We have a normal key
//				return (prefix + key);
//			}
//		};
//
//		return config.with(ConfigurationFunctions.map(mapper));
	}
	
	/** Returns a combined configuration from pre-filtered configs using specified prefixes */
	public static Configuration combine(Configuration config, String... prefixes)
	{
		final Configuration[] configs = new Configuration[prefixes.length];
		for (int i = 0; i < prefixes.length; ++i)
			configs[i] = ConfigHelpers.filter(config, prefixes[i]);
		
		return ConfigHelpers.combine(configs);
	}
	
	/** Returns a combined configuration from specified configs */
	public static Configuration combine(Configuration... configs)
	{
		return ConfigurationFunctions.combine("combined-config", configs);
	}
	
	/** Configure the specified object using passed configuration. */
	public static <T> T configure(T object, Configuration config)
	{
		return ConfigurationInjection.getConfigurationInjector().configure(object, config);
	}

	/** Returns true when the configuration does not contain any entries, else false */
	public static boolean isEmpty(Configuration config)
	{
		return config.getProperties().isEmpty();
	}

	public static void check(String arg, String message)
	{
		if (arg == null || arg.isEmpty())
			throw new ConfigException(message);
	}
	
	public static void check(Collection<?> arg, String message)
	{
		if (arg == null || arg.isEmpty())
			throw new ConfigException(message);
	}
	
	public static void check(int arg, int min, int max, String message)
	{
		if (arg < min || arg > max) {
			String minstr = (min == Integer.MIN_VALUE) ? "-inf" : Integer.toString(min);
			String maxstr = (max == Integer.MAX_VALUE) ? "+inf" : Integer.toString(max);
			String details = " Current: " + arg + ", Expected: [" + minstr + ", " + maxstr + "]";
			throw new ConfigException(message + details);
		}
	}
	
	public static void check(long arg, long min, long max, String message)
	{
		if (arg < min || arg > max) {
			String minstr = (min == Long.MIN_VALUE) ? "-inf" : Long.toString(min);
			String maxstr = (max == Long.MAX_VALUE) ? "+inf" : Long.toString(max);
			String details = " Current: " + arg + ", Expected: [" + minstr + ", " + maxstr + "]";
			throw new ConfigException(message + details);
		}
	}
	
	public static void check(float arg, float min, float max, String message)
	{
		if (arg < min || arg > max) {
			String minstr = (min == Float.MIN_VALUE) ? "-inf" : Float.toString(min);
			String maxstr = (max == Float.MAX_VALUE) ? "+inf" : Float.toString(max);
			String details = " Current: " + arg + ", Expected: [" + minstr + ", " + maxstr + "]";
			throw new ConfigException(message + details);
		}
	}
}
