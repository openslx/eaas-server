package de.bwl.bwfla.emil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ImageInformation;
import de.bwl.bwfla.emucomp.api.AbstractDataResource;
import de.bwl.bwfla.emucomp.api.ObjectArchiveBinding;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;

public class EmilUtils
{
	private static final Logger LOG = Logger.getLogger(EmilUtils.class.getName());
	

	
	
	
	private static void exportCowFile(String ref, File imageDir, String fileName) throws IOException, BWFLAException
	{
		Set<PosixFilePermission> permissions = new HashSet<>();
		permissions.add(PosixFilePermission.OWNER_READ);
		permissions.add(PosixFilePermission.OWNER_WRITE);
		permissions.add(PosixFilePermission.OWNER_EXECUTE);
		permissions.add(PosixFilePermission.GROUP_READ);
		permissions.add(PosixFilePermission.GROUP_WRITE);
		permissions.add(PosixFilePermission.GROUP_EXECUTE);
		
		File tempDir = Files.createTempDirectory("", PosixFilePermissions.asFileAttribute(permissions)).toFile();
		
		java.nio.file.Path cowPath = tempDir.toPath().resolve("export.cow");
		EmulatorUtils.createCowFile(ref, cowPath);
		java.nio.file.Path fuseMountpoint = cowPath
                .resolveSibling(cowPath.getFileName() + ".fuse");
		
        File exportFile = EmulatorUtils.mountCowFile(cowPath, fuseMountpoint).toFile();
        
        File dest = new File(imageDir, fileName); 
        // java.nio.file.Files.copy(exportFile.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        EmulatorUtils.convertImage(exportFile.toPath(), dest.toPath(), ImageInformation.QemuImageFormat.QCOW2, LOG);
        tempDir.delete();
	}
	
	private static void exportObject(File objectIdDir, FileCollectionEntry fce) throws BWFLAException
	{
		
		String typeName = fce.getType().name().toLowerCase();
		if(typeName.equals("cdrom"))
			typeName = "iso";
		
		File typeDir = new File(objectIdDir, typeName);
		if(!typeDir.exists())
			typeDir.mkdir();
		
		File destImage;
		if(fce.getLocalAlias() == null || fce.getLocalAlias().isEmpty())
			destImage = new File(typeDir, fce.getId());
		else
			destImage = new File(typeDir, fce.getLocalAlias());
		
		System.out.println(fce.getUrl() + " to: " + destImage);
		EmulatorUtils.copyRemoteUrl(fce, destImage.toPath(), null);
	}
	
	public static AbstractDataResource getResourceById(MachineConfiguration env, String id)
	{
		for (AbstractDataResource ab : env.getAbstractDataResource()) {
			if (ab.getId().equals(id))
					return ab;
		}
		return null;
				
	}
	
	public static void exportEnvironmentMedia(MachineConfiguration abstractEnv, MachineConfiguration env, 
			File imageDir, File objectDir) 
			throws URISyntaxException, IOException, BWFLAException
	{	
		if(env == null)
			return;
		
		for (AbstractDataResource ab : env.getAbstractDataResource()) {
			
			if(ab instanceof ImageArchiveBinding)
	    	{
	    		ImageArchiveBinding iab = (ImageArchiveBinding)ab;
	    		exportCowFile(iab.getUrl(), imageDir, iab.getImageId());
	    	}
			
			else if(ab instanceof ObjectArchiveBinding)
			{
				ObjectArchiveBinding archive = (ObjectArchiveBinding) ab;
				ObjectArchiveHelper helper = new ObjectArchiveHelper(archive.getArchiveHost());
				FileCollection fc = helper.getObjectReference(archive.getArchive(), archive.getId());
				if (fc == null || fc.id == null)
					throw new BWFLAException("Error retrieving object meta data");

				// create dir with fc.id
				File objectIdDir = new File(objectDir, fc.id);
				if(!objectIdDir.exists())
					objectIdDir.mkdir();
				System.out.println("exporting to: " + objectIdDir);
				for (FileCollectionEntry link : fc.files) {
					if (link.getId() == null || link.getUrl() == null)
						continue;
					exportObject(objectIdDir, link);
				}
			}
		}
	}
}
