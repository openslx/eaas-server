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

package de.bwl.bwfla.metadata.repository.source;

import java.util.Date;


public class QueryOptions
{
	private int offset;
	private int count;
	private long from;
	private long until;
	private String setspec;

	public QueryOptions()
	{
		this.offset = Defaults.OFFSET;
		this.count = Defaults.COUNT;
		this.from = Defaults.FROM;
		this.until = Defaults.UNTIL;
		this.setspec = null;
	}

	public static class Defaults
	{
		public static final int  OFFSET = 0;
		public static final int  COUNT  = Integer.MAX_VALUE;
		public static final long FROM   = 0L;
		public static final long UNTIL  = Long.MAX_VALUE;
	}


	// ===== Getters ===============

	public int offset()
	{
		return offset;
	}

	public int count()
	{
		return count;
	}

	public long from()
	{
		return from;
	}

	public long until()
	{
		return until;
	}

	public String setspec()
	{
		return setspec;
	}


	// ===== Setters ===============

	public QueryOptions withOffset(int offset)
	{
		this.offset = offset;
		return this;
	}

	public QueryOptions withCount(int count)
	{
		this.count = count;
		return this;
	}

	public QueryOptions withFrom(long timestamp)
	{
		this.from = timestamp;
		return this;
	}

	public QueryOptions withFrom(Date date)
	{
		this.from = date.getTime();
		return this;
	}

	public QueryOptions withUntil(long timestamp)
	{
		this.until = timestamp;
		return this;
	}

	public QueryOptions withUntil(Date date)
	{
		this.until = date.getTime();
		return this;
	}

	public QueryOptions withSetSpec(String setspec)
	{
		this.setspec = setspec;
		return this;
	}


	// ===== Checkers ===============

	public boolean hasOffset()
	{
		return offset != Defaults.OFFSET;
	}

	public boolean hasCount()
	{
		return count != Defaults.COUNT;
	}

	public boolean hasFrom()
	{
		return from != Defaults.FROM;
	}

	public boolean hasUntil()
	{
		return until != Defaults.UNTIL;
	}

	public boolean hasSetSpec()
	{
		return setspec != null;
	}
}
