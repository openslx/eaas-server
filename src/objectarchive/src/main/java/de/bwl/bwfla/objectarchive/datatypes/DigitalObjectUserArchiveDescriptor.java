package de.bwl.bwfla.objectarchive.datatypes;

public class DigitalObjectUserArchiveDescriptor extends DigitalObjectArchiveDescriptor {

    public DigitalObjectUserArchiveDescriptor() {
        setType(ArchiveType.USER);
    }

    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
