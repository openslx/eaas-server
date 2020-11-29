package de.bwl.bwfla.historicbuilds;

import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.envproposer.api.ProposalRequest;
import de.bwl.bwfla.historicbuilds.api.HistoricRequest;
import de.bwl.bwfla.historicbuilds.api.HistoricResponse;
import de.bwl.bwfla.restutils.ResponseUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/historic-builds/api/v1")
public class HistoricBuildStarter {

    private static final Logger LOG = Logger.getLogger("HISTORIC-BUILDS");

    @GET
    @Path("/hello")
    @Secured(roles = {Role.PUBLIC})
    @Produces(MediaType.TEXT_PLAIN)
    public Response historicHello() {
        LOG.info("Someone sent a hello request to the historic build API.");
        return ResponseUtils.createResponse(Status.OK, "Hello from the historic builds API!");
    }

    @POST
    @Path("/build")
    @Secured(roles = {Role.PUBLIC})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postProposal(HistoricRequest request) {

        HistoricRequest.SoftwareHeritageRequest swhRequest = request.getSwhRequest();
        HistoricRequest.BuildToolchainRequest buildToolchainRequest = request.getBuildToolchainRequest();

        LOG.info("Someone sent a build request to the historic build API: returning incoming json!");
        LOG.info("Revision ID:" + swhRequest.getRevisionId());
        LOG.info("Mail:" + buildToolchainRequest.getMail());

        return ResponseUtils.createResponse(Status.OK, request);
    }

}
