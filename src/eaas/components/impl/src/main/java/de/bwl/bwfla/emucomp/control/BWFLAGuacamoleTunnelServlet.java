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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glyptodon.guacamole.GuacamoleClientException;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleResourceNotFoundException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.glyptodon.guacamole.servlet.GuacamoleHTTPTunnelServlet;

import de.bwl.bwfla.common.services.guacplay.net.IGuacReader;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.control.connectors.GuacamoleConnector;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;


// Note that this servlet does not have any URL pattern (neither in web.xml)
// The dispatching to this servlet is done in the FilterDispatcher
@WebServlet(name = BWFLAGuacamoleTunnelServlet.SERVLET_NAME)
public class BWFLAGuacamoleTunnelServlet extends HttpServlet 
{
    public static final String SERVLET_NAME = "GuacamoleServlet";
    
    private static final long serialVersionUID = -612269697551642764L;

	/** Charset to use for reading requests and writing responses. */
	private static final String CHARSET = "UTF-8";
	
	/** Protocol ID, that must be present in request's URL */
	private static final String PROTOCOL_SUFFIX = "/" + GuacamoleConnector.PROTOCOL;
	
	/** Length of the Protocol ID */
	private static final int PROTOCOL_SUFFIX_LENGTH = PROTOCOL_SUFFIX.length();
	
	/** Start offset of a component ID in the request's URL */
	private static final int COMPONENT_ID_OFFSET = "/components/".length();
	
	/** Logger instance. */
	private final Logger log = Logger.getLogger("BWFLAGuacamoleTunnelServlet");

	@Inject
    protected NodeManager nodeManager;


	protected String getComponentId(HttpServletRequest request) throws GuacamoleResourceNotFoundException
	{
		// Parse the request's path, that should contain the session's ID
		final String path = request.getPathInfo();
		if (path == null || !path.endsWith(PROTOCOL_SUFFIX))
			throw new GuacamoleResourceNotFoundException("Wrong servlet requested!");

		final int soffset = COMPONENT_ID_OFFSET;
		final int eoffset = path.length() - PROTOCOL_SUFFIX_LENGTH;
		final String componentId = path.substring(soffset, eoffset);
		if (componentId.isEmpty())
			throw new GuacamoleResourceNotFoundException("Component ID is missing in request!");

		return componentId;
	}
    
	protected GuacamoleConnector getGuacamoleConnector(String componentId) throws GuacamoleResourceNotFoundException
	{
		try {
			AbstractEaasComponent component = nodeManager.getComponentById(componentId, AbstractEaasComponent.class);
			IConnector connector = component.getControlConnector(GuacamoleConnector.PROTOCOL);
			if (connector == null || !(connector instanceof GuacamoleConnector)) {
				String message = "No GuacamoleConnector found for component '" + componentId + "'!";
				throw new GuacamoleResourceNotFoundException(message);
			}

			return (GuacamoleConnector) connector;
		}
		catch (Exception e) {
			throw new GuacamoleResourceNotFoundException("No eaas component found with ID " + componentId, e);
		}
	}
	
	protected GuacamoleTunnel getGuacamoleTunnel(HttpServletRequest request) throws GuacamoleResourceNotFoundException
    {
    	final String componentId = this.getComponentId(request);
		return this.getGuacamoleTunnel(componentId);
    }
	
	protected GuacamoleTunnel getGuacamoleTunnel(String componentId) throws GuacamoleResourceNotFoundException
    {
		final GuacamoleConnector connector = this.getGuacamoleConnector(componentId);
		final GuacamoleTunnel tunnel = connector.getTunnel();
		if (tunnel == null)
			throw new GuacamoleResourceNotFoundException("No tunnel found for component '" + componentId + "'!");
		
		return tunnel;
    }
	
	protected GuacamoleTunnel newGuacamoleTunnel(String componentId)
			throws GuacamoleResourceNotFoundException, GuacamoleClientException, GuacamoleServerException
    {
		return this.getGuacamoleConnector(componentId)
				.newTunnel();
    }
	
	protected void handleTunnelRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		response.addHeader("Access-Control-Allow-Origin", "*");

		try {
			final String query = request.getQueryString();
			if (query == null)
				throw new GuacamoleClientException("No query string provided.");

			if (query.startsWith("read:"))
				this.doRead(request, response);

			else if (query.startsWith("write:"))
				this.doWrite(request, response);
			
			else if (query.equals("connect")) {
				final GuacamoleTunnel tunnel = this.doConnect(request);
				if (tunnel == null)
					throw new GuacamoleResourceNotFoundException("No tunnel created.");

				try {
					// Send UUID to client
					response.setContentType("text/plain");
					response.setHeader("Cache-Control", "no-cache");
					response.getWriter().print(tunnel.getUUID().toString());
				}
				catch (IOException error) {
					throw new GuacamoleServerException(error);
				}
			}

			// Otherwise, invalid operation
			else throw new GuacamoleClientException("Invalid tunnel operation: " + query);
		}

		// Catch any thrown guacamole exception and attempt to pass within the
		// HTTP response, logging each error appropriately.
		catch (GuacamoleClientException error) {
			log.warning("HTTP tunnel request rejected: " + error.getMessage());
			GuacamoleHTTPTunnelServlet.sendError(response, error.getStatus(), error.getMessage());
		}
		catch (GuacamoleException error) {
			log.severe("HTTP tunnel request failed: " + error.getMessage());
			log.info("Internal error in HTTP tunnel. " +  error);
			GuacamoleHTTPTunnelServlet.sendError(response, error.getStatus(), "Internal server error.");
		}
	}

	protected GuacamoleTunnel doConnect(HttpServletRequest request)
			throws GuacamoleClientException, GuacamoleServerException
	{
		// Lookup the corresponding tunnel
		final String componentId = this.getComponentId(request);
		final GuacamoleTunnel tunnel = this.newGuacamoleTunnel(componentId);
		final String tunnelId = tunnel.getUUID().toString().toUpperCase();
		log.info("Using tunnel '" + tunnelId + "' for component '" + componentId + "'");
		return tunnel;
	}
	
	protected void doWrite(HttpServletRequest request, HttpServletResponse response) throws GuacamoleException
	{
		// NOTE: This code is mostly copied from the GuacamoleHTTPTunnelServlet.doWrite(...) method!
		//       Important changes are marked appropriately.
		
		final GuacamoleTunnel tunnel = this.getGuacamoleTunnel(request);
		if (!tunnel.isOpen())
			throw new GuacamoleServerException("Tunnel is closed.");

		// We still need to set the content type to avoid the default of text/html, as
		// such a content type would cause some browsers to attempt to parse the result,
		// even though the JavaScript client does not explicitly request such parsing.
		response.setContentType("application/octet-stream");
		response.setHeader("Cache-Control", "no-cache");
		response.setContentLength(0);

		// Transfer data from request to tunnel-output, ensuring stream is always closed
		try {
			// Get input-reader for HTTP-stream and writer for the tunnel
			final GuacamoleWriter writer = tunnel.acquireWriter();
			final Reader input = new InputStreamReader(request.getInputStream(), CHARSET);
			try {
				// IMPORTANT CHANGES:
				//    - The data's length is known in the request, no need to allocate the 8kB buffers.
				//    - Furthermore the data can be written in one shot, no need for a loop.

				int numReadCalls = 0;

				// Allocate a new buffer and fill it with data.
				final int expLength = request.getContentLength();
				final char[] buffer = new char[expLength];
				int curLength = 0;
				do {
					final int remaining = expLength - curLength;
					final int numBytesRead = input.read(buffer, curLength, remaining);
					if (numBytesRead < 0)
						break;
					
					curLength += numBytesRead;
					++numReadCalls;
					
				} while (curLength < expLength);
				
				// Transfer data using the buffer
				if (tunnel.isOpen() && (curLength > 0))
					writer.write(buffer, 0, curLength);
				
				if (numReadCalls > 1) {
					String msg = "Client's HTTP-WriteRequest for tunnel " + tunnel.getUUID() + " required " + numReadCalls + " reads/writes.";
					log.info(msg);
				}
			}

			// Close input-stream in all cases
			finally {
				input.close();
			}
		}
		catch (Exception exception) {
			// Rethrow the exception
			if (exception instanceof GuacamoleException)
				throw (GuacamoleException) exception;
			else {
				String message = "I/O-Error sending data to server: " + exception.getMessage();
				throw new GuacamoleServerException(message, exception);
			}
		}
		
		// Release the tunnel-writer for other threads
		finally {
			tunnel.releaseWriter();
		}
	}
	
	protected void doRead(HttpServletRequest request, HttpServletResponse response) throws GuacamoleException
	{
		// NOTE: This code is mostly copied from the GuacamoleHTTPTunnelServlet.doRead(...) method!
		//       Important changes are marked apropriately.
		
		final GuacamoleTunnel tunnel = this.getGuacamoleTunnel(request);
		if (!tunnel.isOpen())
			throw new GuacamoleServerException("Tunnel is closed.");

		// Note that although we are sending text, Webkit browsers will buffer 1024 bytes
		// before starting a normal stream if we use anything but application/octet-stream.
		response.setContentType("application/octet-stream");
		response.setHeader("Cache-Control", "no-cache");
		
		// IMPORTANT CHANGES:
		//    - BufferedWriter is not needed for writing into the OutputStream in this case!
		//    - Reading through the interceptor implemented in a more efficient way.
		//    - Correct handling of buffer flushing.
		
		// Transfer data from tunnel-input to response, ensuring stream is always closed
		try {
			// Obtain exclusive read access to the tunnel and get writer for response
			final IGuacReader reader = (IGuacReader) tunnel.acquireReader();
			final Writer output = new OutputStreamWriter(response.getOutputStream(), CHARSET);
			try {
				if (!reader.readInto(output))
					throw new GuacamoleServerException("End of stream reached!");
				
				// Send all pending messages, until there are other threads waiting
				while (tunnel.isOpen()) {
					// Stop, if we expect to wait
					if (!reader.available())
						break;

					// Stop, if other requests are waiting
					if (tunnel.hasQueuedReaderThreads())
						break;
					
					// Read and send next message
					if (!reader.readInto(output))
						throw new GuacamoleServerException("End of stream reached!");
				}

				// End-of-instructions marker
				output.write("0.;");
				output.flush();
			}

			// Always close output-stream
			finally {
				output.close();
			}
		}
		catch (Exception exception) {
			Throwable cause = exception.getCause();
			
			// HACK: Catch all possible SocketExceptions and show only
			//       their messages without the lengthy stacktraces.
			//       Inspect here the full chain of exceptions!
			while (cause != null) {
				if (cause instanceof SocketException) {
					log.severe("A socket-error occured! Cause: " + cause.getMessage());
					return;
				}
				
				cause = cause.getCause();
			}
			
			// Rethrow the exception
			if (exception instanceof GuacamoleException)
				throw (GuacamoleException) exception;
			else {
				String message = "I/O-Error reading data from server: " + exception.getMessage();
				throw new GuacamoleServerException(message, exception);
			}
		}
		
		// Release the tunnel-reader for other threads
		finally {
			tunnel.releaseReader();
		}
	}
	
	
	/* =============== HttpServlet Implementation =============== */
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		this.handleTunnelRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		this.handleTunnelRequest(request, response);
	}
}
