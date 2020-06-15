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

package de.bwl.bwfla.objectarchive.impl;

import de.bwl.bwfla.common.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.objectarchive.datatypes.*;
import gov.loc.mets.FileType;
import gov.loc.mets.Mets;
import gov.loc.mets.MetsType;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class DigitalObjectMETSFileArchive implements Serializable, DigitalObjectArchive
{
	private static final long	serialVersionUID	= -3958997016973537612L;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());

	final private String name;
	final private File metaDataDir;
	final private String dataPath;
	private boolean defaultArchive;

	@Inject
	@Config(value="objectarchive.httpexport")
	public String httpExport;

	private HashMap<String, MetsObject> objects;

	public DigitalObjectMETSFileArchive(String name, String metaDataPath, String dataPath, boolean defaultArchive) throws BWFLAException {
		this.name = name;
		this.metaDataDir = new File(metaDataPath);
		if(!metaDataDir.exists() && !metaDataDir.isDirectory())
		{
			throw new BWFLAException("METS metadataPath " + metaDataPath + " does not exist");
		}
		this.dataPath = dataPath;
		this.defaultArchive = defaultArchive;
		this.objects = new HashMap<>();
		ConfigurationInjection.getConfigurationInjector().configure(this);

		load();
	}

	@Override
	public void importObject(String metsdata) throws BWFLAException {
		MetsObject o = new MetsObject(metsdata);
		if(o.getId() == null || o.getId().isEmpty())
			throw new BWFLAException("invalid object id " + o.getId());
		Path dst = this.metaDataDir.toPath().resolve(o.getId());
		try {
			if(Files.exists(dst))
			{
				log.warning("METS object with id " + o.getId() + " exists. skipping...");
				return;
			}
			Files.write( dst, metsdata.getBytes("UTF-8"), StandardOpenOption.CREATE);
		} catch (IOException e) {
			throw new BWFLAException(e);
		}
		objects.put(o.getId(), o);
	}

	private void load()
	{
		for(File mets: metaDataDir.listFiles())
		{
			log.severe("parsing: " + mets.getAbsolutePath());
			try {
				MetsObject obj = new MetsObject(mets);
				objects.put(obj.getId(), obj);
			} catch (BWFLAException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Stream<String> getObjectIds()
	{
		return objects.keySet()
				.stream();
	}

	@Override
	public FileCollection getObjectReference(String objectId) {

		MetsObject obj = objects.get(objectId);
		if(obj == null)
			return null;

		if(dataPath != null) {
			try {
				String exportPrefix = httpExport + URLEncoder.encode(name, "UTF-8");
				return obj.getFileCollection(exportPrefix);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
		else
			return obj.getFileCollection(null);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Path getLocalPath() {
		return Paths.get(dataPath);
	}

	@Override
	public DigitalObjectMetadata getMetadata(String objectId) {
		MetsObject obj = objects.get(objectId);
		if(obj == null)
			return null;

		String id = obj.getId();
		if(id == null)
			return null;

		String label = obj.getLabel();

		DigitalObjectMetadata md = new DigitalObjectMetadata(id, label, label);
		return md;
	}

	@Override
	public Stream<DigitalObjectMetadata> getObjectMetadata() {

		return this.getObjectIds()
				.map(this::getMetadata)
				.filter(Objects::nonNull);
	}

	@Override
	public void sync() {	
	}

	@Override
	public TaskState sync(List<String> objectId) {
		return null;
	}

	@Override
	public void delete(String id) throws BWFLAException {
		throw new BWFLAException("not supported");
	}

	public Mets getMetsMetadata(String id) throws BWFLAException {

		log.warning("getMetsmedatadata " + dataPath);

		MetsObject o = objects.get(id);
		if(o == null)
			throw new BWFLAException("object ID " + id + " not found");

		Mets metsOrig = o.getMets();

		if(dataPath == null)
			return metsOrig;

		// hard copy!
		Mets metsCopy = null;
		JAXBContext jc = null;
		try {
			String metsCopyStr = metsOrig.value();
			jc = JAXBContext.newInstance(Mets.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			metsCopy = (Mets) unmarshaller.unmarshal(new StreamSource(new StringReader(metsCopyStr)));
			String exportPrefix = httpExport + URLEncoder.encode(name, "UTF-8");
			if (metsCopy.getFileSec() != null) {
				List<MetsType.FileSec.FileGrp> fileGrpList = metsCopy.getFileSec().getFileGrp();
				for (MetsType.FileSec.FileGrp fileGrp : fileGrpList) {
					for (FileType file : fileGrp.getFile()) {
						List<FileType.FLocat> locationList = file.getFLocat();
						if(locationList == null)
							continue;
						for (FileType.FLocat fLocat : locationList) {
							if (fLocat.getLOCTYPE() != null && fLocat.getLOCTYPE().equals("URL")) {
								if (fLocat.getHref() != null) {
									if (!fLocat.getHref().startsWith("http")) {
										fLocat.setHref(exportPrefix + "/" + fLocat.getHref());
										log.warning("updating location");
									}
								}
							}
						}
					}
				}
			}
		} catch (JAXBException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		log.warning(metsCopy.toString());

		return metsCopy;
	}

	@Override
	public boolean isDefaultArchive() {
		return defaultArchive;
	}

	@Override
	public int getNumObjectSeats(String id) {
		return -1;
	}

}
