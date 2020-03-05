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
import org.apache.tamaya.inject.api.Config;

import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.inject.Inject;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@SecuredInternal
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilterInternal extends AbstractAuthenticationFilter {

    @Inject
    @Config(value = "rest.apiSecret")
    private String apiSecret;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String token = extractToken(requestContext);

        if(token == null) {
            LOG.severe("internal security token not set");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        try {
            validateToken(token, apiSecret);
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}