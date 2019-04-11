package de.bwl.bwfla.emil.session;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.session.Session;

public class NetworkSession extends Session {

    private String switchId;

    NetworkSession(String id, String groupId, String switchId) throws BWFLAException {
        super(id, groupId);
        this.switchId = switchId;
    }

    public String getSwitchId() {
        return switchId;
    }
}
