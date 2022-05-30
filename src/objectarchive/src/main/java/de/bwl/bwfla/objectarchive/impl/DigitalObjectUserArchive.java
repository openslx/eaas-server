package de.bwl.bwfla.objectarchive.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectS3ArchiveDescriptor;


public class DigitalObjectUserArchive extends DigitalObjectS3Archive
{
    public DigitalObjectUserArchive(DigitalObjectS3ArchiveDescriptor descriptor) throws BWFLAException
    {
        super(descriptor);
    }
}
