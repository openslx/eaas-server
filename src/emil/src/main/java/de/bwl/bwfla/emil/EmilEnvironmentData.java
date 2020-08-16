package de.bwl.bwfla.emil;

import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.datatypes.identification.OperatingSystems;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.emil.datatypes.rest.ReplicateImagesResponse;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.emucomp.api.*;

import de.bwl.bwfla.common.exceptions.BWFLAException;

// TODO: remove this file!

@Deprecated
@Path("EmilEnvironmentData")
@ApplicationScoped
public class EmilEnvironmentData
{
	@Inject
	private EnvironmentRepository envrepo = null;

	@Inject
	@AuthenticatedUser
	private UserContext authenticatedUser = null;


	@Secured(roles={Role.PUBLIC})
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEnvironments(@Context final HttpServletResponse response) {
		return envrepo.environments()
				.list(false, true);
	}

	@Secured(roles={Role.PUBLIC})
	@GET
	@Path("/{envId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEnvironment(@PathParam("envId") String envId, @Context final HttpServletResponse response) {
		return envrepo.environments()
				.get(envId);
	}

	/**
	 * Creates EmilEnvironments for all environments found at the image archive.
	 *
	 * @return
	 */
	@Secured(roles={Role.PUBLIC})
	@GET
	@Path("/init")
	@Produces(MediaType.APPLICATION_JSON)
	public Response init() {
		return envrepo.actions()
				.prepare();
	}


	@Secured(roles={Role.PUBLIC})
	@GET
	@Path("/objectDependencies")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getObjectDependencies(@QueryParam("envId") String envId) {
		return envrepo.environments()
				.getObjectDependencies(envId);
	}

	@Secured(roles={Role.RESTRICTED})
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(EnvironmentDeleteRequest desc) throws JAXBException {
		return envrepo.environments()
				.delete(desc.getEnvId(), desc);
	}

	@Secured(roles={Role.RESTRICTED})
	@GET
	@Path("/getDatabaseContent")
	@Produces(MediaType.APPLICATION_JSON)
	public <T extends JaxbType> Response getDatabaseContent(@QueryParam("type") String type, @QueryParam("className") String className) {
		return envrepo.getDatabaseContent(type, className);
	}

	@Secured(roles={Role.RESTRICTED})
	@POST
	@Path("/createEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createEnvironment(EnvironmentCreateRequest envReq) {
		return envrepo.environments()
				.create(envReq);
	}

	@Secured(roles={Role.RESTRICTED})
	@GET
	@Path("/getEnvironmentTemplates")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 *
	 * 		{"status": "0", "systems": [{"id": "abc", "label": "Windows XP
	 *         SP1", "native_config": "test", "properties": [{"name":
	 *         "Architecture", "value": "x86_64"}, {"name": "Fun Fact", "value":
	 *         "In 1936, the Russians made a computer that ran on water"}]}]}
	 */
	public Response getEnvironmentTemplates() {
		return envrepo.templates()
				.list("oldStyle");
	}

	@Secured(roles={Role.RESTRICTED})
	@GET
	@Path("/getPatches")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 *
	 * 		{"status": "0", "systems": [{"id": "abc", "label": "Windows XP
	 *         SP1", "native_config": "test", "properties": [{"name":
	 *         "Architecture", "value": "x86_64"}, {"name": "Fun Fact", "value":
	 *         "In 1936, the Russians made a computer that ran on water"}]}]}
	 */
	public List<GeneralizationPatch> getPatches() throws BWFLAException, JAXBException {
		return envrepo.patches()
				.list();
	}

	/**
	 * Updates the description of a specified Emil environment. This method
	 * expects a JSON object containing the description changes:
	 * <p>
	 * <pre>
	 * {
	 *      "envId": &ltEnvironment's ID&gt,
	 *      "title": "New title",
	 *      "description": "New description"
	 * }
	 * </pre>
	 * <p>
	 * When an internal error occurs, a JSON response containing the
	 * corresponding message will be returned:
	 * <p>
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 *
	 * @param desc A JSON object containing description changes.
	 * @return A JSON object containing the result message.
	 */
	@Secured(roles={Role.RESTRICTED})
	@POST
	@Path("/updateDescription")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDescription(UpdateEnvironmentDescriptionRequest desc) {
		return envrepo.environments()
				.update(desc.getEnvId(), desc);
	}

	@Secured(roles={Role.RESTRICTED})
	@GET
	@Path("/defaultEnvironments")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> defaultEnvironments()
	{
		return envrepo.defaultEnvironments()
				.list();
	}

	@GET
	@Path("/defaultEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	public DefaultEnvironmentResponse defaultEnvironment(@QueryParam("osId") String osId) {
		return envrepo.defaultEnvironments()
				.get(osId);
	}

	@Secured(roles={Role.RESTRICTED})
	@GET
	@Path("/setDefaultEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	public EmilResponseType setDefaultEnvironment(@QueryParam("osId") String osId, @QueryParam("envId") String envId) {
		return envrepo.defaultEnvironments()
				.set(osId, envId);
	}

	@Secured(roles={Role.RESTRICTED})
	@POST
	@Path("forkRevision")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response forkRevision(ForkRevisionRequest req) {
		return envrepo.environments()
				.revisions(req.getId())
				.create();
	}

	@Secured(roles={Role.RESTRICTED})
	@POST
	@Path("revertRevision")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public synchronized Response revertRevision(RevertRevisionRequest req) {
		return envrepo.environments()
				.revisions(req.getCurrentId())
				.revert(req.getRevId());
	}

	@Secured(roles={Role.PUBLIC})
	@GET
	@Path("operatingSystemMetadata")
	@Produces(MediaType.APPLICATION_JSON)
	public OperatingSystems getOperatingSystemMetadata()
	{
		return envrepo.getOperatingSystemMetadata();
	}

	@Secured(roles={Role.RESTRICTED})
	@GET
	@Path("/sync")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sync() {
		return envrepo.actions()
				.sync();
	}

	@Secured(roles={Role.RESTRICTED})
	@POST
	@Path("/importImage")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse importImage(ImportImageRequest imageReq) {
		return envrepo.actions()
				.importImage(imageReq);
	}

	@Secured(roles={Role.PUBLIC})
	@GET
	@Path("/getNameIndexes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public ImageNameIndex getNameIndexes() throws BWFLAException, JAXBException {
        return envrepo.getNameIndexes();
    }

	@Secured(roles={Role.RESTRICTED})
	@POST
	@Path("/replicateImage")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ReplicateImagesResponse replicateImage(ReplicateImagesRequest replicateImagesRequest) {
		return envrepo.actions()
				.replicateImage(replicateImagesRequest);
	}

	@POST
	@Path("/export")
	@Secured(roles={Role.RESTRICTED})
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse export(ExportRequest exportRequest) {
		return envrepo.environments()
				.export(exportRequest.getEnvId(), exportRequest);
	}
}
