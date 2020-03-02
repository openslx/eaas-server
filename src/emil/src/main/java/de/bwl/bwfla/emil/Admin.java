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

package de.bwl.bwfla.emil;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.EaasBuildInfo;
import de.bwl.bwfla.emil.datatypes.rest.UserInfoResponse;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;


@Path("/admin")
@ApplicationScoped
public class Admin extends EmilRest
{
	@Inject
	@AuthenticatedUser
	private UserContext authenticatedUser = null;

	@Inject
	private EmilEnvironmentRepository environmentRepository = null;


	private static final String USAGE_LOG_PATH = "/home/bwfla/server-data/sessions.csv";


	// ========== Admin API =========================

	@GET
	@Secured({Role.PUBLIC})
	@Path("/build-info")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBuildInfo()
	{
		final JsonObject json = Json.createObjectBuilder()
				.add("status", "0")
				.add("version", EaasBuildInfo.getVersion())
				.build();

		return Admin.createResponse(Status.OK, json.toString());
	}

	@GET
	@Secured({Role.RESTRCITED})
	@Path("/user-info")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfoResponse getUserInfo()
	{
		if (authenticatedUser == null || authenticatedUser.getUsername() == null)
			return new UserInfoResponse(new BWFLAException("no user context"));

		UserInfoResponse resp = new UserInfoResponse();
		resp.setUserId(authenticatedUser.getUsername());
		resp.setFullName(authenticatedUser.getName());
		return resp;
	}


	@GET
	@Secured({Role.RESTRCITED})
	@Path("/server-log")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getServerLog()
	{
		return Response.ok()
				.entity(new File("/home/bwfla/log/eaas.log"))
				.header("Content-Disposition", "attachment; filename=\"eaas.log\"")
				.build();
	}

	@GET
	@Secured({Role.RESTRCITED})
	@Path("/usage-log")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getUsageLog()
	{
		return Response.ok()
				.entity(new File(USAGE_LOG_PATH))
				.header("Content-Disposition", "attachment; filename=\"sessions.csv\"")
				.build();
	}

	@DELETE
	@Secured({Role.RESTRCITED})
	@Path("/usage-log")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetUsageLog()
	{
		try {
			try (FileChannel file = FileChannel.open(Paths.get(USAGE_LOG_PATH))) {
				file.truncate(0L);
			}
		}
		catch (Exception error) {
			return Admin.internalErrorResponse(error);
		}

		return Response.ok()
				.build();
	}

	@POST
	@Secured
	@Path("/metadata-export")
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportMetadata()
	{
		environmentRepository.export();
		return Response.ok()
				.build();
	}
}
