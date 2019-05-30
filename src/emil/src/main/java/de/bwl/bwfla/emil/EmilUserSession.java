package de.bwl.bwfla.emil;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.JsonBuilder;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilSessionEnvironment;
import de.bwl.bwfla.emil.datatypes.UserSessionResponse;
import de.bwl.bwfla.emil.datatypes.UserSessions;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emucomp.api.AbstractDataResource;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
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
        envHelper = new EnvironmentsAdapter(imageArchive, apiAuthenticationToken);
    }

    @GET
    @Secured
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
    @Secured
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userSessionList()
    {
        List<EmilSessionEnvironment> sessions;

        sessions = userSessions.toList();
        try {
            JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
            json.beginObject();
            json.add("status", "0");
            json.name("environments");
            json.beginArray();

            for (EmilSessionEnvironment emilenv : sessions) {
                if(!emilenv.isVisible())
                    continue;

                json.beginObject();
                json.add("title", emilenv.getTitle());
                json.add("envId", emilenv.getEnvId());
                json.add("objectId", emilenv.getObjectId());
                json.add("archiveId", emilenv.getObjectArchiveId());
                json.add("userId", emilenv.getUserId());
                json.add("creationDate", emilenv.getCreationDate() + "");
                json.endObject();
            }

            json.endArray();
            json.endObject();
            json.finish();

            return Emil.createResponse(Response.Status.OK, json.toString());
        }
        catch(IOException e)
        {
            return Emil.internalErrorResponse(e);
        }
    }

    @GET
    @Secured
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
