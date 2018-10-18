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

import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;


/** A custom {@link GuacTunnel} for connections between the client and the player. */
public class PlayerTunnel extends GuacTunnel
{
	/** Constructor. */
	public PlayerTunnel(GuacTunnel tunnel, ICharArrayConsumer output, int msgBufferCapacity)
	{
		super(new PlayerSocket(tunnel.getGuacReader(), tunnel.getGuacWriter(), output, msgBufferCapacity));
	}

	/** Enable writing through this tunnel. */
	public void enableWriting()
	{
		// Obtain exclusive access!
		this.acquireWriter();
		
		// Update mode and release
		PlayerSocket socket = (PlayerSocket) this.getSocket();
		socket.enableWriting();
		this.releaseWriter();
	}
	
	/** Disable writing through this tunnel. */
	public void disableWriting()
	{
		// Obtain exclusive access!
		this.acquireWriter();
		
		// Update mode and release
		PlayerSocket socket = (PlayerSocket) this.getSocket();
		socket.disableWriting();
		this.releaseWriter();
	}
}
