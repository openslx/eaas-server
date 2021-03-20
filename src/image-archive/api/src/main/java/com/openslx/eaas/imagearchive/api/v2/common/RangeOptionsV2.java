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

package com.openslx.eaas.imagearchive.api.v2.common;

import javax.ws.rs.QueryParam;


public class RangeOptionsV2<T extends RangeOptionsV2<T>>
		extends FilterOptionsV2<T>
{
	@QueryParam("offset")
	private int offset = 0;

	@QueryParam("limit")
	private int limit = 0;


	public T setOffset(int offset)
	{
		this.offset = offset;
		return (T) this;
	}

	public int offset()
	{
		return offset;
	}

	public T setLimit(int limit)
	{
		this.limit = limit;
		return (T) this;
	}

	public int limit()
	{
		return (limit == 0) ? Integer.MAX_VALUE : limit;
	}
}
