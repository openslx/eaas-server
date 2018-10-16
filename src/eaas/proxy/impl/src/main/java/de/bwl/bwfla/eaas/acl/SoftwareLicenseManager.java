package de.bwl.bwfla.eaas.acl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SoftwareLicenseManager extends LicenseManager {

    protected SoftwareArchiveHelper softwareArchive = null;
    private int order;

    @PostConstruct
    public void initialize() {
        AclConfig.AclEntry entry = aclConfig.getEntry(AclConfig.AclType.SOFTWARE);
        if(entry == null)
            return;
        
        this.order = entry.order;

        final Configuration config = ConfigurationProvider.getConfiguration();
        final String softwareArchiveUrl = config.get("ws.softwarearchive");

        if (softwareArchiveUrl != null) {
            this.softwareArchive = new SoftwareArchiveHelper(softwareArchiveUrl);
        }
    }

    @Override
    public void checkPermission(UUID sessionContext, Object userContext,
                                ComponentConfiguration config) throws BWFLAException {

        if (softwareArchive == null)
            return; // No archive specified, skip the whole thing.

        // currently, software license management only makes sense for
        // machine configurations
        if (!(config instanceof MachineConfiguration)) {
            return;
        }
        assert(config instanceof MachineConfiguration);

        final List<String> allocatedSoftwareIds = allocations.computeIfAbsent(sessionContext, ctx -> new ArrayList<String>());
        final List<String> installedSoftwareIds = ((MachineConfiguration)config).getInstalledSoftwareIds();
        // Allocate software seats...
        try {
            for (String softwareId : installedSoftwareIds) {
                int max = softwareArchive.getNumSoftwareSeatsById(softwareId);
                if(max < 0)
                    max = Integer.MAX_VALUE;
                allocate(softwareId, max);
                allocatedSoftwareIds.add(softwareId);
            }
        } catch (Throwable t) {
            // TODO: implement some retry logic!

            // Not all seats could be allocated, clean up
            for (String softwareId : allocatedSoftwareIds)
                releaseSeat(softwareId);
            allocatedSoftwareIds.clear();

            throw t;
        }

        if (allocatedSoftwareIds.size() == installedSoftwareIds.size())
            return; // All seats allocated!
    }

    @Override
    public void release(UUID sessionContext, Object userContext) {
        if (softwareArchive == null)
            return; // No archive specified, skip the whole thing.

        final List<String> allocatedSoftwareIds = allocations.get(sessionContext);
        if (allocatedSoftwareIds == null)
            return;  // No allocations made for this UUID

        for (String softwareId : allocatedSoftwareIds)
            releaseSeat(softwareId);
        allocatedSoftwareIds.clear();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

}
