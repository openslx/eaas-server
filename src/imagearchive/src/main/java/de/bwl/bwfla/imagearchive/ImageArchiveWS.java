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

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.imagearchive.datatypes.ImageImportResult;
import org.jboss.ejb3.annotation.TransactionTimeout;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.guacplay.replay.IWDMetaData;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.Environment;
import de.bwl.bwfla.emucomp.api.MachineConfigurationTemplate;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveConfig;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata.ImageType;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveSingleton;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata;
import de.bwl.bwfla.imagearchive.datatypes.ImageExport;


/**
 * @author Isgandar Valizada, bwFLA project, University of Freiburg, Germany
 * 
 */
@Stateless
@MTOM
@WebService(targetNamespace = "http://bwfla.bwl.de/api/imagearchive")
public class ImageArchiveWS {
	protected static final Logger LOG = Logger.getLogger(ImageArchiveWS.class.getName());
	private boolean configured = false;

	private ImageArchiveConfig iaConfig = null;
	private IWDArchive iwdArchive = null;
	private ImageHandler imageHandler = null;

	@Resource
	private WebServiceContext context;


	@PostConstruct
	private void initialize() {
		this.reloadProperties();
	}

	synchronized private void reloadProperties() {

		iaConfig = ImageArchiveSingleton.iaConfig;
		iwdArchive = ImageArchiveSingleton.iwdArchive;
		imageHandler = ImageArchiveSingleton.imageHandler;
		configured = true;
	}
	
	synchronized public void deleteTempEnvironments() throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}
		imageHandler.cleanTmpFiles();
	}
	
	synchronized public void reload() {
		for (ImageType t : ImageType.values()) {
			ImageArchiveSingleton.imagesCache.put(t, imageHandler.loadMetaData(t));
		}
	}

	public String getExportPrefix() {
		return this.imageHandler.getExportPrefix();
	}

	public String getEnvironmentById(String id) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}
		Environment env = imageHandler.getEnvById(id);
		return env == null ? null : env.toString();
	}

	public List<String> getEnvironments(String type) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}

		MessageContext messageContext = context.getMessageContext(); //IllegalStateException thrown
		HttpServletRequest request = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);

		LOG.info("request: " + request.getRequestURI());

		List<String> images = new ArrayList<String>();

		if (type == null) // get all environments
		{
			for (ImageType t : ImageType.values()) {
				if (t.equals(ImageType.template))
					continue;
				
				if (t.equals(ImageType.tmp))
					continue;

				if (t.equals(ImageType.sessions))
					continue;

				Map<String, Environment> iMap = ImageArchiveSingleton.imagesCache.get(t);
				if (iMap == null)
					continue;
				for (Environment env : iMap.values())
					images.add(env.toString());
			}
			return images;
		}
		// type not null...
		try {
			Map<String, Environment> iMap = ImageArchiveSingleton.imagesCache.get(ImageType.valueOf(type));
			if (iMap == null)
				return images;

			for (Environment env : iMap.values()) {
				images.add(env.toString());
			}
		} catch (IllegalArgumentException e) {
			throw new BWFLAException("client has specified an illegal argument as an image type: " + type, e);
		}
		return images;
	}

	public boolean deleteImage(String imageId, String type) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}
		
		return imageHandler.deleteImage(imageId, type);
	}
	
	public boolean deleteMetadata(String envId) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}

		return imageHandler.deleteMetaData(envId);
	}

	public String  importImageFromUrl(URL url, ImageArchiveMetadata request) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}

		if (url == null) {
			throw new BWFLAException("image data handler is null, aborting");
		}

		try {
			return imageHandler.importImageUrl(url, request, true);
		} catch (IOException e) {
			throw new BWFLAException(e);
		}
	}

	@TransactionTimeout(value = 1, unit = TimeUnit.DAYS)
	public String importImageAsStream(@XmlMimeType("application/octet-stream") DataHandler image, ImageArchiveMetadata iaMd) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}

		if (image == null) {
			throw new BWFLAException("image data handler is null, aborting");
		}

		return imageHandler.importImageStream(image, iaMd);
	}

	public ImageImportResult getImageImportResult(String sessionId) throws BWFLAException {
		return imageHandler.getImageImportResult(sessionId);
	}

	public String generalizedImport(String imageId, ImageType type, String templateId) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}

		if (imageId == null) {
			throw new BWFLAException("imageID is null, aborting");
		}

		// check if template requires generalization
		MachineConfigurationTemplate tempEnv = (MachineConfigurationTemplate) imageHandler.getEnvById(templateId);
		if(tempEnv == null)
			throw new BWFLAException("invalid template");

		if(tempEnv.getImageGeneralization() == null || tempEnv.getImageGeneralization().getModificationScript() == null)
			return imageId;

		try {
			String cowId = UUID.randomUUID().toString() + String.valueOf(System.currentTimeMillis()).substring(0, 2);
			imageHandler.createPatchedCow(imageId, cowId, templateId, type.name());
			return cowId;
		}
		catch (IOException e) {
			throw new BWFLAException(e);
		}
	}

	public String createImage(String size, String type) throws BWFLAException {

		String id = UUID.randomUUID().toString() + String.valueOf(System.currentTimeMillis()).substring(0, 2);

		File target = imageHandler.getImageTargetPath(type);

		File destImgFile = new File(target, id);

		EmulatorUtils.createNewCowFile(destImgFile.toPath(), size);
		return id;

	}

	public void updateConfiguration(String conf) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}

		if (conf == null) {
			throw new BWFLAException("image configuration data is null, aborting");
		}

		Environment emuEnv = null;
		try {
			emuEnv = Environment.fromValue(conf);
		} catch (Throwable t) {
			LOG.info(conf);
			throw new BWFLAException("updateConfiguration: failed to parse environment", t);
		}
		ImageType t = ImageHandler.getImageType(emuEnv.getId());
		if(t == null)
			throw new BWFLAException("updateConfiguration: unknown environment : " + emuEnv.getId() + "\n" + conf);

		if(ImageHandler.writeMetaData(conf, emuEnv.getId(), t.name(), true))
		{
			ImageHandler.addCachedEnvironment(t, emuEnv.getId(), emuEnv);
		}
	}

	public void importConfiguration(String conf, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}

		if (conf == null) {
			throw new BWFLAException("image configuration data is null, aborting");
		}

		Environment emuEnv = null;
		try {
			emuEnv = Environment.fromValue(conf);
		} catch (Throwable t) {
			LOG.info(conf);
			throw new BWFLAException("importConfiguration: failed to parse environment", t);
		}

		if (ImageHandler.writeMetaData(conf, emuEnv.getId(), iaMd.getType().name(), deleteIfExists)) {
			ImageHandler.addCachedEnvironment(iaMd.getType(), emuEnv.getId(), emuEnv);
		}
	}
    public void commitTempEnvironment(String id) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}

		try {
			imageHandler.commitTempEnvironment(id);
		} catch (IOException e) {
			throw new BWFLAException("commit tmp image: ", e);
		}
	}
	public void commitTempEnvironmentWithCustomType(String id, String type) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}

		try {
			imageHandler.commitTempEnvironment(id, type);
		} catch (IOException e) {
			throw new BWFLAException("commit tmp image: ", e);
		}
	}

	public String getRecording(String envId, String traceId) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}
		return iwdArchive.getRecording(envId, traceId);
	}

	public List<IWDMetaData> getRecordings(String envId) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");

		}
		return iwdArchive.getRecordings(envId);
	}

	public boolean addRecordingFile(String envId, String traceId, String data) throws BWFLAException {
		if (!configured) {
			throw new BWFLAException("ImageArchive is not configured");
		}
		return iwdArchive.addRecordingFile(envId, traceId, data);
	}

	public synchronized String getDefaultEnvironment(String osId)
	{
		Properties defaults = ImageArchiveSingleton.defaultEnvironments;
		return defaults.getProperty(osId);
	}

	public synchronized void setDefaultEnvironment(String osId, String envId) throws BWFLAException {
		Properties defaults = ImageArchiveSingleton.defaultEnvironments;
		defaults.setProperty(osId, envId);
		LOG.info("set default env");
		if(ImageArchiveSingleton.defaultEnvironmentsFile == null) {

			LOG.warning("default environments File not found");
			return;
		}
		try {
			defaults.store(new FileOutputStream(ImageArchiveSingleton.defaultEnvironmentsFile), null);
			LOG.info("stored to " + ImageArchiveSingleton.defaultEnvironmentsFile);
		} catch (IOException e) {
			throw new BWFLAException(e);
		}
	}

	public ImageExport getImageDependencies(String envId) throws BWFLAException
	{
		try {
			return imageHandler.getImageExportData(envId);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			throw new BWFLAException(e);
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

	public List<String> replicateImages(List<String> images)
	{
		return imageHandler.replicateImages(images);
	}
}
