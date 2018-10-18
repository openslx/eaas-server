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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tamaya.core.internal.DefaultServiceContext;
import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationFormat;
import org.apache.tamaya.format.ConfigurationFormats;
import org.apache.tamaya.format.MappedConfigurationDataPropertySource;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;


// Code based on org.apache.tamaya.format.BaseFormatPropertySourceProvider

public abstract class BaseConfigurationPropertySourceProvider implements PropertySourceProvider
{
	protected final Logger LOG = Logger.getLogger(BaseConfigurationPropertySourceProvider.class.getName());

	/** The paths to be evaluated for configuration files. */
	private final List<URL> paths;
	
	public BaseConfigurationPropertySourceProvider(URL... urls)
	{
		this.paths = new ArrayList<URL>();

		for (URL url : urls) {
		    if (url != null) {
		        paths.add(url);
		    }
		}
	}

	public BaseConfigurationPropertySourceProvider(String... paths)
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null)
			loader = this.getClass().getClassLoader();
		
		this.paths = toUrls(loader, paths);
	}

	public BaseConfigurationPropertySourceProvider(ClassLoader loader, String... paths)
	{
		this.paths = toUrls(loader, paths);
	}
	
	public abstract int getDefaultOrdinal();

	@Override
	public Collection<PropertySource> getPropertySources()
	{
		List<PropertySource> sources = new ArrayList<PropertySource>();
		for (URL url : paths) {
		    PropertySource source = null;
			
			// Try to parse the config using available format implementations...
            List<ConfigurationFormat> formats = ConfigurationFormats.getFormats(url);
            formats.sort((ConfigurationFormat o1, ConfigurationFormat o2) -> {
                // this sorts the list in reverse natural order, i.e.
                // highest priority first
                return DefaultServiceContext.getPriority(o2) - DefaultServiceContext.getPriority(o1);
            });
			for (ConfigurationFormat format : formats) {
				try (InputStream is = url.openStream()) {
					ConfigurationData data = format.readConfiguration(url.toString(), is);
					source = new MappedConfigurationDataPropertySource(getDefaultOrdinal(), data);
					break;
				}
				catch (Exception exception) {
					String message = "Parser '" + format.getClass().getName()
							+ "' failed to parse config resource: " + url;
					
					LOG.log(Level.WARNING, message, exception);
				}
			}
			
			if (source == null)
				LOG.warning("Failed to parse config resource: " + url);
			sources.add(source);
		}
		
		return sources;
	}
	
	private List<URL> toUrls(ClassLoader loader, String... paths)
	{
		List<URL> urls = new ArrayList<URL>();
		
		for (String path : paths) {
			try {
				Enumeration<URL> resources = loader.getResources(path);
				while (resources.hasMoreElements())
					urls.add(resources.nextElement());
			}
			catch (IOException exception) {
			    LOG.log(Level.WARNING, "Failed to read resource: " + path, exception);
				continue;
			}
		}
		
		return urls;
	}
}
