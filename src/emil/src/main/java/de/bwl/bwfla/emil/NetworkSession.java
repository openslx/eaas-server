package de.bwl.bwfla.emil;

import de.bwl.bwfla.common.exceptions.BWFLAException;

public class NetworkSession extends Session {

    private String switchId;

    NetworkSession(String id, String groupId, String switchId) throws BWFLAException {
        super(id, groupId);
        this.switchId = switchId;
        addComponent(switchId);
    }

    public String getSwitchId() {
        return switchId;
    }
}
