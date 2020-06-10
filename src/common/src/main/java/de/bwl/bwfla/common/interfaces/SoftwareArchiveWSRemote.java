package de.bwl.bwfla.common.interfaces;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;

import javax.activation.DataHandler;


public interface SoftwareArchiveWSRemote
{
	public boolean addSoftwarePackage(SoftwarePackage software);
	
	public int getNumSoftwareSeatsById(String id);
	
	public SoftwarePackage getSoftwarePackageById(String id);

	public DataHandler getSoftwarePackages();

	public DataHandler getSoftwarePackageIds();

	public SoftwareDescription getSoftwareDescriptionById(String id);
	
	public DataHandler getSoftwareDescriptions();
	
	public String getName();
}
