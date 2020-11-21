package de.bwl.bwfla.eaas.cluster;

import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;


public class ResourceHandleTest
{
	protected final Logger log = Logger.getLogger(this.getClass().getName());

	@Test
	public void testFromString()
	{
		final ResourceHandle exphdl = new ResourceHandle("abc", new NodeID("127.0.0.1"), UUID.randomUUID());
		final ResourceHandle curhdl = ResourceHandle.fromString(exphdl.toString());
		Assert.assertTrue("Invalid resource handle!", curhdl.compareTo(exphdl) == 0);
	}
}
