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
import de.bwl.bwfla.digpubsharing.api.Settings;


public class PersistentSettings
{
	private String tenant;
	private Settings settings;

	public PersistentSettings()
	{
		// Empty!
	}

	@JsonSetter(Fields.TENANT_ID)
	public PersistentSettings setTenantId(String id)
	{
		this.tenant = id;
		return this;
	}

	@JsonGetter(Fields.TENANT_ID)
	public String getTenantId()
	{
		return tenant;
	}

	@JsonSetter(Fields.SETTINGS)
	public PersistentSettings setSettings(Settings settings)
	{
		this.settings = settings;
		return this;
	}

	@JsonGetter(Fields.SETTINGS)
	public Settings getSettings()
	{
		return settings;
	}

	public static DocumentCollection.Filter filter(String tenant)
	{
		return DocumentCollection.filter()
				.eq(Fields.TENANT_ID, tenant);
	}

	public static void index(DocumentCollection<PersistentSettings> collection) throws BWFLAException
	{
		collection.index(Fields.TENANT_ID);
	}

	public static final class Fields
	{
		public static final String TENANT_ID = "tenant_id";
		public static final String SETTINGS  = "settings";
	}
}
