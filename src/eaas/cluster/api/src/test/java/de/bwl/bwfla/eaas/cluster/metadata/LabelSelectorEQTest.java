package de.bwl.bwfla.eaas.cluster.metadata;

import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;


public class LabelSelectorEQTest
{
	protected final Logger log = Logger.getLogger(this.getClass().getName());

	@Test
	public void testEqualOperator()
	{
		this.testOperator(false);
	}

	@Test
	public void testNotEqualOperator()
	{
		this.testOperator(true);
	}

	@Test
	public void testInvalidKeys()
	{
		final String value = "valid-value";
		for (String key : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
			this.testWithInvalidInput(key, value, false);
			this.testWithInvalidInput(key, value, true);
		}
	}

	@Test
	public void testInvalidValues()
	{
		final String key = "valid-key";
		for (String value : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
			this.testWithInvalidInput(key, value, false);
			this.testWithInvalidInput(key, value, true);
		}
	}

	private void testOperator(boolean negated)
	{
		final String expop = LabelSelectorEQ.operator(negated);
		log.info("Testing " + expop + " operator...");

		try {
			final String expkey = "release";
			final String expvalue = "stable";

			final LabelSelectorParser parser = new LabelSelectorParser();
			LabelSelectorEQ selector = LabelSelectorEQ.create(expkey, expvalue, negated);
			selector = (LabelSelectorEQ) parser.parse(selector.toString());

			final String curkey = selector.getKey();
			Assert.assertTrue("Invalid key! Expected: '" + expkey + "', Parsed: '" + curkey + "'", expkey.equals(curkey));

			final String curvalue = selector.getValue();
			Assert.assertTrue("Invalid value! Expected: '" + expvalue + "', Parsed: '" + curvalue + "'", expvalue.equals(curvalue));

			final String curop = selector.getOperator();
			Assert.assertTrue("Invalid operator! Expected: '" + expop + "', Parsed: '" + curop + "'", expop == curop);

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

	private void testWithInvalidInput(String key, String value, boolean negated)
	{
		final String expop = LabelSelectorEQ.operator(negated);
		log.info("Testing " + expop + " operator with invalid input: key=" + key + ", value=" + value);

		try {
			LabelSelectorEQ.create(key, value, negated);
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
