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

package de.bwl.bwfla.softwarearchive;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.jws.WebService;

import de.bwl.bwfla.common.datatypes.SoftwareDescription;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.interfaces.SoftwareArchiveWSRemote;
import de.bwl.bwfla.softwarearchive.conf.SoftwareArchiveSingleton;


@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebService(targetNamespace = "http://bwfla.bwl.de/api/softwarearchive")
public class SoftwareArchiveWS implements SoftwareArchiveWSRemote
{
	protected static final Logger LOG = Logger.getLogger(SoftwareArchiveWS.class.getName());
	
	@Override
	public boolean addSoftwarePackage(SoftwarePackage software)
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.addSoftwarePackage(software);
	}
	
	@Override
	public String getName()
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.getName();
	}
	
	@Override
	public int getNumSoftwareSeatsById(String id)
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.getNumSoftwareSeatsById(id);
	}
	
	@Override
	public SoftwarePackage getSoftwarePackageById(String id)
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.getSoftwarePackageById(id);
	}
	
	@Override
	public List<String> getSoftwarePackages()
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.getSoftwarePackages();
	}
	
	@Override
	public SoftwareDescription getSoftwareDescriptionById(String id)
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.getSoftwareDescriptionById(id);
	}
	
	@Override
	public List<SoftwareDescription> getSoftwareDescriptions()
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.getSoftwareDescriptions();
	}
}
