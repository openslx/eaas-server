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

package de.bwl.bwfla.envproposer.impl;


public class UserData
{
	private String waitLocation;
	private String resultLocation;
	
	
	public UserData(String waitloc, String resultloc)
	{
		this.waitLocation = waitloc;
		this.resultLocation = resultloc;
	}
	
	public String getWaitLocation()
	{
		return waitLocation;
	}
	
	public void setWaitLocation(String location)
	{
		this.waitLocation = location;
	}
	
	public String getResultLocation()
	{
		return resultLocation;
	}
	
	public void setResultLocation(String location)
	{
		this.resultLocation = location;
	}
}
