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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;


/** External representation of digital-publication's status */
public class DigPubStatus
{
	private String matchdate;
	private boolean isnew;

	public DigPubStatus()
	{
		// Empty!
	}

	@JsonSetter(Fields.MATCHED_ON)
	public DigPubStatus setMatchedOnDate(String date)
	{
		this.matchdate = date;
		return this;
	}

	/**
	 * Date this publication was matched on
	 *
	 * @documentationExample 2020-12-02
	 */
	@JsonGetter(Fields.MATCHED_ON)
	public String getMatchedOnDate()
	{
		return matchdate;
	}

	@JsonSetter(Fields.IS_NEW)
	public DigPubStatus setNewFlag(boolean isnew)
	{
		this.isnew = isnew;
		return this;
	}

	/** Is this publication new? Yes, if it was not exported yet, else no. */
	@JsonGetter(Fields.IS_NEW)
	public boolean isNew()
	{
		return isnew;
	}

	@JsonIgnore
	public boolean isMatched()
	{
		return matchdate != null;
	}

	public static final class Fields
	{
		public static final String MATCHED_ON = "matched_on";
		public static final String IS_NEW     = "is_new";
	}
}
