package de.bwl.bwfla.objectarchive.datatypes;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.emucomp.api.Binding;
import de.bwl.bwfla.emucomp.api.Binding.ResourceType;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectFileArchive.ObjectFileFilter;

public class ObjectFileManifestation {
	private HashMap<ResourceType, List<FileEntry>> objectFiles;
	protected final Logger log = Logger.getLogger(this.getClass().getName());

	private final File objectDir;
	private final ObjectFileFilter fileFilter;
	private final String id;
	private String defaultFile;

	private boolean empty = true;
	
	public ObjectFileManifestation(ObjectFileFilter filter, File objectDir) throws BWFLAException {
		objectFiles = new HashMap<ResourceType, List<FileEntry>>();
		this.objectDir = objectDir;
		this.fileFilter = filter;
		this.id = objectDir.getName();
		this.defaultFile = null;
		load();
	}

	private void load() throws BWFLAException {
		for (ResourceType rt : ResourceType.values()) {
			File subDir = new File(objectDir, rt.value());
			if (subDir.exists() && subDir.isDirectory()) {
				if(rt.equals(ResourceType.FILE)) {
					File zip = new File(objectDir, rt.value() + ".zip");
					if(!zip.exists()) {
						log.info("zipping " + subDir);
						Zip32Utils.zip(zip, subDir);
					}
					loadEntries(subDir, objectDir.getName(), rt);
				}
				else
					loadEntries(subDir, objectDir.getName(), rt);
			} else {
				// log.info("loading object dir failed: " + objectDir.getModificationScript() + " " + subDir);
			}
		}
	}

	private void loadEntries(File dir, String id, Binding.ResourceType type) throws BWFLAException {
		// log.info("loading entries: " + dir + " " + id);

		FileFilter ff = null;
		File[] flist;
		switch (type) {
		case ISO:
			ff = fileFilter.ISO_FILE_FILTER;
			break;
		case FLOPPY:
			ff = fileFilter.FLOPPY_FILE_FILTER;
			break;
		default:
			ff = null;
		}

		if (ff != null)
			flist = dir.listFiles(ff);
		else
			flist = dir.listFiles();

		if (flist == null || flist.length == 0) {
			log.info("no suitable files found");
			return;
		}
	
		empty = false;

		ArrayList<FileEntry> entries = new ArrayList<>();
		objectFiles.put(type, entries);
		for(File f : flist) {
			Path properties = dir.toPath().resolve(f.getName() + ".properties");
			DigitalObjectFileMetadata md = null;
			if(Files.exists(properties)) {
				md = DigitalObjectFileMetadata.fromPropertiesFile(properties);
				log.info("found metadata: " + md);
			}
			entries.add(new FileEntry(f, md));
		}
	}
	
	public List<FileEntry> getResourceFiles(ResourceType rt)
	{
		List<FileEntry> res = objectFiles.get(rt);
		if(res == null)
			return null;
		Collections.sort(res);
		return res;
	}
	
	public boolean isEmpty()
	{
		return empty;
	}
	
	public String getId()
	{
		return id;
	}

	public String getDefaultFile() {
		return defaultFile;
	}

	public static class FileEntry implements Comparable<FileEntry>{
		private File file;
		private DigitalObjectFileMetadata metadata;

		public FileEntry(File f, DigitalObjectFileMetadata md)
		{
			this.file = f;
			this.metadata = md;
		}

		public File getFile() {
			return file;
		}

		public DigitalObjectFileMetadata getMetadata() {
			return metadata;
		}

		@Override
		public int compareTo(FileEntry o) {
			if(metadata == null || metadata.order == null || o.metadata == null || o.metadata.order == null)
				return file.getName().compareTo(o.file.getName());

			return metadata.order.compareTo(o.metadata.order);
		}
	}
}
