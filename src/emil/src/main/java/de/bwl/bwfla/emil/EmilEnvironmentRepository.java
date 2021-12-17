package de.bwl.bwfla.emil;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.ImageArchiveMappers;
import com.openslx.eaas.imagearchive.api.v2.common.CountOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.FetchOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.databind.MetaDataKindV2;
import com.openslx.eaas.migration.IMigratable;
import com.openslx.eaas.migration.MigrationRegistry;
import com.openslx.eaas.migration.MigrationUtils;
import com.openslx.eaas.migration.config.MigrationConfig;
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
import de.bwl.bwfla.emil.utils.ImportCounts;
import de.bwl.bwfla.emucomp.api.*;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.util.*;
import java.util.stream.Stream;


@ApplicationScoped
public class EmilEnvironmentRepository implements IMigratable
{

	private ImageArchiveClient imagearchive = null;

	@Inject
	@Deprecated
	private MongodbEaasConnector dbConnector;

	@Deprecated
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

	@Inject
	private EmilObjectData objects;

	@Inject
	private UserSessions sessions;

	@Inject
	private ObjectClassification classification;

	/** Mapper for parsed JSON to emil-environment */
	private final ImageArchiveMappers.FromJsonTree<EmilEnvironment> ENVIRONMENT_MAPPER
			= new ImageArchiveMappers.FromJsonTree<>(EmilEnvironment.class);

	/** Mapper for parsed JSON to network-environment */
	private final ImageArchiveMappers.FromJsonTree<NetworkEnvironment> NETWORK_MAPPER
			= new ImageArchiveMappers.FromJsonTree<>(NetworkEnvironment.class);

	private static boolean initialized = false;

	public boolean isInitialized()
	{
		return initialized;
	}

	public static final class MetadataCollection {
		public static final String PUBLIC = "public";
		public static final String REMOTE = "remote";
		public static final String DEFAULT = "default";
	}

	private UserContext getUserContext() {
		return (authenticatedUser != null) ? authenticatedUser : new UserContext();
	}

	@Deprecated
	private boolean checkPermissions(EmilEnvironment env, EmilEnvironmentPermissions.Permissions wanted) {
		return this.checkPermissions(env, wanted, this.getUserContext());
	}

	public boolean checkPermissions(EmilEnvironment env, EmilEnvironmentPermissions.Permissions wanted, UserContext userctx) {
		if (env == null)
			return true;

		if(!userctx.isAvailable()) { // authentification disabled
			return true;
		}
		final var tenantid = userctx.getTenantId();
		final var userid = userctx.getUserId();
		final var role = userctx.getRole();

		EmilEnvironmentPermissions permissions = env.getPermissions();
		if (permissions == null) // nothing to be done
		{
			// LOG.info("no permissions set");
			return true;
		}

		final var message = "Access denied for user '%s' to %s environment '%s'!";

		EmilEnvironmentOwner owner = env.getOwner();
		if (owner != null) {
			if (owner.getUsergroup() != null && tenantid != null) {
				// looks like a node-private environment...
				if (!tenantid.equals(owner.getUsergroup())) {
					LOG.warning(String.format(message, userid, "node-private", env.getEnvId()));
					return false;
				}
			}
			else {
				// looks like a user-private environment...
				if (userid == null) {
					LOG.warning(String.format(message, "UNKNOWN", "private", env.getEnvId()));
					return false;
				}

				if (!userid.equals(owner.getUsername())) {
					LOG.warning(String.format(message, userid, "private", env.getEnvId()));
					return false;
				}
			}
		}

		if (role == Role.ADMIN)
			return true;  // skip all other checks!

		// check node-level permissions first
		if (permissions.getGroup() != null) {
			if (wanted.getValue() <= permissions.getGroup().getValue())
				return true;
		}

		final var allowed = permissions.getUser();
		if (wanted.getValue() > allowed.getValue()) {
			final var reason = " Permissions mismatch: " + wanted.name() + " > " + allowed.name();
			LOG.warning(String.format(message + reason, userid, "private", env.getEnvId()));
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

	public Stream<EmilEnvironment> listPublicEnvironments(Filter filter) throws BWFLAException {
		final var options = new FetchOptionsV2()
				.setLocation(MetadataCollection.PUBLIC)
				.setFromTime(filter.from())
				.setUntilTime(filter.until())
				.setOffset(filter.offset())
				.setLimit(filter.limit());

		final var result = imagearchive.api()
				.v2()
				.metadata(MetaDataKindV2.ENVIRONMENTS)
				.fetch(ENVIRONMENT_MAPPER, options);

		return result.stream()
				.onClose(result::close);
	}

	public long countPublicEnvironments(Filter filter) throws BWFLAException {
		final var options = new CountOptionsV2()
				.setLocation(MetadataCollection.PUBLIC)
				.setFromTime(filter.from())
				.setUntilTime(filter.until());

		return imagearchive.api()
				.v2()
				.metadata(MetaDataKindV2.ENVIRONMENTS)
				.count(options);
	}

	private Stream<EmilEnvironment> loadEmilEnvironments(UserContext userctx) throws BWFLAException {
		final var result = imagearchive.api()
				.v2()
				.metadata(MetaDataKindV2.ENVIRONMENTS)
				.fetch(ENVIRONMENT_MAPPER);

		return result.stream()
				.onClose(result::close);
	}

	private Stream<EmilObjectEnvironment> loadEmilObjectEnvironments(UserContext userctx) throws BWFLAException {
		return this.loadEmilEnvironments(userctx)
				.filter((env) -> env instanceof EmilObjectEnvironment)
				.map((env) -> (EmilObjectEnvironment) env);
	}

	private void setPermissions(EmilEnvironment ee, UserContext userCtx)
	{
		if(userCtx.isAvailable())
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
			throw new RuntimeException(error);
		}

		db = dbConnector.getInstance(dbName);

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
			final var env = imagearchive.api()
					.v2()
					.metadata(MetaDataKindV2.ENVIRONMENTS)
					.fetch(envid, ENVIRONMENT_MAPPER);

			switch (env.getArchive()) {
				case MetadataCollection.PUBLIC:
				case MetadataCollection.REMOTE:
					return env;
				default:
					return null;
			}
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Fetching environment '" + envid + "' failed!", error);
			return null;
		}
	}

	public NetworkEnvironment getEmilNetworkEnvironmentById(String envid) throws BWFLAException {
		return imagearchive.api()
				.v2()
				.metadata(MetaDataKindV2.NETWORKS)
				.fetch(envid, NETWORK_MAPPER);
	}

	public void deleteEmilNetworkEnvironment(NetworkEnvironment env) throws BWFLAException {
		imagearchive.api()
				.v2()
				.metadata(MetaDataKindV2.NETWORKS)
				.delete(env.getEnvId());
	}

	@Deprecated
	public EmilEnvironment getEmilEnvironmentById(String envid)
	{
		return this.getEmilEnvironmentById(envid, this.getUserContext());
	}

	public EmilEnvironment getEmilEnvironmentById(String envid, UserContext userctx) {
		if (envid == null)
			return null;

		try {
			final var env = imagearchive.api()
					.v2()
					.metadata(MetaDataKindV2.ENVIRONMENTS)
					.fetch(envid, ENVIRONMENT_MAPPER);

			final var isPrivate = MetadataCollection.DEFAULT.equals(env.getArchive());
			if (!isPrivate || this.checkPermissions(env, EmilEnvironmentPermissions.Permissions.READ, userctx))
				return env;
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Fetching environment '" + envid + "' failed!", error);
		}

		return null;
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

	public Stream<EmilObjectEnvironment> getEmilObjectEnvironmentByObject(String objectId, UserContext userCtx) throws BWFLAException {
		if (objectId == null)
			return Stream.empty();

		return this.loadEmilObjectEnvironments(userCtx)
				.filter((env) -> env.getObjectId().equals(objectId) && isEnvironmentVisible(env)
						&& checkPermissions(env, EmilEnvironmentPermissions.Permissions.READ, userCtx));
	}

	synchronized public void replicate(EmilEnvironment env, String destArchive, UserContext userctx) throws JAXBException, BWFLAException {

		// FIXME: permissions are currently checked externally!

		if (destArchive.equals(MetadataCollection.DEFAULT) && destArchive.equals(env.getArchive())) {
			// change ownership from user-private to node-private
			final var owner = env.getOwner();
			if (owner != null)
				owner.setUsergroup(userctx.getTenantId());

			final var permissions = env.getPermissions();
			if (permissions != null)
				permissions.setGroup(EmilEnvironmentPermissions.Permissions.READ);
		}

		if(env.getArchive().equals(MetadataCollection.DEFAULT)) {
			imagearchive.api()
					.v2()
					.metadata(MetaDataKindV2.ENVIRONMENTS)
					.delete(env.getEnvId());

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
						imagearchive.api()
								.v2()
								.metadata(MetaDataKindV2.ENVIRONMENTS)
								.delete(p.getEnvId());
					}
					catch (Exception error) {
						LOG.log(Level.WARNING, "Deleting parent environment failed!", error);
					}

					p.setArchive(destArchive);
					save(p, false, userctx);
					lastPrivateChild = p.getEnvId();
					parent = p.getParentEnvId();
				}
				else // first parent in dest archive, connecting
				{
					if(!p.getBranches().contains(lastPrivateChild))
						p.addChildEnvId(lastPrivateChild);
					else
						LOG.severe("this a branch. we have now two independent heads.");
					save(p, false, userctx);
					parent  = null;
				}
			}
		}
		env.setArchive(destArchive);
		save(env, false, userctx);
	}

	public boolean existsEmilEnvironment(String envid) throws BWFLAException {
		return imagearchive.api()
				.v2()
				.metadata(MetaDataKindV2.ENVIRONMENTS)
				.exists(envid);
	}


	@Deprecated
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

		final var options = new ReplaceOptionsV2()
				.setLocation(env.getArchive());

		imagearchive.api()
				.v2()
				.metadata(MetaDataKindV2.ENVIRONMENTS)
				.replace(env.getEnvId(), env, ImageArchiveMappers.OBJECT_TO_JSON_TREE, options);
	}

	public void saveNetworkEnvironment(NetworkEnvironment env) throws BWFLAException {
		imagearchive.api()
				.v2()
				.metadata(MetaDataKindV2.NETWORKS)
				.replace(env.getEnvId(), env, ImageArchiveMappers.OBJECT_TO_JSON_TREE);
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

				imagearchive.api()
						.v2()
						.metadata(MetaDataKindV2.ENVIRONMENTS)
						.delete(envId);

				classification.cleanupClassificationData(envId);
			}
			else {
				// user wants to delete this environment, so we need to mark it as deleted
				// it should not be listed, but remain accessible
				// semantics for revert and fork need to be clarified
				env.setDeleted(true);
				save(env, false);
			}
		} else {
			sessions.delete((EmilSessionEnvironment)env);
		}
	}

	@Deprecated
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

	@Override
	public void register(@Observes MigrationRegistry migrations) throws Exception
	{
		migrations.register("import-local-emil-environments", (mc) -> this.importFromFolder("import"));
		migrations.register("import-legacy-emil-database-v1", this::importLegacyDatabaseV1);
		migrations.register("create-absent-emil-environments", this::createAbsentEmilEnvironments);
	}

	private void createAbsentEmilEnvironments(MigrationConfig mc) throws BWFLAException
	{
		final BiFunction<String, Environment, Integer> importer = (archive, env) -> {
			try {
				// LOG.warning("found env " + env.getId()	 + " in archive " + a);
				if(env.isDeleted())
					return 0;

				EmilEnvironment emilEnv = getEmilEnvironmentById(env.getId());
				if(emilEnv != null && (emilEnv.getArchive() == null || !emilEnv.getArchive().equals(archive)))
				{
					EmilEnvironmentPermissions permissions = new EmilEnvironmentPermissions();
					permissions.setUser(EmilEnvironmentPermissions.Permissions.READ);
					emilEnv.setPermissions(permissions);
					emilEnv.setArchive(archive);
					save(emilEnv, false);
					LOG.info("Updated emil-environment '" + env.getId() + "'");
				}

				if ((emilEnv == null && env instanceof ContainerConfiguration)) {
					EmilContainerEnvironment ee = new EmilContainerEnvironment();
					saveImport(env, ee);
					LOG.info("Created emil-environment for container '" + env.getId() + "'");
					return 1;
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
					LOG.info("Created emil-environment for machine '" + env.getId() + "'");
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

		int counter = 0;

		LOG.info("Creating absent emil-environments...");
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

		LOG.info("Created " + counter + " absent emil-environment(s)");
	}

	private void importFromFolder(String directory) throws BWFLAException {
		final var counter = ImportCounts.counter();
		final Consumer<EmilEnvironment> importer = (env) -> {
			try {
				MetaDataKindV2 kind = MetaDataKindV2.ENVIRONMENTS;
				if (env instanceof EmilSessionEnvironment)
					kind = MetaDataKindV2.SESSIONS;

				final var values = imagearchive.api()
						.v2()
						.metadata(kind);

				final var options = new ReplaceOptionsV2()
						.setLocation(env.getArchive());

				values.replace(env.getEnvId(), env, ImageArchiveMappers.OBJECT_TO_JSON_TREE, options);
				LOG.info("Imported environment '" + env.getEnvId() + "' (" + env.getArchive() + ")");
				counter.increment(ImportCounts.IMPORTED);
			}
			catch (Exception error) {
				LOG.log(Level.WARNING, "Importing environment '" + env.getEnvId() + "' failed!", error);
				counter.increment(ImportCounts.FAILED);
			}
		};

		LOG.info("Importing environments from directory '" + directory + "'...");
		importHelper.importFromFolder(directory)
				.forEach((colname, entries) -> entries.forEach(importer));

		final var message = "Imported " + counter.get(ImportCounts.IMPORTED)
				+ " environment(s), failed " + counter.get(ImportCounts.FAILED);

		LOG.info(message);
	}

	private void importLegacyDatabaseV1(MigrationConfig mc) throws Exception
	{
		final var counter = ImportCounts.counter();
		final var mapper = ImageArchiveMappers.OBJECT_TO_JSON_TREE;
		final Consumer<JaxbType> importer = (data) -> {
			try {
				MetaDataKindV2 kind = MetaDataKindV2.ENVIRONMENTS;
				if (data instanceof EmilSessionEnvironment)
					kind = MetaDataKindV2.SESSIONS;
				else if (data instanceof NetworkEnvironment)
					kind = MetaDataKindV2.NETWORKS;

				final var options = new ReplaceOptionsV2();
				final var values = imagearchive.api()
						.v2()
						.metadata(kind);

				JsonNode value = null;
				String envid = null;

				switch (kind) {
					case ENVIRONMENTS:
					case SESSIONS: {
						final var env = (EmilEnvironment) data;
						options.setLocation(env.getArchive());
						value = mapper.apply(env);
						envid = env.getEnvId();
						break;
					}

					case NETWORKS: {
						final var env = (NetworkEnvironment) data;
						value = mapper.apply(env);
						envid = env.getEnvId();
						break;
					}

					default:
						throw new IllegalStateException("Not supported metadata kind!");
				}

				values.replace(envid, value, options);
				LOG.info("Imported environment '" + envid + "' (" + kind.value() + ")");
				counter.increment(ImportCounts.IMPORTED);
			}
			catch (Exception error) {
				LOG.log(Level.WARNING, "Importing environment failed!", error);
				counter.increment(ImportCounts.FAILED);
			}
		};

		LOG.info("Importing environments from legacy database...");
		final var filter = new MongodbEaasConnector.FilterBuilder();
		for (var collection : db.getCollections()) {
			final var values = db.find(collection, filter, "type");
			try (values) {
				values.forEach(importer);
				db.drop(collection);
			}
		}

		final var numImported = counter.get(ImportCounts.IMPORTED);
		final var numFailed = counter.get(ImportCounts.FAILED);
		LOG.info("Imported " + numImported + " environment(s), failed " + numFailed);
		if (!MigrationUtils.acceptable(numImported + numFailed, numFailed, MigrationUtils.getFailureRate(mc)))
			throw new BWFLAException("Importing environments from legacy database failed!");
	}

	public void export()
	{
		final var kinds = new MetaDataKindV2[] {
				MetaDataKindV2.ENVIRONMENTS,
				MetaDataKindV2.SESSIONS
		};

		try {
			final var emilDataExport = new EmilDataExport();
			final Consumer<EmilEnvironment> exporter = (env) -> {
				try {
					emilDataExport.saveEnvToPath(env.getArchive(), env);
				}
				catch (Exception error) {
					LOG.log(Level.WARNING, "Exporting environment failed!", error);
				}
			};

			for (var kind : kinds) {
				final var values = imagearchive.api()
						.v2()
						.metadata(kind)
						.fetch(ENVIRONMENT_MAPPER);

				try (values) {
					values.stream()
							.forEach(exporter);
				}
			}
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Exporting environments failed!", error);
		}
	}

	private void saveImport(Environment env, EmilEnvironment ee) throws JAXBException, BWFLAException {
		ee.setEnvId(env.getId());
		ee.setTitle(env.getDescription().getTitle());
		ee.setAuthor("n.a.");
		if (env instanceof ContainerConfiguration) {
			((EmilContainerEnvironment) ee).setInput(((ContainerConfiguration) env).getInput());
			((EmilContainerEnvironment) ee).setOutput(((ContainerConfiguration) env).getOutputPath());
			((EmilContainerEnvironment) ee).setDigest(((ContainerConfiguration) env).getDigest());

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


	public Stream<EmilEnvironment> getEmilEnvironments(UserContext userCtx) throws BWFLAException
	{
		final Stream<EmilEnvironment> all = loadEmilEnvironments(userCtx);
		final HashSet<String> known = new HashSet<>();

		return all.filter(this::isEnvironmentVisible)
				.filter(e-> !e.isDeleted())
				.filter(e -> (checkPermissions(e, EmilEnvironmentPermissions.Permissions.READ, userCtx)))
				.filter(e -> {
					if (known.contains(e.getEnvId()))
						return false;
					return known.add(e.getEnvId());
				});
	}

	public Stream<NetworkEnvironment> getNetworkEnvironments() throws BWFLAException {
		final var result = imagearchive.api()
				.v2()
				.metadata(MetaDataKindV2.NETWORKS)
				.fetch(NETWORK_MAPPER);

		return result.stream()
				.onClose(result::close);
	}

	@Deprecated
	public Stream<EmilEnvironment> getEmilEnvironments() throws BWFLAException {
		return this.getEmilEnvironments(this.getUserContext());
	}

	@Deprecated
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
		env.setDigest(req.getContainerDigest());

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
		if(req.getArchive() != null)
			env.setArchive(req.getArchive());
		save(env, true, userCtx);
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


	public static class Filter
	{
		private int offset;
		private int limit;
		private long fromtime;
		private long untiltime;

		public Filter setOffset(int offset)
		{
			this.offset = offset;
			return this;
		}

		public int offset()
		{
			return offset;
		}

		public Filter setLimit(int limit)
		{
			this.limit = limit;
			return this;
		}

		public int limit()
		{
			return limit;
		}

		public Filter setFromTime(long timestamp)
		{
			this.fromtime = timestamp;
			return this;
		}

		public long from()
		{
			return fromtime;
		}

		public Filter setUntilTime(long timestamp)
		{
			this.untiltime = timestamp;
			return this;
		}

		public long until()
		{
			return untiltime;
		}
	}
}
