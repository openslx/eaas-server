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


/** External representation of a digital-publication record */
public class DigPubRecord
{
	private String extid;
	private String link;
	private DigPubStatus status;

	public DigPubRecord()
	{
		// Empty!
	}

	@JsonSetter(Fields.EXTERNAL_ID)
	public DigPubRecord setExternalId(String id)
	{
		this.extid = id;
		return this;
	}

	/** Publication's external ID */
	@JsonGetter(Fields.EXTERNAL_ID)
	public String getExternalId()
	{
		return extid;
	}

	@JsonSetter(Fields.LINK)
	public DigPubRecord setLink(String link)
	{
		this.link = link;
		return this;
	}

	/** Publication's access-link */
	@JsonGetter(Fields.LINK)
	public String getLink()
	{
		return link;
	}

	@JsonSetter(Fields.STATUS)
	public DigPubRecord setStatus(DigPubStatus status)
	{
		this.status = status;
		return this;
	}

	/** Publication's status */
	@JsonGetter(Fields.STATUS)
	public DigPubStatus getStatus()
	{
		return status;
	}

	public static final class Fields
	{
		public static final String EXTERNAL_ID = "ext_id";
		public static final String STATUS      = "status";
		public static final String LINK        = "link";
	}
}
