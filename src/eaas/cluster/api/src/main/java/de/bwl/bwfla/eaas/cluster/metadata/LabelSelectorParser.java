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

package de.bwl.bwfla.eaas.cluster.metadata;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.bwl.bwfla.eaas.cluster.exception.MalformedLabelSelectorException;


/** This implementation is not thread-safe! */
public class LabelSelectorParser
{
	private static final Pattern PATTERN_KEY_VALUE = Pattern.compile(Label.KV_REGEX);
	
	private static final Pattern PATTERN_WHITESPACE = Pattern.compile(" *");

	private static final Pattern PATTERN_OPERATOR;
	
	private static final Map<String, SelectorCtor> CONSTRUCTORS;
	static {
		CONSTRUCTORS = new HashMap<String, SelectorCtor>();
		CONSTRUCTORS.put(LabelSelectorEQ.operator(false), new SelectorEqCtor(false));
		CONSTRUCTORS.put(LabelSelectorEQ.operator(true) , new SelectorEqCtor(true));
		CONSTRUCTORS.put(LabelSelectorIN.operator(false), new SelectorInCtor(false));
		CONSTRUCTORS.put(LabelSelectorIN.operator(true) , new SelectorInCtor(true));
		
		// Construct the operator pattern...
		final StringBuilder sb = new StringBuilder(512);
		for (String key : CONSTRUCTORS.keySet()) {
			sb.append('(')
			  .append(key)
			  .append(")|");
		}
		
		PATTERN_OPERATOR = Pattern.compile(sb.substring(0, sb.length() - 1));
	}
	
	private final Matcher matcher;
	private String input;
	
	
	public LabelSelectorParser()
	{
		this.matcher = PATTERN_WHITESPACE.matcher(" ");
		this.input = "";
	}
	
	public LabelSelector parse(String selector) throws MalformedLabelSelectorException
	{
		this.prepare(selector);
		
		final boolean isNegatedWith = selector.startsWith(LabelSelectorWITH.OPERATOR_NEGATED);
		if (isNegatedWith) {
			// Adjust the matcher region to skip the negated prefix...
			matcher.region(LabelSelectorWITH.OPERATOR_NEGATED.length(), selector.length());
		}
		
		// Parse key...
		final String key = this.parse(PATTERN_KEY_VALUE);
		this.skip(PATTERN_WHITESPACE);
		
		// Nothing more to parse?
		if (this.isDone()) {
			// It should be the WITH selector!
			return LabelSelectorWITH.createFromChecked(key, isNegatedWith);
		}
		
		// More data left, try to parse an operator...
		final String operator = this.parse(PATTERN_OPERATOR);
		final SelectorCtor ctor = CONSTRUCTORS.get(operator);
		if (ctor == null)
			this.fail("Unknown operator '" + operator + "' parsed!");

		this.skip(PATTERN_WHITESPACE);
		return ctor.create(key, operator, this);
	}

	
	/* =============== Internal Helpers =============== */
	
	private void prepare(String selector)
	{
		matcher.reset(selector);
		input = selector;
	}
	
	private char peek()
	{
		final int offset = this.position();
		return input.charAt(offset);
	}
	
	private int position()
	{
		return matcher.regionStart();
	}
	
	private String peek(int count)
	{
		if (count < 1)
			throw new IllegalArgumentException();
		
		final int offset = this.position();
		return input.substring(offset, offset + count);
	}
	
	private boolean skip(Pattern pattern)
	{
		matcher.usePattern(pattern);
		final boolean matched = matcher.lookingAt();
		if (matched)
			this.advance(matcher.end());
		
		return matched;
	}
	
	private int skip(char c)
	{
		final int start = this.position();
		int index = start;
		while (index < input.length()) {
			if (input.charAt(index) != c)
				break;
			
			++index;
		}
		
		final int skipped = index - start;
		if (skipped > 0)
			this.advance(index);
		
		return skipped;
	}
	
	private void skip(char c, int count) throws MalformedLabelSelectorException
	{
		final int skipped = this.skip(c);
		if (skipped != count)
			this.fail(this.position() + skipped, c);
	}
	
	private String parse(Pattern pattern) throws MalformedLabelSelectorException
	{
		return this.parse(pattern, true);
	}
	
	private String parse(Pattern pattern, boolean expected) throws MalformedLabelSelectorException
	{
		matcher.usePattern(pattern);
		if (matcher.lookingAt()) {
			String match = matcher.group();
			this.advance(matcher.end());
			return match;
		}
		
		if (expected)
			this.fail(this.position());

		return null;
	}
	
	private void advance(int start)
	{
		final int end = matcher.regionEnd();
		matcher.region(start, end);
	}
	
	private boolean isDone()
	{
		return matcher.hitEnd();
	}
	
	private void fail(int pos) throws MalformedLabelSelectorException
	{
		throw new MalformedLabelSelectorException(input, pos);
	}
	
	private void fail(int pos, char expected) throws MalformedLabelSelectorException
	{
		throw new MalformedLabelSelectorException(input, pos, expected);
	}
	
	private void fail(String message) throws MalformedLabelSelectorException
	{
		throw new MalformedLabelSelectorException(input, message);
	}
	
	private void fail() throws MalformedLabelSelectorException
	{
		throw new MalformedLabelSelectorException(input);
	}
	
	
	private static abstract class SelectorCtor
	{
		protected final boolean negated;
		
		protected SelectorCtor(boolean negated)
		{
			this.negated = negated;
		}
		
		public abstract LabelSelector create(String key, String operator, LabelSelectorParser parser)
				throws MalformedLabelSelectorException;
	}
	
	private static class SelectorEqCtor extends SelectorCtor
	{
		public SelectorEqCtor(boolean negated)
		{
			super(negated);
		}
		
		@Override
		public LabelSelector create(String key, String operator, LabelSelectorParser parser)
				throws MalformedLabelSelectorException
		{
			// Try to parse a single value...
			final String value = parser.parse(PATTERN_KEY_VALUE);
			parser.skip(PATTERN_WHITESPACE);
			if (!parser.isDone())
				parser.fail("Unparsed or invalid data left!");

			return LabelSelectorEQ.createFromChecked(key, value, negated);
		}
	}
	
	private static class SelectorInCtor extends SelectorCtor
	{
		public SelectorInCtor(boolean negated)
		{
			super(negated);
		}
		
		@Override
		public LabelSelector create(String key, String operator, LabelSelectorParser parser)
				throws MalformedLabelSelectorException
		{
			final LinkedHashSet<String> values = new LinkedHashSet<String>();
			
			// Try to parse all values...
			parser.skip(LabelSelectorIN.TOKEN_VALUES_BEGIN, 1);
			parser.skip(PATTERN_WHITESPACE);
			while (true) {
				final String value = parser.parse(PATTERN_KEY_VALUE);
				values.add(value);
				
				// Do we have more values?
				parser.skip(PATTERN_WHITESPACE);
				if (parser.peek() == LabelSelectorIN.TOKEN_VALUES_END)
					break;  // Doesn't seem so!
				
				// Skip values separator and try to parse next value
				parser.skip(LabelSelectorIN.TOKEN_VALUES_DELIMITER, 1);
				parser.skip(PATTERN_WHITESPACE);
			}
			
			parser.skip(LabelSelectorIN.TOKEN_VALUES_END, 1);
			parser.skip(PATTERN_WHITESPACE);
			if (!parser.isDone())
				parser.fail("Unparsed or invalid data left!");
			
			if (values.isEmpty())
				parser.fail("No values parsed!");

			return LabelSelectorIN.createFromChecked(key, values, negated);
		}
	}
}
