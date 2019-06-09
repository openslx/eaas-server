package de.bwl.bwfla.objectarchive.utils;

import java.io.File;
import java.util.*;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.container.helpers.CdromIsoHelper;
import de.bwl.bwfla.emucomp.api.Binding.ResourceType;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectFileArchive.ObjectImportHandle;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectFileMetadata;
import de.bwl.bwfla.objectarchive.datatypes.ObjectFileManifestation;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;

public class DefaultDriveMapper 
{
	final ObjectImportHandle handle;
	
	
	public DefaultDriveMapper(ObjectImportHandle handle)
	{
		this.handle = handle; 
	}
	
	private FileCollectionEntry createEntry(Drive.DriveType driveType, String urlPrefix, File outFile) throws BWFLAException
	{
		if(driveType == null || outFile == null)
			throw new BWFLAException("unsupported format");
		
		FileCollectionEntry fe = new FileCollectionEntry(urlPrefix + "/" + outFile.getName(), driveType, outFile.getName());
		fe.setFileSize(outFile.length());
		fe.setLocalAlias(outFile.getName());
		
		return fe;
	}
	
	private static boolean isInCollection(FileCollection c, FileCollectionEntry e)
	{
		for(FileCollectionEntry ine : c.files)
		{
			if(e.getId().equals(ine.getId()))
				return true;
		}
		return false;
	}
	
	public FileCollection map(String urlPrefix, ObjectFileManifestation object) throws BWFLAException
	{
		FileCollection o = new FileCollection(object.getId());
		
		for (ResourceType rt : ResourceType.values()) {	
			List<FileCollectionEntry> entries = mapResource(urlPrefix, rt, object);
			if(entries == null)
				continue;
			
			for(FileCollectionEntry e : entries)
			{
				if(!isInCollection(o, e))
					o.files.add(e);
			}
		}
		
		return o;
	}
	
	List<ObjectFileManifestation.FileEntry> mapFiles(List<ObjectFileManifestation.FileEntry> flist, String objectId)
	{
		List<ObjectFileManifestation.FileEntry> outFiles = new ArrayList<>();
		File dest = handle.getImportFile(objectId, ResourceType.ISO);
		if(dest.exists())
			return outFiles;

		// avoid dependencies

		List<File> fileList = new ArrayList<>();
		for(ObjectFileManifestation.FileEntry fe : flist)
		{
			fileList.add(fe.getFile());
		}

		if(CdromIsoHelper.createIso(dest, fileList))
			outFiles.add(new ObjectFileManifestation.FileEntry(dest,null));
		
		return outFiles;
	}
	
	
	List<FileCollectionEntry> mapResource(String urlPrefix, ResourceType rt, ObjectFileManifestation object) throws BWFLAException 
	{
		Drive.DriveType driveType = null;

		List<ObjectFileManifestation.FileEntry> flist = object.getResourceFiles(rt);
		if(flist == null || flist.size() == 0)
			return null;
		
		switch (rt) {
		case ISO:
			urlPrefix += "/" + "iso";
			driveType = Drive.DriveType.CDROM;
			break;
		case DISK:
			urlPrefix += "/" + "disk";
			driveType = Drive.DriveType.DISK;
			break;
		case FLOPPY:
			urlPrefix += "/" + "floppy";
			driveType = Drive.DriveType.FLOPPY;
			break;
		case FILE:
			urlPrefix += "/" + "iso";
			driveType = Drive.DriveType.CDROM;
			flist = mapFiles(flist, object.getId());
			break;
		default:
			return null;
		}
		
		List<FileCollectionEntry> entries = new ArrayList<FileCollectionEntry>();
		int i = 0;
		for(ObjectFileManifestation.FileEntry fe : flist) {
			FileCollectionEntry fc = createEntry(driveType, urlPrefix, fe.getFile());
			DigitalObjectFileMetadata md = fe.getMetadata();
			if(md != null)
			{
				// System.out.println("map res: found metadata: " + md.toString());
				fc.setOrder(md.getOrder());
				fc.setLabel(md.getLabel());
			}
			if(i == 0) // default to first object, flist is sorted
			{
				// System.out.println("default to: " + fc.getId() + " " + fc.getLabel() + " " + fc.getOrder());
				fc.setDefault(true);
			}
			i++;
			entries.add(fc);
		}
		return entries;
	}

}
