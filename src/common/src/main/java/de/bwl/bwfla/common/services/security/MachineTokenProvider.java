package de.bwl.bwfla.common.services.security;

import org.apache.tamaya.ConfigurationProvider;
import org.eclipse.persistence.internal.oxm.conversion.Base64;

public class MachineTokenProvider {

    private static final String apiKey = ConfigurationProvider.getConfiguration().get("ws.apikey");
    private static final String authProxy = ConfigurationProvider.getConfiguration().get("emucomp.image_proxy");

    public static String getAuthenticationProxy()
    {
        if(apiKey != null && !apiKey.isEmpty() && authProxy != null && !authProxy.isEmpty())
            return "http://jwt:" + apiKey + "@" + authProxy;
        else
            return null;
    }

    public static String getProxyAuthenticationHeader()
    {
        return "Basic " + new String(Base64.base64Encode(("jwt:" + apiKey).getBytes()));
    }

    public static SOAPClientAuthenticationHandlerResolver getSoapAuthenticationResolver()
    {
        if(apiKey != null && !apiKey.isEmpty())
            return new SOAPClientAuthenticationHandlerResolver(apiKey);
        else
            return null;
    }
}
