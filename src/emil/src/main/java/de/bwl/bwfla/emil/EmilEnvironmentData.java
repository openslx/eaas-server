package de.bwl.bwfla.emil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.datatypes.identification.OperatingSystems;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.emil.datatypes.rest.ReplicateImagesResponse;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Role;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import de.bwl.bwfla.emil.tasks.ExportEnvironmentTask;
import de.bwl.bwfla.emil.utils.ContainerUtil;
import de.bwl.bwfla.emil.utils.TaskManager;
import de.bwl.bwfla.emil.tasks.ImportImageTask;
import de.bwl.bwfla.emil.tasks.ReplicateImageTask;
import de.bwl.bwfla.emucomp.api.*;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.JsonBuilder;
import de.bwl.bwfla.emucomp.api.MachineConfiguration.NativeConfig;
import de.bwl.bwfla.imageproposer.client.ImageProposer;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;
import de.bwl.bwfla.emil.tasks.ImportImageTask.ImportImageTaskRequest;

@Path("EmilEnvironmentData")
@ApplicationScoped
public class EmilEnvironmentData extends EmilRest {

	@Inject
	private DatabaseEnvironmentsAdapter environments;

	@Inject
	@Config(value = "emil.imageproposerservice")
	private String imageProposerService;

	@Inject
	private EmilEnvironmentRepository emilEnvRepo;

	private ImageProposer imageProposer;

	@Inject
	private ContainerUtil containerUtil;

	@Inject
	private TaskManager taskManager;

	@Inject
	@AuthenticatedUser
	private UserContext authenticatedUser = null;

	@Inject
	private ObjectClassification classification;

	@Inject
	@Config(value = "ws.imagearchive")
	private String imageArchive;

	@PostConstruct
	private void initialize() {
		try {
			imageProposer = new ImageProposer(imageProposerService + "/imageproposer");
		} catch (IllegalArgumentException e) { }
	}

	@Secured({Role.PUBLIC})
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEnvironments(@Context final HttpServletResponse response)
	{
		try {
			List<EmilEnvironment> environments = emilEnvRepo.getEmilEnvironments();
			List<EnvironmentListItem> result = new ArrayList<>();

			for (EmilEnvironment emilenv : environments) {
				result.add(new EnvironmentListItem(emilenv));
			}

			return Response.status(Response.Status.OK).entity(result).build();
		} catch (Throwable t) {
			t.printStackTrace();
			throw new BadRequestException(Response
				.status(Response.Status.BAD_REQUEST)
				.entity(new ErrorInformation(t.getMessage()))
				.build());
		}
	}

	@Secured({Role.PUBLIC})
	@GET
	@Path("/{envId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEnvironment(@PathParam("envId") String envId,
									   @Context final HttpServletResponse response) {

		EmilEnvironment emilenv = emilEnvRepo.getEmilEnvironmentById(envId);
		if(emilenv == null)
		{
			throw new BadRequestException(Response
					.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorInformation("environment id not found " + envId))
					.build());
		}

		try {
		// Add all environments to the response...
			MachineConfiguration machineConf = null;

			Environment env = environments.getEnvironmentById(emilenv.getArchive(), emilenv.getEnvId());
			if (env instanceof MachineConfiguration)
				machineConf = (MachineConfiguration) env;

			List<EmilEnvironment> parents = emilEnvRepo.getParents(emilenv.getEnvId());
			EnvironmentDetails result = new EnvironmentDetails(emilenv, machineConf, parents);
			return Response.status(Response.Status.OK).entity(result).build();

		} catch (BWFLAException e) {
			e.printStackTrace();
			throw new BadRequestException(Response
					.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorInformation("failed retrieving data"))
					.build());
		}
	}

	/**
	 * Creates EmilEnvironments for all environments found at the image archive.
	 *
	 * @return
	 */
	@Secured({Role.PUBLIC})
	@GET
	@Path("/init")
	@Produces(MediaType.APPLICATION_JSON)
	public Response init() {
		try {
			return Emil.successMessageResponse("import of " + emilEnvRepo.initialize() + " environments completed");
		} catch (Throwable t) {
			return Emil.internalErrorResponse(t);
		}
	}


	@Secured({Role.PUBLIC})
	@GET
	@Path("/objectDependencies")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getObjectDependencies(@QueryParam("envId") String envId)
	{
		if(envId == null || envId.trim().isEmpty()){
			return new ArrayList<>();
		}
		return classification.getEnvironmentDependencies(envId);
	}


	@Secured({Role.RESTRCITED})
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(EnvironmentDeleteRequest desc) throws JAXBException {
		if (desc.getEnvId() == null || desc.getEnvId().trim().isEmpty()) {
			return Emil.errorMessageResponse("Emil environment ID is null or empty");
		}

		if (!desc.getDeleteMetaData() && !desc.getDeleteImage())
			return Emil.successMessageResponse("nothing to be deleted");

		List<String> objectDependencies = classification.getEnvironmentDependencies(desc.getEnvId());
		System.out.println("size " + objectDependencies.size() + " " + desc.isForce());
		if(objectDependencies != null && objectDependencies.size() > 0 && !desc.isForce())
		{
			final String json = createJsonResponse("2", objectDependencies.toString());
			return createResponse(Status.OK, json);
		}

		try {
			emilEnvRepo.delete(desc.getEnvId(), desc.getDeleteMetaData(), desc.getDeleteImage());
		} catch (BWFLAException e1) {
			return Emil.internalErrorResponse(e1);
		}

		imageProposer.refreshIndex();
		return Emil.successMessageResponse("delete success!");
	}

//	@Secured
//	@GET
//	@Path("/remoteList")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response remoteList(@QueryParam("host") String host, @QueryParam("type") String type) {
//		String hostUrl;
//		try {
//			hostUrl = URLDecoder.decode(host, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			LOG.log(Level.SEVERE, e.getMessage(), e);
//			return Emil.errorMessageResponse(e.getMessage());
//		}
//
//		List<Environment> envs;
//		EnvironmentsAdapter remoteArchive = new EnvironmentsAdapter(hostUrl);
//		try {
//			envs = remoteArchive.getEnvironments(type);
//			LOG.info(remoteArchive.toString());
//		} catch (BWFLAException | JAXBException e) {
//			LOG.log(Level.SEVERE, e.getMessage(), e);
//			return Emil.errorMessageResponse(e.getMessage());
//		}
//
//		if (envs == null)
//			return Emil.errorMessageResponse("no envs");
//
//
//		try {
//			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
//			json.beginObject();
//			json.add("status", "0");
//			json.name("environments");
//			json.beginArray();
//
//			for (Environment m : envs) {
//				json.beginObject();
//				json.add("envId", m.getId());
//				json.endObject();
//			}
//
//			json.endArray();
//			json.endObject();
//			json.finish();
//
//			return Emil.createResponse(Status.OK, json.toString());
//
//		} catch (Throwable t) {
//			return Emil.internalErrorResponse(t);
//		}
//	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/getDatabaseContent")
	@Produces(MediaType.APPLICATION_JSON)
	public <T extends JaxbType> Response getDatabaseContent(@QueryParam("type") String type, @QueryParam("className") String className) {
		try {
			Class<T> classType = (Class<T>) Class.forName(className);
			if (classType == null) {
				throw new BWFLAException("Class name is incorrect!");
			}
			return Emil.createResponse(Status.OK, emilEnvRepo.getDatabaseContent(type, classType));
		} catch (ClassNotFoundException | BWFLAException e) {
			LOG.warning("getDatabaseContent failed!\n" + e.getMessage());
			return Emil.internalErrorResponse(e);
		}
	}

	@Secured({Role.RESTRCITED})
	@POST
	@Path("/createEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 */
	public Response createEnvironment(EnvironmentCreateRequest envReq) {
		final DatabaseEnvironmentsAdapter environmentHelper = environments;

		if(envReq.getTemplateId() == null)
			return Emil.errorMessageResponse("invalid template id");

		try {
			MachineConfiguration pEnv = environmentHelper.getTemplate(envReq.getTemplateId());
			if (pEnv == null)
				return Emil.errorMessageResponse("invalid template id: " + envReq.getTemplateId());
			MachineConfiguration env = pEnv.copy(); // don't modify the real
			// template
			env.getDescription().setTitle(envReq.getLabel());
			if (env.getNativeConfig() == null)
				env.setNativeConfig(new NativeConfig());

			env.getNativeConfig().setValue(envReq.getNativeConfig());

			ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
			iaMd.setType(ImageType.TMP);
			String id = environmentHelper.createEnvironment("default", env, envReq.getSize(), iaMd);
			if (id == null) {
				return Emil.errorMessageResponse("failed to create image");
			}

			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.add("id", id);
			json.endObject();
			json.finish();
			return Emil.createResponse(Status.OK, json.toString());

		} catch (Throwable t) {
			return Emil.errorMessageResponse(t.getMessage());
		}
	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/getEnvironmentTemplates")
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
	public Response getEnvironmentTemplates() {
		final DatabaseEnvironmentsAdapter environmentHelper = environments;
		try {
			List<MachineConfigurationTemplate> envs = environmentHelper.getTemplates();

			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");

			json.name("systems");
			json.beginArray();
			for (MachineConfiguration e : envs) {
				json.beginObject();
				json.add("id", e.getId());
				json.add("label", e.getDescription().getTitle());
				if (e.getNativeConfig() != null)
					json.add("native_config", e.getNativeConfig().getValue());
				else
					json.add("native_config", "");
				json.name("properties");
				json.beginArray();

				if (e.getArch() != null && !e.getArch().isEmpty()) {
					json.beginObject();
					json.add("name", "Architecture");
					json.add("value", e.getArch());
					json.endObject();
				}

				if (e.getEmulator() != null && e.getEmulator().getBean() != null
						&& !e.getEmulator().getBean().isEmpty()) {
					json.beginObject();
					json.add("name", "EmulatorContainer");
					json.add("value", e.getEmulator().getBean());
					json.endObject();
				}

				json.endArray();
				json.endObject();
			}
			json.endArray();
			json.endObject();
			json.finish();
			return Emil.createResponse(Status.OK, json.toString());
		} catch (Throwable t) {
			return Emil.internalErrorResponse(t);
		}
	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/getPatches")
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
	public List<GeneralizationPatch> getPatches() throws BWFLAException, JAXBException {
		final DatabaseEnvironmentsAdapter environmentHelper = environments;
		try {
			List<GeneralizationPatch> envs = environmentHelper.getPatches();
			return envs;
		} catch (Throwable t) {
			throw t;
		}
	}


	/**
	 * Updates the description of a specified Emil environment. This method
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
	@Secured({Role.RESTRCITED})
	@POST
	@Path("/updateDescription")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDescription(UpdateEnvironmentDescriptionRequest desc) {
		boolean imported = false;
		final String curEnvId = desc.getEnvId();
		if(curEnvId == null)
			return Emil.errorMessageResponse("envId was null");

		EmilEnvironment currentEnv = emilEnvRepo.getEmilEnvironmentById(curEnvId);
		EmilEnvironment newEnv;

		if(currentEnv == null) {
			return Emil.errorMessageResponse("No emil environment found with ID: " + curEnvId);
		}

		try {
			Environment environment = environments.getEnvironmentById(currentEnv.getArchive(), desc.getEnvId());

			if(environment instanceof MachineConfiguration)
			{
				MachineConfiguration machineConfiguration = (MachineConfiguration) environment;
				machineConfiguration.setOperatingSystemId(desc.getOs());
				if(desc.getNativeConfig() != null) {
					if(machineConfiguration.getNativeConfig() == null)
						machineConfiguration.setNativeConfig(new NativeConfig());
					machineConfiguration.getNativeConfig().setValue(desc.getNativeConfig());
				}
				if(machineConfiguration.getUiOptions() == null)
					machineConfiguration.setUiOptions(new UiOptions());
				if(desc.isUseXpra())
				{
					machineConfiguration.getUiOptions().setForwarding_system("XPRA");
				}
				else
					machineConfiguration.getUiOptions().setForwarding_system(null);

				if(desc.isUseWebRTC())
				{
					machineConfiguration.getUiOptions().setAudio_system("webRTC");
				}
				else
					machineConfiguration.getUiOptions().setAudio_system(null);

				if (machineConfiguration.getUiOptions().getHtml5() == null)
					machineConfiguration.getUiOptions().setHtml5(new Html5Options());

//				Do not check for null. EmuBean would choose latest, if value is null
				machineConfiguration.getEmulator().setContainerName(desc.getContainerEmulatorName());
				machineConfiguration.getEmulator().setContainerVersion(desc.getContainerEmulatorVersion());

				machineConfiguration.getUiOptions().getHtml5().setPointerLock(desc.isEnableRelativeMouse());

				machineConfiguration.setDrive(desc.getDrives());
				machineConfiguration.setLinuxRuntime(desc.isLinuxRuntime());

				if(desc.getNetworking() != null && desc.getNetworking().isEnableInternet())
				{
					List<Nic> nics = machineConfiguration.getNic();
					if (nics.size() == 0) {
						Nic nic = new Nic();
						nic.setHwaddress(NetworkUtils.getRandomHWAddress());
						nics.add(nic);
					}
				}
			}

			environment.setUserTag(desc.getUserTag());
			if(!currentEnv.getArchive().equals("default")) {
				// we need to import / duplicate the env

				if(currentEnv instanceof EmilObjectEnvironment)
					newEnv = new EmilObjectEnvironment(currentEnv);
				else
					newEnv = new EmilEnvironment(currentEnv);

				ImageArchiveMetadata md = new ImageArchiveMetadata();
				md.setType(ImageType.USER);
				newEnv.setArchive("default");
				String id = environments.importMetadata("default", environment, md, false);
				newEnv.setEnvId(id);
				newEnv.setParentEnvId(currentEnv.getEnvId());
				currentEnv.addChildEnvId(newEnv.getEnvId());
				imported = true;
			}
			else {
				environments.updateMetadata(currentEnv.getArchive(), environment);
				newEnv = currentEnv;
			}
			imageProposer.refreshIndex();
		} catch (BWFLAException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return Emil.errorMessageResponse("No emulation environment found with ID: " + curEnvId);
		}

		newEnv.setTitle(desc.getTitle());
		newEnv.setAuthor(desc.getAuthor());
		newEnv.setDescription(desc.getDescription());
		newEnv.setEnablePrinting(desc.isEnablePrinting());
		newEnv.setEnableRelativeMouse(desc.isEnableRelativeMouse());
		newEnv.setShutdownByOs(desc.isShutdownByOs());
		newEnv.setNetworking(desc.getNetworking());
		newEnv.setCanProcessAdditionalFiles(desc.canProcessAdditionalFiles());
		newEnv.setXpraEncoding(desc.getXpraEncoding());
		newEnv.setLinuxRuntime(desc.isLinuxRuntime());
		newEnv.setHelpText(desc.getHelpText());

		if (desc.getTime() != null) {
			newEnv.setTimeContext(desc.getTime());
		} else {
			newEnv.setTimeContext(null);
		}

		try {
			if(imported) {
				// emilEnvRepo.save(currentEnv, false);
				emilEnvRepo.save(newEnv, true);
			}
			else
				emilEnvRepo.save(newEnv, false);
		} catch (BWFLAException e) {
			return Emil.internalErrorResponse(e);
		}

		final String json = "{\"status\":\"0\", \"id\":\"" + newEnv.getEnvId() + "\"}";
		return Emil.createResponse(Status.OK, json);
	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/defaultEnvironments")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> defaultEnvironments()
	{
		Map<String, String> map = new HashMap<>();
		try {
			List<DefaultEntry> defaultEnvironments = environments.getDefaultEnvironments();
			for(DefaultEntry e : defaultEnvironments)
			{
				map.put(e.getKey(), e.getValue());
			}
			return map;
		} catch (BWFLAException e) {
			e.printStackTrace();
			Emil.errorMessageResponse(e.getMessage());
			return null;
		}
	}

	@GET
	@Path("/defaultEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	public DefaultEnvironmentResponse defaultEnvironment(@QueryParam("osId") String osId) {

		try {
			String env = environments.getDefaultEnvironment(osId);
			DefaultEnvironmentResponse response = new DefaultEnvironmentResponse();
			response.setEnvId(env);
			return response;
		} catch (BWFLAException e) {
			return new DefaultEnvironmentResponse(e);
		}
	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/setDefaultEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	public EmilResponseType setDefaultEnvironment(@QueryParam("osId") String osId, @QueryParam("envId") String envId) {
		try {
			environments.setDefaultEnvironment(osId, envId);
			return new EmilResponseType();
		} catch (BWFLAException e) {
			return new EmilResponseType(e);
		}
	}

	@Secured({Role.RESTRCITED})
	@POST
	@Path("forkRevision")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 */
	public Response forkRevision(ForkRevisionRequest req) {
		EmilEnvironment emilEnv = emilEnvRepo.getEmilEnvironmentById(req.getId());
		if(emilEnv == null) {
			return Emil.internalErrorResponse("not found: " + req.getId());
		}
		try {
			Environment environment = environments.getEnvironmentById(emilEnv.getArchive(), req.getId());
			ImageArchiveMetadata md = new ImageArchiveMetadata();
			md.setType(ImageType.USER);
			String id = environments.importMetadata("default", environment, md, false);
			EmilEnvironment newEmilEnv = new EmilEnvironment(emilEnv);
			newEmilEnv.setEnvId(id);
			newEmilEnv.setTitle("[fork]: " + newEmilEnv.getTitle() + " " + newEmilEnv.getEnvId());
			newEmilEnv.setArchive("default");
			newEmilEnv.setParentEnvId(emilEnv.getParentEnvId());
			emilEnvRepo.save(newEmilEnv, true);
		} catch (BWFLAException  e) {
			return Emil.internalErrorResponse(e);
		}

		return Emil.successMessageResponse("forked environment: " + req.getId());
	}

	@Secured({Role.RESTRCITED})
	@POST
	@Path("revertRevision")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 */
	public synchronized Response revertRevision(RevertRevisionRequest req) {
		EmilEnvironment currentEnv;

		if (req.getCurrentId() == null || req.getRevId() == null)
			return Emil.errorMessageResponse("Invalid Request");

		List<String> deleteList= new ArrayList<>();
		currentEnv = emilEnvRepo.getEmilEnvironmentById(req.getCurrentId());

		if(currentEnv == null) {
			return Emil.errorMessageResponse("No emil environment found with ID: " + req.getCurrentId());
		}

		LOG.info("current: " + req.getCurrentId() + "  req rev: " + req.getRevId());

		try {
			EmilEnvironment parentEnv = emilEnvRepo.getEmilEnvironmentById(currentEnv.getParentEnvId());
			if (parentEnv != null) {
				do {
					if (currentEnv.getEnvId().equals(req.getRevId()))
						break;

					deleteList.add(currentEnv.getEnvId());
					emilEnvRepo.save(currentEnv, false);

					currentEnv = parentEnv;
					parentEnv = emilEnvRepo.getEmilEnvironmentById(currentEnv.getParentEnvId());

				} while (parentEnv != null);
			}
			if (!currentEnv.getEnvId().equals(req.getRevId()))
				return Emil.errorMessageResponse("could not revert to Revision: " + req.getRevId());

			emilEnvRepo.save(currentEnv, false);
			for(String id: deleteList)
				emilEnvRepo.delete(id, true, true);

			return Emil.successMessageResponse("reverted to environment: " + req.getRevId());

		} catch (BWFLAException | JsonSyntaxException | JsonIOException e) {
			return Emil.errorMessageResponse("No emil environment found with ID: " + currentEnv.getParentEnvId());
		}
	}

	@Secured({Role.PUBLIC})
	@GET
	@Path("operatingSystemMetadata")
	@Produces(MediaType.APPLICATION_JSON)
	public OperatingSystems getOperatingSystemMetadata()
	{
		String serverDataDir = ConfigurationProvider.getConfiguration().get("commonconf.serverdatadir");
		File osInfo = new File(serverDataDir, "operating-systems.json");
		if(!osInfo.exists())
		{
			LOG.severe("no operating-systems.json found.");
			return null;
		}

		byte[] encoded = new byte[0];
		try {
			encoded = Files.readAllBytes(osInfo.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		String json = new String(encoded);

		OperatingSystems metaData = null;
		try {
			metaData = OperatingSystems.fromJsonValue("{ \"operatingSystems\" : " + json + "}", OperatingSystems.class);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return metaData;
	}

	@Secured({Role.RESTRCITED})
	@GET
	@Path("/sync")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sync() {
		environments.sync();
		emilEnvRepo.init();
		return Emil.successMessageResponse("syncing archives ");
	}

	@Secured({Role.RESTRCITED})
	@POST
	@Path("/importImage")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 */
	public TaskStateResponse importImage(ImportImageRequest imageReq) {

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
		request.environmentHelper = environments;
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

	@Secured({Role.PUBLIC})
	@GET
	@Path("/getNameIndexes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 */
    public ImageNameIndex getNameIndexes() throws BWFLAException, JAXBException {
        return environments.getNameIndexes();
    }

//	@POST
//	@Path("/exportToRemoteArchive")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public TaskStateResponse exportToRemoteArchive(RemoteExportRequest req) {
//		if (req.getWsHost() == null || req.getEnvId() == null)
//			return new TaskStateResponse(new BWFLAException("invalid arguments"));
//
//		String host = null;
//		try {
//			host = URLDecoder.decode(req.getWsHost(), "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			LOG.log(Level.SEVERE, e.getMessage(), e);
//			return new TaskStateResponse(new BWFLAException("metadata import failed"));
//		}
//		return new TaskStateResponse(taskManager.submitTask(new ExportImageTask(host, req.getObjectArchiveHost(),
//				req.getEnvId(), req.isExportObjectEmbedded())));
//	}

	@Secured({Role.RESTRCITED})
	@POST
	@Path("/replicateImage")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 */
	public ReplicateImagesResponse replicateImage(ReplicateImagesRequest replicateImagesRequest) {
		if(replicateImagesRequest.getReplicateList() == null)
			return new ReplicateImagesResponse(new BWFLAException("no environments given"));

		ReplicateImagesResponse response = new ReplicateImagesResponse();
		List<String> taskList = new ArrayList<String>();

		ReplicateImageTask.ReplicateImageTaskRequest importRequest = new ReplicateImageTask.ReplicateImageTaskRequest();

		for(String envId : replicateImagesRequest.getReplicateList())
		{
			EmilEnvironment emilEnvironment = emilEnvRepo.getEmilEnvironmentById(envId);
			MachineConfiguration env;
			if(emilEnvironment == null) {
				LOG.severe("Environment " + envId + " not found");
				continue;
			}
			try {
				Environment e = environments.getEnvironmentById(emilEnvironment.getArchive(), envId);
//				if(!(e instanceof MachineConfiguration))
//					continue;
//				env = e;
				importRequest.env = e;

				importRequest.repository = emilEnvRepo;
				importRequest.emilEnvironment = emilEnvironment;
			} catch (BWFLAException e) {
				e.printStackTrace();
			}

			importRequest.environmentHelper = environments;
			importRequest.destArchive = replicateImagesRequest.getDestArchive();
			importRequest.imageProposer = imageProposer;
			importRequest.containerUtil = containerUtil;

			if(authenticatedUser != null)
				importRequest.username = authenticatedUser.getUsername();

			try {
				importRequest.validate();
			} catch (BWFLAException e) {
				e.printStackTrace();
				return new ReplicateImagesResponse(e);
			}

			taskList.add(taskManager.submitTask(new ReplicateImageTask(importRequest, LOG)));
		}
		response.setTaskList(taskList);
		return response;
	}


//		@POST
//	@Path("/exportToRemoteArchive")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public TaskStateResponse exportToRemoteArchive(RemoteExportRequest req) {
//		if (req.getWsHost() == null || req.getEnvId() == null)
//			return new TaskStateResponse(new BWFLAException("invalid arguments"));
//
//		String host = null;
//		try {
//			host = URLDecoder.decode(req.getWsHost(), "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			LOG.log(Level.SEVERE, e.getMessage(), e);
//			return new TaskStateResponse(new BWFLAException("metadata import failed"));
//		}
//		return new TaskStateResponse(taskManager.submitTask(new ExportImageTask(host, req.getObjectArchiveHost(),
//				req.getEnvId(), req.isExportObjectEmbedded())));
//	}

	@POST
	@Path("/export")
	@Secured({Role.RESTRCITED})
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse export(ExportRequest exportRequest) {
		if (exportPath == null || exportPath.isEmpty())
			return new TaskStateResponse(new BWFLAException("Emil export is not configured "));

		ExportEnvironmentTask.ExportEnvironmentRequest request = new ExportEnvironmentTask.ExportEnvironmentRequest();
		request.exportFilePath = exportPath;
		request.envId = exportRequest.getEnvId();
		request.envHelper = environments;
		request.archive = exportRequest.getArchive();
		request.environmentRepository  = emilEnvRepo;
		request.userCtx =  null;
		
		if(authenticatedUser != null)
			request.userCtx = authenticatedUser.getUsername();

		return new TaskStateResponse(taskManager.submitTask(new ExportEnvironmentTask(request)));
	}


}
