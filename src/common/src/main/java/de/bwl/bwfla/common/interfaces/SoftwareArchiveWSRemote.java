package de.bwl.bwfla.common.interfaces;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebService;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;

public interface SoftwareArchiveWSRemote
{
	public boolean addSoftwarePackage(SoftwarePackage software);
	
	public int getNumSoftwareSeatsById(String id);
	
	public SoftwarePackage getSoftwarePackageById(String id);
	
	public List<String> getSoftwarePackages();
	
	public SoftwareDescription getSoftwareDescriptionById(String id);
	
	public List<SoftwareDescription> getSoftwareDescriptions();
	
	public String getName();
}
