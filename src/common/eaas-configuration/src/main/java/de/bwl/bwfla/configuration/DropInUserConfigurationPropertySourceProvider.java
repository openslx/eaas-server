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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class DropInUserConfigurationPropertySourceProvider extends BaseConfigurationPropertySourceProvider
{
	private static final Path[] CONFIG_DIRECTORIES = {
			UserConfigurationPropertySourceProvider.getConfigPath()
					.resolve("eaas-config.d")
	};

	protected static final Logger LOG = Logger.getLogger(DropInUserConfigurationPropertySourceProvider.class.getName());

	public static final int DEFAULT_ORDINAL = 700;


	public DropInUserConfigurationPropertySourceProvider()
	{
        super(DropInUserConfigurationPropertySourceProvider.getConfigLocations());
	}

	private static URL[] getConfigLocations()
	{
		final ArrayList<URL> locations = new ArrayList<>();
		for (Path dir : CONFIG_DIRECTORIES) {
			if (!Files.exists(dir))
				continue;

			try (final Stream<Path> files = Files.list(dir)) {
				files.filter(DropInUserConfigurationPropertySourceProvider::isYamlFile)
						.map(DropInUserConfigurationPropertySourceProvider::toURL)
						.forEach(locations::add);
			}
			catch (Exception error) {
				LOG.log(Level.WARNING, "Processing drop-in configuration directory failed!", error);
			}
		}

		return locations.toArray(new URL[0]);
	}

	private static URL toURL(Path path)
	{
		try {
			LOG.info("Found drop-in configuration file at '" + path.toString() + "'");
			return path.toUri().toURL();
		}
		catch (MalformedURLException exception) {
			// This should never happen!
			throw new RuntimeException("Malformed drop-in configuration file name: " + path.toString());
		}
	}

	private static boolean isYamlFile(Path path)
	{
		final String filename = path.getFileName().toString();
		return filename.endsWith(".yaml") || filename.endsWith(".yml");
	}

    @Override
    public int getDefaultOrdinal() {
        return DEFAULT_ORDINAL;
    }
}
