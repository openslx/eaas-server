package de.bwl.bwfla.imagebuilder;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


class DockerTools {

    private final Logger log;
    private ImageContentDescription.DockerDataSource ds;

    DockerTools(Path workdir, ImageContentDescription.DockerDataSource dockerDataSource, Logger log) throws BWFLAException {
        this.log = log;
        this.ds = dockerDataSource;

        if (ds.imageRef == null)
            throw new BWFLAException("Docker image ref and/or tag must not be null");
    }


    public void pull(Path destDir) throws BWFLAException {
        
        if(ds.imageRef == null)
            throw new BWFLAException("image ref is not set");

        if(ds.digest == null || ds.digest.isEmpty())
            ds.digest = getDigest();

        if(ds.digest.isEmpty())
            throw new BWFLAException("could not determine digest");

        log.info("Copying digest " + ds.digest + "...");

        DeprecatedProcessRunner runner_ = new DeprecatedProcessRunner();
        runner_.setLogger(log);
        runner_.setCommand("/bin/sh");
        runner_.setWorkingDirectory(destDir);
        runner_.addArgument("-c");
        runner_.addArgument("crane export \"$1\" - | tar x");
        runner_.addArgument("sh");
        runner_.addArgument(ds.imageRef + "@" + ds.digest);
        if(!runner_.execute(true))
            throw new BWFLAException("crane export failed.");

        String config = getConfig();
        
        ds.emulatorType = jq(config, ".config.Labels.EAAS_EMULATOR_TYPE");
        log.info("Emulator's type: " + ds.emulatorType);

        ds.version = jq(config, ".config.Labels.EAAS_EMULATOR_VERSION");
        log.info("Emulator's version: " + ds.version);

        String md = jq(config, "{Cmd: .config.Cmd, Env: .config.Env, WorkingDir: .config.WorkingDir}");
        if(md == null)
        {
            log.warning("Docker metadata seems to be incomplete. Could not determine, CMD, ENV or WORKINGDIR. \n" + config);
            return;
        }

        final JsonReader reader = Json.createReader(new StringReader(md));

        final JsonObject json = reader.readObject();
        final ArrayList<String> envvars = new ArrayList<>();

        try {
            JsonArray envArray = json.getJsonArray("Env");
            for (int i = 0; i < envArray.size(); i++)
                envvars.add(envArray.getString(i));
        }
        catch(ClassCastException e)
        {
            log.warning("importing ENV failed");
            log.warning("Metadata object " + json.toString());
        }

        final ArrayList<String> cmds = new ArrayList<>();
        try {
            JsonArray cmdJson = json.getJsonArray("Cmd");
            for (int i = 0; i < cmdJson.size(); i++)
                cmds.add(cmdJson.getString(i));
        }
        catch (ClassCastException e)
        {
            log.warning("importing CMD failed");
            log.warning("Metadata object " + json.toString());
        }

        try {
            JsonString workDirObject = json.getJsonString("WorkingDir");
            if (workDirObject != null) {
                ds.workingDir = workDirObject.getString();
            }
        }
        catch(ClassCastException e)
        {
            log.warning("importing WorkingDir failed");
            log.warning("Metadata object " + json.toString());
        }
        ds.entryProcesses = cmds;
        ds.envVariables = envvars;

    }

    private String getConfig() throws BWFLAException
    {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand("crane");
        runner.addArguments("config", ds.imageRef + "@" + ds.digest);
        runner.setLogger(log);

        try {
            final DeprecatedProcessRunner.Result result = runner.executeWithResult(false)
                    .orElse(null);

            if (result == null || !result.successful())
                throw new BWFLAException("Running crane failed!");

            return result.stdout()
                    .trim();
        }
        catch (IOException error) {
            throw new BWFLAException("Fetching docker config failed", error);
        }
    }

    private String jq(String config, String query) throws BWFLAException
    {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand("/bin/sh");
        runner.addEnvVariable("config", config);
        runner.addEnvVariable("query", query);
        runner.addArguments("-c", "printf %s \"$config\" | jq -r \"$query\"");
        runner.setLogger(log);
        try {
            final DeprecatedProcessRunner.Result result = runner.executeWithResult(true)
                    .orElse(null);

            if (result == null || !result.successful())
                return null;

            return result.stdout()
                    .trim();
        }
        catch (IOException error) {
            throw new BWFLAException("Parsing docker config failed!", error);
        }
    }


    private String getDigest() throws BWFLAException {

        if(ds.tag == null)
            ds.tag = "latest";

        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setLogger(log);
        runner.setCommand("crane");
        runner.addArgument("digest");
        runner.addArgument(ds.imageRef + ":" + ds.tag);
          try {
            final DeprecatedProcessRunner.Result result = runner.executeWithResult(true)
                    .orElse(null);
            if (result == null || !result.successful())
                throw new BWFLAException("Running crane failed!");

            return result.stdout()
                    .trim();
        }
        catch (IOException error) {
            throw new BWFLAException("Parsing docker digest failed!", error);
        }
    }
}
