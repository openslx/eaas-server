package de.bwl.bwfla.emil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.bwl.bwfla.emil.datatypes.EmilContainerEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilSessionEnvironment;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@ApplicationScoped
public class EmilDataExport {

    protected final static Logger LOG = Logger.getLogger(EmilDataExport.class.getName());
    @Inject
    @Config(value = "commonconf.serverdatadir")
    private String serverdatadir;

    private Path emilEnvPath;
    private Path objEnvPath;
    private Path sessionEnvPath;
    private Path containersEnvPath;

    private static final Gson GSON = new GsonBuilder().create();

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

    public void saveEnvToPath(EmilEnvironment env) throws IOException {
        String json;
        Path envpath;
        if (env instanceof EmilSessionEnvironment) {
            envpath = sessionEnvPath.resolve(env.getEnvId());
            json = GSON.toJson((EmilSessionEnvironment) env);
        } else if (env instanceof EmilObjectEnvironment) {
            envpath = objEnvPath.resolve(env.getEnvId());
            json = GSON.toJson((EmilObjectEnvironment) env);
        } else if (env instanceof EmilContainerEnvironment) {
            envpath = containersEnvPath.resolve(env.getEnvId());
            json = GSON.toJson((EmilContainerEnvironment) env);
        } else {
            envpath = emilEnvPath.resolve(env.getEnvId());
            json = GSON.toJson(env);
        }
        Files.write(envpath, json.getBytes());
    }
}
