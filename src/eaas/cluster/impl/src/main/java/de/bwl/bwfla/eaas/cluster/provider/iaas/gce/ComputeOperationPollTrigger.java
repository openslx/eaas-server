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

import com.google.api.services.compute.model.Operation;


/** A helper class for triggering the polling of compute operations. */
public class ComputeOperationPollTrigger
{
	public static CompletableFuture<Operation> submit(ComputeOperationWrapper operation, Executor executor)
	{
		return ComputeOperationPollTrigger.submit(operation, executor, null);
	}

	public static CompletableFuture<Operation> submit(ComputeOperationWrapper operation,
			Executor executor, ScheduledExecutorService scheduler)
	{
		return ComputeRequestTrigger.submit(operation, PREDICATE, executor, scheduler);
	}


	/* =============== Internal Stuff =============== */

	private static final Predicate<Operation> PREDICATE = (operation) -> {
				return operation.getStatus().equals("DONE");
			};
	
	private ComputeOperationPollTrigger()
	{
		// Empty
	}
}
