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

package de.bwl.bwfla.imagearchive;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.logging.PrefixLoggerContext;
import de.bwl.bwfla.common.services.guacplay.replay.IWDMetaData;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.ImageIndex.Alias;
import de.bwl.bwfla.imagearchive.ImageIndex.ImageMetadata;
import de.bwl.bwfla.imagearchive.ImageIndex.ImageDescription;
import de.bwl.bwfla.imagearchive.ImageIndex.ImageNameIndex;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveBackendConfig;
import de.bwl.bwfla.imagearchive.datatypes.DefaultEnvironments;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata.ImageType;
import de.bwl.bwfla.imagearchive.datatypes.ImageImportResult;
import de.bwl.bwfla.imagearchive.generalization.ImageGeneralizationPatch;
import de.bwl.bwfla.imagearchive.tasks.CreateImageTask;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;


public class ImageArchiveBackend implements Comparable<ImageArchiveBackend>
{
	private final Logger log;

	private final ImageArchiveBackendConfig config;
	private final ImageMetadataCache cache;
	private final IWDArchive iwdArchive;
	private final ImageHandler imageHandler;
	private final Properties defaultEnvironments;


	public ImageArchiveBackend(ImageArchiveBackendConfig config) throws BWFLAException
	{
		final PrefixLoggerContext logctx = new PrefixLoggerContext();
		logctx.add("name", config.getName());

		this.log = new PrefixLogger(ImageArchiveBackend.class.getName(), logctx);
		this.config = config;
		this.cache = new ImageMetadataCache();
		this.iwdArchive = new IWDArchive(config);
		this.imageHandler = new ImageHandler(config, cache, log);
		this.defaultEnvironments = ImageArchiveBackend.loadProperties(config.getDefaultEnvironmentsPath());
	}

	public ImageArchiveBackendConfig getConfig()
	{
		return config;
	}

	public ImageNameIndex getNameIndexes()
	{
		return imageHandler.getNameIndexes();
	}

	public void addNameIndexesEntry(ImageMetadata entry, Alias alias) throws BWFLAException {
		imageHandler.addNameIndexesEntry(entry, alias);
	}

	public void updateLatestEmulator(String emulator, String version){
		imageHandler.updateLatestEmulator(emulator, version);
	}

	synchronized public void deleteTempEnvironments() throws BWFLAException
	{
		imageHandler.cleanTmpFiles();
	}

	synchronized public void reload()
	{
		log.info("Populating metadata-cache...");
		for (ImageType t : ImageType.values()) {
			cache.put(t, imageHandler.loadMetaData(t));
		}
	}

	public String getExportPrefix()
	{
		return this.imageHandler.getExportPrefix();
	}

	public String getEnvironmentById(String id) throws BWFLAException
	{
		Environment env = imageHandler.getEnvById(id);
		return env == null ? null : env.toString();
	}

	public List<String> getEnvironments(String type) throws BWFLAException
	{
		List<String> images = new ArrayList<String>();

		if (type == null) // get all environments
		{
			for (ImageType t : ImageType.values()) {
				if (t.equals(ImageType.template))
					continue;

				if (t.equals(ImageType.patches))
					continue;

				if (t.equals(ImageType.tmp))
					continue;

				if (t.equals(ImageType.sessions))
					continue;

				Map<String, Environment> iMap = cache.get(t);
				if (iMap == null)
					continue;
				for (Environment env : iMap.values())
					images.add(env.toString());
			}
			return images;
		}
		// type not null...
		try {
			Map<String, Environment> iMap = cache.get(ImageType.valueOf(type));
			if (iMap == null)
				return images;

			for (Environment env : iMap.values()) {
				images.add(env.toString());
			}
		}
		catch (IllegalArgumentException e) {
			throw new BWFLAException("client has specified an illegal argument as an image type: " + type, e);
		}
		return images;
	}

	public boolean deleteImage(String imageId, String type) throws BWFLAException
	{
		return imageHandler.deleteImage(imageId, type);
	}

	public boolean deleteMetadata(String envId) throws BWFLAException
	{
		return imageHandler.deleteMetaData(envId);
	}

	public TaskState importImageFromUrlAsync(URL url, ImageArchiveMetadata request) throws BWFLAException
	{
		if (url == null) {
			throw new BWFLAException("image data handler is null, aborting");
		}

		try {
			return imageHandler.importImageUrlAsync(url, request, true);
		}
		catch (IOException e) {
			throw new BWFLAException(e);
		}
	}

	@Deprecated
	public String importImageFromUrl(URL url, ImageArchiveMetadata request) throws BWFLAException
	{
		if (url == null) {
			throw new BWFLAException("image data handler is null, aborting");
		}

		try {
			return imageHandler.importImageUrl(url, request, true);
		}
		catch (IOException e) {
			throw new BWFLAException(e);
		}
	}

	public TaskState importImageAsStreamAsync(DataHandler image, ImageArchiveMetadata iaMd) throws BWFLAException
	{
		if (image == null) {
			throw new BWFLAException("image data handler is null, aborting");
		}

		return imageHandler.importImageStreamAsync(image, iaMd);
	}

	@Deprecated
	public String importImageAsStream(DataHandler image, ImageArchiveMetadata iaMd) throws BWFLAException
	{
		if (image == null) {
			throw new BWFLAException("image data handler is null, aborting");
		}

		return imageHandler.importImageStream(image, iaMd);
	}

	public ImageImportResult getImageImportResult(String sessionId) throws BWFLAException
	{
		return imageHandler.getImageImportResult(sessionId);
	}

	public String createPatchedImage(String imageId, ImageType type, ImageGeneralizationPatch patch) throws BWFLAException
	{
		if (patch == null)
			throw new BWFLAException("Requested patch was not found!");

		try {
			final String cowId = UUID.randomUUID().toString();
			return imageHandler.createPatchedImage(imageId, cowId, type.name(), patch);
		}
		catch (IOException e) {
			throw new BWFLAException(e);
		}
	}

	public void extractMetadata(String imageId) throws BWFLAException {
		imageHandler.extractMetadata(imageId);
	}

	public String createImage(String size, String type) throws BWFLAException
	{
		String id = UUID.randomUUID().toString();
		File target = imageHandler.getImageTargetPath(type);
		File destImgFile = new File(target, id);
		QcowOptions qcowOptions = new QcowOptions();
		qcowOptions.setSize(size);
		EmulatorUtils.createCowFile(destImgFile.toPath(), qcowOptions);
		return id;
	}

	public TaskState createImage(String size, ImageType type, ImageMetadata md) throws BWFLAException
	{
		File target = imageHandler.getImageTargetPath(type.name());
		CreateImageTask t = new CreateImageTask(target.toPath(), size);
		if(md != null)
			t.setMetadata(imageHandler.getNameIndexes(), md);
		return ImageArchiveRegistry.submitTask(t);
	}

	public void updateConfiguration(String conf) throws BWFLAException
	{
		if (conf == null) {
			throw new BWFLAException("image configuration data is null, aborting");
		}

		Environment emuEnv = null;
		try {
			emuEnv = Environment.fromValue(conf);
		}
		catch (Throwable t) {
			log.info(conf);
			throw new BWFLAException("updateConfiguration: failed to parse environment", t);
		}
		ImageType t = imageHandler.getImageType(emuEnv.getId());
		if (t == null)
			throw new BWFLAException("updateConfiguration: unknown environment : " + emuEnv.getId() + "\n" + conf);

		if (imageHandler.writeMetaData(conf, emuEnv.getId(), t.name(), true)) {
			imageHandler.addCachedEnvironment(t, emuEnv.getId(), emuEnv);
		}
	}

	public void importConfiguration(String conf, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException
	{
		if (conf == null) {
			throw new BWFLAException("image configuration data is null, aborting");
		}

		Environment emuEnv = null;
		try {
			emuEnv = Environment.fromValue(conf);
		}
		catch (Throwable t) {
			log.info(conf);
			throw new BWFLAException("importConfiguration: failed to parse environment", t);
		}

		if (imageHandler.writeMetaData(conf, emuEnv.getId(), iaMd.getType().name(), deleteIfExists)) {
			imageHandler.addCachedEnvironment(iaMd.getType(), emuEnv.getId(), emuEnv);
		}
	}

	@Deprecated
	public void commitTempEnvironment(String id) throws BWFLAException
	{
		try {
			imageHandler.commitTempEnvironment(id);
		}
		catch (IOException e) {
			throw new BWFLAException("commit tmp image: ", e);
		}
	}

	@Deprecated
	public void commitTempEnvironmentWithCustomType(String id, String type) throws BWFLAException
	{
		try {
			imageHandler.commitTempEnvironment(id, type);
		}
		catch (IOException e) {
			throw new BWFLAException("commit tmp image: ", e);
		}
	}

	public String getRecording(String envId, String traceId) throws BWFLAException
	{
		return iwdArchive.getRecording(envId, traceId);
	}

	public List<IWDMetaData> getRecordings(String envId) throws BWFLAException
	{
		return iwdArchive.getRecordings(envId);
	}

	public boolean addRecordingFile(String envId, String traceId, String data) throws BWFLAException
	{
		return iwdArchive.addRecordingFile(envId, traceId, data);
	}

	public String getDefaultEnvironment(String osId)
	{
		synchronized (defaultEnvironments) {
			return defaultEnvironments.getProperty(osId);
		}
	}

	public DefaultEnvironments getDefaultEnvironments()
	{
		synchronized (defaultEnvironments) {
			Properties defaults = defaultEnvironments;
			List<DefaultEnvironments.DefaultEntry> map = new ArrayList<>();

			Enumeration<?> enumeration = defaults.propertyNames();
			while (enumeration.hasMoreElements()) {
				String k = (String) enumeration.nextElement();
				DefaultEnvironments.DefaultEntry e = new DefaultEnvironments.DefaultEntry();
				e.setKey(k);
				e.setValue(defaults.getProperty(k));
				map.add(e);
			}
			DefaultEnvironments response = new DefaultEnvironments();
			response.setMap(map);
			return response;
		}
	}

	public synchronized void setDefaultEnvironment(String osId, String envId) throws BWFLAException
	{
		synchronized (defaultEnvironments) {
			defaultEnvironments.setProperty(osId, envId);
			if (config.getDefaultEnvironmentsPath() == null)
				return;

			try {
				try (OutputStream outstream = new FileOutputStream(config.getDefaultEnvironmentsPath())) {
					defaultEnvironments.store(outstream, null);
					log.info("List of default environments updated!");
				}
			}
			catch (Exception error) {
				throw new BWFLAException("Updating default environments failed!", error);
			}
		}
	}

	public String getImageBinding(String name, String version) throws BWFLAException
	{
		final ImageArchiveBinding binding = imageHandler.getImageBinding(name, version);
		try {
			return (binding != null) ? binding.value() : null;
		}
		catch (Exception error) {
			throw new BWFLAException(error);
		}
	}

	public List<TaskState> replicateImagesAsync(List<String> images)
	{
		return imageHandler.replicateImagesAsync(images);
	}

	@Deprecated
	public List<String> replicateImages(List<String> images)
	{
		return imageHandler.replicateImages(images);
	}

	public File getMetaDataTargetPath(String type)
	{
		return imageHandler.getMetaDataTargetPath(type);
	}

	private static Properties loadProperties(File source) throws BWFLAException
	{
		final Properties properties = new Properties();
		if (source.exists()) {
			try {
				try (InputStream input = new FileInputStream(source)) {
					properties.load(input);
				}
			}
			catch (Exception error) {
				throw new BWFLAException("Loading properties failed!", error);
			}
		}

		return properties;
	}

	private static int compare(int a, int b) {
		return a > b ? +1 : a < b ? -1 : 0;
	}

	@Override
	public int compareTo(ImageArchiveBackend backend) {
		return compare(getConfig().getOrder(), backend.getConfig().getOrder());
	}

    public void deleteNameIndexesEntry(String id, String version) {
		imageHandler.deleteNameIndexesEntry(id, version);
    }
}
