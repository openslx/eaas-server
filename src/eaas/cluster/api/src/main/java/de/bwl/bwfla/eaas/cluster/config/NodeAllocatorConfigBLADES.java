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

package de.bwl.bwfla.eaas.cluster.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;

import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.config.util.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;


public class NodeAllocatorConfigBLADES extends NodeAllocatorConfig
{
	private ResourceSpec nodeCapacity = null;
	private List<String> addresses = null;
	
	
	/* ========== Getters and Setters ========== */
	
	public ResourceSpec getNodeCapacity()
	{
		return nodeCapacity;
	}
	
	public void setNodeCapacity(ResourceSpec capacity)
	{
		ConfigHelpers.check(capacity, "Node capacity is invalid!");
		
		this.nodeCapacity = capacity;
	}
	
	public List<String> getNodeAddresses()
	{
		return addresses;
	}
	
	public void setNodeAddresses(List<String> addresses)
	{
		ConfigHelpers.check(addresses, "List of node addresses is invalid!");

		this.addresses = addresses;
	}
	
	public boolean hasHomogeneousNodes()
	{
		return true;
	}
	
	
	/* ========== Initialization ========== */

	@Override
	public void load(Configuration config) throws ConfigException
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);
		
		// Configure node capacity for this instance
		{
			Configuration newconfig = ConfigHelpers.filter(config, "node_capacity.");
			nodeCapacity = ConfigHelpers.toResourceSpec(newconfig);
		}
		
		// Configure node addresses for this instance
		addresses = ConfigHelpers.getAsList(config, "node_addresses");
	}
	
	@Override
	public void validate() throws ConfigException
	{
		this.valid = false;
		
		super.validate();
		
		// Re-check the arguments...
		this.setNodeCapacity(nodeCapacity);
		this.setNodeAddresses(addresses);
		
		this.valid = true;
	}

	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			final JsonObject object = super.dump();
			object.forEach((key, value) -> {
				dumper.add(key, () -> json.write(key, value));
			});
			
			dumper.add(DumpFields.NODE_CAPACITY, () -> {
				DumpHelpers.write(json, DumpFields.NODE_CAPACITY, nodeCapacity);
			});
			
			dumper.add(DumpFields.NODE_ADDRESSES, () -> {
				json.writeStartArray(DumpFields.NODE_ADDRESSES);
				addresses.forEach((address) -> json.write(address));
				json.writeEnd();
			});
			
			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String NODE_CAPACITY   = "node_capacity";
		private static final String NODE_ADDRESSES  = "node_addresses";
	}
}
