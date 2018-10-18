package de.bwl.bwfla.softwarearchive.util;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import de.bwl.bwfla.api.softwarearchive.SoftwareArchiveWS;
import de.bwl.bwfla.api.softwarearchive.SoftwareArchiveWSService;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;


public class SoftwareArchiveHelper
{
	protected final Logger log = Logger.getLogger(this.getClass().getName());
	
	private SoftwareArchiveWS archive = null; 
	private final String wsHost;
	
	public SoftwareArchiveHelper(String wsHost)
	{
		this.wsHost = wsHost;
	}
	
	public boolean addSoftwarePackage(SoftwarePackage software) throws BWFLAException
	{
		this.connectArchive();
		
		return archive.addSoftwarePackage(software);
	}
	
	public int getNumSoftwareSeatsById(String id) throws BWFLAException
	{
		this.connectArchive();
		
		return archive.getNumSoftwareSeatsById(id);
	}
	
	public SoftwarePackage getSoftwarePackageById(String id) throws BWFLAException
	{
		this.connectArchive();
		
		return archive.getSoftwarePackageById(id);
	}
	
	public List<String> getSoftwarePackages() throws BWFLAException
	{
		this.connectArchive();
		
		return archive.getSoftwarePackages();
	}
	
	public SoftwareDescription getSoftwareDescriptionById(String id) throws BWFLAException
	{
		this.connectArchive();
		
		return archive.getSoftwareDescriptionById(id);
	}
	
	public List<SoftwareDescription> getSoftwareDescriptions() throws BWFLAException
	{
		this.connectArchive();
		
		return archive.getSoftwareDescriptions();
	}
	
	public String getName() throws BWFLAException
	{
		this.connectArchive();
		
		return archive.getName();
	}
	
	public String getHost()
	{
		return wsHost;
	}
	

	/* =============== Internal Methods =============== */
	
	
	private void connectArchive() throws BWFLAException
	{
		if (archive != null)
			return;
		
		final String address = wsHost + "/softwarearchive/SoftwareArchiveWS?wsdl";
		try {
			SoftwareArchiveWSService service = new SoftwareArchiveWSService(new URL(address));
			archive = service.getSoftwareArchiveWSPort();
		}
		catch (Throwable throwable) {
			throw new BWFLAException("Connecting to '" + address + "' failed!", throwable);
		}

		BindingProvider bp = (BindingProvider) archive;
		Map<String, Object> context = bp.getRequestContext();
		context.put("javax.xml.ws.client.receiveTimeout", "0");
		context.put("javax.xml.ws.client.connectionTimeout", "0");
		context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
	}
}
