package de.bwl.bwfla.common.services.security;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.tamaya.inject.api.Config;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.logging.Logger;

@RequestScoped
public class AuthenticatedUserProducer {
    @Produces
    @RequestScoped
    @AuthenticatedUser
    private UserContext authenticatedUser = new UserContext();

    protected static final Logger LOG = Logger.getLogger("Authentication");

    @Inject
    @Config(value = "emil.adminRoleLabel")
    private String adminRoleLabel;

    @Inject
    @Config(value = "emil.userRoleLabel")
    private String userRoleLabel;

    @Inject
    @Config(value = "emil.keycloakAdminUser")
    private String keycloakAdminUser;

    public AuthenticatedUserProducer() {}

    public void handleAuthenticationEvent(@Observes @AuthenticatedUser AuthenticationFilter.JwtLoginEvent event) {

        DecodedJWT jwt = event.getJwt();
        if(jwt == null || jwt.getClaim("sub") == null)
        {
            authenticatedUser.setRole(Role.PUBLIC);
            authenticatedUser.setUsername("anonymous");
            authenticatedUser.setUserId("anonymous");
            return;
        }

        Claim userIdC = jwt.getClaim("sub");
        authenticatedUser.setUserId(userIdC.asString());
        authenticatedUser.setRole(Role.RESTRICTED);

        Claim usernameC = jwt.getClaim("preferred_username");
        if(usernameC != null)
        {
            authenticatedUser.setUsername(usernameC.asString());
        }

        Claim nameC = jwt.getClaim("name");
        if(nameC != null)
            authenticatedUser.setName(nameC.asString());

        if(authenticatedUser.getUsername() != null && authenticatedUser.getUsername().equals(keycloakAdminUser)) {
            authenticatedUser.setRole(Role.ADMIN);
        } else if(!adminRoleLabel.isEmpty()) {

            // reset role if we require user roles
            if(!userRoleLabel.isEmpty())
                authenticatedUser.setRole(Role.PUBLIC);

            Claim roles = jwt.getClaim("roles");
            if (roles != null) {
                String[] roleList = roles.asArray(String.class);
                if (roleList != null) {
                    for (String r : roleList) {
                        if (r.equals(adminRoleLabel))
                            authenticatedUser.setRole(Role.ADMIN);
                        else if(!userRoleLabel.isEmpty() && r.equals(userRoleLabel))
                            authenticatedUser.setRole(Role.RESTRICTED);
                    }
                }
            }
        }
    }
}
