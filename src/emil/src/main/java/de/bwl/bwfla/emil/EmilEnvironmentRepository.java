package de.bwl.bwfla.emil;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.api.imagearchive.ImageArchiveMetadata;
import de.bwl.bwfla.api.imagearchive.ImageType;
import de.bwl.bwfla.common.datatypes.EnvironmentDescription;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.ContainerNetworkingType;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.EmilEnvironmentOwner;
import de.bwl.bwfla.common.services.security.EmilEnvironmentPermissions;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.emil.datatypes.snapshot.*;
import de.bwl.bwfla.emil.utils.Snapshot;
import de.bwl.bwfla.emucomp.api.*;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ApplicationScoped
public class EmilEnvironmentRepository {

	@Inject
	private MongodbEaasConnector dbConnector;
	private MongodbEaasConnector.DatabaseInstance db;

	@Inject
	private DatabaseEnvironmentsAdapter environmentsAdapter;

	@Inject
	@Config("emil.emilDatabase")
	private String dbName;

	protected final static Logger LOG = Logger.getLogger(EmilEnvironmentRepository.class.getName());

	static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

	@Inject
	@Config(value = "commonconf.serverdatadir")
	protected String serverdatadir;

	@Inject
	@Config(value = "ws.objectarchive")
	protected String objectArchive;

	@Inject
	@Config(value = "ws.imagearchive")
	protected String imageArchive;

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

	public final class MetadataCollection {
		public static final String PUBLIC = "public";
		public static final String REMOTE = "remote";
		public static final String DEFAULT = "default";
	}

	private boolean checkPermissions(String envId, EmilEnvironmentPermissions.Permissions wanted) throws BWFLAException {
		EmilEnvironment env = getEmilEnvironmentById(envId);
		return checkPermissions(env, wanted);
	}

	private String getCollectionCtx(String username) {
		if (username == null)
			return emilDbCollectionName;
		return username;
	}

	private String getCollectionCtx() {
		if (authenticatedUser == null || authenticatedUser.getUserId() == null)
			return emilDbCollectionName;

		return authenticatedUser.getUserId();
	}

	private String getUserCtx() {
		if (authenticatedUser == null)
			return null;

		return authenticatedUser.getUserId();
	}

	private boolean checkPermissions(EmilEnvironment env, EmilEnvironmentPermissions.Permissions wanted) {

		String userCtx = getUserCtx();
		if(userCtx == null)
			return true;

		return checkPermissions(env, wanted, getUserCtx());
	}

	private boolean checkPermissions(EmilEnvironment env, EmilEnvironmentPermissions.Permissions wanted, String userCtx) {
		if (env == null)
			return true;

		EmilEnvironmentPermissions permissions = env.getPermissions();
		if (permissions == null) // nothing to be done
		{
			// LOG.info("no permissions set");
			return true;
		}

		EmilEnvironmentOwner owner = env.getOwner();
		if (owner != null && owner.getUsername() != null) {
			if (userCtx == null) {
				LOG.severe("environment " + env.getEnvId() + " access denied to unknown user " + owner.getUsername());
				return false;
			}

			if (!userCtx.equals(owner.getUsername())) {
				LOG.warning("access denied to environment " + env.getEnvId()
						+ ". Reason username mismatch: owner " + owner.getUsername() + " ctx " + userCtx);
				return false;
			}
		}

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


	private Stream<EmilEnvironment> loadEmilEnvironments(String userCtx) {
		Stream<EmilEnvironment> all = db.find(getCollectionCtx(userCtx), new MongodbEaasConnector.FilterBuilder(), "type");
		all = Stream.concat(all, db.find(MetadataCollection.PUBLIC, new MongodbEaasConnector.FilterBuilder(), "type"));
		all = Stream.concat(all, db.find(MetadataCollection.REMOTE, new MongodbEaasConnector.FilterBuilder(), "type"));
		return all;
	}

	private List<EmilObjectEnvironment> loadEmilObjectEnvironments(String userCtx) throws BWFLAException {
		//TODO: refactor to stream
		List<EmilObjectEnvironment> result = db.getRootlessJaxbObjects(getCollectionCtx(userCtx),
				EmilObjectEnvironment.class.getCanonicalName(), "type");

		result.addAll(db.getRootlessJaxbObjects(MetadataCollection.PUBLIC,
				EmilObjectEnvironment.class.getCanonicalName(), "type"));

		result.addAll(db.getRootlessJaxbObjects(MetadataCollection.REMOTE,
				EmilObjectEnvironment.class.getCanonicalName(), "type"));

		return result;
	}

	private void setPermissions(EmilEnvironment ee) {
		if (authenticatedUser != null && authenticatedUser.getUserId() != null) {
			EmilEnvironmentOwner owner = new EmilEnvironmentOwner();
			owner.setUsername(authenticatedUser.getUserId());
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
		return getEmilEnvironmentById(envid, getUserCtx());
	}

	public EmilEnvironment getEmilEnvironmentById(String envid, String userCtx) {
		if (envid == null)
			return null;

		try {
			EmilEnvironment env = db.getObjectWithClassFromDatabaseKey(getCollectionCtx(userCtx), "type", envid, "envId");

			if (!checkPermissions(env, EmilEnvironmentPermissions.Permissions.READ, userCtx))
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
	public List<EmilObjectEnvironment> getEmilObjectEnvironmentByObject(String objectId, String userCtx) throws BWFLAException {
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

	synchronized public void replicate(EmilEnvironment env, String destArchive, String userctx) throws JAXBException, BWFLAException {
		if(env.getArchive().equals(MetadataCollection.DEFAULT)) {
			String collection = null;
			String username = null;

			if(userctx == null) {
				collection = emilDbCollectionName;
				// throw new BWFLAException("no user context in publish image context");
			}
			else
			{
				collection = userctx;
				username = userctx;
			}
			db.deleteDoc(collection, env.getEnvId(), env.getIdDBkey());

			String parent = env.getParentEnvId();
			String lastPrivateChild = env.getEnvId();
			while(parent != null)
			{
				EmilEnvironment p = getEmilEnvironmentById(parent, username);
				if(p == null)
				{
					throw new BWFLAException("parent image " + parent + " not found");
				}
				if(!p.getArchive().equals(destArchive))
				{
					Environment pe = environmentsAdapter.getEnvironmentById(p.getArchive(), p.getEnvId());
					if(pe == null)
						throw new BWFLAException("parent id not found! loose end: \n" + p.value(true));

					ImageArchiveMetadata iam = new ImageArchiveMetadata();
					iam.setType(ImageType.USER);
					environmentsAdapter.importMetadata(destArchive, pe, iam, true);
					LOG.severe("trying to delete parent with id: " + p.getEnvId() + " in archive " + p.getArchive());
					try {
						db.deleteDoc(collection, p.getEnvId(), p.getIdDBkey());
					} catch (Exception e)
					{
						e.printStackTrace();
						LOG.severe("fix me");
					}
					p.setArchive(destArchive);
					save(p, false);
					lastPrivateChild = p.getEnvId();
					parent = p.getParentEnvId();
				}
				else // first parent in dest archive, connecting
				{
					p.addChildEnvId(lastPrivateChild);
					save(p, false);
					parent  = null;
				}
			}
		}
		env.setArchive(destArchive);
		save(env, false);
	}

	public void save(EmilEnvironment env, boolean setPermission) throws BWFLAException {

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
				setPermissions(env);
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
				db.saveDoc(getCollectionCtx(), env.getEnvId(), env.getIdDBkey(), env.jsonValueWithoutRoot(false));
		}
		// LOG.severe(env.toString());
	}

	public void saveNetworkEnvironemnt(NetworkEnvironment env) throws BWFLAException {
		db.saveDoc(emilDbNetworkEnvCollectionName , env.getEnvId(), env.getIdDBkey(), env.jsonValueWithoutRoot(false));
	}

	public synchronized <T extends JaxbType> void delete(String envId, boolean deleteMetadata, boolean deleteImages) throws BWFLAException {
		EmilEnvironment env = getEmilEnvironmentById(envId);
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
				LOG.info("deleting env " + env.getEnvId());
				try {
					environmentsAdapter.delete(env.getArchive(), envId, deleteMetadata, deleteImages);
				} catch (NoSuchElementException | BWFLAException e) {
					e.printStackTrace();
				}
				db.deleteDoc(getCollectionCtx(), envId, env.getIdDBkey());
				classification.cleanupClassificationData(envId);
			}
		} else {
			sessions.delete((EmilSessionEnvironment)env);
		}
	}

	public  <T extends JaxbType> ArrayList<T> getDatabaseContent(String type, Class<T> klass ) throws BWFLAException {
		return db.getRootlessJaxbObjects(getCollectionCtx(), type, "type");
	}

	public int initialize() throws JAXBException, BWFLAException {
		int counter = 0;

//		List<EmilEnvironment> oldEnvs = null;
//		try {
//			 oldEnvs = importHelper.importExistentEnv(dbConnector.getInstance("eaas"), "emilEnv");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		for(EmilEnvironment env : oldEnvs)
//		{
//			Environment e = environmentsAdapter.getEnvironmentById("default", env.getEnvId());
//			if(e == null)
//			{
//				LOG.warning("old env import failed. env not found: " + env.getEnvId());
//				continue;
//			}
//
//			LOG.warning("importing " + env.getEnvId());
//			LOG.warning(env.toString());
//			LOG.warning(env.isVisible() + " xxx \n");
//
//			env.setArchive(MetadataCollection.PUBLIC);
//			save(env, false);
//
//			EmilEnvironment __env = getEmilEnvironmentById(env.getEnvId());
//			LOG.warning(__env.isVisible() + "yyy");
//		}

		importFromFolder("import");

		Collection<String> archives = environmentsAdapter.listBackendNames();
		for(String a : archives) {
			List<Environment> envs;
			//if(a.equals("default"))
			//	continue;
			//else
				envs = environmentsAdapter.getEnvironments(a);

			for (Environment env : envs) {
				// LOG.warning("found env " + env.getId()	 + " in archive " + a);
				EmilEnvironment emilEnv = getEmilEnvironmentById(env.getId());

				if(emilEnv != null && (emilEnv.getArchive() == null || !emilEnv.getArchive().equals(a)))
				{
					EmilEnvironmentPermissions permissions = new EmilEnvironmentPermissions();
					permissions.setUser(EmilEnvironmentPermissions.Permissions.READ);
					emilEnv.setPermissions(permissions);

					emilEnv.setArchive(a);
					save(emilEnv, false);
				}

				if ((emilEnv == null && env instanceof ContainerConfiguration)) {
					EmilContainerEnvironment ee = new EmilContainerEnvironment();
					saveImport(env, ee);
				} else if (emilEnv == null && env instanceof MachineConfiguration) {
					counter++;
					EmilEnvironment ee;
					String objectId = EmulationEnvironmentHelper.isObjectEnvironment((MachineConfiguration) env);
					if (objectId != null) {
						ee = new EmilObjectEnvironment();
						((EmilObjectEnvironment) ee).setObjectId(objectId);
					} else
						ee = new EmilEnvironment();
					// since you have to initialize object with correct instance, save function could not be moved out of if-else scope
					ee.setArchive(a);
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

						if(a.equals("default"))
							ee.setOwner(owner);
						ee.setPermissions(permissions);
					}
					save(ee, false);
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


	public List<EmilEnvironment> getEmilEnvironments(String userCtx)
	{
		final Stream<EmilEnvironment> all = loadEmilEnvironments(userCtx);
		final HashSet<String> known = new HashSet<>();

		return all.filter(e -> {
			if(known.contains(e.getEnvId()))
				return false;
			return known.add(e.getEnvId());
		}).filter(this::isEnvironmentVisible)
				.filter(e-> (authenticatedUser == null || checkPermissions(e, EmilEnvironmentPermissions.Permissions.READ, userCtx)))
				.collect(Collectors.toList());
	}

	public List<NetworkEnvironment> getNetworkEnvironments() {
		Stream<NetworkEnvironment> emilNetworkEnvironments = db.find(emilDbNetworkEnvCollectionName, new MongodbEaasConnector.FilterBuilder(), "type");
		return emilNetworkEnvironments.collect(Collectors.toList());
	}

	public List<EmilEnvironment> getEmilEnvironments() {

		String userCtx = getUserCtx();
		return getEmilEnvironments(userCtx);
	}

	public List<EmilEnvironment> getChildren(String envId, List<EmilEnvironment> envs) throws BWFLAException {
		String userCtx = getUserCtx();
		return getChildren(envId, envs, userCtx);
	}

	public List<EmilEnvironment> getChildren(String envId, List<EmilEnvironment> envs, String userCtx) throws BWFLAException {
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
		String sessionEnvId = snapshot.saveUserSession(environmentsAdapter, objects.helper(), request);

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
		EmilObjectEnvironment ee = snapshot.createObjectEnvironment(environmentsAdapter, objects.helper(), request);

		parentEnv.addBranchId(ee.getEnvId());
		save(parentEnv, false);
		save(ee, true);

		return ee.getEnvId();
	}

	@Deprecated
	String saveImport(Snapshot snapshot, SaveImportRequest request) throws BWFLAException {
		Environment environment = environmentsAdapter.getEnvironmentById(request.getArchive(), request.getEnvId());
		EnvironmentDescription description = new EnvironmentDescription();
		description.setTitle(request.getTitle());
		environment.setDescription(description);

		environmentsAdapter.updateMetadata("default", environment);
		environmentsAdapter.commitTempEnvironment("default", request.getEnvId());

		EmilEnvironment newEmilEnv = getEmilEnvironmentById(request.getEnvId());

		if (newEmilEnv != null)
			throw new BWFLAException("import failed: environment with id: " + request.getEnvId() + " exists.");

		newEmilEnv = new EmilEnvironment();
		newEmilEnv.setTitle(request.getTitle());
		newEmilEnv.setEnvId(request.getEnvId());
		newEmilEnv.setAuthor(request.getAuthor());
		newEmilEnv.setEnableRelativeMouse(request.isRelativeMouse());

		newEmilEnv.setDescription(request.getMessage());
		save(newEmilEnv, true);

		return request.getEnvId();
	}


	void saveImportedContainer(SaveImportedContainerRequest req) throws BWFLAException {
		environmentsAdapter.commitTempEnvironmentWithCustomType("default", req.getId(), "containers");

		EmilEnvironment newEmilEnv = getEmilEnvironmentById(req.getId());
		if (newEmilEnv != null)
			throw new BWFLAException("import failed: environment with id: " + req.getId() + " exists.");

		OciContainerConfiguration containerConfiguration = (OciContainerConfiguration) environmentsAdapter.getEnvironmentById("default", req.getId());

		EmilContainerEnvironment env = new EmilContainerEnvironment();
		env.setEnvId(req.getId());
		env.setTitle(req.getTitle());
		env.setDescription(req.getDescription());
		env.setInput(containerConfiguration.getInput());
		env.setOutput(containerConfiguration.getOutputPath());
		env.setArgs(containerConfiguration.getProcess().getArguments());
		if(req.getRuntimeId() != null)
			env.setRuntimeId(req.getRuntimeId());
		if(req.isEnableNetwork())
		{
			ContainerNetworkingType net = new ContainerNetworkingType();
			net.setConnectEnvs(true);
			env.setNetworking(net);
		}
		if (containerConfiguration.getProcess().getEnvironmentVariables() != null)
			env.setEnv(containerConfiguration.getProcess().getEnvironmentVariables());

		env.setServiceContainer(req.isServiceContainer());
		env.setAuthor(req.getAuthor());
		save(env, true);
	}

	String saveAsRevision(Snapshot snapshot, SaveDerivateRequest req, boolean checkpoint) throws BWFLAException {

		EmilEnvironment env = getEmilEnvironmentById(req.getEnvId());
		if (env == null) {
			if (req instanceof SaveCreatedEnvironmentRequest) {
				// no emil env -> new environment has been created and committed
				Environment _env = null;
				_env = environmentsAdapter.getEnvironmentById(req.getArchive(), req.getEnvId());

				env = new EmilEnvironment();
				env.setTitle(_env.getDescription().getTitle());
				env.setEnvId(req.getEnvId());
				env.setDescription("empty hard disk");
			} else
				throw new BWFLAException("Environment with id " + " not found");
		}

		EmilEnvironment newEnv = snapshot.createEnvironment(environmentsAdapter, req, env, checkpoint);
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
