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

package de.bwl.bwfla.metadata.oai.harvester;

import de.bwl.bwfla.common.datatypes.security.Secured;
import de.bwl.bwfla.metadata.oai.harvester.config.BackendConfig;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;


@ApplicationScoped
@Path("/harvesters")
public class HarvesterAPI
{
	@Resource(lookup = "java:jboss/ee/concurrency/executor/io")
	private Executor executor = null;

	@Inject
	private HarvesterRegistry harvesters = null;


	// ========== Admin API ==============================

	@GET
	@Secured
	@Produces(MediaType.APPLICATION_JSON)
	public Response listHarvesters()
	{
		final Collection<String> ids = harvesters.list();
		return Response.ok(ids, MediaType.APPLICATION_JSON_TYPE)
				.build();
	}

	@POST
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(BackendConfig config)
	{
		harvesters.add(config);
		return Response.ok()
				.build();
	}

	@DELETE
	@Secured
	@Path("/{name}")
	public Response unregister(@PathParam("name") String name)
	{
		if (!harvesters.remove(name))
			throw new NotFoundException("Harvester not found: " + name);

		return Response.ok()
				.build();
	}

	@POST
	@Secured
	@Path("/{name}")
	public CompletionStage<Response> harvest(@PathParam("name") String name, @Context HttpServletRequest request)
	{
		final HarvesterBackend harvester = harvesters.lookup(name);
		if (harvester == null)
			throw new NotFoundException("Harvester not found: " + name);

		final Instant fromts = HarvesterAPI.getFromTimestamp(request);
		final Instant untilts = HarvesterAPI.getUntilTimestamp(request);

		final Supplier<Response> responder = () -> {
			try {
				final HarvestingResult result = harvester.execute(fromts, untilts);
				return Response.ok(result.toJsonString(), MediaType.APPLICATION_JSON_TYPE)
						.build();
			}
			catch (Exception error) {
				final String message = "Harvesting failed!";
				throw new InternalServerErrorException(message, error);
			}
		};

		return CompletableFuture.supplyAsync(responder, executor);
	}

	@GET
	@Secured
	@Path("/{name}/status")
	public Response status(@PathParam("name") String name)
	{
		final HarvesterBackend harvester = harvesters.lookup(name);
		if (harvester == null)
			throw new NotFoundException("Harvester not found: " + name);

		return Response.ok(harvester.getStatus(), MediaType.APPLICATION_JSON_TYPE)
				.build();
	}


	// ========== Internal Helpers ==============================

	private static Instant getTimestampParam(HttpServletRequest request, String name, Instant defvalue)
	{
		final String value = request.getParameter(name);
		if (value == null || value.isEmpty())
			return defvalue;

		return Instant.parse(value);
	}

	private static Instant getFromTimestamp(HttpServletRequest request)
	{
		return HarvesterAPI.getTimestampParam(request, "from", null);
	}

	private static Instant getUntilTimestamp(HttpServletRequest request)
	{
		return HarvesterAPI.getTimestampParam(request, "until", null);
	}
}
