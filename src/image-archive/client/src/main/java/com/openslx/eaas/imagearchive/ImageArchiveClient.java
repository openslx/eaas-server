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

package com.openslx.eaas.imagearchive;


import de.bwl.bwfla.common.services.security.MachineTokenProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;


public class ImageArchiveClient implements Closeable
{
	private final ImageArchive archive;


	public ImageArchive instance()
	{
		return archive;
	}

	@Override
	public void close() throws IOException
	{
		// NOTE: according to docs, casting should be safe!
		((Closeable) archive).close();
	}

	public static ImageArchiveClient create() throws Exception
	{
		return ImageArchiveClient.create("http://localhost:8080/image-archive");
	}

	public static ImageArchiveClient create(String endpoint) throws Exception
	{
		final var archive = RestClientBuilder.newBuilder()
				.baseUrl(new URL(endpoint))
				.register(new AuthFilter())
				.build(ImageArchive.class);

		return new ImageArchiveClient(archive);
	}


	// ===== Internal Helpers ===============

	private ImageArchiveClient(ImageArchive archive)
	{
		this.archive = archive;
	}

	private static class AuthFilter implements ClientRequestFilter
	{
		@Override
		public void filter(ClientRequestContext context) throws IOException
		{
			context.getHeaders()
					.add(HttpHeaders.AUTHORIZATION, MachineTokenProvider.getInternalApiToken());
		}
	}
}
