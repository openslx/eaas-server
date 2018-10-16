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

package de.bwl.bwfla.emucomp.ws;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.api.ContainerComponent;

import javax.inject.Inject;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.soap.MTOM;


@MTOM
@WebServlet("/ComponentService/Container")
@WebService(targetNamespace="http://bwfla.bwl.de/api/emucomp")
public class Container
{
    @Inject
    protected NodeManager nodeManager;
    
	public void startContainer(String componentId) throws BWFLAException
	{
		final ContainerComponent component = this.lookup(componentId);
		component.start();
	}
	
	public void stopContainer(String componentId) throws BWFLAException
	{
		final ContainerComponent component = this.lookup(componentId);
		component.stop();
	}


	/* =============== Internal Helpers =============== */

	private ContainerComponent lookup(String id) throws BWFLAException
	{
		return nodeManager.getComponentById(id, ContainerComponent.class);
	}
}
