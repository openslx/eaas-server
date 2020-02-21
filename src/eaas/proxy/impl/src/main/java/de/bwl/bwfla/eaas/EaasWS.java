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

package de.bwl.bwfla.eaas;

import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.api.emucomp.ComponentService;
import de.bwl.bwfla.api.emucomp.Machine;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.eaas.acl.EnvironmentLock;
import de.bwl.bwfla.eaas.acl.IAccessControlList;
import de.bwl.bwfla.eaas.cluster.IClusterManager;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.ResourceSpec.CpuUnit;
import de.bwl.bwfla.eaas.cluster.ResourceSpec.MemoryUnit;
import de.bwl.bwfla.eaas.cluster.config.util.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.exception.AllocationFailureException;
import de.bwl.bwfla.eaas.cluster.exception.MalformedLabelSelectorException;
import de.bwl.bwfla.eaas.cluster.exception.OutOfResourcesException;
import de.bwl.bwfla.eaas.cluster.exception.QuotaExceededException;
import de.bwl.bwfla.eaas.cluster.metadata.LabelSelector;
import de.bwl.bwfla.eaas.cluster.metadata.LabelSelectorParser;
import de.bwl.bwfla.eaas.proxy.DirectComponentClient;
import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import de.bwl.bwfla.emucomp.api.Environment;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;


@MTOM
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebService(targetNamespace = "http://bwfla.bwl.de/api/eaas")
public class EaasWS
{
    @Inject
    IClusterManager clusterManager;
    
    @Inject
    DirectComponentClient serviceCache;

	@Inject
	@Config(value = "ws.eaasgw")
	protected String eaasGw;

    @Inject
    Instance<IAccessControlList> acls;

    final private List<IAccessControlList> sortedAcls = new ArrayList<>();

    @Inject
    PrefixLogger log;

	@Inject
	private SessionRegistry sessions = null;

	private static final Logger			LOG	= Logger.getLogger(EaasWS.class.getName());
	@Deprecated
	private static Machine			    emulatorPort;

	private ResourceSpec defaultSessionSpec = null;

	@PostConstruct
	private void postConstruct()
	{
		// Get resource spec to use per session by default
		{
			final Configuration config = ConfigurationProvider.getConfiguration();
			final Configuration newconfig = ConfigHelpers.filter(config, "ws.session_resources.");
			defaultSessionSpec = ConfigHelpers.toResourceSpec(newconfig);
		}

		try
		{
	        final String proxyWsdl    = eaasGw + "/eaas/ComponentProxy?wsdl";

            ComponentService proxyService = new ComponentService(new URL(proxyWsdl));
            emulatorPort = proxyService.getMachinePort();
		}
		catch(Exception exception) {
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
		}

		for (IAccessControlList acl : acls)
		{
			sortedAcls.add(acl);
		}
		Collections.sort(sortedAcls, new Comparator<IAccessControlList>() {
			@Override
			public int compare(IAccessControlList o1, IAccessControlList o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});

	}

	@WebMethod
	public String createSession(final String xmlConfig)
			throws OutOfResourcesException, QuotaExceededException, BWFLAException
	{
		return this.createSessionWithOptions(xmlConfig, new SessionOptions());
	}

	@WebMethod
	public String createSessionWithOptions(final String xmlConfig, SessionOptions options)
			throws OutOfResourcesException, QuotaExceededException, BWFLAException
	{
	    final ComponentConfiguration config;

	    if(options == null)
	    	options = new SessionOptions();

		List<String> selectors;
		if(options.selectors == null)
			selectors = Collections.EMPTY_LIST;
		else
			selectors = options.selectors;

	    try  {
	        config = ComponentConfiguration.fromValue(xmlConfig, ComponentConfiguration.class);
	    } catch (JAXBException e) {
            LOG.log(Level.SEVERE, "Could not unmarshal configuration", e);
	        throw new IllegalArgumentException("Could not unmarshal configuration", e);
	    }
	    
        final UUID allocationId = UUID.randomUUID();
		final String componentId = allocationId.toString();
		final String tenantId = options.getTenantId();
	    try {
            for (IAccessControlList acl : sortedAcls) {
            	if(acl instanceof EnvironmentLock) {
					if(options.lockEnvironment)
						acl.checkPermission(allocationId, options.userId, config);
				}
				else acl.checkPermission(allocationId, options.userId, config);
            }
            
            final List<LabelSelector> labelSelectors = this.parseLabelSelectors(selectors);

            ResourceSpec spec = options.getResourceSpec();
            if (spec == null) {
				// Resources not specified, use defaults...
				if (config instanceof Environment) {
					spec = defaultSessionSpec;
				}
				else
					spec = ResourceSpec.create(1, CpuUnit.MILLICORES, 1, MemoryUnit.MEGABYTES);
			}

	        final ResourceHandle resource = clusterManager.allocate(tenantId, labelSelectors, allocationId, spec, Duration.ofMinutes(2));
			try {
				final Component component = serviceCache.getComponentPort(resource.getNodeID());
				component.initialize(componentId, config.value(false));
			}
			catch (Exception error) {
				clusterManager.release(resource);
				throw error;
			}

			sessions.add(componentId, resource, () -> this.releaseSession(componentId));
            return componentId;
        }
        catch (Exception error) {
            LOG.log(Level.WARNING, "Creating new session failed!\n", error);
            LOG.info("Rolling back all ACL allocations...");
            for (IAccessControlList acl : sortedAcls) {
                acl.release(allocationId, null);
            }

            // Rethrow the error, using its original type...

			if (error instanceof QuotaExceededException)
				throw (QuotaExceededException) error;

			if (error instanceof OutOfResourcesException)
				throw (OutOfResourcesException) error;

			// Threat allocation errors as out-of-resources errors
			if (error instanceof AllocationFailureException)
				throw new OutOfResourcesException(error.getMessage(), error);

            if (error instanceof BWFLAException)
				throw (BWFLAException) error;

            throw new BWFLAException("Creating new session failed!", error);
        }
	}

	@WebMethod
	public void releaseSession(String componentId)
	{
		final SessionRegistry.Entry session = sessions.remove(componentId);
        if (session == null)
        	return;

		final ResourceHandle resource = session.getResourceHandle();
        try {
            Component component = serviceCache.getComponentPort(resource.getNodeID());
            component.destroy(componentId);
        } catch (BWFLAException e) {
            log.log(Level.SEVERE, "Could not connect to the web service to properly release the component", e);
        } finally {
            this.clusterManager.release(resource);
            for (IAccessControlList acl : sortedAcls) {
                acl.release(resource.getAllocationID(), null);
            }
        }
	}


	/* ==================== Internal Helpers ==================== */
	
	private List<LabelSelector> parseLabelSelectors(List<String> selectors) throws BWFLAException
	{
		final List<LabelSelector> list = new ArrayList<LabelSelector>(selectors.size());
		final LabelSelectorParser parser = new LabelSelectorParser();
		try {
			for (String selstr : selectors)
				list.add(parser.parse(selstr));
		}
		catch (MalformedLabelSelectorException error) {
			throw new BWFLAException("Parsing label selectors failed!", error);
		}

		return list;
	}


	public static class SessionOptions {
		List<String> selectors;
		String userId;
		boolean lockEnvironment = false;
		private String tenantId = null;

		// Requested session resources
		private ResourceSpec spec = null;

		public boolean isLockEnvironment() {
			return lockEnvironment;
		}

		public void setLockEnvironment(boolean lockEnvironment) {
			this.lockEnvironment = lockEnvironment;
		}

		public List<String> getSelectors() {
			return selectors;
		}

		public void setSelectors(List<String> selectors) {
			this.selectors = selectors;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public ResourceSpec getResourceSpec() {
			return resourceSpec;
		}

		public void setResourceSpec(ResourceSpec resourceSpec) {
			this.resourceSpec = resourceSpec;
		}

		public void setTenantId(String id) {
			this.tenantId = id;
		}

		public String getTenantId() {
			return tenantId;
		}

		public void setResourceSpec(ResourceSpec spec) {
			this.spec = spec;
		}

		public ResourceSpec getResourceSpec() {
			return spec;
		}
	}
}
