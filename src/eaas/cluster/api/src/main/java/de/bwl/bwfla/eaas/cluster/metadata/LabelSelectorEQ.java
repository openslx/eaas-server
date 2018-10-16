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


public class LabelSelectorEQ extends LabelSelector
{
	private static final String OPERATOR          = "==";
	private static final String OPERATOR_NEGATED  = "!=";

	private final String svalue;


	private LabelSelectorEQ(String key, String value, boolean negated, boolean checked)
	{
		super(key, negated, checked);

		if (!checked)
			LabelSelectorEQ.validate(value, "value");

		this.svalue = value;
	}

	public String getValue()
	{
		return svalue;
	}

	public String getOperator()
	{
		return LabelSelectorEQ.operator(negated);
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

		final boolean matched = value.equals(this.svalue);
		return (negated) ? !matched : matched;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder(256)
				.append(skey)
				.append(' ')
				.append(LabelSelectorEQ.operator(negated))
				.append(' ')
				.append(svalue);

		return sb.toString(); 
	}

	public static String operator(boolean negated)
	{
		return (negated) ? OPERATOR_NEGATED : OPERATOR;
	}

	public static LabelSelectorEQ create(String key, String value)
	{
		return LabelSelectorEQ.create(key, value, false);
	}

	public static LabelSelectorEQ create(String key, String value, boolean negated)
	{
		return new LabelSelectorEQ(key, value, negated, false);
	}

	public static LabelSelectorEQ createFromChecked(String key, String value, boolean negated)
	{
		return new LabelSelectorEQ(key, value, negated, true);
	}
}
