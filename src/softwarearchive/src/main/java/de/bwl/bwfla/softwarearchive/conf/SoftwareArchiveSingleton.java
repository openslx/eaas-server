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

package de.bwl.bwfla.softwarearchive.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.softwarearchive.ISoftwareArchive;
import de.bwl.bwfla.softwarearchive.SoftwareArchiveFactory;
import de.bwl.bwfla.softwarearchive.impl.SoftwareFileArchive;


@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SoftwareArchiveSingleton
{
	protected static final Logger LOG = Logger.getLogger(SoftwareArchiveSingleton.class.getName());
	private static ISoftwareArchive ARCHIVE;

	@Inject
	@Config(value = "commonconf.serverdatadir")
	protected String serverdatadir;


	public String baseDir;
	
	@PostConstruct
	public void init()
	{
		baseDir = new File(serverdatadir, "software-archive").getAbsolutePath();
		Path base = Paths.get(baseDir);
		if(!Files.exists(base))
			try {
				Files.createDirectory(base);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}


		List<ISoftwareArchive> archives = SoftwareArchiveFactory.createAllFromJson(baseDir);
		if (!archives.isEmpty()) {
			// NOTE: currently only one software archive is supported!
			ARCHIVE = archives.get(0);
			LOG.info("Adding software archive: " + ARCHIVE.getName());
		}
		else {
			ARCHIVE = new SoftwareFileArchive("default", baseDir);
			LOG.info("creating default software archive in " + baseDir);
		}

	}
	
	public static ISoftwareArchive getArchiveInstance()
	{
		return ARCHIVE;
	}
}
