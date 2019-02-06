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

    DockerTools(Path workdir, ImageContentDescription.DockerDataSource dockerDataSource) throws BWFLAException
    {
        this.workdir = workdir;
        this.ds = dockerDataSource;

        if(ds.imageRef == null || ds.tag == null)
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

        DeprecatedProcessRunner skopeoRunner = new DeprecatedProcessRunner();
        skopeoRunner.setCommand("skopeo");
        skopeoRunner.addArgument("--insecure-policy");
        skopeoRunner.addArgument("copy");
        skopeoRunner.addArgument("docker://" + ds.imageRef + ":" + ds.tag);
        skopeoRunner.addArgument("oci:" + ds.dockerDir + ":" + ds.tag);
        if (!skopeoRunner.execute()) {
            try {
                Files.deleteIfExists(ds.dockerDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ds.dockerDir = null;
            throw new BWFLAException("Skopeo failed to fetch image from DockerHub");
        }
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
        ociRunner.addArguments("--ref", "name=" + ds.tag);
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


    private String[] getLayer() throws  BWFLAException {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand("oci-layer-list.sh");
        runner.addArgument(ds.dockerDir.toString());
        runner.addArgument(ds.tag);

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
