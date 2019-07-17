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

package de.bwl.bwfla.emucomp;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.common.services.sse.EventSink;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.util.logging.Logger;


@ApplicationScoped
@Path("/api/v1/components")
public class EventManager
{
	private final Logger log = Logger.getLogger(EventManager.class.getName());

	@Inject
	private NodeManager nodemgr = null;

	@GET
	@Path("/{componentId}/events")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public void register(@PathParam("componentId") String componentId, @Context SseEventSink sink, @Context Sse sse)
	{
		try {
			final AbstractEaasComponent component = nodemgr.getComponentById(componentId);
			if (component.hasEventSink())
				throw new BadRequestException("An event-sink is already registered!");

			log.warning("Start sending server-sent-events for component " + componentId);
			component.setEventSink(new EventSink(sink, sse));
		}
		catch (BWFLAException error) {
			throw new NotFoundException("Component not found: " + componentId);
		}
	}
}
