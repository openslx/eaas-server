package de.bwl.bwfla.objectarchive.impl;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.emucomp.api.Binding.AccessType;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectArchive;
import de.bwl.bwfla.common.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.common.taskmanager.TaskState;
import solutions.emulation.preservica.client.*;
import solutions.emulation.preservica.client.Manifestation.DigitalFileContent;
import solutions.emulation.preservica.client.SDBRestSession.SDBRestSessionException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class DigitalObjectPreservicaArchive implements Serializable, DigitalObjectArchive {

	SDBConfiguration config;
	private final String archiveLabel;

	public enum PUID
	{
		ISO("fmt/468"),
		JPEG("fmt/43");

		private String label;

		PUID(String label)
		{
			this.label = label;
		}
	}

	protected final Logger log = Logger.getLogger(this.getName());
	private static DigitalObjectFileArchive fileCache = null;
	private AsyncIoTaskManager taskManager;

	private SDBRestSession session;
	private Collection collection;
	private final boolean isDefaultArchive;
	private String loaderTask;

	class LoadMetadata extends BlockingTask<Collection>
	{
		private final ExecutorService pool;
		private final String collectionId;

		LoadMetadata(String collectionId)
		{
			this.collectionId = collectionId;
			this.pool = Executors.newFixedThreadPool(16);
		}

		@Override
		protected Collection execute() throws Exception {

			log.info("starting preservica sync...");
			Collection _collection;
			try {
				 _collection = new Collection(session, collectionId);
			} catch (SDBRestSessionException e) {
				log.warning("_collection threw exception");
				throw new BWFLAException(e);
			}

			List<String> units = _collection.getDeliverableUnits();
			for(String unitId : units)
			{
				DeliverableUnit unit = _collection.getDeliverableUnit(unitId);
				pool.execute(new ThumbLoader(unitId, unit, log));
			}

			pool.shutdown();

			try {
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("preservica sync successful for " + collectionId);
			return _collection;
		}
	}

	class AsyncIoTaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Collection> {
		public AsyncIoTaskManager() throws NamingException {
			super("PRESERVICA-TASKS", InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
		}
	}

	public DigitalObjectPreservicaArchive(String label, SDBConfiguration config, String collectionId, boolean defaultArchive)
			throws BWFLAException {

		archiveLabel = label;
		session = new SDBRestSession(config);
		this.config = config;
		this.isDefaultArchive = defaultArchive;

		try {
			taskManager = new AsyncIoTaskManager();
		} catch (NamingException e) {
			throw new BWFLAException("failed to create AsyncIoTaskManager");
		}

		if (fileCache == null)
			fileCache = new DigitalObjectFileCache();

		loaderTask = taskManager.submit(new LoadMetadata(collectionId));
		collection = null;
	}

	private synchronized void updateTaskCheck()
	{
		if(loaderTask == null)
			return;

		final TaskInfo<Collection> info = taskManager.lookup(loaderTask);
		if (!info.result().isDone())
			return;

		taskManager.remove(loaderTask);
		try {
			collection = info.result().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		loaderTask = null;
	}

	@Override
	public Stream<String> getObjectIds()
	{
		updateTaskCheck();
		if (collection == null)
			return Stream.empty();

		return collection.getDeliverableUnits()
				.stream();
	}

	private FileCollectionEntry createFileCollectionEntry(String objectId, DigitalFileContent dfc, DriveType t)
	{
		FileCollectionEntry fce = new FileCollectionEntry();
		fce.setId(objectId);
		if(dfc.getFileContentUrl() == null)
			return null;

		fce.setUrl(dfc.getFileContentUrl().toString());
		fce.setType(t);
		fce.setUsername(config.getUsername());
		fce.setPassword(config.getPassword());
		fce.setAccess(AccessType.COPY);
		fce.setLocalAlias(dfc.getTitle());
		return fce;
	}

	private FileCollectionEntry createFileCollectionEntry(String objectId, DigitalFileContent dfc) 
	{
			FileCollectionEntry fce = new FileCollectionEntry();
			fce.setId(objectId);
			if(dfc.getFileContentUrl() == null)
				return null;

			fce.setUrl(dfc.getFileContentUrl().toString());
			fce.setType(DriveType.CDROM);
			fce.setUsername(config.getUsername());
			fce.setPassword(config.getPassword());
			fce.setAccess(AccessType.COPY);
			fce.setLocalAlias(dfc.getTitle());
			return fce;
	}

	private void processDeliverableUnit(String objectId, String unitId) throws SDBRestSessionException {
		if(collection == null)
		{
			updateTaskCheck();
		}

		if(collection == null)
			throw new SDBRestSessionException("collection for " + archiveLabel + "  not available yet");

		DeliverableUnit unit = collection.getDeliverableUnit(unitId);
		if (unit == null) {
			throw new SDBRestSessionException("cant find unit " + unitId);
		}

		YaleMetsData mets = unit.getMets();
		Manifestation[] manifestations = unit.getManifestations();
		for (Manifestation m : manifestations) 
		{
			for(DigitalFileContent dfc : m.getDigitalFileContents())
			{
				if(dfc.getSummary().contains(PUID.ISO.label)) {
					FileCollectionEntry fce = createFileCollectionEntry(objectId, dfc);
					if (fce == null)
						continue;

					if (mets != null) {
						YaleMetsData.YaleMetsFileInformation fileInformation = mets.getFileInformation(dfc.getTitle());
						System.out.println("processDeliverableUnit: file info: " + fileInformation);
						if (fileInformation != null) {
							fce.setLabel(fileInformation.getLabel());
							fce.setOrder(fileInformation.getOrder());
						}
					} else
						System.out.println("no mets info");

					try {
						fileCache.importObjectFile(objectId, fce);
					} catch (BWFLAException e) {
						log.log(Level.WARNING, e.getMessage(), e);
					}
				}
				else if(dfc.getSummary().contains("fmt/383"))
				{
					log.severe("dfc: " + dfc.getSummary() + " : " + dfc.getTitle());

					FileCollectionEntry fce = createFileCollectionEntry(objectId, dfc, DriveType.FLOPPY);
					if (fce == null)
						continue;

					if (mets != null) {
						YaleMetsData.YaleMetsFileInformation fileInformation = mets.getFileInformation(dfc.getTitle());
						System.out.println("processDeliverableUnit: file info: " + fileInformation);
						if (fileInformation != null) {
							fce.setLabel(fileInformation.getLabel());
							fce.setOrder(fileInformation.getOrder());
						}
					} else
						System.out.println("no mets info");

					try {
						fileCache.importObjectFile(objectId, fce);
					} catch (BWFLAException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public FileCollection getObjectReference(String objectId) {
		// looking first into the cache
		FileCollection fc = fileCache.getObjectReference(objectId);
		if (fc != null && fc.files.size() > 0)
			return fc;

		// if not found load it into the cache
		try {
			processDeliverableUnit(objectId, objectId);
		} catch (SDBRestSessionException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}

		// try again
		return fileCache.getObjectReference(objectId);
	}

	@Override
	public void importObject(String metsdata) throws BWFLAException {

	}

	@Override
	public String getName() {
		return archiveLabel;
	}

	@Override
	public Path getLocalPath() {
		return fileCache.getLocalPath();
	}

	@Override
	public DigitalObjectMetadata getMetadata(String objectId) {
		DeliverableUnit unit = collection.getDeliverableUnit(objectId);
		if(unit == null)
			return null;
		DigitalObjectMetadata md = new DigitalObjectMetadata(objectId, unit.getTitle(), unit.getDescription());
		md.setSummary(unit.getSummary());
		md.setCustomData(unit.getMarc21());

		YaleMetsData mets = unit.getMets();
		if(mets != null)
		{
			md.setWikiDataId(mets.getWikiId());
		}

		String thumb = null;
		try {
			thumb = fileCache.getThumbnail(objectId);
		} catch (BWFLAException e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}
		if (thumb != null)
			md.setThumbnail(thumb);
		return md;
	}

	@Override
	public Stream<DigitalObjectMetadata> getObjectMetadata() {

		return this.getObjectIds()
				.map(this::getMetadata)
				.filter(Objects::nonNull);
	}

	@Override
	public void sync() {
		try {
			collection.sync(session);
		} catch (SDBRestSessionException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public TaskState sync(List<String> objectIDs) {

		return ObjectArchiveSingleton.submitTask(new PreservicaSyncTask(objectIDs));

	}

	class PreservicaSyncTask extends BlockingTask<Object>
	{
		private final List<String> objects;
		public PreservicaSyncTask(List<String> objectIDs)
		{
			this.objects = objectIDs;
		}

		@Override
		protected Object execute() throws Exception {
			collection.sync(session, objects);
			return null;
		}
	}

	@Override
	public boolean isDefaultArchive() {
		return isDefaultArchive;
	}

	@Override
	public int getNumObjectSeats(String id) {
		return 1;
	}

	private class ThumbLoader implements Runnable
	{
		private final Logger log;
		private DeliverableUnit unit;
		private String objectId;

		public ThumbLoader(String objectId, DeliverableUnit unit, Logger log) {

			this.log = log;
			this.unit = unit;
			this.objectId = objectId;
		}

		@Override
		public void run() {
			//	log.info("Worker task " + id + " started.");

			Manifestation[] manifestations = unit.getManifestations();
			for (Manifestation m : manifestations) {
				for (DigitalFileContent dfc : m.getDigitalFileContents()) {

					if (dfc.getSummary().contains(PUID.JPEG.label)) {
						FileCollectionEntry fce = createFileCollectionEntry(objectId, dfc);
						if (fce == null)
							continue;
						try {
							fileCache.importObjectThumbnail(fce);
						} catch (BWFLAException e) {
							log.log(Level.WARNING, e.getMessage(), e);
						}

					}
				}
			}
		}
	}

	@Override
	public void delete(String id) throws BWFLAException {
		throw new BWFLAException("not supported");
	}
}
