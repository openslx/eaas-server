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

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.common.datatypes.GenericId;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.interfaces.SoftwareArchiveWSRemote;
import de.bwl.bwfla.common.utils.jaxb.JaxbCollectionWriter;
import de.bwl.bwfla.common.utils.jaxb.JaxbNames;
import de.bwl.bwfla.softwarearchive.conf.SoftwareArchiveSingleton;


@Singleton
@MTOM(enabled = true)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebService(targetNamespace = "http://bwfla.bwl.de/api/softwarearchive")
public class SoftwareArchiveWS implements SoftwareArchiveWSRemote
{
	protected static final Logger LOG = Logger.getLogger(SoftwareArchiveWS.class.getName());

	@Resource(lookup = "java:jboss/ee/concurrency/executor/io")
	private Executor executor = null;

	@Inject
	private SeatManager seatmgr;

	@Override
	public boolean hasSoftwarePackage(String id)
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.hasSoftwarePackage(id);
	}

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
	public void delete(String id) {
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		archive.deleteSoftware(id);
	}

	@Override
	public int getNumSoftwareSeatsById(String id)
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.getNumSoftwareSeatsById(id);
	}

	@Override
	public int getNumSoftwareSeatsForTenant(String id, String tenant) throws BWFLAException
	{
		int seats = -1;

		if (tenant != null)
			seats = seatmgr.getNumSeats(tenant, id);

		if (seats < 0) {
			// No tenant specific limits defined!
			seats = this.getNumSoftwareSeatsById(id);
		}

		return seats;
	}

	@Override
	public void setNumSoftwareSeatsForTenant(String id, String tenant, int seats) throws BWFLAException
	{
		seatmgr.setNumSeats(tenant, id, seats);
	}

	@Override
	public void resetNumSoftwareSeatsForTenant(String id, String tenant) throws BWFLAException
	{
		seatmgr.resetNumSeats(tenant, id);
	}

	@Override
	public void resetAllSoftwareSeatsForTenant(String tenant) throws BWFLAException
	{
		seatmgr.resetNumSeats(tenant);
	}
	
	@Override
	public SoftwarePackage getSoftwarePackageById(String id)
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.getSoftwarePackageById(id);
	}
	
	@Override
	public @XmlMimeType("application/xml") DataHandler getSoftwarePackageIds()
	{
		final ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		final Stream<String> ids = archive.getSoftwarePackageIds();
		return this.toDataHandler(ids.map(GenericId::new), GenericId.class, JaxbNames.SOFTWARE_PACKAGE_IDS);
	}

	public @XmlMimeType("application/xml") DataHandler getSoftwarePackages()
	{
		final ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		final Stream<SoftwarePackage> packages = archive.getSoftwarePackages();
		return this.toDataHandler(packages, SoftwarePackage.class, JaxbNames.SOFTWARE_PACKAGES);
	}
	
	@Override
	public SoftwareDescription getSoftwareDescriptionById(String id)
	{
		ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		return archive.getSoftwareDescriptionById(id);
	}
	
	@Override
	public @XmlMimeType("application/xml") DataHandler getSoftwareDescriptions()
	{
		final ISoftwareArchive archive = SoftwareArchiveSingleton.getArchiveInstance();
		final Stream<SoftwareDescription> descriptions = archive.getSoftwareDescriptions();
		return this.toDataHandler(descriptions, SoftwareDescription.class, JaxbNames.SOFTWARE_DESCRIPTIONS);
	}

	private <T> DataHandler toDataHandler(Stream<T> source, Class<T> klass, String name)
	{
		try {
			final String mimetype = "application/xml";
			final JaxbCollectionWriter<T> pipe = new JaxbCollectionWriter<>(source, klass, name, mimetype, LOG);
			executor.execute(pipe);
			return pipe.getDataHandler();
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Returning data-handler for '" + name + "' failed!", error);
			source.close();
			return null;
		}
	}
}
