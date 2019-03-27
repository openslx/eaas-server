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

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.objectarchive.datatypes.*;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import javax.inject.Inject;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class DigitalObjectMETSFileArchive implements Serializable, DigitalObjectArchive
{
	private static final long	serialVersionUID	= -3958997016973537612L;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());

	final private String name;
	final private String metaDataPath;
	final private String dataPath;
	private boolean defaultArchive;

	@Inject
	@Config(value="objectarchive.httpexport")
	public String httpExport;

	private HashMap<String, MetsObject> objects;

	public DigitalObjectMETSFileArchive(String name, String metaDataPath, String dataPath, boolean defaultArchive)
	{
		this.name = name;
		this.metaDataPath = metaDataPath;
		this.dataPath = dataPath;
		this.defaultArchive = defaultArchive;
		ConfigurationInjection.getConfigurationInjector().configure(this);

		load();
	}

	private void load()
	{
		HashMap<String, MetsObject> _objects = new HashMap<>();

		File objectDir = new File(metaDataPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			log.info("metadataPath " + metaDataPath + " does not exist");
		}

		for(File mets: objectDir.listFiles())
		{
			log.severe("parsing: " + mets.getAbsolutePath());
			try {
				MetsObject obj = new MetsObject(mets);
				_objects.put(obj.getId(), obj);
			} catch (BWFLAException e) {
				e.printStackTrace();
			}
		}
		this.objects = _objects;
	}

	@Override
	public List<String> getObjectList()
	{	
		log.severe("getObjectList: " + objects.size());
		return new ArrayList<>(objects.keySet());
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
	public ObjectFileCollection getObjectHandle(String objectId) {
		return null;
	}

	@Override
	public void importObject(ObjectFileCollection fc) throws BWFLAException {

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
		String label = obj.getLabel();

		DigitalObjectMetadata md = new DigitalObjectMetadata(id, label, label);
		return md;
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

	@Override
	public boolean isDefaultArchive() {
		return defaultArchive;
	}

	@Override
	public int getNumObjectSeats(String id) {
		return -1;
	}

}
