/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.resolver;

import de.bwl.bwfla.common.services.security.UserContext;
import org.apache.tamaya.ConfigurationProvider;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class DataResolver
{
	protected final String endpoint;

	public DataResolver(String endpoint)
	{
		if (endpoint == null || endpoint.isEmpty())
			throw new IllegalArgumentException();

		if (endpoint.endsWith("/"))
			endpoint = endpoint.substring(0, endpoint.length() - 1);

		this.endpoint = endpoint;
	}

	protected String resolve(UserContext userctx, String... subpaths)
	{
		final var location = new StringBuilder(4 * 1024)
				.append(endpoint);

		if (userctx != null && userctx.getToken() != null) {
			location.append("/t/")
					.append(userctx.getToken());
		}

		for (final var subpath : subpaths) {
			final var encpath = DataResolver.encode(subpath);
			location.append("/")
					.append(encpath);
		}

		return location.toString();
	}

	public static String encode(String path)
	{
		return URLEncoder.encode(path, StandardCharsets.UTF_8);
	}

	public static String decode(String path)
	{
		return URLDecoder.decode(path, StandardCharsets.UTF_8);
	}

	public static String getDefaultEndpoint()
	{
		return ConfigurationProvider.getConfiguration()
				.get("emucomp.resolver");
	}

	public static boolean isRelativeUrl(String url)
	{
		return !DataResolver.isAbsoluteUrl(url);
	}

	public static boolean isAbsoluteUrl(String url)
	{
		if (url == null)
			throw new IllegalArgumentException();

		return url.startsWith("http:") || url.startsWith("https:") || url.startsWith("file:");
	}
}
