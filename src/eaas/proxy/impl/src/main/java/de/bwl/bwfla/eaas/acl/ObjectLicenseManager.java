package de.bwl.bwfla.eaas.acl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import de.bwl.bwfla.emucomp.api.EmulationEnvironmentHelper;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.ObjectArchiveBinding;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ObjectLicenseManager extends LicenseManager {

    protected ObjectArchiveHelper objectArchiveHelper = null;
    private int order;

    @PostConstruct
    public void initialize() {

        AclConfig.AclEntry entry = aclConfig.getEntry(AclConfig.AclType.OBJECT);
        if(entry == null)
            return;

        order = entry.order;

        final Configuration config = ConfigurationProvider.getConfiguration();
        final String objectArchiveUrl = config.get("ws.objectarchive");

        if(objectArchiveUrl != null)
        {
            objectArchiveHelper = new ObjectArchiveHelper(objectArchiveUrl);
        }
    }

    @Override
    public void checkPermission(UUID sessionContext, Object userContext, ComponentConfiguration config) throws BWFLAException {

        if(objectArchiveHelper == null)
            return;

        if (!(config instanceof MachineConfiguration)) {
            return;
        }

        final List<String> allocatedObjectIds = allocations.computeIfAbsent(sessionContext, ctx -> new ArrayList<String>());
        final List<ObjectArchiveBinding> objects = EmulationEnvironmentHelper.getObjects((MachineConfiguration)config);

        try {
            for (ObjectArchiveBinding o: objects) {
                int max = objectArchiveHelper.getNumObjectSeats(o.getArchive(), o.getObjectId());
                if(max < 0)
                    max = Integer.MAX_VALUE;
                allocate(o.getObjectId() + o.getArchive(), max);
                allocatedObjectIds.add(o.getObjectId() + o.getArchive());
            }
        } catch (Throwable t) {
            // TODO: implement some retry logic!

            // Not all seats could be allocated, clean up
            for (String softwareId : allocatedObjectIds)
                releaseSeat(softwareId);
            allocatedObjectIds.clear();

            throw t;
        }
    }

    @Override
    public void release(UUID sessionContext, Object userContext) {
        if (objectArchiveHelper == null)
            return;

        final List<String> allocatedObjects = allocations.get(sessionContext);
        if (allocatedObjects == null)
            return;  // No allocations made for this UUID

        for (String obj : allocatedObjects)
            releaseSeat(obj);
        allocatedObjects.clear();
    }

    @Override
    public int getOrder() {
        return order;
    }
}
