package de.bwl.bwfla.emil.tasks;

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportRequestV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportTargetV2;
import de.bwl.bwfla.common.datatypes.EnvironmentDescription;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.rest.ImportContainerRequest;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.OciContainerConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class ImportContainerTask extends BlockingTask<Object>
{
    private final ImageArchiveClient archive;
    private final ImportContainerRequest containerRequest;
    private final EmilEnvironmentRepository emilEnvironmentRepository;
    private final UserContext userCtx;


    public ImportContainerTask(ImportContainerRequest containerRequest,
                               EmilEnvironmentRepository environmentRepository, UserContext userCtx) {
        this.containerRequest = containerRequest;
        this.archive = environmentRepository.getImageArchive();
        this.emilEnvironmentRepository = environmentRepository;
        this.userCtx = userCtx;
    }

    private String importContainer(ImportContainerRequest containerRequest) throws Exception
    {
        final var importRequest = new ImportRequestV2();
        importRequest.source()
                .setUrl(containerRequest.getImageUrl());

        importRequest.target()
                .setKind(ImportTargetV2.Kind.IMAGE);

        if(containerRequest.getArchive() != null)
            importRequest.target().setLocation(containerRequest.getArchive());

        final var imageid = archive.api()
                .v2()
                .imports()
                .await(importRequest, 1L, TimeUnit.HOURS);


        final var binding = new ImageArchiveBinding();
        binding.setId("rootfs");
        binding.setImageId(imageid);
        binding.setFileSystemType("ext4");

        OciContainerConfiguration config = new OciContainerConfiguration();
        EnvironmentDescription description = new EnvironmentDescription();
        description.setTitle(containerRequest.getName());
        config.setDescription(description);
        config.getDataResources().add(binding);
        config.setDigest(containerRequest.getContainerDigest());

        config.setGui(containerRequest.guiRequired());
        if (containerRequest.getCustomSubdir() != null && !containerRequest.getCustomSubdir().equals(""))
            config.setCustomSubdir(containerRequest.getCustomSubdir());

        config.setOutputPath(containerRequest.getOutputFolder());
        config.setInputPath(containerRequest.getInputFolder());
        config.setRootFilesystem("binding://rootfs");

        OciContainerConfiguration.Process process = new OciContainerConfiguration.Process();
        process.getArguments().addAll(containerRequest.getProcessArgs());
        process.setWorkingDir(containerRequest.getWorkingDir());
        if (containerRequest.getProcessEnvs() != null && containerRequest.getProcessEnvs().size() > 0)
            process.setEnvironmentVariables(containerRequest.getProcessEnvs());

        config.setProcess(process);
        if(containerRequest.isServiceContainer() && containerRequest.getServiceContainerId() != null && !containerRequest.getServiceContainerId().isEmpty()) {
            config.setId(containerRequest.getServiceContainerId());
        }
        else {
            config.setId(UUID.randomUUID().toString());
        }

        final var id = config.getId();

        ReplaceOptionsV2 optionsV2 = new ReplaceOptionsV2();
        if(containerRequest.getArchive() != null)
            optionsV2.setLocation(containerRequest.getArchive());

        archive.api()
                .v2()
                .containers()
                .replace(id, config, optionsV2);

        return id;
    }

    @Override
    protected Object execute() throws Exception {

        String newEnvironmentId = importContainer(containerRequest);
        Map<String, String> userData = new HashMap<>();
        userData.put("environmentId", newEnvironmentId);
        emilEnvironmentRepository.saveImportedContainer(newEnvironmentId, containerRequest, this.userCtx);
        return userData;
    }
}
