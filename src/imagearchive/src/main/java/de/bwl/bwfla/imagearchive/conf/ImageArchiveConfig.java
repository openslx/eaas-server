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
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata.ImageType;

import java.io.File;

public class ImageArchiveConfig 
{
	public final File imagePath;
	public final File metaDataPath;
	public final File recordingsPath;
	
	public final String nbdPrefix;
	public final String httpPrefix;
	public final String handlePrefix;


	public ImageArchiveConfig(String base, String nbdExport, String httpExport, String handlePrefix) throws BWFLAException {
		imagePath = new File(base + "/images");
		if(!imagePath.exists()) {
			if(!imagePath.mkdirs())
				throw new BWFLAException("failed creating " + imagePath);
		}
		for(ImageType t : ImageType.values())
		{
			File subdir = new File(imagePath, t.name());
			if(subdir.exists())
				continue;
			if(!subdir.mkdirs())
				throw new BWFLAException("failed creating subdir " + subdir);
		}

		metaDataPath = new File(base + "/meta-data");

		if(!metaDataPath.exists()) {
			metaDataPath.mkdirs();
		}

		for(ImageType t : ImageType.values())
		{
			File subdir = new File(metaDataPath, t.name());
			if(subdir.exists())
				continue;
			if(!subdir.mkdirs())
				throw new BWFLAException("failed creating subdir " + subdir);
		}

		recordingsPath = new File(base + "/recordings");
		
		this.nbdPrefix = nbdExport;
		this.httpPrefix = httpExport;
		this.handlePrefix = handlePrefix;
	}

	public boolean isHandleConfigured()
	{
		return (handlePrefix != null);
	}
}
