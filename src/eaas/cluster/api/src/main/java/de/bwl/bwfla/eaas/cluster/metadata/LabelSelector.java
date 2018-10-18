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


public abstract class LabelSelector
{
	protected final String skey;
	protected final boolean negated;
	
	protected LabelSelector(String key, boolean negated, boolean checked)
	{
		if (!checked)
			LabelSelector.validate(key, "key");
		
		this.skey = key;
		this.negated = negated;
	}
	
	public String getKey()
	{
		return skey;
	}
	
	public boolean isNegated()
	{
		return negated;
	}
	
	public boolean match(Label label)
	{
		return this.match(label.getKey(), label.getValue());
	}
	
	public abstract boolean match(String key, String value);
	public abstract boolean match(String value);
	
	protected static void validate(String arg, String argname)
	{
		if (!Label.validate(arg))
			throw new IllegalArgumentException("Label selector's " + argname + " is malformed: " + arg);
	}
}
