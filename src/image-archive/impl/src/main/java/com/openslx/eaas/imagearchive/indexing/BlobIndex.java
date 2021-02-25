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

package com.openslx.eaas.imagearchive.indexing;

import com.openslx.eaas.imagearchive.BlobKind;
import com.openslx.eaas.imagearchive.storage.StorageLocation;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;
import de.bwl.bwfla.blobstore.BlobDescription;
import de.bwl.bwfla.common.exceptions.BWFLAException;


public class BlobIndex<T> extends Index<T>
{
	private final BlobKind kind;
	private final IBlobIngestor<T> ingestor;


	public int id()
	{
		return kind.ordinal();
	}

	public BlobKind kind()
	{
		return kind;
	}

	public synchronized void rebuild(StorageRegistry storage) throws BWFLAException
	{
		try (final var indexer = new BlobIndexer<>(this)) {
			indexer.index(storage);
		}
		catch (Exception error) {
			if (error instanceof BWFLAException)
				throw (BWFLAException) error;
			else throw new BWFLAException(error);
		}
	}

	public void ingest(BlobDescription blob, StorageLocation location) throws BWFLAException
	{
		try {
			// special context where buffer and destination collections are the same!
			final var context = new BlobIngestorContext<>(this, this.collection());
			ingestor.ingest(context, blob, location);
		}
		catch (Exception error) {
			if (error instanceof BWFLAException)
				throw (BWFLAException) error;
			else throw new BWFLAException(error);
		}
	}


	// ===== Internal Helpers ==============================

	protected BlobIndex(BlobKind kind, Class<T> clazz, IPreparer<T> preparer, IBlobIngestor<T> ingestor)
	{
		super(BlobIndex.toName(kind), clazz, preparer);
		this.kind = kind;
		this.ingestor = ingestor;
	}

	protected IBlobIngestor<T> ingestor()
	{
		return ingestor;
	}

	protected static String toName(BlobKind kind)
	{
		return kind.value() + "s";
	}
}
