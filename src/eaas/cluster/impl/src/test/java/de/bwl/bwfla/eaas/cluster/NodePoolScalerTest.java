package de.bwl.bwfla.eaas.cluster;

import java.util.concurrent.TimeUnit;

import de.bwl.bwfla.eaas.cluster.config.HeterogeneousNodePoolScalerConfig;
import de.bwl.bwfla.eaas.cluster.provider.NodePoolScaler;
import org.junit.Assert;
import org.junit.Test;

import de.bwl.bwfla.eaas.cluster.provider.Node;
import de.bwl.bwfla.eaas.cluster.provider.NodePool;


public class NodePoolScalerTest extends BaseTest
{
	private final Class<?> noopActionClazz = NodePoolScaler.NoopAction.class;
	private final Class<?> downActionClazz = NodePoolScaler.ScaleDownAction.class;
	private final Class<?> upActionClazz = NodePoolScaler.ScaleUpAction.class;

	@Test
	public void testNoScaling()
	{
		this.info("Testing NodePoolScaler's no-op action...");

		final HeterogeneousNodePoolScalerConfig config = NodePoolScalerTest.newConfig(0, 2);
		final NodePoolScaler poolscaler = NodePoolScaler.create(config);

		final int imax = 10;
		final int cpudelta = 10;
		final int memdelta = 10;

		// Empty node pool
		{
			final ResourceSpec spec = new ResourceSpec();
			final NodePool pool = new NodePool(false);

			NodePoolScaler.Action action = poolscaler.execute(pool, spec, spec, spec);
			NodePoolScalerTest.assertAction("", action, noopActionClazz);
		}

		// Empty node pool + pending
		{
			final ResourceSpec pending = NodePoolScalerTest.newResourceSpec(1);
			final MutableResourceSpec requested = new MutableResourceSpec(pending);
			final ResourceSpec spec = new ResourceSpec();
			final NodePool pool = new NodePool(false);
			pool.addPendingResources(pending);

			// Request resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, requested, spec);
				NodePoolScalerTest.assertAction("Requested: { " + requested + " }", action, noopActionClazz);
				NodePoolScalerTest.updateResourceSpec(requested, -cpudelta, -memdelta);
			}
		}

		// Node pool with 1 node
		{
			final Node node = NodePoolScalerTest.newNode("node-1", 1);
			final MutableResourceSpec used = new MutableResourceSpec(node.getCapacity());
			final MutableResourceSpec requested = new MutableResourceSpec();
			final ResourceSpec spec = new ResourceSpec();
			final NodePool pool = new NodePool(false);
			pool.registerNode(node);

			// Release used resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, spec, used);
				NodePoolScalerTest.assertAction("Used: { " + used + " }", action, noopActionClazz);
				NodePoolScalerTest.updateResourceSpec(used, -cpudelta, -memdelta);
			}

			// Request more resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScalerTest.updateResourceSpec(requested, cpudelta, memdelta);
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, requested, spec);
				NodePoolScalerTest.assertAction("Requested: { " + requested + " }", action, noopActionClazz);
			}

			// Allocate requested resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, requested, used);
				String message = "Requested: { " + requested + " }, Used: { " + used + " }";
				NodePoolScalerTest.assertAction(message, action, noopActionClazz);
				NodePoolScalerTest.updateResourceSpec(requested, -cpudelta, -memdelta);
				NodePoolScalerTest.updateResourceSpec(used, cpudelta, memdelta);
			}
		}

		// Node pool with 1 node + pending
		{
			final Node node = NodePoolScalerTest.newNode("node-1", 1);
			final MutableResourceSpec used = NodePoolScalerTest.newResourceSpec(1);
			final MutableResourceSpec requested = new MutableResourceSpec();
			final ResourceSpec pending = NodePoolScalerTest.newResourceSpec(1);
			final ResourceSpec spec = new ResourceSpec();
			final NodePool pool = new NodePool(false);
			pool.registerNode(node);
			pool.addPendingResources(pending);

			// Release used resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, spec, used);
				NodePoolScalerTest.assertAction("Used: { " + used + " }", action, noopActionClazz);
				NodePoolScalerTest.updateResourceSpec(used, -cpudelta, -memdelta);
			}

			// Request more resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScalerTest.updateResourceSpec(requested, 2 * cpudelta, 2 * memdelta);
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, requested, spec);
				NodePoolScalerTest.assertAction("Requested: { " + requested + " }", action, noopActionClazz);
			}

			// Allocate requested resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, requested, used);
				String message = "Requested: { " + requested + " }, Used: { " + used + " }";
				NodePoolScalerTest.assertAction(message, action, noopActionClazz);
				NodePoolScalerTest.updateResourceSpec(requested, -cpudelta, -memdelta);
				NodePoolScalerTest.updateResourceSpec(used, cpudelta, memdelta);
			}
		}
	}

	@Test
	public void testScalingUp()
	{
		this.info("Testing NodePoolScaler's scale-up action...");

		final HeterogeneousNodePoolScalerConfig config = NodePoolScalerTest.newConfig(0, 2);
		final NodePoolScaler poolscaler = NodePoolScaler.create(config);

		final int imax = 20;
		final int cpudelta = 10;
		final int memdelta = 10;

		// Empty node pool
		{
			final MutableResourceSpec requested = new MutableResourceSpec();
			final MutableResourceSpec missing = new MutableResourceSpec();
			final ResourceSpec spec = new ResourceSpec();
			final NodePool pool = new NodePool(false);

			// Request resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScalerTest.updateResourceSpec(requested, cpudelta, memdelta);
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, requested, spec);
				NodePoolScalerTest.assertAction("Requested: { " + requested + " }", action, upActionClazz);
				NodePoolScalerTest.assertScaleUpResource(action, requested);
			}

			// Request missing resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScalerTest.updateResourceSpec(missing, cpudelta, memdelta);
				NodePoolScaler.Action action = poolscaler.execute(pool, missing, spec, spec);
				NodePoolScalerTest.assertAction("Missing: { " + missing + " }", action, upActionClazz);
				NodePoolScalerTest.assertScaleUpResource(action, missing);
			}

			missing.set(spec);

			// Request more resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScalerTest.updateResourceSpec(requested, -cpudelta, -memdelta);
				NodePoolScalerTest.updateResourceSpec(missing, cpudelta, memdelta);
				NodePoolScaler.Action action = poolscaler.execute(pool, missing, requested, spec);
				NodePoolScalerTest.assertAction("Missing: { " + missing + " } Requested: { " + requested + " }", action, upActionClazz);

				ResourceSpec expected = MutableResourceSpec.max(missing, requested);
				NodePoolScalerTest.assertScaleUpResource(action, expected);
			}
		}

		// Empty node pool + pending
		{
			final ResourceSpec pending = NodePoolScalerTest.newResourceSpec(1);
			final MutableResourceSpec requested = new MutableResourceSpec(pending);
			final ResourceSpec spec = new ResourceSpec();
			final NodePool pool = new NodePool(false);
			pool.addPendingResources(pending);

			// Request resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScalerTest.updateResourceSpec(requested, cpudelta, memdelta);
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, requested, spec);
				NodePoolScalerTest.assertAction("Requested: { " + requested + " }", action, upActionClazz);

				MutableResourceSpec expected = null;
				if (i >= imax / 2) {
					// Upper pool size reached!
					expected = MutableResourceSpec.min(requested, config.getMaxPoolSize());
					expected = MutableResourceSpec.sub(expected, pending);
				}
				else expected = MutableResourceSpec.sub(requested, pending);

				NodePoolScalerTest.assertScaleUpResource(action, expected);
			}
		}

		// Node pool with 1 node
		{
			final Node node = NodePoolScalerTest.newNode("node-1", 1);
			final MutableResourceSpec used = new MutableResourceSpec(node.getCapacity());
			final MutableResourceSpec requested = new MutableResourceSpec();
			final ResourceSpec spec = new ResourceSpec();
			final NodePool pool = new NodePool(false);
			pool.registerNode(node);

			// Request resources...
			for (int i = 0; i < imax; ++i) {
				NodePoolScalerTest.updateResourceSpec(requested, cpudelta, memdelta);
				NodePoolScaler.Action action = poolscaler.execute(pool, spec, requested, used);
				NodePoolScalerTest.assertAction("Requested: { " + requested + " }", action, upActionClazz);

				MutableResourceSpec expected = null;
				if (i >= imax / 2) {
					// Upper pool size reached!
					expected = MutableResourceSpec.sub(config.getMaxPoolSize(), used);
					expected = MutableResourceSpec.min(requested, expected);
				}
				else expected = requested;

				NodePoolScalerTest.assertScaleUpResource(action, expected);
			}
		}
	}

	@Test
	public void testScalingDown()
	{
		this.info("Testing NodePoolScaler's scale-down action...");

		final HeterogeneousNodePoolScalerConfig config = NodePoolScalerTest.newConfig(0, 2);
		final NodePoolScaler poolscaler = NodePoolScaler.create(config);

		final int imax = 20;
		final int cpudelta = 10;
		final int memdelta = 10;

		// Node pool with 1 node
		{
			final Node node = NodePoolScalerTest.newNode("node-1", 1);
			final ResourceSpec spec = new ResourceSpec();
			final NodePool pool = new NodePool(false);
			pool.registerNode(node);

			// Request resources...
			NodePoolScaler.Action action = poolscaler.execute(pool, spec, spec, spec);
			NodePoolScalerTest.assertAction("", action, downActionClazz);

			NodePoolScaler.ScaleDownAction sda = (NodePoolScaler.ScaleDownAction) action;
			Assert.assertTrue(sda.getNodes().size() == 1);
			Assert.assertTrue(sda.getNodes().get(0) == node.getId());
		}

		// Node pool with 2 nodes
		{
			final Node node1 = NodePoolScalerTest.newNode("node-1", 1);
			final Node node2 = NodePoolScalerTest.newNode("node-2", 1);
			final ResourceSpec spec = new ResourceSpec();
			final NodePool pool = new NodePool(false);
			pool.registerNode(node1);
			pool.registerNode(node2);

			// Request resources...
			NodePoolScaler.Action action = poolscaler.execute(pool, spec, spec, spec);
			NodePoolScalerTest.assertAction("", action, downActionClazz);

			NodePoolScaler.ScaleDownAction sda = (NodePoolScaler.ScaleDownAction) action;
			Assert.assertTrue(sda.getNodes().size() == 2);
			Assert.assertTrue(sda.getNodes().get(0) == node1.getId());
			Assert.assertTrue(sda.getNodes().get(1) == node2.getId());
		}
	}


	private static MutableResourceSpec newResourceSpec(int n)
	{
		return new MutableResourceSpec(n * 100, n * 1000);
	}

	private static void updateResourceSpec(MutableResourceSpec spec, int cpudelta, int memdelta)
	{
		spec.cpu(spec.cpu() + cpudelta);
		spec.memory(spec.memory() + memdelta);
	}

	private static HeterogeneousNodePoolScalerConfig newConfig(int min, int max)
	{
		final HeterogeneousNodePoolScalerConfig config = new HeterogeneousNodePoolScalerConfig();
		config.setPoolScalingInterval(5, TimeUnit.SECONDS);
		config.setMinPoolSize(NodePoolScalerTest.newResourceSpec(min));
		config.setMaxPoolSize(NodePoolScalerTest.newResourceSpec(max));
		config.setMaxPoolSizeScaleUpAdjustment(NodePoolScalerTest.newResourceSpec(max));
		config.setMaxPoolSizeScaleDownAdjustment(NodePoolScalerTest.newResourceSpec(max));
		config.setNodeWarmUpPeriod(0L);
		config.setNodeCoolDownPeriod(0L);
		config.validate();

		return config;
	}

	private static Node newNode(String id, int cap)
	{
		ResourceSpec capacity = NodePoolScalerTest.newResourceSpec(cap);
		return new Node(new NodeID(id), capacity);
	}

	private static void assertAction(String prefix, NodePoolScaler.Action action, Class<?> expected)
	{
		final Class<?> actual = action.getClass();
		Assert.assertTrue(prefix + ", Action: " + actual.toString(), actual == expected);
	}

	private static void assertScaleUpResource(NodePoolScaler.Action action, ResourceSpec expected)
	{
		ResourceSpec actual = ((NodePoolScaler.ScaleUpAction) action).getResourceSpec();
		String details = " Expected: { " + expected.toString() + " }, Actual: { " + actual.toString() + " }";
		Assert.assertTrue("Resources are not scaled up properly!" + details, ResourceSpec.compare(actual, expected) == 0);
	}
}
