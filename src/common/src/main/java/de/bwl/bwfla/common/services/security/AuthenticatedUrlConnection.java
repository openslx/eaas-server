package de.bwl.bwfla.common.services.security;

import org.apache.tamaya.ConfigurationProvider;
import org.eclipse.persistence.internal.oxm.conversion.Base64;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class AuthenticatedUrlConnection {

    private static String apiKey = ConfigurationProvider.getConfiguration().get("ws.apikey");
    private static String authProxy = ConfigurationProvider.getConfiguration().get("emucomp.image_proxy");

    private static String getProxyHost() {
        return authProxy.split(":")[0];
    }

    private static int getProxyPort()
    {
        return Integer.parseInt(authProxy.split(":")[1]);
    }

    public static HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection connection = null;
        if(apiKey != null && authProxy!= null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyHost(), getProxyPort()));
            connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setRequestProperty("Proxy-Authorization", MachineTokenProvider.getProxyAuthenticationHeader());

            return connection;

        } else {
           return (HttpURLConnection) url.openConnection();
        }
    }
}
