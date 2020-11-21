package de.bwl.bwfla.eaas.cluster.metadata;

import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;


public class LabelSelectorWITHTest
{
	protected final Logger log = Logger.getLogger(this.getClass().getName());

	@Test
	public void testWithOperator()
	{
		this.testOperator(false);
	}

	@Test
	public void testWithoutOperator()
	{
		this.testOperator(true);
	}

	@Test
	public void testInvalidKeys()
	{
		for (String key : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
			this.testWithInvalidInput(key, false);
			this.testWithInvalidInput(key, true);
		}
	}

	private void testOperator(boolean negated)
	{
		final String expop = (negated) ? "WITHOUT" : "WITH";
		log.info("Testing " + expop + " operator...");

		try {
			final String expkey = "release";
			final String expvalue = "stable";

			final LabelSelectorParser parser = new LabelSelectorParser();
			LabelSelectorWITH selector = LabelSelectorWITH.create(expkey, negated);
			selector = (LabelSelectorWITH) parser.parse(selector.toString());

			final String curkey = selector.getKey();
			Assert.assertTrue("Invalid key! Expected: '" + expkey + "', Parsed: '" + curkey + "'", expkey.equals(curkey));

			final Label label = new Label(expkey, expvalue);
			if (negated)
				Assert.assertTrue("Matching label failed!", !selector.match(label));
			else Assert.assertTrue("Matching label failed!", selector.match(label));
		}
		catch (Exception exception) {
			exception.printStackTrace();
			Assert.fail();
		}
	}

	private void testWithInvalidInput(String key, boolean negated)
	{
		final String expop = LabelSelectorEQ.operator(negated);
		log.info("Testing " + expop + " operator with invalid input: key=" + key);

		try {
			LabelSelectorWITH.create(key, negated);
		}
		catch (IllegalArgumentException exception) {
			return;  // Expected outcome!
		}
		catch (Exception exception) {
			String message = "IllegalArgumentException was expected, but caught:  "
					+ exception.getMessage();

			exception.printStackTrace();
			Assert.fail(message);
		}

		Assert.fail("Illegal arguments were undetected!");
	}
}
