package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emil.utils.ContainerUtil;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.util.EmulatorRegistryUtil;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imageproposer.client.ImageProposer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static de.bwl.bwfla.emil.datatypes.rest.ImportContainerRequest.ContainerImageType.DOCKERHUB;

public class ReplicateImageTask extends AbstractTask<Object> {

    private ReplicateImageTaskRequest request;
    private Logger log;

    public ReplicateImageTask(ReplicateImageTaskRequest request, Logger log)
    {
        this.request = request;
        this.log = log;
    }

    // TODO: refactor
    static final Map<String, String> emulatorContainerMap = new HashMap<String, String>() {{
        put("Qemu", "qemu-system");
        put("BasiliskII", "basiliskII");
        put("Beebem", "beebem");
        put("Hatari", "hatari");
        put("Sheepshaver", "sheepshaver");
        put("ViceC64", "vice-sdl");
    }};

    public static class ReplicateImageTaskRequest {

        public DatabaseEnvironmentsAdapter environmentHelper;
        public ImageProposer imageProposer;
        public String destArchive;
        public Environment env;
        public EmilEnvironment emilEnvironment;
        public EmilEnvironmentRepository repository;
        public String username;
        public ContainerUtil containerUtil;

        public void validate() throws BWFLAException
        {
            if(destArchive == null || env == null)
                throw new BWFLAException("input validation failed");

            if(environmentHelper == null || imageProposer == null)
                throw new BWFLAException("missing dependencies");
        }
    }
    @Override
    protected Object execute() throws Exception {
        ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
        iaMd.setType(ImageType.USER);

        System.out.println("REPLICATING IMAGE");
        EmulatorSpec emulatorSpec = null;
        if(request.env instanceof MachineConfiguration) {
            emulatorSpec = ((MachineConfiguration)request.env).getEmulator();
        }


        // ensure the published environments have emulator info
        if (request.emilEnvironment.getArchive().equals(EmilEnvironmentRepository.MetadataCollection.DEFAULT)) {

            if (emulatorSpec.getContainerName() == null || emulatorSpec.getContainerName().isEmpty()) {
                String containerName = emulatorContainerMap.get(emulatorSpec.getBean());
                if (containerName == null)
                    throw new BWFLAException("this environment cannot be exported. old metadata. set an emulator first");

                emulatorSpec.setContainerName("emucon-rootfs/" + containerName);
            }
            ImageNameIndex index = request.environmentHelper.getNameIndexes();


            if (emulatorSpec.getOciSourceUrl() == null || emulatorSpec.getOciSourceUrl().isEmpty()) {
                ImageMetadata entry = EmulatorRegistryUtil.getEntry(index, emulatorSpec.getContainerName(), emulatorSpec.getContainerVersion());

                if (entry == null)
                    throw new BWFLAException("emulator entry not found. can't publish this environment");

                emulatorSpec.setContainerVersion(entry.getVersion());
                emulatorSpec.setOciSourceUrl(entry.getProvenance().getOciSourceUrl());
                emulatorSpec.setDigest(entry.getDigest());
            }
        }


        if(request.env instanceof MachineConfiguration && request.emilEnvironment.getArchive().equals(EmilEnvironmentRepository.MetadataCollection.REMOTE)) {
            if(emulatorSpec.getContainerName() == null || emulatorSpec.getContainerName().isEmpty())
                throw new BWFLAException("this environment cannot be imported. old metadata. set an emulator first");

            ImageNameIndex index = request.environmentHelper.getNameIndexes();
            ImageMetadata entry = EmulatorRegistryUtil.getEntry(index, emulatorSpec.getContainerName(), emulatorSpec.getContainerVersion());
            if(entry == null) // we need to import the emulator
            {
                if(emulatorSpec == null)
                    throw new BWFLAException("no emulator info available. fix metadata");

                String ociSourceUrl = emulatorSpec.getOciSourceUrl();
                String digest = emulatorSpec.getDigest();

                if(ociSourceUrl == null)
                    throw new BWFLAException("invalid emulator metadata: ociSource is mandatory");

                ImportEmulatorRequest importEmulatorRequest = new ImportEmulatorRequest();
                importEmulatorRequest.setDigest(digest);
                importEmulatorRequest.setUrlString(ociSourceUrl);
                importEmulatorRequest.setImageType(DOCKERHUB);

                request.containerUtil.importEmulator(importEmulatorRequest);
            }

        }

        // disable for now. for default items we only need to create a HDL. TODO
        // if(request.emilEnvironment.getArchive().equals(EmilEnvironmentRepository.MetadataCollection.REMOTE)) {
        List<AbstractDataResource> resources = null;
        if (request.env instanceof MachineConfiguration)
            resources = ((MachineConfiguration) request.env).getAbstractDataResource();
        else if (request.env instanceof OciContainerConfiguration)
            resources = ((OciContainerConfiguration) request.env).getDataResources();

        for (AbstractDataResource abr : resources) {
                if (abr instanceof ImageArchiveBinding) {
                    List<String> images = new ArrayList<>();
                    ImageArchiveBinding iab = (ImageArchiveBinding) abr;
                    images.add(iab.getUrl());

                    List<EnvironmentsAdapter.ImportImageHandle> sessions =
                            request.environmentHelper.replicateImages(request.destArchive, images);

                    if (sessions.size() != 1) {
                        log.severe("replicate images failed: session creation failed");
                        continue;
                    }

                    ImageArchiveBinding binding = sessions.get(0).getBinding(60 * 60);
                    if (binding == null) {
                        log.severe("binding null: " + iab.getImageId());
                        throw new BWFLAException("ImportImageTask: import image failed. Could not create binding");
                    }

                    iab.setUrl(null);
                    iab.setBackendName(binding.getBackendName());
                    iab.setType(binding.getType());
                    iab.setUrlPrefix(null);
                    iab.setImageId(binding.getImageId());
                } else
                    log.severe("not an imagearchive binding");
            }
        // }
        try {
            request.environmentHelper.importMetadata(request.destArchive, request.env, iaMd, true);
            request.repository.replicate(request.emilEnvironment, request.destArchive, request.username);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return request.env;
    }
}
