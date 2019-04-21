package de.bwl.bwfla.emil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.datatypes.identification.OperatingSystems;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.classification.ArchiveAdapter;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.emil.datatypes.rest.ReplicateImagesResponse;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import de.bwl.bwfla.emil.utils.ContainerUtil;
import de.bwl.bwfla.emil.utils.tasks.ImportImageTask;
import de.bwl.bwfla.emil.utils.tasks.ReplicateImageTask;
import de.bwl.bwfla.emucomp.api.*;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.JsonBuilder;
import de.bwl.bwfla.emucomp.api.MachineConfiguration.NativeConfig;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imageproposer.client.ImageProposer;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;
import de.bwl.bwfla.emil.utils.tasks.ImportImageTask.ImportImageTaskRequest;
import org.openjena.atlas.logging.Log;

@Path("EmilEnvironmentData")
@ApplicationScoped
public class EmilEnvironmentData extends EmilRest {

	@Inject
	private DatabaseEnvironmentsAdapter envHelper;

	@Inject
	@Config(value = "emil.imageproposerservice")
	private String imageProposerService;

	@Inject
	private EmilEnvironmentRepository emilEnvRepo;

	@Inject
	private EmilDataExport exportService;

	@Inject
	private ArchiveAdapter archive;

	private ObjectArchiveHelper objHelper;

	private AsyncIoTaskManager taskManager;
	private ImageProposer imageProposer;

	@Inject
	private ContainerUtil containerUtil;

	@Inject
	@AuthenticatedUser
	private UserContext authenticatedUser = null;

	@PostConstruct
	private void initialize() {
		try {
			imageProposer = new ImageProposer(imageProposerService + "/imageproposer");
		} catch (IllegalArgumentException e) {}

		objHelper = new ObjectArchiveHelper(objectArchive);
		try {
			taskManager = new AsyncIoTaskManager();
		} catch (NamingException e) {
			throw new IllegalStateException("failed to create AsyncIoTaskManager");
		}
	}

	/**
	 * Creates EmilEnvironments for all environments found at the image archive.
	 *
	 * @return
	 */
	@Secured
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


	@Secured
	@GET
	@Path("/objectDependencies")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getObjectDependencies(@QueryParam("envId") String envId)
	{
		if(envId == null || envId.trim().isEmpty()){
			return new ArrayList<>();
		}

		try {
			return archive.getEnvironmentDependencies(envId);
		} catch (IOException|JAXBException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return new ArrayList<>();
		}
	}
	

	@Secured
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

		try {
			List<String> objectDependencies = archive.getEnvironmentDependencies(desc.getEnvId());
            System.out.println("size " + objectDependencies.size() + " " + desc.isForce());
			if(objectDependencies != null && objectDependencies.size() > 0 && !desc.isForce())
			{
				final String json = createJsonResponse("2", objectDependencies.toString());
				return createResponse(Status.OK, json);
			}
		} catch (IOException|JAXBException e) {

			LOG.log(Level.SEVERE, e.getMessage(), e);
//			return Emil.internalErrorResponse(e);
		}

		try {
			emilEnvRepo.delete(desc.getEnvId(), desc.getDeleteMetaData(), desc.getDeleteImage());
		} catch (BWFLAException e1) {
			return Emil.internalErrorResponse(e1);
		}

		imageProposer.refreshIndex();
		return Emil.successMessageResponse("delete success!");
	}

	/**
	 * Looks up and returns all possible environments. A JSON response will be
	 * returned, containing:
	 * <p>
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "environments": [
	 *          { "id": &ltEnvironment's ID&gt, "label": "Environment's label" },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 * <p>
	 * In case of an error a JSON response containing the corresponding message
	 * will be returned:
	 * <p>
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 *
	 * @return A JSON object with supported environments when found, else an
	 * error message.
	 */
	@Secured
	@GET
	@Path("/getAllEnvironments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllEnvironments() {
		try {
			List<EmilEnvironment> environments = emilEnvRepo.getEmilEnvironments();
			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.name("environments");
			json.beginArray();

			for (EmilEnvironment env : environments) {
				json.beginObject();
				json.add("id", env.getEnvId());
				json.add("label", env.getTitle());
				json.endObject();
			}

			json.endArray();
			json.endObject();
			json.finish();

			return Emil.createResponse(Status.OK, json.toString());
		} catch (Throwable t) {
			return Emil.errorMessageResponse(t.getMessage());
		}
	}


	/**
	 * returns Environment with given ID
	 * @param id
	 * @return
	 */
	@Secured
	@GET
	@Path("/getEnvById")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEnvById(@QueryParam("id") String id) {
		try {
			EmilEnvironment env = emilEnvRepo.getEmilEnvironmentById(id);
			return Emil.createResponse(Status.OK, env.jsonValueWithoutRoot(false));
		} catch (Throwable t) {
			return Emil.errorMessageResponse(t.getMessage());
		}
	}

	@Secured
	@GET
	@Path("/exportEnvsToPath")
	@Produces(MediaType.APPLICATION_JSON)
	public Response export() throws IOException {
		List<EmilEnvironment> environments = null;

		environments = emilEnvRepo.getEmilEnvironments();
		if (environments != null && environments.size() == 0) {
			emilEnvRepo.init();
			environments = emilEnvRepo.getEmilEnvironments();
		}

		environments.forEach(env -> {
			try {
				LOG.info(env.value(true));
				exportService.saveEnvToPath(env);
			} catch (JAXBException | IOException e) {
				e.printStackTrace();
			}
		});
		return Emil.successMessageResponse("success!");
	}

	@Secured
	@GET
	@Path("/remoteList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response remoteList(@QueryParam("host") String host, @QueryParam("type") String type) {
		String hostUrl;
		try {
			hostUrl = URLDecoder.decode(host, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return Emil.errorMessageResponse(e.getMessage());
		}

		List<Environment> envs;
		EnvironmentsAdapter remoteArchive = new EnvironmentsAdapter(hostUrl);
		try {
			envs = remoteArchive.getEnvironments(type);
			LOG.info(remoteArchive.toString());
		} catch (BWFLAException | JAXBException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return Emil.errorMessageResponse(e.getMessage());
		}

		if (envs == null)
			return Emil.errorMessageResponse("no envs");


		try {
			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.name("environments");
			json.beginArray();

			for (Environment m : envs) {
				json.beginObject();
				json.add("envId", m.getId());
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

	@Secured
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

	/**
	 * Looks up and returns a list of all Emil's environments as a JSON object:
	 * <p>
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "environments": [
	 *          {
	 *              "envId": &ltEnvironment's ID&gt,
	 *              "os": "Environment's OS name",
	 *              "title": "Environment's title",
	 *              "description": "Environment's description",
	 *              "version": "Environment's version",
	 *              "emulator": "Environment's emulator"
	 *          },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 *
	 * @return A JSON object containing the environment descriptions.
	 */
	@Secured
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public Response list() {
		List<EmilEnvironment> environments = null;


		try{
			environments = emilEnvRepo.getEmilEnvironments();
			if (environments != null && environments.size() == 0) {
				init();
				environments = emilEnvRepo.getEmilEnvironments();
			}
		} catch (Exception exception) {
			return Emil.internalErrorResponse(exception);
		}

		try {
			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.name("environments");
			json.beginArray();

			// Add all environments to the response...
			for (EmilEnvironment emilenv : environments) {
				MachineConfiguration machineConf = null;
				try {
					Environment env =  envHelper.getEnvironmentById(emilenv.getArchive(), emilenv.getEnvId());

					if(!(env instanceof MachineConfiguration))
						continue;

					machineConf = (MachineConfiguration)env;
				} catch (BWFLAException e) {
					LOG.warning("skipping env: " + emilenv.getArchive() + " " + emilenv.getEnvId() + " " + e.getMessage());
					continue;
				}

				String envType = "base";
				json.beginObject();

				json.add("parentEnvId", emilenv.getParentEnvId());
				json.add("envId", emilenv.getEnvId());
				json.add("title", emilenv.getTitle());
				json.add("description", emilenv.getDescription());
				json.add("version", emilenv.getVersion());
				json.add("emulator", emilenv.getEmulator());
				json.add("helpText", emilenv.getHelpText());
				json.add("enableRelativeMouse", emilenv.isEnableRelativeMouse());
				json.add("enablePrinting", emilenv.isEnablePrinting());
				json.add("shutdownByOs", emilenv.isShutdownByOs());
				json.add("timeContext", emilenv.getTimeContext());
				json.add("serverMode", emilenv.isServerMode());
				json.add("localServerMode", emilenv.isLocalServerMode());
				json.add("enableSocks", emilenv.isEnableSocks());
				json.add("serverPort", emilenv.getServerPort());
				json.add("serverIp", emilenv.getServerIp());
				json.add("gwPrivateIp", emilenv.getGwPrivateIp());
				json.add("gwPrivateMask", emilenv.getGwPrivateMask());
				json.add("enableInternet", emilenv.isEnableInternet());
				json.add("connectEnvs", emilenv.isConnectEnvs());
				json.add("author", emilenv.getAuthor());
				json.add("canProcessAdditionalFiles", emilenv.isCanProcessAdditionalFiles());
				json.add("archive", emilenv.getArchive());
				json.add("xpraEncoding", emilenv.getXpraEncoding());

				if( emilenv.getOwner() != null)
					json.add("owner" , emilenv.getOwner().getUsername());
				else
					json.add("owner", "shared");

				if(emilenv instanceof EmilObjectEnvironment)
				{
					EmilObjectEnvironment emilObjEnv = (EmilObjectEnvironment) emilenv;
					json.add("objectId", emilObjEnv.getObjectId());
					json.add("objectArchive", emilObjEnv.getObjectArchiveId());
					envType = "object";
				}

				if(emilenv instanceof EmilContainerEnvironment)
				{
					envType = "container";
					EmilContainerEnvironment cEnv = (EmilContainerEnvironment) emilenv;
					json.add("input", cEnv.getInput());
					json.add("output", cEnv.getOutput());

					json.name("processArgs");
					json.beginArray();
					if(cEnv.getArgs() != null)
					for(String _arg : cEnv.getArgs())
						json.value(_arg);
					json.endArray();

					json.name("processEnvs");
					json.beginArray();
					if(cEnv.getEnv() != null)
						for(String _env : cEnv.getEnv())
						json.value(_env);
					json.endArray();
				}


				List<EmilEnvironment> parents = emilEnvRepo.getParents(emilenv.getEnvId());
				if (parents.size() > 0) {
					json.name("revisions");
					json.beginArray();
					for(EmilEnvironment parentEnv : parents)
					{
						json.beginObject();
						json.add("id", parentEnv.getEnvId());
						json.add("text", parentEnv.getDescription());
						json.add("archive", parentEnv.getArchive());
						json.endObject();
					}
					json.endArray();
				}

				// Add installed software IDs to the response
				if (machineConf != null) {
					json.name("installedSoftwareIds");
					json.beginArray();
					for (String softwareId : machineConf.getInstalledSoftwareIds())
						json.value(softwareId);

					json.endArray();

					json.add("userTag", machineConf.getUserTag());
					json.add("os", machineConf.getOperatingSystemId());

					if(machineConf.getNativeConfig() != null)
						json.add("nativeConfig", machineConf.getNativeConfig().getValue());

					if(machineConf.getUiOptions() != null && machineConf.getUiOptions().getForwarding_system() != null)
					{
						if (machineConf.getUiOptions().getForwarding_system().equalsIgnoreCase("xpra"))
							json.add("useXpra", true);
                    }
					else
						json.add("useXpra", false);

					if (machineConf.getEmulator().getBean() != null) {
						json.add("emulator", machineConf.getEmulator().getBean());
					}

					if (machineConf.getEmulator().getContainerName() != null) {
						json.add("containerName", machineConf.getEmulator().getContainerName());
					}

					if (machineConf.getEmulator().getContainerVersion() != null) {
						json.add("containerVersion", machineConf.getEmulator().getContainerVersion());
					}
				}
				json.add("envType", envType);
				json.endObject();
			}
			json.endArray(); // environments array
			json.endObject();
			json.finish();

			return Emil.createResponse(Status.OK, json.toString());
		} catch (Throwable t) {
			return Emil.internalErrorResponse(t);
		}
	}

	@Secured
	@POST
	@Path("/createEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 */
	public Response createEnvironment(EnvironmentCreateRequest envReq) {
		final DatabaseEnvironmentsAdapter environmentHelper = envHelper;

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

	@Secured
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
		final DatabaseEnvironmentsAdapter environmentHelper = envHelper;
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
	@Secured
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
			Environment environment = envHelper.getEnvironmentById(currentEnv.getArchive(), desc.getEnvId());

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

				if (machineConfiguration.getUiOptions().getHtml5() == null)
					machineConfiguration.getUiOptions().setHtml5(new Html5Options());

//				Do not check for null. EmuBean would choose latest, if value is null
				machineConfiguration.getEmulator().setContainerName(desc.getContainerEmulatorName());
				machineConfiguration.getEmulator().setContainerVersion(desc.getContainerEmulatorVersion());

				machineConfiguration.getUiOptions().getHtml5().setPointerLock(desc.isEnableRelativeMouse());

				if(desc.isEnableInternet())
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
				String id = envHelper.importMetadata("default", environment, md, false);
				newEnv.setEnvId(id);
				newEnv.setParentEnvId(currentEnv.getEnvId());
				currentEnv.addChildEnvId(newEnv.getEnvId());
				imported = true;
			}
			else {
				envHelper.updateMetadata(currentEnv.getArchive(), environment);
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
		newEnv.setHelpText(desc.getHelpText());
		newEnv.setEnablePrinting(desc.isEnablePrinting());
		newEnv.setEnableRelativeMouse(desc.isEnableRelativeMouse());
		newEnv.setShutdownByOs(desc.isShutdownByOs());
		newEnv.setEnableInternet(desc.isEnableInternet());
		newEnv.setConnectEnvs(desc.canConnectEnvs());
		newEnv.setServerMode(desc.isServerMode());
		newEnv.setLocalServerMode(desc.isLocalServerMode());
		newEnv.setEnableSocks(desc.isEnableSocks());
		newEnv.setGwPrivateIp(desc.getGwPrivateIp());
		newEnv.setGwPrivateMask(desc.getGwPrivateMask());
		newEnv.setServerIp(desc.getServerIp());
		newEnv.setServerPort(desc.getServerPort());
		newEnv.setCanProcessAdditionalFiles(desc.canProcessAdditionalFiles());
		newEnv.setXpraEncoding(desc.getXpraEncoding());

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

		final String json = "{\"status\":\"0\"}";
		return Emil.createResponse(Status.OK, json);
	}

	@Secured
	@GET
	@Path("/defaultEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	public DefaultEnvironmentResponse defaultEnvironment(@QueryParam("osId") String osId) {

		try {
			String env = envHelper.getDefaultEnvironment(osId);
			DefaultEnvironmentResponse response = new DefaultEnvironmentResponse();
			response.setEnvId(env);
			return response;
		} catch (BWFLAException e) {
			return new DefaultEnvironmentResponse(e);
		}
	}

	@Secured
	@GET
	@Path("/setDefaultEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	public EmilResponseType setDefaultEnvironment(@QueryParam("osId") String osId, @QueryParam("envId") String envId) {
		try {
			envHelper.setDefaultEnvironment(osId, envId);
			return new EmilResponseType();
		} catch (BWFLAException e) {
			return new EmilResponseType(e);
		}
	}

	@Secured
	@GET
	@Path("/environment")
	@Produces(MediaType.APPLICATION_JSON)
	public Response environment(@QueryParam("envId") String envId) {
		EmilEnvironment env = emilEnvRepo.getEmilEnvironmentById(envId);

		if(env == null) {
			return Emil.errorMessageResponse("Environment ID not found or null.");
		}
		return Emil.createResponse(Status.OK, env);
	}

	@Secured
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
			Environment environment = envHelper.getEnvironmentById(emilEnv.getArchive(), req.getId());
			ImageArchiveMetadata md = new ImageArchiveMetadata();
			md.setType(ImageType.USER);
			String id = envHelper.importMetadata("default", environment, md, false);
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

	@Secured
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

	@Secured
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

	@Secured
	@GET
	@Path("/sync")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sync() {
		envHelper.sync();
		init();
		return Emil.successMessageResponse("syncing archives ");
	}

	@Secured
	@GET
	@Path("/environmentMetaData")
	@Produces(MediaType.APPLICATION_JSON)
	public EnvironmentMetaData getEnvironmentMetaData(@QueryParam("archive") String archive, @QueryParam("envId") String envId)
	{
		if(envId == null || archive == null)
			return new EnvironmentMetaData(new BWFLAException("environment ID / archive was null"));

		final DatabaseEnvironmentsAdapter environmentHelper = envHelper;
		try {
			MachineConfiguration machineConfiguration = (MachineConfiguration)envHelper.getEnvironmentById(archive, envId);

			if(machineConfiguration.getEmulator() == null)
				return new EnvironmentMetaData(new BWFLAException("invalid machine metadata: " + machineConfiguration.toString()));
			final String bean = machineConfiguration.getEmulator().getBean();

			EnvironmentMetaData emd = new EnvironmentMetaData();
			emd.setMediaChangeSupport(EmulationEnvironmentHelper.beanSupportsMediaChange(bean, null));

			return emd;

		} catch (BWFLAException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return new EnvironmentMetaData(e);
		}

	}

	@Secured
	@POST
	@Path("/overrideObjectCharacterization")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response overrideObjectCharacterization(OverrideCharacterizationRequest request) {
		String objectId = request.getObjectId();
		String objectArchive = request.getObjectArchive();
		List<EnvironmentInfo> environments = request.getEnvironments();
		try {
			archive.setCachedEnvironmentsForObject(objectId, environments, request.getDescription());
			return Emil.successMessageResponse("");
		} catch (Exception e) {
			return Emil.errorMessageResponse(e.getMessage());
		}
	}

	@Secured
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
		request.environmentHelper = envHelper;
		request.imageProposer = imageProposer;

		try {
			request.validate();
		} catch (BWFLAException e) {
			e.printStackTrace();
			return new TaskStateResponse(e);
		}

		return new TaskStateResponse(taskManager.submitTask(new ImportImageTask(request, LOG)));
	}

	@Secured
	@GET
	@Path("/getNameIndexes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 *
	 * @return
	 */
    public ImageNameIndex getNameIndexes() throws BWFLAException, JAXBException {
        return envHelper.getNameIndexes();
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

	@Secured
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
				Environment e = envHelper.getEnvironmentById(emilEnvironment.getArchive(), envId);
				if(!(e instanceof MachineConfiguration))
					continue;
				env = (MachineConfiguration)e;
				importRequest.env = env;

				importRequest.repository = emilEnvRepo;
				importRequest.emilEnvironment = emilEnvironment;
			} catch (BWFLAException e) {
				e.printStackTrace();
			}

			importRequest.environmentHelper = envHelper;
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

	@Secured
	@GET
	@Path("/taskState")
	@Produces(MediaType.APPLICATION_JSON)
	public TaskStateResponse taskState(@QueryParam("taskId") String taskId) {
		final TaskInfo<Object> info = taskManager.getTaskInfo(taskId);
		if (info == null)
			return new TaskStateResponse(new BWFLAException("task failed"));

		if (!info.result().isDone())
			return new TaskStateResponse(taskId);

		try {
			Object o = info.result().get();
			TaskStateResponse response = new TaskStateResponse(taskId, true);

			if(o != null) {

				if(o instanceof BWFLAException)
					return new TaskStateResponse((BWFLAException)o);

				if(o instanceof Map)
					response.setUserData((Map<String,String>)o);
			}

			return response;

		} catch (InterruptedException|ExecutionException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return new TaskStateResponse(new BWFLAException(e));
		}

		finally {
			taskManager.removeTaskInfo(taskId);
		}
	}

	private String createEnvIdJson(String envId) throws IOException {
		JsonBuilder json = new JsonBuilder();
		json.beginObject();
		json.add("envId", envId);
		json.endObject();
		json.finish();
		return json.toString();
	}

	class AsyncIoTaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<Object> {
		public AsyncIoTaskManager() throws NamingException {
			super(InitialContext.doLookup("java:jboss/ee/concurrency/executor/io"));
		}
	}

//	class ExportImageTask extends AbstractTask<Object>
//	{
//		private final String host;
//		private final List<String> envIds;
//		private boolean embedded;
//		private final String remoteObjectArchiveHost;
//
//		ExportImageTask(String host, String remoteObjectArchiveHost, List<String> envIds, boolean embedded)
//		{
//			this.host = host;
//			this.envIds = envIds;
//			this.embedded = embedded;
//			this.remoteObjectArchiveHost = remoteObjectArchiveHost;
//		}
//
//		private void exportObjectAsImage(MachineConfiguration conf, EnvironmentsAdapter remoteAdapter)
//				throws BWFLAException {
//			List<AbstractDataResource> resources = conf.getAbstractDataResource();
//			for(AbstractDataResource r : resources)
//			{
//				if(!(r instanceof ObjectArchiveBinding))
//					continue;
//
//				ObjectArchiveBinding oab = (ObjectArchiveBinding)r;
//
//				String host = oab.getArchiveHost();
//				String objectId = oab.getObjectId();
//				String archive = oab.getArchive();
//
//				ObjectArchiveHelper helper = new ObjectArchiveHelper(host);
//				ObjectArchiveHelper remoteHelper = new ObjectArchiveHelper(remoteObjectArchiveHost);
//				ObjectFileCollection object = helper.getObjectHandle(archive, objectId);
////			if(object.getFiles() != null) {
////				for (ObjectFileCollectionHandle entry : object.getFiles()) {
////					LOG.info("entry: " + entry.getFilename());
////				}
////			}
//				remoteHelper.importObject(archive, object);
//				oab.setArchiveHost(remoteObjectArchiveHost);
//			}
//		}
//
//		private void exportObjectEmbedded(MachineConfiguration conf, EnvironmentsAdapter remoteAdapter) throws BWFLAException {
//			LOG.info("export image embedded");
//			List<ImageArchiveBinding> importedObjects = new ArrayList<>();
//			for(Iterator<AbstractDataResource> iter = conf.getAbstractDataResource().iterator(); iter.hasNext();)
//			{
//				AbstractDataResource r = iter.next();
//				if(!(r instanceof ObjectArchiveBinding))
//					continue;
//
//				ObjectArchiveBinding oab = (ObjectArchiveBinding)r;
//				String host = oab.getArchiveHost();
//				String objectId = oab.getObjectId();
//				String archive = oab.getArchive();
//
//				ObjectArchiveHelper helper = new ObjectArchiveHelper(host);
//				ObjectFileCollection object = helper.getObjectHandle(archive, objectId);
//
//				if(object.getFiles() == null || object.getFiles().size() == 0)
//					continue;
//
//				if(object.getFiles().size() > 1) {
//					LOG.warning("objects with multiple files are not supported");
//					continue;

//				}
//
//				ImageArchiveMetadata iaMD = new ImageArchiveMetadata();
//				iaMD.setType(ImageType.OBJECT);
//				ObjectFileCollectionHandle objHandle = object.getFiles().get(0);
//				EnvironmentsAdapter.ImportImageHandle imageHandle= remoteAdapter.importImage(objHandle.getHandle(), iaMD);
//
//				iter.remove();
//				ImageArchiveBinding imageArchiveBinding = imageHandle.getBinding(60*60*24);
//				imageArchiveBinding.setId(objectId);
//				importedObjects.add(imageArchiveBinding);
//			}
//
//			for(ImageArchiveBinding b : importedObjects)
//			{
//				String bindingUrl = "binding://" + b.getId();
//				for(Drive d : conf.getDrive())
//				{
//					if(!d.getData().startsWith(bindingUrl))
//						continue;
//					d.setData(bindingUrl);
//				}
//				conf.getAbstractDataResource().add(b);
//			}
//		}
//
//		@Override
//		protected Object execute() throws Exception {
//			EnvironmentsAdapter remoteAdapter = new EnvironmentsAdapter(host);
//			try {
//				for (String envId : envIds) {
//					LOG.info("syncing " + envId);
//					Environment env = envHelper.getEnvironmentById(envId);
//
//					ImageExport dependencies = envHelper.getImageDependecies(envId);
//					List<ImageFileInfo> infos = dependencies.getImageFiles();
//					for (ImageFileInfo info : infos) {
//						LOG.info("ExportImageTask: upload dependency " + info.getId());
//						ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
//						iaMd.setType(info.getType());
//						iaMd.setImageId(info.getId());
//						iaMd.setDeleteIfExists(true);
//						EnvironmentsAdapter.ImportImageHandle handle = remoteAdapter.importImage(info.getFileHandle(), iaMd);
//						if(handle.getBinding(60*60*24*7)== null)
//						 	return new BWFLAException("import failed: timeout");
//					}
//
//					MachineConfiguration configuration = (MachineConfiguration)env;
//					if(!embedded)
//						exportObjectAsImage(configuration, remoteAdapter);
//					else
//						exportObjectEmbedded(configuration, remoteAdapter);
//
//					try {
//						ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
//						iaMd.setType(ImageType.OBJECT);
//						remoteAdapter.importMetadata(env.value(), iaMd, true);
//					} catch (JAXBException e) {
//						LOG.log(Level.WARNING, e.getMessage(), e);
//						return new BWFLAException("metadata import failed");
//					}
//				}
//			} catch (BWFLAException e) {
//				e.printStackTrace();
//				return e;
//			}
//
//			return null;
//		}
//	}

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

	//	@GET
//	@Path("/export")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response export(@QueryParam("envId") String envId) {
//
//		if (exportPath == null || exportPath.isEmpty())
//			return Emil.errorMessageResponse("Emil export is not configured ");
//
//		File objectDir = createFolder("objects");
//		File imageDir = createFolder("images");
//		File metadataDir = createFolder("metadata");
//		File uiDir = createFolder("ui");
//
//		final DatabaseEnvironmentsAdapter environmentHelper = envHelper;
//		BufferedWriter writer = null;
//		try {
//			Environment localChosenEnv = environmentHelper.getEnvironmentById(envId);
//
//			// this env has abstract (relative) references to an yet unknown image archive
//			Environment abstractEnv = environmentHelper.getEnvironmentById(envId);
//			if (abstractEnv == null || localChosenEnv == null)
//				return Emil.errorMessageResponse("could not find environment: " + envId);
//
//			EmilUtils.exportEnvironmentMedia((MachineConfiguration) abstractEnv,
//					(MachineConfiguration) localChosenEnv, imageDir, objectDir);
//
//			// fix archive binding for USB
//			for (AbstractDataResource ab : ((MachineConfiguration) abstractEnv).getAbstractDataResource()) {
//				if (ab instanceof ObjectArchiveBinding) {
//					ObjectArchiveBinding binding = (ObjectArchiveBinding) ab;
//					binding.setArchive("objects");
//					binding.setArchiveHost("localhost:8080");
//				}
//			}
//
//			writer = new BufferedWriter(new FileWriter(metadataDir.getAbsolutePath() + "/" + envId + ".xml"));
//			writer.write(abstractEnv.toString());
//
//			File uiSrcDir = new File("/eaas/ui");
//			if(uiSrcDir.exists()) {
//				FileUtils.copyDirectory(uiSrcDir, uiDir);
//				Files.write(Paths.get(uiDir.toPath() + "/envId"), createEnvIdJson(envId).getBytes());
//			}
//
//			return Emil.successMessageResponse("export completed");
//		} catch (Throwable t) {
//			return Emil.internalErrorResponse(t);
//		} finally {
//			try {
//				if (writer != null)
//					writer.close();
//			} catch (IOException e) {
//				LOG.log(Level.SEVERE, e.getMessage(), e);
//			}
//		}
//	}
//
//
//	private File createFolder(String folderName) {
//		String objectPath = exportPath + "/" + folderName;
//		File objectDir = new File(objectPath);
//		if (!objectDir.isDirectory()) {
//			objectDir.mkdir();
//		}
//		return objectDir;
//	}

}
