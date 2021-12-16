package de.bwl.bwfla.emil.tasks;

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import de.bwl.bwfla.api.imagearchive.ImageMetadata;
import de.bwl.bwfla.api.imagebuilder.DockerImport;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderResult;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.rest.ComponentResponse;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageRequest;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageResult;
import de.bwl.bwfla.emil.utils.BuildContainerUtil;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import org.apache.jena.atlas.logging.Log;
import org.apache.tamaya.ConfigurationProvider;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

public class BuildContainerImageTask extends BlockingTask<Object> {

    private final CreateContainerImageRequest request;
    private final ImageArchiveClient archive;

    public BuildContainerImageTask(CreateContainerImageRequest request, EmilEnvironmentRepository emilEnvRepo) {
        this.request = request;
        this.archive = emilEnvRepo.getImageArchive();
        System.out.println("... build container task...");
    }

    @Override
    protected Object execute() throws Exception {
        if (request.getUrlString() == null) {
            return new BWFLAException("invalid url: " + request.getUrlString());
        }

        if (request.getContainerType() == null)
            return new BWFLAException("missing image type");

        if (request.shouldCheckForExistingDigest()) {

            String digest = (null == request.getDigest()) ?
                    getDigest(request.getUrlString(), request.getTag()) : request.getDigest();


            Log.info("BuildContainerImageTask", "Checking digest: " + digest);

            Optional<ContainerConfiguration> existingContainer = archive.api()
                    .v2()
                    .containers()
                    .fetch()
                    .stream()
                    .filter(e -> e.getDigest() != null)
                    .filter(e -> !e.isDeleted())
                    .filter(e -> e.getDigest().equals(digest)).findFirst();

            //.forEach(e -> System.out.println("ContainerID:" + e.getId() + " Digest:" + e.getDigest()));


            if (existingContainer.isPresent()) {
                OciContainerConfiguration _config = (OciContainerConfiguration) existingContainer.get();
                log.severe(" Container with digest " + digest + " already found, returning environment id: " + _config.getId());

                return new ComponentResponse(_config.getId());
            }

        }


        try {
            return BuildContainerUtil.build(request);
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

    private String getDigest(String imageRef, String tag) throws BWFLAException {

        if (tag == null)
            tag = "latest";

        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setLogger(log);
        runner.setCommand("crane");
        runner.addArgument("digest");
        runner.addArgument(imageRef + ":" + tag);
        try {
            final DeprecatedProcessRunner.Result result = runner.executeWithResult(true)
                    .orElse(null);
            if (result == null || !result.successful())
                throw new BWFLAException("Running crane failed!");

            return result.stdout()
                    .trim();
        } catch (IOException error) {
            throw new BWFLAException("Parsing docker digest failed!", error);
        }
    }

}
