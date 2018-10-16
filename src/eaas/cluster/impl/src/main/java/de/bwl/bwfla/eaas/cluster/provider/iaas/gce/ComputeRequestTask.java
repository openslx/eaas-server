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
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;


// package-private class

final class ComputeRequestTask<T> implements Runnable
{
	private final ComputeRequestWrapper<T> request;
	private final CompletableFuture<T> result;
	private final Predicate<T> predicate;
	private final ScheduledExecutorService scheduler;
	private final Executor executor;


	public ComputeRequestTask(ComputeRequestWrapper<T> request, Predicate<T> predicate,
			Executor executor, ScheduledExecutorService scheduler)
	{
		this.request = request;
		this.result = new CompletableFuture<T>();
		this.predicate = predicate;
		this.scheduler = scheduler;
		this.executor = executor;
	}

	public CompletableFuture<T> completion()
	{
		return result;
	}

	@Override
	public void run()
	{
		Exception error = null;

		while (true) {
			try {
				final T response = request.execute();
				if (predicate.test(response)) {
					result.complete(response);
					return;
				}
			}
			catch (Exception exception) {
				if (!ComputeRequestWrapper.isRateLimited(exception)) {
					final String message = "Executing HTTP request failed!";
					error = new ComputeRequestException(message, exception, request.unwrap());
					break;
				}
			}

			// A rate-limit is reached, retry later
			final long delay = request.nextRetryDelay();
			if (delay < 0)
				break;  // Max. number of attemps reached!

			if (scheduler == null) {
				// Block current thread
				ComputeRequestTask.sleep(delay);
			}
			else {
				// Re-submit itself again
				final Runnable trigger = () -> executor.execute(this);
				scheduler.schedule(trigger, delay, TimeUnit.MILLISECONDS);
				return;
			}
		}

		if (error == null) {
			final String message = "HTTP request failed due to rate-limiting!";
			error = new ComputeRequestException(message, request.unwrap());
		}

		result.completeExceptionally(error);
	}
	
	private static void sleep(long millis)
	{
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException exception) {
			// Do nothing!
		}
	}
}