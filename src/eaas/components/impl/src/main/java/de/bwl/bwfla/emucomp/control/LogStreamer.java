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
import javax.ws.rs.core.StreamingOutput;
import java.io.*;

@ApplicationScoped
@Path("/components")
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
			final Tail stdout = bean.getEmulatorStdOut();
			if(stdout == null)
			    return Response.status(Response.Status.NOT_FOUND).build();

			StreamingOutput stream = out -> {
				final var buffer = new byte[8 * 1024];
				final var source = stdout.getStream();
				while (source.read(buffer, 0, 1) > 0) {
					final int explen = Math.min(source.available(), buffer.length - 1);
					final int curlen = 1 + source.read(buffer, 1, explen);
					out.write(buffer, 0, curlen);
					out.flush();
				}
				stdout.cleanup();
			};
			return Response.status(Response.Status.OK)
						.entity(stream).build();
		}
		catch (BWFLAException error) {
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

			StreamingOutput stream = out -> {
				final var buffer = new byte[8 * 1024];
				final var source = stderr.getStream();
				while (source.read(buffer, 0, 1) > 0) {
					final int explen = Math.min(source.available(), buffer.length - 1);
					final int curlen = 1 + source.read(buffer, 1, explen);
					out.write(buffer, 0, curlen);
					out.flush();
				}
				stderr.cleanup();
			};
			return Response.status(Response.Status.OK)
						.entity(stream).build();
		}
		catch (BWFLAException error) {
			throw new NotFoundException("Component not found: " + componentId);
		}
	}
}
