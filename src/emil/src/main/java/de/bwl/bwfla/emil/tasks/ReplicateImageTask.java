package de.bwl.bwfla.emil.tasks;

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.util.EmulatorMetaHelperV2;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.EmilEnvironmentPermissions;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageRequest;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageResult;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emil.utils.BuildContainerUtil;
import de.bwl.bwfla.emil.utils.ImportEmulatorUtil;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imageproposer.client.ImageProposer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicateImageTask extends BlockingTask<Object>
{

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
        put("Browser", "browser");
        put("VisualBoyAdvance", "visualboyadvance");
    }};

    public static class ReplicateImageTaskRequest {

        @Deprecated
        public DatabaseEnvironmentsAdapter environmentHelper;
        public ImageArchiveClient imagearchive;
        public ImageProposer imageProposer;
        public String destArchive;
        public Environment env;
        public EmilEnvironment emilEnvironment;
        public EmilEnvironmentRepository repository;
        public UserContext userctx;

        public void validate() throws BWFLAException
        {
            if(destArchive == null || env == null)
                throw new BWFLAException("input validation failed");

            if(environmentHelper == null || imageProposer == null)
                throw new BWFLAException("missing dependencies");

            if (!repository.checkPermissions(emilEnvironment, EmilEnvironmentPermissions.Permissions.WRITE, userctx))
                throw new BWFLAException("Access denied!");

            if (userctx.getTenantId() != null) {
                // only node-admin can make an environment public!
                if (destArchive.equals(EmilEnvironmentRepository.MetadataCollection.PUBLIC) && userctx.getRole() != Role.ADMIN)
                    throw new BWFLAException("Access denied!");
            }
        }
    }

    @Override
    protected Object execute() throws Exception {
        EmulatorSpec emulatorSpec = null;
        if(request.env instanceof MachineConfiguration) {
            emulatorSpec = ((MachineConfiguration)request.env).getEmulator();
        }

        final var emulatorMetaHelper = new EmulatorMetaHelperV2(request.imagearchive, log);

        // ensure the published environments have emulator info
        if (request.env instanceof MachineConfiguration && request.emilEnvironment.getArchive().equals(EmilEnvironmentRepository.MetadataCollection.DEFAULT)) {

            if (emulatorSpec.getContainerName() == null || emulatorSpec.getContainerName().isEmpty()) {
                String containerName = emulatorContainerMap.get(emulatorSpec.getBean());
                if (containerName == null)
                    throw new BWFLAException("this environment cannot be exported. old metadata. set an emulator first: " + emulatorSpec.getBean());

                emulatorSpec.setContainerName(containerName);
            }

            if (emulatorSpec.getOciSourceUrl() == null || emulatorSpec.getOciSourceUrl().isEmpty()) {
                final var entry = emulatorMetaHelper.fetch(emulatorSpec.getContainerName(), emulatorSpec.getContainerVersion());
                if (entry == null)
                    throw new BWFLAException("emulator entry not found. can't publish this environment");

                emulatorSpec.setContainerVersion(entry.version());
                emulatorSpec.setOciSourceUrl(entry.provenance().url());
                emulatorSpec.setDigest(entry.digest());
            }
        }

        if(request.env instanceof MachineConfiguration && request.emilEnvironment.getArchive().equals(EmilEnvironmentRepository.MetadataCollection.REMOTE)) {
            if(emulatorSpec.getContainerName() == null || emulatorSpec.getContainerName().isEmpty())
                throw new BWFLAException("this environment cannot be imported. old metadata. set an emulator first");

            if(!emulatorMetaHelper.exists(emulatorSpec.getContainerName(), emulatorSpec.getContainerVersion())) {
                // we need to import the emulator
                if(emulatorSpec == null)
                    throw new BWFLAException("no emulator info available. fix metadata");

                String ociSourceUrl = emulatorSpec.getOciSourceUrl();
                String digest = emulatorSpec.getDigest();
                String tag = emulatorSpec.getContainerVersion();

                if(ociSourceUrl == null)
                    throw new BWFLAException("invalid emulator metadata: ociSource is mandatory");

                CreateContainerImageRequest containerImageRequest = new CreateContainerImageRequest();
                containerImageRequest.setContainerType(CreateContainerImageRequest.ContainerType.DOCKERHUB);
                if(tag != null)
                    containerImageRequest.setTag(tag.replace('.', '-').replace('+', '-'));
                else
                    containerImageRequest.setDigest(digest);
                containerImageRequest.setUrlString(ociSourceUrl);
                CreateContainerImageResult containerImage = BuildContainerUtil.build(containerImageRequest);

                ImportEmulatorRequest importEmulatorRequest = new ImportEmulatorRequest();
                importEmulatorRequest.setImageUrl(containerImage.getContainerUrl());
                importEmulatorRequest.setMetadata(containerImage.getMetadata());
                ImportEmulatorUtil.doImport(importEmulatorRequest, emulatorMetaHelper, request.environmentHelper);
            }

        }

        // disable for now. for default items we only need to create a HDL. TODO

        List<AbstractDataResource> resources = null;
        if (request.env instanceof MachineConfiguration)
            resources = ((MachineConfiguration) request.env).getAbstractDataResource();
        else if (request.env instanceof OciContainerConfiguration)
            resources = ((OciContainerConfiguration) request.env).getDataResources();

        final var skipcopy = request.destArchive.equals(request.emilEnvironment.getArchive())
                && request.destArchive.equals(EmilEnvironmentRepository.MetadataCollection.DEFAULT);

        if (skipcopy)
            resources = null;  // all resources should be available locally!

        try {
            final var options = new ReplaceOptionsV2()
                    .setLocation(request.destArchive);

            request.imagearchive.api()
                    .v2()
                    .environments()
                    .replicate(request.env, resources, request.userctx, options);

            request.repository.replicate(request.emilEnvironment, request.destArchive, request.userctx);
        }
        catch (Throwable error) {
            log.log(Level.WARNING, "Replicating environment failed!", error);
        }

        return request.env;
    }
}
