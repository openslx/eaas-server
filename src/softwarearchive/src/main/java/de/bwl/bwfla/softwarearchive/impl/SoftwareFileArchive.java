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

package de.bwl.bwfla.softwarearchive.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;
import de.bwl.bwfla.softwarearchive.ISoftwareArchive;
import de.bwl.bwfla.wikidata.reader.QIDsFinder;

import javax.xml.bind.JAXBException;


public class SoftwareFileArchive implements Serializable, ISoftwareArchive
{
	private static final long serialVersionUID = -8250444788579220131L;

	protected final Logger log = Logger.getLogger(this.getClass().getName());

	private final String name;
	private final Path archivePath;

	
	/**
	 * File-based SoftwareArchive, with SoftwarePackages stored as XML files:
	 * <i>path/ID</i>
	 */
	public SoftwareFileArchive(String name, String path)
	{
		this.name = name;
		this.archivePath = Paths.get(path);
	}
	
	@Override
	public synchronized boolean addSoftwarePackage(SoftwarePackage software)
	{
		final String id = software.getObjectId();
		final Path path = archivePath.resolve(id);
		if (Files.exists(path)) {
			log.info("Software package with ID " + id + " already exists! Replacing it...");
			try {
				Files.deleteIfExists(path);
			} catch (IOException exception) {
				log.warning("Deleting software package with ID " + id + " failed!");
				log.log(Level.SEVERE, exception.getMessage(), exception);
			}
		}
		try {
			Files.write( path, software.value(true).getBytes("UTF-8"), StandardOpenOption.CREATE);
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
			log.warning("Writing software package '" + path.toString() + "' failed!");
			return false;
		}
		return true;
	}
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getNumSoftwareSeatsById(String id)
	{
		SoftwarePackage software = this.getSoftwarePackageById(id);
		return (software != null) ? software.getNumSeats() : -1;
	}
	
	@Override
	public SoftwarePackage getSoftwarePackageById(String id)
	{
		final Path path = archivePath.resolve(id);
		if (Files.notExists(path)) {
			// log.warning("Software package with ID " + id + " does not exist!");
			return null;
		}
		// check if QID is set
		// if QID => get support file formats *read and write
		// add PUIDs to array
		SoftwarePackage sp = this.getSoftwarePackageByPath(path);
		if(sp == null)
			return null;

		String QID =  sp.getQID();
		List<String> formats = sp.getSupportedFileFormats();
		if (formats == null) {
			formats = new ArrayList<String>();
		}

		// if(QID  != null)
		//	QIDsFinder.extendSupportedFormats(QID, formats);
		return sp;
	}

	@Override
	public List<String> getSoftwarePackages()
	{
		List<String> packages = new ArrayList<String>();
		try (DirectoryStream<Path> files = Files.newDirectoryStream(archivePath)) {
			for (Path file: files)
				packages.add(file.getFileName().toString());
		}
		catch (Exception exception) {
			log.warning("Reading software package directory failed!");
			log.log(Level.SEVERE, exception.getMessage(), exception);
		}
		
		return packages;
	}
	
	@Override
	public SoftwareDescription getSoftwareDescriptionById(String id)
	{
		SoftwarePackage software = this.getSoftwarePackageById(id);
		if (software == null)
			return null;
		
		SoftwareDescription result =  new SoftwareDescription(id, software.getName(), software.getIsOperatingSystem(), software.getArchive());
		result.setPublic(software.isPublic());
		return result;
	}
	
	@Override
	public List<SoftwareDescription> getSoftwareDescriptions()
	{
		List<SoftwareDescription> descriptions = new ArrayList<SoftwareDescription>();
		try {
			for (String id: this.getSoftwarePackages())
				descriptions.add(this.getSoftwareDescriptionById(id));
		}
		catch (Exception exception) {
			log.warning("Reading software package descriptions failed!");
			log.log(Level.SEVERE, exception.getMessage(), exception);
		}
		
		return descriptions;
	}
	

	/* =============== Internal Methods =============== */
	
	private synchronized SoftwarePackage getSoftwarePackageByPath(Path path)
	{
		try {
			byte[] encoded = Files.readAllBytes(path);
			return SoftwarePackage.fromValue(new String(encoded, StandardCharsets.UTF_8), SoftwarePackage.class);
		}
		catch (Exception exception) {
			log.warning("Reading software package '" + path.toString() + "' failed!");
			return null;
		}
	}
}
