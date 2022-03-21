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

package com.openslx.eaas.imagearchive;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Stream;


public class AbstractRegistry<T extends AutoCloseable> implements AutoCloseable
{
	protected final ArrayList<T> entries;


	public T lookup(BlobKind kind)
	{
		return entries.get(kind.ordinal());
	}

	@Override
	public void close() throws Exception
	{
		entries.forEach(AbstractRegistry::close);
	}

	public Stream<T> stream()
	{
		return entries.stream()
				.filter(Objects::nonNull);
	}


	// ===== Internal Helpers ==============================

	protected AbstractRegistry()
	{
		final int capacity = BlobKind.count();
		this.entries = new ArrayList<>(capacity);
		for (int i = 0; i < capacity; ++i)
			entries.add(null);
	}

	protected <U extends T> U lookup(BlobKind kind, Class<U> clazz)
	{
		return clazz.cast(this.lookup(kind));
	}

	protected void insert(BlobKind kind, T entry)
	{
		entries.set(kind.ordinal(), entry);
	}

	protected static void close(AutoCloseable entry)
	{
		if (entry == null)
			return;

		try {
			entry.close();
		}
		catch (Exception error) {
			ArchiveBackend.logger()
					.log(Level.WARNING, "Closing registry failed!", error);
		}
	}
}
