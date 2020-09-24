package de.bwl.bwfla.imagearchive.util;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.*;


public class EnvironmentsAdapter extends ImageArchiveWSClient {

	public EnvironmentsAdapter(String wsHost) {
		super(wsHost);
	}

	public List<Environment> getEnvironments(String type) throws BWFLAException, JAXBException {

		return this.getEnvironments(this.getDefaultBackendName(), type);
	}
	// replace hardcoded EMULATOR_DEFAULT_ARCHIVE with something nicer
	private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

	public List<Environment> getEnvironments(String backend, String type) throws BWFLAException, JAXBException {
		connectArchive();

		List<String> envs = archive.getEnvironments(backend, type);

		List<Environment> out = new ArrayList<Environment>();
		for (String envStr : envs) {
			Environment emuEnv = Environment.fromValue(envStr);
			if (emuEnv == null)
				continue;

			this.updateUrlPrefix(backend, emuEnv);
			out.add(emuEnv);
		}

		return out;
	}

	public List<String> getRawEnvironemts(String backend,String type) throws BWFLAException {
		return archive.getEnvironments(backend, type);
	}

	public MachineConfigurationTemplate getTemplate(String id) throws BWFLAException {
		return this.getTemplate(EMULATOR_DEFAULT_ARCHIVE, id);
	}

	public MachineConfigurationTemplate getTemplate(String backend, String id) throws BWFLAException {
		List<MachineConfigurationTemplate> envs = this.getTemplates(backend);
		for (MachineConfigurationTemplate e : envs) {
			if (e.getId().equals(id))
				return e;
		}

		return null;
	}

	public void updateUrlPrefix(String backend, Environment env) throws BWFLAException
	{
		if (env instanceof MachineConfiguration) {
			final MachineConfiguration config = (MachineConfiguration) env;
			for (AbstractDataResource r : config.getAbstractDataResource()) {
				if (r instanceof ImageArchiveBinding) {
					ImageArchiveBinding iaBinding = (ImageArchiveBinding) r;
					iaBinding.setUrlPrefix(this.getExportPrefix(backend));
				}
			}
		}
		else if (env instanceof ContainerConfiguration) {
			final ContainerConfiguration config = (ContainerConfiguration) env;
			for (AbstractDataResource r : config.getDataResources()) {
				if (r instanceof ImageArchiveBinding) {
					ImageArchiveBinding iaBinding = (ImageArchiveBinding) r;
					iaBinding.setUrlPrefix(this.getExportPrefix(backend));
				}
			}
		}
	}

	public void sync() throws BWFLAException {
		connectArchive();
		Collection<String> archives = listBackendNames();
		for(String archive : archives)
			this.sync(archive);
	}

	public void sync(String backend) throws BWFLAException {
		connectArchive();
		archive.reload(backend);
	}

	public List<MachineConfigurationTemplate> getTemplates() throws BWFLAException {
		archive.reload(EMULATOR_DEFAULT_ARCHIVE);
		return this.getTemplates(EMULATOR_DEFAULT_ARCHIVE);
	}

	public List<ImageGeneralizationPatchDescription> getImageGeneralizationPatches() throws BWFLAException {
		connectArchive();
		return archive.getImageGeneralizationPatches();
	}

	public List<MachineConfigurationTemplate> getTemplates(String backend) throws BWFLAException {
		connectArchive();

		List<MachineConfigurationTemplate> _templates = new ArrayList<MachineConfigurationTemplate>();
		List<String> envlist = archive.getEnvironments(backend, "template");

		for (String env : envlist) {
			try {
				MachineConfigurationTemplate emuEnv = MachineConfigurationTemplate.fromValue(env);
				if (emuEnv == null)
					continue;

				if (emuEnv.getEmulator() == null) {
					log.info("no emu " + emuEnv.getDescription().getTitle());
					continue;
				}

				if (emuEnv.getDescription() == null) {
					log.info("DescriptionTag is mandatory: " + emuEnv.getId());
					continue;
				}
				updateUrlPrefix(backend, emuEnv);
				_templates.add(emuEnv);
			} catch (Throwable t) {
				log.info("loadTemplates2: failed to parse environment: " + t.getMessage());
				log.info(env);
				log.log(Level.SEVERE, t.getMessage(), t);
			}
		}

		log.info("found " + _templates.size() + " templates");
		return _templates;
	}

	public Environment getEnvironmentById(String id) throws BWFLAException {
		return this.getEnvironmentById(this.getDefaultBackendName(), id);
	}

	public Environment getEnvironmentById(String backend, String id) throws BWFLAException {
		connectArchive();
		Environment env = null;
		String imageConf = archive.getEnvironmentById(backend, id);
		if (imageConf == null)
			throw new BWFLAException("image with the following id cannot be located in the image archive: " + id);

		try {
			env = Environment.fromValue(imageConf);
		} catch (Exception e) {
			throw new BWFLAException("can't load image with id " + id + ": " + e.getMessage());
		}
		updateUrlPrefix(backend, env);
		return env;
	}

	public void delete(String envId, boolean deleteMetadata, boolean deleteImage) throws BWFLAException {
		this.delete(this.getDefaultBackendName(), envId, deleteMetadata, deleteImage);
	}

	public void delete(String backend, String envId, boolean deleteMetadata, boolean deleteImage) throws BWFLAException {
		connectArchive();
		Environment environment = this.getEnvironmentById(backend, envId);
		if(deleteMetadata)
			this.deleteMetaData(backend, envId);

		if(deleteMetadata || deleteImage) // if we delete metadata we have to delete the image too!
		{
			if(environment instanceof MachineConfiguration) {
				for (AbstractDataResource b : ((MachineConfiguration) environment).getAbstractDataResource()) {
					if (!(b instanceof ImageArchiveBinding))
						continue;
					if (b.getId().equals("main_hdd")) {
						ImageArchiveBinding iab = (ImageArchiveBinding) b;
						log.info("deleting image: " + iab.getImageId());
						this.deleteImage(backend, iab.getImageId(), iab.getType());
					}
				}
			}
			else if (environment instanceof OciContainerConfiguration)
			{
				for (AbstractDataResource b : ((OciContainerConfiguration) environment).getDataResources()) {
					if (!(b instanceof ImageArchiveBinding))
						continue;
					if (b.getId().equals("main_hdd")) {
						ImageArchiveBinding iab = (ImageArchiveBinding) b;
						log.info("deleting image: " + iab.getImageId());
						this.deleteImage(backend, iab.getImageId(), iab.getType());
					}
				}
			}
		}
	}

	public boolean deleteMetaData(String envId) throws BWFLAException {
		return this.deleteMetaData(this.getDefaultBackendName(), envId);
	}

	public boolean deleteMetaData(String backend, String envId) throws BWFLAException {
		connectArchive();
		return archive.deleteMetadata(backend, envId);
	}

	public boolean deleteImage(String imageId, String type) throws BWFLAException {
		return this.deleteImage(this.getDefaultBackendName(), imageId, type);
	}

	public boolean deleteImage(String backend, String imageId, String type) throws BWFLAException {
		connectArchive();
		return archive.deleteImage(backend, imageId, type);
	}

	public ImportImageHandle importImage(URL ref, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException {
		return this.importImage(this.getDefaultBackendName(), ref, iaMd, deleteIfExists);
	}

	public ImportImageHandle importImage(String backend, URL ref, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException {
		connectArchive();
		if (ref == null)
			throw new BWFLAException("URL was null");

		String sessionId = archive.importImageFromUrl(backend, ref.toString(), iaMd);
		return new ImportImageHandle(archive, backend, iaMd.getType(), sessionId);
	}

	public TaskState importImageAsync(URL ref, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException {
		return this.importImageAsync(this.getDefaultBackendName(), ref, iaMd, deleteIfExists);
	}

	public TaskState importImageAsync(String backend, URL ref, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException {
		connectArchive();
		if (ref == null)
			throw new BWFLAException("URL was null");

		return archive.importImageFromUrlAsync(backend, ref.toString(), iaMd);
	}

	public String createPatchedImage(String imageId, ImageType type, String patchId) throws BWFLAException {
		return this.createPatchedImage(this.getDefaultBackendName(), imageId, type, patchId);
	}

	public String createPatchedImage(String backend, String imageId, ImageType type, String patchId) throws BWFLAException {
		connectArchive();
		return archive.createPatchedImage(backend, imageId, type, patchId);
	}

	public List<DefaultEntry> getDefaultEnvironments(String backend) throws BWFLAException {
		connectArchive();
		return archive.getDefaultEnvironments(backend).getMap();
	}

	public String getDefaultEnvironment(String osId) throws BWFLAException {
		return this.getDefaultEnvironment(this.getDefaultBackendName(), osId);
	}

	public String getDefaultEnvironment(String backend, String osId) throws BWFLAException {
		connectArchive();
		return archive.getDefaultEnvironment(backend, osId);
	}

	public void setDefaultEnvironment(String osId, String envId) throws BWFLAException {
		this.setDefaultEnvironment(this.getDefaultBackendName(), osId, envId);
	}

	public void setDefaultEnvironment(String backend, String osId, String envId) throws BWFLAException {
		connectArchive();
		archive.setDefaultEnvironment(backend, osId, envId);
	}

	public ImportImageHandle importImage(DataHandler handler, ImageArchiveMetadata iaMd) throws BWFLAException {
		return this.importImage(this.getDefaultBackendName(), handler, iaMd);
	}

	public ImportImageHandle importImage(String backend, DataHandler handler, ImageArchiveMetadata iaMd) throws BWFLAException {
		connectArchive();

		String sessionId = archive.importImageAsStream(backend, handler, iaMd);
		return new ImportImageHandle(archive, backend, iaMd.getType(), sessionId);
	}

	public ImportImageHandle importImage(File image, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException {
		return this.importImage(this.getDefaultBackendName(), image, iaMd, deleteIfExists);
	}

	public TaskState createImageAsync(String backend, String size, ImageType type, ImageMetadata md) throws BWFLAException
	{
		connectArchive();
		return archive.createImageAsync(backend, size, type, md);
	}

	public ImportImageHandle importImage(String backend, File image, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException {
		connectArchive();

		if (image == null)
			throw new BWFLAException("image file was null");

		if (!image.exists()) {
			throw new BWFLAException("importImage: file not found: " + image);
		}

		DataHandler dataHandler = new DataHandler(new FileDataSource(image));
		String sessionId = archive.importImageAsStream(backend, dataHandler, iaMd);
		return new ImportImageHandle(archive, backend, iaMd.getType(), sessionId);
	}

	public TaskState getTaskState(String id) throws BWFLAException
	{
		return archive.getTaskState(id);
	}

	public ImageNameIndex getImagesIndex(String _archive) throws BWFLAException {
		if(_archive == null)
			_archive = getDefaultBackendName();

		return archive.getNameIndexes(_archive);
	}

	public String importMachineEnvironment(MachineConfiguration env, List<BindingDataHandler> data, ImageArchiveMetadata iaMd) throws BWFLAException {
		return this.importMachineEnvironment(this.getDefaultBackendName(), env, data, iaMd);
	}

	public String importMachineEnvironment(String backend, MachineConfiguration env, List<BindingDataHandler> data, ImageArchiveMetadata iaMd)
			throws BWFLAException
	{
		if (data != null) {
			ImportImageHandle handle = null;
			ImageArchiveBinding binding = null;
			for (BindingDataHandler bdh : data) {
				if(bdh.getData() != null) {
					handle = this.importImage(backend, bdh.getData(), iaMd);
					binding = handle.getBinding(60 * 60 * 60); // wait an hour
				}
				else
				{
					try {
						handle = this.importImage(backend, new URL(bdh.getUrl()), iaMd, true);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					binding = handle.getBinding(60 * 60 * 60); // wait an hour
				}
				binding.setId(bdh.getId());
				EmulationEnvironmentHelper.replace(env, binding, iaMd.getType().equals(ImageType.CHECKPOINTS));
			}
		}

		return this.importMetadata(backend, env.toString(), iaMd, false);
	}

	public void updateMetadata(Environment conf) throws BWFLAException {
		this.updateMetadata(this.getDefaultBackendName(), conf);
	}

	public void updateMetadata(String backend, Environment conf) throws BWFLAException {
		connectArchive();
		updateUrlPrefix(backend, conf);
		archive.updateConfiguration(backend, conf.toString());
	}

	public String importMetadata(String conf, ImageArchiveMetadata iaMd, boolean preserveId) throws BWFLAException {
		return this.importMetadata(this.getDefaultBackendName(), conf, iaMd, preserveId);
	}

	public String importMetadata(String backend, String conf, ImageArchiveMetadata iaMd, boolean preserveId) throws BWFLAException {
		connectArchive();

		Environment emuEnv = null;
		try {
			emuEnv = Environment.fromValue(conf);
		} catch (Throwable t) {
			log.info("loadTemplates4: failed to parse environment: " + t.getMessage());
			log.info(conf);
			throw new BWFLAException(t);
		}

		if (emuEnv == null)
			throw new BWFLAException("emuEnv is null");

		if (!preserveId)
			emuEnv.setId(getRandomId());

		emuEnv.setTimestamp(Instant.now().toString());
		archive.importConfiguration(backend, emuEnv.toString(), iaMd, preserveId);
		log.info("Archive '" + backend + "' imported image '" + emuEnv.getId() + "'");
		return emuEnv.getId();
	}

	public static String getRandomId() {
		return UUID.randomUUID().toString();
	}

	public void commitTempEnvironment(String id) throws BWFLAException {
		this.commitTempEnvironment(this.getDefaultBackendName(), id);
	}

	public void commitTempEnvironment(String backend, String id) throws BWFLAException {
		connectArchive();
		archive.commitTempEnvironment(backend, id);
	}

	public void commitTempEnvironmentWithCustomType(String id, String type) throws BWFLAException {
		this.commitTempEnvironmentWithCustomType(this.getDefaultBackendName(), id, type);
	}

	public void commitTempEnvironmentWithCustomType(String backend, String id, String type) throws BWFLAException {
		connectArchive();
		archive.commitTempEnvironmentWithCustomType(backend, id, type);
	}

	public void cleanTempEnvironments() throws BWFLAException {
		this.cleanTempEnvironments(this.getDefaultBackendName());
	}

	public void cleanTempEnvironments(String backend) throws BWFLAException {
		connectArchive();
		archive.deleteTempEnvironments(backend);
	}

	public void deleteNameIndexesEntry(String backend, String id, String version) throws BWFLAException {
		connectArchive();
		archive.deleteNameIndexesEntry(backend, id, version);
	}

	public class ImportImageHandle {
		private final String sessionId;
		private final ImageType type;
		private final ImageArchiveWS archive;
		private final String backend;

		ImportImageHandle(ImageArchiveWS archive, String backend, ImageType type, String sessionId) {
			this.sessionId = sessionId;
			this.type = type;
			this.archive = archive;
			this.backend = backend;
		}

		public ImageArchiveBinding getBinding() throws ImportNoFinishedException, BWFLAException {
			final ImageImportResult result = archive.getImageImportResult(backend, sessionId);
			if (result == null)
				throw new ImportNoFinishedException();

			return new ImageArchiveBinding(backend, result.getUrlPrefix(), result.getImageId(), type.value());
		}

		public ImageArchiveBinding getBinding(long timeout /* seconds */ ) throws BWFLAException {

			ImageArchiveBinding binding = null;

			while (binding == null) { // will throw a BWFLAException in case of an error
				try {
					if (timeout < 0)
						throw new BWFLAException("getBinding: timeout exceeded");
					binding = getBinding();
					timeout--;
				} catch (EnvironmentsAdapter.ImportNoFinishedException e) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						throw new BWFLAException(e1);
					}
				}
			}
			return binding;
		}
	}

	public ImageArchiveBinding getImageBinding(String name, String version) throws BWFLAException {
		return this.getImageBinding(this.getDefaultBackendName(), name, version);
	}

	public ImageArchiveBinding getImageBinding(String backend, String name, String version) throws BWFLAException {
		connectArchive();
		final String binding = archive.getImageBinding(backend, name, version);
		try {
			return (binding != null) ? ImageArchiveBinding.fromValue(binding) : null;
		}
		catch (Exception error) {
			throw new BWFLAException(error);
		}
	}

	public void extractMetadata(String imageId) throws BWFLAException {
		connectArchive();
		archive.extractMetadata(EMULATOR_DEFAULT_ARCHIVE, imageId);
	}

	public ImageImportResult getImageImportResult(String sessionId) throws BWFLAException {
		return this.getImageImportResult(this.getDefaultBackendName(), sessionId);
	}

	public ImageImportResult getImageImportResult(String backend, String sessionId) throws BWFLAException {
		return archive.getImageImportResult(backend, sessionId);
	}

	public List<ImportImageHandle> replicateImages(List<String> images) throws BWFLAException {
		return this.replicateImages(this.getDefaultBackendName(), images);
	}

	public List<ImportImageHandle> replicateImages(String backend, List<String> images) throws BWFLAException {
		connectArchive();
		List<String> sessions = archive.replicateImages(backend, images);
		List<ImportImageHandle> result = new ArrayList<>();
		for(String sessionId : sessions)
		{
			log.severe(sessionId);
			result.add(new ImportImageHandle(archive, backend, ImageType.BASE, sessionId));
		}
		return result;
	}

	public ImageNameIndex getNameIndexes() throws BWFLAException {
		connectArchive();
		return archive.getNameIndexes(getDefaultBackendName());
	}

	public ImageNameIndex getNameIndexes(String backend) throws BWFLAException {
		 connectArchive();
		 return archive.getNameIndexes(backend);
	}

	public void addNameIndexesEntry(String backend, ImageMetadata entry, Alias alias) throws BWFLAException {
		connectArchive();
		archive.addNameIndexesEntry(backend, entry, alias);
	}

	public void updateLatestEmulator(String backend, String emulator, String version) throws BWFLAException {
		connectArchive();
		archive.updateLatestEmulator(backend, emulator, version);
	}

	public Collection<String> listBackendNames() throws BWFLAException
	{
		connectArchive();
		return archive.listBackendNames();
	}

	public static class ImportNoFinishedException extends Exception {  }
}
