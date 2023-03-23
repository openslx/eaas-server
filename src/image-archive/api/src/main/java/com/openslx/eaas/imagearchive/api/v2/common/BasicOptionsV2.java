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

package com.openslx.eaas.imagearchive.api.v2.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.openslx.eaas.common.databind.DataUtils;
import com.openslx.eaas.common.util.JaxRsUtils;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.Map;


public class BasicOptionsV2<T extends BasicOptionsV2<T>>
{
	@QueryParam("location")
	private String location;

	@HeaderParam("eaas-user-info")
	private UserInfo userinfo;


	public T setLocation(String location)
	{
		this.location = location;
		return (T) this;
	}

	public String location()
	{
		return location;
	}

	public T setUserInfo(UserInfo info)
	{
		this.userinfo = info;
		return (T) this;
	}

	public UserInfo userinfo()
	{
		if (userinfo == null)
			userinfo = new UserInfo();

		return userinfo;
	}

	public Map<String, String> toHeaderParams()
	{
		final var headers = new HashMap<String, String>();
		JaxRsUtils.extractHeaderParams(this, headers);
		return headers;
	}

	public Map<String, String> toQueryParams()
	{
		final var query = new HashMap<String, String>();
		JaxRsUtils.extractQueryParams(this, query);
		return query;
	}


	public static class UserInfo
	{
		private String userid;
		private String tenantid;

		@JsonSetter("uid")
		public UserInfo setUserId(String id)
		{
			this.userid = id;
			return this;
		}

		@JsonGetter("uid")
		public String userid()
		{
			return userid;
		}

		@JsonSetter("tid")
		public UserInfo setTenantId(String id)
		{
			this.tenantid = id;
			return this;
		}

		@JsonGetter("tid")
		public String tenantid()
		{
			return tenantid;
		}

		@Override
		public String toString()
		{
			try {
				return DataUtils.json()
						.writer(false)
						.writeValueAsString(this);
			}
			catch (Exception error) {
				throw new RuntimeException("Serializing user-info failed!", error);
			}
		}

		public static UserInfo valueOf(String value) throws Exception
		{
			return DataUtils.json()
					.read(value, UserInfo.class);
		}
	}
}
