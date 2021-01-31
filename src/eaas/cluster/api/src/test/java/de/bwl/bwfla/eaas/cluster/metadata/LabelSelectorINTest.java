package de.bwl.bwfla.eaas.cluster.metadata;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;


public class LabelSelectorINTest
{
	protected final Logger log = Logger.getLogger(this.getClass().getName());

	@Test
	public void testInOperator()
	{
		this.testOperator(false);
	}

	@Test
	public void testNotInOperator()
	{
		this.testOperator(true);
	}

	@Test
	public void testInvalidKeys()
	{
		final Set<String> values = new HashSet<String>();
		values.add("valid-value");

		for (String key : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
			this.testWithInvalidInput(key, values, false);
			this.testWithInvalidInput(key, values, true);
		}
	}

	@Test
	public void testInvalidValues()
	{
		final String key = "valid-key";
		final Set<String> values = new HashSet<String>();
		for (String value : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
			values.clear();
			values.add(value);

			this.testWithInvalidInput(key, values, false);
			this.testWithInvalidInput(key, values, true);
		}
	}

	public void testOperator(boolean negated)
	{
		final String expop = LabelSelectorIN.operator(negated);
		log.info("Testing " + expop + " operator...");

		try {
			final String expkey = "release";
			final Set<String> expvalues = new LinkedHashSet<String>();
			expvalues.add("stable");
			expvalues.add("devel");
			expvalues.add("canary");

			final LabelSelectorParser parser = new LabelSelectorParser();
			LabelSelectorIN selector = LabelSelectorIN.create(expkey, expvalues, negated);
			selector = (LabelSelectorIN) parser.parse(selector.toString());

			final String curkey = selector.getKey();
			Assert.assertTrue("Invalid key! Expected: '" + expkey + "', Parsed: '" + curkey + "'", expkey.equals(curkey));

			final Set<String> curvalues = selector.getValues();
			for (String expvalue : expvalues)
				Assert.assertTrue("Missing value! Expected: '" + expvalue, curvalues.contains(expvalue));

			final String curop = selector.getOperator();
			Assert.assertTrue("Invalid operator! Expected: '" + expop + "', Parsed: '" + curop + "'", expop == curop);

			final Label label = new Label(expkey, expvalues.iterator().next());
			if (negated)
				Assert.assertTrue("Matching label failed!", !selector.match(label));
			else Assert.assertTrue("Matching label failed!", selector.match(label));
		}
		catch (Exception exception) {
			exception.printStackTrace();
			Assert.fail();
		}
	}

	private void testWithInvalidInput(String key, Set<String> values, boolean negated)
	{
		final String expop = LabelSelectorEQ.operator(negated);
		log.info("Testing " + expop + " operator with invalid input: key=" + key + ", value=" + values.toString());

		try {
			LabelSelectorIN.create(key, values, negated);
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
