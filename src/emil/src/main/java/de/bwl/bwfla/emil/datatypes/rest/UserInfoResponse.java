package de.bwl.bwfla.emil.datatypes.rest;


import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UserInfoResponse extends EmilResponseType {

    @XmlElement(required = true)
    private String userId;

    @XmlElement
    private String fullName;

    @XmlElement
    private String role;

    @XmlElement
    private String username;

    public UserInfoResponse(BWFLAException e)
    {
        super(e);
    }

    public UserInfoResponse() {}


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
