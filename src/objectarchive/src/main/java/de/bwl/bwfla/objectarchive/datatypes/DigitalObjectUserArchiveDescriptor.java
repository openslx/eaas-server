package de.bwl.bwfla.objectarchive.datatypes;

import java.io.IOException;


public class DigitalObjectUserArchiveDescriptor extends DigitalObjectS3ArchiveDescriptor {

    public DigitalObjectUserArchiveDescriptor() {
        setType(ArchiveType.USER);
    }

    public static DigitalObjectUserArchiveDescriptor create(String user, DigitalObjectS3ArchiveDescriptor s3desc)
            throws IOException
    {
        if (s3desc == null)
            s3desc = DigitalObjectS3ArchiveDescriptor.zeroconf();

        final var desc = new DigitalObjectUserArchiveDescriptor();
        desc.setEndpoint(s3desc.getEndpoint());
        desc.setAccessKey(s3desc.getAccessKey());
        desc.setSecretKey(s3desc.getSecretKey());
        desc.setBucket(s3desc.getBucket());
        desc.setPath(s3desc.getPath());
        desc.setName(user);
        return desc;
    }
}
