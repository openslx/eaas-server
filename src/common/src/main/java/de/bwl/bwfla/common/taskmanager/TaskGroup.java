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

import java.util.HashSet;
import java.util.Set;


public class TaskGroup
{
	private final Set<String> pendingTasks;
	private final Set<String> doneTasks;
	
	
	public TaskGroup()
	{
		this.pendingTasks = new HashSet<String>();
		this.doneTasks = new HashSet<String>();
	}
	
	public boolean addTask(String task)
	{
		return pendingTasks.add(task);
	}
	
	public boolean removeTask(String task)
	{
		return (pendingTasks.remove(task) || doneTasks.remove(task));
	}
	
	public Set<String> getPendingTasks()
	{
		return pendingTasks;
	}
	
	public Set<String> getDoneTasks()
	{
		return doneTasks;
	}
	
	public boolean hasPendingTasks()
	{
		return !pendingTasks.isEmpty();
	}

	public boolean hasDoneTasks()
	{
		return !doneTasks.isEmpty();
	}
	
	public boolean isDone()
	{
		return pendingTasks.isEmpty();
	}
	
	synchronized void markTaskAsDone(String task)
	{
		pendingTasks.remove(task);
		doneTasks.add(task);
	}
}
