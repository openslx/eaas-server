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

package de.bwl.bwfla.imageclassifier.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class HistogramEntry
{
    @XmlElement
	private String value;
    @XmlElement
	private String type;
    @XmlElement
	private int count;

	public HistogramEntry()
	{
		this(null, 0, null);
	}
	
	public HistogramEntry(String type, int count)
	{
		this(type, count, null);
	}
	
	public HistogramEntry(String type, int count, String value)
	{
		this.type = type;
		this.count = count;
		this.value = value;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public void setCount(int count)
	{
		this.count = count;
	}
	
	public void setCount(String count)
	{
		this.count = Integer.parseInt(count);
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
}