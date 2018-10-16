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

package de.bwl.bwfla.eaas.cluster;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ResourceSpec
{
	/** Supported CPU units. */
	public static enum CpuUnit
	{
		CORES,
		MILLICORES
	}
	
	/** Supported memory units. */
	public static enum MemoryUnit
	{
		MEGABYTES,
		GIGABYTES
	}

	/** Internal unit for CPU resources */
	public static final CpuUnit CPU_UNIT = CpuUnit.MILLICORES;
	
	/** Internal unit for memory resources */
	public static final MemoryUnit MEMORY_UNIT = MemoryUnit.MEGABYTES;
	
	@XmlElement(name="cpu", required=true)
	protected int cpu;  // in millicores
	
	@XmlElement(name="memory", required=true)
	protected int memory;  // in megabytes

	
	protected ResourceSpec()
	{
		this(0, 0);
	}
	
	protected ResourceSpec(int cpu, int memory)
	{
		this.checkAndSetCpu(cpu);
		this.checkAndSetMemory(memory);
	}
	
	public ResourceSpec(ResourceSpec other)
	{
		this(other.cpu, other.memory);
	}
	
	public int cpu()
	{
		return cpu;
	}
	
	public int memory()
	{
		return memory;
	}
	
	public boolean isDefined()
	{
		return (cpu > 0 || memory > 0);
	}
	
	@Override
	public String toString()
	{
		return "{ cpu: " + cpu + "m, memory: " + memory + "MB }";
	}
	
	
	/* ========== Factory Methods ========== */
	
	public static ResourceSpec create(float cpu, int memory, MemoryUnit memUnit)
	{
		return ResourceSpec.create((int)(cpu * 1000F), CpuUnit.MILLICORES, memory, memUnit);
	}
	
	public static ResourceSpec create(int cpu, CpuUnit cpuUnit, int memory, MemoryUnit memUnit)
	{
		switch (cpuUnit)
		{
			case CORES:
				cpu *= 1000;
				break;
				
			case MILLICORES:
				break;
		}
		
		switch (memUnit)
		{
			case MEGABYTES:
				break;
				
			case GIGABYTES:
				memory *= 1024;
				break;
		}
		
		return new ResourceSpec(cpu, memory);
	}
	
	public static ResourceSpec create(int scaler, ResourceSpec other)
	{
		final int cpu = scaler * other.cpu();
		final int memory = scaler * other.memory();
		return new ResourceSpec(cpu, memory);
	}
	
	public static ResourceSpec create(float scaler, ResourceSpec other)
	{
		final float cpu = scaler * (float) other.cpu();
		final float memory = scaler * (float) other.memory();
		return new ResourceSpec((int) cpu, (int) memory);
	}
	
	public static int compare(ResourceSpec s1, ResourceSpec s2)
	{
		final int result = Integer.compare(s1.cpu(), s2.cpu());
		if (result != 0)
			return result;
		
		return Integer.compare(s1.memory(), s2.memory());
	}


	/* ========== Internal Helpers ========== */
	
	protected void checkAndSetCpu(int newcpu)
	{
		if (newcpu < 0)
			throw new IllegalArgumentException("CPU parameter must be >= 0! Actual: " + newcpu);
		
		this.cpu = newcpu;
	}
	
	protected void checkAndSetMemory(int newmemory)
	{
		if (newmemory < 0)
			throw new IllegalArgumentException("Memory parameter must be >= 0! Actual: " + newmemory);
		
		this.memory = newmemory;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + cpu;
        result = prime * result + memory;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceSpec other = (ResourceSpec) obj;
        if (cpu != other.cpu)
            return false;
        if (memory != other.memory)
            return false;
        return true;
    }
}
