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

package de.bwl.bwfla.imagearchive.conf;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.handle.HandleUtils;
import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata.ImageType;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import java.io.File;
import java.util.logging.Logger;


public class ImageArchiveBackendConfig extends BaseConfig
{
	private String name;
	private String type;
	private String httpPrefix;
	private String handlePrefix;
	private String nameIndexConfigPath;

	private File imagePath;
	private File metaDataPath;
	private File recordingsPath;
	private File defaultEnvironmentsPath;


	/* ========== Getters and Setters ========== */

	public String getName()
	{
		return name;
	}

	@Config("name")
	public void setName(String name)
	{
		ConfigHelpers.check(name, "Name is invalid!");
		this.name = name;
	}

	public String getType()
	{
		return type;
	}

	@Config("type")
	public void setType(String type)
	{
		ConfigHelpers.check(type, "Type is invalid!");
		this.type = type;
	}

	public String getHttpPrefix()
	{
		return httpPrefix;
	}

	@Config("http_prefix")
	public void setHttpPrefix(String prefix)
	{
		ConfigHelpers.check(prefix, "HTTP prefix is invalid!");
		this.httpPrefix = prefix;
	}

	public String getHandlePrefix()
	{
		return handlePrefix;
	}

	public void setHandlePrefix(String prefix)
	{
		ConfigHelpers.check(prefix, "Handle.net prefix is invalid!" );
		this.handlePrefix = prefix;
	}

	public boolean isHandleConfigured()
	{
		return (handlePrefix != null);
	}

	public String getNameIndexConfigPath()
	{
		return nameIndexConfigPath;
	}

	@Config("nameindex_config_path")
	public void setNameIndexConfigPath(String path)
	{
		ConfigHelpers.check(path, "NameIndex config path is invalid!");
		this.nameIndexConfigPath = path;
	}

	@Config("basepath")
	public void setBasePath(String base) throws BWFLAException
	{
		ConfigHelpers.check(base, "Base path is invalid!");

		final Logger log = Logger.getLogger(this.getClass().getName());
		log.info("Preparing directories for image-archive '" + name + "'...");
		{
			this.imagePath = ImageArchiveBackendConfig.createDirectories(base, "images");
			for (ImageType t : ImageType.values())
				ImageArchiveBackendConfig.createDirectories(imagePath, t.name());

			this.metaDataPath = ImageArchiveBackendConfig.createDirectories(base,"meta-data");
			for (ImageType t : ImageType.values())
				ImageArchiveBackendConfig.createDirectories(metaDataPath, t.name());

			this.recordingsPath = ImageArchiveBackendConfig.createDirectories(base, "recordings");
			this.defaultEnvironmentsPath = new File(metaDataPath, "defaultEnvironments.properties");
		}
	}

	public File getImagePath()
	{
		return imagePath;
	}

	public File getMetaDataPath()
	{
		return metaDataPath;
	}

	public File getRecordingsPath()
	{
		return recordingsPath;
	}

	public File getDefaultEnvironmentsPath()
	{
		return defaultEnvironmentsPath;
	}


	/* ========== Initialization ========== */

	public void load(Configuration config)
	{
		this.setName(config.get("name"));
		if (config.get("export_handles", Boolean.class))
			this.setHandlePrefix(HandleUtils.getHandlePrefix());

		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);
	}


	/* ========== Internal Helpers ========== */

	private static File createDirectories(String base, String subdir) throws BWFLAException
	{
		return ImageArchiveBackendConfig.createDirectories(new File(base), subdir);
	}

	private static File createDirectories(File base, String subdir) throws BWFLAException
	{
		final File dir = new File(base, subdir);
		if (!dir.exists() && !dir.mkdirs())
			throw new BWFLAException("Creating directory failed: " + dir.toString());

		return dir;
	}
}
