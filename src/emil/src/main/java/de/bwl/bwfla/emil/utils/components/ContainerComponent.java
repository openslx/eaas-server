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


    private BlobHandle prepareMetadata(OciContainerConfiguration config, boolean isDHCPenabled, boolean requiresInputFiles, boolean enableTelnet) throws IOException, BWFLAException {
        String metadata = createContainerMetadata(config, isDHCPenabled, requiresInputFiles, enableTelnet);
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

    String createContainerMetadata(OciContainerConfiguration config, boolean isDHCPenabled, boolean requiresInputFiles, boolean enableTelnet) throws BWFLAException {
        ArrayList<String> args = new ArrayList<String>();
        ContainerMetadata metadata = new ContainerMetadata();
        final String inputDir = "container-input";
        final String outputDir = "container-output";
        metadata.setDhcp(isDHCPenabled);
        metadata.setTelnet(true);
        metadata.setProcess("/bin/sh");
        args.add("-c");
        args.add("mkdir " + outputDir
                + " && emucon-cgen --enable-extensive-caps --disable-network-namespace \"$@\"; runc run eaas-job | tee "
                + outputDir + "/container-log-" + UUID.randomUUID() + ".log");
        args.add("");

        args.add("--output");
        args.add("config.json");


        if(requiresInputFiles) {
            args.add("--mount");
            args.add(getMountStr(inputDir, config.getInput(), true));
        }
        if(config.getOutputPath() != null) {
            args.add("--mount");
            args.add(getMountStr(outputDir, config.getOutputPath(), false));
        }

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

        if(config.getProcess().getWorkingDir() != null)
        {
            args.add("--workdir");
            args.add(config.getProcess().getWorkingDir());
        }

        // Add emulator's command
        args.add("--");
        for (String arg : config.getProcess().getArguments())
            args.add(arg);

        metadata.setArgs(args);

        return metadata.jsonValueWithoutRoot(true);
    }


    public ImageDescription prepareContainerRuntimeImage(OciContainerConfiguration config, LinuxRuntimeContainerReq linuxRuntime, ArrayList<ComponentWithExternalFilesRequest.InputMedium> inputMedia) throws IOException, BWFLAException {
        if (inputMedia.size() > 1)
            throw new BWFLAException("Size of Input drives cannot exceed 1");

        final FileSystemType fileSystemType = FileSystemType.EXT4;

        if(linuxRuntime.getUserEnvironment() != null && linuxRuntime.getUserEnvironment().size() > 0)
            config.getProcess().getEnvironmentVariables().addAll(linuxRuntime.getUserEnvironment());

        int sizeInMb = 1024;
        if(inputMedia.size() > 0 && inputMedia.get(0).getSizeInMb() >= 0)
            sizeInMb = inputMedia.get(0).getSizeInMb();

        final ImageDescription description = new ImageDescription()
                .setMediumType(MediumType.HDD)
                .setPartitionTableType(PartitionTableType.NONE)
                .setPartitionStartBlock(0)
                .setFileSystemType(fileSystemType)
                .setLabel("eaas-job")
                .setSizeInMb(sizeInMb);

        boolean requiersInputFiles = false;
        if(inputMedia.size() > 0 && inputMedia.get(0).getExtFiles().size() > 0)
            requiersInputFiles = true;

        BlobHandle mdBlob = prepareMetadata(config, linuxRuntime.isDHCPenabled(), requiersInputFiles, linuxRuntime.isTelnetEnabled());

        final ImageContentDescription metadataEntry = new ImageContentDescription();
        metadataEntry.setAction(ImageContentDescription.Action.COPY)
                .setUrlDataSource(new URL(mdBlob.toRestUrl(blobStoreRestAddress)))
                .setName("metadata.json");
        description.addContentEntry(metadataEntry);

        if(inputMedia.size() > 0) {
            ComponentWithExternalFilesRequest.InputMedium medium = inputMedia.get(0);
            for (ComponentWithExternalFilesRequest.FileURL extfile : medium.getExtFiles()) {
                final URL url = new URL(extfile.getUrl());
                final ImageContentDescription entry = new ImageContentDescription()
                        .setAction(extfile.getAction())
                        .setArchiveFormat(ImageContentDescription.ArchiveFormat.TAR)
                        .setUrlDataSource(url)
                        .setSubdir("container-input");

                if (extfile.hasName())
                    entry.setName(extfile.getName());
                else entry.setName(Components.getFileName(url));

                description.addContentEntry(entry);
            }

            for (ComponentWithExternalFilesRequest.FileData inlfile : medium.getInlineFiles()) {
                final ImageContentDescription entry = new ImageContentDescription()
                        .setAction(inlfile.getAction())
                        .setArchiveFormat(inlfile.getCompressionFormat())
                        .setName(inlfile.getName())
                        .setByteArrayDataSource(inlfile.getData())
                        .setSubdir("container-input");

                description.addContentEntry(entry);
            }
        }

        return description;
    }



}
