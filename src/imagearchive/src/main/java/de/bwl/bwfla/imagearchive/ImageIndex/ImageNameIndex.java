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

package de.bwl.bwfla.imagearchive.ImageIndex;

import de.bwl.bwfla.common.concurrent.SequentialExecutor;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.configuration.BaseConfigurationPropertySourceProvider;
import de.bwl.bwfla.imagearchive.ImageArchiveBackend;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.spi.ConfigurationContext;

import javax.naming.InitialContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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


/** A simple index, containing mappings from symbolic names to imageDescription descriptions. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageNameIndex extends JaxbType
{
	private final Logger log;

	final static String configPath = "/home/bwfla/server-data/nameindexes.dump";

	private final SequentialExecutor executor;

	/** Symbolic imageDescription-name index: name --> imageDescription's description */
	@XmlElement
	private final Map<String, Entry> entries = new HashMap<String, Entry>();
	@XmlElement
	private final Map<String, Alias> aliases = new HashMap<String, Alias>();

	private static final String VERSION_SEPARATOR = "|";
	private static final String LATEST_TAG = "latest";

	public ImageNameIndex(String indexConfigPath, Logger log) throws BWFLAException
	{
		this.log = log;
		this.executor = ImageNameIndex.lookup(log);

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

	public ImageNameIndex() throws BWFLAException
	{
		this.log = new PrefixLogger(ImageArchiveBackend.class.getName());
		this.executor = ImageNameIndex.lookup(log);
	}

	/** Returns index entry for specified name. */
	public Entry get(String name)
	{
		return this.get(name, null);
	}

	/** Returns index entry for specified name and version. */
	public Entry get(String name, String version) {
		System.out.println("looking for " + ImageNameIndex.toIndexKey(name, version));
		Entry entry = entries.get(ImageNameIndex.toIndexKey(name, version));
		if (entry != null)
			return entry;
		else {
			Alias alias = aliases.get(ImageNameIndex.toIndexKey(name, version));
			if (alias == null) {
				log.warning("Emulator is not found! " + name + " " + version);
				return null;
			}
			return entries.get(ImageNameIndex.toIndexKey(name, alias.getVersion()));
		}
	}

	public static String getConfigPath() {
		return configPath;
	}
	/* =============== Internal Helpers =============== */

	private void load(String indexConfigPath) throws IOException {
		log.info("Loading imagename-index config from '" + indexConfigPath + "'...");

		final ConfigurationContext context = ConfigurationProvider.getConfigurationContextBuilder()
				.addPropertySources(new ImageNameIndex.ConfigPropertySourceProvider(indexConfigPath).getPropertySources())
				.addDefaultPropertyConverters()
				.build();

		final Configuration config = ConfigurationProvider.createConfiguration(context);

		// Load all entries
		{
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

			log.info("" + entries.size() + " imageDescription entries loaded");
		}

		// Load all aliases
		{
			while (true) {
				// Parse next entry...
				final String aliasPrefix = ConfigHelpers.toListKey("aliases", aliases.size(), ".");
				final Configuration subconfigAlias = ConfigHelpers.filter(config, aliasPrefix);
				if (ConfigHelpers.isEmpty(subconfigAlias))
					break;  // No more entries found!

				final Alias alias = new Alias(subconfigAlias);
				final String key = ImageNameIndex.toIndexKey(alias.getName(), alias.getAlias());
				aliases.put(key, alias);
			}

			log.info("" + entries.size() + " imageDescription aliases loaded");
		}
	}

	static String getOrDefault(String name, Configuration values, Configuration defaults)
	{
		final String value = values.get(name);
		return (value != null) ? value : defaults.get(name);
	}

	static String getOrDefault(String name, Configuration values, Configuration defaults, boolean required)
			throws IllegalStateException
	{
		final String value = ImageNameIndex.getOrDefault(name, values, defaults);
		if (required && (value == null || value.isEmpty()))
			throw new IllegalStateException("Value for field '" + name + "' is missing! No defaults specified.");

		return value;
	}

	public static String toIndexKey(String name, String version)
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

	public Map<String, Entry> getEntries() {
		return entries;
	}

	public Map<String, Alias> getAliases() {
		return aliases;
	}

	public void addNameIndexesEntry(Entry entry, Alias alias) {
		this.entries.put(ImageNameIndex.toIndexKey(entry.getName(), entry.getVersion()), entry);
		this.aliases.put(ImageNameIndex.toIndexKey(alias.getName(), alias.getAlias()), alias);
		executor.execute(this::dump);

		if(get(entry.getName()) == null)
		{
			updateLatest(entry.getName(), entry.getVersion());
		}
	}

    public void updateLatest(String emulator, String version) {
        log.info("\nLatest (default) emulator update!\nemulator: " + emulator + "\nversion: " + version);
        this.aliases.put(ImageNameIndex.toIndexKey(emulator, LATEST_TAG), new Alias(emulator, version, LATEST_TAG));
        executor.execute(this::dump);
    }


	private void dump()
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(configPath));
			writer.write( this.yamlValue(false));
			writer.close();
		} catch (IOException e) {
			throw new IllegalStateException("NameIndexes dump failed!");
		}
	}



	private static SequentialExecutor lookup(Logger log)
	{
		final String name = "java:jboss/ee/concurrency/executor/io";
		try {
			return new SequentialExecutor(InitialContext.doLookup(name),4);
		}
		catch (Exception exception) {
			log.log(Level.SEVERE, "Lookup for '" + name + "' failed!", exception);
			return null;
		}
	}
}
