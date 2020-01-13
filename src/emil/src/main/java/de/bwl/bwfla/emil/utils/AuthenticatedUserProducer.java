package de.bwl.bwfla.emil.utils;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Role;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import de.bwl.bwfla.emil.filters.AuthenticationFilter;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

@RequestScoped
public class AuthenticatedUserProducer {
    @Produces
    @RequestScoped
    @AuthenticatedUser
    private UserContext authenticatedUser = new UserContext();

    public AuthenticatedUserProducer() {}

    public void handleAuthenticationEvent(@Observes @AuthenticatedUser AuthenticationFilter.JwtLoginEvent event) {

        DecodedJWT jwt = event.getJwt();
        if(jwt == null)
        {
            authenticatedUser.setRole(Role.PUBLIC);
            authenticatedUser.setUsername("anonymous");
            return;
        }

        Claim usernameC = jwt.getClaim("sub");
        if(usernameC == null)
        {
            authenticatedUser = null;
            return;
        }
        authenticatedUser.setUsername(usernameC.asString());

        Claim nameC = jwt.getClaim("name");
        if(nameC != null)
            authenticatedUser.setName(nameC.asString());

        authenticatedUser.setRole(Role.RESTRCITED);
    }
}
