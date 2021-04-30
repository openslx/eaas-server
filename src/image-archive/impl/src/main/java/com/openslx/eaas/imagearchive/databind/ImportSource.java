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

package com.openslx.eaas.imagearchive.databind;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.LinkedHashMap;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportSource
{
	private String url;
	private Map<String, String> headers;

	@JsonSetter(Fields.URL)
	public ImportSource setUrl(String url)
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
	public ImportSource setHeaders(Map<String, String> headers)
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
	public ImportSource setHeader(String name, String value)
	{
		if (headers == null)
			headers = new LinkedHashMap<>();

		headers.put(name, value);
		return this;
	}

	@JsonIgnore
	public void validate() throws IllegalArgumentException
	{
		if (url == null || url.isEmpty())
			throw new IllegalArgumentException("URL is invalid!");

		if (!(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:/")))
			throw new IllegalArgumentException("Source is not supported!");
	}


	private static final class Fields
	{
		public static final String URL     = "url";
		public static final String HEADERS = "hdr";
	}
}
