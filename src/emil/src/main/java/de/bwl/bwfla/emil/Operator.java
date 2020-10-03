package de.bwl.bwfla.emil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/operator")
public interface Operator {

    /**
     * ### List all configured channels
     * GET http://localhost:8000/operator/api/v1/channels
     * accepts: application/json
     *
     * ### Refresh (reload/download) channels's metadata
     * POST http://localhost:8000/operator/api/v1/channels/example
     * accepts: application/json
     *
     * ### List channels's releases
     * GET http://localhost:8000/operator/api/v1/channels/example/releases
     * accepts: application/json
     *
     * ### Get channels's latest release
     * GET http://localhost:8000/operator/api/v1/channels/example/releases/latest
     * accepts: application/json
     *
     * ### Update to release
     * POST http://localhost:8000/operator/api/v1/channels/example/releases/2.0
     * accepts: application/json
     *
     * ### Get current release
     * GET http://localhost:8000/operator/api/v1/deployment/current
     * accepts: application/json
     *
     * ### Re-install current release
     * POST http://localhost:8000/operator/api/v1/deployment/current
     * accepts: application/json
     *
     * ### Get previous release
     * GET http://localhost:8000/operator/api/v1/deployment/previous
     * accepts: application/json
     *
     * ### Re-install previous release
     * POST http://localhost:8000/operator/api/v1/deployment/previous
     * accepts: application/json
     */

    @GET
    @Path("api/v1/channels")
    @Produces(MediaType.APPLICATION_JSON)
    Response getChannels();

    @POST
    @Path("api/v1/channels/{channel}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response reload(@PathParam("channel")String channel);

    @GET
    @Path("api/v1/channels/{channel}/releases")
    @Produces(MediaType.APPLICATION_JSON)
    Response getReleases(@PathParam("channel")String channel);

    @GET
    @Path("api/v1/channels/{channel}/releases/latest")
    @Produces(MediaType.APPLICATION_JSON)
    Response getLatest(@PathParam("channel")String channel);

    @POST
    @Path("api/v1/channels/{channel}/releases/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response update(@PathParam("channel")String channel, @PathParam("version")String version);

    @GET
    @Path("api/v1/deployment/current")
    @Produces(MediaType.APPLICATION_JSON)
    Response getCurrent();

    @POST
    @Path("api/v1/deployment/current")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response redeployCurrent();

    @GET
    @Path("api/v1/deployment/previous")
    @Produces(MediaType.APPLICATION_JSON)
    Response getPrevious();

    @POST
    @Path("api/v1/deployment/previous")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response redeployPrevious();
}
