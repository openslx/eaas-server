package de.bwl.bwfla.emil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.datatypes.SoftwareDescription;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.utils.JsonBuilder;
import de.bwl.bwfla.emil.datatypes.EmilSoftwareObject;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;

@Path("EmilSoftwareData")
@ApplicationScoped
public class EmilSoftwareData extends EmilRest {
    SoftwareArchiveHelper swHelper;
    ObjectArchiveHelper objHelper;


    @Inject
    @Config(value = "ws.softwarearchive")
    private String softwareArchive;

    @Inject
    @Config(value = "ws.objectarchive")
    private String objectArchive;
    
    @PostConstruct
    private void init() {
        swHelper = new SoftwareArchiveHelper(softwareArchive);
        objHelper = new ObjectArchiveHelper(objectArchive);
    }
    
	@GET
	@Path("/getSoftwareObject")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoftwareObject(@QueryParam("softwareId") String softwareId)
	{
        final SoftwareArchiveHelper swarchive = swHelper;
        try {
            EmilSoftwareObject swo = new EmilSoftwareObject();
            SoftwarePackage software = swarchive.getSoftwarePackageById(softwareId);
            if(software == null)
            {
                return Emil.createResponse(Status.OK, swo);
            }
            
            swo.setObjectId(software.getObjectId());
            swo.setAllowedInstances(software.getNumSeats());
            List<String> fmts = software.getSupportedFileFormats();
            if(fmts == null)
                fmts = new ArrayList<String>();
            swo.setNativeFMTs(fmts);
            swo.setExportFMTs(new ArrayList<String>());
            swo.setImportFMTs(new ArrayList<String>());
            swo.setLicenseInformation(software.getLicence());
            swo.setIsOperatingSystem(software.getIsOperatingSystem());
            return Emil.createResponse(Status.OK, swo);
        }
        catch(Throwable t)
        {
            return Emil.internalErrorResponse(t);
        }
	
	}
	
	/**
	 * Looks up and returns the description for a specified software package.
	 * When the software package is found, a JSON response will be returned, containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "id": &ltSoftwarePackage's ID&gt,
	 *      "label": "Short description"
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON response containing
	 * the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param softwareId The software package's ID to look up.
	 * @return A JSON response containing software package's description when found,
	 *         else an error message.
	 */
	@GET
	@Path("/getSoftwarePackageDescription")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoftwarePackageDescription(@QueryParam("softwareId") String softwareId)
	{
        final SoftwareArchiveHelper swarchive = swHelper;
        try {
            SoftwareDescription desc = swarchive.getSoftwareDescriptionById(softwareId);
            if (desc == null)
                return Emil.errorMessageResponse("Software with ID '" + softwareId + "' was not found!");

            // Construct response
            JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
            json.beginObject();
            json.add("status", "0");
            json.add("id", desc.getSoftwareId());
            json.add("label", desc.getLabel());
            json.add("isOperatingSystem", desc.getIsOperatingSystem());
            json.endObject();
            json.finish();

            return Emil.createResponse(Status.OK, json.toString());
        }
        catch (Throwable throwable) {
            return Emil.internalErrorResponse(throwable);
        }
	}

	
	/**
	 * Looks up and returns the descriptions for all software packages.
	 * A JSON response will be returned, containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "descriptions": [
	 *          { "id": &ltSoftwarePackage's ID&gt, "label": "Short description" },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON response containing
	 * the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "2",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @return A JSON response containing a list of descriptions
	 *         for all software packages or an error message.
	 */
	@GET
	@Path("/getSoftwarePackageDescriptions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoftwarePackageDescriptions()
	{
        final SoftwareArchiveHelper swarchive = swHelper;
        try {
            List<SoftwareDescription> descriptions = swarchive.getSoftwareDescriptions();
            if (descriptions == null)
                return Emil.errorMessageResponse("Software archive could not be read!");

            // Construct response
            JsonBuilder json = new JsonBuilder(1024);
            json.beginObject();
            json.add("status", "0");
            json.name("descriptions");
            json.beginArray();

            for (SoftwareDescription desc : descriptions) {
                json.beginObject();
                json.add("id", desc.getSoftwareId());
                json.add("label", desc.getLabel());
                json.add("isOperatingSystem", desc.getIsOperatingSystem());
                json.endObject();
            }

            json.endArray();
            json.endObject();
            json.finish();

            return Emil.createResponse(Status.OK, json.toString());
        }
        catch (Throwable throwable) {
            return Emil.internalErrorResponse(throwable);
        }
	}

//	@OPTIONS
//	@Path("/saveSoftwareObject")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response saveSoftwareObject() {
//		return WS_OPTIONS_RESPONSE;
//	}
	
	/**
	 * saves or updates software object meta data. 
	 * expects a JSON object:
	 * <pre>
	 * {"objectId":"id","licenseInformation":"","allowedInstances":1,"nativeFMTs":[],"importFMTs":[],"exportFMTs":[]}
	 * </pre>
	 * @param swo EmilSoftwareObject as JSON string
	 * @return JSON response (error) message
	 */
	@POST
	@Path("/saveSoftwareObject")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveSoftwareObject(EmilSoftwareObject swo)
	{

		final SoftwareArchiveHelper swarchive = swHelper;

		try {
			SoftwarePackage software = swarchive.getSoftwarePackageById(swo.getObjectId());
			if(software == null)
			{
				
				String archiveName = swo.getArchiveId();
				if(archiveName == null)
					archiveName = "default";
				
				software = new SoftwarePackage();
				software.setObjectId(swo.getObjectId());
				software.setArchive(archiveName);
				software.setName(swo.getLabel());
			}
			
			software.setNumSeats(swo.getAllowedInstances());
			software.setLicence(swo.getLicenseInformation());
			software.setIsOperatingSystem(swo.getIsOperatingSystem());
			
			software.setSupportedFileFormats(swo.getNativeFMTs());
			if(swo.getImportFMTs() != null)
				software.getSupportedFileFormats().addAll(swo.getImportFMTs());
			if(swo.getImportFMTs() != null)
				software.getSupportedFileFormats().addAll(swo.getExportFMTs());

			software.setQID(swo.getQID());
			
			swarchive.addSoftwarePackage(software);
		}
		catch(Throwable t)
		{
			return Emil.internalErrorResponse(t);
		}
		
		String message = "succsess";
		return Emil.successMessageResponse(message);
	}
}
