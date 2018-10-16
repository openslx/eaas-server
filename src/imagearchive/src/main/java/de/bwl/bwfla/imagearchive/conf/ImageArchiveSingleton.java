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

package de.bwl.bwfla.imagearchive.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.services.handle.HandleUtils;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Environment;
import de.bwl.bwfla.imagearchive.IWDArchive;
import de.bwl.bwfla.imagearchive.ImageHandler;


@Singleton
@Startup
public class ImageArchiveSingleton
{
	protected static final Logger			LOG	= Logger.getLogger(ImageArchiveSingleton.class.getName());
	public volatile boolean 			confValid = false;
	private static AtomicBoolean	constructed	= new AtomicBoolean(false);

	public static volatile ImageArchiveConfig iaConfig;
	public static volatile IWDArchive iwdArchive;
	public static volatile ImageHandler imageHandler;

	public static volatile Properties defaultEnvironments = new Properties();
	public static volatile Map<ImageArchiveMetadata.ImageType, ConcurrentHashMap<String, Environment>> imagesCache = new ConcurrentHashMap<>();
	public static volatile File defaultEnvironmentsFile;

	@Inject
	@Config(value="imagearchive.basepath")
	public String basePath;
	
//	@Inject
//	@Config(value="imagearchive.httpprefix")
	public String httpPrefix;
	
//	@Inject
//	@Config(value="imagearchive.nbdprefix")
	public String nbdPrefix;
	
	@PostConstruct
    public void init()  
	{
		LOG.info("initializing ImageArchiveSingleton");

		try {
			loadConf();
		} catch (IOException|JAXBException e) {
			// TODO Auto-generated catch block
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		catch (BWFLAException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public boolean validate()
	{
		if(basePath != null)return true;
		else return false;// we have defaults 
	}

	
	synchronized public void loadConf() throws IOException, JAXBException, BWFLAException {
	    Configuration config = ConfigurationProvider.getConfiguration();
	    nbdPrefix = config.get("imagearchive.nbdprefix");
        httpPrefix = config.get("imagearchive.httpprefix");

		final String handlePrefix = (config.get("imagearchive.export_handles", Boolean.class)) ? HandleUtils.getHandlePrefix(config) : null;
	    
		if(constructed.compareAndSet(false, true))
		{	
			confValid = validate();

			iaConfig     = new ImageArchiveConfig(basePath, nbdPrefix, httpPrefix, handlePrefix);
			iwdArchive   = new IWDArchive(iaConfig);
			
			imageHandler = new ImageHandler(iaConfig);

			imagesCache  = new HashMap<>();
			for(ImageArchiveMetadata.ImageType t : ImageArchiveMetadata.ImageType.values())
			{
				imagesCache.put(t, imageHandler.loadMetaData(t));
			}
		}

		defaultEnvironmentsFile = new File(iaConfig.metaDataPath, "defaultEnvironments.properties");
		if(defaultEnvironmentsFile.exists()) {
			try (FileInputStream fis = new FileInputStream(defaultEnvironmentsFile)) {
				defaultEnvironments.load(fis);
			}
		}
	}

//	private static void updateImageMetadata(Path path, String filename)
//	{
//		String[] elems = path.toString().split("/");
//		ImageType t;
//		try {
//			t = ImageType.valueOf(elems[elems.length-1]);
//		}
//		catch(IllegalArgumentException e)
//		{
//			LOG.warning("unknown ImageType " + e.getMessage());
//			return;
//		}
//
//		Map<Path,String> map = imagesCache.get(t);
//		if(map == null)
//			return;
//		String env = imageHandler.loadMetaDataFile(new File(path.toFile(), filename));
//		if(env != null)
//		{
//			// LOG.info("updating " + new File(path.toFile(), filename).toPath() + " " + env);
//			map.put(new File(path.toFile(), filename).toPath(), env);
//		}
//	}
//
//	private static void removeImageMetadata(Path path, String filename)
//	{
//		String[] elems = path.toString().split("/");
//		ImageType t;
//		try {
//			t = ImageType.valueOf(elems[elems.length-1]);
//		}
//		catch(IllegalArgumentException e)
//		{
//			LOG.warning("unknown ImageType " + e.getMessage());
//			return;
//		}
//
//		Map<Path,String> map = imagesCache.get(t);
//		if(map == null)
//			return;
//		// LOG.info("removing " + new File(path.toFile(), filename).toPath().toString());
//		map.remove(new File(path.toFile(), filename).toPath());
//	}

//	private static void registerWatchRecursively(final Path root, final WatchService watcher) throws IOException
//	{
//		final Map<WatchKey, Path> keys = new HashMap<>();
//
//		Files.walkFileTree(root, new SimpleFileVisitor<Path>() 
//				{
//			@Override
//			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
//			{
//				if(!dir.equals(root))
//				{
//					WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.OVERFLOW);
//					LOG.info("watching :" + dir);
//					keys.put(key, dir);
//				}
//
//				return FileVisitResult.CONTINUE;
//			}
//				});
//
//		new Thread()
//		{	
//			@Override
//			public void run()
//			{	
//				while(true)
//					try 
//				{
//						WatchKey key = watcher.take();
//						Path dir = keys.get(key);
//						// key.pollEvents();
//						for (WatchEvent<?> event: key.pollEvents()) 
//						{
//							WatchEvent.Kind<?> kind = event.kind();
//
//							if (kind == StandardWatchEventKinds.OVERFLOW) 
//								continue;
//
//							WatchEvent<Path> ev = (WatchEvent<Path>) event;
//							Path filename = ev.context();
//							if(filename != null)
//							{
//								//		            	        	LOG.info("dir " + dir + ", filename : " + filename + " has changed " + kind);
//								if(kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE)
//									updateImageMetadata(dir, filename.toString());
//								else if(kind == StandardWatchEventKinds.ENTRY_DELETE)
//									removeImageMetadata(dir, filename.toString());
//							}
//							else
//								LOG.info("unable to retrieve information about the changed watch key while recursively watching directory: " + root.toString());
//						}
//						key.reset();   	
//
//				}
//				catch(ClosedWatchServiceException e)
//				{
//					return;
//				}
//				catch(Throwable e) 
//				{
//					e.printStackTrace();
//					LOG.info("disabling recursive watching of the directory: " + root.toString());
//					return;
//				}
//			}
//		}
//		.start();
//	}
}