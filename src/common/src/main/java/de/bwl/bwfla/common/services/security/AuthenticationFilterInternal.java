package de.bwl.bwfla.common.services.security;


import org.apache.tamaya.inject.api.Config;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@SecuredInternal
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilterInternal extends AbstractAuthenticationFilter {

    @Inject
    @Config(value = "rest.internalApiSecret")
    private String apiSecret;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String token = extractToken(requestContext);
        // LOG.severe("SecuredInternal");
        // debug(requestContext);
        if(token == null) {
            LOG.severe("internal security token not set");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        try {
            validateToken(token, apiSecret);
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}