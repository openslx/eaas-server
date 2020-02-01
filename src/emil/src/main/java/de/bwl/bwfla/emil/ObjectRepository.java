/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.emil;

import de.bwl.bwfla.api.objectarchive.DigitalObjectMetadata;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.ErrorInformation;
import de.bwl.bwfla.emil.datatypes.ObjectListItem;
import de.bwl.bwfla.emil.datatypes.rest.ImportObjectRequest;
import de.bwl.bwfla.emil.datatypes.rest.MediaDescriptionResponse;
import de.bwl.bwfla.emil.datatypes.rest.ObjectArchivesResponse;
import de.bwl.bwfla.emil.datatypes.rest.SyncObjectRequest;
import de.bwl.bwfla.emil.datatypes.rest.TaskStateResponse;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Role;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import de.bwl.bwfla.emil.tasks.ImportObjectTask;
import de.bwl.bwfla.emil.utils.TaskManager;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;


@ApplicationScoped
@Path("/object-repository")
public class ObjectRepository extends EmilRest
{

	private SoftwareArchiveHelper swHelper;
	private ObjectArchiveHelper objHelper;

	@Inject
	@AuthenticatedUser
	private UserContext userctx = null;

	@Inject
	private TaskManager taskmgr = null;

	@Inject
	@Config(value = "ws.objectarchive")
	private String objectArchive = null;

	private Set<String> objArchives;

	@Inject
	@Config(value="objectarchive.default_archive")
	private String defaultArchive;

	@Inject
	@Config(value="objectarchive.user_archive_prefix")
	private String USER_ARCHIVE_PREFIX;


	@PostConstruct
    private void initialize()
	{
		try {
			objHelper = new ObjectArchiveHelper(objectArchive);
			swHelper = new SoftwareArchiveHelper(softwareArchive);
			objArchives = new HashSet<>(objHelper.getArchives());
		}
		catch (BWFLAException error) {
			LOG.log(Level.SEVERE, "Initializing object-repository failed!", error);
		}
	}

	ObjectArchiveHelper helper()
	{
		return objHelper;
	}

	FileCollection getFileCollection(String archiveId, String objectId) throws BWFLAException
	{
		if (archiveId == null || archiveId.equals(defaultArchive)) {
			try {
				archiveId = this.manageUserCtx(defaultArchive);
			}
			catch (BWFLAException error) {
				archiveId = defaultArchive;
			}
		}

		FileCollection fc = objHelper.getObjectReference(archiveId, objectId);
		if (fc == null)
			throw new BWFLAException("Returned FileCollection is null for '" + archiveId + "/" + objectId + "'!");

		return fc;
	}


	// ========== Public API ==============================

	@Path("/actions")
	public Actions actions()
	{
		return new Actions();
	}

	@Path("/archives")
	public Archives archives()
	{
		return new Archives();
	}

	@Path("/tasks")
	public Tasks tasks()
	{
		return new Tasks();
	}


	// ========== Subresources ==============================

	public class Actions
	{
		@GET
		@Path("/sync")
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public Response sync()
		{
			try {
				objHelper.sync();
			}
			catch (BWFLAException error) {
				return Emil.internalErrorResponse(error);
			}

			return Emil.successMessageResponse("Archives were synced successfully");
		}
	}


	public class Archives
	{
		/** List all available object-archives. */
		@GET
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public ObjectArchivesResponse list()
		{
			try {
				// FIXME: do we need to refresh the list of archives here?
				objArchives = new HashSet<>(objHelper.getArchives());

				final String _defaultArchive = ObjectRepository.this.manageUserCtx(defaultArchive);
				final List<String> archives = objArchives.stream()
						.filter(e -> !(e.startsWith(USER_ARCHIVE_PREFIX) && !e.equals(_defaultArchive)))
						.filter(e -> !e.equals("default"))
						// remove zero conf archive if usercontext is available
						.filter(e -> !(!_defaultArchive.equals(defaultArchive) && e.equals("zero conf")))
						.collect(Collectors.toList());

				ObjectArchivesResponse response = new ObjectArchivesResponse();
				response.setArchives(archives);
				return response;
			}
			catch (BWFLAException error) {
				return new ObjectArchivesResponse(error);
			}
		}

		@Path("/{archiveId}/objects")
		public Objects objects(@PathParam("archiveId") String archiveId)
		{
			return new Objects(archiveId);
		}

		@POST
		@Secured({Role.RESTRCITED})
		@Path("/{archiveId}/actions/sync")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public TaskStateResponse sync(@PathParam("archiveId") String archiveId, SyncObjectRequest request)
		{
			if (request.getArchive() == null || request.getObjectIDs() == null)
				return new TaskStateResponse(new BWFLAException("invalid arguments"));

			try {
				return new TaskStateResponse(objHelper.sync(archiveId, request.getObjectIDs()));
			}
			catch (BWFLAException error) {
				LOG.log(Level.WARNING, "Synchronizing objects failed!", error);
				return new TaskStateResponse(error);
			}
		}
	}


	public class Objects
	{
		private final String archiveId;

		public Objects(String archiveId)
		{
			this.archiveId = this.lookupArchiveId(archiveId);
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
		@GET
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public Response list(@Context final HttpServletResponse response)
		{
			LOG.info("Listing all digital objects in archive '" + archiveId + "'...");

			try {
				final List<String> objects = objHelper.getObjectList(archiveId);
				if (objects == null) {
					final String message = "No objects found in archive '" + archiveId + "'!";
					LOG.warning(message);
					return Response.status(Response.Status.BAD_REQUEST)
							.entity(message)
							.build();
				}

				final ArrayList<ObjectListItem> objList = new ArrayList<>();
				for (String id : objects) {
					SoftwarePackage software = swHelper.getSoftwarePackageById(id);
					if (software != null)
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

				return Response.ok()
						.entity(objList)
						.build();
			}
			catch (Exception error) {
				LOG.log(Level.WARNING, "Listing digital objects failed!", error);
				throw new BadRequestException(Response
						.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorInformation(error.getMessage()))
						.build());
			}
		}

		@POST
		@Secured({Role.RESTRCITED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public TaskStateResponse importObject(ImportObjectRequest req)
		{
			final ImportObjectTask task = new ImportObjectTask(req, archiveId, objHelper);
			return new TaskStateResponse(taskmgr.submitTask(task));
		}

		/**
		 * Looks up and returns metadata for specified object.
		 *
		 * @param objectId The object's ID to look up metadata for.
		 * @return A JSON object with object's metadata when found, else an error message.
		 */
		@GET
		@Path("/{objectId}")
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public MediaDescriptionResponse get(@PathParam("objectId") String objectId)
		{
			LOG.info("Looking up digital object '" + objectId + "'...");

			final MediaDescriptionResponse resp = new MediaDescriptionResponse();
			try {
				final FileCollection fc = objHelper.getObjectReference(archiveId, objectId);
				if (fc == null)
					return resp;

				resp.setMediaItems(fc);
				resp.setMetadata(objHelper.getObjectMetadata(archiveId, objectId));
				return resp;
			}
			catch (BWFLAException error) {
				return new MediaDescriptionResponse(new BWFLAException(error));
			}
		}

		@DELETE
		@Path("/{objectId}")
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public Response delete(@PathParam("objectId") String objectId)
		{
			LOG.info("Deleting digital object '" + objectId + "'...");

			try {
				objHelper.delete(archiveId, objectId);
				return Response.ok()
						.build();
			}
			catch (BWFLAException error) {
				throw new BadRequestException(Response
						.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorInformation(error.getMessage()))
						.build());
			}
		}

		private String lookupArchiveId(String archiveId)
		{
			if (archiveId == null || archiveId.equals(defaultArchive)) {
				try {
					archiveId = ObjectRepository.this.manageUserCtx(defaultArchive);
				} catch (BWFLAException error) {
					archiveId = defaultArchive;
				}
			}

			return archiveId;
		}
	}


	public class Tasks
	{
		@GET
		@Path("/{taskId}")
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public TaskStateResponse get(@PathParam("taskId") String taskId)
		{
			try {
				return new TaskStateResponse(objHelper.getTaskState(taskId));
			}
			catch (BWFLAException error) {
				return new TaskStateResponse(error);
			}
		}
	}


	// ========== Internal Helpers ==============================

	private String manageUserCtx(String archiveId) throws BWFLAException
	{
		if (userctx != null && userctx.getUsername() != null) {
			LOG.info("Using user context: " + userctx.getUsername());
			archiveId = USER_ARCHIVE_PREFIX + userctx.getUsername();
			if (!objArchives.contains(archiveId)) {
				objHelper.registerUserArchive(archiveId);
				objArchives = new HashSet<>(objHelper.getArchives());
			}
		}

		return archiveId;
	}
}
