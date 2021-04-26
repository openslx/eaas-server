package de.bwl.bwfla.emil;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.common.FetchOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.*;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.ContainerNetworkingType;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.EmilEnvironmentOwner;
import de.bwl.bwfla.common.services.security.EmilEnvironmentPermissions;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.emil.datatypes.rest.ImportContainerRequest;
import de.bwl.bwfla.emil.datatypes.snapshot.*;
import de.bwl.bwfla.emil.utils.Snapshot;
import de.bwl.bwfla.emucomp.api.*;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Startup
@ApplicationScoped
public class EmilEnvironmentRepository {

	private ImageArchiveClient imagearchive = null;

	@Inject
	private MongodbEaasConnector dbConnector;
	private MongodbEaasConnector.DatabaseInstance db;

	@Inject
	@Config("emil.emilDatabase")
	private String dbName;

	protected final static Logger LOG = Logger.getLogger(EmilEnvironmentRepository.class.getName());

	@Inject
	@Config(value = "commonconf.serverdatadir")
	protected String serverdatadir;

	@Inject
	@Config(value = "ws.objectarchive")
	protected String objectArchive;

	@Inject
	@AuthenticatedUser
	private UserContext authenticatedUser = null;

	@Inject
	private EmilDataImport importHelper;

	private String emilDbCollectionName = "eaasEnv";
	private String emilDbNetworkEnvCollectionName = "emilNetworkEnvironments";

	@Inject
	private EmilObjectData objects;

	@Inject
	private UserSessions sessions;

	@Inject
	private ObjectClassification classification;

	private static boolean initialized = false;

	public boolean isInitialized()
	{
		return initialized;
	}

	public final class MetadataCollection {
		public static final String PUBLIC = "public";
		public static final String REMOTE = "remote";
		public static final String DEFAULT = "default";
	}

	private String getDataCollection(UserContext userctx) {
		if (userctx != null) {

			if (userctx.getUserId() != null)
				return userctx.getUserId();
		}

		// default, if user-context is unknown!
		return emilDbCollectionName;
	}

	private String getDataCollection() {
		return this.getDataCollection(this.getUserContext());
	}

	private UserContext getUserContext() {
		return (authenticatedUser != null) ? authenticatedUser : new UserContext();
	}

	private boolean checkPermissions(EmilEnvironment env, EmilEnvironmentPermissions.Permissions wanted) {
		return this.checkPermissions(env, wanted, this.getUserContext());
	}

	private boolean checkPermissions(EmilEnvironment env, EmilEnvironmentPermissions.Permissions wanted, UserContext userctx) {
		if (env == null)
			return true;

		final var userid = userctx.getUserId();
		final var role = userctx.getRole();

		if(role == Role.ADMIN)
			return true;

		EmilEnvironmentPermissions permissions = env.getPermissions();
		if (permissions == null) // nothing to be done
		{
			// LOG.info("no permissions set");
			return true;
		}

		EmilEnvironmentOwner owner = env.getOwner();
		if (owner != null && owner.getUsername() != null) {
			if (userid == null) {
				LOG.severe("environment " + env.getEnvId() + " access denied to unknown user " + owner.getUsername());
				return false;
			}

			if (!userid.equals(owner.getUsername())) {
				LOG.warning("access denied to environment " + env.getEnvId()
						+ ". Reason username mismatch: owner " + owner.getUsername() + " ctx " + userid);
				return false;
			}
		}

		// TODO: check org-level permissions!

		if (wanted.getValue() > permissions.getUser().getValue()) {
			LOG.info("permission missmatch: got " + permissions.getUser().getValue() + " wanted: " + wanted.getValue());
			return false;
		}

		return true;
	}

	private boolean sameOwner(EmilEnvironment env) {
		if (env == null)
			return false;

		String username = null;
		if (authenticatedUser != null) {
			username = authenticatedUser.getUserId();
		}

		EmilEnvironmentOwner owner = env.getOwner();
		if (owner == null || owner.getUsername() == null) {
			LOG.severe("environment " + env.getEnvId() + " has but no owner data.");
			return (username == null);
		}

		if (username != null && username.equals(owner.getUsername()))
			return true;

		LOG.warning("username mismatch");
		return false;
	}

	public Stream<EmilEnvironment> listPublicEnvironments(int offset, int maxcount, MongodbEaasConnector.FilterBuilder filter) {
		return db.find(MetadataCollection.PUBLIC, offset, maxcount, filter, "type");
	}

	public long countPublicEnvironments(MongodbEaasConnector.FilterBuilder filter) {
		return db.count(MetadataCollection.PUBLIC, filter);
	}


	private Stream<EmilEnvironment> loadEmilEnvironments(UserContext userctx) {
		final var colnames = new ArrayList<String>(3);
		colnames.add(this.getDataCollection(userctx));
		colnames.add(MetadataCollection.PUBLIC);
		colnames.add(MetadataCollection.REMOTE);

		final Function<String, Stream<EmilEnvironment>> finder =
				(colname) -> db.find(colname, new MongodbEaasConnector.FilterBuilder(), "type");

		final var result = colnames.stream()
				.map(finder)
				.reduce(Stream::concat);

		return result.orElse(Stream.empty());
	}

	private List<EmilObjectEnvironment> loadEmilObjectEnvironments(UserContext userctx) throws BWFLAException {
		//TODO: refactor to stream
		return this.loadEmilEnvironments(userctx)
				.filter((env) -> env instanceof EmilObjectEnvironment)
				.map((env) -> (EmilObjectEnvironment) env)
				.collect(Collectors.toList());
	}

	private void setPermissions(EmilEnvironment ee, UserContext userCtx)
	{
		if(userCtx != null)
		{	EmilEnvironmentOwner owner = new EmilEnvironmentOwner();
			owner.setUsername(userCtx.getUserId());
			ee.setOwner(owner);

			EmilEnvironmentPermissions permissions = new EmilEnvironmentPermissions();
			permissions.setUser(EmilEnvironmentPermissions.Permissions.WRITE);
			ee.setPermissions(permissions);
		}
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

	@PostConstruct
	public synchronized void init() {

		try {
			imagearchive = ImageArchiveClient.create();
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Initializing image-archive client failed!", error);
		}

		db = dbConnector.getInstance(dbName);
		try {
			db.createIndex(emilDbCollectionName, "envId");
			db.ensureTimestamp(emilDbCollectionName);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
			initialize();
		}
		catch (BWFLAException| JAXBException e)
		{
			e.printStackTrace();
		}

		initialized = true;

//		try {
//			try {
//				 importExistentEnv();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} catch (BWFLAException e) {
//			LOG.log(Level.SEVERE, e.getMessage(), e);
//			throw new IllegalStateException();
//		}

		// removeMissingEnvironments();
	}

	@PreDestroy
	private void destroy()
	{
		try {
			if (imagearchive != null)
				imagearchive.close();
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Closing image-archive client failed!", error);
		}
	}

	public ImageArchiveClient getImageArchive()
	{
		return imagearchive;
	}

	private EmilEnvironment getSharedEmilEnvironmentById(String envid) {
		try {
			return db.getObjectWithClassFromDatabaseKey(MetadataCollection.PUBLIC, "type", envid, "envId");
		} catch (BWFLAException | NoSuchElementException e) {
			try {
				return db.getObjectWithClassFromDatabaseKey(MetadataCollection.REMOTE, "type", envid, "envId");
			} catch (BWFLAException | NoSuchElementException e1) {
				return null;
			}
		}
	}

	public NetworkEnvironment getEmilNetworkEnvironmentById(String envid) throws BWFLAException {
		return db.getObjectWithClassFromDatabaseKey(emilDbNetworkEnvCollectionName, "type", envid, "envId");
	}

	public void deleteEmilNetworkEnvironment(NetworkEnvironment env) {
		db.deleteDoc(emilDbNetworkEnvCollectionName, env.getEnvId(), env.getIdDBkey());
	}

	public EmilEnvironment getEmilEnvironmentById(String envid)
	{
		return this.getEmilEnvironmentById(envid, this.getUserContext());
	}

	public EmilEnvironment getEmilEnvironmentById(String envid, UserContext userctx) {
		if (envid == null)
			return null;

		try {
			EmilEnvironment env = db.getObjectWithClassFromDatabaseKey(this.getDataCollection(userctx), "type", envid, "envId");
			if (!this.checkPermissions(env, EmilEnvironmentPermissions.Permissions.READ, userctx))
				return getSharedEmilEnvironmentById(envid);

			return env;
		} catch (BWFLAException | NoSuchElementException e) {
			return getSharedEmilEnvironmentById(envid);
		}
	}

	public boolean isEnvironmentVisible(EmilEnvironment env)
	{
		Set<String> ids = env.getChildrenEnvIds();
		if(ids == null || ids.size() == 0)
			return true;

		if(!env.getArchive().equals("default"))
		{
			for(String id : ids)
			{
				if(getSharedEmilEnvironmentById(id) != null)
					return false;
			}
			return true;
		}
		return false;
	}

	//TODO: refactor
	public List<EmilObjectEnvironment> getEmilObjectEnvironmentByObject(String objectId, UserContext userCtx) throws BWFLAException {
		// TODO: refactor to use Stream<T>
		List<EmilObjectEnvironment> result = new ArrayList<>();
		if (objectId == null)
			return result;

		List<EmilObjectEnvironment> all = loadEmilObjectEnvironments(userCtx);
		for (EmilObjectEnvironment objEnv : all) {
			if (objEnv.getObjectId().equals(objectId) && isEnvironmentVisible(objEnv)
					&& checkPermissions(objEnv, EmilEnvironmentPermissions.Permissions.READ, userCtx))
				result.add(objEnv);
		}
		return result;
	}

	synchronized public void replicate(EmilEnvironment env, String destArchive, UserContext userctx) throws JAXBException, BWFLAException {
		if(env.getArchive().equals(MetadataCollection.DEFAULT)) {
			final var collection = this.getDataCollection(userctx);
			db.deleteDoc(collection, env.getEnvId(), env.getIdDBkey());

			String parent = env.getParentEnvId();
			String lastPrivateChild = env.getEnvId();
			while(parent != null)
			{
				EmilEnvironment p = getEmilEnvironmentById(parent, userctx);
				if(p == null)
				{
					throw new BWFLAException("parent image " + parent + " not found");
				}
				if(!p.getArchive().equals(destArchive))
				{
					final var pe = imagearchive.api()
							.v2()
							.environments()
							.fetch(p.getEnvId());

					if(pe == null)
						throw new BWFLAException("parent id not found! loose end: \n" + p.value(true));

					final var options = new ReplaceOptionsV2()
							.setLocation(destArchive);

					imagearchive.api()
							.v2()
							.environments()
							.replace(pe.getId(), pe, options);

					LOG.severe("trying to delete parent with id: " + p.getEnvId() + " in archive " + p.getArchive());
					try {
						db.deleteDoc(collection, p.getEnvId(), p.getIdDBkey());
					} catch (Exception e)
					{
						e.printStackTrace();
						LOG.severe("fix me");
					}
					p.setArchive(destArchive);
					save(p, false, userctx);
					lastPrivateChild = p.getEnvId();
					parent = p.getParentEnvId();
				}
				else // first parent in dest archive, connecting
				{
					p.addChildEnvId(lastPrivateChild);
					save(p, false, userctx);
					parent  = null;
				}
			}
		}
		env.setArchive(destArchive);
		save(env, false, userctx);
	}

	public void save(EmilEnvironment env, boolean setPermission) throws BWFLAException {
		save(env, setPermission, getUserContext());
	}

	public void save(EmilEnvironment env, boolean setPermission, UserContext userCtx) throws BWFLAException {

		env.setTimestamp(Instant.now().toString());

		if(env.getArchive() == null)
			env.setArchive(MetadataCollection.DEFAULT);

		if(env.getArchive().equals(MetadataCollection.PUBLIC) || env.getArchive().equals(MetadataCollection.REMOTE))
		{
			env.setOwner(null);
			EmilEnvironmentPermissions permissions = new EmilEnvironmentPermissions();
			permissions.setUser(EmilEnvironmentPermissions.Permissions.READ);
			env.setPermissions(permissions);
		}
		else {
			if(setPermission)
				setPermissions(env, userCtx);
		}

		switch (env.getArchive())
		{
			case MetadataCollection.PUBLIC:
				db.saveDoc(MetadataCollection.PUBLIC, env.getEnvId(), env.getIdDBkey(), env.jsonValueWithoutRoot(false));
				break;
			case MetadataCollection.REMOTE:
				db.saveDoc(MetadataCollection.REMOTE, env.getEnvId(), env.getIdDBkey(), env.jsonValueWithoutRoot(false));
				break;
			default:
				db.saveDoc(this.getDataCollection(userCtx), env.getEnvId(), env.getIdDBkey(), env.jsonValueWithoutRoot(false));
		}
		// LOG.severe(env.toString());
	}

	public void saveNetworkEnvironemnt(NetworkEnvironment env) throws BWFLAException {
		db.saveDoc(emilDbNetworkEnvCollectionName , env.getEnvId(), env.getIdDBkey(), env.jsonValueWithoutRoot(false));
	}

	public synchronized <T extends JaxbType> void delete(String envId, boolean deleteMetadata, boolean deleteImages) throws BWFLAException {
		EmilEnvironment env = getEmilEnvironmentById(envId);
		if(env == null)
			throw new BWFLAException("Environment " + envId + " is not available");
		
		if(!checkPermissions(env, EmilEnvironmentPermissions.Permissions.WRITE))
			throw new BWFLAException("permission denied");

		if (!(env instanceof EmilSessionEnvironment)) {
			if (env.getParentEnvId() != null) {
				EmilEnvironment parentEnv = getEmilEnvironmentById(env.getParentEnvId());
				if(parentEnv != null) {
					parentEnv.removeChildEnvId(envId);
					parentEnv.removeBranchEnvId(envId);
					save(parentEnv, false);
				}
			}

			if (env.getChildrenEnvIds().size() == 0 && env.getBranches().size() == 0) {
				try {
					imagearchive.api()
							.v2()
							.environments()
							.delete(envId, deleteMetadata, deleteImages);
				}
				catch (Exception error) {
					LOG.log(Level.WARNING, "Deleting environment failed!", error);
				}

				db.deleteDoc(this.getDataCollection(), envId, env.getIdDBkey());
				classification.cleanupClassificationData(envId);
			}
		} else {
			sessions.delete((EmilSessionEnvironment)env);
		}
	}

	public  <T extends JaxbType> ArrayList<T> getDatabaseContent(String type, Class<T> klass ) throws BWFLAException {
		return db.getRootlessJaxbObjects(this.getDataCollection(), type, "type");
	}

	public void importOldDb() throws BWFLAException {
		List<EmilEnvironment> oldEnvs = null;
		try {
			 oldEnvs = importHelper.importExistentEnv(dbConnector.getInstance("eaas"), "emilEnv");
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(EmilEnvironment env : oldEnvs)
		{
			final Environment e = imagearchive.api()
					.v2()
					.environments()
					.fetch(env.getEnvId());

			if(e == null)
			{
				LOG.warning("old env import failed. env not found: " + env.getEnvId());
				continue;
			}

			LOG.warning("importing " + env.getEnvId());
			LOG.warning(env.toString());

			env.setArchive(MetadataCollection.DEFAULT);
			save(env, false);

			EmilEnvironment __env = getEmilEnvironmentById(env.getEnvId());
			LOG.warning(__env.isVisible() + " y");
		}
	}

	public int initialize() throws JAXBException, BWFLAException {
		int counter = 0;

		importFromFolder("import");

		final BiFunction<String, Environment, Integer> importer = (archive, env) -> {
			try {
				// LOG.warning("found env " + env.getId()	 + " in archive " + a);
				EmilEnvironment emilEnv = getEmilEnvironmentById(env.getId());
				if(emilEnv != null && (emilEnv.getArchive() == null || !emilEnv.getArchive().equals(archive)))
				{
					EmilEnvironmentPermissions permissions = new EmilEnvironmentPermissions();
					permissions.setUser(EmilEnvironmentPermissions.Permissions.READ);
					emilEnv.setPermissions(permissions);

					emilEnv.setArchive(archive);
					save(emilEnv, false);
				}

				if ((emilEnv == null && env instanceof ContainerConfiguration)) {
					EmilContainerEnvironment ee = new EmilContainerEnvironment();
					saveImport(env, ee);
				} else if (emilEnv == null && env instanceof MachineConfiguration) {
					EmilEnvironment ee;
					String objectId = EmulationEnvironmentHelper.isObjectEnvironment((MachineConfiguration) env);
					if (objectId != null) {
						ee = new EmilObjectEnvironment();
						((EmilObjectEnvironment) ee).setObjectId(objectId);
					} else
						ee = new EmilEnvironment();
					// since you have to initialize object with correct instance, save function could not be moved out of if-else scope
					ee.setArchive(archive);
					ee.setEnvId(env.getId());
					ee.setTitle(env.getDescription().getTitle());
					ee.setEmulator(((MachineConfiguration) env).getEmulator().getBean());
					ee.setOs("n.a.");
					ee.setDescription("imported base environment");

					if(authenticatedUser != null && authenticatedUser.getUserId() != null)
					{
						EmilEnvironmentOwner owner = new EmilEnvironmentOwner();
						owner.setUsername(authenticatedUser.getUserId());

						EmilEnvironmentPermissions permissions = new EmilEnvironmentPermissions();
						permissions.setUser(EmilEnvironmentPermissions.Permissions.READ);
						ee.setPermissions(permissions);
					}
					save(ee, false);
					return 1;
				}

				return 0;
			}
			catch (Exception error) {
				throw new RuntimeException(error);
			}
		};

		final var locations = imagearchive.api()
				.v2()
				.storage()
				.locations()
				.list();

		try (locations) {
			for (var iter = locations.iterator(); iter.hasNext();) {
				final var location = iter.next();
				if (location.equals("default"))
					continue;

				final var options = new FetchOptionsV2()
						.setLocation(location);

				final var environments = imagearchive.api()
						.v2()
						.environments()
						.fetch(options);

				try (environments) {
					counter += environments.stream()
							.map((env) -> importer.apply(location, env))
							.reduce(0, Integer::sum);
				}
			}
		}

		return counter;
	}

	private void importFromFolder(String directory) throws BWFLAException {
		HashMap<String, List<EmilEnvironment>> _result = importHelper.importFromFolder(directory);
		for(String collection : _result.keySet()) {
			List<EmilEnvironment> envs = _result.get(collection);
			for (EmilEnvironment e : envs)
			{
				LOG.severe("saving " + collection + " -> " + e.jsonValueWithoutRoot(true));
				db.saveDoc(collection, e.getEnvId(), e.getIdDBkey(), e.jsonValueWithoutRoot(false));
			}
		}
	}

	void export()
	{
		List<String> collections = db.getCollections();
		try {
			EmilDataExport emilDataExport = new EmilDataExport();
			for(String collection : collections) {
				try {
					Stream<EmilEnvironment> all = db.find(collection, new MongodbEaasConnector.FilterBuilder(), "type");
					all.forEach(emilEnvironment -> {
						try {
							emilDataExport.saveEnvToPath(collection, emilEnvironment);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					LOG.warning("failed to export collection: " + collection);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOG.severe("failed to initialize export");
		}
	}

	private void saveImport(Environment env, EmilEnvironment ee) throws JAXBException, BWFLAException {
		ee.setEnvId(env.getId());
		ee.setTitle(env.getDescription().getTitle());
		ee.setAuthor("n.a.");
		if (env instanceof ContainerConfiguration) {
			((EmilContainerEnvironment) ee).setInput(((ContainerConfiguration) env).getInput());
			((EmilContainerEnvironment) ee).setOutput(((ContainerConfiguration) env).getOutputPath());
			if(env instanceof  OciContainerConfiguration){
				((EmilContainerEnvironment) ee).setArgs(((OciContainerConfiguration) env).getProcess().getArguments());
				((EmilContainerEnvironment) ee).setEnv(((OciContainerConfiguration) env).getProcess().getEnvironmentVariables());
			}
		}
		if (env instanceof MachineConfiguration)
			ee.setEmulator(((MachineConfiguration) env).getEmulator().getBean());
		else
			ee.setEmulator("n.a.");

		ee.setTitle(env.getDescription().getTitle());
		ee.setOs("n.a.");
		ee.setDescription("n.a.");
		save(ee, true);
	}


	public Stream<EmilEnvironment> getEmilEnvironments(UserContext userCtx)
	{
		final Stream<EmilEnvironment> all = loadEmilEnvironments(userCtx);
		final HashSet<String> known = new HashSet<>();

		return all.filter(this::isEnvironmentVisible)
				.filter(e -> (userCtx == null || checkPermissions(e, EmilEnvironmentPermissions.Permissions.READ, userCtx)))
				.filter(e -> {
					if (known.contains(e.getEnvId()))
						return false;
					return known.add(e.getEnvId());
				});
	}

	public List<NetworkEnvironment> getNetworkEnvironments() {
		Stream<NetworkEnvironment> emilNetworkEnvironments = db.find(emilDbNetworkEnvCollectionName, new MongodbEaasConnector.FilterBuilder(), "type");
		return emilNetworkEnvironments.collect(Collectors.toList());
	}

	public Stream<EmilEnvironment> getEmilEnvironments() {
		return this.getEmilEnvironments(this.getUserContext());
	}

	public List<EmilEnvironment> getChildren(String envId, List<EmilEnvironment> envs) throws BWFLAException {
		return this.getChildren(envId, envs, this.getUserContext());
	}

	public List<EmilEnvironment> getChildren(String envId, List<EmilEnvironment> envs, UserContext userCtx) throws BWFLAException {
		List<EmilEnvironment> result = new ArrayList<>();
		for (EmilEnvironment e : envs) {
			if (e.getParentEnvId() != null && e.getParentEnvId().equals(envId)) {
				LOG.info("getChildren: found child " + e.getEnvId() + " for envId " + envId);
				result.addAll(getChildren(e.getEnvId(), envs, userCtx));
			}
		}
		if (result.size() == 0) {
			LOG.info("no child found for " + envId);
			EmilEnvironment emilEnvironment = getEmilEnvironmentById(envId, userCtx);
			if (emilEnvironment != null)
				result.add(emilEnvironment);
		}
		return result;
	}


	public String saveAsUserSession(Snapshot snapshot, SaveUserSessionRequest request) throws BWFLAException {
		String sessionEnvId = snapshot.saveUserSession(imagearchive, objects.helper(), request);

		EmilEnvironment parentEnv = getEmilEnvironmentById(request.getEnvId());
		if (parentEnv == null)
			throw new BWFLAException("parent environment not found: " + request.getEnvId());

		EmilSessionEnvironment sessionEnv = new EmilSessionEnvironment(parentEnv);
		sessionEnv.setObjectId(request.getObjectId());
		sessionEnv.setCreationDate((new Date()).getTime());
		sessionEnv.setUserId(request.getUserId());
		sessionEnv.setParentEnvId(parentEnv.getEnvId());

		LOG.info("adding session for user: " + request.getUserId()
				+ " object: " + request.getObjectId() + " env " + sessionEnvId);

		sessionEnv.setEnvId(sessionEnvId);

		save(sessionEnv, false);
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
		save(parentEnv, false);
		return sessionEnv.getEnvId();
	}

	synchronized String saveAsObjectEnvironment(Snapshot snapshot, SaveObjectEnvironmentRequest request) throws BWFLAException {

		String archiveName = request.getObjectArchiveId();
		if (archiveName == null) {
			if(authenticatedUser == null || authenticatedUser.getUserId() == null)
				request.setObjectArchiveId("default");
			else
				request.setObjectArchiveId(authenticatedUser.getUserId());
		}

		if(request.getObjectArchiveId() == null)
			request.setObjectArchiveId(archiveName);

		EmilEnvironment parentEnv = getEmilEnvironmentById(request.getEnvId());
		EmilObjectEnvironment ee = snapshot.createObjectEnvironment(imagearchive, objects.helper(), request);

		parentEnv.addBranchId(ee.getEnvId());
		save(parentEnv, false);
		save(ee, true);

		return ee.getEnvId();
	}

	public void saveImportedContainer(String envId, ImportContainerRequest req, UserContext userCtx) throws BWFLAException
	{
		EmilContainerEnvironment env = new EmilContainerEnvironment();
		env.setEnvId(envId);
		env.setTitle(req.getTitle());
		env.setDescription(req.getDescription());
		env.setInput(req.getInputFolder());
		env.setOutput(req.getOutputFolder());
		env.setArgs(req.getProcessArgs());
		env.setEnv(req.getProcessEnvs());

		if(req.getRuntimeId() != null)
			env.setRuntimeId(req.getRuntimeId());
		if(req.isEnableNetwork())
		{
			ContainerNetworkingType net = new ContainerNetworkingType();
			net.setConnectEnvs(true);
			env.setNetworking(net);
		}

		env.setServiceContainer(req.isServiceContainer());
		env.setAuthor(req.getAuthor());
		save(env, true, userCtx);
	}

	String saveAsRevision(Snapshot snapshot, SaveDerivateRequest req, boolean checkpoint) throws BWFLAException {

		EmilEnvironment env = getEmilEnvironmentById(req.getEnvId());
		if (env == null) {
			if (req instanceof SaveCreatedEnvironmentRequest) {
				// no emil env -> new environment has been created and committed
				final Environment _env = imagearchive.api()
						.v2()
						.environments()
						.fetch(req.getEnvId());

				env = new EmilEnvironment();
				env.setTitle(_env.getDescription().getTitle());
				env.setEnvId(req.getEnvId());
				env.setDescription("empty hard disk");
			} else
				throw new BWFLAException("Environment with id " + req.getEnvId() + " not found");
		}

		EmilEnvironment newEnv = snapshot.createEnvironment(imagearchive, req, env, checkpoint);
		if (req instanceof SaveCreatedEnvironmentRequest)
			newEnv.setTitle(((SaveCreatedEnvironmentRequest) req).getTitle());

		if(req instanceof SaveNewEnvironmentRequest)
		{
			env.addBranchId(newEnv.getEnvId());
		}
		else
			env.addChildEnvId(newEnv.getEnvId());


		save(env, false);
		save(newEnv, true);
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

		return newEnv.getEnvId();
	}

	public EmilSessionEnvironment getUserSession(String userId, String objectId) {
		System.out.println("userid: " + userId + " objectId " + objectId);
		return sessions.get(userId, objectId);
	}

	public List<EmilEnvironment> getParents(String envId)
	{
		EmilEnvironment root = getEmilEnvironmentById(envId);
		List<EmilEnvironment> result = new ArrayList<>();
		if(root == null)
		{
			LOG.severe("no environment found for id: " + envId);
			return result;
		}

		if (root.getParentEnvId() != null) {
			EmilEnvironment parentEnv = getEmilEnvironmentById(root.getParentEnvId());
			while (parentEnv != null) {
				result.add(parentEnv);
				parentEnv = getEmilEnvironmentById(parentEnv.getParentEnvId());
			}
		}
		return result;
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
