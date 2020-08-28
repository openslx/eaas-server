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

package de.bwl.bwfla.emil.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.cache.template.NoOpTemplateCache;
import com.mitchellbosecke.pebble.loader.StringLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.MediumType;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class AutoRunScripts
{
	private final Logger log = Logger.getLogger("AUTORUN-SCRIPTS");

	private Map<String, CompiledTemplate> templates;
	private PebbleEngine engine;

	public static class Variables
	{
		public static final String FILENAME = "filename";
	}

	public Template lookup(String osid, MediumType medium)
	{
		String key = AutoRunScripts.toLookupKey(osid, medium);
		return templates.get(key);
	}


	public interface Template
	{
		Template evaluate(Writer writer, Map<String, Object> context) throws IOException;
		byte[] evaluate(Map<String, Object> context) throws IOException;
		Charset getTargetEncoding();
		String getFileName();
	}


	// ========== Internal Helpers ====================

	@PostConstruct
	protected void initialize()
	{
		this.templates = new ConcurrentHashMap<>();

		// Initialize Pebble with disabled cache (using NoOpTemplateCache).
		// NOTE: using StringLoader has the effect, that template's name is
		//       interpreted as the template's content by Pebble!
		this.engine = new PebbleEngine.Builder()
				.templateCache(new NoOpTemplateCache())
				.loader(new StringLoader())
				.build();

		this.load(AutoRunScripts.getTemplatesDir());
	}

	private static Path getTemplatesDir()
	{
		final String dir = ConfigurationProvider.getConfiguration()
				.get("emil.autorun_scripts_dir");

		return Paths.get(dir);
	}

	private void load(Path basedir)
	{
		if (!Files.exists(basedir)) {
			log.warning("Templates directory does not exist! Skip loading templates...");
			return;
		}

		log.info("Loading templates...");

		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		try (final DirectoryStream<Path> files = Files.newDirectoryStream(basedir)) {
			for (Path path : files) {
				log.info("Loading: " + path.toString());
				try {
					final String content = Files.readString(path, StandardCharsets.UTF_8);
					final CompiledTemplate template = mapper.readValue(content, TemplateDescription.class)
							.compile(engine, path);

					this.register(template);
				}
				catch (Exception error) {
					log.log(Level.WARNING, "Loading template '" + path.toString() + "' failed!", error);
				}
			}
		}
		catch (Exception error) {
			throw new RuntimeException("Loading templates failed!", error);
		}

		log.info(templates.size() + " template(s) loaded");
	}

	private static String toLookupKey(String osid, MediumType medium)
	{
		return osid + "/" + medium.name();
	}

	private void register(CompiledTemplate template)
	{
		for (String id : template.getOperatingSystemIds()) {
			String key = AutoRunScripts.toLookupKey(id, template.getMediumType());
			CompiledTemplate old = templates.put(key, template);
			if (old != null) {
				log.warning("Template for '" + key + "' already exists! Replacing...");
				log.warning("Old template: " + old.getSourcePath());
				log.warning("New template: " + template.getSourcePath());
			}
		}
	}

	private static class CompiledTemplate implements Template
	{
		private final PebbleTemplate template;
		private final Collection<String> osids;
		private final MediumType medium;
		private final Charset encoding;
		private final String filename;
		private final Path path;


		public CompiledTemplate(PebbleTemplate template, Charset encoding, Path path, String filename, Collection<String> osids, MediumType medium)
		{
			this.template = template;
			this.encoding = encoding;
			this.filename = filename;
			this.medium = medium;
			this.osids = osids;
			this.path = path;
		}

		// ========== Template Implementation ====================

		@Override
		public CompiledTemplate evaluate(Writer writer, Map<String, Object> context) throws IOException
		{
			template.evaluate(writer, context);
			return this;
		}

		@Override
		public byte[] evaluate(Map<String, Object> context) throws IOException
		{
			final Writer writer = new StringWriter();
			this.evaluate(writer, context);
			return writer.toString()
					.getBytes(encoding);
		}

		@Override
		public Charset getTargetEncoding()
		{
			return encoding;
		}

		@Override
		public String getFileName()
		{
			return filename;
		}


		// ========== Internal ====================

		private Collection<String> getOperatingSystemIds()
		{
			return osids;
		}

		private MediumType getMediumType()
		{
			return medium;
		}

		private Path getSourcePath()
		{
			return path;
		}
	}

	private static class TemplateDescription
	{
		private List<String> osids;
		private MediumType medium;
		private String filename;
		private Charset encoding;
		private String content;


		public TemplateDescription setOperatingSystemIds(List<String> ids)
		{
			this.osids = ids;
			return this;
		}

		@JsonProperty("os_ids")
		public List<String> getOperatingSystemIds()
		{
			return osids;
		}

		public TemplateDescription setMediumType(MediumType medium)
		{
			this.medium = medium;
			return this;
		}

		@JsonSetter
		public TemplateDescription setMediumType(String medium)
		{
			this.medium = MediumType.fromString(medium);
			return this;
		}

		@JsonProperty("medium_type")
		public MediumType getMediumType()
		{
			return medium;
		}

		public TemplateDescription setFileName(String filename)
		{
			this.filename = filename;
			return this;
		}

		@JsonProperty("filename")
		public String getFileName()
		{
			return filename;
		}

		public TemplateDescription setTargetEncoding(Charset encoding)
		{
			this.encoding = encoding;
			return this;
		}

		@JsonSetter
		public TemplateDescription setTargetEncoding(String encoding)
		{
			return this.setTargetEncoding(Charset.forName(encoding));
		}

		@JsonProperty("encoding")
		public Charset getTargetEncoding()
		{
			return encoding;
		}

		public TemplateDescription setRawContent(String content)
		{
			this.content = content;
			return this;
		}

		@JsonProperty("template")
		public String getRawContent()
		{
			return content;
		}

		public CompiledTemplate compile(PebbleEngine engine, Path path) throws BWFLAException
		{
			try {
				final PebbleTemplate template = engine.getTemplate(content);
				return new CompiledTemplate(template, encoding, path, filename, osids, medium);
			}
			catch (Exception error) {
				throw new BWFLAException("Compiling template '" + path.toString() + "' failed!", error);
			}
		}
	}
}
