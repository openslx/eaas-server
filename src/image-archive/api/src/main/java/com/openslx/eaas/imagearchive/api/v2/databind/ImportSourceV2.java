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

package com.openslx.eaas.imagearchive.api.v2.databind;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.LinkedHashMap;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportSourceV2
{
	private String url;
	private Map<String, String> headers;

	@JsonSetter(Fields.URL)
	public ImportSourceV2 setUrl(String url)
	{
		this.url = url;
		return this;
	}

	@JsonGetter(Fields.URL)
	public String url()
	{
		return url;
	}

	@JsonSetter(Fields.HEADERS)
	public ImportSourceV2 setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
		return this;
	}

	@JsonGetter(Fields.HEADERS)
	public Map<String, String> headers()
	{
		return headers;
	}

	@JsonIgnore
	public ImportSourceV2 setHeader(String name, String value)
	{
		if (headers == null)
			headers = new LinkedHashMap<>();

		headers.put(name, value);
		return this;
	}


	private static final class Fields
	{
		public static final String URL     = "url";
		public static final String HEADERS = "headers";
	}
}
