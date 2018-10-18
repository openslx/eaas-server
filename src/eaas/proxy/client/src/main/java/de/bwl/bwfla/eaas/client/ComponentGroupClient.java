package de.bwl.bwfla.eaas.client;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;

import de.bwl.bwfla.api.eaas.ComponentGroup;
import de.bwl.bwfla.api.eaas.ComponentGroupService;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.AbstractServiceClient;

@ApplicationScoped
public class ComponentGroupClient
        extends AbstractServiceClient<ComponentGroupService> {
    // TODO: should only be "/EaasWS?wsdl", because this is the public
    //       interface of a web service
    final private String WSDL_URL_TEMPLATE = "%s/eaas/ComponentGroup?wsdl";
    
    @Override
    public ComponentGroupService createService(URL url) {
        return new ComponentGroupService(url);
    }

    @Override
    public String getWsdlUrl(String host) {
        return String.format(WSDL_URL_TEMPLATE, host);
    }
    
    public ComponentGroup getComponentGroupPort(String host) throws BWFLAException {
        return getPort(host, ComponentGroup.class);
    }

}
