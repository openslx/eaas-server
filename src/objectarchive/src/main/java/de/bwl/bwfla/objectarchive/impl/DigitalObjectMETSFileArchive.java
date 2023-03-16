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

import com.openslx.eaas.common.databind.DataUtils;
import com.openslx.eaas.resolver.DataResolver;
import de.bwl.bwfla.common.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.objectarchive.datatypes.*;
import gov.loc.mets.FileType;
import gov.loc.mets.Mets;
import gov.loc.mets.MetsType;
import org.apache.tamaya.ConfigurationProvider;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class DigitalObjectMETSFileArchive implements Serializable, DigitalObjectArchive
{
	private static final long	serialVersionUID	= -3958997016973537612L;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());

	final private String name;
	final private File metaDataDir;
	final private String dataPath;
	final private String exportUrlPrefix;
	private boolean defaultArchive;
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

		var httpExport = ConfigurationProvider.getConfiguration()
				.get("objectarchive.httpexport");

		if (!httpExport.endsWith("/"))
			httpExport += "/";

		this.exportUrlPrefix = httpExport + URLEncoder.encode(name, StandardCharsets.UTF_8);

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
				log.warning("METS object with id " + o.getId() + " exists. overwriting...");
				// return;
			}
			Files.write( dst, metsdata.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new BWFLAException(e);
		}
		objects.put(o.getId(), o);
	}

	@Override
	public void updateLabel(String objectId, String newLabel) throws BWFLAException
	{
		var mets = objects.get(objectId);
		mets.setLabel(newLabel);
		importObject(mets.toString());
	}

	private void load()
	{
		int numLoaded = 0;
		int numFailed = 0;

		for(File mets: metaDataDir.listFiles())
		{
			try {
				MetsObject obj = new MetsObject(mets);
				objects.put(obj.getId(), obj);
				++numLoaded;
			} catch (BWFLAException e) {
				log.log(Level.WARNING, "Parsing METS file '" + mets.getAbsolutePath() + "' failed!", e);
				++numFailed;
			}
		}

		log.info("Loaded " + numLoaded + " METS object(s), failed " + numFailed);
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
	public String resolveObjectResource(String objectId, String resourceId, String method) throws BWFLAException {
		final var url = DigitalObjectArchive.super.resolveObjectResource(objectId, resourceId, method);
		if (url == null || DataResolver.isAbsoluteUrl(url))
			return url;

		return exportUrlPrefix + "/" + objectId + "/" + url;
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
		try {
			String metsCopyStr = metsOrig.value();
			metsCopy = DataUtils.xml()
					.read(metsCopyStr, Mets.class);
			String exportPrefix = exportUrlPrefix + "/" + id;
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
									if (DataResolver.isRelativeUrl(fLocat.getHref())) {
										fLocat.setHref(exportPrefix + "/" + fLocat.getHref());
										log.warning("updating location");
									}
								}
							}
						}
					}
				}
			}
		}
		catch (JAXBException e) {
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
