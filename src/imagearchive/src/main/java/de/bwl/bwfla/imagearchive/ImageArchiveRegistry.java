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

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.openslx.eaas.common.event.ServerStartupEvent;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveBackendConfig;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveConfig;

import static java.util.Map.Entry.comparingByValue;


@Singleton
@Startup
public class ImageArchiveRegistry
{
	private static final Logger log = Logger.getLogger("IMAGE-ARCHIVE-REGISTRY");
	private final Map<String, ImageArchiveBackend> backends = new HashMap<>();
	private final ImageArchiveConfig config = new ImageArchiveConfig();

	private static AsyncIoTaskManager taskManager;

	private static class AsyncIoTaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<String> {
		public AsyncIoTaskManager() throws NamingException {
			super("IMAGE-ARCHIVE-TASKS", InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
		}
	}

	public ImageArchiveBackend lookup(String name)
	{
		return backends.get(name);
	}

	public ImageArchiveBackend getDefaultBackend()
	{
		return this.lookup(config.getDefaultBackendName());
	}

	public ImageArchiveConfig getImageArchiveConfig()
	{
		return config;
	}

	public List<String> listBackendNames()
	{
		List<String> result = backends.entrySet().stream()
				.sorted(comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		return result;
	}

	public void handle(@Observes ServerStartupEvent event)
	{
		// Trigger CDI-based initialization on startup!
		log.info("Received event 'server-startup'");
	}


	/* ==================== Internal Helpers ==================== */

	/** Constructor for CDI */
	protected ImageArchiveRegistry()
	{
		// Empty!
	}

	@PostConstruct
	protected void initialize()
	{
		config.load();

		final List<ImageArchiveBackendConfig> backendConfigs = config.getBackendConfigs();
		log.info("Initializing image-archive(s)...");

		// Initialize the image-archives...
		for (ImageArchiveBackendConfig iac : backendConfigs) {
			try {
				final String name = iac.getName();
				iac.setRegistry(this);
				log.info("Initializing image-archive '" + name + "'...");
				final ImageArchiveBackend backend = new ImageArchiveBackend(iac);
				backend.reload();
				backends.put(name, backend);
				log.info("Image-archive '" + name + "' (" + iac.getType() + ") initialized");
			}
			catch (Exception exception) {
				log.log(Level.WARNING, "Initializing image-archive '" + iac.getName() + "' failed!\n", exception);
			}
		}

		try {
			taskManager = new AsyncIoTaskManager();
		} catch (NamingException e) {
			throw new IllegalStateException("failed to create AsyncIoTaskManager");
		}

		log.info("Initialized " + backendConfigs.size() + " image-archive(s)");
	}

	@PreDestroy
	protected void destroy()
	{
		backends.forEach((name, backend) -> {
			try {
				backend.getNameIndexes()
						.dump();

				log.info("Dumped name-index for backend '" + name + "'");
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Dumping name-index for backend '" + name + "' failed!", error);
			}
		});
	}

	public static TaskState submitTask(BlockingTask<String> task)
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
		try {
			final TaskInfo<String> info = taskManager.lookup(taskId);
			if (info == null)
				return null;


			if (info.result().isDone()) {
				state.setResult((String) info.result().get());
				state.setDone(true);
			}
		}
		catch (InterruptedException | ExecutionException e)
		{
			log.log(Level.WARNING, "Task failed!", e);
			state.setDone(true);
			state.setFailed(true);
		}

		return state;
	}
}