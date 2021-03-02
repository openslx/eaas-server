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

package com.openslx.eaas.imagearchive.indexing;


public class FilterOptions
{
	private String location;
	private long fromts = -1L;
	private long untilts = -1L;


	public FilterOptions setLocation(String location)
	{
		this.location = location;
		return this;
	}

	public String location()
	{
		return location;
	}

	public FilterOptions setFromTime(long timestamp)
	{
		this.fromts = timestamp;
		return this;
	}

	public long from()
	{
		return fromts;
	}

	public FilterOptions setUntilTime(long timestamp)
	{
		this.untilts = timestamp;
		return this;
	}

	public long until()
	{
		return untilts;
	}
}
