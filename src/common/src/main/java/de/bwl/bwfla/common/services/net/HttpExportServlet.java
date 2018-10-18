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
import de.bwl.bwfla.common.utils.FileRangeIterator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class HttpExportServlet extends HttpServlet
{
	public abstract File resolveRequest(String reqStr);
	
	@Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		this.respond(request, response, false);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		this.respond(request, response, true);
	}

	private void respond(HttpServletRequest request, HttpServletResponse response, boolean sendFileData)
			throws ServletException, IOException
	{
		final File file = this.resolveRequest(request.getPathInfo());
		if (file == null || !file.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// We assume here, that we always export a block-device image!
		final long length = HttpUtils.computeBlockDeviceLength(file.length());

		try {
			if (HttpUtils.hasRangeHeader(request)) {
				final List<ByteRange> ranges = HttpUtils.parseRangeHeader(request, length);
				HttpUtils.prepare(response, ranges, length);
				if (!sendFileData)
					return;

				// Send file's ranges
				try (final FileRangeIterator channels = new FileRangeIterator(file.toPath(), ranges)) {
					final ServletOutputStream output = response.getOutputStream();
					HttpUtils.write(output, channels, length, ranges.size() > 1);
				}
			}
			else {
				HttpUtils.prepare(response, file.getName(), length);
				if (!sendFileData)
					return;

				// Send complete file
				final ServletOutputStream output = response.getOutputStream();
				final long written = Files.copy(file.toPath(), output);

				final int padSize = (int)(length - written);
				if(padSize > 0) {
					byte[] bytes = new byte[padSize];
					Arrays.fill(bytes, (byte) 0);
					output.write(bytes);
				}
			}
		}
		catch (Exception error) {
			final Logger log = Logger.getLogger(this.getClass().getName());
			log.log(Level.WARNING, "Writing HTTP response failed!\n", error);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
