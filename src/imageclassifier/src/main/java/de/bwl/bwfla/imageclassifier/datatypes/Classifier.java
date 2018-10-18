package de.bwl.bwfla.imageclassifier.datatypes;


import de.bwl.bwfla.imageclassifier.datatypes.IdentificationOutputIndex;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class Classifier<T> {

    protected List<Path> contentDirectories = new ArrayList<>();

    public void addDirectory(Path contentDirectory)
    {
        contentDirectories.add(contentDirectory);
    }

    public abstract IdentificationOutputIndex<T> runIdentification(boolean verbose);

}
