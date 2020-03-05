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

package de.bwl.bwfla.metadata.repository;

import de.bwl.bwfla.common.services.security.SecuredInternal;
import de.bwl.bwfla.metadata.repository.api.HttpDefs;
import de.bwl.bwfla.metadata.repository.api.ItemDescription;
import de.bwl.bwfla.metadata.repository.api.ItemDescriptionStream;
import de.bwl.bwfla.metadata.repository.api.ItemIdentifierDescription;
import de.bwl.bwfla.metadata.repository.api.ItemIdentifierDescriptionStream;
import de.bwl.bwfla.metadata.repository.api.SetDescription;
import de.bwl.bwfla.metadata.repository.api.SetDescriptionStream;
import de.bwl.bwfla.metadata.repository.sink.ItemSink;
import de.bwl.bwfla.metadata.repository.sink.MetaDataSink;
import de.bwl.bwfla.metadata.repository.source.ItemIdentifierSource;
import de.bwl.bwfla.metadata.repository.source.ItemSource;
import de.bwl.bwfla.metadata.repository.source.QueryOptions;
import de.bwl.bwfla.metadata.repository.source.MetaDataSource;
import de.bwl.bwfla.metadata.repository.source.SetSource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;


@ApplicationScoped
public class MetaDataRepositoryAPI implements IMetaDataRepositoryAPI
{
	private final Logger log = Logger.getLogger(this.getClass().getName());

	@Inject
	private MetaDataSourceRegistry sources = null;

	@Inject
	private MetaDataSinkRegistry sinks = null;


	public MetaDataSourceRegistry sources()
	{
		return sources;
	}

	public MetaDataSinkRegistry sinks()
	{
		return sinks;
	}


	// ========== Public API ==============================

	public Sets sets(String name)
	{
		return new Sets(sources.lookup(name));
	}

	public ItemIdentifiers identifiers(String name)
	{
		return new ItemIdentifiers(sources.lookup(name));
	}

	public Items items(String name)
	{
		return new Items(sources.lookup(name), sinks.lookup(name));
	}


	// ========== Subresources ==============================

	public class Sets
	{
		private final SetSource sets;

		private Sets(MetaDataSource mdsource)
		{
			if (mdsource == null || mdsource.sets() == null)
				throw new NotFoundException("Sets are not supported!");

			this.sets = mdsource.sets();
		}

		@HEAD
		public Response supported()
		{
			return Response.ok()
					.build();
		}

		@HEAD
		@Path("/{setspec}")
		@SecuredInternal
		public CompletionStage<Response> exists(@PathParam("setspec") String setspec)
		{
			final Function<Boolean, Response> responder = (isfound) -> {
				final Response.Status status = (isfound) ? Response.Status.OK : Response.Status.NOT_FOUND;
				return Response.status(status)
						.build();
			};

			return sets.exists(setspec)
					.thenApply(responder);
		}

		@GET
		public CompletionStage<Response> list(@Context HttpServletRequest request)
		{
			final int offset = MetaDataRepositoryAPI.getIntParam(request, HttpDefs.QueryParams.OFFSET, 0);
			final int count = MetaDataRepositoryAPI.getIntParam(request, HttpDefs.QueryParams.COUNT, Integer.MAX_VALUE);

			final BiFunction<Stream<SetDescription>, Integer, Response> responder = (descriptions, totalcount) -> {
				// Streaming response writer
				final StreamingOutput streamer = (output) -> {
					try (final SetDescriptionStream.Writer writer = new SetDescriptionStream.Writer(output)) {
						final SetDescriptionStream.Header header = new SetDescriptionStream.Header()
								.setTotalCount(totalcount);

						writer.write(header)
								.write(descriptions);
					}
					catch (Exception error) {
						throw new IOException("Serializing stream of set-descriptions failed!", error);
					}
				};

				try {
					return Response.ok(streamer, HttpDefs.MediaTypes.SETS)
							.encoding(StandardCharsets.UTF_8.toString())
							.build();
				}
				catch (Exception error) {
					final String message = "Building response failed!";
					throw new InternalServerErrorException(message, error);
				}
			};

			// Count and list set-descriptions in parallel
			return sets.list(offset, count)
					.thenCombine(sets.count(), responder);
		}
	}


	public class ItemIdentifiers
	{
		private final ItemIdentifierSource ids;

		private ItemIdentifiers(MetaDataSource mdsource)
		{
			if (mdsource == null || mdsource.identifiers() == null)
				throw new NotFoundException("ItemIdentifiers are not supported!");

			this.ids = mdsource.identifiers();
		}

		@GET
		public CompletionStage<Response> list(@Context HttpServletRequest request)
		{
			final QueryOptions options = MetaDataRepositoryAPI.getQueryOptions(request);

			final BiFunction<Stream<ItemIdentifierDescription>, Integer, Response> responder = (descriptions, totalcount) -> {
				// Streaming response writer
				final StreamingOutput streamer = (output) -> {
					try (final ItemIdentifierDescriptionStream.Writer writer = new ItemIdentifierDescriptionStream.Writer(output)) {
						final ItemIdentifierDescriptionStream.Header header = new ItemIdentifierDescriptionStream.Header()
								.setTotalCount(totalcount);

						writer.write(header)
								.write(descriptions);
					}
					catch (Exception error) {
						throw new IOException("Serializing stream of identifier-descriptions failed!", error);
					}
				};

				try {
					return Response.ok(streamer, HttpDefs.MediaTypes.IDENTIFIERS)
							.encoding(StandardCharsets.UTF_8.toString())
							.build();
				}
				catch (Exception error) {
					final String message = "Building response failed!";
					throw new InternalServerErrorException(message, error);
				}
			};

			// Count and list identifier-descriptions in parallel
			return ids.list(options)
					.thenCombine(ids.count(), responder);
		}
	}

	public class Items
	{
		private ItemSource source;
		private ItemSink sink;

		private Items(MetaDataSource mdsource, MetaDataSink mdsink)
		{
			if ((mdsource == null || mdsource.items() == null) && (mdsink == null || mdsink.items() == null))
				throw new NotFoundException("Items source is not supported!");

			if(mdsource != null && mdsource.items() != null)
				this.source = mdsource.items();
			if(mdsink != null && mdsink.items() != null)
				this.sink = mdsink.items();
		}

		@GET
		public CompletionStage<Response> list(@Context HttpServletRequest request)
		{
			if(source == null)
				throw new InternalServerErrorException("list: no source configured");

			final QueryOptions options = MetaDataRepositoryAPI.getQueryOptions(request);

			final BiFunction<Stream<ItemDescription>, Integer, Response> responder = (descriptions, totalcount) -> {
				// Streaming response writer
				final StreamingOutput streamer = (output) -> {
					try (final ItemDescriptionStream.Writer writer = new ItemDescriptionStream.Writer(output)) {
						final ItemDescriptionStream.Header header = new ItemDescriptionStream.Header()
								.setTotalCount(totalcount);

						writer.write(header)
								.write(descriptions);
					}
					catch (Exception error) {
						throw new IOException("Serializing stream of item-descriptions failed!", error);
					}
				};

				try {
					return Response.ok(streamer, HttpDefs.MediaTypes.ITEMS)
							.encoding(StandardCharsets.UTF_8.toString())
							.build();
				}
				catch (Exception error) {
					final String message = "Building response failed!";
					throw new InternalServerErrorException(message, error);
				}
			};

			// Count and list item-descriptions in parallel
			return source.list(options)
					.thenCombine(source.count(), responder);
		}

		@POST
		public Response insert(@Context HttpServletRequest request)
		{
			if(sink == null)
				throw new InternalServerErrorException("insert: no sink configured");

			try (final ItemDescriptionStream istream = ItemDescriptionStream.create(request.getInputStream())) {
				boolean inserted = false;
				// Process incoming stream of items...
				for (String part : istream) {
					switch (part) {
						case ItemDescriptionStream.Parts.ITEMS:
							// Forward items to sink
							sink.insert(istream.getItemStream());
							inserted = true;
							break;

						case ItemDescriptionStream.Parts.HEADER:
							// Ignore the header for now!
							istream.skipJsonObject();
							break;

						default:
							throw new BadRequestException("Unknown JSON part found: " + part);
					}
				}

				if (!inserted)
					throw new BadRequestException("Items array is missing!");
			}
			catch (ClientErrorException error) {
				throw error;  // Rethrow it!
			}
			catch (Exception error) {
				throw new InternalServerErrorException("Inserting items failed!", error);
			}

			return Response.ok()
					.build();
		}
	}


	// ========== Internal Helpers ==============================

	private static int getIntParam(HttpServletRequest request, String name, int defvalue)
	{
		final String strvalue = request.getParameter(name);
		if (strvalue == null || strvalue.isEmpty())
			return defvalue;

		return Integer.parseInt(strvalue);
	}

	private static long getLongParam(HttpServletRequest request, String name, long defvalue)
	{
		final String strvalue = request.getParameter(name);
		if (strvalue == null || strvalue.isEmpty())
			return defvalue;

		return Long.parseLong(strvalue);
	}

	private static String getStringParam(HttpServletRequest request, String name, String defvalue)
	{
		final String strvalue = request.getParameter(name);
		if (strvalue == null || strvalue.isEmpty())
			return defvalue;

		return strvalue;
	}

	private static QueryOptions getQueryOptions(HttpServletRequest request)
	{
		return new QueryOptions()
				.withOffset(MetaDataRepositoryAPI.getIntParam(request, HttpDefs.QueryParams.OFFSET, QueryOptions.Defaults.OFFSET))
				.withCount(MetaDataRepositoryAPI.getIntParam(request, HttpDefs.QueryParams.COUNT, QueryOptions.Defaults.COUNT))
				.withFrom(MetaDataRepositoryAPI.getLongParam(request, HttpDefs.QueryParams.FROM, QueryOptions.Defaults.FROM))
				.withUntil(MetaDataRepositoryAPI.getLongParam(request, HttpDefs.QueryParams.UNTIL, QueryOptions.Defaults.UNTIL))
				.withSetSpec(MetaDataRepositoryAPI.getStringParam(request, HttpDefs.QueryParams.SETSPEC, null));
	}
}
