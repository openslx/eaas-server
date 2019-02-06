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

import org.dspace.xoai.dataprovider.handlers.results.ListSetsResult;
import org.dspace.xoai.dataprovider.model.Set;
import de.bwl.bwfla.metadata.repository.api.SetDescription;
import de.bwl.bwfla.metadata.repository.api.SetDescriptionStream;
import de.bwl.bwfla.metadata.repository.client.MetaDataRepository;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class SetRepository implements org.dspace.xoai.dataprovider.repository.SetRepository
{
	private final MetaDataRepository repository;

	public SetRepository(MetaDataRepository repository)
	{
		this.repository = repository;
	}


	// =============== SetRepository implementation ==============================

	@Override
	public boolean supportSets()
	{
		final MetaDataRepository.Sets.Head request = repository.sets()
				.head();

		try (final Response response = request.execute()) {
			return response.getStatus() == Response.Status.OK.getStatusCode();
		}
	}

	@Override
	public ListSetsResult retrieveSets(int offset, int length)
	{
		final MetaDataRepository.Sets.List request = repository.sets()
				.list(offset, length);

		// Send HTTP request and parse streaming response...
		try (final SetDescriptionStream response = request.execute()) {
			final ListSetsResultBuilder rb = new ListSetsResultBuilder();
			for (String part : response) {
				switch (part) {
					case SetDescriptionStream.Parts.HEADER:
						final SetDescriptionStream.Header header = response.getHeader();
						final int numtotal = header.getTotalCount();
						rb.withHasMore(offset + length < numtotal)
								.withTotalCount(numtotal);
						break;

					case SetDescriptionStream.Parts.SETS:
						final List<Set> sets = response.getSetStream()
								.map(SET_DESCRIPTION_MAPPER)
								.collect(Collectors.toList());

						rb.withSets(sets);
						break;

					default:
						// TODO: fail with more info!
						throw new IllegalStateException();
				}
			}

			return rb.build();
		}
	}

	@Override
	public boolean exists(String spec)
	{
		final MetaDataRepository.Sets.Head request = repository.sets()
				.head(spec);

		try (final Response response = request.execute()) {
			return response.getStatus() == Response.Status.OK.getStatusCode();
		}
	}


	// =============== Internal Helpers ==============================

	private static final Function<SetDescription, Set> SET_DESCRIPTION_MAPPER = (desc) -> {
		return new Set(desc.getSpec())
				.withName(desc.getName());
	};


	private static class ListSetsResultBuilder
	{
		private List<Set> sets;
		private boolean hasmore = false;
		private int numtotal = -1;


		public ListSetsResultBuilder withHasMore(boolean hasmore)
		{
			this.hasmore = hasmore;
			return this;
		}

		public ListSetsResultBuilder withTotalCount(int count)
		{
			this.numtotal = count;
			return this;
		}

		public ListSetsResultBuilder withSets(List<Set> sets)
		{
			this.sets = sets;
			return this;
		}

		public ListSetsResult build()
		{
			return new ListSetsResult(hasmore, sets, numtotal);
		}
	}
}
