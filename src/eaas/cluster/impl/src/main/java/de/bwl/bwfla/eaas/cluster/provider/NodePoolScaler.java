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

package de.bwl.bwfla.eaas.cluster.provider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.config.HeterogeneousNodePoolScalerConfig;
import de.bwl.bwfla.eaas.cluster.config.HomogeneousNodePoolScalerConfig;
import de.bwl.bwfla.eaas.cluster.config.NodePoolScalerConfig;


public class NodePoolScaler
{
	private static final NoopAction NOOP_ACTION = new NoopAction();
	
	private final NodePoolScalerConfig config;
	private final ResourceSpec minPoolSize;
	private final ResourceSpec maxPoolSize;
	private final ResourceSpec maxScaleUpAdjustment;
	private final ResourceSpec maxScaleDownAdjustment;
	
	
	public static NodePoolScaler create(HomogeneousNodePoolScalerConfig config, ResourceSpec nodeCapacity)
	{
		final ResourceSpec minPoolSize = ResourceSpec.create(config.getMinPoolSize(), nodeCapacity);
		final ResourceSpec maxPoolSize = ResourceSpec.create(config.getMaxPoolSize(), nodeCapacity);
		final ResourceSpec maxUpAdjustment = ResourceSpec.create(config.getMaxPoolSizeScaleUpAdjustment(), nodeCapacity);
		final ResourceSpec maxDownAdjustment = ResourceSpec.create(config.getMaxPoolSizeScaleDownAdjustment(), nodeCapacity);
		return new NodePoolScaler(config, minPoolSize, maxPoolSize, maxUpAdjustment, maxDownAdjustment);
	}
	
	public static NodePoolScaler create(HeterogeneousNodePoolScalerConfig config)
	{
		return new NodePoolScaler(config, config.getMinPoolSize(), config.getMaxPoolSize(),
				config.getMaxPoolSizeScaleUpAdjustment(), config.getMaxPoolSizeScaleDownAdjustment());
	}
	
	protected NodePoolScaler(NodePoolScalerConfig config, ResourceSpec minPoolSize, ResourceSpec maxPoolSize,
			ResourceSpec maxScaleUpAdjustment, ResourceSpec maxScaleDownAdjustment)
	{
		this.config = config;
		this.minPoolSize = minPoolSize;
		this.maxPoolSize = maxPoolSize;
		this.maxScaleUpAdjustment = maxScaleUpAdjustment;
		this.maxScaleDownAdjustment = maxScaleDownAdjustment;
	}
	
	public NodePoolScalerConfig getConfig()
	{
		return config;
	}

	public boolean isMaxPoolSizeReached(NodePool pool)
	{
		final ResourceSpec capacity = pool.getCapacity();
		return ResourceSpec.compare(capacity, maxPoolSize) >= 0;
	}
	
	public Action execute(NodePool pool, ResourceSpec missing, ResourceSpec requested, ResourceSpec used)
	{
		final ResourceSpec capacity = pool.getCapacity();
		if (ResourceSpec.compare(used, capacity) > 0)
			throw new IllegalStateException("Used resources are above node-pool's capacity!");
		
		// Compute the goal capacity...
		MutableResourceSpec goal = MutableResourceSpec.add(used, requested);
		if (missing.isDefined()) {
			// Resources are missing, the current pool's capacity is not enough!
			MutableResourceSpec spec = MutableResourceSpec.add(capacity, missing);
			goal = MutableResourceSpec.max(goal, spec);
		}
		
		// Check poolsize bounds
		goal.max(minPoolSize);
		goal.min(maxPoolSize);
		
		Action action = null;
		
		final int cmp = ResourceSpec.compare(capacity, goal);
		if (cmp < 0) {
			// Scale-up the node pool, considering the pending resources and limits
			final ResourceSpec pending = MutableResourceSpec.add(capacity, pool.getPendingResources());
			ResourceSpec spec = MutableResourceSpec.fromDiff(goal, pending);
			spec = MutableResourceSpec.min(spec, maxScaleUpAdjustment);
			action = (spec.isDefined()) ? new ScaleUpAction(spec) : NOOP_ACTION;
		}
		else if ((cmp > 0) && (pool.getNumUnusedNodes() > 0)) {
			// Scale-down node pool by looking at currently unused nodes
			MutableResourceSpec unused = MutableResourceSpec.fromDiff(capacity, goal);
			unused = MutableResourceSpec.min(unused, maxScaleDownAdjustment);
			
			Stream<Node> nodes = pool.getUnusedNodes().stream();
			if (!pool.isHomogeneous()) {
				// Nodes can have different capacities!
				// Sort the nodes by their ascending capacity.
				final Comparator<Node> comparator = (n1, n2) -> {
					final ResourceSpec c1 = n1.getCapacity();
					final ResourceSpec c2 = n2.getCapacity();
					return ResourceSpec.compare(c1, c2);
				};
				
				nodes = nodes.sorted(comparator);
			}
			
			final ArrayList<NodeID> nodesToStop = new ArrayList<NodeID>();
			for (Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
				// An unused node found...
				final Node node = it.next();
				ResourceSpec spec = node.getCapacity();
				if (ResourceSpec.compare(spec, unused) > 0)
					break;  // This and all other nodes are too big!
				
				if (node.getUptime() < config.getNodeWarmUpPeriod())
					continue;  // Node was "recently" booted!
				
				if (node.getUnusedTime() < config.getNodeCoolDownPeriod())
					continue;  // Node was "recently" used!
				
				nodesToStop.add(node.getId());
				unused.sub(spec, true);
				if (!unused.isDefined())
					break;
			}
			
			action = (nodesToStop.isEmpty()) ? NOOP_ACTION : new ScaleDownAction(nodesToStop);
		}
		else {
			// Nothing to do!
			action = NOOP_ACTION;
		}
		
		return action;
	}
	
	
	public static abstract class Action
	{
		// Empty class!
	}
	
	public static class NoopAction extends Action
	{
		// Empty class!
	}
	
	public static class ScaleUpAction extends Action
	{
		private final ResourceSpec spec;
		
		public ScaleUpAction(ResourceSpec spec)
		{
			this.spec = spec;
		}

		public ResourceSpec getResourceSpec()
		{
			return spec;
		}
	}
	
	public static class ScaleDownAction extends Action
	{
		private final List<NodeID> nodes;
		
		public ScaleDownAction(List<NodeID> nodes)
		{
			this.nodes = nodes;
		}
		
		public List<NodeID> getNodes()
		{
			return nodes;
		}
	}
}
