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

package com.openslx.eaas.imagearchive.client.endpoint.v2.common;

import com.openslx.eaas.common.databind.Streamable;
import com.openslx.eaas.imagearchive.api.v2.common.CountOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.IListable;
import com.openslx.eaas.imagearchive.api.v2.common.IReadable;
import com.openslx.eaas.imagearchive.api.v2.common.ListOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ResolveOptionsV2;
import de.bwl.bwfla.common.exceptions.BWFLAException;


public abstract class AbstractResourceRO<T>
{
	// ===== IListable API ==============================

	public long count() throws BWFLAException
	{
		return this.count(null);
	}

	public long count(CountOptionsV2 options) throws BWFLAException
	{
		return this.listable()
				.count(options);
	}

	public boolean exists(String id)
	{
		try {
			this.listable()
					.exists(id);

			return true;
		}
		catch (Exception error) {
			return false;
		}
	}

	public Streamable<String> list() throws BWFLAException
	{
		return this.list(null);
	}

	public Streamable<String> list(ListOptionsV2 options) throws BWFLAException
	{
		final var response = this.listable()
				.list(options);

		return Streamable.of(response, String.class);
	}


	// ===== IReadable API ==============================

	public String resolve(String id) throws BWFLAException
	{
		return this.resolve(id, null);
	}

	public String resolve(String id, ResolveOptionsV2 options) throws BWFLAException
	{
		return this.readable()
				.resolve(id, options);
	}

	public T fetch(String id) throws BWFLAException
	{
		return this.readable()
				.fetch(id);
	}


	// ===== Internal Helpers ==============================

	protected abstract IListable listable();
	protected abstract IReadable<T> readable();
}
