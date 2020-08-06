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

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.cache.template.NoOpTemplateCache;
import com.mitchellbosecke.pebble.loader.StringLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.MediumType;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

		log.info("Loading templates...");
		try {
			CompiledTemplate template = new TemplateDescription()
					.setRawContent("start \"\" \"%~dp0/{{ filename }}\"")
					.setOperatingSystemIds(Arrays.asList("Q11248", "Q6072277"))
					.setMediumType(MediumType.CDROM)
					.setFileName("uvi.bat")
					.compile(engine);

			this.register(template);

			template = new TemplateDescription()
					.setRawContent("[autorun]\r\nopen=start \"{{ filename }}\"")
					.setOperatingSystemIds(Arrays.asList("UNKNOWN"))
					.setMediumType(MediumType.CDROM)
					.setFileName("autorun.inf")
					.compile(engine);

			this.register(template);
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

	private AutoRunScripts register(CompiledTemplate template)
	{
		for (String id : template.getOperatingSystemIds()) {
			String key = AutoRunScripts.toLookupKey(id, template.getMediumType());
			templates.put(key, template);
		}

		return this;
	}

	private static class CompiledTemplate implements Template
	{
		private final PebbleTemplate template;
		private final Collection<String> osids;
		private final MediumType medium;
		private final String filename;


		public CompiledTemplate(PebbleTemplate template, String filename, Collection<String> osids, MediumType medium)
		{
			this.template = template;
			this.filename = filename;
			this.medium = medium;
			this.osids = osids;
		}

		// ========== Template Implementation ====================

		@Override
		public CompiledTemplate evaluate(Writer writer, Map<String, Object> context) throws IOException
		{
			template.evaluate(writer, context);
			return this;
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
	}

	@XmlRootElement
	private static class TemplateDescription
	{
		@XmlElement(name = "os_ids")
		private List<String> osids;

		@XmlElement(name = "medium_type")
		private MediumType medium;

		@XmlElement(name = "filename")
		private String filename;

		@XmlElement(name = "template")
		private String content;


		public TemplateDescription setOperatingSystemIds(List<String> ids)
		{
			this.osids = ids;
			return this;
		}

		public List<String> getOperatingSystemIds()
		{
			return osids;
		}

		public TemplateDescription setMediumType(MediumType medium)
		{
			this.medium = medium;
			return this;
		}

		public MediumType getMediumType()
		{
			return medium;
		}

		public TemplateDescription setFileName(String filename)
		{
			this.filename = filename;
			return this;
		}

		public String getFileName()
		{
			return filename;
		}

		public TemplateDescription setRawContent(String content)
		{
			this.content = content;
			return this;
		}

		public String getRawContent()
		{
			return content;
		}

		public CompiledTemplate compile(PebbleEngine engine) throws BWFLAException
		{
			try {
				final PebbleTemplate template = engine.getTemplate(content);
				return new CompiledTemplate(template, filename, osids, medium);
			}
			catch (Exception error) {
				throw new BWFLAException("Compiling template '" + filename + "' failed!", error);
			}
		}
	}
}
