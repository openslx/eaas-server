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

package de.bwl.bwfla.blobstore;


import de.bwl.bwfla.blobstore.api.Blob;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ByteRange;
import de.bwl.bwfla.common.utils.ByteRangeIterator;

import java.util.List;
import java.util.concurrent.TimeUnit;


public interface IBlobStoreBackend
{
	BlobHandle save(BlobDescription description) throws BWFLAException;
	Blob load(BlobHandle handle) throws BWFLAException;
	ByteRangeIterator load(Blob blob, List<ByteRange> ranges) throws BWFLAException;
	void delete(BlobHandle handle) throws BWFLAException;

	/** Perform GC of old entries */
	void cleanup(long maxEntryAge, TimeUnit unit) throws BWFLAException;
}
