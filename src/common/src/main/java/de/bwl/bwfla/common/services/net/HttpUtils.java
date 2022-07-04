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

package de.bwl.bwfla.common.services.net;


import de.bwl.bwfla.common.utils.ByteRange;
import de.bwl.bwfla.common.utils.ByteRangeChannel;
import de.bwl.bwfla.common.utils.ByteRangeIterator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class HttpUtils
{
	public static final long DEFAULT_BLOCK_SIZE = 512;

	public static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

	private static final String MULTIPART_BOUNDARY = "MULTIPART-BYTE-RANGE";


	/** Returns true if given URL is relative */
	public static boolean isRelativeUrl(String url)
	{
		return !HttpUtils.isAbsoluteUrl(url);
	}

	/** Returns true if given URL is absolute */
	public static boolean isAbsoluteUrl(String url)
	{
		if (url == null)
			throw new IllegalArgumentException();

		return url.startsWith("http:") || url.startsWith("https:");
	}

	/** Returns true if the request has a Range header, else false */
	public static boolean hasRangeHeader(HttpServletRequest request)
	{
		final String header = request.getHeader("Range");
		return (header != null && !header.isEmpty());
	}

	/** Compute, possibly padded, length for raw block-device images */
	public static long computeBlockDeviceLength(long length)
	{
		final long padding = length % DEFAULT_BLOCK_SIZE;
		if (padding != 0L)
			length += DEFAULT_BLOCK_SIZE - padding;

		return length;
	}

	/** Parses a range request header and return a list of byte-ranges */
	public static List<ByteRange> parseRangeHeader(HttpServletRequest request, long length)
	{
		final String header = request.getHeader("Range");
		if (header == null || header.isEmpty())
			throw new IllegalArgumentException("Invalid range header: " + header);

		// Range header must match format "bytes=n-n,n-n,n-n..."
		if (!header.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$"))
			throw new IllegalArgumentException("Invalid range header: " + header);

		final List<ByteRange> ranges = new ArrayList<ByteRange>();

		// Parse ranges according to https://tools.ietf.org/html/rfc7233
		for (String range : header.substring(6).split(",")) {
			final int splitIndex = range.indexOf("-");
			long start = HttpUtils.parseOffset(range, 0, splitIndex);
			long end = HttpUtils.parseOffset(range, splitIndex + 1, range.length());
			if (start < 0) {
				// Case: bytes=-X (last X bytes)
				start = length - end;
				end = length - 1;
			}
			else if (end < 0 || end >= length) {
				// Case: bytes=X- (last length-X bytes)
				end = length - 1;
			}

			// Safety check!
			if (start > end)
				throw new IllegalArgumentException("Invalid range header: " + header);

			ranges.add(new ByteRange(start, end - start + 1));
		}

		return ranges;
	}

	public static void prepare(HttpServletResponse response, String filename, long length)
	{
		// Set/update response headers
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/octet-stream");
		response.setHeader("Accept-Ranges", "bytes");
		response.setHeader("Content-Length", Long.toString(length));
		response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
	}

	public static void prepare(HttpServletResponse response, List<ByteRange> ranges, long length)
	{
		if (ranges == null || ranges.isEmpty())
			throw new IllegalArgumentException("No ranges specified!");

		// Set/update response headers
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		response.setHeader("Accept-Ranges", "bytes");
		if (ranges.size() == 1) {
			// Partial content with single part
			final ByteRange range = ranges.get(0);
			final long start = range.getStartOffset();
			final long end = range.getEndOffset();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + length);
			response.setHeader("Content-Length", Long.toString(range.getLength()));
		}
		else {
			// Partial content with multiple parts
			response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
		}
	}

	public static void write(ServletOutputStream output, ByteRangeIterator ranges, long length, boolean multipart)
			throws ServletException, IOException
	{
		if (ranges == null)
			throw new IllegalArgumentException("No ranges specified!");

		final ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);

		if (multipart) {
			// Partial content with multiple parts
			while (ranges.hasNext()) {
				final ByteRangeChannel range = ranges.next();
				final long start = range.getStartOffset();
				final long end = range.getEndOffset();

				// Write multipart boundary and headers first
				output.println();
				output.println("--" + MULTIPART_BOUNDARY);
				output.println("Content-Type: application/octet-stream");
				output.println("Content-Range: bytes " + start + "-" + end + "/" + length);
				output.println();

				// Write range's data buffer
				HttpUtils.write(output, range, buffer);
			}

			// End multipart boundary
			output.println("--" + MULTIPART_BOUNDARY + "--");
		}
		else {
			// Partial content with single part
			final ByteRangeChannel range = ranges.next();
			HttpUtils.write(output, range, buffer);
		}
	}


	/* =============== Internal Helpers =============== */

	/** Parses a substring of the given value as Long */
	private static long parseOffset(String value, int beginIndex, int endIndex)
	{
		String substring = value.substring(beginIndex, endIndex);
		return (substring.length() > 0) ? Long.parseLong(substring) : -1;
	}

	private static void write(OutputStream output, ByteRangeChannel range, ByteBuffer buffer) throws IOException
	{
		while (range.hasBytesRemaining()) {
			buffer.clear();
			if (range.read(buffer) < 1)
				break;

			buffer.flip();

			// Write available data...
			final int offset = buffer.arrayOffset() + buffer.position();
			output.write(buffer.array(), offset, buffer.remaining());
		}

		// Padding needed?
		for (int padding = (int) range.getNumBytesRemaining(); padding > 0;) {
			buffer.clear();

			for (int i = Math.min(padding, buffer.remaining()); i > 0; --i)
				buffer.put((byte) 0);

			buffer.flip();

			// Write padding bytes...
			final int offset = buffer.arrayOffset() + buffer.position();
			output.write(buffer.array(), offset, buffer.remaining());
			padding -= buffer.remaining();
		}
	}
}
