package de.bwl.bwfla.emil;

import de.bwl.bwfla.emil.datatypes.EmilContainerEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilSessionEnvironment;
import org.apache.tamaya.ConfigurationProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.logging.Logger;

public class EmilDataExport {

    protected final static Logger LOG = Logger.getLogger(EmilDataExport.class.getName());
    private Path emilEnvPath;
    private Path objEnvPath;
    private Path sessionEnvPath;
    private Path containersEnvPath;


    public EmilDataExport() throws IOException {
        String serverdatadir = ConfigurationProvider.getConfiguration().get("commonconf.serverdatadir");
        Path exportDir = Paths.get(serverdatadir).resolve("export-" + Instant.now().toString());

        if (!Files.exists(exportDir))
            Files.createDirectory(exportDir);
        emilEnvPath = exportDir.resolve("emil-environments");
        if (!Files.exists(emilEnvPath))
            Files.createDirectory(emilEnvPath);
        objEnvPath = exportDir.resolve("emil-object-environments");
        if (!Files.exists(objEnvPath))
            Files.createDirectory(objEnvPath);
        sessionEnvPath = exportDir.resolve("emil-session-environments");
        if (!Files.exists(sessionEnvPath))
            Files.createDirectory(sessionEnvPath);
        containersEnvPath = exportDir.resolve("emil-container-environments");
        if (!Files.exists(containersEnvPath))
            Files.createDirectory(containersEnvPath);
    }

    public void saveEnvToPath(String collection, EmilEnvironment env) throws IOException {
        Path base;

        if(env instanceof EmilSessionEnvironment)
            base = sessionEnvPath;
        else if(env instanceof EmilContainerEnvironment) {
            base = containersEnvPath;
        }
        else if(env instanceof EmilObjectEnvironment)
            base = objEnvPath;
        else
            base = emilEnvPath;

        Path outPath = base.resolve(collection);
        if(!Files.exists(outPath))
            Files.createDirectory(outPath);

        Path outfile = outPath.resolve(env.getEnvId());
        Files.write(outfile, env.jsonValueWithoutRoot(true).getBytes());
    }
}
