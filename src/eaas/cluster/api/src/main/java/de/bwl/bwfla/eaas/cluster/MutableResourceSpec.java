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


public class MutableResourceSpec extends ResourceSpec
{
	public MutableResourceSpec()
	{
		super();
	}
	
	public MutableResourceSpec(int cpu, int memory)
	{
		super(cpu, memory);
	}
	
	public MutableResourceSpec(ResourceSpec other)
	{
		super(other);
	}
	
	public MutableResourceSpec set(ResourceSpec other)
	{
		super.cpu = other.cpu();
		super.memory = other.memory();
		return this;
	}
	
	public void reset()
	{
		super.cpu = 0;
		super.memory = 0;
	}
	
	public MutableResourceSpec cpu(int cpu)
	{
		super.checkAndSetCpu(cpu);
		return this;
	}
	
	public MutableResourceSpec memory(int memory)
	{
		super.checkAndSetMemory(memory);
		return this;
	}
	
	public boolean reserve(int reqcpu, int reqmemory)
	{
		return this.sub(reqcpu, reqmemory);
	}
	
	public boolean reserve(ResourceSpec other)
	{
		return this.sub(other.cpu(), other.memory());
	}
	
	public void free(int reqcpu, int reqmemory)
	{
		this.add(reqcpu, reqmemory);
	}
	
	public void free(ResourceSpec other)
	{
		this.add(other.cpu(), other.memory());
	}
	
	public boolean sub(ResourceSpec other)
	{
		return this.sub(other.cpu(), other.memory());
	}
	
	public boolean sub(ResourceSpec other, boolean clamp)
	{
		return this.sub(other.cpu(), other.memory(), clamp);
	}
	
	public boolean sub(int reqcpu, int reqmemory)
	{
		return this.sub(reqcpu, reqmemory, false);
	}
	
	public boolean sub(int reqcpu, int reqmemory, boolean clamp)
	{
		if (reqcpu < 0 || reqmemory < 0)
			throw new IllegalArgumentException("Passed arguements must be >= 0!");
		
		int newcpu = this.cpu() - reqcpu;
		if (newcpu < 0) {
			if (clamp)
				newcpu = 0;
			else return false;
		}
		
		int newmemory = this.memory() - reqmemory;
		if (newmemory < 0) {
			if (clamp)
				newmemory = 0;
			else return false;
		}
		
		super.cpu = newcpu;
		super.memory = newmemory;
		return true;
	}
	
	public void add(ResourceSpec other)
	{
		this.add(other.cpu(), other.memory());
	}
	
	public void add(int reqcpu, int reqmemory)
	{
		if (reqcpu < 0 || reqmemory < 0)
			throw new IllegalArgumentException("Passed arguements must be >= 0!");
		
		super.cpu += reqcpu;
		super.memory += reqmemory;
	}
	
	public void min(ResourceSpec other)
	{
		this.min(other.cpu(), other.memory());
	}
	
	public void min(int reqcpu, int reqmemory)
	{
		if (reqcpu < 0 || reqmemory < 0)
			throw new IllegalArgumentException("Passed arguements must be >= 0!");
		
		super.cpu = Math.min(super.cpu, reqcpu);
		super.memory = Math.min(super.memory, reqmemory);
	}
	
	public void max(ResourceSpec other)
	{
		this.max(other.cpu(), other.memory());
	}
	
	public void max(int reqcpu, int reqmemory)
	{
		if (reqcpu < 0 || reqmemory < 0)
			throw new IllegalArgumentException("Passed arguements must be >= 0!");
		
		super.cpu = Math.max(super.cpu, reqcpu);
		super.memory = Math.max(super.memory, reqmemory);
	}

	public void scale(int scaler)
	{
		if (scaler < 0)
			throw new IllegalArgumentException("Scaler must be >= 0!");
		
		super.cpu *= scaler;
		super.memory *= scaler;
	}
	
	public void scale(float scaler)
	{
		if (scaler < 0.0F)
			throw new IllegalArgumentException("Scaler must be >= 0!");
		
		super.cpu = (int) (scaler * (float) super.cpu);
		super.memory = (int) (scaler * (float) super.memory);
	}
	
	public boolean apply(ResourceDiff diff)
	{
		final int newcpu = this.cpu() + diff.cpu();
		if (newcpu < 0)
			return false;
		
		final int newmemory = this.memory() + diff.memory();
		if (newmemory < 0)
			return false;
		
		super.cpu = newcpu;
		super.memory = newmemory;
		return true;
	}
	
	
	/** Compute: s1 + s2 */
	public static MutableResourceSpec add(ResourceSpec s1, ResourceSpec s2)
	{
		MutableResourceSpec spec = new MutableResourceSpec(s1);
		spec.add(s2);
		return spec;
	}
	
	/** Compute: s1 - s2 */
	public static MutableResourceSpec sub(ResourceSpec s1, ResourceSpec s2)
	{
		MutableResourceSpec spec = new MutableResourceSpec(s1);
		spec.sub(s2);
		return spec;
	}
	
	/** Compute: min(s1, s2) */
	public static MutableResourceSpec min(ResourceSpec s1, ResourceSpec s2)
	{
		MutableResourceSpec spec = new MutableResourceSpec(s1);
		spec.min(s2);
		return spec;
	}
	
	/** Compute: max(s1, s2) */
	public static MutableResourceSpec max(ResourceSpec s1, ResourceSpec s2)
	{
		MutableResourceSpec spec = new MutableResourceSpec(s1);
		spec.max(s2);
		return spec;
	}
	
	/** Compute: max(0, s1 - s2) */
	public static MutableResourceSpec fromDiff(ResourceSpec s1, ResourceSpec s2)
	{
		int cpu = Math.max(0, s1.cpu() - s2.cpu());
		int memory = Math.max(0, s1.memory() - s2.memory());
		return new MutableResourceSpec(cpu, memory);
	}
}
