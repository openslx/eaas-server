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

package de.bwl.bwfla.objectarchive;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bwl.bwfla.objectarchive.datatypes.*;
import de.bwl.bwfla.objectarchive.impl.*;
import org.apache.commons.io.FileUtils;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import solutions.emulation.preservica.client.SDBConfiguration;


public class DigitalObjectArchiveFactory {

	protected final static Logger log	= Logger.getLogger(DigitalObjectArchiveFactory.class.getName());

	
	private static DigitalObjectArchive fromString(String jsonString)
	{
		final ObjectMapper mapper = new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		// form resulting Description object
		try {
			DigitalObjectArchiveDescriptor result = mapper.readValue(jsonString, DigitalObjectArchiveDescriptor.class);
			if(result == null)
				return null;
			if(result.getType() == null)
				return null;
			DigitalObjectArchiveDescriptor.ArchiveType type = result.getType();
			switch(type)
			{
			case FILE:
				DigitalObjectFileArchiveDescriptor d = mapper.readValue(jsonString, DigitalObjectFileArchiveDescriptor.class);
				if(d == null)
					return null;
				
				return new DigitalObjectFileArchive(d.getName(), d.getLocalPath(), d.isDefaultArchive());
			case ROSETTA:
				DigitalObjectRosettaArchiveDescriptor r = mapper.readValue(jsonString, DigitalObjectRosettaArchiveDescriptor.class);
				if(r == null)
					return null;
				return new DigitalObjectRosettaArchive(r.getUrl(), r.isDefaultArchive());
			case PRESERVICA:
				DigitalObjectPreservicaArchiveDescriptor p = mapper.readValue(jsonString, DigitalObjectPreservicaArchiveDescriptor.class);
				if(p == null)
					return null;
				SDBConfiguration conf = new SDBConfiguration(p.getSdbHost(), p.getUsername(), p.getPassword());
				return new DigitalObjectPreservicaArchive(p.getName(), conf, p.getCollectionId(), p.isDefaultArchive());

			case USER:
				DigitalObjectUserArchiveDescriptor u = mapper.readValue(jsonString, DigitalObjectUserArchiveDescriptor.class);
				if(u == null)
					return null;
				return new DigitalObjectUserArchive(u);

			case METS:
				DigitalObjectMETSFileArchiveDescriptor m = mapper.readValue(jsonString, DigitalObjectMETSFileArchiveDescriptor.class);
				if(m == null)
					return null;
				try {
					return new DigitalObjectMETSFileArchive(m.getName(), m.getMetadataPath(), m.getDataPath(), m.isDefaultArchive());
				}
				catch (BWFLAException e)
				{
					log.severe("failed to create DigitalObjectMETSFileArchive " + e.getMessage());
					return null;
				}

			case S3:
				var descriptor = mapper.readValue(jsonString, DigitalObjectS3ArchiveDescriptor.class);
				if (descriptor == null)
					return null;

				return new DigitalObjectS3Archive(descriptor);

			default:
				return null;
			}
		}
		catch (Exception error) {
			log.log(Level.SEVERE, "Parsing object-archive's description failed!", error);
			return null;
		}
	}
	
	public static List<DigitalObjectArchive> createFromJson(File dir)
	{
		List<DigitalObjectArchive> archives = new ArrayList<DigitalObjectArchive>();
		if(!dir.exists() || !dir.isDirectory())
		{
			log.severe("loading path: " + dir.getAbsolutePath() + " failed");
			return archives;
		}
		
		FileFilter fileFilter = new FileFilter()
		{
			public boolean accept(File file)
			{
				return (!file.isDirectory() && file.getName().endsWith(".json"));
			}
		};

		File[] flist = dir.listFiles(fileFilter);
		if(flist == null)
		{
			log.severe("loading json files in " + dir.getAbsolutePath() + " failed");
			return archives;
		}
	
		for(File jsonFile : flist)
		{
			String json;
			try {
				json = FileUtils.readFileToString(jsonFile);
			} catch (IOException e) {

				log.log(Level.WARNING, e.getMessage(), e);
				continue;
			}

			DigitalObjectArchive a = fromString(json);
			if (a == null)
				continue;
		
			archives.add(a);
		}
		return archives;
	}
}
