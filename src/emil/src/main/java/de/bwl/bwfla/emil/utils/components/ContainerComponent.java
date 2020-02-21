package de.bwl.bwfla.emil.utils.components;

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.configuration.converters.DurationPropertyConverter;
import de.bwl.bwfla.emil.Components;
import de.bwl.bwfla.emil.datatypes.ContainerMetadata;
import de.bwl.bwfla.emil.datatypes.rest.ComponentWithExternalFilesRequest;
import de.bwl.bwfla.emil.datatypes.rest.LinuxRuntimeContainerReq;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.WithPropertyConverter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ContainerComponent {

    protected static final Logger LOG = Logger.getLogger("eaas/components/container");

    @Inject
    private BlobStoreClient blobStoreClient;

    @Inject
    @Config(value = "ws.blobstore")
    private String blobStoreWsAddress;

    @Inject
    @Config(value = "rest.blobstore")
    private String blobStoreRestAddress;


    private BlobStore blobstore;

    @Inject
    @Config(value = "emil.containerdata.imagebuilder.delay")
    @WithPropertyConverter(DurationPropertyConverter.class)
    private Duration imageBuilderDelay = null;

    @Inject
    @Config(value = "emil.containerdata.imagebuilder.timeout")
    @WithPropertyConverter(DurationPropertyConverter.class)
    private Duration imageBuilderTimeout = null;

    @Inject
    @Config(value = "ws.imagebuilder")
    private String imageBuilderAddress;

    private ImageBuilder imagebuilder;

    @PostConstruct
    public void init()
    {
        try {
            this.imagebuilder = ImageBuilderClient.get().getImageBuilderPort(imageBuilderAddress);
            this.blobstore = blobStoreClient.getBlobStorePort(blobStoreWsAddress);
        } catch (BWFLAException e) {
            throw new RuntimeException("Constructing web-services failed!", e);
        }
    }

    private BlobHandle prepareMetadata(OciContainerConfiguration config, boolean isDHCPenabled, boolean requiresInputFiles) throws IOException, BWFLAException {
        String metadata = createContainerMetadata(config, isDHCPenabled, requiresInputFiles);
        File tmpfile = File.createTempFile("metadata.json", null, null);
        Files.write(tmpfile.toPath(), metadata.getBytes(), StandardOpenOption.CREATE);

        BlobDescription blobDescription = new BlobDescription();
        blobDescription.setDataFromFile(tmpfile.toPath())
                .setNamespace("random")
                .setDescription("random")
                .setName("metadata")
                .setType(".json");

        return blobstore.put(blobDescription);
    }

    private String getMountStr(String src, String dst, boolean isReadonly) throws BWFLAException {
        if (src != null && dst != null) {
            return src + ":" + dst + ":bind:" + (isReadonly ? "ro" : "rw");
        } else {
            throw new BWFLAException("src or dst is null! src:" + src + " dst:" + dst);
        }
    }

    String createContainerMetadata(OciContainerConfiguration config, boolean isDHCPenabled, boolean requiresInputFiles) throws BWFLAException {
        ArrayList<String> args = new ArrayList<String>();
        ContainerMetadata metadata = new ContainerMetadata();
        final String inputDir = "container-input";
        final String outputDir = "container-output";
        metadata.setDhcp(isDHCPenabled);
        metadata.setTelnet(true);
        metadata.setProcess("/bin/sh");
        args.add("-c");
        args.add("mkdir " + outputDir
                + " && emucon-cgen --enable-extensive-caps \"$@\"; runc run eaas-job | tee "
                + outputDir + "/container-log-" + UUID.randomUUID() + ".log");
        args.add("");

        args.add("--output");
        args.add("config.json");


        if(requiresInputFiles) {
            args.add("--mount");
            args.add(getMountStr(inputDir, config.getInput(), true));
        }
        args.add("--mount");
        args.add(getMountStr(outputDir, config.getOutputPath(), false));

        if (config.getCustomSubdir() != null) {
            args.add("--rootfs");
            args.add("rootfs/" + config.getCustomSubdir());
        }

        // Add environment variables
        if(config.getProcess().getEnvironmentVariables() != null) {
            for (String env : config.getProcess().getEnvironmentVariables()) {
                args.add("--env");
                args.add(env);
            }
        }

        // Add emulator's command
        args.add("--");
        for (String arg : config.getProcess().getArguments())
            args.add(arg);

        metadata.setArgs(args);

        return metadata.jsonValueWithoutRoot(true);
    }


    public ImageDescription prepareContainerRuntimeImage(OciContainerConfiguration config, LinuxRuntimeContainerReq linuxRuntime, ArrayList<ComponentWithExternalFilesRequest.InputMedium> inputMedia) throws IOException, BWFLAException {
        if (inputMedia.size() != 1)
            throw new BWFLAException("Size of Input drives cannot exceed 1");

        ComponentWithExternalFilesRequest.InputMedium medium = inputMedia.get(0);
        final FileSystemType fileSystemType = FileSystemType.EXT4;
        int sizeInMb = medium.getSizeInMb();
        if (sizeInMb <= 0)
            sizeInMb = 1024;

        final ImageDescription description = new ImageDescription()
                .setMediumType(MediumType.HDD)
                .setPartitionTableType(PartitionTableType.NONE)
                .setPartitionStartBlock(0)
                .setFileSystemType(fileSystemType)
                .setLabel("eaas-job")
                .setSizeInMb(sizeInMb);

        BlobHandle mdBlob = prepareMetadata(config, linuxRuntime.isDHCPenabled(), medium.getExtFiles().size() > 0);
        final ImageContentDescription metadataEntry = new ImageContentDescription();
        metadataEntry.setAction(ImageContentDescription.Action.COPY)
                .setDataFromUrl(new URL(mdBlob.toRestUrl(blobStoreRestAddress)))
                .setName("metadata.json");
        description.addContentEntry(metadataEntry);


        for (ComponentWithExternalFilesRequest.FileURL extfile : medium.getExtFiles()) {
            final ImageContentDescription entry = new ImageContentDescription()
                    .setAction(extfile.getAction())
                    .setArchiveFormat(ImageContentDescription.ArchiveFormat.TAR)
                    .setURL(new URL(extfile.getUrl()))
                    .setSubdir("container-input");


            if (extfile.getName() == null || extfile.getName().isEmpty())
                entry.setName(FilenameUtils.getName(entry.getURL().getPath()));
            else
                entry.setName(extfile.getName());

            description.addContentEntry(entry);
        }

        return description;
    }



}
