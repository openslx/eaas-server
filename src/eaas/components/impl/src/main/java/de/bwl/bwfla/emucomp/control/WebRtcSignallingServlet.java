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

package de.bwl.bwfla.emucomp.control;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.control.connectors.AudioConnector;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;
import de.bwl.bwfla.emucomp.xpra.IAudioStreamer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


// Note that this servlet does not have any URL pattern (neither in web.xml)
// The dispatching to this servlet is done in the FilterDispatcher
@WebServlet(name = WebRtcSignallingServlet.SERVLET_NAME)
public class WebRtcSignallingServlet extends HttpServlet
{
    public static final String SERVLET_NAME = "WebRtcSignallingServlet";

	/** Protocol ID, that must be present in request's URL */
	private static final String PROTOCOL_SUFFIX = "/" + AudioConnector.PROTOCOL;

	/** Length of the Protocol ID */
	private static final int PROTOCOL_SUFFIX_LENGTH = PROTOCOL_SUFFIX.length();

	/** Start offset of a component ID in the request's URL */
	private static final int COMPONENT_ID_OFFSET = "/components/".length();

	/** Logger instance. */
	private final Logger log = Logger.getLogger(SERVLET_NAME);

	@Inject
    protected NodeManager nodeManager;


	protected String getComponentId(HttpServletRequest request) throws WebRtcSignallingException
	{
		// Parse the request's path, that should contain the session's ID
		final String path = request.getPathInfo();
		if (path == null || !path.endsWith(PROTOCOL_SUFFIX))
			throw new WebRtcSignallingException(HttpServletResponse.SC_BAD_REQUEST, "Wrong servlet requested!");

		final int soffset = COMPONENT_ID_OFFSET;
		final int eoffset = path.length() - PROTOCOL_SUFFIX_LENGTH;
		final String componentId = path.substring(soffset, eoffset);
		if (componentId.isEmpty())
			throw new WebRtcSignallingException(HttpServletResponse.SC_BAD_REQUEST, "Component ID is missing in request!");

		return componentId;
	}

	protected AudioConnector getAudioConnector(String componentId) throws WebRtcSignallingException
	{
		try {
			AbstractEaasComponent component = nodeManager.getComponentById(componentId, AbstractEaasComponent.class);
			IConnector connector = component.getControlConnector(AudioConnector.PROTOCOL);
			if (!(connector instanceof AudioConnector)) {
				String message = "No AudioConnector found for component '" + componentId + "'!";
				throw new WebRtcSignallingException(HttpServletResponse.SC_NOT_FOUND, message);
			}

			return (AudioConnector) connector;
		}
		catch (BWFLAException error) {
			final String message = "No component found with ID " + componentId;
			throw new WebRtcSignallingException(HttpServletResponse.SC_NOT_FOUND, message, error);
		}
	}


	/* =============== HttpServlet Implementation =============== */

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try {
			final String compid = this.getComponentId(request);
			final AudioConnector connector = this.getAudioConnector(compid);
			final IAudioStreamer streamer = connector.getAudioStreamer();
			if (streamer == null) {
				final String message = "No AudioStreamer found for component " + compid + "!";
				throw new WebRtcSignallingException(HttpServletResponse.SC_NOT_FOUND, message);
			}

			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Cache-Control", "no-cache");

			final String message = streamer.pollServerControlMessage(30, TimeUnit.SECONDS);
			if (message == null) {
				response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				return;
			}
			response.getWriter().write(message);
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Forwarding S2C control-message failed!", error);
			final int httpcode = (error instanceof WebRtcSignallingException) ?
					((WebRtcSignallingException) error).getHttpCode() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

			response.sendError(httpcode, error.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try {
			final String compid = this.getComponentId(request);
			final AudioConnector connector = this.getAudioConnector(compid);
			final String query = request.getQueryString();
			if (query != null && query.equals("connect")) {
				log.info("New audio stream was requested for component " + compid);
				connector.newAudioStreamer()
						.play();

				response.addHeader("Access-Control-Allow-Origin", "*");
				response.setStatus(HttpServletResponse.SC_OK);
				return;
			}

			final char[] buffer = new char[request.getContentLength()];
			final int length = request.getReader()
					.read(buffer);

			if (length != buffer.length)
				throw new IOException("Reading payload failed! Expected " + buffer.length + " bytes, received " + length);

			final IAudioStreamer streamer = connector.getAudioStreamer();
			if (streamer == null) {
				final String message = "No AudioStreamer found for component " + compid + "!";
				throw new WebRtcSignallingException(HttpServletResponse.SC_NOT_FOUND, message);
			}

			streamer.postClientControlMessage(buffer);

			response.addHeader("Access-Control-Allow-Origin", "*");
			response.setStatus(HttpServletResponse.SC_OK);
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Forwarding C2S control-message failed!", error);
			final int httpcode = (error instanceof WebRtcSignallingException) ?
					((WebRtcSignallingException) error).getHttpCode() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

			response.sendError(httpcode, error.getMessage());
		}
	}


	private static class WebRtcSignallingException extends ServletException
	{
		private final int code;


		public WebRtcSignallingException(int code, String message)
		{
			super(message);

			this.code = code;
		}

		public WebRtcSignallingException(int code, String message, Throwable cause)
		{
			super(message, cause);

			this.code = code;
		}

		public int getHttpCode()
		{
			return code;
		}
	}
}
