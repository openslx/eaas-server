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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import de.bwl.bwfla.imagearchive.conf.ImageArchiveBackendConfig;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveConfig;


@ApplicationScoped
public class ImageArchiveRegistry
{
	private final Logger log = Logger.getLogger(this.getClass().getName());
	private final Map<String, ImageArchiveBackend> backends = new HashMap<>();
	private final ImageArchiveConfig config = new ImageArchiveConfig();


	public ImageArchiveBackend lookup(String name)
	{
		return backends.get(name);
	}

	public ImageArchiveBackend getDefaultBackend()
	{
		return this.lookup(config.getDefaultBackendName());
	}

	public ImageArchiveConfig getImageArchiveConfig()
	{
		return config;
	}


	/* ==================== Internal Helpers ==================== */

	/** Constructor for CDI */
	protected ImageArchiveRegistry()
	{
		// Empty!
	}

	@PostConstruct
	protected void initialize()
	{
		config.load();

		final List<ImageArchiveBackendConfig> backendConfigs = config.getBackendConfigs();
		log.info("Initializing image-archive(s)...");

		// Initialize the image-archives...
		for (ImageArchiveBackendConfig iac : backendConfigs) {
			try {
				final String name = iac.getName();
				log.info("Initializing image-archive '" + name + "'...");
				final ImageArchiveBackend backend = new ImageArchiveBackend(iac);
				backend.reload();
				backends.put(name, backend);
				log.info("Image-archive '" + name + "' (" + iac.getType() + ") initialized");
			}
			catch (Exception exception) {
				log.log(Level.WARNING, "Initializing image-archive '" + iac.getName() + "' failed!\n", exception);
			}
		}

		log.info("Initialized " + backendConfigs.size() + " image-archive(s)");
	}
}