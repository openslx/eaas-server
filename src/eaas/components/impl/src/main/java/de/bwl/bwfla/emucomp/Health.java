package de.bwl.bwfla.emucomp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/health")
public class Health {
    @GET
    public Response health() {
        return Response.status(Status.OK).build();
    }
}
