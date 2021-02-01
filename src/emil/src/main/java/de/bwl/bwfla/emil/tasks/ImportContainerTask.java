package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.ImageArchiveMetadata;
import de.bwl.bwfla.api.imagearchive.ImageType;
import de.bwl.bwfla.api.imagebuilder.DockerImport;
import de.bwl.bwfla.common.datatypes.EnvironmentDescription;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageRequest;
import de.bwl.bwfla.emil.datatypes.rest.ImportContainerRequest;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emil.utils.ContainerUtil;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.OciContainerConfiguration;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;

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

    public ImportContainerTask(ImportContainerRequest containerRequest, DatabaseEnvironmentsAdapter envHelper, EmilEnvironmentRepository environmentRepository) {
        this.containerRequest = containerRequest;
        this.envHelper = envHelper;
        this.emilEnvironmentRepository = environmentRepository;
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
        // Derive docker variables and processes from image

        if (containerRequest.getImageType() == CreateContainerImageRequest.ContainerType.DOCKERHUB) {
            if(containerRequest.getMetadata() == null)
                throw new BWFLAException("ImageBuilder returned empty result for DOCKERHUB container type");
            process.setEnvironmentVariables(((DockerImport) containerRequest.getMetadata()).getEnvVariables());
            process.setArguments(((DockerImport) containerRequest.getMetadata()).getEntryProcesses());
            process.setWorkingDir(((DockerImport) containerRequest.getMetadata()).getWorkingDir());
        } else {
            process.setArguments(containerRequest.getProcessArgs());

            if (containerRequest.getProcessEnvs() != null && containerRequest.getProcessEnvs().size() > 0)
                process.setEnvironmentVariables(containerRequest.getProcessEnvs());
        }

        config.setProcess(process);
        config.setId(UUID.randomUUID().toString());
        return envHelper.importMetadata("default", config, meta, false);
    }

    @Override
    protected Object execute() throws Exception {
        /*
        if (containerRequest.getUrlString() == null && !(containerRequest instanceof ImportEmulatorRequest)) {
            return new BWFLAException("invalid url: " + containerRequest.getUrlString());
        }
        if ((containerRequest.getProcessArgs() == null || containerRequest.getProcessArgs().size() == 0) &&
                containerRequest.getImageType() != ImportContainerRequest.ContainerImageType.DOCKERHUB &&
                !(containerRequest instanceof ImportEmulatorRequest)) {
            return new BWFLAException("missing process args");
        }
        */

        /*
        if (containerRequest.getImageType() == null)
            return new BWFLAException("missing image type");
        */
        if (containerRequest instanceof ImportEmulatorRequest) {
           // ImportEmulatorRequest request = (ImportEmulatorRequest) containerRequest;
           //  containerUtil.importEmulator(request);
            return new HashMap<>();
        } else {
            String newEnvironmentId = importContainer(containerRequest);
            Map<String, String> userData = new HashMap<>();
            envHelper.sync();
            userData.put("environmentId", newEnvironmentId);

            emilEnvironmentRepository.saveImportedContainer(newEnvironmentId, containerRequest);

            return userData;
        }
    }
}
