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

package de.bwl.bwfla.metadata.repository.client;

import de.bwl.bwfla.metadata.repository.api.HttpDefs;
import de.bwl.bwfla.metadata.repository.api.ItemDescription;
import de.bwl.bwfla.metadata.repository.api.ItemDescriptionStream;
import de.bwl.bwfla.metadata.repository.api.ItemIdentifierDescriptionStream;
import de.bwl.bwfla.metadata.repository.api.SetDescriptionStream;
import de.bwl.bwfla.metadata.repository.source.QueryOptions;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;


/** A HTTP-Client for metadata-repositories */
public class MetaDataRepository
{
	private static final Charset ENCODING = StandardCharsets.UTF_8;

	private final WebTarget endpoint;


	public MetaDataRepository(WebTarget endpoint)
	{
		this.endpoint = endpoint;
	}

	public Sets sets()
	{
		return new Sets(endpoint);
	}

	public ItemIdentifiers identifiers()
	{
		return new ItemIdentifiers(endpoint);
	}

	public Items items()
	{
		return new Items(endpoint);
	}


	// ========== Public Resources =========================

	public interface IRequest<T>
	{
		T execute();
	}


	public static class Sets
	{
		private final WebTarget endpoint;

		private Sets(WebTarget endpoint)
		{
			this.endpoint = endpoint.path(HttpDefs.Paths.SETS);
		}

		public Sets.List list(int offset, int count)
		{
			return new List(offset, count);
		}

		public Sets.Head head(String setspec)
		{
			return new Head(setspec);
		}

		public Sets.Head head()
		{
			return new Head(null);
		}


		public class Head implements IRequest<Response>
		{
			private final String setspec;

			private Head(String setspec)
			{
				this.setspec = setspec;
			}

			@Override
			public Response execute()
			{
				final WebTarget target = (setspec != null) ? endpoint.path(setspec) : endpoint;
				return target.request()
						.head();
			}
		}


		public class List implements IRequest<SetDescriptionStream>
		{
			private final int offset;
			private final int count;

			private List(int offset, int count)
			{
				this.offset = offset;
				this.count = count;
			}

			@Override
			public SetDescriptionStream execute()
			{
				final Response response = endpoint.queryParam(HttpDefs.QueryParams.OFFSET, offset)
						.queryParam(HttpDefs.QueryParams.COUNT, count)
						.request(HttpDefs.MediaTypes.SETS)
						.acceptEncoding(ENCODING.displayName())
						.get();

				return SetDescriptionStream.create(response);
			}
		}
	}


	public static class ItemIdentifiers
	{
		private final WebTarget endpoint;

		private ItemIdentifiers(WebTarget endpoint)
		{
			this.endpoint = endpoint.path(HttpDefs.Paths.IDENTIFIERS);
		}

		public ItemIdentifiers.List list(QueryOptions options)
		{
			return new List(options);
		}


		public class List implements IRequest<ItemIdentifierDescriptionStream>
		{
			private final QueryOptions options;

			private List(QueryOptions options)
			{
				this.options = options;
			}

			@Override
			public ItemIdentifierDescriptionStream execute()
			{
				final WebTarget target = MetaDataRepository.addQueryParams(endpoint, options);
				final Response response = target.request(HttpDefs.MediaTypes.IDENTIFIERS)
						.acceptEncoding(ENCODING.displayName())
						.get();

				return ItemIdentifierDescriptionStream.create(response);
			}
		}
	}


	public static class Items
	{
		private final WebTarget endpoint;

		private Items(WebTarget endpoint)
		{
			this.endpoint = endpoint.path(HttpDefs.Paths.ITEMS);
		}

		public Items.Get get(String id)
		{
			return new Get(id);
		}

		public Items.List list(QueryOptions options)
		{
			return new List(options);
		}

		public Items.Insert insert(Stream<ItemDescription> items)
		{
			return new Insert(items);
		}


		public class Get implements IRequest<Response>
		{
			private final String id;

			private Get(String id)
			{
				this.id = id;
			}

			@Override
			public Response execute()
			{
				return endpoint.path(id)
						.request()
						.get();
			}
		}

		public class List implements IRequest<ItemDescriptionStream>
		{
			private final QueryOptions options;

			private List(QueryOptions options)
			{
				this.options = options;
			}

			@Override
			public ItemDescriptionStream execute()
			{
				final WebTarget target = MetaDataRepository.addQueryParams(endpoint, options);
				final Response response = target.request(HttpDefs.MediaTypes.ITEMS)
						.acceptEncoding(ENCODING.displayName())
						.get();

				return ItemDescriptionStream.create(response);
			}
		}

		public class Insert implements IRequest<Response>
		{
			private final Stream<ItemDescription> items;

			private Insert(Stream<ItemDescription> items)
			{
				this.items = items;
			}

			@Override
			public Response execute()
			{
				final StreamingOutput streamer = (ostream) -> {
					try (final ItemDescriptionStream.Writer writer = new ItemDescriptionStream.Writer(ostream)) {
						writer.write(items);
					}
				};

				return endpoint.request()
						.post(Entity.entity(streamer, HttpDefs.MediaTypes.ITEMS));
			}
		}
	}


	// ========== Internal Helpers =========================

	private static WebTarget addQueryParam(WebTarget target, String name, Object value, boolean nondefault)
	{
		return (nondefault) ? target.queryParam(name, value) : target;
	}

	private static WebTarget addQueryParams(WebTarget target, QueryOptions options)
	{
		target = MetaDataRepository.addQueryParam(target, HttpDefs.QueryParams.OFFSET, options.offset(), options.hasOffset());
		target = MetaDataRepository.addQueryParam(target, HttpDefs.QueryParams.COUNT, options.count(), options.hasCount());
		target = MetaDataRepository.addQueryParam(target, HttpDefs.QueryParams.FROM, options.from(), options.hasFrom());
		target = MetaDataRepository.addQueryParam(target, HttpDefs.QueryParams.UNTIL, options.until(), options.hasUntil());
		target = MetaDataRepository.addQueryParam(target, HttpDefs.QueryParams.SETSPEC, options.setspec(), options.hasSetSpec());

		return target;
	}
}
