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

package de.bwl.bwfla.eaas.client;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.api.eaas.EaasWSService;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.AbstractServiceClient;


@ApplicationScoped
public class EaasClient extends AbstractServiceClient<EaasWSService> {
    // TODO: should only be "/EaasWS?wsdl", because this is the public
    //       interface of a web service
    final private String WSDL_URL_TEMPLATE = "%s/eaas/EaasWS?wsdl";

    public static EaasClient get() {
        return (EaasClient)CDI.current().select(EaasClient.class).get();
    }
    
    public EaasWS getEaasWSPort(String host) throws BWFLAException {
        return getPort(host, EaasWS.class);
    }

    @Override
    protected EaasWSService createService(URL url) {
        return new EaasWSService(url);
    }

    @Override
    protected String getWsdlUrl(String host) {
        return String.format(WSDL_URL_TEMPLATE, host);
    }
}
