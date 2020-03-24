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
import de.bwl.bwfla.common.datatypes.SoftwareDescription;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.EaasiSoftwareObject;
import de.bwl.bwfla.emil.datatypes.EmilSoftwareObject;
import de.bwl.bwfla.emil.datatypes.SoftwareCollection;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.imageproposer.client.ImageProposer;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


@ApplicationScoped
@Path("/software-repository")
public class SoftwareRepository extends EmilRest
{
    private SoftwareArchiveHelper swHelper;
    private ObjectArchiveHelper objHelper;

    @Inject
    @Config(value = "ws.softwarearchive")
    private String softwareArchive = null;

    @Inject
    @Config(value = "ws.objectarchive")
    private String objectArchive = null;

	@Inject
	@AuthenticatedUser
	private UserContext userctx = null;

	private ImageProposer imageProposer;

	@Inject
	@Config(value = "emil.imageproposerservice")
	private String imageProposerService = null;


    @PostConstruct
    private void inititialize()
	{
		try {
			swHelper = new SoftwareArchiveHelper(softwareArchive);
			objHelper = new ObjectArchiveHelper(objectArchive);
			imageProposer = new ImageProposer(imageProposerService + "/imageproposer");
		}
		catch (IllegalArgumentException error) {
			LOG.log(Level.WARNING, "Initializing sotware-repository failed!", error);
		}
    }

	public SoftwareCollection getSoftwareCollection()
	{
		return new SoftwareCollection(objHelper, swHelper);
	}

	public void importSoftware(EaasiSoftwareObject swo) throws BWFLAException
	{
		SoftwarePackage softwarePackage = swo.getSoftwarePackage();
		softwarePackage.setArchive("Remote Objects");
		try {
			SoftwarePackage software = swHelper.getSoftwarePackageById(softwarePackage.getObjectId());
//			if(software != null) {
//				LOG.warning("software with id " + softwarePackage.getObjectId() + " present. skipping...");
//				return;
//			}
		}
		catch (BWFLAException error) {
			LOG.log(Level.WARNING, "Importing software-package failed!", error);
			return;
		}

		objHelper.importFromMetadata("Remote Objects", swo.getMetsData());
		swHelper.addSoftwarePackage(softwarePackage);
	}


	// ========== Public API ==============================

	@Path("packages")
	public SoftwarePackages packages()
	{
		return new SoftwarePackages();
	}

	@Path("descriptions")
	public SoftwareDescriptions descriptions()
	{
		return new SoftwareDescriptions();
	}


	// ========== Subresources ==============================

	public class SoftwarePackages
	{
		@GET
		@Path("/{softwareId}")
		@Secured(roles = {Role.PUBLIC})
		@Produces(MediaType.APPLICATION_JSON)
		public Response get(@PathParam("softwareId") String softwareId)
		{
			LOG.info("Looking up software-package '" + softwareId + "'...");

			try {
				EmilSoftwareObject swo = new EmilSoftwareObject();
				SoftwarePackage software = swHelper.getSoftwarePackageById(softwareId);
				if (software == null) {
					// TODO: throw NotFoundException here!
					return SoftwareRepository.createResponse(Status.OK, swo);
				}

				swo.setIsPublic(software.isPublic());
				swo.setObjectId(software.getObjectId());
				swo.setArchiveId(software.getArchive());
				swo.setAllowedInstances(software.getNumSeats());
				List<String> fmts = software.getSupportedFileFormats();
				if (fmts == null)
					fmts = new ArrayList<String>();

				swo.setNativeFMTs(fmts);
				swo.setExportFMTs(new ArrayList<String>());
				swo.setImportFMTs(new ArrayList<String>());
				swo.setLicenseInformation(software.getLicence());
				swo.setIsOperatingSystem(software.getIsOperatingSystem());
				swo.setQID(software.getQID());
				return SoftwareRepository.createResponse(Status.OK, swo);
			}
			catch(Throwable error) {
				LOG.log(Level.WARNING, "Looking up software-package failed!", error);
				return Emil.internalErrorResponse(error);
			}
		}

		/**
		 * Save or update software object meta data.
		 * expects a JSON object:
		 * <pre>
		 * {"objectId":"id","licenseInformation":"","allowedInstances":1,"nativeFMTs":[],"importFMTs":[],"exportFMTs":[]}
		 * </pre>
		 * @param swo EmilSoftwareObject as JSON string
		 * @return JSON response (error) message
		 */
		@POST
		@Secured(roles = {Role.RESTRCITED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public Response create(EmilSoftwareObject swo)
		{
			// TODO: maybe, the logic should be split in two separate functions (create() and update())?

			LOG.info("Creating new software-package...");

			try {
				SoftwarePackage software = swHelper.getSoftwarePackageById(swo.getObjectId());
				if (software == null) {
					String archiveName = swo.getArchiveId();
					if (archiveName == null) {
						if (userctx != null && userctx.getUsername() != null) {
							LOG.info("Using user context: " + userctx.getUsername());
							archiveName = userctx.getUsername();
						}
					}

					LOG.info("Trying archive '" + swo.getArchiveId() + "' for " + swo.getObjectId());
					if (archiveName == null || archiveName.startsWith("user")) {
						LOG.info("Importing object...");
						DigitalObjectMetadata md = objHelper.getObjectMetadata(archiveName, swo.getObjectId());
						if (md == null) {
							LOG.severe("Importing object failed!");
							return Emil.errorMessageResponse("Failed to access object!");
						}

						objHelper.importFromMetadata("default", md.getMetsData());
						archiveName = "default";
					}

					software = new SoftwarePackage();
					software.setObjectId(swo.getObjectId());
					software.setArchive(archiveName);
					software.setName(swo.getLabel());
				}

				software.setPublic(swo.getIsPublic());
				LOG.info("Setting software-package's visibility to: " + ((software.isPublic()) ? "public" : "private"));

				software.setNumSeats(swo.getAllowedInstances());
				software.setLicence(swo.getLicenseInformation());
				software.setIsOperatingSystem(swo.getIsOperatingSystem());
				software.setSupportedFileFormats(swo.getNativeFMTs());
				if (swo.getImportFMTs() != null)
					software.getSupportedFileFormats().addAll(swo.getImportFMTs());

				if (swo.getImportFMTs() != null)
					software.getSupportedFileFormats().addAll(swo.getExportFMTs());

				software.setQID(swo.getQID());

				swHelper.addSoftwarePackage(software);
				imageProposer.refreshIndex();
			}
			catch(Throwable error) {
				LOG.log(Level.WARNING, "Creating new software-package failed!", error);
				return SoftwareRepository.internalErrorResponse(error);
			}

			String message = "Successfully imported software object " + swo.getObjectId();
			return SoftwareRepository.successMessageResponse(message);
		}
	}


	public class SoftwareDescriptions
	{
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
		@Secured(roles = {Role.PUBLIC})
		@Produces(MediaType.APPLICATION_JSON)
		public Response list()
		{
			LOG.info("Listing all software-package descriptions...");

			try {
				List<SoftwareDescription> descriptions = swHelper.getSoftwareDescriptions();
				if (descriptions == null) {
					// TODO: throw NotFoundException here!
					return SoftwareRepository.errorMessageResponse("Software archive could not be read!");
				}

				// Construct response
				final StringWriter writer = new StringWriter(2048);
				final JsonGenerator json = Json.createGenerator(writer);
				json.writeStartObject();
				json.write("status", "0");
				json.writeStartArray("descriptions");
				for (SoftwareDescription desc : descriptions) {
					json.writeStartObject();
					json.write("id", desc.getSoftwareId());
					json.write("label", desc.getLabel());
					json.write("isPublic", desc.isPublic());
					json.write("archiveId", (desc.getArchiveId() != null) ? desc.getArchiveId() : "default");
					json.write("isOperatingSystem", desc.getIsOperatingSystem());
					json.writeEnd();
				}

				json.writeEnd();
				json.writeEnd();
				json.flush();
				json.close();

				return SoftwareRepository.createResponse(Status.OK, writer.toString());
			}
			catch (Throwable error) {
				LOG.log(Level.WARNING, "Listing software-package descriptions failed!", error);
				return Emil.internalErrorResponse(error);
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
		@Path("/{softwareId}")
		@Secured(roles = {Role.PUBLIC})
		@Produces(MediaType.APPLICATION_JSON)
		public Response get(@PathParam("softwareId") String softwareId)
		{
			LOG.info("Looking up description of software-package '" + softwareId + "'...");

			try {
				SoftwareDescription desc = swHelper.getSoftwareDescriptionById(softwareId);
				if (desc == null) {
					// TODO: throw NotFoundException here!
					return Emil.errorMessageResponse("Software with ID '" + softwareId + "' was not found!");
				}

				// Construct response
				final JsonObject json = Json.createObjectBuilder()
						.add("status", "0")
						.add("id", desc.getSoftwareId())
						.add("label", desc.getLabel())
						.add("isPublic", desc.isPublic())
						.add("isOperatingSystem", desc.getIsOperatingSystem())
						.build();


				return SoftwareRepository.createResponse(Status.OK, json.toString());
			}
			catch (Throwable error) {
				LOG.log(Level.WARNING, "Looking up description of software-package failed!", error);
				return Emil.internalErrorResponse(error);
			}
		}
	}
}
