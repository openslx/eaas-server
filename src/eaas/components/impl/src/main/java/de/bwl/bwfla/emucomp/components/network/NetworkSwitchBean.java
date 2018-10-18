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

package de.bwl.bwfla.emucomp.components.network;

import javax.enterprise.inject.spi.CDI;

import de.bwl.bwfla.emucomp.api.ClusterComponent;
import de.bwl.bwfla.emucomp.api.NetworkSwitchComponent;
import de.bwl.bwfla.emucomp.api.NetworkSwitchConfiguration;
import de.bwl.bwfla.emucomp.components.EaasComponentBean;

/**
 * @author iv1004
 * 
 */
public abstract class NetworkSwitchBean extends EaasComponentBean implements ClusterComponent, NetworkSwitchComponent 
{
	public static NetworkSwitchBean createNetworkSwitch(NetworkSwitchConfiguration config) throws ClassNotFoundException 
	{
		// TODO: parse and use 'config' parameter
		
		// XXX: only VDE switches are supported right now 
		String targetBean = "VdeSwitchBean";
		
		Class<?> beanClass = Class.forName(NetworkSwitchBean.class.getPackage().getName() + "." + targetBean);
        return (NetworkSwitchBean)CDI.current().select(beanClass).get();
	}
	
	@Override
	public String getComponentType() {
	    return "switch";
	}
}