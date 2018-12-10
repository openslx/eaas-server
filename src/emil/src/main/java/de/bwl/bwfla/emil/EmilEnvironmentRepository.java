package de.bwl.bwfla.emil;


import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import de.bwl.bwfla.common.datatypes.EnvironmentDescription;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.database.MongodbEaasConnector;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.snapshot.*;
import de.bwl.bwfla.emil.utils.Snapshot;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.util.*;


@ApplicationScoped
public class EmilEnvironmentRepository {

	@Inject
	private MongodbEaasConnector dbConnector;

	private Path emilEnvPath;
	private Path objEnvPath;
	private Path sessionEnvPath;

	protected final static Logger LOG = Logger.getLogger(EmilEnvironmentRepository.class.getName());

	@Inject
	@Config(value = "emil.emildbcollectionname")
	private String emilDbCollectionName;

	@Inject
	@Config(value = "commonconf.serverdatadir")
	protected String serverdatadir;

	@Inject
	@Config(value = "emil.emilobjectenvironmentspaths")
	protected String emilObjectEnvironmentsPath;

	@Inject
	@Config(value = "emil.emilenvironmentspath")
	protected String emilEnvironmentsPath;

	@Inject
	@Config(value = "ws.objectarchive")
	protected String objectArchive;

	@Inject
	@Config(value = "ws.imagearchive")
	protected String imageArchive;

	@Inject
	@Config(value = "emil.usersessionretention")
	protected long retention;


	private EnvironmentsAdapter environmentsAdapter;
	private ObjectArchiveHelper objectArchiveHelper;


	private static final Gson GSON = new GsonBuilder().create();

	private UserSessions sessions;
	private ConcurrentHashMap<String, EmilEnvironment> emilEnvironments;


	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	private <T extends EmilEnvironment> List<T> loadEmilEnvironments(final Class<T> klass) throws JAXBException {
		// fixme
		// need to remove this hack. see more in EmilEnvironment.getDatabaseKey()
		char c[] = klass.getSimpleName().toCharArray();
		c[0] = Character.toLowerCase(c[0]);

		if (klass.equals(EmilEnvironment.class) || klass.equals(EmilObjectEnvironment.class) || klass.equals(EmilContainerEnvironment.class)) {
			return dbConnector.getJaxbObjects(emilDbCollectionName, new String(c), klass);
		}
		return dbConnector.getJaxbObjects(emilDbCollectionName, new String(c), klass);
	}


//	synchronized private void migrateDirs()
//	{
//		try {
//			if (emilEnvironmentsPath != null) {
//				Path oldEnvDir = Paths.get(emilEnvironmentsPath);
//				if (oldEnvDir.toFile().exists()) {
//					Path targetDir = Paths.get(serverdatadir).resolve("emil-environments");
//					if(!Files.exists(targetDir)) {
//						Files.createDirectory(targetDir);
//						FileUtils.copyDirectory(oldEnvDir.toFile(), targetDir.toFile());
//					}
//				}
//			}
//
//			if (emilObjectEnvironmentsPath != null) {
//				Path oldEnvDir = Paths.get(emilObjectEnvironmentsPath);
//				if (oldEnvDir.toFile().exists()) {
//					Path targetDir = Paths.get(serverdatadir).resolve("emil-object-environments");
//					if(!Files.exists(targetDir)) {
//						Files.createDirectory(targetDir);
//						FileUtils.copyDirectory(oldEnvDir.toFile(), targetDir.toFile());
//					}
//				}
//			}
//		}
//		catch (IOException e)
//		{
//			throw new IllegalStateException(e);
//		}
//	}


	private void load() throws JAXBException {
		emilEnvironments = new ConcurrentHashMap<>();

		List<EmilEnvironment> _envs = loadEmilEnvironments(EmilEnvironment.class);
		for (EmilEnvironment _e : _envs)
			emilEnvironments.put(_e.getEnvId(), _e);

		List<EmilObjectEnvironment> _oEnvs = loadEmilEnvironments(EmilObjectEnvironment.class);
		for (EmilObjectEnvironment _e : _oEnvs)
			emilEnvironments.put(_e.getEnvId(), _e);

		List<EmilContainerEnvironment> _cEnvs = loadEmilEnvironments(EmilContainerEnvironment.class);
		for (EmilContainerEnvironment _e : _cEnvs)
			emilEnvironments.put(_e.getEnvId(), _e);

		List<EmilSessionEnvironment> _sEnvs = loadEmilEnvironments(EmilSessionEnvironment.class);
		sessions = new UserSessions(_sEnvs);
		for (EmilSessionEnvironment _e : _sEnvs)
			emilEnvironments.put(_e.getEnvId(), _e);
	}

	@PostConstruct
	public void init() {
		if (serverdatadir != null) {
			Path cache = Paths.get(serverdatadir);
			if (!cache.toFile().exists()) // new style configuratioen
			{
				try {
					Files.createDirectory(cache);
				} catch (IOException e) {
					LOG.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		try {
			Path exportDir = Paths.get(serverdatadir).resolve("export");
			if (!Files.exists(exportDir))
				Files.createDirectory(exportDir);
			this.emilEnvPath = exportDir.resolve("emil-environments");
			if (!Files.exists(this.emilEnvPath))
				Files.createDirectory(this.emilEnvPath);
			this.objEnvPath = exportDir.resolve("emil-object-environments");
			if (!Files.exists(this.objEnvPath))
				Files.createDirectory(this.objEnvPath);
			this.sessionEnvPath = exportDir.resolve("emil-session-environments");
			if (!Files.exists(this.sessionEnvPath))
				Files.createDirectory(this.sessionEnvPath);
		} catch (IOException e) {
			LOG.warning("creation of emil dirs failed! \n" + e.getMessage());
		}
		environmentsAdapter = new EnvironmentsAdapter(imageArchive);
		objectArchiveHelper = new ObjectArchiveHelper(objectArchive);

		try {
			try {
				importExistentEnv();
			} catch (IOException e) {
				e.printStackTrace();
			}
			load();
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			throw new IllegalStateException();
		}

		LOG.info("Setting user retention time to: " + retention);
		scheduledExecutorService.scheduleAtFixedRate(new Monitor(retention), 1, 1, TimeUnit.MINUTES);
	}

	public EmilEnvironment getEmilEnvironmentById(String envid) {
		if (envid == null)
			return null;
		return emilEnvironments.get(envid);
	}

	private <T extends EmilEnvironment> List<T> importEnvByPath(final Class<T> klass, Path... paths) throws IOException {
		final List<T> environments = new ArrayList<>();
		for (Path path : paths) {
			if (!Files.exists(path)) {
				continue;
			}
			DirectoryStream<Path> files = Files.newDirectoryStream(path);

			for (java.nio.file.Path fpath : files) {
				if (Files.isDirectory(fpath))
					continue;

				if (fpath.toString().contains(".fuse_hidden"))
					continue;

				if (fpath.getFileName().startsWith("."))
					continue;

				try {
					T env = getEmilEnvironmentByPath(fpath, klass);
					if (env != null) {
						environments.add(env);
						dbConnector.saveDoc(emilDbCollectionName, env.getEnvId(), env.getDatabaseKey(), env.JSONvalue(false));
					}
				} catch (Exception e) {
					LOG.warning("import might be broken! \n " + e.getMessage());
				}
			}

			Path obsoleteEnvsDir = Paths.get(path.getParent() + "/." + path.getFileName());
			if (obsoleteEnvsDir.toFile().exists()) {
				obsoleteEnvsDir = Paths.get(obsoleteEnvsDir + UUID.randomUUID().toString());
			}
			Files.move(path, obsoleteEnvsDir);
		}
		return environments;

	}

	public synchronized void importExistentEnv() throws IOException {

		Path emilEnvPath = Paths.get(serverdatadir).resolve("emil-environments");
		Path objEnvPath = Paths.get(serverdatadir).resolve("emil-object-environments");
		Path sessionEnvPath = Paths.get(serverdatadir).resolve("emil-session-environments");

		// ensure the absence of null elements
		Optional.ofNullable(importEnvByPath(EmilEnvironment.class, Paths.get(emilEnvironmentsPath), emilEnvPath));
		Optional.ofNullable(importEnvByPath(EmilObjectEnvironment.class, Paths.get(emilObjectEnvironmentsPath), objEnvPath));
		Optional.ofNullable(importEnvByPath(EmilSessionEnvironment.class, Paths.get(emilEnvironmentsPath), sessionEnvPath));
	}

	private <T> T getEmilEnvironmentByPath(Path envpath, final Class<T> klass) throws IOException, JsonSyntaxException, JsonIOException {
		if (!Files.exists(envpath))
			throw new IOException("file not found");

		Reader reader = Files.newBufferedReader(envpath, StandardCharsets.UTF_8);
		return GSON.fromJson(reader, klass);
	}

	public List<EmilObjectEnvironment> getEmilObjectEnvironmentByObject(String objectId) {
		List<EmilObjectEnvironment> result = new ArrayList<>();
		for (EmilEnvironment env : emilEnvironments.values()) {
			if (!(env instanceof EmilObjectEnvironment) || env instanceof EmilSessionEnvironment)
				continue;

			EmilObjectEnvironment objEnv = (EmilObjectEnvironment) env;

			if (objEnv.getObjectId().equals(objectId) && objEnv.isVisible())
				result.add(objEnv);
		}
		return result;
	}

	public void save(EmilEnvironment env) throws IOException, JAXBException, BWFLAException {
		dbConnector.saveDoc(emilDbCollectionName, env.getEnvId(), env.getDatabaseIdKey(), env.JSONvalue(false));
		emilEnvironments.put(env.getEnvId(), env);
	}

	public <T extends JaxbType> void delete(String envId, boolean deleteMetadata, boolean deleteImages) throws IOException, BWFLAException, JAXBException {
		EmilEnvironment env = getEmilEnvironmentById(envId);
		if (!(env instanceof EmilSessionEnvironment)) {

			// If environment doesn't have children, we delete it. Otherwise we make it not visible
			if (env.getChildrenEnvIds().size() < 1) {
				LOG.info("deleting env " + env.getEnvId());
				dbConnector.deleteDoc(emilDbCollectionName, envId, env.getDatabaseIdKey());
				environmentsAdapter.delete(envId, deleteMetadata, deleteImages);

				// After we deleted environment, we need to remove childId from parent environment
				if (env.getParentEnvId() != null) {

					EmilEnvironment parentEnv = getEmilEnvironmentById(env.getParentEnvId());
					parentEnv.removeChildEnvId(envId);

					// update: we should not delete all the parents, to avoid deleting the base environment.
					// instead we only delete the top environment (for now). this requires a better UI solution

					// if (!parentEnv.isVisible() && parentEnv.getChildrenEnvIds().size() < 2) {
					parentEnv.setVisible(true);
						// Finally, if parent doesn't have children and not visible, we delete it (means we delete env including revisions)
						// delete(parentEnv.getEnvId());
					// }
					save(parentEnv);
				}
				else {
					LOG.info("no parent found");
				}
				emilEnvironments.remove(envId);

			} else {
				LOG.info("making env " + env.getEnvId() + " invisible");
				env.setVisible(false);
				save(env);
			}
		} else {
			EmilSessionEnvironment session = (EmilSessionEnvironment) env;
			sessions.remove(session);
			environmentsAdapter.delete(envId, true, true);
			dbConnector.deleteDoc(emilDbCollectionName, envId, env.getDatabaseIdKey());
			if (session.getParentEnvId() != null) {
				EmilEnvironment parentEnv = getEmilEnvironmentById(session.getParentEnvId());
				if (parentEnv instanceof EmilSessionEnvironment && !(parentEnv.getChildrenEnvIds().size() > 1)) {
					delete(session.getParentEnvId(), true, true);
				}
			}

			emilEnvironments.remove(envId);
		}
		dbConnector.deleteDoc(ClassificationData.collectionName, envId, ClassificationData.parentElement + ".environmentList.id", false);
	}

	public  <T extends JaxbType> ArrayList<T> getDatabaseContent(String type, Class<T> klass ) throws JAXBException {
		return dbConnector.getJaxbObjects(emilDbCollectionName, type, klass);
	}

	public List<EmilSessionEnvironment> getEmilSessionEnvironments() {
		return sessions.toList();
	}

	public int initialize() throws JAXBException, IOException, BWFLAException {
		int counter = 0;
		List<Environment> envs = environmentsAdapter.getEnvironments(null);
		for (Environment env : envs) {
			EmilEnvironment emilEnv = getEmilEnvironmentById(env.getId());
			if (emilEnv == null && env instanceof MachineConfiguration) {
				counter++;
				EmilEnvironment ee;
				String objectId = EmulationEnvironmentHelper.isObjectEnvironment((MachineConfiguration) env);
				if (objectId != null) {
					ee = new EmilObjectEnvironment();
					((EmilObjectEnvironment) ee).setObjectId(objectId);
				} else
					ee = new EmilEnvironment();
				// since you have to initialize object with correct instance, save function could not be moved out of if-else scope
				save(env, ee);
			} else if (emilEnv == null && env instanceof ContainerConfiguration) {
				EmilContainerEnvironment ee = new EmilContainerEnvironment();
				save(env, ee);
			}
		}
		return counter;
	}

	private void save(Environment env, EmilEnvironment ee) throws JAXBException, IOException, BWFLAException {
		ee.setEnvId(env.getId());
		ee.setTitle(env.getDescription().getTitle());
		ee.setAuthor("n.a.");
		if (env instanceof ContainerConfiguration) {
			((EmilContainerEnvironment) ee).setInput(((ContainerConfiguration) env).getInput());
			((EmilContainerEnvironment) ee).setOutput(((ContainerConfiguration) env).getOutputPath());
		}
		ee.setTitle(env.getDescription().getTitle());
		ee.setEmulator("n.a.");
		ee.setOs("n.a.");
		ee.setDescription("n.a.");
		save(ee);
	}

	public List<EmilEnvironment> getEmilEnvironments() throws IOException
	{
		final List<EmilEnvironment> environments = new ArrayList<>();

		for(EmilEnvironment _e : emilEnvironments.values()) {
			if (!(_e instanceof EmilSessionEnvironment) && !(_e instanceof EmilObjectEnvironment) && !(_e instanceof EmilContainerEnvironment))
				environments.add(_e);
		}

		Collections.sort(environments);
		return environments;
	}

	public List<EmilEnvironment> getAllEnvironments() throws IOException {
		return new ArrayList<>(emilEnvironments.values());
	}

	public void saveEnvToPath(EmilEnvironment env) throws IOException {
		String json;
		Path envpath;
		if (env instanceof EmilSessionEnvironment) {
			envpath = sessionEnvPath.resolve(env.getEnvId());
			json = GSON.toJson((EmilSessionEnvironment) env);
		} else if (env instanceof EmilObjectEnvironment) {
			envpath = objEnvPath.resolve(env.getEnvId());
			json = GSON.toJson((EmilObjectEnvironment) env);
		} else {
			envpath = emilEnvPath.resolve(env.getEnvId());
			json = GSON.toJson(env);
		}
		Files.write(envpath, json.getBytes());
	}


	public List<EmilObjectEnvironment> getEmilObjectEnvironments() {
		final List<EmilObjectEnvironment> environments = new ArrayList<>();

		for (EmilEnvironment _e : emilEnvironments.values())
			if (!(_e instanceof EmilSessionEnvironment) && (_e instanceof EmilObjectEnvironment))
				environments.add((EmilObjectEnvironment) _e);

		Collections.sort(environments);
		return environments;
	}

	public List<EmilContainerEnvironment> getEmilContainerEnvironments() {
		final List<EmilContainerEnvironment> environments = new ArrayList<>();

		for (EmilEnvironment _e : emilEnvironments.values())
			if (_e instanceof EmilContainerEnvironment)
				environments.add((EmilContainerEnvironment) _e);

		Collections.sort(environments);
		return environments;
	}

	public List<EmilEnvironment> getChildren(String envId, List<EmilEnvironment> envs) throws BWFLAException {
		List<EmilEnvironment> result = new ArrayList<>();
		for (EmilEnvironment e : envs) {
			if (e.getParentEnvId() != null && e.getParentEnvId().equals(envId)) {
				LOG.info("getChildren: found child " + e.getEnvId() + " for envId " + envId);
				result.addAll(getChildren(e.getEnvId(), envs));
			}
		}
		if (result.size() == 0) {
			LOG.info("no child found for " + envId);
			EmilEnvironment emilEnvironment = getEmilEnvironmentById(envId);
			if (emilEnvironment != null)
				result.add(emilEnvironment);
		}
		return result;
	}


	public String saveAsUserSession(Snapshot snapshot, SaveUserSessionRequest request) throws BWFLAException {
		String sessionEnvId = snapshot.saveUserSession(environmentsAdapter, objectArchiveHelper, request);

		EmilEnvironment parentEnv = getEmilEnvironmentById(request.getEnvId());
		if (parentEnv == null)
			throw new BWFLAException("parent environment not found: " + request.getEnvId());

		EmilSessionEnvironment sessionEnv = new EmilSessionEnvironment(parentEnv);
		sessionEnv.setObjectId(request.getObjectId());
		sessionEnv.setCreationDate((new Date()).getTime());
		sessionEnv.setUserId(request.getUserId());
		sessionEnv.setVisible(true);
		sessionEnv.setParentEnvId(parentEnv.getEnvId());

		LOG.info("adding session for user: " + request.getUserId()
				+ " object: " + request.getObjectId() + " env " + sessionEnvId);

		sessionEnv.setEnvId(sessionEnvId);

		try {
			save(sessionEnv);
			EmilSessionEnvironment oldEnv = sessions.get(sessionEnv.getUserId(), sessionEnv.getObjectId());
			LOG.info("saving: " + sessionEnvId);
			if (oldEnv != null)
				LOG.info("found oldEnv: " + oldEnv.getEnvId());
			LOG.info("parent env was: " + parentEnv.getParentEnvId());

			if (oldEnv != null && parentEnv.getParentEnvId() != null && !oldEnv.getEnvId().equals(parentEnv.getEnvId())) {

				LOG.info("would like to delete " + oldEnv.getEnvId());
				// delete(oldEnv.getEnvId());
			}
			sessions.add(sessionEnv);


			parentEnv.addChildEnvId(sessionEnvId);
			save(parentEnv);
			if (parentEnv instanceof EmilSessionEnvironment) {
				parentEnv.setVisible(false);
			}
		} catch (JAXBException | IOException e) {
			throw new BWFLAException("saveNewEnvironment: " + e.getMessage(), e);
		}
		return sessionEnv.getEnvId();
	}

	String saveAsObjectEnvironment(Snapshot snapshot, SaveObjectEnvironmentRequest request) throws BWFLAException {
		EmilObjectEnvironment ee = snapshot.createObjectEnvironment(environmentsAdapter, objectArchiveHelper, request);
		try {
			save(ee);
		} catch (JAXBException | IOException e) {
			throw new BWFLAException("saveObjectEnvironment: " + e.getMessage(), e);
		}
		return ee.getEnvId();
	}

	String saveImport(Snapshot snapshot, SaveImportRequest request) throws BWFLAException {
		Environment environment = environmentsAdapter.getEnvironmentById(request.getEnvId());
		EnvironmentDescription description = new EnvironmentDescription();
		description.setTitle(request.getTitle());
		environment.setDescription(description);

		environmentsAdapter.updateMetadata(environment.toString());
		environmentsAdapter.commitTempEnvironment(request.getEnvId());

		EmilEnvironment newEmilEnv = getEmilEnvironmentById(request.getEnvId());

		if (newEmilEnv != null)
			throw new BWFLAException("import failed: environment with id: " + request.getEnvId() + " exists.");

		newEmilEnv = new EmilEnvironment();
		newEmilEnv.setTitle(request.getTitle());
		newEmilEnv.setEnvId(request.getEnvId());
		newEmilEnv.setAuthor(request.getAuthor());


		newEmilEnv.setDescription(request.getMessage());
		try {
			save(newEmilEnv);
		} catch (JAXBException | IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			throw new BWFLAException(e);
		}
		return request.getEnvId();
	}


	void saveImportedContainer(String id, String title, String description, String author) throws BWFLAException {
		environmentsAdapter.commitTempEnvironmentWithCustomType(id, "containers");

		EmilEnvironment newEmilEnv = getEmilEnvironmentById(id);
		if (newEmilEnv != null)
			throw new BWFLAException("import failed: environment with id: " + id + " exists.");


		OciContainerConfiguration containerConfiguration = (OciContainerConfiguration) environmentsAdapter.getEnvironmentById(id);


		EmilContainerEnvironment env = new EmilContainerEnvironment();
		env.setEnvId(id);
		env.setTitle(title);
		env.setDescription(description);
		env.setVisible(true);
		env.setInput(containerConfiguration.getInput());
		env.setOutput(containerConfiguration.getOutputPath());
		env.setArgs(containerConfiguration.getProcess().getArguments());
		if (containerConfiguration.getProcess().getEnvironmentVariables() != null)
			env.setEnv(containerConfiguration.getProcess().getEnvironmentVariables());

		env.setAuthor(author);


		try {
			save(env);
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
		}
	}

	String saveAsRevision(Snapshot snapshot, SaveDerivateRequest req) throws BWFLAException {

		EmilEnvironment env = getEmilEnvironmentById(req.getEnvId());
		if (env == null) {
			if (req instanceof SaveCreatedEnvironmentRequest) {
				// no emil env -> new environment has been created and committed
				Environment _env = null;
				_env = environmentsAdapter.getEnvironmentById(req.getEnvId());

				env = new EmilEnvironment();
				env.setTitle(_env.getDescription().getTitle());
				env.setEnvId(req.getEnvId());
				env.setDescription("empty hard disk");
			} else
				throw new BWFLAException("Environment with id " + " not found");
		}

		if (!(req instanceof SaveNewEnvironmentRequest)) {
			env.setVisible(false);
			try {
				save(env);
			} catch (JAXBException | IOException e) {
				throw new BWFLAException("saveAsRevision: " + e.getMessage(), e);
			}
		}

		EmilEnvironment newEnv = snapshot.createEnvironment(environmentsAdapter, req, env);
		if (req instanceof SaveCreatedEnvironmentRequest)
			newEnv.setTitle(((SaveCreatedEnvironmentRequest) req).getTitle());
		try {
			env.addChildEnvId(newEnv.getEnvId());
			save(env);
			save(newEnv);
			if (newEnv instanceof EmilSessionEnvironment) {
				EmilSessionEnvironment session = (EmilSessionEnvironment) newEnv;
				EmilSessionEnvironment oldEnv = sessions.get(session.getUserId(), session.getObjectId());
				LOG.info("update: " + session.getEnvId());
				if (oldEnv != null)
					LOG.info("update: found oldEnv: " + oldEnv.getEnvId());
				LOG.info("updates parent env was: " + session.getParentEnvId());

				if (oldEnv != null && session.getParentEnvId() != null && !oldEnv.getEnvId().equals(session.getParentEnvId())) {

					LOG.info("would like to delete " + oldEnv.getEnvId());
					// delete(oldEnv.getEnvId());
				}
				sessions.add(session);
			}
		} catch (JAXBException | IOException e) {
			throw new BWFLAException("saveAsRevision: " + e.getMessage(), e);
		}
		return newEnv.getEnvId();
	}

	public EmilSessionEnvironment getUserSession(String userId, String objectId) {
		System.out.println("userid: " + userId + " objectId " + objectId);
		return sessions.get(userId, objectId);
	}


	class Monitor implements Runnable {
		long retention;

		public Monitor(long retention) {
			this.retention = retention;
		}

		@Override
		public void run() {
			try {
				long now = (new Date().getTime());
				List<EmilSessionEnvironment> sessions = getEmilSessionEnvironments();
				for (EmilSessionEnvironment session : sessions) {
					if (now - session.getCreationDate() > retention * 1000 * 60 * 60) // hours (DNB)
					{
						System.out.println("deleting session: " + session.getCreationDate() + " now: " + now);
						delete(session.getEnvId(), true, true);
					}
				}
			} catch (IOException | BWFLAException | JAXBException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		}


//	public static boolean _replaceEmilEnvironment(EmilEnvironment env, String basedir)
//	{
//		try {
//			EmilEnvironment oldEnv = EmilUtils.getEmilEnvironmentById(basedir, env.getParentEnvId());
//			final Path envpath = Paths.get(basedir, env.getParentEnvId());
//			oldEnv.setVisible(false);
//			saveEmilEnvironment(oldEnv, basedir);
//		}
//		catch (Exception exception) {
//			exception.printStackTrace();
//			return false;
//		}
//		env.setVisible(true);
//		return EmilUtils.saveEmilEnvironment(env, basedir);
//	}


	}
}
