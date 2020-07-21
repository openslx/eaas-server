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

import java.util.concurrent.CompletableFuture;


public class TaskInfo<R>
{
	private final AbstractTask<R> task;
	private Object userdata;
	private long lastAccessTimestamp;
	
	TaskInfo(AbstractTask<R> task, Object userdata)
	{
		this.task = task;
		this.userdata = userdata;
		this.lastAccessTimestamp = TaskInfo.now();
	}
	
	public AbstractTask<R> task()
	{
		return task;
	}
	
	public <T> T task(Class<T> clazz)
	{
		return clazz.cast(task);
	}
	
	public CompletableFuture<R> result()
	{
		return task.getTaskResult();
	}
	
	public Object userdata()
	{
		return userdata;
	}
	
	public <T> T userdata(Class<T> clazz)
	{
		return clazz.cast(userdata);
	}
	
	public void setUserData(Object userdata)
	{
		this.userdata = userdata;
	}

	public long getAccessTimestamp()
	{
		return lastAccessTimestamp;
	}

	void updateAccessTimestamp()
	{
		this.lastAccessTimestamp = TaskInfo.now();
	}

	public static long now()
	{
		return System.currentTimeMillis();
	}

}
