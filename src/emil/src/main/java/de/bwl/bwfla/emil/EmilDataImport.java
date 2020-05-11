package de.bwl.bwfla.emil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.EmilContainerEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilSessionEnvironment;
import org.apache.commons.io.FileUtils;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

@ApplicationScoped
public class EmilDataImport {

    private static final Gson GSON = new GsonBuilder().create();

    protected final static Logger LOG = Logger.getLogger(EmilDataImport.class.getName());

    @Inject
    @Config(value = "commonconf.serverdatadir")
    private String serverdatadir;

    @Inject
    @Config(value = "emil.emilobjectenvironmentspaths")
    protected String emilObjectEnvironmentsPath;

    @Inject
    @Config(value = "emil.emilenvironmentspath")
    protected String emilEnvironmentsPath;

    private Path emilEnvPath;
    private Path objEnvPath;
    private Path sessionEnvPath;
    private Path containersEnvPath;

    @PostConstruct
    public void init()
    {
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
            this.containersEnvPath = exportDir.resolve("emil-container-environments");
            if (!Files.exists(this.containersEnvPath))
                Files.createDirectory(this.containersEnvPath);
        } catch (IOException e) {
            LOG.warning("creation of emil dirs failed! \n" + e.getMessage());
        }
    }

    private <T extends JaxbType> T getEmilEnvironmentByPath(Path envpath, final Class<T> klass) throws IOException, JsonSyntaxException, JAXBException, BWFLAException {
        if (!Files.exists(envpath))
            throw new IOException("file not found");

        return JaxbType.fromJsonValueWithoutRoot(FileUtils.readFileToString(envpath.toFile(), StandardCharsets.UTF_8), klass);

        // try (Reader reader = Files.newBufferedReader(envpath, StandardCharsets.UTF_8)) {
        //    return GSON.fromJson(reader, klass);
        // }
    }

    public HashMap<String, List<EmilEnvironment>> importFromFolder(String directory)
    {
        HashMap<String, List<EmilEnvironment>> result = new HashMap<>();
        Path importPath = Paths.get(serverdatadir).resolve(directory);
        if(!Files.exists(importPath)) {
            LOG.severe("import path not found: " + importPath);
            return result;
        }

        try {
            Path emilEnvs = importPath.resolve("emil-environments");
            if(Files.exists(emilEnvs))
            {
                DirectoryStream<Path> collectionStream  = Files.newDirectoryStream(emilEnvs);
                for (Path collectionPath: collectionStream) {
                    String collection = collectionPath.getFileName().toString();
                    if(collection.startsWith("."))
                        continue;
                    List<EmilEnvironment> envs = importEnvByPath(EmilEnvironment.class, collectionPath);
                    result.put(collection, envs);
                }
            }

            Path emilObjEnvs = importPath.resolve("emil-object-environments");
            if (Files.exists(emilObjEnvs))
            {
                DirectoryStream<Path> collectionStream  = Files.newDirectoryStream(emilObjEnvs);
                for (Path collectionPath: collectionStream) {
                    String collection = collectionPath.toString();
                    if(collection.startsWith("."))
                        continue;

                    List<EmilObjectEnvironment> envs = importEnvByPath(EmilObjectEnvironment.class, collectionPath);
                    List<EmilEnvironment> _envs = result.get(collection);
                    if (_envs == null)
                        _envs = new ArrayList<>();
                    _envs.addAll(envs);
                    result.put(collection, _envs);
                }
            }
            // FileUtils.deleteDirectory(importPath.toFile());
        }
        catch (IOException e)
        {
            return result;
        }
        return result;
    }


    public synchronized List<EmilEnvironment> importExistentEnv(MongodbEaasConnector.DatabaseInstance db,
                                                                String emilDbCollectionName) throws IOException, BWFLAException {

//        Path emilEnvPath = Paths.get(serverdatadir).resolve("emil-environments");
//        Path objEnvPath = Paths.get(serverdatadir).resolve("emil-object-environments");
//        Path sessionEnvPath = Paths.get(serverdatadir).resolve("emil-session-environments");
//        Path containerEnvs = Paths.get(serverdatadir).resolve("emil-container-environments");
//
//        // ensure the absence of null elements
//
//        Optional.ofNullable(importEnvByPath(EmilObjectEnvironment.class, Paths.get(emilObjectEnvironmentsPath), objEnvPath));
//        Optional.ofNullable(importEnvByPath(EmilSessionEnvironment.class, Paths.get(emilEnvironmentsPath), sessionEnvPath));
//        Optional.ofNullable(importEnvByPath(EmilContainerEnvironment.class, Paths.get(emilEnvironmentsPath), containerEnvs));

        List<EmilEnvironment> envs = new ArrayList<>();

        //check old database style for existent environments
        try {
            envs.addAll(db.getJaxbObjects(emilDbCollectionName, "emilEnvironment", EmilEnvironment.class));
            envs.addAll(db.getJaxbObjects(emilDbCollectionName, "emilContainerEnvironment", EmilContainerEnvironment.class));
            envs.addAll(db.getJaxbObjects(emilDbCollectionName, "emilObjectEnvironment", EmilObjectEnvironment.class));
            envs.addAll(db.getJaxbObjects(emilDbCollectionName, "emilSessionEnvironment", EmilSessionEnvironment.class));
        } catch (JAXBException e) {
            throw new BWFLAException(e);
        }

        return envs;
    }

    private <T extends EmilEnvironment> List<T> importEnvByPath(final Class<T> klass, Path... paths) throws IOException {
        final List<T> environments = new ArrayList<>();
        for (Path path : paths) {
            LOG.warning("import env by path " + path.getFileName());
            if(path.getFileName().startsWith("."))
                continue;

            if (!Files.exists(path)) {
                continue;
            }

            try (DirectoryStream<Path> files = Files.newDirectoryStream(path)) {
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
                            LOG.severe(env.toString());
                            environments.add(env);
                        }
                    } catch (Exception e) {
                        LOG.warning("import might be broken! \n " + e.getMessage());
                    }
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
}
