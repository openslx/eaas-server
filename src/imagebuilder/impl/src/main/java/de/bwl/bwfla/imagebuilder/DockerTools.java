package de.bwl.bwfla.imagebuilder;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;

import javax.ws.rs.PathParam;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class DockerTools {

    private Path workdir;
    private ImageContentDescription.DockerDataSource ds;

    DockerTools(Path workdir, ImageContentDescription.DockerDataSource dockerDataSource) throws BWFLAException {
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

        System.out.println("got digest " + ds.digest);

        DeprecatedProcessRunner skopeoRunner = new DeprecatedProcessRunner();
        skopeoRunner.setCommand("skopeo");
        skopeoRunner.addArgument("--insecure-policy");
        skopeoRunner.addArgument("copy");
        skopeoRunner.addArgument( ds.imageRef + "@" + ds.digest);
        skopeoRunner.addArgument("oci:" + ds.dockerDir);
        if (!skopeoRunner.execute()) {
            try {
                Files.deleteIfExists(ds.dockerDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ds.dockerDir = null;
            throw new BWFLAException("Skopeo failed to fetch image from DockerHub");
        }

        ds.emulatorType = getLabel(".Labels.EAAS_EMULATOR_TYPE");
        System.out.println("get label emulator -" + ds.emulatorType + "-");

        ds.version = getLabel(".Labels.EAAS_EMULATOR_VERSION");
        System.out.println("get label version -" + ds.version +"-");
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
        if (!ociRunner.execute()) {
            try {
                Files.deleteIfExists(ds.rootfs);
            } catch (IOException e) {
                e.printStackTrace();
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
        if (!runner.execute(false, false))
            throw new BWFLAException("getting label failed");

        String result = null;
        try {
            result = runner.getStdOutString();
        } catch (IOException e)
        {
            throw new BWFLAException(e);
        }
        runner.cleanup();
        return result.trim();
    }


    private String getDigest() throws BWFLAException {

        if(ds.tag == null)
            ds.tag = "latest";

        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand("/bin/bash");
        runner.addArguments("-c", "skopeo inspect " + ds.imageRef + ":" + ds.tag + "| jq -r .Digest");
        if (!runner.execute(false, false))
            throw new BWFLAException("getting docker digest failed");

        String digest = null;
        try {
            digest = runner.getStdOutString();
        } catch (IOException e)
        {
            throw new BWFLAException(e);
        }
        runner.cleanup();
        return digest.trim();
    }

    private String[] getLayer() throws  BWFLAException {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand("oci-layer-list.sh");
        runner.addArgument(ds.dockerDir.toString());

        if (!runner.execute(false, false))
            throw new BWFLAException("oci-layer-list.sh failed");

        String input = null;
        try {
            input = runner.getStdOutString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new BWFLAException(e);
        }
        String[] result = input.split("\\r?\\n");
        runner.cleanup();
        return result;
    }
}
