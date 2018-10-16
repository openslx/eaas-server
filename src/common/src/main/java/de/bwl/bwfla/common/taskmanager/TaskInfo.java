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

import java.util.concurrent.Future;


public class TaskInfo<R>
{
	private final AbstractTask<R> task;
	private final Future<R> result;
	private Object userdata;
	
	TaskInfo(AbstractTask<R> task, Future<R> result, Object userdata)
	{
		this.task = task;
		this.result = result;
		this.userdata = userdata;
	}
	
	public AbstractTask<R> task()
	{
		return task;
	}
	
	public <T> T task(Class<T> clazz)
	{
		return clazz.cast(task);
	}
	
	public Future<R> result()
	{
		return result;
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
}
