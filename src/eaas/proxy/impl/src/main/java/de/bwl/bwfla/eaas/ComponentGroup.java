package de.bwl.bwfla.eaas;

import java.util.*;
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
    
    protected ConcurrentMap<UUID, Set<ComponentGroupElement>> groupToComponents = new ConcurrentHashMap<UUID, Set<ComponentGroupElement>>();
    
    @Inject
    protected ComponentProxy component;
    
    @WebMethod
    public String createGroup() {
        UUID groupId = UUID.randomUUID();
        groupToComponents.computeIfAbsent(groupId, id -> Collections.synchronizedSet(new HashSet<ComponentGroupElement>()));
        return groupId.toString();
    }
    
    @WebMethod
    public void deleteGroup(@WebParam(name="groupId") String groupId) {
        groupToComponents.remove(UUID.fromString(groupId));
    }

    @WebMethod
    public void add(@WebParam(name="groupId") String groupId,
            @WebParam(name="componentId") String componentId) throws BWFLAException {
        Set<ComponentGroupElement> group = groupToComponents.get(UUID.fromString(groupId));
        if (group == null) {
            throw new IllegalArgumentException("Could not find group with the given groupId");
        }
        synchronized(group) {
            // re-verify that the group is still valid
            if (groupToComponents.containsKey(UUID.fromString(groupId))) {
                group.add(new ComponentGroupElement(componentId));
            }
        }
    }

    @WebMethod
    public void update(@WebParam(name = "groupId") String groupId,
                       @WebParam(name = "componentGroupElement") ComponentGroupElement component) throws BWFLAException {
        Set<ComponentGroupElement> group = groupToComponents.get(UUID.fromString(groupId));
        if (group == null) {
            throw new IllegalArgumentException("Could not find group with the given groupId");
        }
        synchronized (group) {
            Optional<ComponentGroupElement> oldElement = group.stream().filter(obj -> obj.getComponentId().equals(component.getComponentId())).findFirst();
            if(!oldElement.isPresent()){
                throw new BWFLAException("component is not found");
            }
            group.remove(oldElement.get());
            group.add(component);

        }
    }

    @WebMethod
    public void remove(@WebParam(name="groupId") String groupId,
            @WebParam(name="componentId") String componentId) throws BWFLAException {
        Set<ComponentGroupElement> group = groupToComponents.get(UUID.fromString(groupId));
        if (group == null) {
            throw new IllegalArgumentException("Could not find group with the given groupId");
        }
        synchronized(group) {
            // re-verify that the group is still valid
            if (groupToComponents.containsKey(UUID.fromString(groupId))) {
                group.removeIf(item -> (item.getComponentId().equals(componentId)));
            }
        }
    }
    
    @WebMethod
    public Set<ComponentGroupElement> list(@WebParam(name="groupId") String groupId) throws BWFLAException {
        UUID uuid = UUID.fromString(groupId);
        if (!groupToComponents.containsKey(uuid)) {
            throw new IllegalArgumentException("Could not find group with the given groupId");
        }
        return new HashSet<ComponentGroupElement>(groupToComponents.get(uuid));
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
        Set<ComponentGroupElement> components = new HashSet<ComponentGroupElement>(groupToComponents.get(uuid));

        BWFLAException e = null;
        for (ComponentGroupElement componentElement : components) {
            try {
                component.keepalive(componentElement.getComponentId());
            } catch(Throwable t) {
                components.remove(componentElement.getComponentId());
                log.log(Level.WARNING, "Could not send keepalive to a component.", t);
                e = new BWFLAException("At least one keepalive could not be sent to the component: " + e.getMessage(), e);
            }
        }
        if (e != null) {
            throw e;
        }
    }
}
