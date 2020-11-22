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

    private ImageDescription defaultContainerImage() {
        return new ImageDescription()
                .setMediumType(MediumType.HDD)
                .setPartitionTableType(PartitionTableType.NONE)
                .setFileSystemType(FileSystemType.EXT4)
                .setLabel("container")
                .setSizeInMb(1024 * 10); // 10 Gb virtual size
    }

    private ImageBuilderResult createImageFromDescription(ImageDescription description) throws BWFLAException
    {
        final ImageBuilder imagebuilder = ImageBuilderClient.get().getImageBuilderPort(imageBuilderAddress);
        return ImageBuilderClient.build(imagebuilder, description, imageBuilderTimeout, imageBuilderDelay);
    }

    private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

    private String getEmulatorArchive() {
        String archive = ConfigurationProvider.getConfiguration().get("emucomp.emulator_archive");
        if(archive == null || archive.isEmpty())
            return EMULATOR_DEFAULT_ARCHIVE;
        return archive;
    }

    private static ImageContentDescription getImageEntryFromUrlStr(String urlString) throws BWFLAException
    {
        ImageContentDescription entry;
        entry = new ImageContentDescription();
        try {
            entry.setUrlDataSource(new URL(urlString));
        } catch (MalformedURLException e) {
            final String filename = urlString;
            if (filename.contains("/")) {
                throw new BWFLAException("filename must not be null/empty or contain '/' characters:" + filename);
            }
            File archiveFile = new File("/eaas/import/", filename);
            if(!archiveFile.exists()) {
                throw new BWFLAException("file " + filename + " not found in input folder");
            }
            entry.setFileDataSource(archiveFile.toPath());
        }
        return entry;
    }

    private ImageBuilderResult createImageFromArchiveFile(String srcUrlString) throws BWFLAException {

        ImageDescription description = defaultContainerImage();
        ImageContentDescription entry = getImageEntryFromUrlStr(srcUrlString);

        entry.setAction(ImageContentDescription.Action.EXTRACT)
                .setArchiveFormat(ImageContentDescription.ArchiveFormat.TAR);

        description.addContentEntry(entry);
        return this.createImageFromDescription(description);
    }

    private ImageBuilderResult createImageFromSingularityImg(String srcUrlString) throws BWFLAException
    {
        ImageDescription description = defaultContainerImage();
        ImageContentDescription entry = getImageEntryFromUrlStr(srcUrlString);

        entry.setAction(ImageContentDescription.Action.EXTRACT)
                .setArchiveFormat(ImageContentDescription.ArchiveFormat.SIMG);

        description.addContentEntry(entry);

        return this.createImageFromDescription(description);
    }

    private ImageBuilderResult createImageFromDockerHub(String dockerName, String tag, String digest) throws BWFLAException {
        ImageDescription description = defaultContainerImage();
        ImageContentDescription entry = new ImageContentDescription();
        entry.setAction(ImageContentDescription.Action.RSYNC);
        ImageContentDescription.DockerDataSource dockerDataSource
                = new ImageContentDescription.DockerDataSource(dockerName, tag);

        dockerDataSource.imageArchiveHost = envHelper.getImageArchiveHost();
        dockerDataSource.digest = digest;
        entry.setDataSource(dockerDataSource);
        description.addContentEntry(entry);
        ImageBuilderResult result = this.createImageFromDescription(description);

        return result;
    }

    private ContainerImage createImage(ImportContainerRequest req) throws BWFLAException, MalformedURLException {
        ImageBuilderResult result = null;
        URL imageUrl = null;
        ContainerImage containerImage = new ContainerImage();

        switch(req.getImageType())
        {
            case ROOTFS:
                result = createImageFromArchiveFile(req.getUrlString());
                break;
            case SIMG:
                result = createImageFromSingularityImg(req.getUrlString());
                break;
            case DOCKERHUB:
                result = createImageFromDockerHub(req.getUrlString(), req.getTag(), req.getDigest());
                break;
            case READYMADE:
                imageUrl = new URL(req.getUrlString());
                break;

            default:
                throw new BWFLAException("unknown imageType " + req.getImageType());
        }
        if (imageUrl == null) {
            if (result.getBlobHandle() == null)
                throw new BWFLAException("Image blob unavailable");
            try {
                imageUrl = new URL(result.getBlobHandle().toRestUrl(blobStoreRestAddress, false));
            } catch (MalformedURLException e) {
                throw new BWFLAException(e);
            }
        }
        containerImage.imageUrl = imageUrl;
        containerImage.imageBuilderResult = result;

        return containerImage;
    }


        public void importEmulator(ImportEmulatorRequest emulatorRequest) throws BWFLAException, MalformedURLException {

        ContainerImage containerImage = createImage(emulatorRequest);

        /*
        if(containerImage.imageBuilderResult == null || containerImage.imageBuilderResult.getMetadata() == null)
            throw new BWFLAException("no metadata available");
        */
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
    }

    public String importContainer(ImportContainerRequest containerRequest) throws BWFLAException, MalformedURLException {

        ContainerImage containerImage = createImage(containerRequest);

        ImageArchiveMetadata meta = new ImageArchiveMetadata();
        meta.setType(ImageType.TMP);

        EnvironmentsAdapter.ImportImageHandle importState = null;
        ImageArchiveBinding binding;
        importState = envHelper.importImage("default", containerImage.imageUrl, meta, true);
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

        if (containerRequest.getImageType() == ImportContainerRequest.ContainerImageType.DOCKERHUB) {
            if(containerImage.imageBuilderResult == null)
                throw new BWFLAException("ImageBuilder returned empty result for DOCKERHUB container type");
            process.setEnvironmentVariables(((DockerImport) containerImage.imageBuilderResult.getMetadata()).getEnvVariables());
            process.setArguments(((DockerImport) containerImage.imageBuilderResult.getMetadata()).getEntryProcesses());
            process.setWorkingDir(((DockerImport) containerImage.imageBuilderResult.getMetadata()).getWorkingDir());
        } else {
            process.setArguments(containerRequest.getProcessArgs());

            if (containerRequest.getProcessEnvs() != null && containerRequest.getProcessEnvs().size() > 0)
                process.setEnvironmentVariables(containerRequest.getProcessEnvs());
        }

        config.setProcess(process);
        config.setId(containerRequest.getUrlString());
        return envHelper.importMetadata("default", config, meta, false);
    }

    private static class ContainerImage {
        public URL imageUrl;
        public ImageBuilderResult imageBuilderResult;
    }
}
