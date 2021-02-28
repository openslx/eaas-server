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

package de.bwl.bwfla.common.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TaskStack
{
	private final Deque<Task> tasks;
	private final Logger log;

	public TaskStack()
	{
		this(16);
	}

	public TaskStack(int capacity)
	{
		this(capacity, Logger.getLogger(TaskStack.class.getSimpleName()));
	}

	public TaskStack(Logger log)
	{
		this(16, log);
	}

	public TaskStack(int capacity, Logger log)
	{
		this.tasks = new ArrayDeque<>(capacity);
		this.log = log;
	}

	public void push(String name, IRunnable task)
	{
		this.push(new Task(name, task, log));
	}

	public void push(Task task)
	{
		tasks.push(task);
	}

	public Task pop()
	{
		return tasks.pop();
	}

	public boolean isEmpty()
	{
		return tasks.isEmpty();
	}

	public boolean execute()
	{
		boolean result = true;
		while (!this.isEmpty()) {
			final Task task = this.pop();
			result = task.run() && result;
		}

		return result;
	}

	@FunctionalInterface
	public interface IRunnable
	{
		void run() throws Exception;
	}

	public static class Task
	{
		private final Logger log;
		private final String name;
		private final IRunnable runnable;

		private Task(String name, IRunnable runnable, Logger log)
		{
			this.log = log;
			this.name = name;
			this.runnable = runnable;
		}

		public String name()
		{
			return name;
		}

		public IRunnable runnable()
		{
			return runnable;
		}

		public boolean run()
		{
			try {
				runnable.run();
				return true;
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Running task '" + name + "' failed!\n", error);
				return false;
			}
		}
	}
}
