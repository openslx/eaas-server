package de.bwl.bwfla.emil.utils.tasks;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emil.utils.ContainerUtil;
import de.bwl.bwfla.emucomp.api.AbstractDataResource;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.imagearchive.util.EmulatorRegistryUtil;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imageproposer.client.ImageProposer;

import javax.inject.Inject;
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

    public static class ReplicateImageTaskRequest {

        public DatabaseEnvironmentsAdapter environmentHelper;
        public ImageProposer imageProposer;
        public String destArchive;
        public MachineConfiguration env;
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

        // ensure the published environments have emulator info
        if(request.emilEnvironment.getArchive().equals(EmilEnvironmentRepository.MetadataCollection.DEFAULT)) {
            if(request.env.getEmulator().getContainerName() == null || request.env.getEmulator().getContainerName().isEmpty())
                throw new BWFLAException("this environment cannot be exported. old metadata. set an emulator first");
            ImageNameIndex index = request.environmentHelper.getNameIndexes();

            if(request.env.getEmulator().getOciSourceUrl() == null || request.env.getEmulator().getOciSourceUrl().isEmpty()) {
                Entry entry = EmulatorRegistryUtil.getEntry(index, request.env.getEmulator().getContainerName(), request.env.getEmulator().getContainerVersion());

                if (entry == null)
                    throw new BWFLAException("emulator entry not found. can't publish this environment");

                request.env.getEmulator().setOciSourceUrl(entry.getProvenance().getOciSourceUrl());
                request.env.getEmulator().setDigest(entry.getDigest());
            }
        }

        if(request.emilEnvironment.getArchive().equals(EmilEnvironmentRepository.MetadataCollection.REMOTE)) {
            if(request.env.getEmulator().getContainerName() == null || request.env.getEmulator().getContainerName().isEmpty())
                throw new BWFLAException("this environment cannot be imported. old metadata. set an emulator first");

            ImageNameIndex index = request.environmentHelper.getNameIndexes();
            Entry entry = EmulatorRegistryUtil.getEntry(index, request.env.getEmulator().getContainerName(), request.env.getEmulator().getContainerVersion());
            if(entry == null) // we need to import the emulator
            {
                if(request.env.getEmulator() == null)
                    throw new BWFLAException("no emulator info available. fix metadata");

                String ociSourceUrl = request.env.getEmulator().getOciSourceUrl();
                String digest = request.env.getEmulator().getDigest();

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
            List<AbstractDataResource> resources = request.env.getAbstractDataResource();
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
