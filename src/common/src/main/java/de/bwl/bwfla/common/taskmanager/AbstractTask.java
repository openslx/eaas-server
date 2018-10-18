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

import java.util.concurrent.Callable;

import de.bwl.bwfla.common.logging.PrefixLogger;


public abstract class AbstractTask<R> implements Callable<R>
{
	protected final PrefixLogger log;
	
	private TaskGroup group;
	private String id;
	
	protected AbstractTask()
	{
		this.log = new PrefixLogger(this.getClass().getName());
		this.group = null;
		this.id = "-1";
	}

	public String getTaskId()
	{
		return id;
	}
	
	public TaskGroup getTaskGroup()
	{
		return group;
	}
	
	@Override
	public final R call() throws Exception
	{
		R result = this.execute();
		
		if (group != null)
			group.markTaskAsDone(id);
		
		return result;
	}
	
	/** Task handler to be implemented by subclasses. */
	protected abstract R execute() throws Exception;
	
	/** package-private initialization */
	void setup(String taskid, TaskGroup group)
	{
		this.id = taskid;
		this.group = group;
		if (group != null)
			group.addTask(id);
		
		log.getContext()
		   .add("TASK-" + taskid)
		   .update();
	}
}