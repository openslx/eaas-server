package de.bwl.bwfla.common.services.security;

import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import com.auth0.jwt.interfaces.DecodedJWT;
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
    @AuthenticatedUser
    private Event<JwtLoginEvent> userAuthenticatedEvent;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if(!authEnabled)
            return;

        final String token = this.extractToken(requestContext);
        if (token == null) {
            LOG.warning("Authentication token is missing! Continue as anonymous user.");
            userAuthenticatedEvent.fire(new JwtLoginEvent(null));
            return;
        }

        try {
            final DecodedJWT jwt = this.verify(token);
            this.verifyAudienceClaim(jwt, authAudience);
            userAuthenticatedEvent.fire(new JwtLoginEvent(jwt));
        }
        catch (Exception error) {
            AbstractAuthenticationFilter.abort(requestContext, error);
        }
    }

    public static class JwtLoginEvent
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