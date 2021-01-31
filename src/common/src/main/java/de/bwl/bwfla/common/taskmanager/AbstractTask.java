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

import de.bwl.bwfla.common.logging.PrefixLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public abstract class AbstractTask<R> implements Runnable
{
	protected final PrefixLogger log;

	private final CompletableFuture<R> result;

	private Executor executor;
	private TaskGroup group;
	private String id;

	protected AbstractTask()
	{
		this.log = new PrefixLogger(this.getClass().getName());
		this.result = new CompletableFuture<>();
		this.group = null;
		this.id = "-1";
	}

	protected void markTaskAsDone()
	{
		if (group != null)
			group.markTaskAsDone(this.getTaskId());
	}

	protected Executor executor()
	{
		return executor;
	}

	public String getTaskId()
	{
		return id;
	}
	
	public TaskGroup getTaskGroup()
	{
		return group;
	}

	public CompletableFuture<R> getTaskResult()
	{
		return result;
	}
	
	/** package-private initialization */
	void setup(String taskid, TaskGroup group, Executor executor)
	{
		this.executor = executor;
		this.id = taskid;
		this.group = group;
		if (group != null)
			group.addTask(id);
		
		log.getContext()
		   .add("TASK-" + taskid)
		   .update();
	}
}
