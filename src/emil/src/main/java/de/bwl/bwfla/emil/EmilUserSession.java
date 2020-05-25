package de.bwl.bwfla.emil;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.EmilSessionEnvironment;
import de.bwl.bwfla.emil.datatypes.UserSessionResponse;
import de.bwl.bwfla.emil.datatypes.UserSessions;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("EmilUserSession")
@ApplicationScoped
public class EmilUserSession extends EmilRest {

    private EnvironmentsAdapter envHelper;

    @Inject
    private EmilEnvironmentRepository emilEnvRepo;

    @Inject
    private UserSessions userSessions;

    @PostConstruct
    private void initialize() {
        envHelper = new EnvironmentsAdapter(imageArchive);
    }

    @GET
    @Secured(roles={Role.RESTRCITED})
    @Path("delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@QueryParam("sessionId") String id) {

        LOG.info("deleting " + id);

        try {
            emilEnvRepo.delete(id, true, true);
        } catch (BWFLAException e1 ) {
            return Emil.internalErrorResponse(e1);
        }

        return Emil.successMessageResponse("delete success!");
    }


    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userSessionList()
    {
        List<EmilSessionEnvironment> sessions;

        sessions = userSessions.toList();
        try {
            final JsonArrayBuilder environments = Json.createArrayBuilder();
            for (EmilSessionEnvironment emilenv : sessions) {
                if(!emilEnvRepo.isEnvironmentVisible(emilenv))
                    continue;

                final JsonObjectBuilder environment = Json.createObjectBuilder()
                        .add("title", emilenv.getTitle())
                        .add("envId", emilenv.getEnvId())
                        .add("objectId", emilenv.getObjectId())
                        .add("archiveId", emilenv.getObjectArchiveId())
                        .add("userId", emilenv.getUserId())
                        .add("creationDate", emilenv.getCreationDate() + "");

                environments.add(environment);
            }

            final JsonObject json = Json.createObjectBuilder()
                    .add("status", "0")
                    .add("environments", environments)
                    .build();

            return Emil.createResponse(Response.Status.OK, json.toString());
        }
        catch(Exception e)
        {
            return Emil.internalErrorResponse(e);
        }
    }

    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/session")
    @Produces(MediaType.APPLICATION_JSON)
    public UserSessionResponse getUserSession(@QueryParam("userId") String userId, @QueryParam("objectId") String objectId) {
        EmilSessionEnvironment session = emilEnvRepo.getUserSession(userId, objectId);
        UserSessionResponse response = new UserSessionResponse();
        if(session != null)
            response.setEnvId(session.getEnvId());
        return response;

    }
}
