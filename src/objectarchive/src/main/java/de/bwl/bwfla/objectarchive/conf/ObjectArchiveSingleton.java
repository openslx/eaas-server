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

package de.bwl.bwfla.objectarchive.conf;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.openslx.eaas.migration.IMigratable;
import com.openslx.eaas.migration.MigrationRegistry;
import com.openslx.eaas.migration.config.MigrationConfig;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectS3ArchiveDescriptor;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectUserArchiveDescriptor;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectMETSFileArchive;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectS3Archive;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectUserArchive;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectUserFileArchive;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectArchive;
import de.bwl.bwfla.objectarchive.DigitalObjectArchiveFactory;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectFileArchive;


//4050f25a-da9b-4c11-90df-d48ded66441f
// 

@Singleton
@Startup
public class ObjectArchiveSingleton
		implements IMigratable
{
	protected static final Logger				LOG	= Logger.getLogger(ObjectArchiveSingleton.class.getName());
	public static volatile boolean 				confValid = false;
	//public static volatile ObjectArchiveConf	CONF;
	public static ConcurrentHashMap<String, DigitalObjectArchive> archiveMap = null;
	// public static ConcurrentHashMap<String, List<FileCollection>> archiveContent = null;
	@Inject
	@Config(value="objectarchive.objectarchiveconfdir")
	public String objArchiveConfDir;
	
	@Inject
	@Config(value="objectarchive.httpexport")
	public String httpExport;

	@Inject
	@Config(value="objectarchive.default_local_file_path")
	public String defaultLocalFilePath;

	@Inject
	@Config(value="commonconf.serverdatadir")
	private String serverdatadir;

	@Inject
	@Config(value="objectarchive.default_archive")
	private String defaultArchive;

	public static final String tmpArchiveDir = "emil-temp-objects";
	public static final String tmpArchiveName = "emil-temp-objects";
	public static final String remoteArchiveName = "Remote Objects";
	public static final String ZEROCONF_ARCHIVE_NAME = "zero conf";

	public static final String remoteMetsObjects = "metsRemoteMetadata";

	private static AsyncIoTaskManager taskManager;

	class AsyncIoTaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Object> {
		public AsyncIoTaskManager() throws NamingException {
			super("OBJECT-ARCHIVE-TASKS", InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
		}
	}

	private static final ExecutorService executor;
	static {
		try {
			executor = InitialContext.doLookup("java:jboss/ee/concurrency/executor/io");
		}
		catch (Exception error) {
			throw new IllegalStateException(error);
		}
	}

	@PostConstruct
	public void init()
	{
		try {
			loadConf();
		} catch (ObjectArchiveInitException e) {
			// TODO do something clever here, we can't throw an exception here
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}

		try {
			taskManager = new AsyncIoTaskManager();
		} catch (NamingException e) {
			throw new IllegalStateException("failed to create AsyncIoTaskManager");
		}
	}

	public boolean validate()
	{
		if(objArchiveConfDir == null || httpExport == null)
			return false; 
		
		File test = new File(objArchiveConfDir);
		if(!test.exists())
			return false;
		
		return true;
	}
	
	synchronized public void loadConf() throws ObjectArchiveInitException
	{ 
		confValid = validate(); 
		
		if(!confValid)
			throw new ObjectArchiveInitException("no valid configuration found");

		List<DigitalObjectArchive> archives = new ArrayList<>();
		archives.addAll(DigitalObjectArchiveFactory.createFromJson(new File(objArchiveConfDir)));

		File defaultObjectsPath = new File(defaultLocalFilePath);
		if(!defaultObjectsPath.exists())
			throw new ObjectArchiveInitException("no archive configuration found");

		// add internal upload archive
		File tempObjectPath = new File(serverdatadir, tmpArchiveDir);
		archives.add(new DigitalObjectFileArchive(tmpArchiveName, tempObjectPath.getAbsolutePath(), false));

		// add mets remote
		File remoteMetsMd = new File(serverdatadir, remoteMetsObjects);
		try {
			archives.add(new DigitalObjectMETSFileArchive(remoteArchiveName, remoteMetsMd.getAbsolutePath(), null, false));
		}
		catch (BWFLAException e)
		{
			LOG.severe("failed to initialize mets remote archive " + e.getMessage());
		}
		
		ObjectArchiveSingleton.archiveMap = new ConcurrentHashMap<>();
		for(DigitalObjectArchive a : archives)
		{
			ObjectArchiveSingleton.archiveMap.put(a.getName(), a);
			LOG.info("Adding object archive: " + a.getName());
			if(a.isDefaultArchive() && !a.getName().equalsIgnoreCase(defaultArchive)) {
				ObjectArchiveSingleton.archiveMap.put(defaultArchive, a);
				LOG.warning("Setting archive '" + a.getName() + "' as default");
			}
		}

		if (!archiveMap.containsKey(ZEROCONF_ARCHIVE_NAME)) {
			try {
				LOG.info("Loading zero-conf archive using defaults...");
				final var zeroconf = new DigitalObjectS3Archive(DigitalObjectS3ArchiveDescriptor.zeroconf());
				archiveMap.put(zeroconf.getName(), zeroconf);
			}
			catch (Exception error) {
				throw new ObjectArchiveInitException("Failed to initialize zero-conf archive!", error);
			}
		}

		// always register a default archive!
		if (!archiveMap.containsKey(defaultArchive))
			archiveMap.put(defaultArchive, archiveMap.get(ZEROCONF_ARCHIVE_NAME));

		final var archive = archiveMap.get(defaultArchive);
		LOG.info("Registered default archive: " + defaultArchive + " -> " + archive.getName());
	}

	public static ExecutorService executor()
	{
		return executor;
	}

	public static TaskState submitTask(BlockingTask<Object> task)
	{
		String taskId = taskManager.submit(task);
		TaskState state = new TaskState(taskId);
		return state;
	}

	public static TaskState getState(String taskId)
	{
		if(taskId == null)
			return null;

		TaskState state = new TaskState(taskId);
		final TaskInfo<Object> info = taskManager.lookup(taskId);
		if(info == null)
			return null;
		if(info.result().isDone())
			state.setDone(true);
		return state;
	}

	public static class ObjectArchiveInitException extends Exception
	{
		public ObjectArchiveInitException(String message, Throwable cause)
		{
			super(message, cause);
		}
		
		public ObjectArchiveInitException(String message)
		{
			super(message);
		}
	}

	@Override
	public void register(@Observes MigrationRegistry migrations) throws Exception
	{
		for (final var name : archiveMap.keySet()) {
			if (name.equals("default"))
				continue;

			final var archive = archiveMap.get(name);
			archive.register(migrations);
		}

		migrations.register("create-mets-objects", this::createMetsObjects);
		migrations.register("fix-mets-objects", this::fixMetsObjects);
		migrations.register("pack-object-files", this::packObjectFiles);
		migrations.register("cleanup-legacy-object-files", this::cleanupLegacyObjectFiles);
		migrations.register("rename-user-object-archives", this::renameUserArchives);
		migrations.register("import-legacy-object-archives-v1", this::importLegacyArchivesV1);
	}

	private interface IHandler<T>
	{
		void handle(T data) throws Exception;
	}

	private void execute(IHandler<DigitalObjectArchive> migration) throws Exception
	{
		// NOTE: there can be multiple instances of archives,
		//       execute migration for each of them!

		for (final var name : archiveMap.keySet()) {
			if (name.equals("default"))
				continue;

			migration.handle(archiveMap.get(name));
		}

		// execute migration for legacy zero-conf archive (file-based) too
		if (!(archiveMap.get(ZEROCONF_ARCHIVE_NAME) instanceof DigitalObjectFileArchive))
			migration.handle(new DigitalObjectFileArchive(ZEROCONF_ARCHIVE_NAME, defaultLocalFilePath, false));

		// NOTE: user-private archives are dynamically registered on-demand, hence
		//       execute migration on a temporary instance for each known user!

		for (final var name : DigitalObjectUserFileArchive.listArchiveNames()) {
			if (archiveMap.containsKey(name))
				continue;  // skip registered archives!

			migration.handle(new DigitalObjectUserFileArchive(name));
		}
	}

	private void createMetsObjects(MigrationConfig mc) throws Exception
	{
		final IHandler<DigitalObjectArchive> migration = (archive) -> {
			if (archive instanceof DigitalObjectFileArchive)
				((DigitalObjectFileArchive) archive).createMetsFiles(mc);
		};

		this.execute(migration);
	}

	private void fixMetsObjects(MigrationConfig mc) throws Exception
	{
		final IHandler<DigitalObjectArchive> migration = (archive) -> {
			if (archive instanceof DigitalObjectFileArchive)
				((DigitalObjectFileArchive) archive).fixMetsFiles(mc);
		};

		this.execute(migration);
	}

	private void packObjectFiles(MigrationConfig mc) throws Exception
	{
		final IHandler<DigitalObjectArchive> migration = (archive) -> {
			if (archive instanceof DigitalObjectFileArchive)
				((DigitalObjectFileArchive) archive).packFilesAsIso(mc);
		};

		this.execute(migration);
	}

	private void cleanupLegacyObjectFiles(MigrationConfig mc) throws Exception
	{
		final IHandler<DigitalObjectArchive> migration = (archive) -> {
			if (archive instanceof DigitalObjectFileArchive)
				((DigitalObjectFileArchive) archive).cleanupLegacyFiles(mc);
		};

		this.execute(migration);
	}

	private void renameUserArchives(MigrationConfig mc) throws Exception
	{
		DigitalObjectUserArchive.renameArchives(mc);
	}

	private void importLegacyArchivesV1(MigrationConfig mc) throws Exception
	{
		final var zeroconf = archiveMap.get(ZEROCONF_ARCHIVE_NAME);
		if (!(zeroconf instanceof DigitalObjectS3Archive))
			throw new IllegalStateException("S3-based archive '" + ZEROCONF_ARCHIVE_NAME + "' not found!");

		final var s3archive = (DigitalObjectS3Archive) zeroconf;
		final var usrbasedir = ConfigurationProvider.getConfiguration()
				.get("objectarchive.userarchive");

		// import file-based zero-conf archive...
		s3archive.importLegacyArchive(mc, Path.of(defaultLocalFilePath));

		// import all user-private archives...
		for (final var name : DigitalObjectUserFileArchive.listArchiveNames()) {
			final var usrdesc = DigitalObjectUserArchiveDescriptor.create(name, s3archive.getDescriptor());
			final var usrarchive = new DigitalObjectUserArchive(usrdesc);
			usrarchive.importLegacyArchive(mc, Path.of(usrbasedir, name));
		}
	}
}
