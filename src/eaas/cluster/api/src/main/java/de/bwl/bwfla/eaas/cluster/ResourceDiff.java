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


public class ResourceDiff
{
	protected int cpu;
	protected int memory;
	
	public ResourceDiff(int cpu, int memory)
	{
		this.cpu = cpu;
		this.memory = memory;
	}
	
	public ResourceDiff(ResourceDiff other)
	{
		this(other.cpu(), other.memory());
	}
	
	public ResourceDiff(ResourceSpec other)
	{
		this(other.cpu(), other.memory());
	}
	
	public int cpu()
	{
		return cpu;
	}
	
	public int memory()
	{
		return memory;
	}
	
	public void apply(ResourceDiff other)
	{
		this.cpu += other.cpu();
		this.memory += other.memory();
	}
	
	public void add(ResourceSpec spec)
	{
		this.cpu += spec.cpu();
		this.memory += spec.memory();
	}
	
	public void subtract(ResourceSpec spec)
	{
		this.cpu -= spec.cpu();
		this.memory -= spec.memory();
	}
	
	/** Compute: s1 - s2 */
	public static ResourceDiff create(ResourceSpec s1, ResourceSpec s2)
	{
		return ResourceDiff.create(s1, s2, false);
	}
	
	/** Compute: clamp ? max(0, s1 - s2) : s1 - s2 */
	public static ResourceDiff create(ResourceSpec s1, ResourceSpec s2, boolean clamp)
	{
		int cpu = s1.cpu() - s2.cpu();
		int memory = s1.memory() - s2.memory();
		if (clamp) {
			cpu = Math.max(0, cpu);
			memory = Math.max(0, memory);
		}
		
		return new ResourceDiff(cpu, memory);
	}
}
