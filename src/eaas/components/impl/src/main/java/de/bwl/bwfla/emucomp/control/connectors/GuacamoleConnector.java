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

package de.bwl.bwfla.emucomp.control.connectors;

import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glyptodon.guacamole.GuacamoleClientException;
import org.glyptodon.guacamole.GuacamoleServerException;

import de.bwl.bwfla.common.services.guacplay.net.GuacTunnel;


public class GuacamoleConnector implements IConnector
{
	public final static String PROTOCOL = "guacamole";

	private final IThrowingSupplier<GuacTunnel> tunnelConstructor;
	private GuacTunnel tunnel;
	private final boolean pointerLock;

	public GuacamoleConnector(IThrowingSupplier<GuacTunnel> tunnelConstructor, boolean pointerLock)
	{
		this.tunnelConstructor = tunnelConstructor;
		this.tunnel = null;
		this.pointerLock = pointerLock;
	}

	public GuacTunnel newTunnel() throws GuacamoleClientException, GuacamoleServerException
	{
		if (tunnel != null && tunnel.isOpen()) {
			try {
				tunnel.disconnect();
				tunnel.close();
			}
			catch (Exception error) {
				Logger log = Logger.getLogger(this.getClass().getName());
				log.log(Level.WARNING, "Closing tunnel failed!", error);
			}
		}

		try {
			tunnel = tunnelConstructor.get();
			return tunnel;
		}
		catch (Exception error) {
			throw new GuacamoleServerException("Constructing tunnel failed!", error);
		}
	}
	
	public GuacTunnel getTunnel()
	{
		return tunnel;
	}

    private String createFragment(SimpleEntry<?, ?>... entries) {
        return "#" + Stream.of(entries)
                .map((entry) -> entry.getKey().toString() + "=" + entry.getValue().toString())
                .collect(Collectors.joining("&"));
    }

    @Override
    public URI getControlPath(final URI resourcePath) {
		final SimpleEntry<String, Boolean> entry = new SimpleEntry<>("pointerLock", this.pointerLock);
        return resourcePath.resolve(GuacamoleConnector.PROTOCOL)
				.resolve(this.createFragment(entry));
    }

	@Override
	public String getProtocol()
	{
		return GuacamoleConnector.PROTOCOL;
	}
}
