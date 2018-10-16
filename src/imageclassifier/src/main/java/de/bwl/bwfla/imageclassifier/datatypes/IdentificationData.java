package de.bwl.bwfla.imageclassifier.datatypes;

import de.bwl.bwfla.common.datatypes.identification.DiskType;
import de.bwl.bwfla.imageclassifier.datatypes.IdentificationOutputIndex;

public class IdentificationData<T> {

    private final IdentificationOutputIndex<T> index;
    private final DiskType type;

    public IdentificationData(IdentificationOutputIndex<T> index, DiskType type)
    {
        this.index = index;
        this.type = type;
    }

    public IdentificationOutputIndex<T> getIndex() {
        return index;
    }

    public DiskType getType() {
        return type;
    }
}
