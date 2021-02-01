package de.bwl.bwfla.emil.utils;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.api.imagebuilder.DockerImport;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderMetadata;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderResult;
import de.bwl.bwfla.common.datatypes.EnvironmentDescription;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.configuration.converters.DurationPropertyConverter;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageRequest;
import de.bwl.bwfla.emil.datatypes.rest.ImportContainerRequest;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.WithPropertyConverter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.logging.Logger;

@ApplicationScoped
public class ContainerUtil {

    protected static final Logger LOG = Logger.getLogger("eaas/containerUtil");

    @Inject
    @Config(value = "emil.containerdata.imagebuilder.delay")
    @WithPropertyConverter(DurationPropertyConverter.class)
    private Duration imageBuilderDelay = null;

    @Inject
    @Config(value = "emil.containerdata.imagebuilder.timeout")
    @WithPropertyConverter(DurationPropertyConverter.class)
    private Duration imageBuilderTimeout = null;

    @Inject
    @Config(value = "rest.blobstore")
    String blobStoreRestAddress;

    @Inject
    @Config(value = "ws.imagebuilder")
    String imageBuilderAddress;

    @Inject
    @Config(value = "ws.blobstore")
    private String blobStoreWsAddress;

    @Inject
    private DatabaseEnvironmentsAdapter envHelper;


    private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

    private String getEmulatorArchive() {
        String archive = ConfigurationProvider.getConfiguration().get("emucomp.emulator_archive");
        if(archive == null || archive.isEmpty())
            return EMULATOR_DEFAULT_ARCHIVE;
        return archive;
    }


        public void importEmulator(ImportEmulatorRequest emulatorRequest) throws BWFLAException, MalformedURLException {


        /*
        if(containerImage.imageBuilderResult == null || containerImage.imageBuilderResult.getMetadata() == null)
            throw new BWFLAException("no metadata available");
        */

            /*
        ImageArchiveMetadata meta = new ImageArchiveMetadata();
        meta.setType(ImageType.BASE);

        EnvironmentsAdapter.ImportImageHandle importState = null;
        ImageArchiveBinding binding;
        importState = envHelper.importImage(getEmulatorArchive(), containerImage.imageUrl, meta, true);
        binding = importState.getBinding(60 * 60 * 60); //wait an hour


        de.bwl.bwfla.api.imagearchive.ImageDescription iD = new de.bwl.bwfla.api.imagearchive.ImageDescription();
        EmulatorMetadata emulatorMetadata = envHelper.extractMetadata(binding.getImageId());
        iD.setType(meta.getType().value());
        iD.setId(binding.getImageId());
        iD.setFstype(FileSystemType.EXT4.toString());

        ImageMetadata entry = new ImageMetadata();
        Alias alias = new Alias();

        entry.setImage(iD);

        if(containerImage.imageBuilderResult != null) {
            ImageBuilderMetadata md = containerImage.imageBuilderResult.getMetadata();
            if (md instanceof DockerImport) {

                DockerImport dockerMd = (DockerImport) md;
                Provenance pMd = new Provenance();
                pMd.getLayers().addAll(dockerMd.getLayers());
                pMd.setOciSourceUrl(dockerMd.getImageRef());
                pMd.setVersionTag(dockerMd.getTag());
                entry.setProvenance(pMd);

                entry.setName("emucon-rootfs/" + dockerMd.getEmulatorType());
                entry.setVersion(dockerMd.getEmulatorVersion());
                entry.setDigest(dockerMd.getDigest());

                alias.setName("emucon-rootfs/" + dockerMd.getEmulatorType());
                alias.setVersion(dockerMd.getEmulatorVersion());
                if (emulatorRequest.getAlias() != null && !emulatorRequest.getAlias().isEmpty())
                    alias.setAlias(emulatorRequest.getAlias());
                else
                    alias.setAlias(dockerMd.getEmulatorVersion());
            }
        }
        else if(emulatorMetadata != null)
        {
            Provenance pMd = new Provenance();

            pMd.setOciSourceUrl(emulatorMetadata.getOciSourceUrl());
            pMd.setVersionTag(emulatorMetadata.getEmulatorVersion());
            entry.setProvenance(pMd);

            entry.setName("emucon-rootfs/" + emulatorMetadata.getEmulatorType());
            entry.setVersion(emulatorMetadata.getEmulatorVersion());
            entry.setDigest(emulatorMetadata.getContainerDigest());

            alias.setName("emucon-rootfs/" + emulatorMetadata.getEmulatorType());
            alias.setVersion(emulatorMetadata.getEmulatorVersion());
            if(emulatorRequest.getAlias() != null && !emulatorRequest.getAlias().isEmpty())
                alias.setAlias(emulatorRequest.getAlias());
            else
                alias.setAlias(emulatorMetadata.getEmulatorVersion());
        }
        else
        {
            throw new BWFLAException("not supported");
        }
        envHelper.addNameIndexesEntry(getEmulatorArchive(), entry, alias);
        */
    }



}
