package de.bwl.bwfla.emil.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmilSessionEnvironment extends EmilObjectEnvironment {

    @XmlElement(required = false)
    private String baseEnv;
    @XmlElement(required = false)
    private String userId;
    @XmlElement(required = false)
    private long creationDate;

    public EmilSessionEnvironment() {}

    public EmilSessionEnvironment(EmilEnvironment template)
    {
        super(template);
    }

    public EmilSessionEnvironment(EmilObjectEnvironment  template)
    {
        super(template);
    }

    public EmilSessionEnvironment(EmilSessionEnvironment template)
    {
        super(template);
        this.baseEnv = template.baseEnv;
        this.userId = template.userId;
        this.creationDate = template.creationDate;
    }

    public String getBaseEnv() {
        return baseEnv;
    }

    public void setBaseEnv(String baseEnv) {
        this.baseEnv = baseEnv;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof EmilSessionEnvironment))
            return false;

        EmilSessionEnvironment info = (EmilSessionEnvironment)o;
        if(!userId.equals(info.userId))
            return false;

        if(!objectId.equals(info.objectId))
            return false;

        if(!envId.equals(info.envId))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 0;
        if(userId != null)
            hashCode += userId.hashCode();
        if(objectId != null)
            hashCode += objectId.hashCode();
        if(envId != null)
            hashCode += envId.hashCode();

        return hashCode;
    }
}
