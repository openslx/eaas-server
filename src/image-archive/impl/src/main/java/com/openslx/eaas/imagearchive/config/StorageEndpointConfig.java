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

package com.openslx.eaas.imagearchive.config;

import de.bwl.bwfla.common.utils.ConfigHelpers;
import org.apache.tamaya.inject.api.Config;


public class StorageEndpointConfig extends BaseConfig<StorageEndpointConfig>
{
	private String name;
	private String address;
	private String accesskey;
	private String secretkey;


	@Config("name")
	public void setName(String name)
	{
		ConfigHelpers.check(name, "Name is invalid!");
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Config("address")
	public void setAddress(String address)
	{
		ConfigHelpers.check(address, "Address is invalid!");
		this.address = address;
	}

	public String getAddress()
	{
		return address;
	}

	@Config("credentials.access_key")
	public void setAccessKey(String key)
	{
		ConfigHelpers.check(key, "Access key is invalid!");
		this.accesskey = key;
	}

	public String getAccessKey()
	{
		return accesskey;
	}

	@Config("credentials.secret_key")
	public void setSecretKey(String key)
	{
		ConfigHelpers.check(key, "Secret key is invalid!");
		this.secretkey = key;
	}

	public String getSecretKey()
	{
		return secretkey;
	}
}
