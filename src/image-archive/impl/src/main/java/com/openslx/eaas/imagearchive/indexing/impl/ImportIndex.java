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

package com.openslx.eaas.imagearchive.indexing.impl;

import com.openslx.eaas.imagearchive.databind.ImportRecord;
import com.openslx.eaas.imagearchive.indexing.Index;
import de.bwl.bwfla.common.exceptions.BWFLAException;


public class ImportIndex extends Index<ImportRecord>
{
	public ImportIndex() throws BWFLAException
	{
		super("imports", ImportRecord.class, ImportRecord::index);

		try {
			this.preparer()
					.prepare(this.collection());
		}
		catch (Exception error) {
			throw new BWFLAException(error);
		}
	}

	public int lastid() throws BWFLAException
	{
		final var count = this.collection()
				.count();

		final var records = this.collection()
				.list();

		try (records) {
			return records.skip((int) (count - 1))
					.stream()
					.map(ImportRecord::taskid)
					.reduce(0, Integer::max);
		}
		catch (Exception error) {
			throw new BWFLAException(error);
		}
	}
}
