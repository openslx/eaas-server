package de.bwl.bwfla.objectarchive;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectArchive;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectUserArchive;
import gov.loc.mets.Mets;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import static de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton.tmpArchiveName;

@Stateless
@MTOM
@WebService(targetNamespace = "http://bwfla.bwl.de/api/objectarchive")
public class ObjectArchiveFacadeWS 
{
	protected static final Logger LOG = Logger.getLogger(ObjectArchiveFacadeWS.class.getName());

	@Inject
	@Config(value="objectarchive.default_archive")
	private String defaultArchive;

	@Inject
	@Config(value="objectarchive.user_archive_prefix")
	private String USERARCHIVEPRIFIX;
	
	@PostConstruct
	private void initialize()
	{
		ConfigurationInjection.getConfigurationInjector().configure(this);
	}
	
	/**
	 * @return list of object IDs
	 */

	private DigitalObjectArchive getArchive(String archive) throws BWFLAException
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}

		if(archive == null)
			archive = defaultArchive;

		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		if(a != null)
			return a;

		if(!archive.startsWith(USERARCHIVEPRIFIX))
		{
			LOG.warning("trying harder: " + archive);
			archive = USERARCHIVEPRIFIX + archive;
			return getArchive(archive);
		}
		throw new BWFLAException("Object archive " + archive + " not found");
	}

	public List<String> getObjectList(String archive) throws BWFLAException {
		DigitalObjectArchive a = getArchive(archive);
		return a.getObjectList();
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

	public void importObjectFromMetadata(String archive, String metadata) throws BWFLAException {
		DigitalObjectArchive a = getArchive(archive);
		a.importObject(metadata);
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
