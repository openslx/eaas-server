package de.bwl.bwfla.common.services.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.apache.tamaya.ConfigurationProvider;
import org.eclipse.persistence.internal.oxm.conversion.Base64;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Date;
import java.util.function.Function;


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

       final var lifetime = MachineTokenProvider.getDefaultLifetime();
       try {
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            String token = JWT.create()
                    .withIssuer("eaasi")
                    .withExpiresAt(new Date(MachineTokenProvider.time() + lifetime.toMillis()))
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
        return MachineTokenProvider.getBearerToken(secret, MachineTokenProvider.getDefaultLifetime());
    }

    private static String getBearerToken(String secret, Duration lifetime)
    {
        if(secret == null)
            return null;

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("eaas")
                    .withExpiresAt(new Date(MachineTokenProvider.time() + lifetime.toMillis()))
                    .sign(algorithm);
            // System.out.println("Token:"  + token);
            return "Bearer " + token;
        } catch (JWTCreationException | UnsupportedEncodingException exception){
            exception.printStackTrace();
            return null;
        }
    }

    public static MachineToken getInternalToken()
    {
        return MachineTokenProvider.getInternalToken(MachineTokenProvider.getDefaultLifetime());
    }

    public static MachineToken getInternalToken(Duration lifetime)
    {
        final Function<Duration, String> refresher = (time) -> {
            final var secret = ConfigurationProvider.getConfiguration()
                    .get("rest.internalApiSecret");

            return MachineTokenProvider.getBearerToken(secret, time);
        };

        return new MachineToken(lifetime, refresher);
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

    public static Duration getDefaultLifetime()
    {
        return Duration.ofHours(2L);
    }

    public static long time()
    {
        return System.currentTimeMillis();
    }
}
