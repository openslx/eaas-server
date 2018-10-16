package de.bwl.bwfla.imagearchive;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.common.services.net.HttpExportServlet;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveSingleton;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata.ImageType;


public class ExportObject extends HttpExportServlet
{
	private static HashMap<String, File> exportCache = new HashMap<String, File>();
	Logger log = Logger.getLogger("ImageArchive Export Object");

	@Override
	public File resolveRequest(String reqStr) 
	{
		File cached = exportCache.get(reqStr);
		{
			if(cached != null) {
				return cached;
			}
		}

		String filename = null;
		if(!reqStr.startsWith("/"))
		{
			System.out.println("wrong req str: " + reqStr);
			return null;
		}
		filename = reqStr;
		
		try {
			filename = URLDecoder.decode(filename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
		
		for(ImageType type : ImageType.values())
		{
			File f = new File(ImageArchiveSingleton.iaConfig.imagePath + "/" + type.name() + filename);
			if(f.exists()) {
				exportCache.put(reqStr, f);
				return f;
			}
		}
		
		return null;
	}

}
