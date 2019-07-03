package de.bwl.bwfla.emil;

import de.bwl.bwfla.api.objectarchive.DigitalObjectMetadata;
import de.bwl.bwfla.api.objectarchive.ObjectFileCollection;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.classification.ArchiveAdapter;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Role;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;
import org.apache.commons.io.IOUtils;
import org.apache.tamaya.inject.api.Config;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Path("/objects")
@ApplicationScoped
public class EmilObjectData extends EmilRest {

    SoftwareArchiveHelper swHelper;

    @Inject
	private ArchiveAdapter archive;

	@Inject
	@AuthenticatedUser
	private UserContext authenticatedUser;

	@Inject
	private EmilEnvironmentRepository environmentRepository;

	@Inject
	@Config(value="commonconf.serverdatadir")
	private String serverdatadir;

	private static final String tmpArchiveDir = "emil-temp-objects";
	private static final String tmpArchiveName = "emil-temp-objects";
	private static Set<String> objArchives;

	private final String USER_ARCHIVE_PREFIX = "user_archive";

	@Inject
	@Config(value="objectarchive.userarchive")
	private String userArchiveBase;

	@PostConstruct
    private void init() {
        swHelper = new SoftwareArchiveHelper(softwareArchive);
		try {
			objArchives = new HashSet<>(archive.objects().getArchives());
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
			archive.objects().sync();
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
			return new TaskStateResponse(archive.objects().sync(req.getArchive(), req.getObjectIDs()));
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
			return new TaskStateResponse(archive.objects().getTaskState(taskId));
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
			List<String> objects = archive.objects().getObjectList(archiveId);
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

				DigitalObjectMetadata md = archive.objects().getObjectMetadata(archiveId, id);
				ObjectListItem item = new ObjectListItem(id);
				item.setTitle(md.getTitle());
				item.setArchiveId(archiveId);

				try {
					item.setDescription(archive.getClassificationResultForObject(id).getUserDescription());
				} catch (NoSuchElementException e){
					LOG.info("no cache for " + id + ". Getting default description");
					item.setDescription(md.getDescription());
				}

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
			archive.objects().delete(archiveId, objectId);
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
												     @PathParam("objectArchive") String archiveId,
													 @QueryParam("updateClassification") @DefaultValue("false") boolean updateClassification,
													 @QueryParam("updateProposal") @DefaultValue("false") boolean updateProposal)
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
			String chosenObjRef = null;
			try {
				chosenObjRef = archive.getFileCollectionForObject(archiveId, objectId);
			} catch (BWFLAException e) {
				return resp;
			}

			FileCollection fc = FileCollection.fromValue(chosenObjRef);
			resp.setMediaItems(fc);
			resp.setMetadata(archive.objects().getObjectMetadata(archiveId, objectId));
			resp.setMetsdata(archive.objects().getMetsdata(archiveId, objectId));

			resp.setObjectEnvironments(archive.getEnvironmentsForObject(archiveId, objectId, updateClassification, updateProposal));
			return resp;
		}
		catch (BWFLAException | JAXBException exception) {
			return new MediaDescriptionResponse(new BWFLAException(exception));
		}
	}

	private static String partToString(List<InputPart> partList) throws BWFLAException, IOException {
		if(partList.size() == 0)
			throw new BWFLAException("partList empty");

		InputPart part = partList.get(0);
		return part.getBodyAsString();

	}

	@Secured({Role.RESTRCITED})
	@POST
	@Path("/pushUpload")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response pushUpload(UploadObjectRequest req)
	{
		String objId = req.getObjectId();
		String archive = req.getArchive();
		if(archive ==  null)
			archive = "default";
		try {
			archive = manageUserCtx(archive);
		} catch (BWFLAException e) {
			return Emil.errorMessageResponse(e.getMessage());
		}

		ObjectArchiveHelper localhelper = new ObjectArchiveHelper("http://localhost:8080");
		try {
			ObjectFileCollection object = localhelper.getObjectHandle(tmpArchiveName, objId);
			this.archive.objects().importObject(archive, object);
			localhelper.delete(tmpArchiveName, objId);
		} catch (BWFLAException e) {
			e.printStackTrace();
		}

		return Emil.successMessageResponse("done");
	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/archives")
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectArchivesResponse getArchives()
	{
		try {

			String defaultArchive = manageUserCtx("default");

			objArchives = new HashSet<>(archive.objects().getArchives());
			List<String> archives = objArchives.stream().filter(
					e -> !(e.startsWith(USER_ARCHIVE_PREFIX) && !e.equals(defaultArchive))
			).filter(
					e -> !e.equals("default")
			).filter(
					e -> !(!defaultArchive.equals("default") && e.equals("zero conf")) // remove zero conf archive is usercontext is available
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
	@Path("/upload")
	@Consumes("multipart/form-data")
	public Response upload(MultipartFormDataInput input)
	{
		String fileName = null;
		String objectId = null;
		String mediaType = null;
		InputStream inputFile =  null;

		if(serverdatadir == null)
			return Emil.errorMessageResponse("invalid configuration");

		File tempObjectPath = new File(serverdatadir, tmpArchiveDir);
		if(!tempObjectPath.exists())
		{
			if(!tempObjectPath.mkdirs())
				return Emil.errorMessageResponse("unable to create " + tempObjectPath);
		}

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputPartsFiles = uploadForm.get("file");
		List<InputPart> metadataPart = uploadForm.get("mediaType");
		List<InputPart> objectIdPart = uploadForm.get("objectId");

		if(inputPartsFiles == null || metadataPart == null || objectIdPart == null)
			return Emil.errorMessageResponse("invalid form data");

		for (InputPart inputPart : inputPartsFiles) {

			try {
				MultivaluedMap<String, String> header = inputPart.getHeaders();
				fileName = getFileName(header);
				if(fileName == null)
					fileName = UUID.randomUUID().toString();
				fileName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

				inputFile = inputPart.getBody(InputStream.class,null);
			} catch (IOException e) {
				return Emil.internalErrorResponse(e);
			}
		}

		try {
			mediaType = partToString(metadataPart);
			objectId = partToString(objectIdPart);
		}
		catch (Exception e)
		{
			return Emil.internalErrorResponse(e);
		}

		File objectDir = new File(tempObjectPath, objectId);
		if(!objectDir.exists())
			objectDir.mkdirs();

		File targetDir = new File(objectDir, mediaType);
		if(!targetDir.exists())
			targetDir.mkdirs();

		File target = new File(targetDir, fileName);
		try (OutputStream outputStream = new FileOutputStream(target)) {
			IOUtils.copy(inputFile, outputStream);
		}
		catch (Exception e)
		{
			return Emil.internalErrorResponse(e);
		}

		return Emil.successMessageResponse("uploadFile is called, Uploaded file name : " + fileName);
	}

	private String getFileName(MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

	private String manageUserCtx(String archiveId) throws BWFLAException {
		if(authenticatedUser != null && authenticatedUser.getUsername() != null) {
			LOG.info("got user context: " + authenticatedUser.getUsername());
			archiveId = USER_ARCHIVE_PREFIX + authenticatedUser.getUsername();
			if (!objArchives.contains(archiveId)) {
				archive.objects().registerUserArchive(archiveId);
				objArchives = new HashSet<>(archive.objects().getArchives());
			}
			return USER_ARCHIVE_PREFIX + authenticatedUser.getUsername();
		}
		return archiveId;
	}
}
