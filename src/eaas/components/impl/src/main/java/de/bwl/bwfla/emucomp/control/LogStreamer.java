package de.bwl.bwfla.emucomp.control;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.components.Tail;
import de.bwl.bwfla.emucomp.components.emulators.EmulatorBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@ApplicationScoped
@Path("/api/v1/components")
public class LogStreamer {

    @Inject
	private NodeManager nodemgr = null;

    @GET
	@Path("/{componentId}/stdout")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getStdout(@PathParam("componentId") String componentId)
	{
		try {
			final AbstractEaasComponent component = nodemgr.getComponentById(componentId);
			if(!(component instanceof EmulatorBean))
			    return Response.status(Response.Status.NOT_FOUND).build();

			EmulatorBean bean = (EmulatorBean) component;
			Tail stdout = bean.getEmulatorStdOut();
			if(stdout == null)
			    return Response.status(Response.Status.NOT_FOUND).build();

			return Response.status(Response.Status.OK)
						.entity(stdout.getStream()).build();
		}
		catch (BWFLAException | IOException error) {
			throw new NotFoundException("Component not found: " + componentId);
		}
	}

	@GET
	@Path("/{componentId}/stderr")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getStderr(@PathParam("componentId") String componentId)
	{
		try {
			final AbstractEaasComponent component = nodemgr.getComponentById(componentId);
			if(!(component instanceof EmulatorBean))
			    return Response.status(Response.Status.NOT_FOUND).build();

			EmulatorBean bean = (EmulatorBean) component;
			Tail stderr = bean.getEmulatorStdErr();
			if(stderr == null)
			    return Response.status(Response.Status.NOT_FOUND).build();

			return Response.status(Response.Status.OK)
						.entity(stderr.getStream()).build();
		}
		catch (BWFLAException | IOException error) {
			throw new NotFoundException("Component not found: " + componentId);
		}
	}
}
