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

import com.openslx.eaas.common.databind.DataUtils;
import com.openslx.eaas.resolver.DataResolver;
import de.bwl.bwfla.common.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.rest.ErrorInformation;
import de.bwl.bwfla.emil.datatypes.ObjectListItem;
import de.bwl.bwfla.emil.datatypes.rest.ImportObjectRequest;
import de.bwl.bwfla.emil.datatypes.rest.MediaDescriptionResponse;
import de.bwl.bwfla.emil.datatypes.rest.ObjectArchivesResponse;
import de.bwl.bwfla.emil.datatypes.rest.SyncObjectRequest;
import de.bwl.bwfla.emil.datatypes.rest.TaskStateResponse;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;
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
import javax.ws.rs.HEAD;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ApplicationScoped
@Path("/object-repository")
public class ObjectRepository extends EmilRest
{

	private SoftwareArchiveHelper swHelper;
	private ObjectArchiveHelper objHelper;

	@Inject
	private ObjectClassification classification;

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
	@Config(value="objectarchive.user_archive_enabled")
	private boolean userArchiveEnabled;

	@Inject
	@Config(value="objectarchive.user_archive_prefix")
	private String USER_ARCHIVE_PREFIX;


	private static boolean initialized = false;

	public boolean isInitialized()
	{
		return initialized;
	}

	@PostConstruct
    private void initialize()
	{
		try {
			objHelper = new ObjectArchiveHelper(objectArchive);
			swHelper = new SoftwareArchiveHelper(softwareArchive);
			objArchives = new HashSet<>(objHelper.getArchives());

			initialized = true;
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
		@Secured(roles = {Role.RESTRICTED})
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
		@Secured(roles = {Role.RESTRICTED})
		@Produces(MediaType.APPLICATION_JSON)
		public ObjectArchivesResponse list()
		{
			try {
				// FIXME: do we need to refresh the list of archives here?
				objArchives = new HashSet<>(objHelper.getArchives());

				final String _defaultArchive = ObjectRepository.this.manageUserCtx(defaultArchive);
				final List<String> archives = objArchives.stream()
						.filter(e -> !(userArchiveEnabled && e.startsWith(USER_ARCHIVE_PREFIX) && !e.equals(_defaultArchive)))
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
			return new Objects(DataResolver.decode(archiveId));
		}

		@POST
		@Secured(roles = {Role.RESTRICTED})
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

		 * @return
		 *
		 * @HTTP 500 if archive is not found
		 *
		 * @documentationType de.bwl.bwfla.emil.datatypes.ObjectListItem
		 */
		@GET
		@Secured(roles = {Role.RESTRICTED})
		@Produces(MediaType.APPLICATION_JSON)
		public Response list(@Context final HttpServletResponse response)
		{
			LOG.info("Listing all digital objects in archive '" + archiveId + "'...");

			try {
				Stream<DigitalObjectMetadata> objects = null;
				try {
					objects = objHelper.getObjectMetadata(archiveId);
				}
				catch (Exception e)
				{
					// we need to initialize the archive list first. otherwise we fail listing the user-archive
					// see: https://gitlab.com/openslx/eaas-server/-/issues/55
					archives().list();
					objects = objHelper.getObjectMetadata(archiveId);
				}

				// Construct response (in streaming-mode)
				Stream<DigitalObjectMetadata> finalObjects = objects;
				final StreamingOutput output = (ostream) -> {
					final var jsonfactory = DataUtils.json()
							.mapper()
							.getFactory();

					try (com.fasterxml.jackson.core.JsonGenerator json = jsonfactory.createGenerator(ostream)) {
						final var writer = DataUtils.json()
								.writer();

						json.writeStartArray();
						finalObjects.forEach((object) -> {
							try {
								final String id = object.getId();
								if (swHelper.hasSoftwarePackage(id))
									return;

								final ObjectListItem item = new ObjectListItem(id);
								item.setTitle(object.getTitle());
								item.setArchiveId(archiveId);
								item.setThumbnail(object.getThumbnail());
								item.setSummary(object.getSummary());

//								try {
//									item.setDescription(archive.getClassificationResultForObject(id).getUserDescription());
//								} catch (NoSuchElementException e){
//									LOG.info("no cache for " + id + ". Getting default description");
//									item.setDescription(object.getDescription());
//								}

								writer.writeValue(json, item);
							}
							catch (Exception error) {
								LOG.log(Level.WARNING, "Serializing object's metadata failed!", error);
								throw new RuntimeException(error);
							}
						});

						json.writeEndArray();
						json.flush();
					}
					finally {
						finalObjects.close();
					}
				};

				return Response.ok()
						.entity(output)
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
		@Secured(roles = {Role.RESTRICTED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public TaskStateResponse importObject(ImportObjectRequest req)
		{
			String _archiveId = null;
			try {
				_archiveId = manageUserCtx(archiveId);
			} catch (BWFLAException e) {
				_archiveId = archiveId;
			}
			final ImportObjectTask task = new ImportObjectTask(req, _archiveId, objHelper);
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
		@Secured(roles = {Role.RESTRICTED})
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
				resp.setObjectEnvironments(classification.getCachedEnvironmentsForObject(objectId));
				return resp;
			}
			catch (BWFLAException error) {
				return new MediaDescriptionResponse(new BWFLAException(error));
			}
		}

		@DELETE
		@Path("/{objectId}")
		@Secured(roles = {Role.RESTRICTED})
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

		@HEAD
		@Secured(roles = {Role.RESTRICTED})
		@Path("/{objectId}/resources/{resourceId}/url")
		public Response resolveHEAD(@PathParam("objectId") String objectId, @PathParam("resourceId") String resourceId)
		{
			return this.resolve(objectId, resourceId, HttpMethod.HEAD);
		}

		@GET
		@Secured(roles = {Role.RESTRICTED})
		@Path("/{objectId}/resources/{resourceId}/url")
		public Response resolveGET(@PathParam("objectId") String objectId, @PathParam("resourceId") String resourceId)
		{
			return this.resolve(objectId, resourceId, HttpMethod.GET);
		}

		private Response resolve(String objectId, String resourceId, String method)
		{
			final var objref = objectId + "/" + resourceId;
			try {
				final var location = objHelper.resolveObjectResource(archiveId, objectId, resourceId, method);
				LOG.info("Resolving object '" + objref + "' -> " + method + " " + location);
				return Response.temporaryRedirect(new URI(location))
						.build();
			}
			catch (Exception error) {
				LOG.log(Level.WARNING, "Resolving object '" + objref + "' failed!", error);
				throw new NotFoundException();
			}
		}

		private String lookupArchiveId(String archiveId)
		{
			if(!userArchiveEnabled)
				return defaultArchive;

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
		@Secured(roles = {Role.RESTRICTED})
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
		if (userctx.isAvailable() && userctx.getUserId() != null && userArchiveEnabled) {
			LOG.info("Using user context: " + userctx.getUserId());
			archiveId = USER_ARCHIVE_PREFIX + userctx.getUserId();
			if (!objArchives.contains(archiveId)) {
				objHelper.registerUserArchive(archiveId);
				objArchives = new HashSet<>(objHelper.getArchives());
			}
		}

		return archiveId;
	}
}
