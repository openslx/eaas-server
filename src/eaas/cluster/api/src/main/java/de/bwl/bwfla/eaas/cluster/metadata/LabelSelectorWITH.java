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


public class LabelSelectorWITH extends LabelSelector
{
	public static final String OPERATOR_NEGATED  = "!";
	
	private LabelSelectorWITH(String key, boolean negated, boolean checked)
	{
		super(key, negated, checked);
	}

	@Override
	public boolean match(String key, String value)
	{
		final boolean matched = skey.equals(key);
		return (negated) ? !matched : matched;
	}

	@Override
	public boolean match(String value)
	{
		final boolean defined = Label.isValueDefined(value);
		return (negated) ? !defined : defined;
	}

	@Override
	public String toString()
	{
		if (negated)
			return OPERATOR_NEGATED + skey;

		return skey;
	}

	public static LabelSelectorWITH create(String key)
	{
		return LabelSelectorWITH.create(key, false);
	}
	
	public static LabelSelectorWITH create(String key, boolean negated)
	{
		return new LabelSelectorWITH(key, negated, false);
	}
	
	public static LabelSelectorWITH createFromChecked(String key, boolean negated)
	{
		return new LabelSelectorWITH(key, negated, true);
	}
}
