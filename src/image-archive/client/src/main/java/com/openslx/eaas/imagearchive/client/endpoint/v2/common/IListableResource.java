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
import com.openslx.eaas.imagearchive.api.v2.common.ListOptionsV2;
import de.bwl.bwfla.common.exceptions.BWFLAException;


public interface IListableResource
{
	// ===== IListable API ==============================

	default long count() throws BWFLAException
	{
		return this.count(null);
	}

	default long count(CountOptionsV2 options) throws BWFLAException
	{
		return this.api()
				.count(options);
	}

	default boolean exists(String id)
	{
		try {
			this.api()
					.exists(id);

			return true;
		}
		catch (Exception error) {
			return false;
		}
	}

	default Streamable<String> list() throws BWFLAException
	{
		return this.list(null);
	}

	default Streamable<String> list(ListOptionsV2 options) throws BWFLAException
	{
		final var response = this.api()
				.list(options);

		return Streamable.of(response, String.class);
	}


	// ===== Internal Helpers ==============================

	IListable api();
}
