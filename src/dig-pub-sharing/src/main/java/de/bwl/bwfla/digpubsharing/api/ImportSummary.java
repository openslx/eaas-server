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

package de.bwl.bwfla.digpubsharing.api;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;


/** Summary of an import */
public class ImportSummary
{
	private int numRecordsImported;
	private int numRecordsMatched;
	private int numRecordsIndexed;

	public ImportSummary()
	{
		// Empty!
	}

	@JsonSetter(Fields.NUM_RECORDS_IMPORTED)
	public ImportSummary setNumRecordsImported(int num)
	{
		this.numRecordsImported = num;
		return this;
	}

	/** Number of imported records */
	@JsonGetter(Fields.NUM_RECORDS_IMPORTED)
	public int getNumRecordsImported()
	{
		return numRecordsImported;
	}

	@JsonSetter(Fields.NUM_RECORDS_MATCHED)
	public ImportSummary setNumRecordsMatched(int num)
	{
		this.numRecordsMatched = num;
		return this;
	}

	/** Number of matched records */
	@JsonGetter(Fields.NUM_RECORDS_MATCHED)
	public int getNumRecordsMatched()
	{
		return numRecordsMatched;
	}

	@JsonSetter(Fields.NUM_RECORDS_INDEXED)
	public ImportSummary setNumRecordsIndexed(int num)
	{
		this.numRecordsIndexed = num;
		return this;
	}

	/** Number of indexed records */
	@JsonGetter(Fields.NUM_RECORDS_INDEXED)
	public int getNumRecordsIndexed()
	{
		return numRecordsIndexed;
	}

	/** Number of missing records */
	@JsonGetter(Fields.NUM_RECORDS_MISSING)
	public int getNumRecordsMissing()
	{
		return numRecordsImported - numRecordsMatched;
	}

	public static final class Fields
	{
		public static final String NUM_RECORDS_IMPORTED = "num_imported_records";
		public static final String NUM_RECORDS_MATCHED  = "num_matched_records";
		public static final String NUM_RECORDS_MISSING  = "num_missing_records";
		public static final String NUM_RECORDS_INDEXED  = "num_indexed_records";
	}
}
