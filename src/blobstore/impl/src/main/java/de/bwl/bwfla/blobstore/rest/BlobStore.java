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

package de.bwl.bwfla.blobstore.rest;

import de.bwl.bwfla.blobstore.BlobStoreBackend;
import de.bwl.bwfla.blobstore.api.Blob;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.net.HttpUtils;
import de.bwl.bwfla.common.utils.ByteRange;
import de.bwl.bwfla.common.utils.ByteRangeIterator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
@Path("/api/v1")
public class BlobStore
{
	private final Logger log = Logger.getLogger(BlobStore.class.getName());

	@Inject
	private BlobStoreBackend backend;


	@HEAD
	@GET
	@Path("/blobs/{namespace}/{id}")
	public Response getBlob(@PathParam("namespace") String namespace,
						@PathParam("id") String id,
						@Context HttpServletRequest request,
						@Context HttpServletResponse response)
	{
		return this.respond(namespace, id, request, response, false);
	}

	@HEAD
	@GET
	@Path("/blkdev/{namespace}/{id}")
	public Response getBlkdev(@PathParam("namespace") String namespace,
						  @PathParam("id") String id,
						  @Context HttpServletRequest request,
						  @Context HttpServletResponse response)
	{
		return this.respond(namespace, id, request, response, true);
	}


	/* ==================== Internal Helpers ==================== */

	private static BlobHandle toBlobHandle(HttpServletRequest request, String namespace, String id)
			throws BadRequestException
	{
		try {
			final String accessToken = request.getParameter("access_token");
			return new BlobHandle(namespace, id, (accessToken != null) ? accessToken : Blob.DEFAULT_ACCESSTOKEN);
		}
		catch (Throwable error) {
			final Response result = Response.status(Status.BAD_REQUEST)
					.header("Access-Control-Allow-Origin", "*")
					.build();

			throw new BadRequestException(result, error);
		}
	}

	private Response respond(String namespace, String id, HttpServletRequest request, HttpServletResponse response, boolean padded)
	{
		try {
			final BlobHandle handle = BlobStore.toBlobHandle(request, namespace, id);
			final Blob blob = backend.get(handle);

			String filename = ((blob.hasName()) ? blob.getName() : "output");
			if(blob.getType() != null)
					filename += blob.getType();
			final long length = (padded) ? HttpUtils.computeBlockDeviceLength(blob.getSize()) : blob.getSize();
			final boolean sendBlobData = request.getMethod().contentEquals("GET");

			try {
				if (HttpUtils.hasRangeHeader(request)) {
					final List<ByteRange> ranges = HttpUtils.parseRangeHeader(request, length);
					HttpUtils.prepare(response, ranges, length);
					if (sendBlobData) {
						// Send blob's ranges
						try (final ByteRangeIterator channels = backend.get(blob, ranges)) {
							final ServletOutputStream output = response.getOutputStream();
							HttpUtils.write(output, channels, length, ranges.size() > 1);
						}
					}
				}
				else {
					HttpUtils.prepare(response, filename, length);
					if (sendBlobData) {
						// Send complete blob
						final ServletOutputStream output = response.getOutputStream();
						blob.getData().writeTo(output);

						if(padded) {
							int padSize = (int) (length - blob.getSize());
							if (padSize > 0)
							{
								byte[] bytes = new byte[padSize];
								Arrays.fill(bytes, (byte) 0);
								output.write(bytes);
							}
						}

						// And then remove the blob
//						try {
//							backend.delete(handle);
//						}
//						catch (Exception error) {
//							log.log(Level.WARNING, "Deleting blob failed!", error);
//						}
					}
				}
			}
			catch (Exception error) {
				throw new InternalServerErrorException("Sending blob failed!", error);
			}
		}
		catch (BWFLAException error) {
			final Response result = Response.status(Status.NOT_FOUND)
					.build();

			throw new NotFoundException(result, error);
		}

		return Response.status(Status.OK)
				.build();
	}
}
