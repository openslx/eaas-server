package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.CompletableTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageResult;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import org.apache.tamaya.ConfigurationProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class ImportEmulatorTask extends CompletableTask<Object> {

    private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";
    private final DatabaseEnvironmentsAdapter envHelper;
    private final ImportEmulatorRequest emulatorRequest;

    public ImportEmulatorTask(ImportEmulatorRequest emulatorRequest, DatabaseEnvironmentsAdapter envHelper)
    {
        this.envHelper = envHelper;
        this.emulatorRequest = emulatorRequest;
    }

    private String getEmulatorArchive() {
        String archive = ConfigurationProvider.getConfiguration().get("emucomp.emulator_archive");
        if(archive == null || archive.isEmpty())
            return EMULATOR_DEFAULT_ARCHIVE;
        return archive;
    }

    private void importEmulator(ImportEmulatorRequest emulatorRequest) throws BWFLAException, MalformedURLException {
        ImageArchiveMetadata meta = new ImageArchiveMetadata();
        meta.setType(ImageType.BASE);

        EnvironmentsAdapter.ImportImageHandle importState = null;
        ImageArchiveBinding binding;
        importState = envHelper.importImage(getEmulatorArchive(), new URL(emulatorRequest.getImageUrl()), meta, true);
        binding = importState.getBinding(60 * 60 * 60); //wait an hour

        de.bwl.bwfla.api.imagearchive.ImageDescription iD = new de.bwl.bwfla.api.imagearchive.ImageDescription();
        EmulatorMetadata emulatorMetadata = envHelper.extractMetadata(binding.getImageId());
        iD.setType(meta.getType().value());
        iD.setId(binding.getImageId());
        iD.setFstype(FileSystemType.EXT4.toString());

        ImageMetadata entry = new ImageMetadata();
        Alias alias = new Alias();

        entry.setImage(iD);
        Provenance pMd = new Provenance();

        if (emulatorMetadata != null) {
            pMd.setOciSourceUrl(emulatorMetadata.getOciSourceUrl());
            pMd.setVersionTag(emulatorMetadata.getEmulatorVersion());
            entry.setProvenance(pMd);

            entry.setName("emucon-rootfs/" + emulatorMetadata.getEmulatorType());
            entry.setVersion(emulatorMetadata.getEmulatorVersion());
            entry.setDigest(emulatorMetadata.getContainerDigest());

            alias.setName("emucon-rootfs/" + emulatorMetadata.getEmulatorType());
            alias.setVersion(emulatorMetadata.getEmulatorVersion());
            alias.setAlias(emulatorMetadata.getEmulatorVersion());
        } else if (emulatorRequest.getMetadata() != null) {
            CreateContainerImageResult.ContainerImageMetadata cmd = emulatorRequest.getMetadata();
            pMd.setOciSourceUrl(cmd.getContainerSourceUrl());
            pMd.setVersionTag(cmd.getTag());
            entry.setProvenance(pMd);

            if( cmd.getEmulatorType() == null || cmd.getEmulatorVersion() == null)
                throw new BWFLAException("not a emulator container or unsupported metadata format");

            entry.setName("emucon-rootfs/" + cmd.getEmulatorType());
            entry.setVersion(cmd.getEmulatorVersion());
            entry.setDigest(cmd.getContainerDigest());

            alias.setName("emucon-rootfs/" + cmd.getEmulatorType());
            alias.setVersion(cmd.getEmulatorVersion());
            alias.setAlias(cmd.getEmulatorVersion());
        } else {
            throw new BWFLAException("not a emulator container or unsupported metadata format");
        }
        envHelper.addNameIndexesEntry(getEmulatorArchive(), entry, alias);
    }

    @Override
    protected CompletableFuture<Object> execute() throws Exception {
        importEmulator(emulatorRequest);
        return null;
    }
}
