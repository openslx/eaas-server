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
import de.bwl.bwfla.api.imagearchive.DefaultEntry;
import de.bwl.bwfla.api.imagearchive.ImageArchiveMetadata;
import de.bwl.bwfla.api.imagearchive.ImageNameIndex;
import de.bwl.bwfla.api.imagearchive.ImageType;
import de.bwl.bwfla.common.datatypes.identification.OperatingSystems;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.DefaultEnvironmentResponse;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import de.bwl.bwfla.emil.datatypes.EnvironmentCreateRequest;
import de.bwl.bwfla.emil.datatypes.EnvironmentDeleteRequest;
import de.bwl.bwfla.emil.datatypes.ErrorInformation;
import de.bwl.bwfla.emil.datatypes.ImportImageRequest;
import de.bwl.bwfla.emil.datatypes.rest.EmilResponseType;
import de.bwl.bwfla.emil.datatypes.rest.EnvironmentDetails;
import de.bwl.bwfla.emil.datatypes.rest.EnvironmentListItem;
import de.bwl.bwfla.emil.datatypes.rest.ExportRequest;
import de.bwl.bwfla.emil.datatypes.rest.ReplicateImagesRequest;
import de.bwl.bwfla.emil.datatypes.rest.ReplicateImagesResponse;
import de.bwl.bwfla.emil.datatypes.rest.TaskStateResponse;
import de.bwl.bwfla.emil.datatypes.rest.UpdateEnvironmentDescriptionRequest;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Role;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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


	// ========== Subresources ==============================

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
				if (pEnv == null)
					return EnvironmentRepository.errorMessageResponse("invalid template id: " + envReq.getTemplateId());

				MachineConfiguration env = pEnv.copy(); // don't modify the real template
				env.getDescription().setTitle(envReq.getLabel());
				if (env.getNativeConfig() == null)
					env.setNativeConfig(new NativeConfig());

				env.getNativeConfig().setValue(envReq.getNativeConfig());

				ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
				iaMd.setType(ImageType.TMP);
				String id = envdb.createEnvironment("default", env, envReq.getSize(), iaMd);
				if (id == null) {
					return EnvironmentRepository.errorMessageResponse("failed to create image");
				}

				final JsonObject json = Json.createObjectBuilder()
						.add("status", "0")
						.add("id", id)
						.build();

				return EnvironmentRepository.createResponse(Status.OK, json);
			}
			catch (Throwable error) {
				return EnvironmentRepository.errorMessageResponse(error.getMessage());
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
		 * @return
		 *
		 * 		{"status": "0", "systems": [{"id": "abc", "label": "Windows XP
		 *         SP1", "native_config": "test", "properties": [{"name":
		 *         "Architecture", "value": "x86_64"}, {"name": "Fun Fact", "value":
		 *         "In 1936, the Russians made a computer that ran on water"}]}]}
		 */
		public Response list()
		{
			LOG.info("Listing environment templates...");
			try {
				List<MachineConfigurationTemplate> templates = envdb.getTemplates();
				return Response.status(Status.OK)
						.entity(templates)
						.build();
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

		/** Import an image for new environment */
		@POST
		@Path("/import-image")
		@Secured({Role.RESTRCITED})
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public TaskStateResponse importImage(ImportImageRequest imageReq)
		{
			LOG.info("Importing image for new environment...");

			ImportImageTaskRequest request = new ImportImageTaskRequest();

			URL url;
			try {
				url = new URL(imageReq.getUrlString());
			} catch (MalformedURLException me) {
				String filename = imageReq.getUrlString();
				if (filename == null || filename.contains("/"))
					return new TaskStateResponse(new BWFLAException("filename must not be null/empty or contain '/' characters:" + filename));
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

			if (imageReq.getRom() != null) {
				File romFile = new File("/eaas/roms", imageReq.getRom());
				if (!romFile.exists())
					return new TaskStateResponse(new BWFLAException("rom file not found"));
				request.romFile = romFile;
			}
			request.destArchive = "default";
			request.templateId = imageReq.getTemplateId();
			request.nativeConfig = imageReq.getNativeConfig();
			request.environmentHelper = envdb;
			request.imageProposer = imageProposer;
			request.patchId = imageReq.getPatchId();

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
	}
}
