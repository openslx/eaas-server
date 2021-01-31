package de.bwl.bwfla.emil;

import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/operator")
@ApplicationScoped
public class OperatorProxy {

    private Operator operatorProxy = null;

    @PostConstruct
    private void init() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://operator:8000/");
        ResteasyWebTarget rtarget = (ResteasyWebTarget) target;

        this.operatorProxy = rtarget.proxy(Operator.class);
    }

    @GET
    @Secured(roles = {Role.ADMIN})
    @Path("api/v1/channels")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChannels() {
        return operatorProxy.getChannels();
    }

    @POST
    @Path("api/v1/channels/{channel}")
    @Secured(roles = {Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reload(@PathParam("channel") String channel)
    {
        return operatorProxy.reload(channel);
    }

    @GET
    @Secured(roles = {Role.ADMIN})
    @Path("api/v1/channels/{channel}/releases")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReleases(@PathParam("channel") String channel) {
        return operatorProxy.getReleases(channel);
    }

    @GET
    @Secured(roles = {Role.ADMIN})
    @Path("api/v1/channels/{channel}/releases/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatest(@PathParam("channel")String name)
    {
        return operatorProxy.getLatest(name);
    }

    @POST
    @Secured(roles = {Role.ADMIN})
    @Path("api/v1/channels/{channel}/releases/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("channel")String channel, @PathParam("version")String version) {
        return operatorProxy.update(channel, version);
    }

    @GET
    @Secured(roles = {Role.ADMIN})
    @Path("api/v1/deployment/current")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrent()
    {
        return operatorProxy.getCurrent();
    }

    @POST
    @Secured(roles = {Role.ADMIN})
    @Path("api/v1/deployment/current")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response redeployCurrent() {
        return operatorProxy.redeployCurrent();
    }

    @GET
    @Secured(roles = {Role.ADMIN})
    @Path("api/v1/deployment/previous")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPrevious() {
        return operatorProxy.getPrevious();
    }

    @POST
    @Secured(roles = {Role.ADMIN})
    @Path("api/v1/deployment/previous")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response redeployPrevious() {
        return operatorProxy.redeployPrevious();
    }
}
