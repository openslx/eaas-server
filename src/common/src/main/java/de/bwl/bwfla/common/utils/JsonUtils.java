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

package de.bwl.bwfla.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;


public class JsonUtils
{
	/** Suffix added to backup-files */
	private static final String BACKUP_SUFFIX  = ".backup";


	public static <T> void store(Path statepath, T data, Logger log) throws Exception
	{
		// First, backup previous state file
		if (Files.exists(statepath)) {
			final Path backup = JsonUtils.toBackupPath(statepath);
			log.info("Backing up previous state as: " + backup.toString());
			try {
				Files.copy(statepath, backup, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Backing up previous state failed!", error);
			}
		}

		// Try to save new state...
		try (final Writer writer = Files.newBufferedWriter(statepath)) {
			final ObjectMapper mapper = new ObjectMapper()
					.enable(SerializationFeature.INDENT_OUTPUT);

			mapper.writeValue(writer, data);
		}
	}

	public static <T> T restore(Path statepath, TypeReference<T> typeref, Logger log) throws Exception
	{
		try {
			return JsonUtils.restore(statepath, typeref);
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Restoring current state failed!", error);
		}

		final Path backup = JsonUtils.toBackupPath(statepath);
		log.info("Trying to restore state from backup: " + backup.toString());
		return JsonUtils.restore(backup, typeref);
	}


	// ========== Internal Helpers ====================

	private static <T> T restore(Path statepath, TypeReference<T> typeref) throws Exception
	{
		try (final Reader input = Files.newBufferedReader(statepath)) {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(input, typeref);
		}
	}

	public static Path toBackupPath(Path path)
	{
		final String backup = path.getFileName() + BACKUP_SUFFIX;
		return path.getParent()
				.resolve(backup);
	}
}
