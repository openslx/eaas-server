package de.bwl.bwfla.eaas.acl;

import de.bwl.bwfla.common.utils.ConfigHelpers;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
public class AclConfig {

    @Inject
    Logger log;

    private final HashMap<AclType, AclEntry> enabledAcls = new HashMap<>();

    public enum AclType
    {
        OBJECT("ObjectLicenseManager"),
        SOFTWARE("SoftwareLicenseManager");

        private final String value;

        AclType(String name)
        {
            this.value = name;
        }

        public static AclType fromString(String value)
        {
            for (AclType type : AclType.values()) {
                if (value.contentEquals(type.value))
                    return type;
            }
            throw new IllegalArgumentException("Invalid AclType: " + value);
        }
    }

    public class AclEntry {
        public AclType type;
        public int order;
        public HashMap<String, String> privateConfig;
    }

    @PostConstruct
    private void initialize() {
        final Configuration config = ConfigurationProvider.getConfiguration();
        while (true) {
            // Parse next entry...
            final String prefix = ConfigHelpers.toListKey("acl", enabledAcls.size(), ".");
            final Configuration subconfig = ConfigHelpers.filter(config, prefix);
            if (ConfigHelpers.isEmpty(subconfig))
                break;  // No more entries found!

            final AclType aclType = AclType.fromString(subconfig.get("type"));
            int order = Integer.parseInt(subconfig.get("order"));

            AclEntry acl = new AclEntry();
            acl.order = order;
            acl.type = aclType;

            log.info("Registered new acl: " + aclType + " (" + order + ")");

            enabledAcls.put(aclType, acl);
        }
    }

    public AclEntry getEntry(AclType t)
    {
        return enabledAcls.get(t);
    }

}
