package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.api.objectarchive.DigitalObjectMetadata;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.EmilSoftwareData;
import de.bwl.bwfla.metadata.repository.api.ItemDescription;
import de.bwl.bwfla.metadata.repository.api.ItemIdentifierDescription;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SoftwareCollection implements Iterable<EaasiSoftwareObject> {

    private final ObjectArchiveHelper objectArchivHelper;
    private final SoftwareArchiveHelper swArchiveHelper;
    private HashMap<String, EaasiSoftwareObject> softwarePackages;

    public SoftwareCollection(ObjectArchiveHelper objectArchiveHelper, SoftwareArchiveHelper swArchiveHelper)
    {
        this.swArchiveHelper = swArchiveHelper;
        this.objectArchivHelper = objectArchiveHelper;
        this.softwarePackages = new HashMap<>();

        List<String> softwareIds = null;

        try {
            softwareIds = swArchiveHelper.getSoftwarePackages();
        } catch (BWFLAException e) {
            e.printStackTrace();
            softwareIds = new ArrayList<>();
        }

        for(String swid : softwareIds)
        {
            SoftwarePackage p = null;
            try {
                p = swArchiveHelper.getSoftwarePackageById(swid);
                DigitalObjectMetadata md = objectArchivHelper.getObjectMetadata(p.getArchive(), p.getObjectId());
                if(md.getMetsData() == null)
                {
                    System.out.println("metsdata null ... skipping");
                    continue;
                }
                softwarePackages.put(p.getId(), new EaasiSoftwareObject(p, md.getMetsData()));
            } catch (BWFLAException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SoftwareObjectsIterator iterator() {

        return new SoftwareObjectsIterator();
    }

    public Stream<EaasiSoftwareObject> toStream()
    {
        final Spliterator<EaasiSoftwareObject> spliterator = Spliterators.spliteratorUnknownSize(iterator(), 0);
        return StreamSupport.stream(spliterator, false);
    }

    public EaasiSoftwareObject getId(String id) {
        return softwarePackages.get(id);
    }

    public int size() {
        return softwarePackages.size();
    }

    public class SoftwareObjectsIterator implements Iterator {

        private final Iterator<String> idIter;

        SoftwareObjectsIterator() {
            this.idIter = softwarePackages.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return idIter.hasNext();
        }

        @Override
        public Object next() {
            String id = idIter.next();
            return softwarePackages.get(id);
        }
    }
}
