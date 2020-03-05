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
public class AuthenticationFilter extends AbstractAuthenticationFilter {

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

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if(!authEnabled)
            return;

        String token = extractToken(requestContext);
        // String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXUyJ9.eyJpc3MiOiJhdXRoMCJ9.AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";

        if(token == null) {
            LOG.warning("anonymous");
            userAuthenticatedEvent.fire(new JwtLoginEvent(null));
            return;
        }

        // check if local key annotation exists

        try {
            initWKProvider(authJwksUri);
            DecodedJWT jwt = validateToken(token);
            verifyAudience(jwt, authAudience);
            userAuthenticatedEvent.fire(new JwtLoginEvent(jwt));
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build());
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