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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;


public class UserConfigurationPropertySourceProvider extends BaseConfigurationPropertySourceProvider
{
	private static final String[] CONFIG_FILENAMES = {
			"eaas-config.yaml",
			"eaas-resource-provider-selection.yaml"
	};

	public static final int DEFAULT_ORDINAL = 600;
	protected static final Logger LOG = Logger.getLogger(UserConfigurationPropertySourceProvider.class.getName());
	
	public UserConfigurationPropertySourceProvider()
	{
        super(getConfigLocation());
	}

	public static Path getConfigPath()
	{
		// return Paths.get(System.getProperty("user.home"), ".bwFLA");
		return Paths.get("/eaas", "config");
	}

	private static URL[] getConfigLocation() 
	{
		final Path basedir =  Paths.get("/eaas", "config"); //Paths.get(System.getProperty("user.home"), ".bwFLA");
		final ArrayList<URL> locations = new ArrayList<URL>(CONFIG_FILENAMES.length);
		for (String filename : CONFIG_FILENAMES) {
			final Path path = basedir.resolve(filename);
			final URL url = UserConfigurationPropertySourceProvider.toURL(path);
			if (url != null)
				locations.add(url);
		}

		final URL[] urls = new URL[locations.size()];
		for (int i = 0; i < locations.size(); ++i)
			urls[i] = locations.get(i);

		return urls;
	}

	private static URL toURL(Path path)
	{
		if (!Files.exists(path)) {
			LOG.warning("No user-configuration file found at '" + path.toString() + "'! Using defaults.");
			return null;
		}

		try {
			LOG.info("Found user-configuration file at '" + path.toString() + "'");
			return path.toUri().toURL();
		}
		catch (MalformedURLException exception) {
			// This should never happen!
			throw new RuntimeException("Malformed user-configuration file name: " + path.toString());
		}
	}

    @Override
    public int getDefaultOrdinal() {
        return DEFAULT_ORDINAL;
    }
}
