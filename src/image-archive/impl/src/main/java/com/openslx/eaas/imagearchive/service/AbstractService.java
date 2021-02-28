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
import com.openslx.eaas.imagearchive.indexing.BlobIndex;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;


public abstract class AbstractService<T> implements AutoCloseable
{
	private final Logger logger;
	private final IdentifierFilter idfilter;
	private final BlobIndex<T> index;


	/** Count currently indexed records */
	public long count()
	{
		return index.collection()
				.count();
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
		return index.collection()
				.list()
				.skip(offset)
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

	protected interface IdentifierFilter extends Function<String, DocumentCollection.Filter>
	{
		// Empty!
	}

	protected AbstractService(BlobIndex<T> index, IdentifierFilter idfilter)
	{
		this.logger = ArchiveBackend.logger("service", index.name());
		this.idfilter = idfilter;
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
}
