package de.bwl.bwfla.common.services.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.tamaya.ConfigurationProvider;


import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class AbstractAuthenticationFilter implements ContainerRequestFilter
{
    protected static final Logger LOG = Logger.getLogger("Authentication");
    protected static final JwkProvider provider = AbstractAuthenticationFilter.createJwkProvider();

    @Context
    protected ResourceInfo resourceInfo;


    protected void debug(ContainerRequestContext requestContext)
    {
        Class<?> resourceClass = resourceInfo.getResourceClass();
        LOG.severe("Debug: class context:  " + resourceClass.getName());

        Method resourceMethod = resourceInfo.getResourceMethod();
        LOG.severe("Debug: method context " + resourceMethod.getName());

        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        LOG.severe("auth header: " + authorizationHeader);
    }

    protected String extractToken(ContainerRequestContext requestContext)
    {
        // debug(requestContext);
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

    protected void verifyAudienceClaim(DecodedJWT jwt, String audience) throws JWTVerificationException
    {
        if (audience == null || audience.isEmpty())
            return;

        final Claim claim = jwt.getClaim("aud");
        if (claim == null)
            throw new JWTVerificationException("Audience missing!");

        if (!audience.equals(claim.asString()))
            throw new JWTVerificationException("Audience mismatch!");
    }

    protected DecodedJWT verify(String token) throws Exception
    {
        if (provider == null)
            throw new JWTVerificationException("Key provider is not initialized!");

        final DecodedJWT jwt = JWT.decode(token);
        final String keyId = jwt.getKeyId();
        final Jwk jwk = provider.get(keyId);
        LOG.severe(jwk.getPublicKey().getAlgorithm() + " " + jwk.getPublicKey().toString());
        return this.verify(token, Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey()));
    }

    protected DecodedJWT verify(String token, String key) throws Exception
    {
        if (key == null || key.isEmpty())
            throw new JWTVerificationException("Private key is invalid!");

        LOG.info("Authenticating using private secret...");
        return this.verify(token, Algorithm.HMAC256(key));
    }

    private DecodedJWT verify(String token, Algorithm algorithm) throws Exception
    {
        final JWTVerifier verifier = JWT.require(algorithm)
                .build();

        return verifier.verify(token);
    }

    static void abort(ContainerRequestContext context)
    {
        LOG.severe("Validation of authentication token failed!");
        context.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    static void abort(ContainerRequestContext context, Exception error)
    {
        LOG.log(Level.SEVERE, "Validation of authentication token failed!", error);
        context.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    private static JwkProvider createJwkProvider()
    {
        final String authJwksUri = ConfigurationProvider.getConfiguration()
                .get("emil.authJwksUri");

        if (authJwksUri.isEmpty())
            return null;

        try {
            return new JwkProviderBuilder(new URL(authJwksUri))
                    .cached(10, 1, TimeUnit.HOURS)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build();
        }
        catch (Exception error) {
            throw new RuntimeException("Initializing JwkProvider failed!", error);
        }
    }
}
