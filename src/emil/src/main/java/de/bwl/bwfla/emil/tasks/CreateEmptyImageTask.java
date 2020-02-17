package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderResult;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.configuration.converters.DurationPropertyConverter;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.MediumType;
import de.bwl.bwfla.emucomp.api.PartitionTableType;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import org.apache.tamaya.ConfigurationProvider;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CreateEmptyImageTask extends AbstractTask<Object> {

    private static final String imageBuilderAddress = ConfigurationProvider.getConfiguration().get("ws.imagebuilder");
    private static final Duration imageBuilderTimeout = DurationPropertyConverter.parse(ConfigurationProvider.getConfiguration().get("emil.containerdata.imagebuilder.timeout"));
    private static final Duration imageBuilderDelay = DurationPropertyConverter.parse(ConfigurationProvider.getConfiguration().get("emil.containerdata.imagebuilder.delay"));
    private static final String blobStoreRestAddress = ConfigurationProvider.getConfiguration().get("rest.blobstore");

    private int sizeInMb;
    private Logger log;

    public CreateEmptyImageTask(int sizeInMb, Logger log)
    {
        this.sizeInMb = sizeInMb;
        this.log = log;
    }

    @Override
    protected Object execute() throws Exception {
        final ImageBuilder imagebuilder = ImageBuilderClient.get().getImageBuilderPort(imageBuilderAddress);

        de.bwl.bwfla.imagebuilder.api.ImageDescription descr = new de.bwl.bwfla.imagebuilder.api.ImageDescription();
        descr.setMediumType(MediumType.HDD)
                .setPartitionTableType(PartitionTableType.NONE)
                .setFileSystemType(FileSystemType.RAW)
                .setSizeInMb(sizeInMb);

        ImageBuilderResult result = ImageBuilderClient.build(imagebuilder, descr, imageBuilderTimeout, imageBuilderDelay);

        if(result.getBlobHandle() == null)
            throw new BWFLAException("Image blob unavailable");

        URL imageUrl = new URL(result.getBlobHandle().toRestUrl(blobStoreRestAddress, false));
        Map<String, String> userData = new HashMap<>();
        userData.put("imageUrl", imageUrl.toString());

        return userData;
    }
}
