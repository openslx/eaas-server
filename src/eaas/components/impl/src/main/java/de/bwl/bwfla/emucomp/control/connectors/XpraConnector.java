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
import java.nio.file.Path;


public class XpraConnector implements IConnector
{
    public final static String PROTOCOL = "xpra";

    private final Path iosock;
    private Runnable disconnector;

    public XpraConnector(Path iosock) {
        this.iosock = iosock;
    }

    @Override
    public URI getControlPath(final URI componentResource) {
        return componentResource.resolve(XpraConnector.PROTOCOL);
    }

    @Override
    public String getProtocol() {
        return XpraConnector.PROTOCOL;
    }

    public Path getIoSocketPath() {
        return iosock;
    }

    public void setDisconnectHandler(Runnable handler) {
        this.disconnector = handler;
    }

    public void disconnect() {
        if (disconnector != null)
            disconnector.run();
    }
}
