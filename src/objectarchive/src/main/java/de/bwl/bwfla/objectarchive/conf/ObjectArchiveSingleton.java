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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectMETSFileArchive;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectArchive;
import de.bwl.bwfla.objectarchive.DigitalObjectArchiveFactory;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectFileArchive;


//4050f25a-da9b-4c11-90df-d48ded66441f
// 

@Singleton
@Startup
public class ObjectArchiveSingleton
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

	public static final String remoteMetsObjects = "metsRemoteMetadata";

	private static AsyncIoTaskManager taskManager;

	class AsyncIoTaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Object> {
		public AsyncIoTaskManager() throws NamingException {
			super(InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
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
		archives.add(new DigitalObjectMETSFileArchive(remoteArchiveName, remoteMetsMd.getAbsolutePath(), null, false));
		
		ObjectArchiveSingleton.archiveMap = new ConcurrentHashMap<>();
		for(DigitalObjectArchive a : archives)
		{
			ObjectArchiveSingleton.archiveMap.put(a.getName(), a);
			LOG.info("adding object archive" + a.getName());
			if(a.isDefaultArchive() && !a.getName().equalsIgnoreCase(defaultArchive)) {
				ObjectArchiveSingleton.archiveMap.put(defaultArchive, a);
				LOG.warning("setting archive " + a.getName() + " as default");
			}
		}

		DigitalObjectArchive _a = new DigitalObjectFileArchive("zero conf", defaultLocalFilePath, false);
		archiveMap.put(_a.getName(), _a);
		if(!archiveMap.containsKey(defaultArchive)) {
			ObjectArchiveSingleton.archiveMap.put(defaultArchive, _a);
		}
	}

	public static TaskState submitTask(AbstractTask<Object> task)
	{
		String taskId = taskManager.submitTask(task);
		TaskState state = new TaskState(taskId);
		return state;
	}

	public static TaskState getState(String taskId)
	{
		if(taskId == null)
			return null;

		TaskState state = new TaskState(taskId);
		final TaskInfo<Object> info = taskManager.getTaskInfo(taskId);
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
}
