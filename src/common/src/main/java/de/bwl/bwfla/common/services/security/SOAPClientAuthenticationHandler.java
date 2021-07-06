package de.bwl.bwfla.common.services.security;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.*;

public class SOAPClientAuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

    private final MachineToken authenticationToken;

    public SOAPClientAuthenticationHandler(MachineToken authenticationToken)
    {
        this.authenticationToken = authenticationToken;
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean isResponse= ((Boolean) context.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();

        if (isResponse) {
            //this is underlying http response object
            Map<String, List<String>> headers = (Map<String, List<String>>)context.get(MessageContext.HTTP_REQUEST_HEADERS);
            if (headers == null) {
                headers = new HashMap<String, List<String>>();
                context.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
            }
            headers.put("authorization", Collections.singletonList(authenticationToken.get()));
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {

    }
}
