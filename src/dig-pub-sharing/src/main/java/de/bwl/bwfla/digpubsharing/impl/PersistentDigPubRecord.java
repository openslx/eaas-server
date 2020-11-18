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

package de.bwl.bwfla.digpubsharing.impl;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.digpubsharing.api.DigPubStatus;


public class PersistentDigPubRecord
{
	private String tenant;
	private String extid;
	private int numseats;
	private DigPubStatus status;

	public PersistentDigPubRecord()
	{
		// Empty!
	}

	@JsonSetter(Fields.TENANT_ID)
	public PersistentDigPubRecord setTenantId(String id)
	{
		this.tenant = id;
		return this;
	}

	@JsonGetter(Fields.TENANT_ID)
	public String getTenantId()
	{
		return tenant;
	}

	@JsonSetter(Fields.EXTERNAL_ID)
	public PersistentDigPubRecord setExternalId(String id)
	{
		this.extid = id;
		return this;
	}

	@JsonGetter(Fields.EXTERNAL_ID)
	public String getExternalId()
	{
		return extid;
	}

	@JsonSetter(Fields.NUM_SEATS)
	public PersistentDigPubRecord setNumSeats(int num)
	{
		this.numseats = num;
		return this;
	}

	@JsonGetter(Fields.NUM_SEATS)
	public int getNumSeats()
	{
		return numseats;
	}

	@JsonSetter(Fields.STATUS)
	public PersistentDigPubRecord setStatus(DigPubStatus status)
	{
		this.status = status;
		return this;
	}

	@JsonGetter(Fields.STATUS)
	public DigPubStatus getStatus()
	{
		if (status == null)
			status = new DigPubStatus();

		return status;
	}

	public static DocumentCollection.Filter filter(String tenant)
	{
		return DocumentCollection.filter()
				.eq(Fields.TENANT_ID, tenant);
	}

	public static DocumentCollection.Filter filter(String tenant, String extid)
	{
		return DocumentCollection.filter()
				.eq(Fields.TENANT_ID, tenant)
				.eq(Fields.EXTERNAL_ID, extid);
	}

	public static void index(DocumentCollection<PersistentDigPubRecord> collection) throws BWFLAException
	{
		collection.index(Fields.TENANT_ID, Fields.EXTERNAL_ID);
	}

	public static final class Fields
	{
		public static final String TENANT_ID   = "tenant_id";
		public static final String EXTERNAL_ID = "ext_id";
		public static final String NUM_SEATS   = "num_seats";
		public static final String STATUS      = "status";
	}
}
