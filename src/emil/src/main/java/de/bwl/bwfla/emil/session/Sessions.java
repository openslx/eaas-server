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

package de.bwl.bwfla.emil.session;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.rest.ErrorInformation;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.emil.session.rest.DetachRequest;
import de.bwl.bwfla.emil.session.rest.RunningNetworkEnvironmentResponse;
import de.bwl.bwfla.emil.session.rest.SessionComponent;
import de.bwl.bwfla.emil.session.rest.SessionResponse;
import de.bwl.bwfla.emucomp.client.ComponentClient;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


@Path("/sessions")
@ApplicationScoped
public class Sessions
{
	@Inject
	private SessionManager sessions = null;

	@Resource(lookup = "java:jboss/ee/concurrency/scheduler/default")
	private ManagedScheduledExecutorService scheduler;

	@Resource(lookup = "java:jboss/ee/concurrency/executor/io")
	private ExecutorService executor;

	@Inject
	private ComponentClient componentClient;

	@Inject
	@Config(value = "ws.eaasgw")
	private String eaasGw;

	@Inject
	@Config("components.client_timeout")
	private Duration resourceExpirationTimeout;


	/* ========================= Public API ========================= */

//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
//	public CreateSessionResponse create(SessionRequest request, @Context final HttpServletResponse response)
//	{
//		final String id = UUID.randomUUID().toString();
//		sessions.register(new Session(id, request.getResources()));
//		sessions.setLifetime(id, request.getLifetime(), request.getLifetimeUnit());
//		response.setStatus(Response.Status.CREATED.getStatusCode());
//		return new CreateSessionResponse(id);
//	}

	@DELETE
	@Path("/{id}")
	@Secured(roles = {Role.RESTRICTED})
	public void delete(@PathParam("id") String id, @Context final HttpServletResponse response)
	{
		sessions.remove(id);
		response.setStatus(Response.Status.OK.getStatusCode());
	}

//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	@Path("/{id}/resources")
//	public void addResources(@PathParam("id") String id, SessionRequest request, @Context final HttpServletResponse response)
//	{
//		sessions.add(id, request.getResources());
//		response.setStatus(Response.Status.OK.getStatusCode());
//	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured(roles = {Role.PUBLIC})
	@Path("/{id}/resources")
	public void removeResources(@PathParam("id") String id, List<String> resources, @Context final HttpServletResponse response)
	{
		sessions.remove(id, resources);
		response.setStatus(Response.Status.OK.getStatusCode());
	}

//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
//	@Path("/{id}/resources")
//	public Collection<String> getResources(@PathParam("id") String id)
//	{
//		final Session session = sessions.get(id);
//		if (session == null) {
//			throw new NotFoundException(Response.status(Response.Status.NOT_FOUND)
//					.entity(new ErrorInformation("Session not found!", "Session-ID: " + id))
//					.build());
//		}
//
//		try {
//			return groupClient.getComponentGroupPort(eaasGw).list(session.resources());
//		} catch (BWFLAException e) {
//			throw new NotFoundException(Response.status(Response.Status.NOT_FOUND)
//					.entity(new ErrorInformation("Session has no valid group", "Session-ID: " + id))
//					.build());
//		}
//	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(roles = {Role.PUBLIC})
	@Path("/{id}/detach")
	public void setLifetime(@PathParam("id") String id, DetachRequest request, @Context final HttpServletResponse response)
	{
		sessions.setLifetime(id, request.getLifetime(), request.getLifetimeUnit(), request.getSessionName(), request.getComponentTitle());
		response.setStatus(Response.Status.OK.getStatusCode());
	}

	@POST
	@Secured(roles = {Role.PUBLIC})
	@Path("/{id}/keepalive")
	public void keepalive(@PathParam("id") String id) {
		if (!sessions.keepalive(id)) {
			throw new NotFoundException(Response.status(Response.Status.NOT_FOUND)
					.entity(new ErrorInformation("Session not found!", "Session-ID: " + id))
					.build());
		}
	}

	@GET
	@Secured(roles = {Role.PUBLIC})
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Session> list() {
		return sessions.list();
	}

	@GET
	@Secured(roles = {Role.PUBLIC})
	@Path("/network-environments")
	@Produces(MediaType.APPLICATION_JSON)
	public List<RunningNetworkEnvironmentResponse> getSessionsWithNetworkEnvID() {
		ArrayList<RunningNetworkEnvironmentResponse> builder = new ArrayList<>();
		for (Session session : sessions.list()) {
			if (session instanceof NetworkSession) {
				final SessionResponse result = new SessionResponse(((NetworkSession) session).getNetworkRequest());
				builder.add(new RunningNetworkEnvironmentResponse(session, result.getNetwork().getNetworkEnvironmentId()));
			}
		}
		return builder;
	}

	@GET
	@Secured(roles = {Role.PUBLIC})
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public SessionResponse listComponents(@PathParam("id") String id) {
		try {
			Session session = sessions.get(id);
			if(session == null || !(session instanceof NetworkSession))
				throw new BWFLAException("session not found " + id);

			SessionResponse result = new SessionResponse(((NetworkSession) session).getNetworkRequest());
			for (de.bwl.bwfla.emil.session.SessionComponent component : session.components()) {

				String type = componentClient.getComponentPort(eaasGw).getComponentType(component.id());
				if(type.equals("nodetcp")) {
					NetworkResponse networkResponse = new NetworkResponse(session.id());

					Map<String, URI> controlUrls = ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw).getControlUrls(component.id()));

					URI uri = controlUrls.get("info");
					if(uri == null)
						continue;
					String nodeInfoUrl = uri.toString();
					networkResponse.addUrl("tcp", URI.create(nodeInfoUrl));
					SessionComponent sc = new SessionComponent(component.id(), type, null);
					sc.addNetworkData(networkResponse);

				} else if (type.equals("machine")){
                    String environmentId = componentClient.getComponentPort(eaasGw).getEnvironmentId(component.id());
                    result.add(new SessionComponent(component.id(), type, environmentId));
				} else
					result.add(new SessionComponent(component.id(), type, null));
			}
			return result;
		} catch (BWFLAException e) {
			e.printStackTrace();
			throw new ServerErrorException(Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new ErrorInformation(
							"Could not acquire group information.", e.getMessage()))
					.build());
		}
	}


	/* ========================= Internal Helpers ========================= */

	@PostConstruct
	private void initialize()
	{
		final Runnable trigger = () -> executor.execute(() -> sessions.update(executor));
		final long delay = resourceExpirationTimeout.toMillis() / 2L;
		scheduler.scheduleWithFixedDelay(trigger, delay, delay, TimeUnit.MILLISECONDS);
	}
}
