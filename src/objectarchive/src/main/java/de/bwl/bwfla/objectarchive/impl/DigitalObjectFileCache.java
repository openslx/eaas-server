package de.bwl.bwfla.objectarchive.impl;

import java.io.File;
import java.io.FileFilter;

import de.bwl.bwfla.common.exceptions.BWFLAException;

public class DigitalObjectFileCache extends DigitalObjectFileArchive {

	public static final String OBJECT_CACHE_PATH = "/home/bwfla/server-data/object-cache";
	public static final String OBJECT_CACHE_NAME = "object-cache";
	
	// protected FileFilter ISO_FILE_FILTER = new NullFileFilter();
	
	public DigitalObjectFileCache() throws BWFLAException {
		super(OBJECT_CACHE_NAME, OBJECT_CACHE_PATH, false);
		this.objectFileFilter.ISO_FILE_FILTER = new NullFileFilter(); 
		final File objectCache;
		
		objectCache = new File(OBJECT_CACHE_PATH);
		if(!objectCache.exists())
			if(!objectCache.mkdirs())
				throw new BWFLAException("could not create object-cache: " + OBJECT_CACHE_PATH);
	}

}
