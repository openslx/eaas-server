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

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.api.client.http.HttpResponseException;
import com.google.api.services.compute.ComputeRequest;


/** A wrapper for a compute request. */
public class ComputeRequestWrapper<T>
{
	/** HTTP status code for rate-limited servers */
	// Source: https://tools.ietf.org/html/rfc6585
	public static final int TOO_MANY_REQUESTS = 429;

	/** HTTP status code for missing resource */
	public static final int RESOURCE_NOT_FOUND = 404;
	
	// Member fields
	private final ComputeRequest<T> request;
	private final long minRetryInterval;
	private final int maxRetryIntervalDelta;
	private final int maxNumRetries;
	private int numRetries;
	private Random random;


	protected ComputeRequestWrapper(Builder<T> builder)
	{
		this(builder.request, builder.minRetryInterval,
				builder.maxRetryIntervalDelta, builder.maxNumRetries);
	}
	
	protected ComputeRequestWrapper(ComputeRequest<T> request,
			long minRetryInterval, int maxRetryIntervalDelta, int maxNumRetries)
	{
		this.request = request;
		this.minRetryInterval = minRetryInterval;
		this.maxRetryIntervalDelta = maxRetryIntervalDelta;
		this.maxNumRetries = maxNumRetries;
		this.numRetries = 0;
		this.random = null;
	}

	public ComputeRequest<T> unwrap()
	{
		return request;
	}

	public T execute() throws IOException
	{
		return request.execute();
	}

	public long nextRetryDelay()
	{
		if (numRetries >= maxNumRetries)
			return -1;

		++numRetries;

		if (maxRetryIntervalDelta == 0)
			return minRetryInterval;

		if (random == null)
			random = new Random();

		int delta = random.nextInt(maxRetryIntervalDelta);
		return (minRetryInterval + (long) delta);
	}

	public static boolean isRateLimited(Exception exception)
	{
		if (!(exception instanceof HttpResponseException))
			return false;

		HttpResponseException cause = (HttpResponseException) exception;
		return (cause.getStatusCode() == TOO_MANY_REQUESTS);
	}


	public static class Initializer extends ConfigData<Initializer>
	{
		public Initializer()
		{
			super();
		}
	}

	/** A builder for {@link ComputeRequestWrapper} objects. */
	public static class Builder<T> extends ConfigData<Builder<T>>
	{
		private IOException reqConstructionException;
		private ComputeRequest<T> request;

		public Builder()
		{
			this(new Initializer());
		}

		public Builder(Initializer initializer)
		{
			super(initializer);
			this.reqConstructionException = null;
			this.request = null;
		}

		public Builder<T> setRequest(ComputeRequest<T> request)
		{
			this.request = request;
			return this;
		}

		public Builder<T> setRequest(ComputeRequestConstructor<T> constructor)
		{
			try {
				this.request = constructor.get();
			}
			catch (IOException exception) {
				reqConstructionException = exception; 
			}

			return this;
		}

		public ComputeRequestWrapper<T> build()
		{
			if (request == null) {
				final String message = (reqConstructionException != null) ?
						"Construction of ComputeRequest failed!" : "ComputeRequest is missing!";

				throw new IllegalStateException(message, reqConstructionException);
			}

			if (minRetryInterval < 0L)
				throw new IllegalStateException("Min. retry interval is negative!");

			if (maxRetryIntervalDelta < 0)
				throw new IllegalStateException("Max. retry interval delta is negative!");

			if (maxNumRetries < 0)
				throw new IllegalStateException("Max. number of retries is negative!");

			return new ComputeRequestWrapper<T>(this);
		}
	}

	protected static abstract class ConfigData<Derived>
	{
		protected long minRetryInterval;
		protected int maxRetryIntervalDelta;
		protected int maxNumRetries;

		protected ConfigData()
		{
			this.minRetryInterval = TimeUnit.SECONDS.toMillis(0L);
			this.maxRetryIntervalDelta = (int) TimeUnit.SECONDS.toMillis(1L);
			this.maxNumRetries = 3;
		}

		protected <I> ConfigData(ConfigData<I> other)
		{
			this.minRetryInterval = other.minRetryInterval;
			this.maxRetryIntervalDelta = other.maxRetryIntervalDelta;
			this.maxNumRetries = other.maxNumRetries;
		}

		@SuppressWarnings("unchecked")
		public Derived setRetryInterval(long interval, TimeUnit unit)
		{
			this.minRetryInterval = unit.toMillis(interval);
			return (Derived) this;
		}

		@SuppressWarnings("unchecked")
		public Derived setRetryInterval(long min, long max, TimeUnit unit)
		{
			this.minRetryInterval = unit.toMillis(min);
			this.maxRetryIntervalDelta = (int) (unit.toMillis(max) - this.minRetryInterval);
			return (Derived) this;
		}

		@SuppressWarnings("unchecked")
		public Derived setRetryIntervalDelta(long delta, TimeUnit unit)
		{
			this.maxRetryIntervalDelta = (int) unit.toMillis(delta);
			return (Derived) this;
		}

		@SuppressWarnings("unchecked")
		public Derived setMaxNumRetries(int number)
		{
			this.maxNumRetries = number;
			return (Derived) this;
		}
		
		@SuppressWarnings("unchecked")
		public Derived setUnlimitedNumRetries()
		{
			this.maxNumRetries = Integer.MAX_VALUE;
			return (Derived) this;
		}
	}
}
