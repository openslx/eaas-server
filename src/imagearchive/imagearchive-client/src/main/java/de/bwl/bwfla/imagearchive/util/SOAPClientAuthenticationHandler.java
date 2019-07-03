package de.bwl.bwfla.imagearchive.util;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.*;

public class SOAPClientAuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

    final String authenticationToken;

    public SOAPClientAuthenticationHandler(String authenticationToken)
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
            headers.put("authorization", Collections.singletonList("Bearer " + authenticationToken));
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
