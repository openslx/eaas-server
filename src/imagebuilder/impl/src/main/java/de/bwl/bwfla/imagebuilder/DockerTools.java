package de.bwl.bwfla.imagebuilder;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;


class DockerTools {

    private final Logger log;
    private Path workdir;
    private ImageContentDescription.DockerDataSource ds;

    DockerTools(Path workdir, ImageContentDescription.DockerDataSource dockerDataSource, Logger log) throws BWFLAException {
        this.log = log;
        this.workdir = workdir;
        this.ds = dockerDataSource;

        if (ds.imageRef == null)
            throw new BWFLAException("Docker image ref and/or tag must not be null");
    }

    public void pull() throws BWFLAException {
        ds.dockerDir = workdir.resolve("image");
        try {
            Files.createDirectories(ds.dockerDir);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BWFLAException(e);
        }

        if(ds.imageRef == null)
            throw new BWFLAException("image ref is not set");

        if(!ds.imageRef.startsWith("docker://")) // assume docker-style registry for now
            ds.imageRef = "docker://" + ds.imageRef;

        if(ds.digest == null || ds.digest.isEmpty())
            ds.digest = getDigest();

        if(ds.digest == null)
            throw new BWFLAException("could not determine digest");

        log.info("Copying digest " + ds.digest + "...");

        DeprecatedProcessRunner skopeoRunner = new DeprecatedProcessRunner();
        skopeoRunner.setCommand("skopeo");
        skopeoRunner.addArgument("--insecure-policy");
        skopeoRunner.addArgument("copy");
        skopeoRunner.addArgument( ds.imageRef + "@" + ds.digest);
        skopeoRunner.addArgument("oci:" + ds.dockerDir);
        skopeoRunner.setLogger(log);
        if (!skopeoRunner.execute()) {
            try {
                Files.deleteIfExists(ds.dockerDir);
            } catch (IOException e) {
                log.log(Level.WARNING, "Running skopeo failed!", e);
            }
            ds.dockerDir = null;
            throw new BWFLAException("Skopeo failed to fetch image from DockerHub");
        }

        ds.emulatorType = getLabel(".Labels.EAAS_EMULATOR_TYPE");
        log.info("Emulator's type: " + ds.emulatorType);

        ds.version = getLabel(".Labels.EAAS_EMULATOR_VERSION");
        log.info("Emulator's version: " + ds.version);
    }

    public void unpack() throws BWFLAException {
        if (ds.dockerDir == null || !Files.exists(ds.dockerDir))
            throw new BWFLAException("Cannot unpack docker! pull() first");

        try {
            ds.rootfs = Files.createDirectory(workdir.resolve("rootfs"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new BWFLAException(e);
        }

        DeprecatedProcessRunner ociRunner = new DeprecatedProcessRunner();
        ociRunner.setCommand("oci-image-tool");
        ociRunner.addArgument("unpack");
        ociRunner.addArguments("--ref", "name=");
        ociRunner.addArgument(ds.dockerDir.toString());
        ociRunner.addArgument(ds.rootfs.toString());
        ociRunner.setLogger(log);
        if (!ociRunner.execute()) {
            try {
                Files.deleteIfExists(ds.rootfs);
            } catch (IOException e) {
                log.log(Level.WARNING, "Running oci-image-tool failed!", e);
            }
            ds.rootfs = null;
            throw new BWFLAException("Skopeo failed to unpack image from DockerHub");
        }
        ds.layers = getLayer();
    }

    private String getLabel(String l) throws BWFLAException
    {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand("/bin/bash");
        runner.addArguments("-c", "skopeo inspect oci://" + ds.dockerDir.toString() + "| jq -r " + l);
        runner.setLogger(log);
        if (!runner.execute(false, false))
            throw new BWFLAException("getting label failed");

        String result = null;
        try {
            result = runner.getStdOutString();
        }
        catch (IOException e) {
            throw new BWFLAException(e);
        }
        finally {
            runner.cleanup();
        }

        return result.trim();
    }


    private String getDigest() throws BWFLAException {

        if(ds.tag == null)
            ds.tag = "latest";

        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand("/bin/bash");
        runner.addArguments("-c", "skopeo inspect " + ds.imageRef + ":" + ds.tag + "| jq -r .Digest");
        runner.setLogger(log);
        if (!runner.execute(false, false))
            throw new BWFLAException("getting docker digest failed");

        String digest = null;
        try {
            digest = runner.getStdOutString();
        }
        catch (IOException e) {
            throw new BWFLAException(e);
        }
        finally {
            runner.cleanup();
        }

        return digest.trim();
    }

    private String[] getLayer() throws  BWFLAException {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand("oci-layer-list.sh");
        runner.addArgument(ds.dockerDir.toString());
        runner.setLogger(log);

        if (!runner.execute(false, false))
            throw new BWFLAException("oci-layer-list.sh failed");

        String input = null;
        try {
            input = runner.getStdOutString();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new BWFLAException(e);
        }
        finally {
            runner.cleanup();
        }

        return input.split("\\r?\\n");
    }
}
