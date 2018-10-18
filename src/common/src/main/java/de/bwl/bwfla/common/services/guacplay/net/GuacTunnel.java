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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.net.GuacamoleTunnel;


/** @see GuacamoleTunnel */
public class GuacTunnel extends GuacamoleTunnel
{
	private String cookie;
	
	/** Factory method. */
	public static GuacTunnel construct(TunnelConfig tunconf) throws GuacamoleException
	{
		final String guachost = tunconf.getGuacdHostname();
		final int guacport = tunconf.getGuacdPort();
		
		GuacTunnel tunnel = null;

		// Construct the tunnel
		try {
			// Perform handshake first, then create the tunnel
			GuacSocket socket = new GuacSocket(guachost, guacport, tunconf.getInterceptor());
			socket.performGuacamoleHandshake(tunconf.getGuacamoleConfiguration(), tunconf.getGuacamoleClientInformation());
			tunnel = new GuacTunnel(socket);
		}
		catch (GuacamoleException exception) {
			Logger log = LoggerFactory.getLogger(GuacTunnel.class);
			log.error("Tunnel creation failed. {}", exception.toString());
			throw exception;
		}

		return tunnel;
	}

	/** Constructor. */
	protected GuacTunnel(GuacamoleSocket socket)
	{
		super(socket);

		final String tunid = this.getUUID().toString().toUpperCase();
		final Logger log = LoggerFactory.getLogger(this.getClass());
		log.info("Tunnel established (ID: {}).", tunid);
	}
	
	/** Returns the underlying socket's writer as {@link GuacWriter}. */
	public GuacWriter getGuacWriter()
	{
		return (GuacWriter) this.getSocket().getWriter();
	}

	/** Returns the underlying socket's reader as {@link GuacReader}. */
	public GuacReader getGuacReader()
	{
		return (GuacReader) this.getSocket().getReader();
	}
	
	/** Send a disconnect-instruction to the underlying socket. */
	public void disconnect() throws GuacamoleException
	{
		GuacamoleWriter writer = this.acquireWriter();
		try {
			// Try to send termination instruction
			writer.write("10.disconnect;".toCharArray());
		}
		catch (GuacamoleException exception) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.warn("Sending disconnect-instruction to guacd failed!");
		}
		finally {
			this.releaseWriter();
		}
	}
	
	@Override
	public void close() throws GuacamoleException
	{
		final String tunid = this.getUUID().toString().toUpperCase();
		final Logger log = LoggerFactory.getLogger(this.getClass());

		// The tunnel can be closed multiple times!
		if (!this.isOpen()) {
			log.warn("Attempt to close an already closed tunnel! ID: {}", tunid);
			return;
		}
		
		super.close();

		if (!(this.getSocket() instanceof GuacSocket)) {
			log.info("Tunnel closed (ID: {}).", tunid);
			return;
		}
		
		// Print tunnel's statistics to log
		final long numBytesRead = this.getGuacReader().getNumBytesRead();
		final long numBytesWritten = this.getGuacWriter().getNumBytesWritten();
		log.info("Tunnel closed (ID: {}). {} bytes read, {} bytes written.", tunid, numBytesRead, numBytesWritten);
	}

	public void setCookie(String cookie)
	{
		this.cookie = cookie;
	}
	
	public String getCookie()
	{
		return this.cookie;
	}
}
