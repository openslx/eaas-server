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
import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.emucomp.api.Binding.ResourceType;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.objectarchive.DefaultDriveMapper;
import de.bwl.bwfla.objectarchive.datatypes.*;
import de.bwl.bwfla.objectarchive.datatypes.ObjectFileCollection.ObjectFileCollectionHandle;
import org.apache.commons.io.FileUtils;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.inject.Inject;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DigitalObjectMETSFileArchive implements Serializable, DigitalObjectArchive
{
	private static final long	serialVersionUID	= -3958997016973537612L;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());

	final private String name;
	final private String localPath;
	private boolean defaultArchive;

	private HashMap<String, MetsObject> objects;

	public DigitalObjectMETSFileArchive(String name, String localPath, boolean defaultArchive)
	{
		this.name = name;
		this.localPath = localPath;
		this.defaultArchive = defaultArchive;
		ConfigurationInjection.getConfigurationInjector().configure(this);

		load();
	}

	private void load()
	{
		HashMap<String, MetsObject> _objects = new HashMap<>();

		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			log.info("objectDir " + localPath + " does not exist");
		}

		for(File mets: objectDir.listFiles())
		{
			log.severe("parsing: " +mets.getAbsolutePath());
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
		return new ArrayList<>(objects.keySet());
	}

	@Override
	public FileCollection getObjectReference(String objectId) {

		MetsObject obj = objects.get(objectId);
		if(obj == null)
			return null;

		return obj.getFileCollection();
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
		return null;
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
