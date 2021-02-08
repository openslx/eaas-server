package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.datatypes.EnvironmentDescription;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.rest.ImportContainerRequest;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.OciContainerConfiguration;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import org.apache.tamaya.ConfigurationProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ImportContainerTask extends BlockingTask<Object>
{
    private final DatabaseEnvironmentsAdapter envHelper;
    private final ImportContainerRequest containerRequest;
    private final EmilEnvironmentRepository emilEnvironmentRepository;
    private final String collectionCtx;


    public ImportContainerTask(ImportContainerRequest containerRequest, DatabaseEnvironmentsAdapter envHelper,
                               EmilEnvironmentRepository environmentRepository, String collectionCtx) {
        this.containerRequest = containerRequest;
        this.envHelper = envHelper;
        this.emilEnvironmentRepository = environmentRepository;
        this.collectionCtx = collectionCtx;
    }

    private String importContainer(ImportContainerRequest containerRequest) throws BWFLAException, MalformedURLException {

        URL containerImageUrl = new URL(containerRequest.getImageUrl());

        ImageArchiveMetadata meta = new ImageArchiveMetadata();
        meta.setType(ImageType.CONTAINERS);

        EnvironmentsAdapter.ImportImageHandle importState = null;
        ImageArchiveBinding binding;
        importState = envHelper.importImage("default", containerImageUrl, meta, true);
        binding = importState.getBinding(60 * 60 * 60); //wait an hour

        binding.setId("rootfs");
        binding.setFileSystemType("ext4");

        OciContainerConfiguration config = new OciContainerConfiguration();
        EnvironmentDescription description = new EnvironmentDescription();
        description.setTitle(containerRequest.getName());
        config.setDescription(description);
        config.getDataResources().add(binding);

        config.setGui(containerRequest.guiRequired());
        if (containerRequest.getCustomSubdir() != null && !containerRequest.getCustomSubdir().equals(""))
            config.setCustomSubdir(containerRequest.getCustomSubdir());

        config.setOutputPath(containerRequest.getOutputFolder());
        config.setInputPath(containerRequest.getInputFolder());
        config.setRootFilesystem("binding://rootfs");

        OciContainerConfiguration.Process process = new OciContainerConfiguration.Process();
        process.setArguments(containerRequest.getProcessArgs());
        process.setWorkingDir(containerRequest.getWorkingDir());
        if (containerRequest.getProcessEnvs() != null && containerRequest.getProcessEnvs().size() > 0)
            process.setEnvironmentVariables(containerRequest.getProcessEnvs());

        config.setProcess(process);
        config.setId(UUID.randomUUID().toString());
        return envHelper.importMetadata("default", config, meta, false);
    }

    @Override
    protected Object execute() throws Exception {

        String newEnvironmentId = importContainer(containerRequest);
        Map<String, String> userData = new HashMap<>();
        userData.put("environmentId", newEnvironmentId);
        emilEnvironmentRepository.saveImportedContainer(newEnvironmentId, containerRequest, this.collectionCtx);
        return userData;
    }
}
