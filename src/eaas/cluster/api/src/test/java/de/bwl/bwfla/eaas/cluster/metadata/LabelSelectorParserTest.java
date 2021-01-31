package de.bwl.bwfla.eaas.cluster.metadata;

import de.bwl.bwfla.eaas.cluster.exception.MalformedLabelSelectorException;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.logging.Logger;


public class LabelSelectorParserTest
{
	private final Logger log = Logger.getLogger(this.getClass().getName());

	@Test
	public void testSelectorEQ()
	{
		class SelEq
		{
			final String selector;
			final String key;
			final String value;
			final boolean negated;

			SelEq(String k, String v, boolean negated)
			{
				this(LabelSelectorEQ.create(k, v, negated).toString(), k, v, negated);
			}

			SelEq(String selector, String k, String v, boolean negated)
			{
				this.selector = selector;
				this.key = k;
				this.value = v;
				this.negated = negated;
			}
		}

		final SelEq[] selectors = {
				new SelEq("release", "stable", false),
				new SelEq("track", "daily", true),
				new SelEq("environment", "dev", true),
				new SelEq("tier", "backend", false),
				new SelEq("release==stable", "release", "stable", false),
				new SelEq("tier   !=backend", "tier", "backend", true),
				new SelEq("track!=    daily", "track", "daily", true),
		};

		for (SelEq sel : selectors)
			this.testSelectorEQ(sel.selector, sel.key, sel.value, sel.negated);
	}

	@Test
	public void testInvalidSelectorEQ()
	{
		final String[] ops = {
				"= =",
				"! =",
				"!!=",
				"!",
				"=",
				"== =",
				"=!="
		};

		for (String key : LabelSelectorBaseTest.VALID_KEYS_VALUES) {
			for (String value : LabelSelectorBaseTest.VALID_KEYS_VALUES) {
				for (String op : ops) {
					this.testWithInvalidInput(key + op + value);
					this.testWithInvalidInput(key + " " + op + " " + value);
				}
			}
		}

		for (String key : LabelSelectorBaseTest.VALID_KEYS_VALUES) {
			for (String value : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
				this.testWithInvalidInput(key + " == " + value);
				this.testWithInvalidInput(key + " != " + value);
			}
		}

		for (String key : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
			for (String value : LabelSelectorBaseTest.VALID_KEYS_VALUES) {
				this.testWithInvalidInput(key + " == " + value);
				this.testWithInvalidInput(key + " != " + value);
			}
		}
	}

	@Test
	public void testSelectorIN()
	{
		class SelIn
		{
			final String selector;
			final String key;
			final Set<String> values;
			final boolean negated;

			SelIn(String k, String[] vs, boolean negated)
			{
				this.values = new TreeSet<String>();
				for (String value : vs)
					values.add(value);

				this.selector = LabelSelectorIN.create(k, values, negated).toString();
				this.key = k;
				this.negated = negated;
			}

			SelIn(String selector, String k, String[] vs, boolean negated)
			{
				this.values = new TreeSet<String>();
				for (String value : vs)
					values.add(value);

				this.selector = selector;
				this.key = k;
				this.negated = negated;
			}
		}

		final SelIn[] selectors = {
				new SelIn("release", new String[]{"stable"}, false),
				new SelIn("track", new String[]{"daily", "weekly"}, true),
				new SelIn("environment", new String[]{"dev", "qa", "production"}, false),
				new SelIn("tier", new String[]{"backend"}, true),
				new SelIn("release    in( stable  )", "release", new String[]{"stable"}, false),
				new SelIn("track !in  (daily,weekly  )", "track", new String[]{"daily", "weekly"}, true),
				new SelIn("tier !in(backend)", "tier", new String[]{"backend"}, true),
		};

		for (SelIn sel : selectors)
			this.testSelectorIN(sel.selector, sel.key, sel.values, sel.negated);
	}

	@Test
	public void testInvalidSelectorIN()
	{
		final String[] ops = {
				"i n",
				"! in",
				"!!in",
				"!",
				"i-n"
		};

		for (String key : LabelSelectorBaseTest.VALID_KEYS_VALUES) {
			for (String value : LabelSelectorBaseTest.VALID_KEYS_VALUES) {
				for (String op : ops)
					this.testWithInvalidInput(key + " " + op + " " + value);
			}
		}

		for (String key : LabelSelectorBaseTest.VALID_KEYS_VALUES) {
			for (String value : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
				this.testWithInvalidInput(key + " in " + value);
				this.testWithInvalidInput(key + " !in " + value);
			}
		}

		for (String key : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
			for (String value : LabelSelectorBaseTest.VALID_KEYS_VALUES) {
				this.testWithInvalidInput(key + " in " + value);
				this.testWithInvalidInput(key + " !in " + value);
			}
		}
	}

	@Test
	public void testSelectorWITH()
	{
		class SelWith
		{
			final String selector;
			final String key;
			final boolean negated;

			SelWith(String k, boolean negated)
			{
				this.selector = LabelSelectorWITH.create(k, negated).toString();
				this.key = k;
				this.negated = negated;
			}
		}

		final SelWith[] selectors = {
				new SelWith("release", false),
				new SelWith("track", true),
				new SelWith("environment", true),
				new SelWith("tier", false)
		};

		for (SelWith sel : selectors)
			this.testSelectorWITH(sel.selector, sel.key, sel.negated);
	}

	@Test
	public void testInvalidSelectorWITH()
	{
		for (String key : LabelSelectorBaseTest.INVALID_KEYS_VALUES) {
			this.testWithInvalidInput("!" + key);
			this.testWithInvalidInput(key);
		}
	}

	private void testSelectorEQ(String selstr, String expkey, String expvalue, boolean negated)
	{
		log.info("Parsing '" + selstr + "'...");

		try {
			final LabelSelectorParser parser = new LabelSelectorParser();
			final LabelSelector result = parser.parse(selstr);
			LabelSelectorBaseTest.checkSelectorClass(result, LabelSelectorEQ.class);

			final LabelSelectorEQ selector = (LabelSelectorEQ) result;
			LabelSelectorBaseTest.checkKey(expkey, selector.getKey());
			LabelSelectorBaseTest.checkValue(expvalue, selector.getValue());
			LabelSelectorBaseTest.checkOperator(LabelSelectorEQ.operator(negated), selector.getOperator());
			LabelSelectorBaseTest.checkLabelMatch(selector, expkey, expvalue, negated);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			Assert.fail();
		}
	}

	private void testSelectorIN(String selstr, String expkey, Set<String> expvalues, boolean negated)
	{
		log.info("Parsing '" + selstr + "'...");

		try {
			final LabelSelectorParser parser = new LabelSelectorParser();
			final LabelSelector result = parser.parse(selstr);
			LabelSelectorBaseTest.checkSelectorClass(result, LabelSelectorIN.class);

			final LabelSelectorIN selector = (LabelSelectorIN) result;
			LabelSelectorBaseTest.checkKey(expkey, selector.getKey());
			if (Collections.disjoint(expvalues, selector.getValues()))
				Assert.fail("Invalid values! Expected: " + expvalues.toString() + ", Parsed: " + selector.getValues().toString());

			LabelSelectorBaseTest.checkOperator(LabelSelectorIN.operator(negated), selector.getOperator());
			LabelSelectorBaseTest.checkLabelMatch(selector, expkey, expvalues.iterator().next(), negated);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			Assert.fail();
		}
	}

	private void testSelectorWITH(String selstr, String expkey, boolean negated)
	{
		log.info("Parsing '" + selstr + "'...");

		try {
			final LabelSelectorParser parser = new LabelSelectorParser();
			final LabelSelector result = parser.parse(selstr);
			LabelSelectorBaseTest.checkSelectorClass(result, LabelSelectorWITH.class);

			final LabelSelectorWITH selector = (LabelSelectorWITH) result;
			LabelSelectorBaseTest.checkKey(expkey, selector.getKey());
			LabelSelectorBaseTest.checkLabelMatch(selector, expkey, "anything", negated);
			if (selector.isNegated() != negated)
				Assert.fail();
		}
		catch (Exception exception) {
			exception.printStackTrace();
			Assert.fail();
		}
	}

	private void testWithInvalidInput(String selstr)
	{
		log.info("Parsing invalid '" + selstr + "'...");

		try {
			final LabelSelectorParser parser = new LabelSelectorParser();
			parser.parse(selstr);
		}
		catch (MalformedLabelSelectorException exception) {
			log.info("Failed with expected error: " + exception.getMessage());
			return;  // Expected outcome!
		}
		catch (Exception exception) {
			String message = "MalformedLabelSelectorException was expected, but caught:  "
					+ exception.getMessage();

			exception.printStackTrace();
			Assert.fail(message);
		}

		Assert.fail("Illegal input was undetected!");
	}
}
