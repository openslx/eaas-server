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


/** A parser for strings representing CPU units. */
public class CpuUnitParser
{
	public CpuUnitParser()
	{
		// Empty
	}
	
	/** Returns value in millicpus (= cpus * 1000) */
	public int parse(String value)
	{
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException("CPU value is null or empty!");
		
		// Values are expected to be in the form:
		//     <float>
		//     <number>
		//     <number>m
		//     +inf
		//
		// Examples:
		//     4 -> 4 CPUs
		//     0.5 -> 0.5 CPUs 
		//     25m -> 0.025 CPUs
		
		if (value.contentEquals("inf") || value.contentEquals("+inf"))
			return Integer.MAX_VALUE;
		
		final int midx = value.indexOf("m");
		if (midx < 0) {
			// Value is not in millicpus, convert it...
			return (int) (Float.parseFloat(value) * 1000F);
		}
		
		// Value is in millicpus, parse it...
		return Integer.parseInt(value.substring(0, midx));
	}
}
