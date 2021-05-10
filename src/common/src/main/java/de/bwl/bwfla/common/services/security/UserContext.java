package de.bwl.bwfla.common.services.security;

import java.util.UUID;

public class UserContext {

    public final static String INVALID_USER = UUID.randomUUID().toString();
    private String userId;
    private String tenantId;
    private String username;
    private String name;
    private Role role;

    public UserContext() {
        userId = INVALID_USER;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String id) {
        this.tenantId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public UserContext clone() {
        UserContext copy = new UserContext();
        copy.userId = userId;
        copy.tenantId = tenantId;
        copy.username = username;
        copy.name = name;
        copy.role = role;
        return copy;
    }

    public boolean isAvailable() {
        return !(userId != null && userId.equals(INVALID_USER));
    }
}
