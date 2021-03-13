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

import de.bwl.bwfla.common.database.document.DocumentCollection;


public class BlobIngestorContext<T>
{
	private final BlobIndex<T> target;
	private final DocumentCollection<T> buffer;
	private final Counters counters;

	public BlobIngestorContext(BlobIndex<T> target, DocumentCollection<T> buffer)
	{
		this.target = target;
		this.buffer = buffer;
		this.counters = new Counters();
	}

	public BlobIndex<T> target()
	{
		return target;
	}

	public DocumentCollection<T> buffer()
	{
		return buffer;
	}

	public Counters counters()
	{
		return counters;
	}

	public BlobIngestorContext<T> fork()
	{
		return new BlobIngestorContext<>(this.target, this.buffer);
	}


	public static class Counters
	{
		private long numBytesDownloaded = 0L;
		private int numBlobsDownloaded = 0;
		private int numBlobsReused = 0;


		// ===== Updaters ===============

		public void onBlobDownloaded(long bytes)
		{
			numBytesDownloaded += bytes;
			++numBlobsDownloaded;
		}

		public void onBlobReused()
		{
			++numBlobsReused;
		}


		// ===== Getters ===============

		public long getNumBytesDownloaded()
		{
			return numBytesDownloaded;
		}

		public int getNumBlobsDownloaded()
		{
			return numBlobsDownloaded;
		}

		public int getNumBlobsReused()
		{
			return numBlobsReused;
		}
	}
}
