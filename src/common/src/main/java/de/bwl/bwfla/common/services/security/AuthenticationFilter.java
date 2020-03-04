package de.bwl.bwfla.common.services.security;

import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import org.apache.tamaya.inject.api.Config;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Inject
    @Config(value = "emil.authEnabled")
    private boolean authEnabled;

    @Inject
    @Config(value = "emil.authAudience")
    private String authAudience;

    @Inject
    @Config(value = "emil.authJwksUri")
    private String authJwksUri;

    @Inject
    @AuthenticatedUser
    Event<JwtLoginEvent> userAuthenticatedEvent;

    @Context
    private ResourceInfo resourceInfo;

    protected static final Logger LOG = Logger.getLogger("Authentication");

    private static JwkProvider provider = null;


    private String extractLocalKey(AnnotatedElement annotatedElement)
    {
        Secured secured = annotatedElement.getAnnotation(Secured.class);
        String key = secured.secret();

        if(key.isEmpty())
            return null;

        return key;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if(!authEnabled)
            return;
        // Get the HTTP Authorization header from the request
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
        // String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXUyJ9.eyJpc3MiOiJhdXRoMCJ9.AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";

        if(token == null) {
            LOG.warning("anonymous");
            userAuthenticatedEvent.fire(new JwtLoginEvent(null));
            return;
        }

        // check if local key annotation exists
        String localKey = null;
        Class<?> resourceClass = resourceInfo.getResourceClass();
        localKey = extractLocalKey(resourceClass);

        Method resourceMethod = resourceInfo.getResourceMethod();
        localKey = extractLocalKey(resourceMethod);
        try {
            if(localKey != null) {
                validateToken(token, localKey);
            }
            else {
                if (provider == null && authJwksUri != null) {
                    provider = new JwkProviderBuilder(new URL(authJwksUri))
                            .cached(10, 24, TimeUnit.HOURS)
                            .rateLimited(10, 1, TimeUnit.MINUTES)
                            .build();
                }
                validateToken(token);

            }
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    private void validateToken(String token, String key) throws Exception {
        try {
            LOG.info("authentification using local secret " + key);
            Algorithm algorithm = Algorithm.HMAC256(key);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);

            if(authAudience != null && jwt.getClaim("aud").asString() != null)
            {
                if(!jwt.getClaim("aud").asString().equals(authAudience))
                {
                    userAuthenticatedEvent.fire(new JwtLoginEvent(null));
                    throw new JWTVerificationException("audience mismatch");
                }
            }
            userAuthenticatedEvent.fire(new JwtLoginEvent(jwt));
        } catch (JWTVerificationException exception){
            throw exception;
        }
    }


    private void validateToken(String token) throws Exception {
        try {
            // System.out.println(" secret " + authSecret);
            DecodedJWT jwt = JWT.decode(token);
            String keyId = jwt.getKeyId();
            Jwk jwk = provider.get(keyId);

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey)(jwk.getPublicKey()));
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
            jwt = verifier.verify(token);
            if(authAudience != null && jwt.getClaim("aud").asString() != null)
            {
                if(!jwt.getClaim("aud").asString().equals(authAudience))
                {
                    userAuthenticatedEvent.fire(new JwtLoginEvent(null));
                   // LOG.severe("configured audience " + authAudience);
                   // LOG.severe("claim: " + jwt.getClaim("aud").asString());
                    throw new JWTVerificationException("audience mismatch");
                }
            }
            userAuthenticatedEvent.fire(new JwtLoginEvent(jwt));
        } catch (JWTVerificationException | JwkException exception){
            exception.printStackTrace();
            throw exception;
        }
    }

    public class JwtLoginEvent
    {
        private final DecodedJWT jwt;

        JwtLoginEvent(DecodedJWT jwt)
        {
            this.jwt = jwt;
        }

        public DecodedJWT getJwt() {
            return jwt;
        }
    }
}