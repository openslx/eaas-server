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

package de.bwl.bwfla.eaas.cluster.provider.iaas.gce;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;


/** A helper class for triggering async compute requests. */
public class ComputeRequestTrigger
{
	public static <T> CompletableFuture<T> submit(ComputeRequestWrapper<T> request, Executor executor)
	{
		return ComputeRequestTrigger.submit(request, executor, null);
	}

	public static <T> CompletableFuture<T> submit(ComputeRequestWrapper<T> request,
			Executor executor, ScheduledExecutorService scheduler)
	{
		final Predicate<T> predicate = (operation) -> true;
		return ComputeRequestTrigger.submit(request, predicate, executor, scheduler);
	}

	public static <T> CompletableFuture<T> submit(ComputeRequestWrapper<T> request,
			Predicate<T> predicate, Executor executor)
	{
		return ComputeRequestTrigger.submit(request, predicate, executor, null);
	}

	public static <T> CompletableFuture<T> submit(ComputeRequestWrapper<T> request,
			Predicate<T> predicate, Executor executor, ScheduledExecutorService scheduler)
	{
		if (request == null)
			throw new IllegalArgumentException("Request is missing!");

		if (predicate == null)
			throw new IllegalArgumentException("Response predicate is missing!");

		if (executor == null)
			throw new IllegalArgumentException("Executor is missing!");

		// scheduler is optional!

		final ComputeRequestTask<T> task =
				new ComputeRequestTask<T>(request, predicate, executor, scheduler);

		executor.execute(task);
		return task.completion();
	}


	/* =============== Internal Stuff =============== */

	private ComputeRequestTrigger()
	{
		// Empty
	}
}
