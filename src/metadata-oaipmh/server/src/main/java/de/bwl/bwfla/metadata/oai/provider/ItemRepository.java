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

package de.bwl.bwfla.metadata.oai.provider;

import de.bwl.bwfla.metadata.repository.source.QueryOptions;
import org.dspace.xoai.dataprovider.exceptions.IdDoesNotExistException;
import org.dspace.xoai.dataprovider.exceptions.OAIException;
import org.dspace.xoai.dataprovider.filter.ScopedFilter;
import org.dspace.xoai.dataprovider.handlers.results.ListItemIdentifiersResult;
import org.dspace.xoai.dataprovider.handlers.results.ListItemsResults;
import org.dspace.xoai.dataprovider.model.Item;
import org.dspace.xoai.dataprovider.model.ItemIdentifier;
import org.dspace.xoai.dataprovider.model.Set;
import org.dspace.xoai.model.oaipmh.About;
import org.dspace.xoai.model.oaipmh.Metadata;
import de.bwl.bwfla.metadata.repository.api.ItemDescription;
import de.bwl.bwfla.metadata.repository.api.ItemDescriptionStream;
import de.bwl.bwfla.metadata.repository.api.ItemIdentifierDescription;
import de.bwl.bwfla.metadata.repository.api.ItemIdentifierDescriptionStream;
import de.bwl.bwfla.metadata.repository.client.MetaDataRepository;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ItemRepository implements org.dspace.xoai.dataprovider.repository.ItemRepository
{
	private final MetaDataRepository repository;

	public ItemRepository(MetaDataRepository repository)
	{
		this.repository = repository;
	}


	// =============== ItemRepository implementation ==============================

	@Override
	public Item getItem(String identifier) throws IdDoesNotExistException
	{
		final MetaDataRepository.Items.Get request = repository.items()
				.get(identifier);

		// Send HTTP request and parse response...
		try (final Response response = request.execute()) {
			if (response.getStatus() != Response.Status.NOT_FOUND.getStatusCode())
				throw new IdDoesNotExistException("Item's ID: " + identifier);

			return ITEM_MAPPER.apply(ItemDescription.from(response));
		}
	}

	@Override
	public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length) throws OAIException
	{
		return this.findItemIdentifiers(offset, length, new QueryOptions());
	}

	@Override
	public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, Date from) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withFrom(from);

		return this.findItemIdentifiers(offset, length, options);
	}

	@Override
	public ListItemIdentifiersResult getItemIdentifiersUntil(List<ScopedFilter> filters, int offset, int length, Date until) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withUntil(until);

		return this.findItemIdentifiers(offset, length, options);
	}

	@Override
	public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, Date from, Date until) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withFrom(from)
				.withUntil(until);

		return this.findItemIdentifiers(offset, length, options);
	}

	@Override
	public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, String setspec) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withSetSpec(setspec);

		return this.findItemIdentifiers(offset, length, options);
	}

	@Override
	public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, String setspec, Date from) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withSetSpec(setspec)
				.withFrom(from);

		return this.findItemIdentifiers(offset, length, options);
	}

	@Override
	public ListItemIdentifiersResult getItemIdentifiersUntil(List<ScopedFilter> filters, int offset, int length, String setspec, Date until) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withSetSpec(setspec)
				.withUntil(until);

		return this.findItemIdentifiers(offset, length, options);
	}

	@Override
	public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> unused, int offset, int length, String setspec, Date from, Date until) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withSetSpec(setspec)
				.withFrom(from)
				.withUntil(until);

		return this.findItemIdentifiers(offset, length, options);
	}

	@Override
	public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length) throws OAIException
	{
		return this.findItems(offset, length, new QueryOptions());
	}

	@Override
	public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, Date from) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withFrom(from);

		return this.findItems(offset, length, options);
	}

	@Override
	public ListItemsResults getItemsUntil(List<ScopedFilter> filters, int offset, int length, Date until) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withUntil(until);

		return this.findItems(offset, length, options);
	}

	@Override
	public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, Date from, Date until) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withFrom(from)
				.withUntil(until);

		return this.findItems(offset, length, options);
	}

	@Override
	public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setspec) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withSetSpec(setspec);

		return this.findItems(offset, length, options);
	}

	@Override
	public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setspec, Date from) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withSetSpec(setspec)
				.withFrom(from);

		return this.findItems(offset, length, options);
	}

	@Override
	public ListItemsResults getItemsUntil(List<ScopedFilter> filters, int offset, int length, String setspec, Date until) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withSetSpec(setspec)
				.withUntil(until);

		return this.findItems(offset, length, options);
	}

	@Override
	public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setspec, Date from, Date until) throws OAIException
	{
		final QueryOptions options = new QueryOptions()
				.withSetSpec(setspec)
				.withFrom(from)
				.withUntil(until);

		return this.findItems(offset, length, options);
	}


	// =============== Internal Helpers ==============================

	private static final Function<ItemIdentifierDescription, ItemIdentifier> ITEM_IDENTIFIER_MAPPER =
			(desc) -> new CustomItemIdentifier(desc);

	private static final Function<ItemDescription, Item> ITEM_MAPPER =
			(desc) -> new CustomItem(desc);


	private static class CustomItemIdentifier implements ItemIdentifier
	{
		private final ItemIdentifierDescription desc;

		public CustomItemIdentifier(ItemIdentifierDescription desc)
		{
			this.desc = desc;
		}

		@Override
		public String getIdentifier()
		{
			return desc.getId();
		}

		@Override
		public Date getDatestamp()
		{
			return desc.getTimestampAsDate();
		}

		@Override
		public List<Set> getSets()
		{
			return desc.getSets().stream()
					.map((setspec) -> new Set(setspec))
					.collect(Collectors.toList());
		}

		@Override
		public boolean isDeleted()
		{
			return desc.isDeleted();
		}
	}

	private static class CustomItem extends CustomItemIdentifier implements Item
	{
		private final ItemDescription desc;

		public CustomItem(ItemDescription desc)
		{
			super(desc.getIdentifier());
			this.desc = desc;
		}

		@Override
		public List<About> getAbout()
		{
			return new ArrayList<>();
		}

		@Override
		public Metadata getMetadata()
		{
			return new Metadata(desc.getMetaData());
		}
	}


	private static class ListItemsResultBuilder
	{
		private List<Item> items;
		private boolean hasmore = false;
		private int numtotal = -1;


		public ListItemsResultBuilder withHasMore(boolean hasmore)
		{
			this.hasmore = hasmore;
			return this;
		}

		public ListItemsResultBuilder withTotalCount(int count)
		{
			this.numtotal = count;
			return this;
		}

		public ListItemsResultBuilder withItems(List<Item> items)
		{
			this.items = items;
			return this;
		}

		public ListItemsResults build()
		{
			return new ListItemsResults(hasmore, items, numtotal);
		}
	}

	private static class ListItemIdentifiersResultBuilder
	{
		private List<ItemIdentifier> ids;
		private boolean hasmore = false;
		private int numtotal = -1;


		public ListItemIdentifiersResultBuilder withHasMore(boolean hasmore)
		{
			this.hasmore = hasmore;
			return this;
		}

		public ListItemIdentifiersResultBuilder withTotalCount(int count)
		{
			this.numtotal = count;
			return this;
		}

		public ListItemIdentifiersResultBuilder withItemIdentifiers(List<ItemIdentifier> ids)
		{
			this.ids = ids;
			return this;
		}

		public ListItemIdentifiersResult build()
		{
			return new ListItemIdentifiersResult(hasmore, ids, numtotal);
		}
	}

	private ListItemIdentifiersResult findItemIdentifiers(int offset, int length, QueryOptions options) throws OAIException
	{
		try {
			options.withOffset(offset)
					.withCount(length);

			final MetaDataRepository.ItemIdentifiers.List request = repository.identifiers()
					.list(options);

			// Send HTTP request and parse streaming response...
			try (final ItemIdentifierDescriptionStream response = request.execute()) {
				final ListItemIdentifiersResultBuilder result = new ListItemIdentifiersResultBuilder();
				for (String part : response) {
					switch (part) {
						case ItemIdentifierDescriptionStream.Parts.HEADER:
							final ItemIdentifierDescriptionStream.Header header = response.getHeader();
							final int numtotal = header.getTotalCount();
							result.withHasMore(offset + length < numtotal)
									.withTotalCount(numtotal);
							break;

						case ItemIdentifierDescriptionStream.Parts.IDENTIFIERS:
							final List<ItemIdentifier> ids = response.getItemIdentifierStream()
									.map(ITEM_IDENTIFIER_MAPPER)
									.collect(Collectors.toList());

							result.withItemIdentifiers(ids);
							break;

						default:
							// TODO: fail with more info!
							throw new IllegalStateException("Unexpected JSON token found: " + part);
					}
				}

				return result.build();
			}
		}
		catch (Exception error) {
			throw new OAIException(error);
		}
	}

	private ListItemsResults findItems(int offset, int length, QueryOptions options) throws OAIException
	{
		try {
			options.withOffset(offset)
					.withCount(length);

			final MetaDataRepository.Items.List request = repository.items()
					.list(options);

			// Send HTTP request and parse streaming response...
			try (final ItemDescriptionStream response = request.execute()) {
				final ListItemsResultBuilder result = new ListItemsResultBuilder();
				for (String part : response) {
					switch (part) {
						case ItemDescriptionStream.Parts.HEADER:
							final ItemDescriptionStream.Header header = response.getHeader();
							final int numtotal = header.getTotalCount();
							result.withHasMore(offset + length < numtotal)
									.withTotalCount(numtotal);
							break;

						case ItemDescriptionStream.Parts.ITEMS:
							final List<Item> items = response.getItemStream()
									.map(ITEM_MAPPER)
									.collect(Collectors.toList());

							result.withItems(items);
							break;

						default:
							// TODO: fail with more info!
							throw new IllegalStateException("Unexpected JSON token found: " + part);
					}
				}

				return result.build();
			}
		}
		catch (Exception error) {
			throw new OAIException(error);
		}
	}
}
