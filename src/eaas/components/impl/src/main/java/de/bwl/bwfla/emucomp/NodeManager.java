/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.emucomp;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.emucomp.components.containers.ContainerBean;
import de.bwl.bwfla.emucomp.components.network.NodeTcpBean;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.components.emulators.EmulatorBean;
import de.bwl.bwfla.emucomp.components.network.NetworkSwitchBean;
import de.bwl.bwfla.emucomp.components.network.VdeSlirpBean;

@ApplicationScoped
public class NodeManager {
    @Inject
    protected PrefixLogger log;

    @Resource(lookup = "java:jboss/ee/concurrency/factory/default")
    private ManagedThreadFactory workerThreadFactory = null;

    @Resource(lookup = "java:jboss/ee/concurrency/scheduler/default")
    protected ManagedScheduledExecutorService scheduler;

    @Resource(lookup = "java:jboss/ee/concurrency/executor/io")
    protected ExecutorService executor;
    
    protected ConcurrentMap<String, AbstractEaasComponent> components = new ConcurrentHashMap<String, AbstractEaasComponent>();

    @Inject
    @Config("components.warmup_timeout")
    protected Duration componentWarmupTimeout;

    @Inject
    @Config("components.timeout")
    protected Duration componentExpirationTimeout;

    private final AtomicBoolean isGcTriggered = new AtomicBoolean(false);
    
    
    // TODO: does it make sense to do something for @PreDestroy?
    

    public ManagedThreadFactory getWorkerThreadFactory()
    {
        return workerThreadFactory;
    }

    /**
     * Creates and registers a component with the given {@componentId} and
     * configuration.
     * 
     * After this method is called, the instance is allocated, under proper
     * resource management and fully initialized.
     * 
     * @param componentId
     * @param config a serialized instance of a {@link ComponentConfiguration}
     * @return the given {@code componentId}
     * @throws BWFLAException if {@code config} could not be unmarshalled or a
     *             runtime error occurred while registering the component to the
     *             local component map or an exception from
     *             {@link createComponentInstance}
     * 
     * @see #createComponentInstance(ComponentConfiguration, String)
     */
    public String allocateComponent(String componentId, String config) throws BWFLAException {
        try {
            final ComponentConfiguration configuration =
                    ComponentConfiguration.fromValue(config, ComponentConfiguration.class);

            // atomically create a new bean iff the given id does not already exist
            final AbstractEaasComponent component = components.computeIfAbsent(componentId, id -> {
                try {
                    return createComponentInstance(configuration, id);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            
            // don't to this in the atomic insert to reduce
            // the time the map's bucket is locked
            component.initialize(configuration);
            
            return componentId;
        }
        catch (JAXBException error) {
            throw new BWFLAException("Unmarshalling configuration metadata failed!", error);
        }
        catch (RuntimeException | BWFLAException error) {
            this.releaseComponent(componentId);
            if (error instanceof BWFLAException)
                throw (BWFLAException) error;
            else if (error.getCause() instanceof BWFLAException)
                throw (BWFLAException) error.getCause();
            else {
                log.log(Level.WARNING, "Allocating component failed!", error);
                throw new BWFLAException("Allocating component failed!", error);
            }
        }
    }
    

    /**
     * Destroys the component instance with the given {@code componentId}. 
     * 
     * @param componentId
     */
    public void releaseComponent(String componentId) {
        AbstractEaasComponent component = components.remove(componentId);
        if (component != null) {
            component.destroy();
            this.triggerGarbageCollection();
        }
    }

    /**
     * Resets the keepalive timeout for the specified {@code component}.
     * 
     * @param componentId
     */
    public void keepalive(String componentId) throws BWFLAException {
        AbstractEaasComponent component = this.getComponentById(componentId);
        component.setKeepaliveTimestamp(NodeManager.timestamp());
    }

    
    /**
     * Returns the component instance for the given {@code componentId}.
     * 
     * @param componentId
     * @return a component instance
     * @throws BWFLAException if there is no registered component instance with
     *             the given id.
     */
    public AbstractEaasComponent getComponentById(String componentId) throws BWFLAException {
        AbstractEaasComponent component = this.components.get(componentId);
        if (component == null) {
            throw new BWFLAException("Could not find a component instance for the given id: " + component);
        }
            
        return component;
    }
    
    public <T> T getComponentById(String componentId, Class<T> klass) throws BWFLAException {
        return klass.cast(this.getComponentById(componentId));
    }
    

    /**
     * Creates a component instance depending on the type of the
     * {@code configuration} argument.
     * 
     * The returned components are bare CDI-enabled instances and are not yet
     * configured or initialized.
     * 
     * @param configuration the configuration to base the instantiation on
     * @param componentId a component id to give to the instance
     * @return A component instance
     * @throws BWFLAException if the required bean type cannot be found by the
     *             classloader or the configuration does not correspond to any
     *             known bean class.
     */
    protected AbstractEaasComponent createComponentInstance(ComponentConfiguration configuration, String componentId) throws BWFLAException {
        try {
            AbstractEaasComponent component;

            if (configuration instanceof MachineConfiguration) {
                component = EmulatorBean.createEmulatorBean((MachineConfiguration) configuration);
                component.setEnvironmentId(((MachineConfiguration)configuration).getId());
            } else if (configuration instanceof ContainerConfiguration) {
                component = ContainerBean.createContainerBean((ContainerConfiguration) configuration);
            } else if (configuration instanceof VdeSlirpConfiguration) {
                component = VdeSlirpBean.createVdeSlirp((VdeSlirpConfiguration) configuration);
            } else if (configuration instanceof NetworkSwitchConfiguration) {
                component = NetworkSwitchBean.createNetworkSwitch((NetworkSwitchConfiguration) configuration);
            } else if (configuration instanceof NodeTcpConfiguration) {
                component = NodeTcpBean.createNodeTcp((NodeTcpConfiguration) configuration);
            } else {
                throw new BWFLAException("(Valid) Configuration does not correspond to a component type. This is almost certainly a programming error!");
            }

            // Explicitly run Tamaya's configuration injection
            ConfigHelpers.configure(component, ConfigurationProvider.getConfiguration());

            component.setComponentId(componentId);
            component.setKeepaliveTimestamp(NodeManager.timestamp());

            // Submit cleanup handler
            final Runnable cleanup = new CleanupTrigger(component, componentExpirationTimeout);
            scheduler.schedule(cleanup, componentWarmupTimeout.toMillis(), TimeUnit.MILLISECONDS);

            return component;

        } catch (ClassNotFoundException e) {
            throw new BWFLAException("The requested bean type could not be found.", e);
        }
    }
    
    protected void onComponentTimeout(String componentId) {
        if (!components.containsKey(componentId))
            return;

        log.info("Aww, component " + componentId + " has timed out :-(");
        this.releaseComponent(componentId);
    }

    private void triggerGarbageCollection()
    {
        if (!isGcTriggered.getAndSet(true))
            executor.execute(new GarbageCollectionRunner());
    }

    private static long timestamp()
    {
        return System.currentTimeMillis();
    }

    private class CleanupTrigger implements Runnable
    {
        private final AbstractEaasComponent component;
        private final long timeout;

        public CleanupTrigger(AbstractEaasComponent component, Duration timeout)
        {
            this.component = component;
            this.timeout = timeout.toMillis();
        }

        @Override
        public void run()
        {
            final long curts = NodeManager.timestamp();
            final long prevts = component.getKeepaliveTimestamp();
            final long elapsed = curts - prevts;
            if (elapsed < timeout) {
                // Component should be kept alive! Schedule this task again.
                final long delay = timeout - elapsed + 10L;
                scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
            }
            else {
                // Timeout expired!

                // Since scheduler tasks should complete quickly and this.onComponentTimeout()
                // can take longer, submit a new task to an unscheduled executor for it.
                executor.execute(() -> NodeManager.this.onComponentTimeout(component.getComponentId()));
            }
        }
    }

    private class GarbageCollectionRunner implements Runnable
    {
        @Override
        public void run()
        {
            this.sleep(500L);

            log.info("Trigger garbage-collection...");
            isGcTriggered.set(false);

            // HACK: certain dependencies (e.g. GStreamer bindings) seem to
            //       require multiple GC runs to properly release resources!

            System.gc();
            this.sleep(250L);
            System.gc();

            log.info("Finished garbage-collection");
        }

        private void sleep(long timeout)
        {
            try {
                Thread.sleep(timeout);
            }
            catch (Exception error) {
                // Ignore it!
            }
        }
    }
}
