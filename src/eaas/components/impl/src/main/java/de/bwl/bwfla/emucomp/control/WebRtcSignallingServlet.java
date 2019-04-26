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
import de.bwl.bwfla.emucomp.control.connectors.IConnector;
import de.bwl.bwfla.emucomp.control.connectors.XpraConnector;

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
	private static final String PROTOCOL_SUFFIX = "/" + XpraConnector.PROTOCOL;

	/** Length of the Protocol ID */
	private static final int PROTOCOL_SUFFIX_LENGTH = PROTOCOL_SUFFIX.length();

	/** Start offset of a component ID in the request's URL */
	private static final int COMPONENT_ID_OFFSET = "/components/".length();

	/** Logger instance. */
	private final Logger log = Logger.getLogger(SERVLET_NAME);

	@Inject
    protected NodeManager nodeManager;


	protected String getComponentId(HttpServletRequest request) throws ServletException
	{
		// Parse the request's path, that should contain the session's ID
		final String path = request.getPathInfo();
		if (path == null || !path.endsWith(PROTOCOL_SUFFIX))
			throw new ServletException("Wrong servlet requested!");

		final int soffset = COMPONENT_ID_OFFSET;
		final int eoffset = path.length() - PROTOCOL_SUFFIX_LENGTH;
		final String componentId = path.substring(soffset, eoffset);
		if (componentId.isEmpty())
			throw new ServletException("Component ID is missing in request!");

		return componentId;
	}

	protected XpraConnector getXpraConnector(String componentId) throws ServletException
	{
		try {
			AbstractEaasComponent component = nodeManager.getComponentById(componentId, AbstractEaasComponent.class);
			IConnector connector = component.getControlConnector(XpraConnector.PROTOCOL);
			if (!(connector instanceof XpraConnector)) {
				String message = "No XpraConnector found for component '" + componentId + "'!";
				throw new ServletException(message);
			}

			return (XpraConnector) connector;
		}
		catch (BWFLAException error) {
			throw new ServletException("No component found with ID " + componentId, error);
		}
	}


	/* =============== HttpServlet Implementation =============== */

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		final String compid = this.getComponentId(request);
		final XpraConnector connector = this.getXpraConnector(compid);
		try {
			final String message = connector.getAudioStreamer()
					.pollServerControlMessage(30, TimeUnit.SECONDS);

			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Cache-Control", "no-cache");
			response.getWriter().write(message);
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Forwarding S2C control-message failed!", error);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		final String compid = this.getComponentId(request);
		final XpraConnector connector = this.getXpraConnector(compid);
		try {
			final char[] buffer = new char[request.getContentLength()];
			final int length = request.getReader()
					.read(buffer);

			if (length != buffer.length)
				throw new IOException("Reading payload failed! Expected " + buffer.length + " bytes, received " + length);

			connector.getAudioStreamer()
					.postClientControlMessage(buffer);

			response.addHeader("Access-Control-Allow-Origin", "*");
			response.setStatus(HttpServletResponse.SC_OK);
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Forwarding C2S control-message failed!", error);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
