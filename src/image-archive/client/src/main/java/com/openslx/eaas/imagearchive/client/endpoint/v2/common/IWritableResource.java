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

import com.openslx.eaas.imagearchive.api.v2.common.IWritable;
import com.openslx.eaas.imagearchive.api.v2.common.InsertOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.function.Function;


public interface IWritableResource<T>
{
	// ===== IWritable API ==============================

	default String insert(T value) throws BWFLAException
	{
		return this.insert(value, (InsertOptionsV2) null);
	}

	default String insert(T value, InsertOptionsV2 options) throws BWFLAException
	{
		return this.api()
				.insert(value, options);
	}

	default <U> String insert(U value, Function<U,T> mapper) throws BWFLAException
	{
		return this.insert(mapper.apply(value));
	}

	default <U> String insert(U value, Function<U,T> mapper, InsertOptionsV2 options) throws BWFLAException
	{
		return this.insert(mapper.apply(value), options);
	}

	default void replace(String id, T value) throws BWFLAException
	{
		this.replace(id, value, (ReplaceOptionsV2) null);
	}

	default void replace(String id, T value, ReplaceOptionsV2 options) throws BWFLAException
	{
		this.api()
				.replace(id, value, options);
	}

	default <U> void replace(String id, U value, Function<U,T> mapper) throws BWFLAException
	{
		this.replace(id, value, mapper, null);
	}

	default <U> void replace(String id, U value, Function<U,T> mapper, ReplaceOptionsV2 options) throws BWFLAException
	{
		this.replace(id, mapper.apply(value), options);
	}


	// ===== Internal Helpers ==============================

	IWritable<T> api();
}
