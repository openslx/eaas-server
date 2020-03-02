package de.bwl.bwfla.common.services.security;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmilEnvironmentPermissions {
    @XmlEnum
    public enum Permissions {
        @XmlEnumValue("0")
        NONE(0),
        @XmlEnumValue("1")
        READ(1),
        @XmlEnumValue("2")
        WRITE(2);


        private final int value;

        private Permissions(int v) {
            this.value = v;
        }

        public int getValue() {
            return this.value;
        }
    }

    @XmlElement(required = false)
    private Permissions user;
    @XmlElement(required = false)
    private Permissions group;

    public Permissions getUser() {
        return user;
    }

    public void setUser(Permissions user) {
        this.user = user;
    }

    public Permissions getGroup() {
        return group;
    }

    public void setGroup(Permissions group) {
        this.group = group;
    }
}
