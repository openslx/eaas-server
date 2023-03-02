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

package de.bwl.bwfla.eaas.cluster.config.util;


import com.openslx.eaas.common.config.util.MemoryUnitParser;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;

import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.ResourceSpec.CpuUnit;
import de.bwl.bwfla.eaas.cluster.ResourceSpec.MemoryUnit;


public class ConfigHelpers extends de.bwl.bwfla.common.utils.ConfigHelpers
{
	public static ResourceSpec toResourceSpec(Configuration config)
	{
		final String cpustr = config.get("cpu");
		final String memstr = config.get("memory");
		if (cpustr == null || memstr == null)
			throw new ConfigException("ResourceSpec parameters are missing!");
		
		final long megabyte = 1024L * 1024L;
		final int cpu = new CpuUnitParser().parse(cpustr);
		long memory = new MemoryUnitParser().parse(memstr);
		if (memory > 0 && memory < megabyte)
			throw new ConfigException("Wrong memory unit! Expected value in megabytes or gigabytes!");
		
		memory /= megabyte;
		memory = Math.min(memory, Integer.MAX_VALUE);
		
		return ResourceSpec.create(cpu, CpuUnit.MILLICORES, (int) memory, MemoryUnit.MEGABYTES);
	}
	
	public static void check(ResourceSpec arg, String message)
	{
		if (arg == null)
			throw new ConfigException(message);
	}
}
