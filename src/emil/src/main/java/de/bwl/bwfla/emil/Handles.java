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
import de.bwl.bwfla.common.services.handle.HandleClient;
import de.bwl.bwfla.emil.datatypes.rest.HandleListResponse;
import de.bwl.bwfla.emil.datatypes.rest.HandleRequest;
import de.bwl.bwfla.emil.datatypes.rest.HandleValueResponse;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;


@ApplicationScoped
@Path("/handles")
public class Handles
{
	protected static final Logger LOG = Logger.getLogger("eaas/handles");

	private HandleClient handleClient;


	@PostConstruct
	private void initialize()
	{
		try {
			this.handleClient = new HandleClient();
		}
		catch (Exception error) {
			throw new RuntimeException("Initializing handle.net client failed!", error);
		}
	}


	/* ============================= Handle API =============================== */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public HandleListResponse getHandleList()
	{
		try {
			return new HandleListResponse(handleClient.list());
		}
		catch (BWFLAException error) {
			final String message = "Server has encountered an internal error: " + error.getMessage();
			throw new InternalServerErrorException(message, error);
		}
	}

	@GET
	@Path("/{handle}")
	@Produces(MediaType.APPLICATION_JSON)
	public HandleValueResponse getHandleValue(@PathParam("handle") String handle)
	{
		try {
			return new HandleValueResponse(handleClient.resolve(handle));
		}
		catch (BWFLAException error) {
			final String message = "Server has encountered an internal error: " + error.getMessage();
			throw new InternalServerErrorException(message, error);
		}
	}

	@GET
	@Path("/{prefix}/{handle}")
	@Produces(MediaType.APPLICATION_JSON)
	public HandleValueResponse getHandleValueWithPrefix(@PathParam("prefix") String prefix, @PathParam("handle") String handle)
	{
		try {
			HandleValueResponse response = new HandleValueResponse(handleClient.resolve(handle));
			return response ;
		}
		catch (BWFLAException error) {
			final String message = "Server has encountered an internal error: " + error.getMessage();
			throw new InternalServerErrorException(message, error);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createHandle(HandleRequest request)
	{
		try {
			handleClient.create(request.getHandle(), request.getHandleValue());
			return new ResponseBuilderImpl().status(Response.Status.OK).build();
		}
		catch (BWFLAException error) {
			final String message = "Server has encountered an internal error: " + error.getMessage();
			throw new InternalServerErrorException(message, error);
		}
	}

	@POST
	@Path("/{handle}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateHandleValue(HandleRequest request)
	{
		try {
			handleClient.update(request.getHandle(), request.getValueIndex(), request.getHandleValue());
		}
		catch (BWFLAException error) {
			final String message = "Server has encountered an internal error: " + error.getMessage();
			throw new InternalServerErrorException(message, error);
		}
	}

	@POST
	@Path("{prefix}/{handle}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateHandleValueWithPrefix(HandleRequest request)
	{
		try {
			handleClient.update(request.getHandle(), request.getValueIndex(), request.getHandleValue());
		}
		catch (BWFLAException error) {
			final String message = "Server has encountered an internal error: " + error.getMessage();
			throw new InternalServerErrorException(message, error);
		}
	}

	@DELETE
	@Path("/{handle}")
	public void deleteHandle(@PathParam("handle") String handle)
	{
		try {
			handleClient.delete(handle);
		}
		catch (BWFLAException error) {
			final String message = "Server has encountered an internal error: " + error.getMessage();
			throw new InternalServerErrorException(message, error);
		}
	}

	@DELETE
	@Path("/{prefix}/{handle}")
	public void deleteHandleWithPrefix(@PathParam("prefix") String prefix, @PathParam("handle") String handle)
	{
		try {
			handleClient.delete(handle);
		}
		catch (BWFLAException error) {
			final String message = "Server has encountered an internal error: " + error.getMessage();
			throw new InternalServerErrorException(message, error);
		}
	}
}
