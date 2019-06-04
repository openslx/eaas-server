package de.bwl.bwfla.objectarchive;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.logging.Level;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.net.HttpExportServlet;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectArchive;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectFileCache;

import javax.servlet.ServletException;


public class ExportObject extends HttpExportServlet
{ 
	private static final long serialVersionUID = 1L;

	@Override
	public File resolveRequest(String reqStr) 
	{
		String archive = null; // archive is the first element of the path
		String objPath = null; // objPath is the rest of the path without the first element
		try {
			// archive name may contain spaces and needs to be poperly en- or decoded
			int index = reqStr.indexOf("/", 2);
			if(index < 0)
				return null;
			archive = URLDecoder.decode(reqStr.substring(1, index), "UTF-8");
			objPath = URLDecoder.decode(reqStr.substring(reqStr.indexOf("/", 2) + 1), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}

		System.out.println("archive: " + archive + "-" + objPath);

		// System.out.println("http-object-archive: " + requestedObject);
		if(ObjectArchiveSingleton.archiveMap == null)
			return null;
		
		Path localPath = null;
		DigitalObjectArchive doa = ObjectArchiveSingleton.archiveMap.get(archive);
		if(doa == null)
		{
			if(archive.equals(DigitalObjectFileCache.OBJECT_CACHE_NAME))
				localPath = new File(DigitalObjectFileCache.OBJECT_CACHE_PATH).toPath();
		}
		else
			localPath = doa.getLocalPath();
		
		if(localPath == null) // not supported
			return null;

		return new File(localPath.toFile(), objPath);
	}

	@Override
	public File resolveMetaData(String path) throws ServletException {
		throw new ServletException("Method is not implemented");
	}

}
