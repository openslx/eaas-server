package de.bwl.bwfla.emil;
import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import org.apache.tamaya.inject.api.Config;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;


@ApplicationScoped
public class DatabaseEnvironmentsAdapter {

    protected static final Logger LOG = Logger.getLogger(" de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter");
    EnvironmentsAdapter environmentsAdapter;

    private String metaDataIdKey = "id";
    private String classNameDBKey = "configurationType";

    @Inject
    @Config(value = "ws.imagearchive")
    private String imageArchive;

    @Inject
    private MongodbEaasConnector dbConnector;
    private MongodbEaasConnector.DatabaseInstance db;

    @Inject
    @Config("emil.imagesDatabase")
    private String dbName;

    @PostConstruct
    public void init() {
        environmentsAdapter = new EnvironmentsAdapter(imageArchive);
        db = dbConnector.getInstance(dbName);
        db.ensureTimestamp("public");
        sync();
    }

    @Override
    public String toString(){
    return environmentsAdapter.toString();
    }

    public ImageArchiveBinding generalizedImport(String archive, String id, ImageType imageType, String patchId) throws BWFLAException {
        return environmentsAdapter.generalizedImport(archive, id, imageType, patchId);
    }

    public MachineConfigurationTemplate getTemplate(String id) throws BWFLAException {
        return environmentsAdapter.getTemplate(id);
    }

    public String createEnvironment(String archive, MachineConfiguration env, String size, ImageArchiveMetadata iaMd) throws BWFLAException {
        return environmentsAdapter.createEnvironment(archive, env, size, iaMd);
    }

    public EnvironmentsAdapter.ImportImageHandle importImage(String archive, URL url, ImageArchiveMetadata iaMd, boolean b) throws BWFLAException {
        return environmentsAdapter.importImage(archive, url, iaMd, b);
    }

    public EnvironmentsAdapter.ImportImageHandle importImage(String archive, DataHandler handler, ImageArchiveMetadata iaMd) throws BWFLAException {
        return environmentsAdapter.importImage(archive, handler, iaMd);
    }

    public String getDefaultEnvironment(String osId) throws BWFLAException {
        return environmentsAdapter.getDefaultEnvironment(osId);
    }

    public void setDefaultEnvironment(String osId, String envId) throws BWFLAException {
        environmentsAdapter.setDefaultEnvironment(osId, envId);
    }

    public String importMachineEnvironment(String archive, MachineConfiguration env, List<BindingDataHandler> data, ImageArchiveMetadata iaMd) throws BWFLAException {
        String id = environmentsAdapter.importMachineEnvironment(archive, env, data, iaMd);
        Environment environment = environmentsAdapter.getEnvironmentById(archive, id);
        db.saveDoc(archive, id, metaDataIdKey, environment.jsonValueWithoutRoot(false));
        return id;
    }

    public TaskState createImageAsync(String backend, String size, ImageType type, ImageMetadata md) throws BWFLAException {
        return environmentsAdapter.createImageAsync(backend, size, type, md);
    }

    public void sync() {
        try {
            environmentsAdapter.sync();
            for (String archive : listBackendNames()) {
                LOG.warning("updating archive: " + archive);
                try {
                    updateDatabase(archive);
                } catch (BWFLAException | JAXBException e) {
                    e.printStackTrace();
                }
            }
        } catch (BWFLAException e) {
            e.printStackTrace();
        }
    }

    private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

    public ImageNameIndex getNameIndexes() throws BWFLAException {
        return environmentsAdapter.getNameIndexes(EMULATOR_DEFAULT_ARCHIVE);
    }

    public List<Environment> getEnvironments(String archive) throws BWFLAException, JAXBException {
        List<Environment> environments = db.getObjectsWithClassFromDatabaseKey(archive, classNameDBKey);
        if (environments.size() > 0)
            return environments;
        else {
            return updateDatabase(archive);
        }
    }

    public TaskState getState(String id) throws BWFLAException {
        return environmentsAdapter.getTaskState(id);
    }

    public TaskState importImage(URL ref, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException {
        return environmentsAdapter.importImageAsync(ref, iaMd, deleteIfExists);
    }

    public List<MachineConfigurationTemplate> getTemplates() throws BWFLAException, JAXBException {
      return environmentsAdapter.getTemplates();
    }

    public List<GeneralizationPatch> getPatches() throws BWFLAException, JAXBException {
      return environmentsAdapter.getPatches();
    }

    public Environment getEnvironmentById(String id) throws BWFLAException {
        return getEnvironmentById(environmentsAdapter.getDefaultBackendName(), id);
    }

    public Environment getEnvironmentById(String archive, String id) throws BWFLAException {
        try {
            Environment environment = db.getObjectWithClassFromDatabaseKey(archive, classNameDBKey, id, metaDataIdKey);
            if (environment == null)
                throw new NoSuchElementException();
            else
                return environment;

        } catch (NoSuchElementException | BWFLAException e) {
            Environment environment = environmentsAdapter.getEnvironmentById(environmentsAdapter.getDefaultBackendName(), id);
            db.saveDoc(archive, environment.getId(), metaDataIdKey, environment.jsonValueWithoutRoot(false));
            return environment;
        }
    }

    public void delete(String archive, String envId, boolean deleteMetadata, boolean deleteImage) throws BWFLAException {
//      it doesn't matter, whether we preserve metaData or not.
//      In fact, it's internal archive option and Emil should not be aware of this environment anymore.
        db.deleteDoc(archive, envId, metaDataIdKey);
        environmentsAdapter.delete(archive, envId, deleteMetadata, deleteImage);
    }


    public void commitTempEnvironment(String archive, String id) throws BWFLAException {
        commitTempEnvironmentWithCustomType(archive, id, "user");
    }


    public void commitTempEnvironmentWithCustomType(String archive, String id, String type) throws BWFLAException {
        try {
            environmentsAdapter.commitTempEnvironmentWithCustomType(id, type);
        } catch (Exception e) {
            throw new BWFLAException(e);
        }

        Environment environment = environmentsAdapter.getEnvironmentById(archive, id);
        db.saveDoc(archive, id, metaDataIdKey, environment.jsonValueWithoutRoot(false));
    }


    public void updateMetadata(String archive, Environment environment) throws BWFLAException {
        environment.setTimestamp(Instant.now().toString());
        db.saveDoc(archive, environment.getId(), metaDataIdKey, environment.jsonValueWithoutRoot(false));
        environmentsAdapter.updateMetadata(archive, environment.toString());
    }


    public void addNameIndexesEntry(String backend, ImageMetadata entry, Alias alias) throws BWFLAException {
        environmentsAdapter.addNameIndexesEntry(backend, entry, alias);
    }

    public void deleteNameIndexesEntry(String backend, String id, String version) throws BWFLAException {
        environmentsAdapter.deleteNameIndexesEntry(backend, id, version);
    }

    public void updateLatestEmulator(String backend, String emulator, String version) throws BWFLAException {
        environmentsAdapter.updateLatestEmulator(backend, emulator, version);
    }


    public String importMetadata(String archive, Environment environment, ImageArchiveMetadata iaMd, boolean preserveId) throws BWFLAException {
        String id = environmentsAdapter.importMetadata(archive, environment.toString(), iaMd, preserveId);
        if (iaMd.getType() != ImageType.TMP && iaMd.getType() != ImageType.TEMPLATE) {
            Environment newEnv = environmentsAdapter.getEnvironmentById(archive, id);
            db.saveDoc(archive, newEnv.getId(), metaDataIdKey, newEnv.jsonValueWithoutRoot(false));
            return newEnv.getId();
        }
        return id;
    }

    public List<EnvironmentsAdapter.ImportImageHandle> replicateImages(String archive, List<String> images) throws BWFLAException {
        return environmentsAdapter.replicateImages(archive, images);
    }

    private List<Environment> updateDatabase(String archive) throws BWFLAException, JAXBException {
        List<Environment> environments = environmentsAdapter.getEnvironments(archive, null);
        db.drop(archive);
        environments.forEach(env -> {
            try {
                if (!env.getConfigurationType().equals(MachineConfigurationTemplate.class))
                    db.saveDoc(archive, env.getId(), metaDataIdKey, env.jsonValueWithoutRoot(false));
            } catch (BWFLAException e) {
                e.printStackTrace();
            }
        });

        return environments;
    }


    public ImageNameIndex getImagesIndex() throws BWFLAException
    {
        return environmentsAdapter.getNameIndexes();
    }

    public ImageNameIndex getImagesIndex(String archive) throws BWFLAException {
        return environmentsAdapter.getNameIndexes(archive);
    }

    public Collection<String> listBackendNames() throws BWFLAException {
        return environmentsAdapter.listBackendNames();
    }


    public Stream<Environment> listEnvironments(String archive, int offset, int maxcount, MongodbEaasConnector.FilterBuilder filter)
    {
        return db.find(archive, offset, maxcount, filter, classNameDBKey);
    }

    public long countEnvironments(String archive)
    {
        return db.count(archive);
    }

    public List<Environment> getBaseEnvironments(String a) throws BWFLAException, JAXBException {
        return environmentsAdapter.getEnvironments(a, "base");
    }

    public String getImageArchiveHost()
    {
        return imageArchive;
    }

    public void extractMetadata(String imageId) throws BWFLAException {
        environmentsAdapter.extractMetadata(imageId);
    }

    public List<DefaultEntry> getDefaultEnvironments() throws BWFLAException {
        return environmentsAdapter.getDefaultEnvironments("default");
    }

    public void deleteImage(String backend, String id, ImageType type) throws BWFLAException {
        environmentsAdapter.deleteImage(backend, id, type.value());
    }
}
