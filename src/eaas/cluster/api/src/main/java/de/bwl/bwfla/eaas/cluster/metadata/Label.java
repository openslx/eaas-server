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


public class Label
{
	public static final String KV_REGEX = "\\w(?:\\w|-|_|\\.)*";

	/** Special value representing a missing label */
	public static final String UNDEFINED_VALUE = "*";
	
	private String key;
	private String value;
	
	public Label(String key, String value)
	{
		this.key = key;
		this.value = value;
	}
	
	public void setKey(String key)
	{
		if (!Label.validate(key))
			throw new IllegalArgumentException("Label's key is malformed: " + key);

		this.key = key;
	}
	
	public void setValue(String value)
	{
		if (!Label.validate(value))
			throw new IllegalArgumentException("Label's value is malformed: " + value);

		this.value = value;
	}
	
	public String getKey()
	{
		return key;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public static boolean validate(String str)
	{
		if (str == null || str.isEmpty())
			return false;
		
		return str.matches(KV_REGEX);
	}

	public static boolean isValueDefined(String value)
	{
		if (value == null)
			return false;

		return !value.contentEquals(UNDEFINED_VALUE);
	}
}
