package de.bwl.bwfla.emil.session;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.NetworkRequest;
import de.bwl.bwfla.emil.session.Session;

public class NetworkSession extends Session {

    private final String switchId;
    private final NetworkRequest networkRequest;

    NetworkSession(String id, String groupId, String switchId, NetworkRequest request) throws BWFLAException {
        super(id, groupId);
        this.switchId = switchId;
        this.networkRequest = request;
    }

    public String getSwitchId() {
        return switchId;
    }

    public NetworkRequest getNetworkRequest() {
        return networkRequest;
    }
}
