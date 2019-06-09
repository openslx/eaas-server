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

package de.bwl.bwfla.objectarchive.impl;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.inject.Inject;

import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.objectarchive.datatypes.*;
import de.bwl.bwfla.objectarchive.datatypes.ObjectFileCollection.ObjectFileCollectionHandle;

import gov.loc.mets.Mets;
import org.apache.commons.io.FileUtils;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Binding.ResourceType;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.objectarchive.utils.DefaultDriveMapper;


// FIXME: this class should be implemented in a style of a "Builder" pattern
public class DigitalObjectFileArchive implements Serializable, DigitalObjectArchive
{
	private static final long	serialVersionUID	= -3958997016973537612L;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());

	private String name;
	private String localPath;
	private boolean defaultArchive;
	
	protected ObjectFileFilter objectFileFilter = new ObjectFileFilter();
	protected ObjectImportHandle importHandle;

	@Inject
	@Config(value="objectarchive.httpexport")
	public String httpExport;

	@Inject
	@Config(value="commonconf.serverdatadir")
	public String serverdatadir;


	/**
	 * Simple ObjectArchive example. Files are organized as follows
	 * localPath/
	 *          ID/
	 *            floppy/
	 *            iso/
	 *               disk1.iso
	 *               disk2.iso
	 *               
	 * Allowed extensions:
	 *      iso : {.iso}
	 *      floppy : {.img, .ima, .adf, .D64, .x64, .dsk, .st }
	 * 
	 * @param name
	 * @param localPath
	 */
	public DigitalObjectFileArchive(String name, String localPath, boolean defaultArchive)
	{
		this.init(name, localPath, defaultArchive);
	}

	protected DigitalObjectFileArchive() {}

	protected void init(String name, String localPath, boolean defaultArchive)
	{
		this.name = name;
		this.localPath = localPath;
		this.defaultArchive = defaultArchive;
		importHandle = new ObjectImportHandle(localPath);
		ConfigurationInjection.getConfigurationInjector().configure(this);
	}

	private static String strSaveFilename(String filename)
	{
		return filename.replace(" ", "");
	}

	private Path resolveTarget(String id, ResourceType rt) throws BWFLAException
	{
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		Path targetDir = objectDir.toPath().resolve(id);
		targetDir = targetDir.resolve(rt.value());
		if(!targetDir.toFile().exists())
			if(!targetDir.toFile().mkdirs())
			{
				throw new BWFLAException("could not create directory: " + targetDir);
			}

		return targetDir;
	}

	private Path resolveTarget(String id, Drive.DriveType type) throws BWFLAException
	{
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		Path targetDir = objectDir.toPath().resolve(id);
		switch(type)
		{
			case CDROM:
				targetDir = targetDir.resolve("iso");
				break;
			case FLOPPY:
				targetDir = targetDir.resolve("floppy");
				break;
			default:
				throw new BWFLAException("unsupported type " + type);
		}
		if(!targetDir.toFile().exists())
			if(!targetDir.toFile().mkdirs())
			{
				throw new BWFLAException("could not create directory: " + targetDir);
			}
		return targetDir;
	}

	public String getThumbnail(String id) throws BWFLAException {
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}
		Path targetDir = objectDir.toPath().resolve(id);
		Path target = targetDir.resolve("thumbnail.jpeg");
		if(!Files.exists(target))
			return null;

		String exportPrefix;
		try {
			exportPrefix = httpExport + URLEncoder.encode(name, "UTF-8") + "/" + id;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
		return exportPrefix + "/thumbnail.jpeg";
	}

	public void importObjectThumbnail(FileCollectionEntry resource) throws BWFLAException
	{
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}
		Path targetDir = objectDir.toPath().resolve(resource.getId());
		if(!targetDir.toFile().exists()) {
			if (!targetDir.toFile().mkdirs()) {
				throw new BWFLAException("could not create directory: " + targetDir);
			}
		}

		Path target = targetDir.resolve("thumbnail.jpeg");
		if(Files.exists(target))
			return;
		EmulatorUtils.copyRemoteUrl(resource, target, null);
	}

	void importObjectFile(String objectId, ObjectFileCollection.ObjectFileCollectionHandle fc) throws BWFLAException {
		Path targetDir = resolveTarget(objectId, fc.getType());
		Path target = targetDir.resolve(fc.getFilename());
		try {
			InputStream inputStream = fc.getHandle().getInputStream();
			if(inputStream == null)
			{
				throw new BWFLAException("can't get inputstream");
			}
			if(fc.getType() == ResourceType.FILE)
			{
				Zip32Utils.unzip(inputStream, targetDir.toFile());
			}
			else {
				FileUtils.copyInputStreamToFile(inputStream, target.toFile());
			}
		} catch (IOException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			throw new BWFLAException(e);
		}
	}

	void importObjectFile(FileCollectionEntry resource) throws BWFLAException
	{
		Path targetDir = resolveTarget(resource.getId(), resource.getType());
		
		String fileName = resource.getLocalAlias();
		if(fileName == null || fileName.isEmpty())
			fileName = resource.getId();

		fileName = strSaveFilename(fileName);
		Path target = targetDir.resolve(fileName);

		EmulatorUtils.copyRemoteUrl(resource, target, null);

		if(resource.getLabel() !=  null || resource.getOrder() != null)
		{
			DigitalObjectFileMetadata md = new DigitalObjectFileMetadata(resource.getLocalAlias(), resource.getLabel(), resource.getOrder());
			try {
				md.writeProperties(targetDir.resolve(fileName + ".properties"));
				log.info("writing extended file properties to: " + targetDir.resolve(fileName + ".properties"));
			}
			catch(IOException e)
			{
				throw new BWFLAException(e);
			}
		}
	}

	public void importObject(ObjectFileCollection fc) throws BWFLAException
	{
		if(fc == null || fc.getFiles() == null)
			throw new BWFLAException("Invalid arguments");

		if(objectExits(fc.getId()))
			return;

		for(ObjectFileCollectionHandle entry : fc.getFiles())
			importObjectFile(fc.getId(), entry);
	}

	@Override
	public void importObject(String metsdata) throws BWFLAException {


	}

	public List<String> getObjectList()
	{	
		List<String> objects = new ArrayList<String>();
		
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			log.info("objectDir " + localPath + " does not exist");
			return objects;
		}

		for(File dir: objectDir.listFiles())
		{
			if(dir != null && !dir.isDirectory())
				continue;

			ObjectFileManifestation mf = null;
			try {
				mf = new ObjectFileManifestation(objectFileFilter, dir);
			} catch (BWFLAException e) {
				log.log(Level.WARNING, e.getMessage(), e);
			}

			if(!mf.isEmpty())
				objects.add(mf.getId());
		}
		return objects;
	}

	private boolean objectExits(String objectId)
	{
		if(objectId == null)
			return false;

		log.info("looking for: " + objectId);
		File topDir = new File(localPath);
		if(!topDir.exists() || !topDir.isDirectory())
		{
			log.warning("objectDir " + localPath + " does not exist");
			return false;
		}

		File objectDir = new File(topDir, objectId);
		if(!objectDir.exists() || !objectDir.isDirectory())
		{
			log.warning("objectDir " + objectDir + " does not exist");
			return false;
		}
		return true;
	}

	public void delete(String objectId) throws BWFLAException {
		if(objectId == null)
			throw new BWFLAException("object id was null");

		File topDir = new File(localPath);
		if(!topDir.exists() || !topDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		File objectDir = new File(topDir, objectId);
		if(!objectDir.exists() || !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + objectDir + " does not exist");
		}

		try {
			FileUtils.deleteDirectory(objectDir);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BWFLAException(e);
		}

	}

	@Override
	public Mets getMetsMetadata(String id) {
		return null;
	}

	public FileCollection getObjectReference(String objectId)
	{
		if(objectId == null)
			return null;

		log.info("looking for: " + objectId);
		File topDir = new File(localPath);
		if(!topDir.exists() || !topDir.isDirectory())
		{
			log.warning("objectDir " + localPath + " does not exist");
			return null;
		}		
		
		File objectDir = new File(topDir, objectId);
		if(!objectDir.exists() || !objectDir.isDirectory())
		{
			log.warning("objectDir " + objectDir + " does not exist");
			return null;
		}

		ObjectFileManifestation mf = null;
		try {
			mf = new ObjectFileManifestation(objectFileFilter, objectDir);
		} catch (BWFLAException e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}
		String exportPrefix;
		try {
			exportPrefix = httpExport + URLEncoder.encode(name, "UTF-8") + "/" + URLEncoder.encode(objectId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
		
		DefaultDriveMapper driveMapper = new DefaultDriveMapper(importHandle);
		try {
			return driveMapper.map(exportPrefix, mf);
		} catch (BWFLAException e) {
			// TODO Auto-generated catch block
			log.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
		
	}

	@Override
	public ObjectFileCollection getObjectHandle(String objectId) {
		if (objectId == null)
			return null;

		log.info("looking for: " + objectId);
		File topDir = new File(localPath);
		if (!topDir.exists() || !topDir.isDirectory()) {
			log.warning("objectDir " + localPath + " does not exist");
			return null;
		}

		File objectDir = new File(topDir, objectId);
		if (!objectDir.exists() || !objectDir.isDirectory()) {
			log.warning("objectDir " + objectDir + " does not exist");
			return null;
		}

		ObjectFileManifestation mf = null;
		try {
			mf = new ObjectFileManifestation(objectFileFilter, objectDir);
		} catch (BWFLAException e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}

		List<ObjectFileCollectionHandle> entries = new ArrayList<>();
		for (ResourceType rt : ResourceType.values()) {
			List<ObjectFileManifestation.FileEntry> flist = mf.getResourceFiles(rt);
			if(flist == null)
				continue;

			if(rt.equals(ResourceType.FILE)) // we just add the zip file.
			{
				File zip = new File(objectDir, rt.value() + ".zip");
				if(zip.exists())
				{
					DataHandler handle = new DataHandler(new FileDataSource(zip));
					log.info("adding handle for : " + zip.getName());
					ObjectFileCollectionHandle entry = new ObjectFileCollectionHandle(handle, rt, zip.getName());
					entries.add(entry);
				}
				continue;
			}

			for(ObjectFileManifestation.FileEntry fe : flist)
			{
				DataHandler handle = new DataHandler(new FileDataSource(fe.getFile()));
				log.info("adding handle for : " + fe.getFile().getName());
				ObjectFileCollectionHandle entry = new ObjectFileCollectionHandle(handle, rt, fe.getFile().getName());
				entries.add(entry);
			}
		}

		ObjectFileCollection fc = new ObjectFileCollection(objectId);
		fc.setFiles(entries);
		return fc;
	}

	public Path getLocalPath()
	{
		return new File(localPath).toPath();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public static class ObjectImportHandle
	{
		private final File objectsDir;
		private final Logger log	= Logger.getLogger(this.getClass().getName());
		
		public ObjectImportHandle(String localPath)
		{
			this.objectsDir = new File(localPath);
		}
		
		public File getImportFile(String id, ResourceType rt)
		{
			Path targetDir = objectsDir.toPath().resolve(id);
			switch(rt)
			{
			case ISO: 
				// if(!fileName.endsWith("iso"))
				// 	fileName+=".iso";
				targetDir = targetDir.resolve("iso");
				if(!targetDir.toFile().exists())
					if(!targetDir.toFile().mkdirs())
					{
						log.warning("could not create directory: " + targetDir);
						return null;
					}
				return new File(targetDir.toFile(), "__import.iso");
			default:
				return null;
			}
		}
	}
	
	protected static class NullFileFilter implements FileFilter
	{
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;

			if (file.getName().endsWith(".properties"))
				return false;

			return true;
		}
	}
	
	protected static class IsoFileFilter implements FileFilter
	{
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;
			
			// Check file's extension...
			final String name = file.getName();
			final int length = name.length();
			return (name.regionMatches(true, length - 4, ".iso", 0, 4)  || name.regionMatches(true, length - 4, ".bin", 0, 4));
		}
	};
	
	protected static class FloppyFileFilter implements FileFilter
	{
		private final Set<String> formats = new HashSet<String>();
		
		public FloppyFileFilter()
		{
			// Add all valid formats
			formats.add(".img");
			formats.add(".ima");
			formats.add(".adf");
			formats.add(".d64");
			formats.add(".x64");
			formats.add(".dsk");
			formats.add(".st");
			formats.add(".tap");
		}
		
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;
			
			// Check the file's extension...
			final String name = file.getName();
			final int extpos = name.lastIndexOf('.');
			if (extpos < 0)
				return false;  // No file extension found!
			
			final String ext = name.substring(extpos);
			return formats.contains(ext.toLowerCase());
		}
	}

	@Override
	public DigitalObjectMetadata getMetadata(String objectId) {
		DigitalObjectMetadata md = new DigitalObjectMetadata(objectId, objectId, objectId);

		String thumb = null;
		try {
			thumb = getThumbnail(objectId);
		} catch (BWFLAException e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}
		if (thumb != null)
			md.setThumbnail(thumb);

		return md;
	}

	@Override
	public void sync() {	
	}

	@Override
	public TaskState sync(List<String> objectId) {
		return null;
	}


	@Override
	public boolean isDefaultArchive() {
		return defaultArchive;
	}

	@Override
	public int getNumObjectSeats(String id) {
		return -1;
	}

	public static class ObjectFileFilter
	{
		public FileFilter ISO_FILE_FILTER = new IsoFileFilter();
		public FileFilter FLOPPY_FILE_FILTER = new FloppyFileFilter();
	}
}
