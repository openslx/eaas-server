package de.bwl.bwfla.emil.utils;

import com.openslx.eaas.imagearchive.client.endpoint.v2.util.EmulatorMetaHelperV2;
import com.openslx.eaas.imagearchive.databind.EmulatorMetaData;
import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageResult;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import org.apache.tamaya.ConfigurationProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class ImportEmulatorUtil {

    private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

    private static String getEmulatorArchive() {
        String archive = ConfigurationProvider.getConfiguration().get("emucomp.emulator_archive");
        if(archive == null || archive.isEmpty())
            return EMULATOR_DEFAULT_ARCHIVE;
        return archive;
    }

    public static void doImport(ImportEmulatorRequest emulatorRequest, EmulatorMetaHelperV2 emuMetaHelper, DatabaseEnvironmentsAdapter envHelper)
            throws BWFLAException, MalformedURLException
    {
        ImageArchiveMetadata meta = new ImageArchiveMetadata();
        meta.setType(ImageType.BASE);

        EnvironmentsAdapter.ImportImageHandle importState = null;
        ImageArchiveBinding binding;
        importState = envHelper.importImage(getEmulatorArchive(), new URL(emulatorRequest.getImageUrl()), meta, true);
        binding = importState.getBinding(60 * 60 * 60); //wait an hour

        final var newEmulatorMetaData = new EmulatorMetaData();
        newEmulatorMetaData.image()
                .setId(binding.getImageId())
                .setCategory(meta.getType().value())
                .setFileSystemType(FileSystemType.EXT4.toString());

        final var oldEmulatorMetaData = envHelper.extractMetadata(binding.getImageId());
        if (oldEmulatorMetaData != null) {
            newEmulatorMetaData.provenance()
                    .setUrl(oldEmulatorMetaData.getOciSourceUrl())
                    .setTag(oldEmulatorMetaData.getEmulatorVersion());

            newEmulatorMetaData.setName("emucon-rootfs/" + oldEmulatorMetaData.getEmulatorType())
                    .setVersion(oldEmulatorMetaData.getEmulatorVersion())
                    .setDigest(oldEmulatorMetaData.getContainerDigest());
        }
        else if (emulatorRequest.getMetadata() != null) {
            CreateContainerImageResult.ContainerImageMetadata cmd = emulatorRequest.getMetadata();
            newEmulatorMetaData.provenance()
                    .setUrl(cmd.getContainerSourceUrl())
                    .setTag(cmd.getTag());

            if( cmd.getEmulatorType() == null || cmd.getEmulatorVersion() == null)
                throw new BWFLAException("not a emulator container or unsupported metadata format");

            newEmulatorMetaData.setName("emucon-rootfs/" + cmd.getEmulatorType())
                    .setVersion(cmd.getEmulatorVersion())
                    .setDigest(cmd.getContainerDigest());
        }
        else {
            throw new BWFLAException("not a emulator container or unsupported metadata format");
        }

        emuMetaHelper.insert(newEmulatorMetaData);
    }
}
