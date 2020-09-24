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

package de.bwl.bwfla.imagearchive.generalization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


@Startup
@Singleton
public class ImageGeneralizationPatches
{
	private final Logger log = Logger.getLogger("GENERALIZATION-PATCHES");

	private Map<String, ImageGeneralizationPatch> patches;


	public ImageGeneralizationPatch lookup(String name)
	{
		return patches.get(name);
	}

	public Collection<ImageGeneralizationPatch> list()
	{
		return patches.values();
	}


	// ===== Internal Helpers =========================

	@PostConstruct
	protected void initialize()
	{
		this.patches = new ConcurrentHashMap<>();

		this.load(ImageGeneralizationPatches.getBaseDir());
	}

	private static Path getBaseDir()
	{
		final String dir = ConfigurationProvider.getConfiguration()
				.get("imagearchive.generalization_patches_dir");

		return Paths.get(dir);
	}

	private void load(Path basedir)
	{
		if (!Files.exists(basedir)) {
			log.warning("Base directory does not exist! Skip loading patches...");
			return;
		}

		log.info("Loading patches...");

		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		try (final DirectoryStream<Path> files = Files.newDirectoryStream(basedir)) {
			for (Path path : files) {
				if (!Files.isDirectory(path)) {
					log.warning("Skipping file: " + path.toString());
					continue;
				}

				log.info("Loading: " + path.toString());
				try {
					final Path source = path.resolve("description.yaml");
					final ImageGeneralizationPatch patch = mapper.readValue(source.toFile(), ImageGeneralizationPatch.class)
							.setLocationDir(path);

					this.register(patch);
				}
				catch (Exception error) {
					log.log(Level.WARNING, "Loading patch '" + path.toString() + "' failed!", error);
				}
			}
		}
		catch (Exception error) {
			throw new RuntimeException("Loading patches failed!", error);
		}

		log.info(patches.size() + " patches loaded");
	}

	private void register(ImageGeneralizationPatch patch)
	{
		final String name = patch.getName();
		final ImageGeneralizationPatch old = patches.put(name, patch);
		if (old != null) {
			log.warning("Patch with name '" + name + "' already exists! Replacing...");
			log.warning("Old patch: " + old.getLocationDir());
			log.warning("New patch: " + patch.getLocationDir());
		}

		patches.put(name, patch);
	}
}
