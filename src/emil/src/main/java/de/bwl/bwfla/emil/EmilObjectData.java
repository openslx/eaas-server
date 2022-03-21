package de.bwl.bwfla.emil;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// TODO: remove this!

@Deprecated
@Path("/objects")
@ApplicationScoped
public class EmilObjectData extends EmilRest {
	@Inject
	private ObjectRepository objrepo = null;


	@Secured(roles = {Role.RESTRICTED})
	@GET
	@Path("/sync")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sync()
	{
		return objrepo.actions()
				.sync();
	}

	@Secured(roles = {Role.RESTRICTED})
	@POST
	@Path("/syncObjects")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse syncObjects(SyncObjectRequest req)
	{
		return objrepo.archives()
				.sync(req.getArchive(), req);
	}

	@Secured(roles = {Role.RESTRICTED})
	@GET
	@Path("/objectImportTaskState")
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse getObjectImportTaskState(@QueryParam("taskId") String taskId)
	{
		return objrepo.tasks()
				.get(taskId);
	}

	/**
	 * Looks up and returns a list of all digital objects
	 * <p>
	 *
	 * @param archiveId Archive to be used. Default value: "default"
	 * @return
	 *
	 * @HTTP 500 if archive is not found
	 *
	 * @documentationType de.bwl.bwfla.emil.datatypes.ObjectListItem
	 */
	@Secured(roles = {Role.RESTRICTED})
	@GET
	@Path("/{objectArchive}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response list(@PathParam("objectArchive") String archiveId,
								   @Context final HttpServletResponse response)
	{
		return objrepo.archives()
				.objects(archiveId)
				.list(response);
	}

	@Secured(roles = {Role.RESTRICTED})
	@DELETE
	@Path("/{objectArchive}/{objectId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("objectId") String objectId,
						   @PathParam("objectArchive") String archiveId) {
		return objrepo.archives()
				.objects(archiveId)
				.delete(objectId);
	}


	/**
	 * Looks up and returns metadata for specified object.
	 * 
	 * @param objectId The object's ID to look up metadata for.
	 * @return A JSON object with object's metadata when found, else an error message.
	 */
	@Secured(roles = {Role.RESTRICTED})
	@GET
	@Path("/{objectArchive}/{objectId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MediaDescriptionResponse mediaDescription(@PathParam("objectId") String objectId,
												     @PathParam("objectArchive") String archiveId)
	{
		return objrepo.archives()
				.objects(archiveId)
				.get(objectId);
	}

	@Secured(roles = {Role.RESTRICTED})
	@GET
	@Path("/archives")
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectArchivesResponse getArchives()
	{
		return objrepo.archives()
				.list();
	}

	@Secured(roles = {Role.RESTRICTED})
	@POST
	@Path("/import")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse importObject(ImportObjectRequest req)
	{
		return objrepo.archives()
				.objects(req.getObjectArchive())
				.importObject(req);
	}

	public ObjectArchiveHelper helper() {
		return objrepo.helper();
	}

	FileCollection getFileCollection(String archiveId, String objectId) throws BWFLAException
	{
		return objrepo.getFileCollection(archiveId, objectId);
	}
}
