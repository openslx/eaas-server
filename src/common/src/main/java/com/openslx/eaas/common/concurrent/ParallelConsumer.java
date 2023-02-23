/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.common.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Stream;


public class ParallelConsumer<T> extends ParallelProcessor<ParallelConsumer<T>>
{
	private final Predicate<T> filter;
	private final Consumer<T> consumer;

	public ParallelConsumer(Predicate<T> filter, Consumer<T> consumer)
	{
		if (filter == null)
			throw new IllegalArgumentException();

		if (consumer == null)
			throw new IllegalArgumentException();

		this.filter = filter;
		this.consumer = consumer;
	}

	public void consume(Stream<T> stream, ExecutorService executor)
			throws Exception
	{
		this.consume(stream.iterator(), executor);
	}

	public void consume(Iterator<T> iterator, ExecutorService executor)
			throws Exception
	{
		Exception error = null;

		// Submit subtasks for parallel processing
		final var results = new ArrayList<Future<?>>(this.getNumTasks());
		for (int i = this.getNumTasks() - 1; i > 0; --i) {
			final var task = new Task(iterator);
			results.add(executor.submit(task));
		}

		// Run task from current thread too!
		try {
			new Task(iterator).run();
		}
		catch (Exception exception) {
			error = exception;
		}

		// Wait for all subtasks
		for (final var result : results) {
			try {
				result.get();
			}
			catch (Exception exception) {
				error = exception;
			}
		}

		// Re-throw last error!
		if (error != null)
			throw error;
	}

	public CompletableFuture<Void> consumeAsync(Stream<T> stream, ExecutorService executor)
	{
		return this.consumeAsync(stream.iterator(), executor);
	}

	public CompletableFuture<Void> consumeAsync(Iterator<T> iterator, ExecutorService executor)
	{
		final Runnable runnable = () -> {
			try {
				this.consume(iterator, executor);
			}
			catch (Exception error) {
				throw new CompletionException(error);
			}
		};

		return CompletableFuture.runAsync(runnable, executor);
	}


	// ===== Internal Helpers ====================

	private class Task implements Runnable
	{
		private final Iterator<T> iterator;

		public Task(Iterator<T> iterator)
		{
			this.iterator = iterator;
		}

		@Override
		public void run()
		{
			try {
				T value = null;
				while ((value = this.next()) != null) {
					if (!filter.test(value))
						continue;

					consumer.accept(value);
				}
			}
			catch (Throwable error) {
				log.log(Level.WARNING, "Processing stream failed!", error);
			}
		}

		private T next()
		{
			synchronized (iterator) {
				return (iterator.hasNext()) ? iterator.next() : null;
			}
		}
	}
}
