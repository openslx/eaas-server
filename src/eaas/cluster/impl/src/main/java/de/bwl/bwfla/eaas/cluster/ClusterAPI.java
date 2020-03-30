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

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.logging.PrefixLoggerContext;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.SecuredInternal;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpFlags;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.rest.ClusterDescription;


@Path("api/v1")
public class ClusterAPI
{
	private static final int NUM_BASE_SEGMENTS = 3;
	
	private PrefixLogger log;

	@Context
	private UriInfo uri;
	
	@Inject
	private IClusterManager clustermgr;


	/* ========== Admin API ========== */

	@GET
	@Path("/clusters")
	@Secured(roles = {Role.ADMIN})
	@Produces(MediaType.APPLICATION_JSON)
	public Response listClusters()
	{
		// Currently max. 1 is supported!
		
		final Function<JsonGenerator, Status> handler = (json) -> {
			json.writeStartArray();
			if (clustermgr != null) {
				json.write(clustermgr.getName());
			}

			json.writeEnd();
			return Status.OK;
		};

		return this.execute(handler, JSON_RESPONSE_CAPACITY);
	}

	@GET
	@Path("/clusters/{cluster_name}/description")
	@Secured(roles = {Role.ADMIN})
	@Produces(MediaType.APPLICATION_JSON)
	public ClusterDescription getClusterDescription(@PathParam("cluster_name") String name)
	{
		return this.findClusterManager(name)
				.describe(true);
	}


	/* ========== Internal API ========== */

	@GET
	@SecuredInternal
	@Path("/internal/clusters/{cluster_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dumpClusterResource(@PathParam("cluster_name") String name)
	{
		return this.execute(name, JSON_RESPONSE_CAPACITY);
	}
	
	@GET
	@SecuredInternal
	@Path("/internal/clusters/{cluster_name}/{subres:.*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dumpClusterSubResource(@PathParam("cluster_name") String name)
	{
		return this.execute(name, JSON_RESPONSE_CAPACITY);
	}
	
	
	/* ========== Internal Helpers ========== */

	private static final int JSON_RESPONSE_CAPACITY = 4 * 1024;
	
	/** Constructor for CDI */
	public ClusterAPI()
	{
		// Empty!
	}

	@PostConstruct
	public void initialize()
	{
		PrefixLoggerContext logContext = new PrefixLoggerContext();
		logContext.add("CM", clustermgr.getName());

		this.log = new PrefixLogger(this.getClass().getName(), logContext);
	}

	private List<PathSegment> skipPathSegments(int num)
	{
		final List<PathSegment> segments = uri.getPathSegments();
		return segments.subList(NUM_BASE_SEGMENTS + num, segments.size());
	}

	private Response execute(Function<JsonGenerator, Status> handler, int capacity)
	{
		final StringWriter buffer = new StringWriter(capacity);
		try (JsonGenerator json = ClusterAPI.newJsonGenerator(buffer, true)) {
			final Status status = handler.apply(json);
			json.flush();

			return ClusterAPI.newResponse(status, buffer);
		}
		catch (Throwable error) {
			final String url = uri.getAbsolutePath().toString();
			log.log(Level.WARNING, "Executing handler for URL '" + url + "' failed!\n", error);
			if (error instanceof WebApplicationException)
				return ((WebApplicationException) error).getResponse();
			else return ClusterAPI.newErrorResponse(error);
		}
	}

	private Response execute(String clusterName, int capacity)
	{
		final Function<JsonGenerator, Status> handler = (json) -> {
			final IClusterManager cluster = this.findClusterManager(clusterName);

			DumpConfig dconf = new DumpConfig(this.skipPathSegments(1), uri.getQueryParameters());
			cluster.dump(json, dconf, DumpFlags.TIMESTAMP | DumpFlags.RESOURCE_TYPE);
			return Status.OK;
		};

		return this.execute(handler, capacity);
	}

	private IClusterManager findClusterManager(String name)
	{
		if (clustermgr == null || !clustermgr.getName().contentEquals(name)) {
			String message = "Cluster manager '" + name + "' was not found!";
			throw new NotFoundException(message);
		}

		return clustermgr;
	}

	private static JsonGenerator newJsonGenerator(Writer writer, boolean pretty)
	{
		if (!pretty)
			return Json.createGenerator(writer);

		final Map<String, Boolean> config = new HashMap<String, Boolean>();
		config.put(JsonGenerator.PRETTY_PRINTING, pretty);
		return Json.createGeneratorFactory(config)
				.createGenerator(writer);
	}

	private static Response newErrorResponse(Throwable error)
	{
		final JsonObjectBuilder json = Json.createObjectBuilder();
		DumpHelpers.addResourceTimestamp(json);
		DumpHelpers.addResourceType(json, "InternalServerError");
		json.add("error_type", error.getClass().getName())
			.add("error_message", error.getMessage());

		return ClusterAPI.newResponse(Status.INTERNAL_SERVER_ERROR, json.build().toString());
	}

	private static Response newResponse(Status status, StringWriter message)
	{
		return ClusterAPI.newResponse(status, message.toString());
	}

	private static Response newResponse(Status status, String message)
	{
		return Response.status(status)
				.entity(message)
				.build();
	}
}
