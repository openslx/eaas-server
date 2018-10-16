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

import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeRequest;
import com.google.api.services.compute.Compute.GlobalOperations;
import com.google.api.services.compute.Compute.ZoneOperations;
import com.google.api.services.compute.model.Operation;


/** A wrapper for a compute operation resource. */
public class ComputeOperationWrapper extends ComputeRequestWrapper<Operation>
{
	private final Compute gce;
	private final String project;
	private Operation operation;
	private String zone;


	protected ComputeOperationWrapper(Builder builder)
	{
		super(builder.request, builder.minRetryInterval, builder.maxRetryIntervalDelta, builder.maxNumRetries);
		
		this.gce = builder.gce;
		this.project = builder.project;
		this.operation = builder.operation;
		this.zone = ComputeOperationWrapper.getZone(operation);
	}

	public Operation operation()
	{
		return operation;
	}

	@Override
	public Operation execute() throws IOException
	{
		if (operation == null) {
			// Send initial request first
			operation = super.execute();
			zone = ComputeOperationWrapper.getZone(operation);
		}
		else {
			// Update the operation resource
			final String opid = operation.getName();
			if (zone != null) {
				final ZoneOperations operations = gce.zoneOperations();
				operation = operations.get(project, zone, opid).execute();
			}
			else {
				final GlobalOperations operations = gce.globalOperations();
				operation = operations.get(project, opid).execute();
			}
		}

		return operation;
	}


	public static class Initializer extends ConfigData<Initializer>
	{
		public Initializer()
		{
			super();
		}
	}

	/** A builder for {@link ComputeOperationWrapper} objects. */
	public static class Builder extends ConfigData<Builder>
	{
		private IOException reqConstructionException;
		private ComputeRequest<Operation> request;
		private Operation operation;

		public Builder()
		{
			this(new Initializer());
		}

		public Builder(Initializer initializer)
		{
			super(initializer);
			this.operation = null;
		}

		public Builder setOperation(Operation operation)
		{
			this.operation = operation;
			this.request = null;
			return this;
		}
		
		public Builder setRequest(ComputeRequest<Operation> request)
		{
			this.request = request;
			this.operation = null;
			return this;
		}

		public Builder setRequest(ComputeRequestConstructor<Operation> constructor)
		{
			try {
				this.request = constructor.get();
				this.operation = null;
			}
			catch (IOException exception) {
				reqConstructionException = exception; 
			}

			return this;
		}

		public ComputeOperationWrapper build()
		{
			if (gce == null)
				throw new IllegalStateException("Google Compute service is missing!");

			if (project == null || project.isEmpty())
				throw new IllegalStateException("Project ID is missing!");

			if (minRetryInterval < 0L)
				throw new IllegalStateException("Min. retry interval is negative!");

			if (maxRetryIntervalDelta < 0L)
				throw new IllegalStateException("Max. retry interval delta is negative!");

			if (operation == null && request == null) {
				final String message = (reqConstructionException != null) ?
						"Construction of ComputeRequest<Operation> failed!" : "Operation is missing!";

				throw new IllegalStateException(message, reqConstructionException);
			}

			return new ComputeOperationWrapper(this);
		}
	}

	private static abstract class ConfigData<Derived> extends ComputeRequestWrapper.ConfigData<Derived>
	{
		protected Compute gce;
		protected String project;

		protected ConfigData()
		{
			super();
			
			this.gce = null;
			this.project = null;
		}

		protected <I> ConfigData(ConfigData<I> other)
		{
			super(other);
			
			this.gce = other.gce;
			this.project = other.project;
		}

		@SuppressWarnings("unchecked")
		public Derived setComputeService(Compute gce)
		{
			this.gce = gce;
			return (Derived) this;
		}

		@SuppressWarnings("unchecked")
		public Derived setProjectId(String project)
		{
			this.project = project;
			return (Derived) this;
		}
	}
	
	private static String getZone(Operation operation)
	{
		if (operation == null)
			return null;
		
		// Will be null for global/regional operations
		String zone = operation.getZone();
		if (zone != null) {
			final int index = zone.lastIndexOf('/');
			zone = zone.substring(index + 1);
		}
		
		return zone;
	}
}
