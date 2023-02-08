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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Stream;


public class ParallelReducer<T,R> extends ParallelProcessor<ParallelReducer<T,R>>
{
	private final Predicate<T> filter;
	private final Function<T,R> mapper;
	private final BinaryOperator<R> reducer;

	public ParallelReducer(Predicate<T> filter, Function<T,R> mapper, BinaryOperator<R> reducer)
	{
		if (filter == null)
			throw new IllegalArgumentException();

		if (mapper == null)
			throw new IllegalArgumentException();

		if (reducer == null)
			throw new IllegalArgumentException();

		this.filter = filter;
		this.mapper = mapper;
		this.reducer = reducer;
	}

	public R reduce(R identity, Stream<T> stream, ExecutorService executor)
			throws Exception
	{
		return this.reduce(identity, stream.iterator(), executor);
	}

	public R reduce(R identity, Iterator<T> iterator, ExecutorService executor)
			throws Exception
	{
		if (identity == null)
			throw new IllegalArgumentException();

		Exception error = null;

		// Submit subtasks for parallel processing
		final var results = new ArrayList<Future<R>>(this.getNumTasks());
		for (int i = this.getNumTasks(); i > 0; --i) {
			final var task = new Task(iterator, identity);
			results.add(executor.submit(task));
		}

		// Reduce all sub-results
		R result = identity;
		for (final var subresult : results) {
			try {
				result = reducer.apply(result, subresult.get());
			}
			catch (Exception exception) {
				error = exception;
			}
		}

		// Re-throw last error!
		if (error != null)
			throw error;

		return result;
	}

	public CompletableFuture<R> reduceAsync(R identity, Stream<T> stream, ExecutorService executor)
	{
		return this.reduceAsync(identity, stream.iterator(), executor);
	}

	public CompletableFuture<R> reduceAsync(R identity, Iterator<T> iterator, ExecutorService executor)
	{
		final Supplier<R> supplier = () -> {
			try {
				return this.reduce(identity, iterator, executor);
			}
			catch (Exception error) {
				throw new CompletionException(error);
			}
		};

		return CompletableFuture.supplyAsync(supplier, executor);
	}


	// ===== Internal Helpers ====================

	private class Task implements Callable<R>
	{
		private final Iterator<T> iterator;
		private final R identity;

		public Task(Iterator<T> iterator, R identity)
		{
			this.iterator = iterator;
			this.identity = identity;
		}

		@Override
		public R call()
		{
			try {
				R result = identity;
				T valnext = null;
				while ((valnext = this.next()) != null) {
					if (!filter.test(valnext))
						continue;

					final var valmapped = mapper.apply(valnext);
					result = reducer.apply(result, valmapped);
				}

				return result;
			}
			catch (Throwable error) {
				log.log(Level.WARNING, "Processing stream failed!", error);
				throw new RuntimeException(error);
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
