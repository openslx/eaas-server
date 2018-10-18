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

public class WebEmulatorConnector implements IConnector {
    public final static String PROTOCOL = "webemulator";
    public String emulator;

    public WebEmulatorConnector(String emulator) {
        this.emulator = emulator;
    }

    private String createFragment(SimpleEntry<?, ?>... entries) {
        return "#" + Stream.of(entries).map((entry) -> entry.getKey().toString() + "=" + entry.getValue().toString())
                .collect(Collectors.joining("&"));
    }

    @Override
    public URI getControlPath(final URI resourcePath) {
        return resourcePath.resolve(WebEmulatorConnector.PROTOCOL)
                .resolve(createFragment(new SimpleEntry<>("emulator", emulator)));
    }

    @Override
    public String getProtocol() {
        return WebEmulatorConnector.PROTOCOL;
    }
}
