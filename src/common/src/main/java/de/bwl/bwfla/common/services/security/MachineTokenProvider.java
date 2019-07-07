package de.bwl.bwfla.common.services.security;

import org.apache.tamaya.ConfigurationProvider;
import org.eclipse.persistence.internal.oxm.conversion.Base64;

public class MachineTokenProvider {

    private static final String apiKey = ConfigurationProvider.getConfiguration().get("ws.apikey");
    private static final String authProxy = ConfigurationProvider.getConfiguration().get("emucomp.image_proxy");

    public static String getApiKey()
    {
        if(apiKey != null && (apiKey.isEmpty() || apiKey.equals("null")))
            return null;
        return apiKey;
    }

    public static String getAuthProxy()
    {
        if(authProxy != null && (authProxy.isEmpty() || authProxy.equals("null")))
            return null;
        return authProxy;
    }

    public static String getAuthenticationProxy()
    {
        if( getApiKey() != null && getAuthProxy() != null)
            return "http://jwt:" + getApiKey() + "@" + getAuthProxy();
        else
            return null;
    }

    public static String getProxyAuthenticationHeader()
    {
        if(getApiKey() != null)
            return "Basic " + new String(Base64.base64Encode(("jwt:" + getApiKey()).getBytes()));
        return null;
    }

    public static SOAPClientAuthenticationHandlerResolver getSoapAuthenticationResolver()
    {
        if(getApiKey() != null)
            return new SOAPClientAuthenticationHandlerResolver(getApiKey());
        else
            return null;
    }
}
