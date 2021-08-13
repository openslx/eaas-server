package de.bwl.bwfla.common.services.security;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmilEnvironmentPermissions {
    @XmlEnum
    @XmlJavaTypeAdapter(PermissionAdapter.class)
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

        public static Permissions from(String value) {
            switch (value.toUpperCase()) {
                case "0":
                case "NONE":
                    return NONE;
                case "1":
                case "READ":
                    return READ;
                case "2":
                case "WRITE":
                    return WRITE;
                default:
                    throw new IllegalArgumentException("Unknown permission: "  + value);
            }
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


    private static class PermissionAdapter extends XmlAdapter<String, Permissions>
    {
        @Override
        public Permissions unmarshal(String value) throws Exception
        {
            return Permissions.from(value);
        }

        @Override
        public String marshal(Permissions permission) throws Exception
        {
            return permission.name();
        }
    }
}
