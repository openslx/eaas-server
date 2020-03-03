package de.bwl.bwfla.emil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.bwl.bwfla.emil.datatypes.rest.UserInfoResponse;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;


// TODO: remove this file!

@Deprecated
@Path("Emil")
@ApplicationScoped
public class Emil extends EmilRest
{
	/* ### ADMIN Interfaces ### */

	@Inject
	@AuthenticatedUser
	private UserContext authenticatedUser;

	@Inject
	private Admin admin;

	@GET
	@Secured(roles = {Role.PUBLIC})
	@Path("/buildInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response buildInfo()
	{
		return admin.getBuildInfo();
	}

	@GET
	@Secured(roles = {Role.RESTRCITED})
	@Path("/userInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfoResponse userInfo() {
		return admin.getUserInfo();
	}

	@GET
	@Secured(roles = {Role.RESTRCITED})
	@Path("/serverLog")
	@Produces(MediaType.TEXT_PLAIN)
	public Response serverLog()
	{
		return admin.getServerLog();
	}

	@GET
	@Secured(roles = {Role.RESTRCITED})
	@Path("/resetUsageLog")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetUsageLog()
	{
		return admin.resetUsageLog();
	}

	@GET
	@Secured(roles = {Role.RESTRCITED})
	@Path("/usageLog")
	@Produces(MediaType.TEXT_PLAIN)
	public Response usageLog()
	{
		return admin.getUsageLog();
	}

	@GET
	@Secured(roles = {Role.RESTRCITED})
	@Path("/exportMetadata")
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportMetadata()
	{
		return admin.exportMetadata();
	}
}
