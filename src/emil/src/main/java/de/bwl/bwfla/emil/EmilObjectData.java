package de.bwl.bwfla.emil;

import de.bwl.bwfla.api.objectarchive.DigitalObjectMetadata;
import de.bwl.bwfla.api.objectarchive.ObjectFileCollection;
import de.bwl.bwfla.api.objectarchive.TaskState;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.classification.ArchiveAdapter;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
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

@Path("EmilObjectData")
@ApplicationScoped
public class EmilObjectData extends EmilRest {

    ObjectArchiveHelper objHelper;
    SoftwareArchiveHelper swHelper;

    @Inject
	private ArchiveAdapter archive;

    @Inject
	private EmilEnvironmentRepository environmentRepository;

	@Inject
	@Config(value="commonconf.serverdatadir")
	private String serverdatadir;

	private static final String tmpArchiveDir = "emil-temp-objects";
	private static final String tmpArchiveName = "emil-temp-objects";

    @PostConstruct
    private void init() {
        objHelper = new ObjectArchiveHelper(objectArchive);
        swHelper = new SoftwareArchiveHelper(softwareArchive);
	}
	
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

	@GET
	@Path("/objectImportTaskState")
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse getOjectImportTaskState(@QueryParam("taskId") String taskId)
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
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectListResponse list(@QueryParam("archiveId") @DefaultValue("default")  String archiveId,
								   @Context final HttpServletResponse response)
	{
		ObjectListResponse resp = new ObjectListResponse();
		try {
			List<String> objects = objHelper.getObjectList(archiveId);
			if(objects == null) {
				LOG.warning("objects null");
//				throw new BadRequestException(Response
//						.status(Status.BAD_REQUEST)
//						.entity(new ErrorInformation("loading archive '" + archiveId + "' failed"))
//						.build());
				resp.setStatus("1");
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
				item.setDescription(md.getDescription());
				item.setThumbnail(md.getThumbnail());
				item.setSummary(md.getSummary());
				objList.add(item);
			}
			resp.setObjects(objList);
			return resp;
		}
		catch (Exception e)
		{
			LOG.severe("I've got an exception in list");
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
	@GET
	@Path("/metadata")
	@Produces(MediaType.APPLICATION_JSON)
	public DigitalObjectMetadataResponse metadata(@QueryParam("objectId") String objectId,
												  @QueryParam("archiveId") @DefaultValue("default")  String archiveId)
	{
		try {
			return new DigitalObjectMetadataResponse(objHelper.getObjectMetadata(archiveId, objectId));
		}
		catch (BWFLAException e) {
			return new DigitalObjectMetadataResponse(e);
		}
	}
	
	/**
	 */
	@GET
	@Path("/environments")
	@Produces(MediaType.APPLICATION_JSON)
	public ClassificationResult environments(@QueryParam("objectId") String objectId,
                                             @QueryParam("archiveId") @DefaultValue("default")  String archiveId,
                                             @QueryParam("updateClassification") @DefaultValue("false") boolean updateClassification,
                                             @QueryParam("updateProposal") @DefaultValue("false") boolean updateProposal)
	{
		try {
			if(environmentRepository.getEmilEnvironments().isEmpty())
				environmentRepository.initialize();
			return archive.getEnvironmentsForObject(archiveId, objectId, updateClassification, updateProposal);
		}
		catch (JAXBException | IOException | NoSuchElementException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
			return new ClassificationResult();
		}
		catch(BWFLAException e)
		{
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return new ClassificationResult(e);
		}
	}

	/**
	 * Returns a description of media corresponding to a specified digital object:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "media": [
	 *          {
	 *              "mediumtype": &ltMedium's type&gt,
	 *              "labels": [ "label-1", ..., "label-n" ]
	 *          },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 * 
	 * @param objectId The digital object's ID.
	 * @return A JSON response containing media description, or an error message.
	 */
	@GET
	@Path("/mediaDescription")
	@Produces(MediaType.APPLICATION_JSON)
	public MediaDescriptionResponse mediaDescription(@QueryParam("objectId") String objectId, @QueryParam("archiveId") @DefaultValue("default")  String archiveId)
	{
		MediaDescriptionResponse resp = new MediaDescriptionResponse();
		try {
			String chosenObjRef = null;
			try {
				chosenObjRef = archive.getFileCollectionForObject(archiveId, objectId);
			} catch (BWFLAException e) {
				return resp;
			}

			FileCollection fc = FileCollection.fromValue(chosenObjRef);
			DriveType type = fc.files.get(0).getType();

			MediaDescriptionTypeList item = new MediaDescriptionTypeList();
			item.setMediumType(type.name());

			for (FileCollectionEntry fce : fc.files) {
				if(fce.getLabel() == null)
					item.getItems().add(new MediaDescriptionItem(fce.getId()));
				else
					item.getItems().add(new MediaDescriptionItem(fce.getId(), fce.getLabel()));
			}

			resp.getMedium().add(item);
			return resp;
		}
		catch (JAXBException exception) {
			return new MediaDescriptionResponse(new BWFLAException(exception));
		}
	}

	private static String partToString(List<InputPart> partList) throws BWFLAException, IOException {
		if(partList.size() == 0)
			throw new BWFLAException("partList empty");

		InputPart part = partList.get(0);
		return part.getBodyAsString();

	}

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

		ObjectArchiveHelper localhelper = new ObjectArchiveHelper("http://localhost:8080");
		try {
			ObjectFileCollection object = localhelper.getObjectHandle(tmpArchiveName, objId);
			objHelper.importObject(archive, object);
		} catch (BWFLAException e) {
			e.printStackTrace();
		}

		return Emil.successMessageResponse("done");
	}


	@GET
	@Path("/archives")
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectArchivesResponse getArchives()
	{
		try {
			List<String> archives = objHelper.getArchives();
			ObjectArchivesResponse response = new ObjectArchivesResponse();
			response.setArchives(archives);
			return response;
		} catch (BWFLAException e) {
			return new ObjectArchivesResponse(e);
		}
	}



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
		try {
			OutputStream outputStream = new FileOutputStream(target);
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
}
