package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.emil.datatypes.rest.ContainerNetworkingType;

import java.util.ArrayList;

public class NetworkEnvironmentCreateReq {
    ArrayList<String> environmentIDs = new ArrayList<>();
    ContainerNetworkingType networking = new ContainerNetworkingType();
}
