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

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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

	public void execute(Runnable task)
	{
		executor.execute(task);
	}
	
	public <T> Future<T> submit(Callable<T> task)
	{
		return executor.submit(task);
	}
	
	public String submitTask(AbstractTask<R> task)
	{
		return this.submit(task, null, null);
	}
	
	public String submitTask(AbstractTask<R> task, Object userdata)
	{
		return this.submit(task, userdata, null);
	}

	public String submitTaskGroup(List<AbstractTask<R>> tasks)
	{
		String groupid = this.nextTaskId();
		TaskGroup group = new TaskGroup();

		for (AbstractTask<R> task : tasks)
			this.submit(task, null, group);

		log.info("Task group " + groupid + " with " + tasks.size() + " task(s) submitted.");

		groups.put(groupid, group);
		return groupid;
	}

	public TaskInfo<R> getTaskInfo(String taskid)
	{
		return tasks.get(taskid);
	}

	public TaskGroup getTaskGroup(String groupid)
	{
		return groups.get(groupid);
	}

	public boolean removeTaskInfo(String taskid)
	{
		boolean removed = (tasks.remove(taskid) != null);
		if (removed)
			log.info("Task " + taskid + " removed.");

		return removed;
	}

	public boolean removeTaskGroup(String groupid)
	{
		boolean removed = (groups.remove(groupid) != null);
		if (removed)
			log.info("Task group " + groupid + " removed.");

		return removed;
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
		if (group != null)
			group.addTask(id);

		task.setup(id, group);

		Future<R> future = this.submit(task);
		tasks.put(id, new TaskInfo<R>(task, future, userdata));
		log.info("Task " + id + " submitted.");

		return id;
	}
}
