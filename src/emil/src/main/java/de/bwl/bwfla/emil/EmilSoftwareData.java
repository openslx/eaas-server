package de.bwl.bwfla.emil;

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

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.EaasiSoftwareObject;
import de.bwl.bwfla.emil.datatypes.SoftwareCollection;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.emil.datatypes.EmilSoftwareObject;


// TODO: remove this!

@Deprecated
@Path("EmilSoftwareData")
@ApplicationScoped
public class EmilSoftwareData extends EmilRest {

	@Inject
	private SoftwareRepository swrepo = null;

	@Secured(roles = {Role.PUBLIC})
	@GET
	@Path("/getSoftwareObject")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoftwareObject(@QueryParam("softwareId") String softwareId)
	{
		return swrepo.packages()
				.get(softwareId);
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
	@Secured(roles = {Role.PUBLIC})
	@GET
	@Path("/getSoftwarePackageDescription")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoftwarePackageDescription(@QueryParam("softwareId") String softwareId)
	{
		return swrepo.descriptions()
				.get(softwareId);
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
	@Secured(roles = {Role.PUBLIC})
	@GET
	@Path("/getSoftwarePackageDescriptions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoftwarePackageDescriptions()
	{
		return swrepo.descriptions()
				.list();
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
	@Secured(roles = {Role.RESTRCITED})
	@POST
	@Path("/saveSoftwareObject")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveSoftwareObject(EmilSoftwareObject swo)
	{

		return swrepo.packages()
				.create(swo);
	}

	public SoftwareCollection getSoftwareCollection()
	{
		return swrepo.getSoftwareCollection();
	}

	public void importSoftware(EaasiSoftwareObject swo) throws BWFLAException
	{
		swrepo.importSoftware(swo);
	}
}
