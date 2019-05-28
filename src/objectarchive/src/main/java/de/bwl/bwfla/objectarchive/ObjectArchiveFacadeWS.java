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
import de.bwl.bwfla.objectarchive.impl.DigitalObjectUserArchive;

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

	// todo: make it a configuration option
	private static String USERARCHIVEPRIFIX = "user_archive";

	private DigitalObjectArchive getArchive(String archive) throws BWFLAException
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}

		if(archive == null)
			archive = "default";

		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		if(a != null)
			return a;

		if(!archive.startsWith(USERARCHIVEPRIFIX))
		{
			// try harder, use user_archive prefix
			archive = USERARCHIVEPRIFIX + archive;
			return getArchive(archive);
		}
		throw new BWFLAException("Object archive " + archive + " not found");
	}

	public List<String> getObjectList(String archive) throws BWFLAException {
		DigitalObjectArchive a = getArchive(archive);
		return a.getObjectList();
	}


	public ObjectFileCollection getObjectHandle(String archive, String id) throws BWFLAException
	{
		DigitalObjectArchive a = getArchive(archive);
		return a.getObjectHandle(id);
	}

	/**
	 * @param id object-id
	 * @return object reference as PID / PURL
	 */
	public String getObjectReference(String archive, String id) throws BWFLAException {
		DigitalObjectArchive a = getArchive(archive);
		if(id == null)
		{
			throw new BWFLAException("request for object with invalid objectId " + id);
		}

		try {
			FileCollection fc = a.getObjectReference(id);
			if(fc == null)
				throw new BWFLAException("could not find object");
			return fc.value();
		} catch (JAXBException e) {
			throw new BWFLAException(e);
		}
	}

	public void importObject(String archive, ObjectFileCollection collection) throws BWFLAException {
		DigitalObjectArchive a = getArchive(archive);
		a.importObject(collection);
	}

	public void delete(String archive, String id) throws BWFLAException {
		DigitalObjectArchive a = getArchive(archive);
		a.delete(id);
	}

	public DigitalObjectMetadata getObjectMetadata(String archive, String id) throws BWFLAException {
		DigitalObjectArchive a = getArchive(archive);
		return a.getMetadata(id);
	}

	public int getNumObjectSeats(String archive, String id) throws BWFLAException {
		DigitalObjectArchive a = getArchive(archive);
		return a.getNumObjectSeats(id);

	}

	public TaskState getTaskState(String id)
	{
		return ObjectArchiveSingleton.getState(id);
	}

	public TaskState syncObjects(String _archive, List<String> objectIDs) throws BWFLAException {
		DigitalObjectArchive a = getArchive(_archive);
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

	public void registerUserArchive(String userId) throws BWFLAException {
		ObjectArchiveSingleton.archiveMap.put(userId, new DigitalObjectUserArchive(userId));
	}
}
