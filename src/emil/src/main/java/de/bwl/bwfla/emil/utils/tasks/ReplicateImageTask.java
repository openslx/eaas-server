package de.bwl.bwfla.emil.utils.tasks;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emucomp.api.AbstractDataResource;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imageproposer.client.ImageProposer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
