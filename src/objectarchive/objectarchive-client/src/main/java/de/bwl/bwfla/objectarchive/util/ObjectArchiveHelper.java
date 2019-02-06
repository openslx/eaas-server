package de.bwl.bwfla.objectarchive.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import de.bwl.bwfla.api.objectarchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.FileCollection;

public class ObjectArchiveHelper {

	protected final Logger	log	= Logger.getLogger(this.getClass().getName());
	
	private ObjectArchiveFacadeWS archive = null; 
	private final String wsHost;
	
	public ObjectArchiveHelper(String wsHost)
	{
		this.wsHost = wsHost;
	}
	
	private static ObjectArchiveFacadeWS getImageArchiveCon(String host)
	{
		URL wsdl;
		ObjectArchiveFacadeWS archive;
		try 
		{
			wsdl = new URL(host + "/objectarchive/ObjectArchiveFacadeWS?wsdl");
			ObjectArchiveFacadeWSService service = new ObjectArchiveFacadeWSService(wsdl);
			archive = service.getObjectArchiveFacadeWSPort();
		} 
		catch (Throwable t) 
		{
			// TODO Auto-generated catch block
			Logger.getLogger(ObjectArchiveFacadeWS.class.getName()).info("Can not initialize wsdl from " + host + "/objectarchive/ObjectArchiveFacadeWS?wsdl");
			return null;
		}

		BindingProvider bp = (BindingProvider)archive;
		SOAPBinding binding = (SOAPBinding) bp.getBinding();
		binding.setMTOMEnabled(true);
		bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
		bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", "0");
		bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, host + "/objectarchive/ObjectArchiveFacadeWS?wsdl");

		return archive;
	}
	
	private void connectArchive() throws BWFLAException
	{
		if(archive != null)
			return;
		
		archive = getImageArchiveCon(wsHost);
		if(archive == null)
			throw new BWFLAException("could not connect to object archive @ " + wsHost);	
	}
	
	public DigitalObjectMetadata getObjectMetadata(String _archive, String id) throws BWFLAException
	{
		connectArchive();
		return archive.getObjectMetadata(_archive, id);
	}
	
	public List<String> getObjectList(String _archive) throws BWFLAException
	{
		connectArchive();
		List<String> objs = archive.getObjectList(_archive);
		
		if(objs == null)
		{
			log.warning("archive  " + _archive + " is empty");
			return new ArrayList<>();
		}
		
		log.info(_archive + ": found " + objs.size() + " objects");
		List<String> uniqueList = new ArrayList<String>(
				new HashSet<String>(objs));
		java.util.Collections.sort(uniqueList);
		return uniqueList;
	}

	public ObjectFileCollection getObjectHandle(String _archive, String id) throws BWFLAException {
		connectArchive();
		return archive.getObjectHandle(_archive, id);
	}

	public FileCollection getObjectReference(String _archive, String id) throws BWFLAException
	{
		connectArchive();
		String colStr = archive.getObjectReference(_archive, id);
		if(colStr == null)
		{
			log.warning("could not get metadata for ID: " + id);
			return null;
		}
		
		FileCollection fc = null;
		try {
			fc = FileCollection.fromValue(colStr);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			log.log(Level.WARNING, e.getMessage(), e);
		}
		if (fc == null || fc.id == null)
			return null;
		
		if(fc.files.size() == 0)
			return null;
		
		return fc;
	}

	public boolean importObject(String _archive, ObjectFileCollection collection) throws BWFLAException {
		connectArchive();
		return archive.importObject(_archive, collection);
	}
	
	public void sync(String _archive) throws BWFLAException
	{
		connectArchive();
		archive.sync(_archive);
	}

	public TaskState sync(String _archive, List<String> objectIDs) throws BWFLAException
	{
		connectArchive();
		return archive.syncObjects(_archive, objectIDs);
	}

	public TaskState getTaskState(String taskId) throws BWFLAException {
		connectArchive();
		return archive.getTaskState(taskId);
	}
	
	public void sync() throws BWFLAException
	{
		connectArchive();
		archive.syncAll();
	}

	public int getNumObjectSeats(String _archive, String objectId) throws BWFLAException {
		connectArchive();
		return archive.getNumObjectSeats(_archive, objectId);
	}

	public List<String> getArchives() throws BWFLAException {
		connectArchive();
		return archive.getArchives();
	}

	public String getHost()
	{
		return wsHost;
	}

	public void registerUserArchive(String userId) throws BWFLAException {
		connectArchive();
		archive.registerUserArchive(userId);
	}

	public void delete(String _archive, String objectId) throws BWFLAException {
		connectArchive();
		archive.delete(_archive, objectId);
	}
}
