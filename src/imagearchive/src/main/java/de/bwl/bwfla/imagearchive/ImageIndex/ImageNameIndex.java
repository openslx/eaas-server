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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/** A simple index, containing mappings from symbolic names to imageDescription descriptions. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageNameIndex extends JaxbType
{
	private final Logger log;

	private String configPath;

	private final SequentialExecutor executor;

	/** Symbolic imageDescription-name index: name --> imageDescription's description */
	@XmlElement
	private final Map<String, ImageMetadata> entries = new ConcurrentHashMap<>();
	@XmlElement
	private final Map<String, Alias> aliases = new ConcurrentHashMap<>();

	private static final String VERSION_SEPARATOR = "|";
	private static final String LATEST_TAG = "latest";

	private ImageNameIndex() throws BWFLAException
	{
		this.log = new PrefixLogger(ImageArchiveBackend.class.getName());
		this.executor = ImageNameIndex.lookup(log);
		this.configPath = null;
	}

	public ImageNameIndex(String path)
	{
		this.log = new PrefixLogger(ImageArchiveBackend.class.getName());
		this.executor = ImageNameIndex.lookup(log);
		this.configPath = path;
	}

	public static ImageNameIndex parse(String path) throws BWFLAException
	{
		try {
			String content = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
			ImageNameIndex index = ImageNameIndex.fromYamlValue(content, ImageNameIndex.class);
			index.configPath = path;
			return index;
		} catch (IOException e) {
			throw new BWFLAException(e);
		}
	}

	/** Returns index entry for specified name. */
	public ImageMetadata get(String name)
	{
		return this.get(name, null);
	}

	/** Returns index entry for specified name and version. */
	public ImageMetadata get(String name, String version) {
		System.out.println("looking for " + ImageNameIndex.toIndexKey(name, version));
		ImageMetadata entry = entries.get(ImageNameIndex.toIndexKey(name, version));
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

	public void delete(String name) {
		delete(name, null);
	}

	public void delete(String name, String version)
	{
		ImageMetadata md = entries.remove(ImageNameIndex.toIndexKey(name, version));
		if(md == null)
			log.severe("failed to find key: " + ImageNameIndex.toIndexKey(name, version));
		executor.execute(this::dump);
	}

	/* =============== Internal Helpers =============== */

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

	public Map<String, ImageMetadata> getEntries() {
		return entries;
	}

	public Map<String, Alias> getAliases() {
		return aliases;
	}

	public synchronized void addNameIndexesEntry(ImageMetadata entry, Alias alias) {
		this.entries.put(ImageNameIndex.toIndexKey(entry.getName(), entry.getVersion()), entry);

		if(alias != null)
			this.aliases.put(ImageNameIndex.toIndexKey(alias.getName(), alias.getAlias()), alias);

		executor.execute(this::dump);
		if(get(entry.getName()) == null)
		{
			updateLatest(entry.getName(), entry.getVersion());
		}
	}

    public synchronized void updateLatest(String emulator, String version) {
        log.info("\nLatest (default) emulator update!\nemulator: " + emulator + "\nversion: " + version);
        this.aliases.put(ImageNameIndex.toIndexKey(emulator, LATEST_TAG), new Alias(emulator, version, LATEST_TAG));
        executor.execute(this::dump);
    }


	private void dump()
	{
		Path outPath = Paths.get(configPath);
		if (Files.isDirectory(outPath))
			outPath = outPath.resolve("index.yaml");

		try (final BufferedWriter writer = Files.newBufferedWriter(outPath)) {
			writer.write(this.yamlValue(false));
		}
		catch (IOException error) {
			throw new IllegalStateException("NameIndexes dump failed!", error);
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
