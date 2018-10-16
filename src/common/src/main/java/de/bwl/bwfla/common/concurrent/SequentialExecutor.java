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

package de.bwl.bwfla.common.concurrent;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SequentialExecutor extends AbstractExecutorService
{
	private static final int PRIORITY_DEFAULT = 0;

	private final ExecutorService executor;
	private final Queue<Task> tasks;
	private final Logger log;
	private boolean active;
	
	/** Time for executing tasks per batch (in ms) */
	private long maxBatchExecutionTime;
	
	public SequentialExecutor(ExecutorService executor, int maxQueueSize)
	{
		this(Logger.getLogger(SequentialExecutor.class.getName()), executor, maxQueueSize);
	}
	
	public SequentialExecutor(Logger log, ExecutorService executor, int maxQueueSize)
	{
		this.executor = executor;
		this.tasks = new PriorityQueue<Task>(maxQueueSize);
		this.log = log;
		this.active = false;
		this.maxBatchExecutionTime = 250L;
	}

	
	/* =============== ExecutorService Implementation =============== */
	
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
	{
		return executor.awaitTermination(timeout, unit);
	}

	@Override
	public boolean isShutdown()
	{
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated()
	{
		return executor.isTerminated();
	}

	@Override
	public void shutdown()
	{
		executor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		List<Runnable> pending = executor.shutdownNow();
		for (Task task : tasks)
			pending.add(task.runnable);
		
		return pending;
	}

	@Override
	public <T> Future<T> submit(Callable<T> callable)
	{
		return this.submit(PRIORITY_DEFAULT, callable);
	}

	@Override
	public Future<?> submit(Runnable runnable)
	{
		return this.submit(PRIORITY_DEFAULT, runnable);
	}

	@Override
	public <T> Future<T> submit(Runnable runnable, T result)
	{
		return this.submit(PRIORITY_DEFAULT, runnable, result);
	}
	
	@Override
	public void execute(Runnable command)
	{
		this.execute(PRIORITY_DEFAULT, command);
	}
	
	
	/* =============== SequentialExecutor Extensions =============== */
	
	public synchronized void execute(int priority, Runnable runnable)
	{
		if (runnable== null)
			throw new NullPointerException();
		
		// Invert priority, since the queue uses natural-ordering:
		// smaller priority value -> higher task priority
		tasks.add(new Task(-priority, runnable));
		if (!active)
			this.submit();
	}
	
	public Future<?> submit(int priority, Runnable runnable)
	{
		RunnableFuture<Object> future = super.newTaskFor(runnable, null);
		this.execute(priority, future);
		return future;
	}

	public <T> Future<T> submit(int priority, Runnable runnable, T result)
	{
		RunnableFuture<T> future = super.newTaskFor(runnable, result);
		this.execute(priority, future);
		return future;
	}

	public <T> Future<T> submit(int priority, Callable<T> callable)
	{
		RunnableFuture<T> future = super.newTaskFor(callable);
		this.execute(priority, future);
		return future;
	}
	
	public long getMaxBatchExecutionTime()
	{
		return maxBatchExecutionTime;
	}
	
	public SequentialExecutor setMaxBatchExecutionTime(long time, TimeUnit unit)
	{
		if (time < 1L)
			throw new IllegalArgumentException();
		
		this.maxBatchExecutionTime = unit.toMillis(time);
		return this;
	}

	public static int getDefaultPriority()
	{
		return PRIORITY_DEFAULT;
	}
	
	
	/* =============== Internal Helpers =============== */
	
	private synchronized void submit()
	{
		active = !tasks.isEmpty();
		if (active)
			executor.execute(new BatchTask(this, log));
	}
	
	private synchronized Task nextPendingTask()
	{
		return tasks.poll();
	}
	
	private static class Task implements Runnable, Comparable<Task>
	{
		private final long timestamp;
		private final int priority;
		private final Runnable runnable;
		
		public Task(int priority, Runnable runnable)
		{
			this.timestamp = System.nanoTime();
			this.priority = priority;
			this.runnable = runnable;
		}
		
		public long timestamp()
		{
			return timestamp;
		}
		
		public int priority()
		{
			return priority;
		}
		
		@Override
		public void run()
		{
			runnable.run();
		}

		@Override
		public int compareTo(Task other)
		{
			final int result = Integer.compare(priority, other.priority());
			return (result != 0) ? result : Long.compare(timestamp, other.timestamp());
		}
	}
	
	private static class BatchTask implements Runnable
	{
		private final SequentialExecutor executor;
		private final Logger log;
		
		public BatchTask(SequentialExecutor executor, Logger log)
		{
			this.executor = executor;
			this.log = log;
		}
		
		@Override
		public void run()
		{
			final long deadline = BatchTask.now() + executor.getMaxBatchExecutionTime();
			
			// Execute pending tasks until the max. processing time elapses...
			do {
				final Task task = executor.nextPendingTask();
				if (task == null)
					break;
				
				try {
					task.run();
				}
				catch (Throwable error) {
					log.log(Level.WARNING, "Executing sequential task failed!\n", error);
				}
			}
			while (BatchTask.now() < deadline);
			
			// Submit next batch
			executor.submit();
		}
		
		public static long now()
		{
			return System.currentTimeMillis();
		}
	}
}
