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

package de.bwl.bwfla.eaas.cluster.tenant;

import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;


@Path("/tenants")
@ApplicationScoped
public class TenantAPI
{
	@Inject
	private TenantManager tenants = null;


	// ========== Admin API ==============================

	@GET
	@Secured({Role.ADMIN})
	@Produces(MediaType.APPLICATION_JSON)
	public Response list()
	{
		final Collection<String> ids = tenants.list();
		return Response.ok(ids, MediaType.APPLICATION_JSON_TYPE)
				.build();
	}

	@POST
	@Secured({Role.ADMIN})
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(TenantConfig config)
	{
		tenants.add(config);
		return Response.ok()
				.build();
	}

	@DELETE
	@Secured({Role.ADMIN})
	@Path("/{name}")
	public Response unregister(@PathParam("name") String name)
	{
		if (!tenants.remove(name))
			throw new NotFoundException("Tenant not found: " + name);

		return Response.ok()
				.build();
	}

	@GET
	@Secured({Role.ADMIN})
	@Path("/{name}/quota")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQuota(@PathParam("name") String name)
	{
		try {
			final Tenant.Quota quota = tenants.quota(name);
			return Response.ok(quota, MediaType.APPLICATION_JSON_TYPE)
					.build();
		}
		catch (Exception error) {
			throw new NotFoundException("Tenant not found: " + name);
		}
	}

	@POST
	@Secured({Role.ADMIN})
	@Path("/{name}/quota")
	public Response setQuota(@PathParam("name") String name, ResourceSpec quota)
	{
		try {
			tenants.update(name, quota);
		}
		catch (Exception error) {
			throw new NotFoundException("Tenant not found: " + name);
		}

		return Response.ok()
				.build();
	}
}
