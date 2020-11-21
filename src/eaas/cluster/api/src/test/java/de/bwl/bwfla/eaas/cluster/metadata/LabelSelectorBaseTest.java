package de.bwl.bwfla.eaas.cluster.metadata;


import org.junit.Assert;

public class LabelSelectorBaseTest
{
	public static final String[] VALID_KEYS_VALUES = {
			"abc",
			"nums123",
			"with-dash",
			"with_underscore",
			"with_and-456",
			"with.point"
	};

	public static final String[] INVALID_KEYS_VALUES = {
			"abc!",
			"#",
			"def-gh+j",
			"$x",
			"w=",
			"z?u",
			"a,b",
			"()",
			"f{_}",
			"k[i]",
			"%i",
			"z&x",
			"cmd|s",
			"o o",
			"'x'"
	};


	public static void checkSelectorClass(LabelSelector selector, Class<? extends LabelSelector> clazz)
	{
		if (selector.getClass() != clazz)
			Assert.fail("Unexpected class returned: " + selector.getClass().getName() + ", Expected: " + clazz);
	}

	public static void checkKey(String expkey, String curkey)
	{
		Assert.assertTrue("Invalid key! Expected: '" + expkey + "', Parsed: '" + curkey + "'", expkey.equals(curkey));
	}

	public static void checkValue(String expvalue, String curvalue)
	{
		Assert.assertTrue("Invalid value! Expected: '" + expvalue + "', Parsed: '" + curvalue + "'", expvalue.equals(curvalue));
	}

	public static void checkOperator(String expop, String curop)
	{
		Assert.assertTrue("Invalid operator! Expected: '" + expop + "', Parsed: '" + curop + "'", expop.equals(curop));
	}

	public static void checkLabelMatch(LabelSelector selector, String expkey, String expvalue, boolean negated)
	{
		final Label label = new Label(expkey, expvalue);
		if (negated)
			Assert.assertTrue("Matching label failed!", !selector.match(label));
		else Assert.assertTrue("Matching label failed!", selector.match(label));
	}
}
