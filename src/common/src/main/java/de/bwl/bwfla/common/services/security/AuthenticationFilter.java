package de.bwl.bwfla.common.services.security;

import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

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
    @Config(value = "emil.authSecret")
    private String authSecret;

    @Inject
    @Config(value = "emil.authAudience")
    private String authAudience;

    @Inject
    @AuthenticatedUser
    Event<JwtLoginEvent> userAuthenticatedEvent;

    protected static final Logger LOG = Logger.getLogger("Authentication");

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
        }
        else {

            try {
                // Validate the token

                validateToken(token);
            } catch (Exception e) {
                LOG.severe(e.getMessage());
                requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED).build());
            }
        }
    }

    private void validateToken(String token) throws Exception {

        if(authSecret == null)
            throw new BWFLAException("no auth secret configured. configure 'emil.authSecret'");

        try {
            // System.out.println(" secret " + authSecret);
            Algorithm algorithm = Algorithm.HMAC256(authSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            if(authAudience != null && jwt.getClaim("aud") != null)
            {
                if(!jwt.getClaim("aud").equals(authAudience))
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