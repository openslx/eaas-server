package de.bwl.bwfla.common.services.security;


import org.apache.tamaya.inject.api.Config;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;


@SecuredAPI
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilterApi extends AbstractAuthenticationFilter {

    @Inject
    @Config(value = "rest.apiSecret")
    private String apiSecret;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        final String token = this.extractToken(requestContext);
        if (token == null) {
            AbstractAuthenticationFilter.abort(requestContext);
            return;
        }

        try {
            this.verify(token, apiSecret);
        }
        catch (Exception error) {
            AbstractAuthenticationFilter.abort(requestContext, error);
        }
    }
}