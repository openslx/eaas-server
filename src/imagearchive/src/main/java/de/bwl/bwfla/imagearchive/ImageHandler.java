package de.bwl.bwfla.imagearchive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import de.bwl.bwfla.common.services.handle.HandleClient;
import de.bwl.bwfla.common.services.handle.HandleException;
import de.bwl.bwfla.common.services.handle.HandleUtils;
import de.bwl.bwfla.common.utils.*;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata;
import de.bwl.bwfla.imagearchive.datatypes.ImageExport;
import de.bwl.bwfla.imagearchive.datatypes.ImageImportResult;
import de.bwl.bwfla.imagearchive.generalization.ImageGeneralizer;
import org.apache.commons.io.FileUtils;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ImageInformation.QemuImageFormat;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveConfig;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata.ImageType;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveSingleton;


public class ImageHandler {

	protected static final Logger log = Logger.getLogger(ImageHandler.class.getName());
	static ImageArchiveConfig iaConfig;

	HashMap<String, FutureTask<ImageLoaderResult>> importTasks = new HashMap<>();

	private final HandleClient handleClient;
	private final ImageNameIndex imageNameIndex;
	private final ExecutorService pool;

	enum ExportType {
		NBD, HTTP
	}

	public ImageHandler(ImageArchiveConfig conf) throws BWFLAException {
		iaConfig = conf;
		pool = Executors.newFixedThreadPool(20);
		this.imageNameIndex = new ImageNameIndex();
		this.handleClient = (conf.isHandleConfigured()) ? new HandleClient() : null;

		cleanTmpFiles();
		resolveLocalBackingFiles();
	}


	static private String getBackingImageId(String bf)
	{
		if (bf.contains("exportname")) {
			return bf.substring(bf.lastIndexOf('=') + 1);
		}
		else if (bf.startsWith("http")) {
			return bf.substring(bf.lastIndexOf('/') + 1);
		}
		else return null;
	}

	private static String getArchivePrefix()
	{
		String prefix = null;
		if (iaConfig.nbdPrefix != null && !iaConfig.nbdPrefix.isEmpty()) {
			prefix = iaConfig.nbdPrefix;

		} else if (iaConfig.httpPrefix != null && !iaConfig.httpPrefix.isEmpty()) {
			prefix = iaConfig.httpPrefix;
			// make sure there's a trailing slash in the URL base
			if (!prefix.endsWith("/")) {
				prefix += "/";
			}
		}
		return prefix;
	}

	public static String getExportPrefix()
	{
		if (iaConfig.isHandleConfigured())
			return "http://hdl.handle.net/" + iaConfig.handlePrefix + "/";

		return ImageHandler.getArchivePrefix();
	}

	private static ImageExport.ImageFileInfo getDependency(ImageType parentType, String parentId) throws IOException, BWFLAException {

		File f = new File(ImageArchiveSingleton.iaConfig.imagePath + "/" + parentType.name() + "/" + parentId);
		if(!f.exists())
			throw new BWFLAException("parent file not found. broken parameters");

		ImageInformation info = new ImageInformation(f.getAbsolutePath());
		if (info.getBackingFile() == null)
			return null;

		String id = getBackingImageId(info.getBackingFile());
		if (id == null)
			return null;

		File file = null;
		ImageType type = null;
		for(ImageType _type : ImageType.values()) {
			File backing = new File(ImageArchiveSingleton.iaConfig.imagePath + "/" + _type.name() + "/" + id);

			if(backing.exists()) {
				file = backing;
				type = _type;
			}
		}
		if(file == null)
			return null;

		DataHandler fileHandle = new DataHandler(new FileDataSource(file));
		return new ImageExport.ImageFileInfo(fileHandle, id, type);
	}

	private static void resolveLocalBackingFile(File f)
	{
		try {
			ImageInformation info = new ImageInformation(f.getAbsolutePath());
			if (info.getBackingFile() == null)
				return;

			log.info(f.getAbsolutePath() + " got backing file: " + info.getBackingFile());

			String id = getBackingImageId(info.getBackingFile());
			if (id == null)
				return;
			log.info(" got id: " + id);

			File tmpTarget = getImageTargetPath(ImageType.tmp.name());
			File tmpImageFile = new File(tmpTarget, id);
			String newFileId = null;
			if(tmpImageFile.exists())
			{
				MachineConfiguration mc = getEnvByImageId(ImageType.tmp, id);
				id = commitTempEnvironment(mc.getId());
				// log.info("commited mc: " + mc.getId() + " got new FileId: " + id);
			}

			boolean hasLocalBackingfile = false;
			for(ImageType _type : ImageType.values()) {
				File backing = new File(ImageArchiveSingleton.iaConfig.imagePath + "/" + _type.name() + "/" + id);

				if(backing.exists()) {
					hasLocalBackingfile = true;
					break;
				}
			}

			if(!hasLocalBackingfile)
				return;

			String newBackingFile = getExportPrefix() + id;
			log.info("rebase " + f.getAbsolutePath() + " to: " + newBackingFile);
			EmulatorUtils.changeBackingFile(f.toPath(), newBackingFile, log);

		} catch (IOException|BWFLAException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private void resolveLocalBackingFiles()
	{
		for(ImageType type : ImageType.values()) {
			File dir = new File(ImageArchiveSingleton.iaConfig.imagePath + "/" + type.name());
			if(!dir.exists())
				continue;

			File[] files = dir.listFiles();
			if(files == null)
				continue;

			for (final File fileEntry : files) {
				if (fileEntry.isDirectory())
					continue;

				if (fileEntry.getName().startsWith(".fuse"))
					continue;

				resolveLocalBackingFile(fileEntry);
				if (handleClient != null) {
					final String imgname = fileEntry.getName();
					try {
						this.createOrUpdateHandle(imgname);
					}
					catch (Exception error) {
						log.log(Level.WARNING, "Registering image '" + imgname + "' failed!", error);
					}
				}
			}
		}
	}

	private static List<ImageExport.ImageFileInfo> processBindingForExport(ImageArchiveBinding iab) throws BWFLAException, IOException {
		List<ImageExport.ImageFileInfo> fileInfos = new ArrayList<>();

		File target = getImageTargetPath(iab.getType());
		if(target == null)
			throw new BWFLAException("getImageExportData: inconsistent metadata: " + target.getAbsolutePath() + " type " + iab.getType());
		File imageFile = new File(target, iab.getImageId());
		if(!imageFile.exists())
			throw new BWFLAException("getImageExportData: inconsistent metadata " + target.getAbsolutePath() + " not found.");

		DataHandler fileHandle = new DataHandler(new FileDataSource(imageFile));
		ImageExport.ImageFileInfo info = new ImageExport.ImageFileInfo(fileHandle, iab.getImageId(), ImageType.valueOf(iab.getType().toLowerCase()));

		fileInfos.add(info);

		ImageExport.ImageFileInfo parent = info;
		while((parent = getDependency(parent.getType(), parent.getId())) != null)
		{
			fileInfos.add(parent);
		}

		return fileInfos;
	}

	ImageExport getImageExportData(String envId) throws BWFLAException, IOException {
		Environment env = getEnvById(envId);
		if(env == null)
			return null;

		MachineConfiguration mc = (MachineConfiguration) env;

		ImageExport export = new ImageExport();
		List<ImageExport.ImageFileInfo> fileInfos = new ArrayList<>();

		ImageArchiveBinding iab = null;
		for (AbstractDataResource b : mc.getAbstractDataResource()) {
			if (b instanceof ImageArchiveBinding) {
				iab = (ImageArchiveBinding) b;
				fileInfos.addAll(processBindingForExport(iab));
			}
		}

		export.setImageFiles(fileInfos);
		return export;
	}
	
	private static void deleteDirectory(File dir) throws IOException
	{
		if(!dir.exists())
			return;
		
		String[]entries = dir.list();
		for(String s: entries){
		    File currentFile = new File(dir.getPath(), s);
		    currentFile.delete();
		}
		Files.deleteIfExists(dir.toPath());
	}

	public void cleanTmpFiles()
	{
		File tmpEnvFile = new File(iaConfig.metaDataPath, "tmp");
		try {
			deleteDirectory(tmpEnvFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		tmpEnvFile.mkdir();
		
		File tmpImgFile = new File(iaConfig.imagePath, "tmp");
		try {
			deleteDirectory(tmpImgFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		tmpImgFile.mkdir();
	
		ConcurrentHashMap<String, Environment> map = ImageArchiveSingleton.imagesCache.get(ImageType.valueOf("tmp"));
		if(map == null)
			ImageArchiveSingleton.imagesCache.put(ImageType.valueOf("tmp"), new ConcurrentHashMap<String, Environment>());
		else
			map.clear();
	}
	
	protected static File getImageTargetPath(String type) {
		if(type == null)
			return  null;
		
		File target = new File(iaConfig.imagePath, type);
		if (target.isDirectory())
			return target;
		else
			return null;
	}

	public static File getMetaDataTargetPath(String type) {
		File target = new File(iaConfig.metaDataPath, type);
		if (target.isDirectory())
			return target;
		else
			return null;
	}
	
	synchronized static void removeCachedEnv(ImageType type, String id) throws BWFLAException
	{
		ConcurrentHashMap<String, Environment> map = ImageArchiveSingleton.imagesCache.get(type);
		if(map == null)
			throw new BWFLAException("map for image type :" + type + " not found");
		
		map.remove(id);		
	}
	
	synchronized static void addCachedEnvironment(ImageType type, String id, Environment env) throws BWFLAException
	{
		ConcurrentHashMap<String, Environment> map = ImageArchiveSingleton.imagesCache.get(type);
		if(map == null)
		{
			throw new BWFLAException("map for image type : -" + type + "- not found");
		}
		map.put(id, env);		
	}

	String importImageUrl(URL url, ImageArchiveMetadata iaMd, boolean delete) throws BWFLAException, IOException {
		
		File target = getImageTargetPath(iaMd.getType().name());
		String importId = UUID.randomUUID().toString();

		File destImgFile = new File(target, importId);

		if (destImgFile.exists()) {
			if (!delete) {
				throw new BWFLAException("the following file already exists, will not overwrite: " + destImgFile.getAbsolutePath());

			} else
				destImgFile.delete();
		}

		String uuid = UUID.randomUUID().toString();
		FutureTask<ImageLoaderResult> ft =  new FutureTask<ImageLoaderResult>(new ImageLoader(url, target, importId, this));
		importTasks.put(uuid, ft);
		pool.submit(ft);
		return uuid;
	}

	String importImageStream(DataHandler image, ImageArchiveMetadata iaMd) throws BWFLAException {
		File target = getImageTargetPath(iaMd.getType().name());

		String importId;
		if(iaMd.getImageId() == null)
			importId = UUID.randomUUID().toString();
		else
			importId = iaMd.getImageId();

		File destImgFile = new File(target, importId);
		if (destImgFile.exists()) {
			if (!iaMd.isDeleteIfExists()) {
				throw new BWFLAException("the following file already exists, will not overwrite: " + destImgFile.getAbsolutePath());
			} else
				destImgFile.delete();
		}

		String taskId = UUID.randomUUID().toString();
		try
		{
			InputStream inputStream = image.getInputStream();
			DataUtil.writeData(inputStream, destImgFile);
			ImageHandler.resolveLocalBackingFile(destImgFile);

			FutureTask<ImageLoaderResult> ft =  new FutureTask<ImageLoaderResult>(new ImageLoader(inputStream, target, importId, this));
			importTasks.put(taskId, ft);
			pool.submit(ft);
			return taskId;
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
			throw new BWFLAException(" image getInputStream: " + e);
		}
	}

	public ImageImportResult getImageImportResult(String session) throws BWFLAException {
		FutureTask<ImageLoaderResult> ft = importTasks.get(session);
		if(ft.isDone())
		{
			importTasks.remove(session);
			try {
				ImageLoaderResult res = ft.get();
				if(!res.success)
					throw new BWFLAException(res.message);
				return new ImageImportResult(ImageHandler.getExportPrefix(), res.id);
			} catch (InterruptedException|ExecutionException e) {
				throw new BWFLAException(e);
			}
		}
		return null;
	}

	boolean deleteImage(String imageId, String type)
	{
		File target = getImageTargetPath(type);
		File destImgFile = new File(target, imageId);
		if(!destImgFile.exists())
		{
			log.severe("image: " + destImgFile + " does not exist");
			return false;
		}
		return destImgFile.delete();
	}

	static boolean writeMetaData(String conf, String id, String type, boolean delete) {
		File metaDataDir = new File(iaConfig.metaDataPath, type);
		if (!metaDataDir.isDirectory())
			return false;

		String confFullName = id + ".xml";

		File destConfFile = new File(metaDataDir + File.separator + confFullName);
		if (destConfFile.exists()) {
			if (!delete) {
				log.severe("the following file already exists, will not overwrite: " + destConfFile.getAbsolutePath());
				return false;
			} else
				destConfFile.delete();
		}
		return DataUtil.writeString(conf, destConfFile);
	}

	static boolean deleteMetaData(String id) throws BWFLAException
	{
		log.info("deleting: " + id);
		for(ImageType imageType : ImageType.values())
		{
			File path = getMetaDataTargetPath(imageType.toString());

			if (path == null || !path.exists() || !path.isDirectory())
				continue;

			for (final File fileEntry : path.listFiles()) {
				if (fileEntry.isDirectory())
					continue;
			
				String env = loadMetaDataFile(fileEntry);
				if(env == null)
					continue;
				
				Environment emuEnv = null;
				try {
					emuEnv = Environment.fromValue(env);
				} catch (Throwable t) {
					continue;
				}
				if(emuEnv.getId().equals(id))
				{
					boolean ret = fileEntry.delete();
					if(ret)
						removeCachedEnv(imageType, id);
					
					log.info("deleting envId success: " + ret);
					return ret;
				}
			}
		}
		return false;
	}

	public static String loadMetaDataFile(File mf) {
		
		if(mf.getName().startsWith(".fuse"))
		{
			log.warning("found fuse hidden file, skipping");
			return null;
		}
		
		String env;
		try {
			env = FileUtils.readFileToString(mf, "UTF-8");
		} catch (IOException e) {
			log.info("failed loading " + mf + " - " + e.getMessage());
			return null;
		}

		return env;
	}

	protected final static String IA_URI_SCHEME = "imagearchive";

	private Environment migrateEnvironment(Environment env, ImageType t) {
		if (!(env instanceof MachineConfiguration))
			return env;

		MachineConfiguration conf = (MachineConfiguration) env;
		conf.setMetaDataVersion("1");

		ArrayList<AbstractDataResource> addList = new ArrayList<AbstractDataResource>();
		Iterator<AbstractDataResource> iter = conf.getAbstractDataResource().iterator();
		while (iter.hasNext()) {
			AbstractDataResource r = iter.next();
			
			if (!(r instanceof Binding))
				continue;

			Binding b = (Binding) r;
			URI uri = null;
			try {
				uri = new URI(b.getUrl());
			} catch (URISyntaxException e) {
				log.info("invalid uri: " + b.getUrl() + " " + e.getMessage());
				continue;
			}
	
			if (uri.isOpaque() && uri.getScheme().equalsIgnoreCase(IA_URI_SCHEME)) {
				ImageArchiveBinding iaBinding = new ImageArchiveBinding("", uri.getSchemeSpecificPart(), t.toString());
				iaBinding.setId(b.getId());
				addList.add(iaBinding);
				iter.remove();	
			}
		}
		conf.getAbstractDataResource().addAll(addList);
	//	log.info(conf.toString());
		return conf;
	}

	public ConcurrentHashMap<String, Environment> loadMetaData(ImageArchiveMetadata.ImageType imageType) {

		File path = getMetaDataTargetPath(imageType.toString());
		log.info("loading metadata for type: " + imageType);
		ConcurrentHashMap<String, Environment> md = new ConcurrentHashMap<String, Environment>();
		if (path == null || !path.exists() || !path.isDirectory()) {
			log.info("path " + path + " not a valid meta-data directory");
			return md;
		}

		for (final File fileEntry : path.listFiles()) {
			if (fileEntry.isDirectory())
				continue;
			
			if(fileEntry.getName().startsWith(".fuse"))
				continue;
			
			// log.info("loading: " + fileEntry);

			String env;
			if ((env = loadMetaDataFile(fileEntry)) != null) {
				Environment emuEnv = null;
				try {
					log.info("env \n" + env);
					emuEnv = Environment.fromValue(env);
				} catch (Throwable t) {
					log.info("loadMetadata: failed to parse environment: " + t.getMessage());
					log.log(Level.WARNING, t.getMessage(), t);
					continue;
				}
				if (emuEnv.getMetaDataVersion() == null) {
					emuEnv = migrateEnvironment(emuEnv, imageType);
					emuEnv.setMetaDataVersion("1");
					fileEntry.delete();
					writeMetaData(emuEnv.toString(), emuEnv.getId(), imageType.toString(), true);
				}
				md.put(emuEnv.getId(), emuEnv);
			}
		}
		return md;
	}

	protected static Environment getEnvById(String id) {
		if(id == null)
			return null;
		for (ImageType t : ImageType.values()) {
			ConcurrentHashMap<String, Environment> map = ImageArchiveSingleton.imagesCache.get(t);
			if (map == null)
				continue;
			// log.info("map " + t.toString() + " entries # " + map.size());
			// log.info(map.keySet().toString());
			Environment env = map.get(id);
			if (env != null)
			{
				return env;
			}
		}
		return null;
	}

	public static ImageType getImageType(String id)
	{
		if(id == null)
			return null;
		for (ImageType t : ImageType.values()) {
			ConcurrentHashMap<String, Environment> map = ImageArchiveSingleton.imagesCache.get(t);
			if (map == null)
				continue;

			if(map.get(id) != null)
				return t;
		}
		return null;
	}

	public ImageArchiveBinding getImageBinding(String name)
	{
		return this.getImageBinding(name, null);
	}

	public ImageArchiveBinding getImageBinding(String name, String version)
	{
		if (name == null || name.isEmpty())
			return null;

		final ImageNameIndex.Entry entry = imageNameIndex.get(name, version);
		if (entry == null)
			return null;

		final ImageNameIndex.ImageDescription image = entry.image();
		final ImageArchiveBinding binding = new ImageArchiveBinding();
		binding.setUrlPrefix((image.url() != null) ? image.url() : ImageHandler.getExportPrefix());
		binding.setAccess(Binding.AccessType.COW);
		binding.setFileSystemType(image.fstype());
		binding.setType(image.type());
		binding.setImageId(image.id());
		binding.setUrl("");
		return binding;
	}

	private static MachineConfiguration getEnvByImageId(ImageType t, String id)
	{
		ConcurrentHashMap<String, Environment> map = ImageArchiveSingleton.imagesCache.get(t);
		for(String key : map.keySet()) {
			Environment env = map.get(key);
			if (!(env instanceof MachineConfiguration))
				continue;

			MachineConfiguration mc = (MachineConfiguration) env;

			for (AbstractDataResource b : mc.getAbstractDataResource()) {
				if (b instanceof ImageArchiveBinding && b.getId().equals("main_hdd")) {
					ImageArchiveBinding iab = (ImageArchiveBinding) b;
					if (iab.getImageId().equals(id))
						return mc;
				}
			}
		}
		return null;
	}

	static String commitTempEnvironment(String id, String type, ImageArchiveMetadata.ImageType imageType ) throws IOException, BWFLAException {


		Environment env = getEnvById(id);
		List<AbstractDataResource> abstractDataResources = null;

		if (env == null) {
			throw new BWFLAException("cannot commit environment " + id + ": not found");
		}

		if (env instanceof MachineConfiguration)
			abstractDataResources = ((MachineConfiguration) env).getAbstractDataResource();
		else if (env instanceof ContainerConfiguration)
			abstractDataResources = ((ContainerConfiguration) env).getDataResources();

		if (abstractDataResources == null) {
			throw new BWFLAException("cannot commit environment: environment type is unknown ");
		}


			log.info("commiting env id: " + id);

		File srcImgDir = getImageTargetPath("tmp");
		File dstImgDir = getImageTargetPath(type);
		File srcEnvDir = getMetaDataTargetPath("tmp");
		File dstEnvDir = getMetaDataTargetPath(type);
		
		if(srcImgDir == null || dstImgDir == null || srcEnvDir == null || dstEnvDir == null)
		{
			throw new BWFLAException("cannot commit environment " + id + ": invalid src/dst path");
		}

		String newImageId = UUID.randomUUID().toString() + String.valueOf(System.currentTimeMillis()).substring(0, 2);
		for(AbstractDataResource b : abstractDataResources)
		{
			if(b instanceof ImageArchiveBinding && (b.getId().equals("main_hdd") || b.getId().equals("rootfs")))
			{
				ImageArchiveBinding iab = (ImageArchiveBinding)b;
				File srcImgFile = new File(srcImgDir, iab.getImageId());
				if(!srcImgFile.exists())
				{
					throw new BWFLAException("cannot commit environment " + id + ": src file not found " + srcImgFile);
				}

				File destImgFile = new File(dstImgDir, newImageId);
				log.info("move " + srcImgFile + " " + destImgFile);
				if(!srcImgFile.renameTo(destImgFile))
				{
					throw new BWFLAException("cannot commit environment " + id + ": src file not found " + srcImgFile);
				}
				iab.setImageId(newImageId);
				iab.setType(type);

				updateTmpBackingFiles(destImgFile.getAbsolutePath(), dstImgDir);
				break;
			}
		}
		removeCachedEnv(ImageType.tmp, id);
		deleteMetaData(id);
		writeMetaData(env.toString(), id, type, true);
		addCachedEnvironment(imageType, id, env);
		return newImageId;
	}

	static String commitTempEnvironment(String id ) throws IOException, BWFLAException {
		return commitTempEnvironment(id, "user", ImageType.user);
	}

	static String commitTempEnvironment(String id, String type) throws IOException, BWFLAException {
		return commitTempEnvironment(id, type, ImageType.valueOf(type));
	}

	private static void updateTmpBackingFiles(String image, File target) throws IOException, BWFLAException {
		ImageInformation info = new ImageInformation(image);

		if(info.getBackingFile() == null)
			return;

		String id = getBackingImageId(info.getBackingFile());
		if(id == null)
			return;

		log.info("update image: got id: " + id);
		File backing = null;

		for(ImageType _type : ImageType.values()) {
			backing = new File(ImageArchiveSingleton.iaConfig.imagePath + "/" + _type.name() + "/" + id);
			if(backing.exists()) {
				break;
			}
			else backing = null;
		}

		if(backing == null)
			return;

		String newImageId = UUID.randomUUID().toString() + String.valueOf(System.currentTimeMillis()).substring(0, 2);
		String newBackingFile = getExportPrefix() + newImageId;

		File destImgFile = new File(target, newImageId);
		backing.renameTo(destImgFile);

		log.info("rebase " + image + " to: " + newBackingFile);
		EmulatorUtils.changeBackingFile(new File(image).toPath(), newBackingFile, log);

		updateTmpBackingFiles(destImgFile.getAbsolutePath(), target);
	}

	public ConcurrentHashMap<String, Environment> getImages(String type) {
		try {
			ImageType t = ImageType.valueOf(type);
			File dir = new File(iaConfig.metaDataPath, t.name());
			if (!dir.exists()) {
				log.info("dir not found: " + dir);
				return new ConcurrentHashMap<String, Environment>();
			}
			return loadMetaData(t);
		} catch (IllegalArgumentException e) {
			log.warning("unknown ImageType " + e.getMessage());
			return new ConcurrentHashMap<String, Environment>();
		}
	}

	/**
	 * Asynchronously replicates specified images by importing them into this image archive.
	 * @param images A list of source URLs for images to import.
	 * @return A list of import-task IDs, one for each image to import.
	 * @see #getImageImportResult(String)
	 */
	public List<String> replicateImages(List<String> images) {
		final List<String> taskids = new ArrayList<String>(images.size());
		images.forEach((urlstr) -> {
			try {
				final URL url = new URL(urlstr);
				final String urlpath = url.getPath();
				final String imageid = urlpath.substring(urlpath.lastIndexOf("/") + 1);
				final ImageArchiveMetadata metadata = new ImageArchiveMetadata(ImageType.base);
				metadata.setDeleteIfExists(false);
				metadata.setImageId(imageid);

				taskids.add(this.importImageUrl(url, metadata, metadata.isDeleteIfExists()));
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Preparing image-import from URL failed!", error);
			}
		});

		return taskids;
	}

	protected void createPatchedCow(String parentId, String cowId, String templateId, String type) throws IOException, BWFLAException {
		String newBackingFile = getExportPrefix() + parentId;
		File target = getImageTargetPath(type);
		File destImgFile = new File(target, cowId);

		EmulatorUtils.createCowFile(newBackingFile, destImgFile.toPath());
		MachineConfigurationTemplate tempEnv = (MachineConfigurationTemplate) getEnvById(templateId);
		ImageGeneralizer.applyScriptIfCompatible(destImgFile, tempEnv.copy());
	}

	private void createOrUpdateHandle(String imageId) throws BWFLAException
	{
		final String url = ImageHandler.getArchivePrefix() + imageId;
		try {
			log.info("Trying to create new handle for image '" + imageId + "'...");
			handleClient.create(imageId, url);
			log.info("New handle for image '" + imageId + "' created");
			return;
		}
		catch (HandleException error) {
			if (error.getErrorCode() != HandleException.ErrorCode.HANDLE_ALREADY_EXISTS) {
				log.log(Level.WARNING, "Creating new handle for image '" + imageId + "' failed! ", error);
				throw error;
			}
		}

		try {
			log.info("Handle already exists! Trying to add new URL for image '" + imageId + "'...");
			handleClient.add(imageId, url);
			log.info("URL added to exisiting handle for image '" + imageId + "'");
			return;
		}
		catch (HandleException error) {
			if (error.getErrorCode() != HandleException.ErrorCode.INVALID_VALUE) {
				log.log(Level.WARNING, "Adding new URL to exisiting handle for image '" + imageId + "' failed!", error);
				throw error;
			}
		}

		log.info("URL-entry already exists! Trying to update URL for image '" + imageId + "'...");
		handleClient.update(imageId, url);
		log.info("URL updated in exisiting handle for image '" + imageId + "'");
	}

	private static class ImageLoaderResult
	{
		boolean success;
		String message;
		String id;

		ImageLoaderResult(String id)
		{
			this.success = true;
			this.message = "success";
			this.id = id;
		}

		ImageLoaderResult(boolean success, String message)
		{
			this.success = success;
			this.message = message;
		}
	}

	private static class ImageLoader implements Callable<ImageLoaderResult>
	{
		private File destImgFile;
		private URL url;
		private File target;
		private String importId;
		private ImportType type;
		private InputStream inputStream;

		private final ImageHandler imageHandler;

		private enum ImportType {
			URL,
			STREAM
		};



		public ImageLoader(InputStream inputStream, File target, String importId, ImageHandler imageHandler)  {

			this.imageHandler = imageHandler;
			this.inputStream = inputStream;

			this.importId = importId;
			this.target = target;
			destImgFile = new File(target, importId);

			type = ImportType.STREAM;
		}

		public ImageLoader(URL url, File target, String importId, ImageHandler imageHandler)
		{
			type = ImportType.URL;

			this.url = url;
			this.imageHandler = imageHandler;
			this.target = target;
			this.importId = importId;
			destImgFile = new File(target, importId);
		}

		private ImageLoaderResult fromStream()
		{
			if (imageHandler.handleClient != null) {
				try {
					imageHandler.createOrUpdateHandle(importId);
				}
				catch (BWFLAException error) {
					return new ImageLoaderResult(false, error.getMessage());
				}
			}

			return new ImageLoaderResult(importId);
		}

		private ImageLoaderResult fromUrl()
		{
			try {
				Binding b = new Binding();
				b.setUrl(url.toString());
				EmulatorUtils.copyRemoteUrl(b, destImgFile.toPath(), null);

				QemuImageFormat fmt = EmulatorUtils.getImageFormat(destImgFile.toPath(), log);
				if (fmt == null) {
					throw new BWFLAException("could not determine file fmt");
				}
				switch (fmt) {
					case VMDK:
					case VHD:
						File convertedImgFile = new File(target, "convertTmp");
						destImgFile.renameTo(convertedImgFile);
						File outFile = new File(target, importId);
						EmulatorUtils.convertImage(convertedImgFile.toPath(), outFile.toPath(), ImageInformation.QemuImageFormat.QCOW2, log);
						convertedImgFile.delete();
					default:
						ImageHandler.resolveLocalBackingFile(destImgFile);
						if (imageHandler.handleClient != null)
							imageHandler.createOrUpdateHandle(importId);

						return new ImageLoaderResult(importId);
				}
			} catch (Exception e1) {
				log.log(Level.WARNING, e1.getMessage(), e1);
				return new ImageLoaderResult(false, "failed moving incoming image to " + destImgFile + " reason " + e1.getMessage());

			}
		}

		@Override
		public ImageLoaderResult call() {
			switch(type) {
				case URL:
					return fromUrl();
				case STREAM:
					return fromStream();
				default:
					return new ImageLoaderResult(false, "");
			}
		}
	}
}
