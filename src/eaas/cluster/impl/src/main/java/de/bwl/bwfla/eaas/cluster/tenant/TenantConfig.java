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

package de.bwl.bwfla.eaas.cluster.tenant;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;


public class TenantConfig
{
	private String name;
	private ResourceSpec quota;


	// ========== Getters and Setters ==============================

	@JsonGetter(Fields.NAME)
	public String getName()
	{
		return name;
	}

	@JsonSetter(Fields.NAME)
	public void setName(String name)
	{
		ConfigHelpers.check(name, "Name is invalid!");
		this.name = name;
	}

	@JsonGetter(Fields.QUOTA)
	public ResourceSpec getQuotaLimits()
	{
		return quota;
	}

	@JsonSetter(Fields.QUOTA)
	public void setQuotaLimits(ResourceSpec quota)
	{
		ConfigHelpers.check(quota, "Quota is invalid!");
		this.quota = quota;
	}

	public static DocumentCollection.Filter filter(String name)
	{
		return DocumentCollection.filter()
				.eq(Fields.NAME, name);
	}


	// ========== Internal Helpers ==============================

	public static class Fields
	{
		public static final String NAME   = "name";
		public static final String QUOTA  = "quota";
	}
}
