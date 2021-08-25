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

package com.openslx.eaas.imagearchive.service;

import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.BlobKind;
import com.openslx.eaas.imagearchive.indexing.BlobDescriptor;
import com.openslx.eaas.imagearchive.indexing.BlobIndex;
import com.openslx.eaas.imagearchive.indexing.FilterOptions;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;


public abstract class AbstractService<T extends BlobDescriptor> implements AutoCloseable
{
	private final Logger logger;
	private final Filter<String> idfilter;
	private final Filter<FilterOptions> optfilter;
	private final BlobIndex<T> index;

	public static final String UNKNOWN_LOCATION = null;


	/** Count currently indexed records */
	public long count()
	{
		return this.count(null);
	}

	/** Count currently indexed records */
	public long count(FilterOptions options)
	{
		final var collection = index.collection();
		return (options == null) ? collection.count()
				: collection.count(optfilter.apply(options));
	}

	/** List all indexed records */
	public Stream<T> list() throws BWFLAException
	{
		return index.collection()
				.list()
				.stream();
	}

	/** List all indexed records within given range */
	public Stream<T> list(int offset, int limit) throws BWFLAException
	{
		return this.list(null, offset, limit);
	}

	/** List all indexed records within given range */
	public Stream<T> list(FilterOptions options, int offset, int limit) throws BWFLAException
	{
		final var collection = index.collection();
		final var result = (options == null) ? collection.list()
				: collection.find(optfilter.apply(options));

		return result.skip(offset)
				.limit(limit)
				.stream();
	}

	/** Look up a record by ID */
	public T lookup(String id) throws BWFLAException
	{
		return index.collection()
				.lookup(idfilter.apply(id));
	}

	/** Remove record by ID */
	public boolean remove(String id) throws BWFLAException
	{
		return index.collection()
				.delete(idfilter.apply(id));
	}

	@Override
	public void close() throws Exception
	{
		// Empty!
	}

	public Logger logger()
	{
		return logger;
	}


	// ===== Internal Helpers ==============================

	protected interface Filter<U> extends Function<U, DocumentCollection.Filter>
	{
		// Empty!
	}

	protected AbstractService(BlobIndex<T> index, Filter<String> idfilter, Filter<FilterOptions> optfilter)
	{
		this.logger = ArchiveBackend.logger("service", index.name());
		this.idfilter = idfilter;
		this.optfilter = optfilter;
		this.index = index;
	}

	protected BlobIndex<T> index()
	{
		return index;
	}

	protected BlobKind kind()
	{
		return index.kind();
	}

	protected boolean update(String id, DocumentCollection.Update update) throws BWFLAException
	{
		return this.update(id, update, false) > 0;
	}

	protected long update(String id, DocumentCollection.Update update, boolean many) throws BWFLAException
	{
		return index.collection()
				.update(idfilter.apply(id), update, many);
	}
}
