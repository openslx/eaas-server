package de.bwl.bwfla.eaas.cluster.metadata;

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;


public class LabelIndexTest
{
	private final Logger log = Logger.getLogger(this.getClass().getName());

	@Test
	public void testUsingSelectorEQ()
	{
		final LabelSelectorParser parser = new LabelSelectorParser();
		final LabelIndex index = new LabelIndex();
		index.add(new Label("zone", "eu-north-1"));
		index.add(new Label("tier", "backend"));

		this.execute(parser, index, "zone == eu-north-1", true);
		this.execute(parser, index, "zone != eu-north-1", false);
		this.execute(parser, index, "zone == unknown", false);
		this.execute(parser, index, "zone != unknown", true);

		this.execute(parser, index, "tier == backend", true);
		this.execute(parser, index, "tier != backend", false);
		this.execute(parser, index, "tier == unknown", false);
		this.execute(parser, index, "tier != unknown", true);

		this.execute(parser, index, "missing == unknown", false);
		this.execute(parser, index, "missing != unknown", true);
	}

	@Test
	public void testUsingSelectorIN()
	{
		final LabelSelectorParser parser = new LabelSelectorParser();
		final LabelIndex index = new LabelIndex();
		index.add(new Label("zone", "eu-north-1"));
		index.add(new Label("tier", "backend"));

		this.execute(parser, index, "zone in (eu-north-1)", true);
		this.execute(parser, index, "zone !in (eu-north-1)", false);
		this.execute(parser, index, "zone in (eu-north-1, uknown)", true);
		this.execute(parser, index, "zone !in (eu-north-1, unknown)", false);
		this.execute(parser, index, "zone in (unknown)", false);
		this.execute(parser, index, "zone !in (unknown)", true);

		this.execute(parser, index, "tier in (backend)", true);
		this.execute(parser, index, "tier !in (backend)", false);
		this.execute(parser, index, "tier in (backend, unknown)", true);
		this.execute(parser, index, "tier !in (backend, unknown)", false);
		this.execute(parser, index, "tier in (unknown)", false);
		this.execute(parser, index, "tier !in (unknown)", true);

		this.execute(parser, index, "missing in (unknown)", false);
		this.execute(parser, index, "missing !in (unknown)", true);
	}

	@Test
	public void testUsingSelectorWITH()
	{
		final LabelSelectorParser parser = new LabelSelectorParser();
		final LabelIndex index = new LabelIndex();
		index.add(new Label("zone", "eu-north-1"));
		index.add(new Label("tier", "backend"));

		this.execute(parser, index, "zone", true);
		this.execute(parser, index, "!zone", false);

		this.execute(parser, index, "tier", true);
		this.execute(parser, index, "!tier", false);

		this.execute(parser, index, "missing", false);
		this.execute(parser, index, "!missing", true);
	}

	private void execute(LabelSelectorParser parser, LabelIndex index, String selstr, boolean outcome)
	{
		try {
			final LabelSelector selector = parser.parse(selstr);
			final String message = "Applying selector '" + selstr + "' failed!";
			if (outcome)
				Assert.assertTrue(message, index.apply(selector));
			else Assert.assertFalse(message, index.apply(selector));
		}
		catch (Exception error) {
			Assert.fail();
		}
	}
}
