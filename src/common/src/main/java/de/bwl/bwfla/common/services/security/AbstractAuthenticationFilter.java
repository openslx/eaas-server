package de.bwl.bwfla.common.services.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class AbstractAuthenticationFilter  implements ContainerRequestFilter {

    protected static JwkProvider provider = null;
    protected static final Logger LOG = Logger.getLogger("Authentication");

    protected void initWKProvider(String jwkUri) throws MalformedURLException {
        if (provider == null && jwkUri != null) {
            provider = new JwkProviderBuilder(new URL(jwkUri))
                    .cached(10, 24, TimeUnit.HOURS)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build();
        }
    }

    protected String extractToken(ContainerRequestContext requestContext)
    {
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        String token = null;

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring("Bearer".length()).trim();
        }
        else
        {
            MultivaluedMap<String, String> map = requestContext.getUriInfo().getQueryParameters();
            token = map.getFirst("access_token");
        }

        return token;
    }

    protected void verifyAudience(DecodedJWT jwt, String audience)
    {
        if(audience != null && jwt.getClaim("aud").asString() != null)
        {
            if(!jwt.getClaim("aud").asString().equals(audience))
            {
                throw new JWTVerificationException("audience mismatch");
            }
        }
    }

    protected DecodedJWT validateToken(String token, String key) throws Exception {
        if(key == null)
            throw new JWTVerificationException("no valid hash key");

        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm)
                .build(); //Reusable verifier instance
        return verifier.verify(token);
    }


    protected DecodedJWT validateToken(String token) throws Exception {
        try {
            // System.out.println(" secret " + authSecret);
            DecodedJWT jwt = JWT.decode(token);
            String keyId = jwt.getKeyId();
            if(provider == null)
                throw new JwkException("key provider not initialized");

            Jwk jwk = provider.get(keyId);

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey)(jwk.getPublicKey()));
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException | JwkException exception){
            exception.printStackTrace();
            throw exception;
        }
    }


}
