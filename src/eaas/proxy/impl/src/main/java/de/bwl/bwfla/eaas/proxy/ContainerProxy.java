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

package de.bwl.bwfla.eaas.proxy;

import de.bwl.bwfla.api.emucomp.Container;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.SessionRegistry;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.soap.MTOM;


@MTOM
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebServlet("/ComponentProxy/Container")
@WebService(targetNamespace = "http://bwfla.bwl.de/api/emucomp", serviceName = "ContainerService", portName = "ContainerPort")
public class ContainerProxy implements de.bwl.bwfla.api.emucomp.Container
{
	@Inject
	private DirectComponentClient componentClient;

	@Inject
	private SessionRegistry sessions = null;

	protected Container lookup(String componentId) throws BWFLAException
	{
		final SessionRegistry.Entry session = sessions.lookup(componentId);
		final ResourceHandle resource = session.getResourceHandle();
		return componentClient.getContainerPort(resource.getNodeID());
	}

	@Override
	public void startContainer(String componentId) throws BWFLAException
	{
		this.lookup(componentId).startContainer(componentId);
	}

	@Override
	public void stopContainer(String componentId) throws BWFLAException
	{
		this.lookup(componentId).stopContainer(componentId);
	}
}