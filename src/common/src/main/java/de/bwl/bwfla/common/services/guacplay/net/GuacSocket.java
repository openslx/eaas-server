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

package de.bwl.bwfla.common.services.guacplay.net;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;


/** This class provides read/write access to a Guacamole connection. */
public class GuacSocket implements GuacamoleSocket
{
	/** The number of milliseconds to wait for data on the TCP socket. */
	//private static final int SOCKET_TIMEOUT = 15000;
	private static final int SOCKET_TIMEOUT = 30000;
	
	private final Socket socket;
	private final GuacReader reader;
	private final GuacWriter writer;
	
	
	/** Constructor */
	public GuacSocket(String hostname, int port, IGuacInterceptor interceptor) throws GuacamoleException
	{
		final Logger log = LoggerFactory.getLogger(GuacSocket.class);
		log.info("Connecting to guacd at {}:{}.", hostname, port);
		
		try {
			// Create addresses
			final InetAddress ipaddr = InetAddress.getByName(hostname);
			final InetSocketAddress sockaddr = new InetSocketAddress(ipaddr, port);
			
			// Create socket and connect
			this.socket = new Socket();
			socket.connect(sockaddr, SOCKET_TIMEOUT);
			socket.setSoTimeout(SOCKET_TIMEOUT);
		
			// On successful connect, retrieve I/O streams
			final String charset = "UTF-8";
			final InputStreamReader inpstream = new InputStreamReader(socket.getInputStream(), charset);
			final OutputStreamWriter outstream = new OutputStreamWriter(socket.getOutputStream(), charset);
			
			// Construct the reader and writer for the socket
			this.reader = new GuacReader(inpstream, interceptor);
			this.writer = new GuacWriter(outstream, interceptor);
			
			// The session begins before handshaking!
			if (interceptor != null)
				interceptor.onBeginConnection();
		}
		catch (Exception exception) {
			// Something gone wrong, rethrow
			throw new GuacamoleServerException(exception);
		}
	}
	
	/** Perform the handshaking according to the Guacamole protocol. */
	public void performGuacamoleHandshake(GuacamoleConfiguration config, GuacamoleClientInformation info) throws GuacamoleException
	{
		final Logger log = LoggerFactory.getLogger(GuacSocket.class);
		log.info("Performing the Guacamole's handshake...");
		
		// The constructor of ConfiguredGuacamoleSocket performs the actual handshaking!
		@SuppressWarnings("unused")
		GuacamoleSocket dummy = new ConfiguredGuacamoleSocket(this, config, info);
	}
	
	@Override
	public GuacamoleReader getReader()
	{
		return reader;
	}

	@Override
	public GuacamoleWriter getWriter()
	{
		return writer;
	}
	
	@Override
	public void close() throws GuacamoleException
	{
		final Logger log = LoggerFactory.getLogger(GuacSocket.class);
		log.info("Closing socket to guacd.");
		
		// Signal the session end first!
		IGuacInterceptor interceptor = reader.getInterceptor();
		try {
			if (interceptor != null)
				interceptor.onEndConnection();
		
			// Close the socket
			socket.close();
		}
		catch (Exception exception) {
			// Something gone wrong, rethrow
			if (exception instanceof GuacamoleException)
				throw (GuacamoleException) exception;
			else throw new GuacamoleServerException(exception);
		}
	}

	@Override
	public boolean isOpen()
	{
		return !socket.isClosed();
	}
}
