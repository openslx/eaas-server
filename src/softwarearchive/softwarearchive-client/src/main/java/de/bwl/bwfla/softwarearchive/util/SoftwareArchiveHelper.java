package de.bwl.bwfla.softwarearchive.util;

import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.xml.transform.Source;
import javax.xml.ws.BindingProvider;

import de.bwl.bwfla.api.softwarearchive.SoftwareArchiveWS;
import de.bwl.bwfla.api.softwarearchive.SoftwareArchiveWSService;
import de.bwl.bwfla.common.datatypes.GenericId;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbCollectionReader;
import de.bwl.bwfla.common.utils.jaxb.JaxbNames;


public class SoftwareArchiveHelper
{
	protected final Logger log = Logger.getLogger(this.getClass().getName());
	
	private SoftwareArchiveWS archive = null; 
	private final String wsHost;
	
	public SoftwareArchiveHelper(String wsHost)
	{
		this.wsHost = wsHost;
	}

	public boolean hasSoftwarePackage(String id) throws BWFLAException
	{
		this.connectArchive();

		return archive.hasSoftwarePackage(id);
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

	public int getNumSoftwareSeatsForTenant(String id, String tenant) throws BWFLAException
	{
		this.connectArchive();

		return archive.getNumSoftwareSeatsForTenant(id, tenant);
	}

	public void setNumSoftwareSeatsForTenant(String id, String tenant, int seats) throws BWFLAException
	{
		this.connectArchive();

		archive.setNumSoftwareSeatsForTenant(id, tenant, seats);
	}

	public void resetNumSoftwareSeatsForTenant(String id, String tenant) throws BWFLAException
	{
		this.connectArchive();

		archive.resetNumSoftwareSeatsForTenant(id, tenant);
	}

	public void resetAllSoftwareSeatsForTenant(String tenant) throws BWFLAException
	{
		this.connectArchive();

		archive.resetAllSoftwareSeatsForTenant(tenant);
	}
	
	public SoftwarePackage getSoftwarePackageById(String id) throws BWFLAException
	{
		this.connectArchive();
		
		return archive.getSoftwarePackageById(id);
	}
	
	public Stream<SoftwarePackage> getSoftwarePackages() throws BWFLAException
	{
		this.connectArchive();
		
		final Source source = archive.getSoftwarePackages();
		try {
			final String name = JaxbNames.SOFTWARE_PACKAGES;
			return new JaxbCollectionReader<>(source, SoftwarePackage.class, name, log)
					.stream();
		}
		catch (Exception error) {
			throw new BWFLAException("Parsing software-packages failed!", error);
		}
	}

	public Stream<String> getSoftwarePackageIds() throws BWFLAException
	{
		this.connectArchive();

		final Source source = archive.getSoftwarePackageIds();
		try {
			final String name = JaxbNames.SOFTWARE_PACKAGE_IDS;
			return new JaxbCollectionReader<>(source, GenericId.class, name, log)
					.stream()
					.map(GenericId::get);
		}
		catch (Exception error) {
			throw new BWFLAException("Parsing software-package IDs failed!", error);
		}
	}
	
	public SoftwareDescription getSoftwareDescriptionById(String id) throws BWFLAException
	{
		this.connectArchive();
		
		return archive.getSoftwareDescriptionById(id);
	}
	
	public Stream<SoftwareDescription> getSoftwareDescriptions() throws BWFLAException
	{
		this.connectArchive();

		final Source source = archive.getSoftwareDescriptions();
		try {
			final String name = JaxbNames.SOFTWARE_DESCRIPTIONS;
			return new JaxbCollectionReader<>(source, SoftwareDescription.class, name, log)
					.stream();
		}
		catch (Exception error) {
			throw new BWFLAException("Parsing software-descriptions failed!", error);
		}
	}
	
	public String getName() throws BWFLAException
	{
		this.connectArchive();
		
		return archive.getName();
	}

	public void deleteSoftware(String id) throws BWFLAException
	{
		this.connectArchive();
		archive.delete(id);
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
