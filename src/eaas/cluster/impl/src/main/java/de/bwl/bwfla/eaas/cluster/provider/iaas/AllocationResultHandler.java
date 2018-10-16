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

import java.util.function.Consumer;
import java.util.logging.Logger;

import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;

// package-private

/** A helper class for computing callback's result when node allocation failures arise. */
class AllocationResultHandler
{
	private final Logger log;

	private final Consumer<ResourceSpec> onFailureCallback;
	private final MutableResourceSpec readyResources;
	private final ResourceSpec reqResources;
	private final int numRequestedNodes;
	private int numReadyNodes;
	private int numFailedNodes;

	public AllocationResultHandler(int numNodes, ResourceSpec reqResources,
			Consumer<ResourceSpec> onFailureCallback, Logger log)
	{
		this.log = log;
		this.onFailureCallback = onFailureCallback;
		this.readyResources = new MutableResourceSpec();
		this.reqResources = reqResources;
		this.numRequestedNodes = numNodes;
		this.numReadyNodes = 0;
		this.numFailedNodes = 0;
	}

	public synchronized void onNodeReady(ResourceSpec spec)
	{
		readyResources.add(spec);
		++numReadyNodes;
		this.update();
	}

	public synchronized void onNodeFailure()
	{
		++numFailedNodes;
		this.update();
	}

	private void update()
	{
		if (numReadyNodes + numFailedNodes != numRequestedNodes)
			return;

		if (numFailedNodes > 0) {
			ResourceSpec missing = MutableResourceSpec.fromDiff(reqResources, readyResources);
			onFailureCallback.accept(missing);
		}

		if (log != null) {
			final String message = new StringBuilder(512)
					.append("From ")
					.append(numRequestedNodes)
					.append(" requested node(s) ")
					.append(numReadyNodes)
					.append(" started successfully, ")
					.append(numFailedNodes)
					.append(" failed to boot")
					.toString();

			log.info(message);
		}
	}
}
