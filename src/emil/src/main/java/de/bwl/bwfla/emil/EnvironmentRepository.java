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

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderResult;
import de.bwl.bwfla.common.datatypes.identification.OperatingSystems;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.configuration.converters.DurationPropertyConverter;
import de.bwl.bwfla.emil.datatypes.DefaultEnvironmentResponse;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import de.bwl.bwfla.emil.datatypes.EnvironmentCreateRequest;
import de.bwl.bwfla.emil.datatypes.EnvironmentDeleteRequest;
import de.bwl.bwfla.emil.datatypes.ErrorInformation;
import de.bwl.bwfla.emil.datatypes.ImportImageRequest;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.emil.datatypes.rest.ReplicateImagesResponse;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Role;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import de.bwl.bwfla.emil.tasks.CreateEmptyImageTask;
import de.bwl.bwfla.emil.tasks.ExportEnvironmentTask;
import de.bwl.bwfla.emil.tasks.ImportImageTask;
import de.bwl.bwfla.emil.tasks.ImportImageTask.ImportImageTaskRequest;
import de.bwl.bwfla.emil.tasks.ReplicateImageTask;
import de.bwl.bwfla.emil.utils.ContainerUtil;
import de.bwl.bwfla.emil.utils.TaskManager;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.emucomp.api.MachineConfiguration.NativeConfig;
import de.bwl.bwfla.imageproposer.client.ImageProposer;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;
import org.eclipse.persistence.annotations.DeleteAll;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;


@ApplicationScoped
@Path("/environment-repository")
public class EnvironmentRepository extends EmilRest
{
	@Inject
	private DatabaseEnvironmentsAdapter envdb = null;

	@Inject
	@Config(value = "emil.imageproposerservice")
	private String imageProposerService = null;

	@Inject
	@Config(value = "ws.imagebuilder")
	String imageBuilderAddress;

	@Inject
	private EmilEnvironmentRepository emilEnvRepo = null;

	private ImageProposer imageProposer = null;

	@Inject
	private ContainerUtil containerUtil = null;

	@Inject
	private TaskManager taskManager = null;

	@Inject
	@AuthenticatedUser
	private UserContext authenticatedUser = null;

	@Inject
	private ObjectClassification classification = null;

	private SoftwareArchiveHelper swHelper;

	@Inject
	private EmilObjectData objects;

	@PostConstruct
	private void initialize()
	{
		try {
			imageProposer = new ImageProposer(imageProposerService + "/imageproposer");
			swHelper = new SoftwareArchiveHelper(softwareArchive);
		}
		catch (IllegalArgumentException error) {
			LOG.log(Level.WARNING, "Initializing image-proposer failed!", error);
		}
	}


	// ========== Public API ==============================

	@Path("/environments")
	public Environments environments()
	{
		return new Environments();
	}

	@Path("/default-environments")
	public DefaultEnvironments defaultEnvironments()
	{
		return new DefaultEnvironments();
	}

	@Path("/templates")
	public Templates templates()
	{
		return new Templates();
	}

	@Path("/patches")
	public Patches patches()
	{
		return new Patches();
	}

	@Path("/actions")
	public Actions actions()
	{
		return new Actions();
	}

	@Path("/images")
	public Images images() { return new Images(); }

	@GET
	@Path("/db-content")
	@Secured({Role.RESTRCITED})
	@Produces(MediaType.APPLICATION_JSON)
	public <T extends JaxbType> Response getDatabaseContent(@QueryParam("type") String type, @QueryParam("className") String className)
	{
		LOG.info("Loading DB content...");
		try {
			Class<T> classType = (Class<T>) Class.forName(className);
			if (classType == null) {
				throw new BWFLAException("Class name is incorrect!");
			}
			return EnvironmentRepository.createResponse(Status.OK, emilEnvRepo.getDatabaseContent(type, classType));
		}
		catch (ClassNotFoundException | BWFLAException error) {
			LOG.log(Level.WARNING,"Loading database content failed!\n", error);
			return EnvironmentRepository.internalErrorResponse(error);
		}
	}

	@GET
	@Path("/os-metadata")
	@Secured({Role.PUBLIC})
	@Produces(MediaType.APPLICATION_JSON)
	public OperatingSystems getOperatingSystemMetadata()
	{
		LOG.info("Loading OS-metadata...");

		final String serverDataDir = ConfigurationProvider.getConfiguration()
				.get("commonconf.serverdatadir");

		final java.nio.file.Path osInfoPath = Paths.get(serverDataDir, "operating-systems.json");
		if (!Files.exists(osInfoPath)) {
			LOG.severe("No operating-systems.json found!");
			throw new NotFoundException();
		}

		try {
			final String json = new String(Files.readAllBytes(osInfoPath));
			final String value = "{\"operatingSystems\": " + json + "}";
			return OperatingSystems.fromJsonValue(value, OperatingSystems.class);
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Deserializing OS-metadata failed!", error);
			throw new InternalServerErrorException(error);
		}
	}

	/** Get the image-name index */
	@GET
	@Path("/image-name-index")
	@Secured({Role.PUBLIC})
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public ImageNameIndex getNameIndexes() throws BWFLAException
	{
		LOG.info("Loading image-name index...");
        return envdb.getNameIndexes();
    }

	@GET
	@Path("/images-index")
	@Secured({Role.PUBLIC})
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ImageNameIndex getImagesIndex() throws BWFLAException
	{
		LOG.info("Loading images index...");
		return envdb.getImagesIndex();
	}

	// ========== Subresources ==============================

	public class Images {

		/** Create a new environment */
//		@POST
//		@Secured({Role.RESTRCITED})
//		@Produces(MediaType.APPLICATION_JSON)
//		@Consumes(MediaType.APPLICATION_JSON)
//		public TaskStateResponse _import(ImageImRequest envReq)
//		{
//			LOG.info("Importing a new image ...");
//
//			try {
//				ImageMetadata d = null;
//				if (envReq.getLabel() != null)
//				{
//					d = new ImageMetadata();
//					d.setName(envReq.getLabel());
//				}
//				TaskState id = envdb.importImage("default", envReq.getSize(), ImageType.USER, d);
//				return new TaskStateResponse(id.getTaskId(), id.isDone());
//			}
//			catch (BWFLAException e)
//			{
//				return new TaskStateResponse(e);
//			}
//		}
	}

	public class Environments
	{
		/** List all available environments */
		@GET
		@Secured({Role.PUBLIC})
		@Produces(MediaType.APPLICATION_JSON)
		public Response list(@Context final HttpServletResponse response)
		{
			LOG.info("Listing all available environments...");
			try {
				final List<EnvironmentListItem> environments = emilEnvRepo.getEmilEnvironments()
						.stream()
						.map(EnvironmentListItem::new)
						.collect(Collectors.toList());

				return Response.status(Status.OK)
						.entity(environments)
						.build();
			}
			catch (Throwable error) {
				LOG.log(Level.WARNING, "Loading environments failed!", error);
				throw new BadRequestException(Response.status(Status.BAD_REQUEST)
						.entity(new ErrorInformation(error))
						.build());
			}
		}

		/** Get specific environment */
		@GET
		@Path("/{envId}")
		@Secured({Role.PUBLIC})
		@Produces(MediaType.APPLICATION_JSON)
		public Response get(@PathParam("envId") String envId, @Context final HttpServletResponse response)
		{
			LOG.info("Looking up environment '" + envId + "'...");

			EmilEnvironment emilenv = emilEnvRepo.getEmilEnvironmentById(envId);
			if (emilenv == null) {
				// TODO: throw NotFoundException here
				throw new BadRequestException(Response
						.status(Status.BAD_REQUEST)
						.entity(new ErrorInformation("environment id not found " + envId))
						.build());
			}

			try {
				Environment env = envdb.getEnvironmentById(emilenv.getArchive(), emilenv.getEnvId());
				MachineConfiguration machine = (env instanceof MachineConfiguration) ? (MachineConfiguration) env : null;
				List<EmilEnvironment> parents = emilEnvRepo.getParents(emilenv.getEnvId());
				EnvironmentDetails result = new EnvironmentDetails(emilenv, machine, parents, swHelper);
				return Response.ok()
						.entity(result)
						.build();
			}
			catch (BWFLAException error) {
				LOG.log(Level.WARNING, "Loading environment failed!", error);
				throw new BadRequestException(Response
						.status(Status.BAD_REQUEST)
						.entity(new ErrorInformation("failed retrieving data"))
						.build());
			}
		}

		/** Create a new environment */
		@POST
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		@Consumes(MediaType.APPLICATION_JSON)
		public Response create(EnvironmentCreateRequest envReq)
		{
			LOG.info("Creating new environment...");

			if (envReq.getTemplateId() == null)
				return EnvironmentRepository.errorMessageResponse("invalid template id");

			try {
				MachineConfiguration pEnv = envdb.getTemplate(envReq.getTemplateId());
				if (pEnv == null) {
					LOG.severe("invalid template id: " + envReq.getTemplateId());
					throw new BadRequestException(Response
							.status(Response.Status.BAD_REQUEST)
							.entity(new ErrorInformation("invalid template id: " + envReq.getTemplateId()))
							.build());
				}

				MachineConfiguration env = pEnv.copy(); // don't modify the real template
				env.getDescription().setTitle(envReq.getLabel());
				if (env.getNativeConfig() == null)
					env.setNativeConfig(new NativeConfig());

				env.setOperatingSystemId(envReq.getOperatingSystemId());
				env.getNativeConfig().setValue(envReq.getNativeConfig());
				ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
				iaMd.setType(ImageType.USER);

				driveUpdateHelper(env, envReq.getDriveSettings(), objects);

				if (env.getUiOptions() == null)
					env.setUiOptions(new UiOptions());

				final UiOptions uiopts = env.getUiOptions();
				if (envReq.isUseXpra())
					uiopts.setForwarding_system("XPRA");
				else uiopts.setForwarding_system(null);

				if (envReq.isUseWebRTC())
					uiopts.setAudio_system("webRTC");
				else uiopts.setAudio_system(null);

				if (uiopts.getHtml5() == null)
					uiopts.setHtml5(new Html5Options());

				String id = envdb.importMetadata("default", env, iaMd, false);

				EmilEnvironment newEmilEnv = emilEnvRepo.getEmilEnvironmentById(id);

				if (newEmilEnv != null)
					throw new BWFLAException("import failed: environment with id: " + id + " exists.");

				newEmilEnv = new EmilEnvironment();
				newEmilEnv.setTitle(envReq.getLabel());
				newEmilEnv.setEnvId(id);
				newEmilEnv.setEnableRelativeMouse(envReq.isEnableRelativeMouse());
				newEmilEnv.setEnablePrinting(envReq.isEnablePrinting());
				newEmilEnv.setShutdownByOs(envReq.isShutdownByOs());
				newEmilEnv.setXpraEncoding(envReq.getXpraEncoding());
				newEmilEnv.setOs(envReq.getOperatingSystemId());

				newEmilEnv.setDescription("imported / user created environment");
				emilEnvRepo.save(newEmilEnv, true);

				final JsonObject json = Json.createObjectBuilder()
						.add("id", id)
						.build();

				return Response.ok()
						.entity(json)
						.build();
			}
			catch (Throwable error) {
				error.printStackTrace();
				throw new BadRequestException(Response
						.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorInformation(error.getMessage()))
						.build());
			}
		}

		/**
		 * Update the description of a specified environment. This method
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
		@PATCH
		@Path("/{envId}")
		@Secured({Role.RESTRCITED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public Response update(@PathParam("envId") String envId, UpdateEnvironmentDescriptionRequest desc)
		{
			LOG.info("Updating environment '" + envId + "'...");

			boolean imported = false;

			if (envId == null)
				return EnvironmentRepository.errorMessageResponse("envId was null");

			final EmilEnvironment oldenv = emilEnvRepo.getEmilEnvironmentById(envId);
			if (oldenv == null)
				return EnvironmentRepository.errorMessageResponse("No emil environment found with ID: " + envId);

			EmilEnvironment newenv;

			try {
				final Environment environment = envdb.getEnvironmentById(oldenv.getArchive(), envId);
				if (environment instanceof MachineConfiguration) {
					final MachineConfiguration machineConfiguration = (MachineConfiguration) environment;
					machineConfiguration.setOperatingSystemId(desc.getOs());
					if (desc.getNativeConfig() != null) {
						if (machineConfiguration.getNativeConfig() == null)
							machineConfiguration.setNativeConfig(new NativeConfig());
						machineConfiguration.getNativeConfig().setValue(desc.getNativeConfig());
					}

					if (machineConfiguration.getUiOptions() == null)
						machineConfiguration.setUiOptions(new UiOptions());

					final UiOptions uiopts = machineConfiguration.getUiOptions();
					if (desc.isUseXpra())
						uiopts.setForwarding_system("XPRA");
					else uiopts.setForwarding_system(null);

					if (desc.isUseWebRTC())
						uiopts.setAudio_system("webRTC");
					else uiopts.setAudio_system(null);

					if (uiopts.getHtml5() == null)
						uiopts.setHtml5(new Html5Options());

					// Do not check for null. EmuBean would choose latest, if value is null
					final EmulatorSpec emulator = machineConfiguration.getEmulator();
					emulator.setContainerName(desc.getContainerEmulatorName());
					emulator.setContainerVersion(desc.getContainerEmulatorVersion());

					uiopts.getHtml5().setPointerLock(desc.isEnableRelativeMouse());

					machineConfiguration.setDrive(desc.getDrives());
					driveUpdateHelper(machineConfiguration, desc.getDriveSettings(), objects);

					machineConfiguration.setLinuxRuntime(desc.isLinuxRuntime());

					if (desc.getNetworking() != null && desc.getNetworking().isEnableInternet()) {
						List<Nic> nics = machineConfiguration.getNic();
						if (nics.size() == 0) {
							Nic nic = new Nic();
							nic.setHwaddress(NetworkUtils.getRandomHWAddress());
							nics.add(nic);
						}
					}
				}



				environment.setUserTag(desc.getUserTag());
				if (!oldenv.getArchive().equals("default")) {
					// we need to import / duplicate the env
					if (oldenv instanceof EmilObjectEnvironment)
						newenv = new EmilObjectEnvironment(oldenv);
					else newenv = new EmilEnvironment(oldenv);

					ImageArchiveMetadata md = new ImageArchiveMetadata();
					md.setType(ImageType.USER);
					newenv.setArchive("default");
					String id = envdb.importMetadata("default", environment, md, false);
					newenv.setEnvId(id);
					newenv.setParentEnvId(oldenv.getEnvId());
					oldenv.addChildEnvId(newenv.getEnvId());
					imported = true;
				}
				else {
					envdb.updateMetadata(oldenv.getArchive(), environment);
					newenv = oldenv;
				}

				imageProposer.refreshIndex();
			}
			catch (BWFLAException error) {
				LOG.log(Level.SEVERE, "Loading environment description failed!", error);
				return EnvironmentRepository.errorMessageResponse("No emulation environment found with ID: " + envId);
			}

			newenv.setTitle(desc.getTitle());
			newenv.setAuthor(desc.getAuthor());
			newenv.setDescription(desc.getDescription());
			newenv.setEnablePrinting(desc.isEnablePrinting());
			newenv.setEnableRelativeMouse(desc.isEnableRelativeMouse());
			newenv.setShutdownByOs(desc.isShutdownByOs());
			newenv.setNetworking(desc.getNetworking());
			newenv.setCanProcessAdditionalFiles(desc.canProcessAdditionalFiles());
			newenv.setXpraEncoding(desc.getXpraEncoding());
			newenv.setLinuxRuntime(desc.isLinuxRuntime());
			newenv.setHelpText(desc.getHelpText());

			if (desc.getTime() != null)
				newenv.setTimeContext(desc.getTime());
			else newenv.setTimeContext(null);

			try {
				if (imported) {
					// emilEnvRepo.save(currentEnv, false);
					emilEnvRepo.save(newenv, true);
				}
				else emilEnvRepo.save(newenv, false);
			}
			catch (BWFLAException error) {
				return EnvironmentRepository.internalErrorResponse(error);
			}

			final JsonObject json = Json.createObjectBuilder()
					.add("status", "0")
					.add("id", newenv.getEnvId())
					.build();

			return EnvironmentRepository.createResponse(Status.OK, json);
		}

		/** Delete a specific environment */
		@DELETE
		@Path("/{envId}")
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		@Consumes(MediaType.APPLICATION_JSON)
		public Response delete(@PathParam("envId") String envId, EnvironmentDeleteRequest desc)
		{
			LOG.info("Deleting environment '" + envId + "'...");

			if (desc.getEnvId() == null || desc.getEnvId().trim().isEmpty()) {
				return EnvironmentRepository.errorMessageResponse("Emil environment ID is null or empty");
			}

			if (!desc.getDeleteMetaData() && !desc.getDeleteImage())
				return EnvironmentRepository.successMessageResponse("nothing to be deleted");

			List<String> objectDependencies = classification.getEnvironmentDependencies(desc.getEnvId());
			if (objectDependencies != null && objectDependencies.size() > 0 && !desc.isForce()) {
				final JsonObject json = EnvironmentRepository.newJsonObject("2", objectDependencies.toString());
				return EnvironmentRepository.createResponse(Status.OK, json);
			}

			try {
				emilEnvRepo.delete(desc.getEnvId(), desc.getDeleteMetaData(), desc.getDeleteImage());
			}
			catch (BWFLAException error) {
				return EnvironmentRepository.internalErrorResponse(error);
			}

			imageProposer.refreshIndex();
			return EnvironmentRepository.successMessageResponse("deleting environment was successful!");
		}

		/** Export the specified environment. */
		@POST
		@Path("/{envId}/export")
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public TaskStateResponse export(@PathParam("envId") String envId, ExportRequest exportRequest)
		{
			LOG.info("Exporting environment '" + envId + "'...");

			if (exportPath == null || exportPath.isEmpty())
				return new TaskStateResponse(new BWFLAException("Emil export is not configured "));

			ExportEnvironmentTask.ExportEnvironmentRequest request = new ExportEnvironmentTask.ExportEnvironmentRequest();
			request.exportFilePath = exportPath;
			request.envId = exportRequest.getEnvId();
			request.envHelper = envdb;
			request.archive = exportRequest.getArchive();
			request.environmentRepository  = emilEnvRepo;
			request.userCtx = (authenticatedUser != null) ? authenticatedUser.getUsername() : null;
			return new TaskStateResponse(taskManager.submitTask(new ExportEnvironmentTask(request)));
		}

		/** List all object dependencies of an environment */
		@GET
		@Secured({Role.PUBLIC})
		@Path("/{envId}/object-deps")
		@Produces(MediaType.APPLICATION_JSON)
		public List<String> getObjectDependencies(@PathParam("envId") String envId)
		{
			LOG.info("Listing object-deps for environment '" + envId + "'...");

			if (envId == null || envId.trim().isEmpty()) {
				return new ArrayList<>();
			}

			return classification.getEnvironmentDependencies(envId);
		}

		@Path("/{envId}/revisions")
		public Revisions revisions(@PathParam("envId") String envId)
		{
			return new Revisions(envId);
		}
	}


	public class DefaultEnvironments
	{
		/** List all default environments */
		@GET
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public Map<String, String> list()
		{
			LOG.info("Listing default environments...");

			Map<String, String> map = new HashMap<>();
			try {
				List<DefaultEntry> defaultEnvironments = envdb.getDefaultEnvironments();
				for (DefaultEntry e : defaultEnvironments)
					map.put(e.getKey(), e.getValue());
			}
			catch (BWFLAException error) {
				LOG.log(Level.WARNING, "Loading default environments failed!", error);
			}

			return map;
		}

		/** Get configured default environment for a specific operating system ID */
		@GET
		@Path("/{osId}")
		@Produces(MediaType.APPLICATION_JSON)
		public DefaultEnvironmentResponse get(@PathParam("osId") String osId)
		{
			LOG.info("Looking up default environment for OS '" + osId + "'...");

			try {
				DefaultEnvironmentResponse response = new DefaultEnvironmentResponse();
				response.setEnvId(envdb.getDefaultEnvironment(osId));
				return response;
			}
			catch (BWFLAException error) {
				return new DefaultEnvironmentResponse(error);
			}
		}

		/** Set default environment for a specific operating system ID */
		@PATCH
		@Path("/{osId}")
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public EmilResponseType set(@PathParam("osId") String osId, @QueryParam("envId") String envId)
		{
			LOG.info("Setting default environment for OS '" + osId + "'...");

			try {
				envdb.setDefaultEnvironment(osId, envId);
				return new EmilResponseType();
			}
			catch (BWFLAException error) {
				return new EmilResponseType(error);
			}
		}
	}


	public class Revisions
	{
		private final String envId;

		protected Revisions(String envId)
		{
			this.envId = envId;
		}

		/** Create a new revision  */
		@POST
		@Secured({Role.RESTRCITED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public Response create()
		{
			LOG.info("Creating revision for environment '" + envId + "'...");

			EmilEnvironment emilEnv = emilEnvRepo.getEmilEnvironmentById(envId);
			if (emilEnv == null)
				return EnvironmentRepository.internalErrorResponse("not found: " + envId);  // TODO: throw NotFoundException!

			try {
				Environment environment = envdb.getEnvironmentById(emilEnv.getArchive(), envId);
				ImageArchiveMetadata md = new ImageArchiveMetadata();
				md.setType(ImageType.USER);
				String id = envdb.importMetadata("default", environment, md, false);
				EmilEnvironment newEmilEnv = new EmilEnvironment(emilEnv);
				newEmilEnv.setEnvId(id);
				newEmilEnv.setTitle("[fork]: " + newEmilEnv.getTitle() + " " + newEmilEnv.getEnvId());
				newEmilEnv.setArchive("default");
				newEmilEnv.setParentEnvId(emilEnv.getParentEnvId());
				emilEnvRepo.save(newEmilEnv, true);
			}
			catch (BWFLAException error) {
				return EnvironmentRepository.internalErrorResponse(error);
			}

			return EnvironmentRepository.successMessageResponse("forked environment: " + envId);
		}

		@POST
		@Path("/{revId}")
		@Secured({Role.RESTRCITED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public synchronized Response revert(@PathParam("revId") String revId)
		{
			if (envId == null || revId == null)
				return EnvironmentRepository.errorMessageResponse("Invalid Request");  // TODO: throw BadRequestException!

			EmilEnvironment currentEnv = emilEnvRepo.getEmilEnvironmentById(envId);
			if (currentEnv == null)  // TODO: throw NotFoundException!
				return EnvironmentRepository.errorMessageResponse("No emil environment found with ID: " + envId);

			LOG.info("Reverting environment '" + envId + "' to revison '" + revId + "'...");

			final List<String> emilEnvsToDelete = new ArrayList<>();
			try {
				EmilEnvironment parentEnv = null;
				while ((parentEnv = emilEnvRepo.getEmilEnvironmentById(currentEnv.getParentEnvId())) != null) {
					if (currentEnv.getEnvId().equals(revId))
						break;

					emilEnvsToDelete.add(currentEnv.getEnvId());
					emilEnvRepo.save(currentEnv, false);
					currentEnv = parentEnv;
				}

				if (!currentEnv.getEnvId().equals(revId))
					return EnvironmentRepository.errorMessageResponse("Reverting to revision '" + revId + "' failed!");

				emilEnvRepo.save(currentEnv, false);
				for (String id : emilEnvsToDelete)
					emilEnvRepo.delete(id, true, true);

				return EnvironmentRepository.successMessageResponse("Environment reverted to revision '" + revId + "'");
			}
			catch (BWFLAException | JsonSyntaxException | JsonIOException error) {
				return EnvironmentRepository.errorMessageResponse("No emil environment found with ID: " + currentEnv.getParentEnvId());
			}
		}
	}


	public class Templates
	{
		@GET
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		/**
		 *
		 * for old-style template use ?compat=... get parameter
		 */
		public Response list(@QueryParam("compat") @DefaultValue("newStyle") String compat)
		{
			LOG.info("Listing environment templates...");
			try {
				if(compat.equals("newStyle")) {
					List<MachineConfigurationTemplate> templates = envdb.getTemplates();
					return Response.status(Status.OK)
							.entity(templates)
							.build();
				}
				else {
					try {
						final StringWriter output = new StringWriter();
						final JsonGenerator json = Json.createGenerator(output);
						json.writeStartObject();
						json.write("status", "0");
						json.writeStartArray("systems");
						for (MachineConfiguration machine : envdb.getTemplates()) {
							json.writeStartObject();
							json.write("id", machine.getId());
							json.write("label", machine.getDescription().getTitle());
							if (machine.getNativeConfig() != null)
								json.write("native_config", machine.getNativeConfig().getValue());
							else json.write("native_config", "");

							json.writeStartArray("properties");
							if (machine.getArch() != null && !machine.getArch().isEmpty()) {
								json.writeStartObject();
								json.write("name", "Architecture");
								json.write("value", machine.getArch());
								json.writeEnd();
							}

							final String emubean = (machine.getEmulator() != null) ? machine.getEmulator().getBean() : null;
							if (emubean != null && !emubean.isEmpty()) {
								json.writeStartObject();
								json.write("name", "EmulatorContainer");
								json.write("value", emubean);
								json.writeEnd();
							}

							json.writeEnd();
							json.writeEnd();
						}

						json.writeEnd();
						json.writeEnd();
						json.flush();
						json.close();

						return EnvironmentRepository.createResponse(Status.OK, output.toString());
					}
					catch (Throwable error) {
						return EnvironmentRepository.internalErrorResponse(error);
					}
				}
			}
			catch (Throwable error) {
				return EnvironmentRepository.internalErrorResponse(error);
			}
		}
	}


	public class Patches
	{
		@GET
		@Secured({Role.RESTRCITED})
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
		public List<GeneralizationPatch> list() throws BWFLAException, JAXBException
		{
			LOG.info("Listing environment patches...");

			// TODO: fix response in case of errors!
			return envdb.getPatches();
		}
	}


	public class Actions
	{
		/** Initialize internal database of environments. */
		@POST
		@Path("/prepare")
		@Secured({Role.PUBLIC})
		@Produces(MediaType.APPLICATION_JSON)
		public Response prepare()
		{
			LOG.info("Preparing environment-repository...");
			try {
				return EnvironmentRepository.successMessageResponse("import of " + emilEnvRepo.initialize() + " environments completed");
			}
			catch (Throwable t) {
				return EnvironmentRepository.internalErrorResponse(t);
			}
		}

		/** Synchronize internal database with the image archives. */
		@POST
		@Path("/sync")
		@Secured({Role.RESTRCITED})
		@Produces(MediaType.APPLICATION_JSON)
		public Response sync()
		{
			LOG.info("Syncing internal DB...");
			envdb.sync();
			emilEnvRepo.init();
			return Emil.successMessageResponse("syncing archives ");
		}


		/** create new image */
		@POST
		@Path("/create-image")
		@Secured({Role.RESTRCITED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public TaskStateResponse createImage(ImageCreateRequest imageReq)
		{
			LOG.info("Create empty image ...");

			return new TaskStateResponse(taskManager.submitTask(new CreateEmptyImageTask(imageReq.getSize(), LOG)));
		}

		/** Import an image for new environment */
		@POST
		@Path("/import-image")
		@Secured({Role.RESTRCITED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public TaskStateResponse importImage(ImportImageRequest imageReq)
		{
			LOG.info("Importing image ...");

			ImportImageTaskRequest request = new ImportImageTaskRequest();

			URL url;
			try {
				url = new URL(imageReq.getUrl());
				if(url.getProtocol().equalsIgnoreCase("file"))
					return new TaskStateResponse((new BWFLAException("invalid url format")));
			} catch (MalformedURLException me) {
				String filename = imageReq.getUrl();
				if (filename == null || filename.contains("/"))
					return new TaskStateResponse(new BWFLAException("filename must not be null/empty or contain '/' characters: " + filename));
				File image = new File("/eaas/import/", filename);
				LOG.info("path: " + image);
				if (!image.exists())
					return new TaskStateResponse(new BWFLAException("image : " + filename + " not found."));

				try {
					url = image.toURI().toURL();
				} catch (MalformedURLException e) {
					return new TaskStateResponse(new BWFLAException(e));
				}
			}
			request.url = url;
			request.destArchive = "default";
			request.environmentHelper = envdb;
			request.label = imageReq.getLabel();

			try {
				request.validate();
			} catch (BWFLAException e) {
				e.printStackTrace();
				return new TaskStateResponse(e);
			}

			return new TaskStateResponse(taskManager.submitTask(new ImportImageTask(request, LOG)));
		}

		/** Replicate an image from remote archives */
		@POST
		@Path("/replicate-image")
		@Secured({Role.RESTRCITED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public ReplicateImagesResponse replicateImage(ReplicateImagesRequest replicateImagesRequest)
		{
			LOG.info("Replicating remote image...");

			if (replicateImagesRequest.getReplicateList() == null)
				return new ReplicateImagesResponse(new BWFLAException("no environments given"));

			ReplicateImagesResponse response = new ReplicateImagesResponse();
			List<String> taskList = new ArrayList<String>();

			ReplicateImageTask.ReplicateImageTaskRequest importRequest = new ReplicateImageTask.ReplicateImageTaskRequest();

			for (String envId : replicateImagesRequest.getReplicateList()) {
				EmilEnvironment emilEnvironment = emilEnvRepo.getEmilEnvironmentById(envId);
				if (emilEnvironment == null) {
					LOG.severe("Environment " + envId + " not found");
					continue;
				}
				try {
					importRequest.env = envdb.getEnvironmentById(emilEnvironment.getArchive(), envId);
					importRequest.repository = emilEnvRepo;
					importRequest.emilEnvironment = emilEnvironment;
				}
				catch (BWFLAException error) {
					LOG.log(Level.WARNING, "Looking up environment failed!", error);
				}

				importRequest.environmentHelper = envdb;
				importRequest.destArchive = replicateImagesRequest.getDestArchive();
				importRequest.imageProposer = imageProposer;
				importRequest.containerUtil = containerUtil;
				if (authenticatedUser != null)
					importRequest.username = authenticatedUser.getUsername();

				try {
					importRequest.validate();
				}
				catch (BWFLAException error) {
					LOG.log(Level.WARNING, "Validating image replication request failed!", error);
					return new ReplicateImagesResponse(error);
				}

				taskList.add(taskManager.submitTask(new ReplicateImageTask(importRequest, LOG)));
			}

			response.setTaskList(taskList);
			return response;
		}

		@POST
		@Path("/delete-image")
		@Secured({Role.PUBLIC})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public Response deleteImage(DeleteImageRequest request) throws BWFLAException
		{
			LOG.info("delete image");
			envdb.deleteNameIndexesEntry(request.getImageArchive(), request.getImageId(), null);
			envdb.deleteImage(request.getImageArchive(), request.getImageId(), ImageType.USER);
			return Response.status(Status.OK)
					.build();
		}
	}

	// helper
	private static void driveUpdateHelper(MachineConfiguration env, List<EnvironmentCreateRequest.DriveSetting> driveSettings, EmilObjectData objects) throws BWFLAException {
		if(driveSettings == null)
			return;

		for (EnvironmentCreateRequest.DriveSetting ds : driveSettings) {
			if (ds.getObjectId() != null && ds.getObjectArchive() != null) {
				FileCollection fc = objects.getFileCollection(ds.getObjectArchive(), ds.getObjectId());
				ObjectArchiveBinding binding = new ObjectArchiveBinding(objects.helper().getHost(), ds.getObjectArchive(), ds.getObjectId());
				if (EmulationEnvironmentHelper.addObjectArchiveBinding(env, binding, fc, ds.getDriveIndex()) < 0)
					throw new BadRequestException(Response
							.status(Response.Status.BAD_REQUEST)
							.entity(new ErrorInformation("could not insert object"))
							.build());
			} else if (ds.getImageId() != null && ds.getImageArchive() != null) {
				ImageArchiveBinding binding = new ImageArchiveBinding(ds.getImageArchive(),
						"",
						ds.getImageId(),
						ImageType.USER.value());
				binding.setId(ds.getImageId());
				env.getAbstractDataResource().add(binding);
				EmulationEnvironmentHelper.setDrive(env, ds.getDrive(), ds.getDriveIndex());
				if (EmulationEnvironmentHelper.registerDrive(env, binding.getId(), null, ds.getDriveIndex()) < 0)
					throw new BadRequestException(Response
							.status(Response.Status.BAD_REQUEST)
							.entity(new ErrorInformation("could not insert image"))
							.build());
			} else {
				EmulationEnvironmentHelper.registerEmptyDrive(env, ds.getDriveIndex());
			}
		}
	}
}
