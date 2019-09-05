package de.bwl.bwfla.emil;

import de.bwl.bwfla.api.objectarchive.DigitalObjectMetadata;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Role;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import de.bwl.bwfla.emil.utils.TaskManager;
import de.bwl.bwfla.emil.utils.tasks.ImportObjectTask;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;
import org.apache.tamaya.inject.api.Config;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/objects")
@ApplicationScoped
public class EmilObjectData extends EmilRest {

    private static SoftwareArchiveHelper swHelper;

	@Inject
	@AuthenticatedUser
	private UserContext authenticatedUser;

	@Inject
	private EmilEnvironmentRepository environmentRepository;

	@Inject
	@Config(value="commonconf.serverdatadir")
	private String serverdatadir;

	@Inject
	private TaskManager taskManager;

	private static Set<String> objArchives;

	private final String USER_ARCHIVE_PREFIX = "user_archive";

	static private ObjectArchiveHelper objHelper;

	@Inject
	@Config(value = "ws.objectarchive")
	private String objectArchive;

	@Inject
	@Config(value="objectarchive.userarchive")
	private String userArchiveBase;

	@PostConstruct
    private void init() {

		objHelper = new ObjectArchiveHelper(objectArchive);
        swHelper = new SoftwareArchiveHelper(softwareArchive);
		try {
			objArchives = new HashSet<>(objHelper.getArchives());
		} catch (BWFLAException e) {
			e.printStackTrace();
		}
	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/sync")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sync()
	{
		try {
			objHelper.sync();
		} catch (BWFLAException e) {
			return Emil.internalErrorResponse(e);
		}
		return Emil.successMessageResponse("sync archives successful");
	}

	@Secured({Role.RESTRCITED})
	@POST
	@Path("/syncObjects")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse syncObjects(SyncObjectRequest req)
	{
		if(req.getArchive() == null || req.getObjectIDs() == null)
			return new TaskStateResponse(new BWFLAException("invalid arguments"));
		
		try {
			return new TaskStateResponse(objHelper.sync(req.getArchive(), req.getObjectIDs()));
		} catch (BWFLAException e) {
			return new TaskStateResponse(e);
		}
	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/objectImportTaskState")
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse getObjectImportTaskState(@QueryParam("taskId") String taskId)
	{
		try {
			return new TaskStateResponse(objHelper.getTaskState(taskId));
		} catch (BWFLAException e) {
			return new TaskStateResponse(e);
		}
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
	 * @documentationType de.bwl.bwfla.emil.datatypes.ObjectListResponse
	 */
	@Secured({Role.RESTRCITED})
	@GET
	@Path("/{objectArchive}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response list(@PathParam("objectArchive") String archiveId,
								   @Context final HttpServletResponse response)
	{
		if(archiveId == null || archiveId.equals("default")) {
			try {
				archiveId = manageUserCtx(archiveId);
			} catch (BWFLAException e) {
				archiveId = "default";
			}
		}

		try {
			List<String> objects = objHelper.getObjectList(archiveId);
			if(objects == null) {
				LOG.warning("objects null");
				return Response.status(Response.Status.BAD_REQUEST).entity("Objects are null").build();
			}

			ArrayList<ObjectListItem> objList = new ArrayList<>();

			for(String id : objects)
			{
				SoftwarePackage software = swHelper.getSoftwarePackageById(id);
				if(software != null)
					continue;

				DigitalObjectMetadata md = objHelper.getObjectMetadata(archiveId, id);
				ObjectListItem item = new ObjectListItem(id);
				item.setTitle(md.getTitle());
				item.setArchiveId(archiveId);

//				try {
//					item.setDescription(archive.getClassificationResultForObject(id).getUserDescription());
//				} catch (NoSuchElementException e){
//					LOG.info("no cache for " + id + ". Getting default description");
//					item.setDescription(md.getDescription());
//				}

				item.setThumbnail(md.getThumbnail());
				item.setSummary(md.getSummary());
				objList.add(item);
			}
			return Response.status(Response.Status.OK).entity(objList).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			LOG.severe("I've got an exception in list");
			throw new BadRequestException(Response
					.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorInformation(e.getMessage()))
					.build());
		}
	}

	@Secured({Role.RESTRCITED})
	@DELETE
	@Path("/{objectArchive}/{objectId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("objectId") String objectId,
						   @PathParam("objectArchive") String archiveId) {
		if(archiveId == null || archiveId.equals("default")) {
			try {
				archiveId = manageUserCtx(archiveId);
			} catch (BWFLAException e) {
				archiveId = "default";
			}
		}

		try {
			objHelper.delete(archiveId, objectId);
			return Response.status(Response.Status.OK).build();
		}
		catch (BWFLAException e)
		{
			throw new BadRequestException(Response
					.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorInformation(e.getMessage()))
					.build());
		}
	}


	/**
	 * Looks up and returns metadata for specified object.
	 * 
	 * @param objectId The object's ID to look up metadata for.
	 * @return A JSON object with object's metadata when found, else an error message.
	 */
	@Secured({Role.RESTRCITED})
	@GET
	@Path("/{objectArchive}/{objectId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MediaDescriptionResponse mediaDescription(@PathParam("objectId") String objectId,
												     @PathParam("objectArchive") String archiveId)
	{
		if(archiveId == null || archiveId.equals("default")) {
			try {
				archiveId = manageUserCtx(archiveId);
			} catch (BWFLAException e) {
				archiveId = "default";
			}
		}

		MediaDescriptionResponse resp = new MediaDescriptionResponse();
		try {
			FileCollection fc = null;
			try {
				fc = getFileCollection(archiveId, objectId);
			} catch (BWFLAException e) {
				return resp;
			}

			resp.setMediaItems(fc);
			resp.setMetadata(objHelper.getObjectMetadata(archiveId, objectId));
			return resp;
		}
		catch (BWFLAException exception) {
			return new MediaDescriptionResponse(new BWFLAException(exception));
		}
	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/archives")
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectArchivesResponse getArchives()
	{
		try {

			String defaultArchive = manageUserCtx("default");

			objArchives = new HashSet<>(objHelper.getArchives());
			List<String> archives = objArchives.stream().filter(
					e -> !(e.startsWith(USER_ARCHIVE_PREFIX) && !e.equals(defaultArchive))
			).filter(
					e -> !e.equals("default")
			).filter(
					e -> !(!defaultArchive.equals("default") && e.equals("zero conf")) // remove zero conf archive if usercontext is available
			).collect(Collectors.toList());
			ObjectArchivesResponse response = new ObjectArchivesResponse();
			response.setArchives(archives);
			return response;
		} catch (BWFLAException e) {
			return new ObjectArchivesResponse(e);
		}
	}

	@Secured({Role.RESTRCITED})
	@POST
	@Path("/import")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse importObject(ImportObjectRequest req)
	{
		String archiveId = req.getObjectArchive();
		if(archiveId == null || archiveId.equals("default")) {
			try {
				archiveId = manageUserCtx(archiveId);
			} catch (BWFLAException e) {
				archiveId = "default";
			}
		}

		return new TaskStateResponse(taskManager.submitTask(new ImportObjectTask(req, archiveId, objHelper)));
	}


	private String manageUserCtx(String archiveId) throws BWFLAException {
		if(authenticatedUser != null && authenticatedUser.getUsername() != null) {
			LOG.info("got user context: " + authenticatedUser.getUsername());
			archiveId = USER_ARCHIVE_PREFIX + authenticatedUser.getUsername();
			if (!objArchives.contains(archiveId)) {
				objHelper.registerUserArchive(archiveId);
				objArchives = new HashSet<>(objHelper.getArchives());
			}
			return USER_ARCHIVE_PREFIX + authenticatedUser.getUsername();
		}
		return archiveId;
	}

	FileCollection getFileCollection(String archiveId, String objectId)
			throws  BWFLAException {
		if(archiveId == null || archiveId.equals("default")) {
			try {
				archiveId = manageUserCtx(archiveId);
			} catch (BWFLAException e) {
				archiveId = "default";
			}
		}

		FileCollection fc = objHelper.getObjectReference(archiveId, objectId);
		if (fc == null)
			throw new BWFLAException("Returned FileCollection is null for '" + archiveId + "/" + objectId + "'!");
		return fc;
	}

	ObjectArchiveHelper helper() {
		return objHelper;
	}
}
