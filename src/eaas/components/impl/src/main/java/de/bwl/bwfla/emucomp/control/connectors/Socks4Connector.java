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

public class Socks4Connector implements IConnector {
    public final static String PROTOCOL = "socks4";

    private final int port;
    
    public Socks4Connector(int port) {
        super();
        this.port = port;
    }

    @Override
    public URI getControlPath(URI componentResource) {
        // the URI's host part does not matter here, it is overriden by the
        // proxy
        return URI.create("socks4://host:" + this.port);
    }

    @Override
    public String getProtocol() {
        return Socks4Connector.PROTOCOL;
    }
}
