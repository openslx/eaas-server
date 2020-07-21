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

package de.bwl.bwfla.common.taskmanager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


public class TaskManager<R>
{
	/** Counter for task ID generation */
	private final AtomicInteger idCounter = new AtomicInteger(0);

	/** Logger */
	protected final Logger log = Logger.getLogger(this.getClass().getName());
	
	/** Task registry */
	private final Map<String, TaskInfo<R>> tasks = new ConcurrentHashMap<String, TaskInfo<R>>();

	/** Task group registry */
	private final Map<String, TaskGroup> groups = new ConcurrentHashMap<String, TaskGroup>();

	/** Task executor */
	private final ExecutorService executor;

	/** Constructor */
	public TaskManager(ExecutorService executor)
	{
		this.executor = executor;
	}
	
	public ExecutorService executor()
	{
		return executor;
	}
	
	public String submit(AbstractTask<R> task)
	{
		return this.submit(task, null, null);
	}
	
	public String submit(AbstractTask<R> task, Object userdata)
	{
		return this.submit(task, userdata, null);
	}

	public String submit(Collection<AbstractTask<R>> tasks)
	{
		String groupid = this.nextTaskId();
		TaskGroup group = new TaskGroup();

		for (AbstractTask<R> task : tasks)
			this.submit(task, null, group);

		log.info("Task group " + groupid + " with " + tasks.size() + " task(s) submitted.");

		groups.put(groupid, group);
		return groupid;
	}

	public TaskInfo<R> lookup(String taskid)
	{
		return tasks.get(taskid);
	}

	public boolean remove(String taskid)
	{
		boolean removed = (tasks.remove(taskid) != null);
		if (removed)
			log.info("Task " + taskid + " removed.");

		return removed;
	}

	public Groups groups()
	{
		return new Groups();
	}

	public class Groups
	{
		public TaskGroup lookup(String id)
		{
			return groups.get(id);
		}

		public boolean remove(String id)
		{
			return this.remove(id, true);
		}

		public boolean remove(String id, boolean recursive)
		{
			if (recursive) {
				final TaskGroup group = groups.get(id);
				if (group == null)
					return false;

				for (String tid : group.getPendingTasks())
					TaskManager.this.remove(tid);

				for (String tid : group.getDoneTasks())
					TaskManager.this.remove(tid);
			}

			boolean removed = (groups.remove(id) != null);
			if (removed)
				log.info("Group " + id + " removed.");

			return removed;
		}
	}


	/* =============== Internal Helpers =============== */

	private String nextTaskId()
	{
		int id = idCounter.incrementAndGet();
		return Integer.toString(id);
	}

	private String submit(AbstractTask<R> task, Object userdata, TaskGroup group)
	{
		String id = this.nextTaskId();
		task.setup(id, group, executor);
		task.getTaskResult()
				.whenComplete((result, error) -> {
					if (error == null) {
						log.info("Task " + id + " completed.");
						return;
					}

					String message = "Task " + id + " failed!";
					if (error.getMessage() != null)
						message += " Cause: " + error.getMessage();

					log.warning(message);
				});

		executor.execute(task);

		tasks.put(id, new TaskInfo<R>(task, userdata));
		log.info("Task " + id + " submitted.");

		return id;
	}
}
