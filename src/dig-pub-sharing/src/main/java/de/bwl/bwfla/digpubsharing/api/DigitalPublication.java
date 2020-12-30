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
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;


/** Internal representation of a digital-publication record */
public class DigitalPublication
{
	private String extid;
	private String objid;
	private String envid;

	private String objectArchive;

	public DigitalPublication()
	{
		// Empty!
	}

	@JsonSetter(Fields.EXTERNAL_ID)
	public DigitalPublication setExternalId(String id)
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

	@JsonSetter(Fields.OBJECT_ID)
	public DigitalPublication setObjectId(String id)
	{
		this.objid = id;
		return this;
	}

	/** Publication's internal object ID */
	@JsonGetter(Fields.OBJECT_ID)
	public String getObjectId()
	{
		return objid;
	}

	@JsonSetter(Fields.ENVIRONMENT_ID)
	public DigitalPublication setEnvironmentId(String id)
	{
		this.envid = id;
		return this;
	}

	/** Environment ID this publication is compatible with */
	@JsonGetter(Fields.ENVIRONMENT_ID)
	public String getEnvironmentId()
	{
		return envid;
	}

	@JsonSetter(Fields.OBJECT_ARCHIVE)
	public DigitalPublication setObjectArchive(String archive)
	{
		this.objectArchive = archive;
		return this;
	}

	/** Publication's internal object archive */
	@JsonGetter(Fields.OBJECT_ARCHIVE)
	public String getObjectArchive()
	{
		return objectArchive;
	}

	public static DocumentCollection.Filter filter(String id)
	{
		return DigitalPublication.filter(id, true);
	}

	public static DocumentCollection.Filter filter(String id, boolean external)
	{
		final String field = (external) ? Fields.EXTERNAL_ID : Fields.OBJECT_ID;
		return DocumentCollection.filter()
				.eq(field, id);
	}

	public static void index(DocumentCollection<DigitalPublication> collection) throws BWFLAException
	{
		collection.index(Fields.EXTERNAL_ID);
	}

	public static final class Fields
	{
		public static final String EXTERNAL_ID    = "ext_id";
		public static final String OBJECT_ID      = "obj_id";
		public static final String ENVIRONMENT_ID = "env_id";
		public static final String OBJECT_ARCHIVE = "obj_archive";
	}
}
