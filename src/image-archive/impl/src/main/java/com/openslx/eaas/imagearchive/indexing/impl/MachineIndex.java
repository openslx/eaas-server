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

import com.openslx.eaas.imagearchive.BlobKind;
import com.openslx.eaas.imagearchive.indexing.BlobIndex;
import com.openslx.eaas.imagearchive.indexing.BlobIngestors;
import com.openslx.eaas.imagearchive.indexing.DataRecord;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;

import java.io.InputStream;


public class MachineIndex extends BlobIndex<MachineIndex.Record>
{
	public static class Record extends DataRecord<MachineConfiguration>
	{
		@Override
		public DataRecord<MachineConfiguration> setData(InputStream data) throws Exception
		{
			final var machine = MachineConfiguration.from(data, MachineConfiguration.class);
			machine.setTimestamp(this.mtime());
			return this.setData(machine);
		}
	}

	public MachineIndex()
	{
		super(BlobKind.MACHINE, Record.class, Record::index, BlobIngestors.records(Record::new));
	}
}
