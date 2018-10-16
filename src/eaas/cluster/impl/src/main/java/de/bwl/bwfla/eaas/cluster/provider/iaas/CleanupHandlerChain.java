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

package de.bwl.bwfla.eaas.cluster.provider.iaas;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

// package-private

class CleanupHandlerChain
{
	private final Deque<Object> handlers;
	private final String name;
	private final Logger log;

	public CleanupHandlerChain(String name, Logger log)
	{
		this.handlers = new ArrayDeque<Object>();
		this.name = name;
		this.log = log;
	}

	public void add(Runnable handler)
	{
		handlers.push(handler);
	}

	public void add(Callable<CompletableFuture<Void>> handler)
	{
		handlers.push(handler);
	}

	public void execute()
	{
		final int numHandlers = handlers.size();
		int numHandlersLeft = numHandlers;

		log.info("Executing " + numHandlers + " cleanup handler(s) for '" + name + "'...");
		final CompletableFuture<Void> trigger = new CompletableFuture<Void>();
		CompletableFuture<Void> curstage = trigger;

		// Run handlers in LIFO order...
		for (Object handler : handlers) {
			if (handler instanceof Runnable) {
				// Case: sync handler
				curstage = curstage.thenRun((Runnable) handler);
			}
			else if (handler instanceof Callable) {
				// Case: async handler, we need a trigger for the execution...
				final Function<Void, CompletableFuture<Void>> functor = (unused) -> {
					@SuppressWarnings("unchecked")
					final Callable<CompletableFuture<Void>> callable =
							(Callable<CompletableFuture<Void>>) handler;

					try {
						return callable.call();
					}
					catch (Exception exception) {
						throw new CompletionException(exception);
					}
				};

				curstage = curstage.thenCompose(functor);
			}
			else {
				// Should never happen!
				final String clazz = handler.getClass().getName();
				throw new IllegalStateException("Unsupported class: " + clazz);
			}

			final int curnum = numHandlersLeft;
			final BiFunction<Void, Throwable, Void> onDoneFtor = (unused, error) -> {
				if (error != null) {
					final String message = "Executing " + curnum
							+ ". cleanup handler for '" + name + "' failed!\n";

					log.log(Level.WARNING, message, error);
				}
				else {
					log.info("Done executing " + curnum + ". cleanup handler for '" + name + "'");
				}

				return null;
			};

			curstage = curstage.handle(onDoneFtor);
			--numHandlersLeft;
		}

		final Runnable onDoneAction = () -> {
			log.info("Done executing all cleanup handlers for '" + name + "'");
		};

		curstage.thenRun(onDoneAction);

		// Start the execution
		trigger.complete(null);
	}
}
