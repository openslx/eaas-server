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

import com.openslx.eaas.common.util.CurlTool;
import com.openslx.eaas.common.util.JaxRsUtils;
import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.common.IWritable;
import com.openslx.eaas.imagearchive.api.v2.common.InsertOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.ws.rs.core.MediaType;
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
		final var context = this.context();
		final var curl = new CurlTool(context.logger())
				.url(context.endpoint())
				.authorization(context.token())
				.accept(MediaType.APPLICATION_JSON);

		if (options != null) {
			curl.headers(options.toHeaderParams());
			curl.query(options.toQueryParams());
		}

		final CurlTool.ResponseBodyHandler<String> handler = (payload) -> {
			final var bytes = payload.readAllBytes();
			return new String(bytes);
		};

		try (curl) {
			return curl.post(value, handler);
		}
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
		final var context = this.context();
		final var endpoint = JaxRsUtils.join(context.endpoint(), id);
		final var curl = new CurlTool(context.logger())
				.url(endpoint)
				.authorization(context.token());

		if (options != null) {
			curl.headers(options.toHeaderParams());
			curl.query(options.toQueryParams());
		}

		try (curl) {
			curl.put(value);
		}
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
	ImageArchiveClient.Context context();
}
