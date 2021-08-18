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

package de.bwl.bwfla.common.database.document;


import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.time.Instant;


public class DocumentUtils
{
	public static <T> long ensureDocumentTimestamp(DocumentCollection<T> collection) throws BWFLAException
	{
		final String field = "timestamp";

		final var filter = DocumentCollection.filter()
				.eq(field, null);

		final var update = DocumentCollection.updater()
				.set(field, Instant.now().toString());

		return collection.update(filter, update, true);
	}

	/** Construct a nested field from multiple parts */
	public static String nested(String... fields)
	{
		return String.join(".", fields);
	}
}
