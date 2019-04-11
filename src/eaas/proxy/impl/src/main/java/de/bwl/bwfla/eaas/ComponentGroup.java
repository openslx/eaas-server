package de.bwl.bwfla.eaas;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.proxy.ComponentProxy;

@MTOM
@WebService(targetNamespace = "http://bwfla.bwl.de/api/eaas")
@ApplicationScoped
public class ComponentGroup {
    @Inject
    Logger log;
    
    protected ConcurrentMap<UUID, Set<String>> groupToComponents = new ConcurrentHashMap<UUID, Set<String>>();
    
    @Inject
    protected ComponentProxy component;
    
    @WebMethod
    public String createGroup() {
        UUID groupId = UUID.randomUUID();
        groupToComponents.computeIfAbsent(groupId, id -> Collections.synchronizedSet(new HashSet<String>()));
        return groupId.toString();
    }
    
    @WebMethod
    public void deleteGroup(@WebParam(name="groupId") String groupId) {
        groupToComponents.remove(UUID.fromString(groupId));
    }

    @WebMethod
    public void add(@WebParam(name="groupId") String groupId,
            @WebParam(name="componentId") String componentId) throws BWFLAException {
        Set<String> group = groupToComponents.get(UUID.fromString(groupId));
        if (group == null) {
            throw new IllegalArgumentException("Could not find group with the given groupId");
        }
        synchronized(group) {
            // re-verify that the group is still valid
            if (groupToComponents.containsKey(UUID.fromString(groupId))) {
                group.add(componentId);
            }
        }
    }

    @WebMethod
    public void remove(@WebParam(name="groupId") String groupId,
            @WebParam(name="componentId") String componentId) throws BWFLAException {
        Set<String> group = groupToComponents.get(UUID.fromString(groupId));
        if (group == null) {
            throw new IllegalArgumentException("Could not find group with the given groupId");
        }
        synchronized(group) {
            // re-verify that the group is still valid
            if (groupToComponents.containsKey(UUID.fromString(groupId))) {
                group.remove(componentId);
            }
        }
    }
    
    @WebMethod
    public Set<String> list(@WebParam(name="groupId") String groupId) throws BWFLAException {
        UUID uuid = UUID.fromString(groupId);
        if (!groupToComponents.containsKey(uuid)) {
            throw new IllegalArgumentException("Could not find group with the given groupId");
        }
        return new HashSet<String>(groupToComponents.get(uuid));
    }

    @WebMethod
    public Set<String> listGroupIds() throws BWFLAException {
        return groupToComponents.keySet()
                .stream()
                .map(UUID::toString)
                .collect(Collectors.toSet());
    }

    @WebMethod
    public void keepalive(@WebParam(name="groupId")String groupId) throws BWFLAException {
        UUID uuid = UUID.fromString(groupId);
        Set<String> components = new HashSet<String>(groupToComponents.get(uuid));

        BWFLAException e = null;
        for (String componentId : components) {
            try {
                component.keepalive(componentId);
            } catch(Throwable t) {
                components.remove(componentId);
                log.log(Level.WARNING, "Could not send keepalive to a component.", t);
                e = new BWFLAException("At least one keepalive could not be sent to the component: " + e.getMessage(), e);
            }
        }
        if (e != null) {
            throw e;
        }
    }
}
