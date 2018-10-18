package de.bwl.bwfla.objectarchive;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectArchive;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.objectarchive.datatypes.ObjectFileCollection;
import de.bwl.bwfla.objectarchive.datatypes.TaskState;

import static de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton.tmpArchiveName;

@Stateless
@MTOM
@WebService(targetNamespace = "http://bwfla.bwl.de/api/objectarchive")
public class ObjectArchiveFacadeWS 
{
	protected static final Logger LOG = Logger.getLogger(ObjectArchiveFacadeWS.class.getName());
	
	@PostConstruct
	private void initialize()
	{
		
	}
	
	/**
	 * @return list of object IDs
	 */

	public List<String> getObjectList(String archive)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}
		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		return a.getObjectList();
	}


	public ObjectFileCollection getObjectHandle(String archive, String id)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}

		if(archive == null)
			archive = "default";

		LOG.info("archive: " + archive);
		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		return a.getObjectHandle(id);
	}

	/**
	 * @param id object-id
	 * @return object reference as PID / PURL
	 */
	public String getObjectReference(String archive, String id)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}

		if(id == null)
		{
			LOG.warning("request for object with invalid objectId " + id);
			return null;
		}

		if(archive == null)
			archive = "default";

		LOG.info("archive: " + archive);
		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		try {
			FileCollection fc = a.getObjectReference(id);
			if(fc == null)
			{
				LOG.warning("could not find object");
				return null;
			}
			return fc.value();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			LOG.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
	}

	public boolean importObject(String archive, ObjectFileCollection collection)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return false;
		}

		if(archive == null)
			archive = "default";

		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		if(a == null)
			return false;

		try {
			a.importObject(collection);
		} catch (BWFLAException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}

		return true;
	}
	
	public DigitalObjectMetadata getObjectMetadata(String archive, String id)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}

		if(archive == null)
			archive = "default";
		
		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		if(a == null)
			return null;
		
		return a.getMetadata(id);
	}

	public int getNumObjectSeats(String archive, String id)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return 0; // todo: throw an exeception
		}

		if(archive == null)
			archive = "default";

		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		if(a == null)
			return 0;

		return a.getNumObjectSeats(id);

	}

	public TaskState getTaskState(String id)
	{
		return ObjectArchiveSingleton.getState(id);
	}

	public TaskState syncObjects(String _archive, List<String> objectIDs)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}

		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(_archive);
		if(a == null)
			return null ;

		return a.sync(objectIDs);
	}

	public void sync(String _archive)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return;
		}
		
		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(_archive);
		if(a == null)
			return;
		
		a.sync();
	}
	
	public void syncAll()
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return;
		}
		
		for(DigitalObjectArchive a : ObjectArchiveSingleton.archiveMap.values())
		{
			if(a.getName().equals("default"))
				continue;
			a.sync();
		}
	}

	public List<String> getArchives() {
		if (!ObjectArchiveSingleton.confValid) {
			LOG.severe("ObjectArchive not configured");
			return null;
		}

		Set<String> keys = ObjectArchiveSingleton.archiveMap.keySet();
		ArrayList<String> result = new ArrayList<>();
		for (String key : keys)
		{
			if(key.equals(tmpArchiveName))
				continue;

			result.add(key);
		}
		return result;
	}
}
