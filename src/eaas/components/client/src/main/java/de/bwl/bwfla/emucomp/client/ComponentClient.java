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

package de.bwl.bwfla.emucomp.client;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

import de.bwl.bwfla.api.emucomp.*;
import de.bwl.bwfla.api.emucomp.GetControlUrlsResponse.Return;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.AbstractServiceClient;

@ApplicationScoped
public class ComponentClient extends AbstractServiceClient<ComponentService> {
    // TODO: should only be "/ComponentProxy?wsdl", because this is the public
    //       interface of a web service
    final private String WSDL_URL_TEMPLATE = "%s/eaas/ComponentProxy?wsdl";

    public static ComponentClient get() {
        return (ComponentClient)CDI.current().select(ComponentClient.class).get();
    }
    
    @Override
    protected ComponentService createService(URL url) {
        return new ComponentService(url);
    }

    @Override
    protected String getWsdlUrl(String host) {
        return String.format(WSDL_URL_TEMPLATE, host);
    }
    
    public Component getComponentPort(String host) throws BWFLAException {
        return getPort(host, Component.class);
    }

    public Machine getMachinePort(String host) throws BWFLAException {
        return getPort(host, Machine.class);
    }
    public Container getContainerPort(String host) throws BWFLAException {
        return getPort(host, Container.class);
    }

    public NetworkSwitch getNetworkSwitchPort(String host)
            throws BWFLAException {
        return getPort(host, NetworkSwitch.class);
    }
    
    public static Map<String, URI> controlUrlsToMap(Return value) {
        return value.getEntry().stream().collect(
                Collectors.toMap(e -> e.getKey(), e -> URI.create(e.getValue())));
    }
}
