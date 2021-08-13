package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.ImageMetadata;
import de.bwl.bwfla.api.imagebuilder.DockerImport;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderResult;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageRequest;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageResult;
import de.bwl.bwfla.emil.utils.BuildContainerUtil;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.MediumType;
import de.bwl.bwfla.emucomp.api.PartitionTableType;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import org.apache.tamaya.ConfigurationProvider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BuildContainerImageTask extends BlockingTask<Object> {

    private final CreateContainerImageRequest request;

    public BuildContainerImageTask(CreateContainerImageRequest request) {
        this.request = request;
        System.out.println("... build container task...");
    }

    @Override
    protected Object execute() throws Exception {
        if (request.getUrlString() == null) {
            return new BWFLAException("invalid url: " + request.getUrlString());
        }

        if (request.getContainerType() == null)
            return new BWFLAException("missing image type");

        try {
            return BuildContainerUtil.build(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return e;
        }
    }
}
