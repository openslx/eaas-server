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

package de.bwl.bwfla.eaas.cluster;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;


public class ClusterManagerExecutors
{
	private final ScheduledExecutorService scheduler;
	private final ExecutorService compExecutor;
	private final ExecutorService ioExecutor;
	
	public ClusterManagerExecutors()
	{
		Logger log = Logger.getLogger(ClusterManagerExecutors.class.getName());
		this.scheduler = ClusterManagerExecutors.lookup("java:jboss/ee/concurrency/scheduler/default", log);
		this.compExecutor = ClusterManagerExecutors.lookup("java:jboss/ee/concurrency/executor/compute", log);
		this.ioExecutor = ClusterManagerExecutors.lookup("java:jboss/ee/concurrency/executor/io", log);
	}
	
	public ClusterManagerExecutors(ScheduledExecutorService scheduler, ExecutorService computation, ExecutorService io)
	{
		this.scheduler = scheduler;
		this.compExecutor = computation;
		this.ioExecutor = io;
	}
	
	public ScheduledExecutorService scheduler()
	{
		return scheduler;
	}
	
	public ExecutorService computation()
	{
		return compExecutor;
	}
	
	public ExecutorService io()
	{
		return ioExecutor;
	}
	
	
	/* ========== Internal Stuff ========== */
	
	private static <T> T lookup(String name, Logger log)
	{
		try {
			return InitialContext.doLookup(name);
		}
		catch (Exception exception) {
			log.log(Level.SEVERE, "Lookup for '" + name + "' failed!", exception);
			return null;
		}
	}
}
