package de.bwl.bwfla.common.services.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import de.bwl.bwfla.common.services.handle.HandleClient;
import org.apache.tamaya.ConfigurationProvider;
import org.eclipse.persistence.internal.oxm.conversion.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class MachineTokenProvider {
    private static String authProxy = ConfigurationProvider.getConfiguration().get("emucomp.image_proxy");
    private static String apiSecret;

    static {
        apiSecret = ConfigurationProvider.getConfiguration().get("ws.apiSecret");
        if(apiSecret != null && (apiSecret.isEmpty() || apiSecret.equals("null")))
            apiSecret = null;
    }

    public static String getAuthProxy()
    {
        return authProxy;
    }

    static String getApiSecret()
    {
        return apiSecret;
    }

    public static String getApiKey()
    {
       if(apiSecret == null)
            return null;

       try {
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            String token = JWT.create()
                    .withIssuer("eaasi")
                    .withExpiresAt(new Date(System.currentTimeMillis() + (2 * 60 * 60 * 1000))) // 2h
                    .sign(algorithm);
           // System.out.println("Token:"  + token);
            return token;
        } catch (JWTCreationException | UnsupportedEncodingException exception){
            exception.printStackTrace();
            return null;
        }
    }

    public static String getJwt(String secret)
    {
        if(secret == null)
            return null;

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("eaas")
                    .withExpiresAt(new Date(System.currentTimeMillis() + (6 * 60 * 60 * 1000))) // 6h
                    .sign(algorithm);
            // System.out.println("Token:"  + token);
            return "Bearer " + token;
        } catch (JWTCreationException | UnsupportedEncodingException exception){
            exception.printStackTrace();
            return null;
        }
    }

    public static String getAuthenticationProxy()
    {
        if( getApiKey() != null )
            return "http://jwt:" + getApiKey() + "@" + authProxy;
        else
            if(authProxy.startsWith("http"))
                return authProxy;
            return "http://" + authProxy;
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
