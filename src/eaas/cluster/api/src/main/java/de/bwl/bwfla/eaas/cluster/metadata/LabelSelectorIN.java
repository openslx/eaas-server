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

import java.util.Set;


public class LabelSelectorIN extends LabelSelector
{
	private static final String OPERATOR  = "in";
	private static final String OPERATOR_NEGATED  = "!" + OPERATOR;
	
	static final char TOKEN_VALUES_BEGIN      = '(';
	static final char TOKEN_VALUES_END        = ')';
	static final char TOKEN_VALUES_DELIMITER  = ',';

	private final Set<String> svalues;


	private LabelSelectorIN(String key, Set<String> values, boolean negated, boolean checked)
	{
		super(key, negated, checked);

		if (values == null || values.isEmpty())
			throw new IllegalArgumentException("Label selector's values are missing!");

		if (!checked) {
			for (String value : values)
				LabelSelectorIN.validate(value, "value");
		}

		this.svalues = values;
	}

	public Set<String> getValues()
	{
		return svalues;
	}

	public String getOperator()
	{
		return LabelSelectorIN.operator(negated);
	}

	@Override
	public boolean match(String key, String value)
	{
		if (!skey.equals(key))
			return false;

		return this.match(value);
	}

	@Override
	public boolean match(String value)
	{
		// If label's value is missing/undefined,
		// assume a match for a negated selector!
		if (negated && !Label.isValueDefined(value))
			return true;

		final boolean matched = svalues.contains(value);
		return (negated) ? !matched : matched;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder(256)
				.append(skey)
				.append(' ')
				.append(LabelSelectorIN.operator(negated))
				.append(' ')
				.append(TOKEN_VALUES_BEGIN);

		for (String svalue : svalues) {
			sb.append(svalue);
			sb.append(TOKEN_VALUES_DELIMITER);
			sb.append(' ');
		}

		if (!svalues.isEmpty()) {
			// Remove last separator
			sb.setLength(sb.length() - 2);
		}

		sb.append(TOKEN_VALUES_END);
		return sb.toString();
	}

	public static String operator(boolean negated)
	{
		return (negated) ? OPERATOR_NEGATED : OPERATOR;
	}

	public static LabelSelectorIN create(String key, Set<String> values)
	{
		return LabelSelectorIN.create(key, values, false);
	}

	public static LabelSelectorIN create(String key, Set<String> values, boolean negated)
	{
		return new LabelSelectorIN(key, values, negated, false);
	}

	public static LabelSelectorIN createFromChecked(String key, Set<String> values, boolean negated)
	{
		return new LabelSelectorIN(key, values, negated, true);
	}
}
