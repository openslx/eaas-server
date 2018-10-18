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

package de.bwl.bwfla.softwarearchive;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import de.bwl.bwfla.softwarearchive.impl.SoftwareFileArchive;


public class SoftwareArchiveFactory
{
	protected final static Logger LOG = Logger.getLogger(SoftwareArchiveFactory.class.getName());

	private static ISoftwareArchive parseJson(Path input) throws JsonIOException, JsonSyntaxException, IOException
	{
		JsonObject object = null;
		
		try (Reader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
			JsonParser parser = new JsonParser();
			JsonElement root = parser.parse(reader);
			if (!root.isJsonObject()) {
				LOG.warning("Parsed software archive's description is not a JSON object!");
				return null;
			}
			
			object = root.getAsJsonObject();
		}

		final JsonElement type = object.get("type");
		if (type == null || !type.getAsString().equalsIgnoreCase("FILE")) {
			LOG.warning("Not supported software archive type: " + type);
			return null;
		}

		final JsonElement name = object.get("name");
		final JsonElement path = object.get("localPath");
		return new SoftwareFileArchive(name.getAsString(), path.getAsString());
	}
	
	public static ISoftwareArchive createFromJson(Path path)
	{
		if (!Files.exists(path) || Files.isDirectory(path)) {
			LOG.severe("Invalid path for software archive specified: " + path.toString());
			return null;
		}
		
		ISoftwareArchive archive = null;
		try {
			archive = SoftwareArchiveFactory.parseJson(path);
		} catch (Exception exception) {
			LOG.warning("Parsing software archive's description failed: " + path);
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
		}
		
		return archive;
	}
	
	public static List<ISoftwareArchive> createAllFromJson(String basedir)
	{
		return SoftwareArchiveFactory.createAllFromJson(Paths.get(basedir));
	}
	
	public static List<ISoftwareArchive> createAllFromJson(Path basedir)
	{
		List<ISoftwareArchive> archives = new ArrayList<ISoftwareArchive>();
		if (!Files.exists(basedir) || !Files.isDirectory(basedir)) {
			LOG.severe("Path is not a directory containing software archives: " + basedir.toString());
			return archives;
		}
		
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			public boolean accept(Path p)
			{
				if (Files.isDirectory(p))
					return false;
				
				String name = p.getFileName().toString();
				return name.endsWith(".json");
			}
		};

		try (DirectoryStream<Path> files = Files.newDirectoryStream(basedir, filter)) {
			// Parse all JSON files...
			for (Path json : files) {
				try {
					ISoftwareArchive archive = SoftwareArchiveFactory.parseJson(json);
					if (archive != null)
						archives.add(archive);
				}
				catch (Exception exception) {
					LOG.warning("Parsing software archive's description failed: " + json);
					LOG.log(Level.WARNING, exception.getMessage(), exception);
				}
			}
		} catch (Exception exception) {
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
		}

		return archives;
	}
	
	private SoftwareArchiveFactory() { }
}
