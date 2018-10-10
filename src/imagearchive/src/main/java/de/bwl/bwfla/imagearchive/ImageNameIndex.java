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

package de.bwl.bwfla.imagearchive;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.configuration.BaseConfigurationPropertySourceProvider;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.spi.ConfigurationContext;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/** A simple index, containing mappings from symbolic names to image descriptions. */
public class ImageNameIndex
{
	private final Logger log;

	/** Symbolic image-name index: name --> image's description */
	private final Map<String, Entry> entries = new HashMap<String, Entry>();

	private static final String VERSION_SEPARATOR = "|";


	public ImageNameIndex(String indexConfigPath, Logger log) throws BWFLAException
	{
		this.log = log;

		// Load configuration for imagename-index, if possible...
		if (indexConfigPath == null) {
			log.info("Disabling imagename-index! No configuration found.");
			return;
		}

		try {
			final List<String> paths = new ArrayList<String>();
			final Path dir = Paths.get(indexConfigPath);
			if (Files.isDirectory(dir)) {
				// Multiple config files in a directory
				Files.list(dir).forEach((path) -> {
					if(Files.exists(path) && !Files.isDirectory(path))
						paths.add(path.toString());
				});
			}
			else {
				// A single config file
				if(Files.exists(dir))
					paths.add(indexConfigPath);
			}

			for (String path : paths) {
				this.load(path);
			}
		}
		catch (Exception exception) {
			log.log(Level.SEVERE,"Loading imagename-index config failed! Cause", exception);
			throw new BWFLAException(exception);
		}
	}

	/** Returns index entry for specified name. */
	public Entry get(String name)
	{
		return this.get(name, null);
	}

	/** Returns index entry for specified name and version. */
	public Entry get(String name, String version)
	{
		return entries.get(ImageNameIndex.toIndexKey(name, version));
	}


	public static class Entry
	{
		private final String name;
		private final String version;
		private final ImageDescription image;

		private Entry(String name, String version, ImageDescription image)
		{
			this.name = name;
			this.version = version;
			this.image = image;
		}

		private Entry(Configuration values, Configuration defaults)
		{
			this.name = ImageNameIndex.getOrDefault("name", values, defaults, true);
			this.version = ImageNameIndex.getOrDefault("version", values, defaults);
			this.image = new ImageDescription("image.", values, defaults);
		}

		public String name()
		{
			return name;
		}

		public String version()
		{
			return version;
		}

		public ImageDescription image()
		{
			return image;
		}
	}

	public static class ImageDescription
	{
		private final String url;
		private final String id;
		private final String type;
		private final String fstype;

		private ImageDescription(String url, String id, String type, String fstype)
		{
			this.url = url;
			this.id = id;
			this.type = type;
			this.fstype = fstype;
		}

		private ImageDescription(String prefix, Configuration values, Configuration defaults)
		{
			this.url = ImageNameIndex.getOrDefault(prefix + "url", values, defaults);
			this.id = ImageNameIndex.getOrDefault(prefix + "id", values, defaults, true);
			this.type = ImageNameIndex.getOrDefault(prefix + "type", values, defaults, true);
			this.fstype = ImageNameIndex.getOrDefault(prefix + "fstype", values, defaults);
		}

		public String url()
		{
			return url;
		}

		public String id()
		{
			return id;
		}

		public String type()
		{
			return type;
		}

		public String fstype()
		{
			return fstype;
		}
	}


	/* =============== Internal Helpers =============== */

	private void load(String indexConfigPath) throws MalformedURLException
	{
		log.info("Loading imagename-index config from '" + indexConfigPath + "'...");

		final ConfigurationContext context = ConfigurationProvider.getConfigurationContextBuilder()
				.addPropertySources(new ImageNameIndex.ConfigPropertySourceProvider(indexConfigPath).getPropertySources())
				.addDefaultPropertyConverters()
				.build();

		final Configuration config = ConfigurationProvider.createConfiguration(context);
		final Configuration defaults = ConfigHelpers.filter(config, "defaults.");
		while (true) {
			// Parse next entry...
			final String prefix = ConfigHelpers.toListKey("entries", entries.size(), ".");
			final Configuration subconfig = ConfigHelpers.filter(config, prefix);
			if (ConfigHelpers.isEmpty(subconfig))
				break;  // No more entries found!

			final Entry entry = new Entry(subconfig, defaults);
			final String key = ImageNameIndex.toIndexKey(entry.name(), entry.version());
			entries.put(key, entry);
		}

		log.info("" + entries.size() + " image aliases loaded");
	}

	private static String getOrDefault(String name, Configuration values, Configuration defaults)
	{
		final String value = values.get(name);
		return (value != null) ? value : defaults.get(name);
	}

	private static String getOrDefault(String name, Configuration values, Configuration defaults, boolean required)
			throws IllegalStateException
	{
		final String value = ImageNameIndex.getOrDefault(name, values, defaults);
		if (required && (value == null || value.isEmpty()))
			throw new IllegalStateException("Value for field '" + name + "' is missing! No defaults specified.");

		return value;
	}

	private static String toIndexKey(String name, String version)
	{
		if (version == null || version.isEmpty())
			version = "*";

		return name + VERSION_SEPARATOR + version;
	}

	private static class ConfigPropertySourceProvider extends BaseConfigurationPropertySourceProvider
	{
		public ConfigPropertySourceProvider(String path) throws MalformedURLException
		{
			this(Paths.get(path));
		}

		public ConfigPropertySourceProvider(Path path) throws MalformedURLException
		{
			super(path.toUri().toURL());
		}

		@Override
		public int getDefaultOrdinal()
		{
			return 500;
		}
	}
}
