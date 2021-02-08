package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.ImageMetadata;
import de.bwl.bwfla.api.imagebuilder.DockerImport;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderResult;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageRequest;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageResult;
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

    private final Duration imageBuilderDelay = Duration.ofSeconds(30);
    private final Duration imageBuilderTimeout = Duration.ofHours(1);

    private final String blobStoreRestAddress;
    private final String imageBuilderAddress;
    private final String imageArchiveHost;

    public BuildContainerImageTask(CreateContainerImageRequest request) {
        this.request = request;
        blobStoreRestAddress = ConfigurationProvider.getConfiguration().get("rest.blobstore");
        imageBuilderAddress = ConfigurationProvider.getConfiguration().get("ws.imagebuilder");
        imageArchiveHost = ConfigurationProvider.getConfiguration().get("ws.imagearchive");
        System.out.println("... build container task...");
    }

    private ImageBuilderResult createImageFromDescription(ImageDescription description) throws BWFLAException
    {
        final ImageBuilder imagebuilder = ImageBuilderClient.get().getImageBuilderPort(imageBuilderAddress);
        return ImageBuilderClient.build(imagebuilder, description, imageBuilderTimeout, imageBuilderDelay);
    }

    private ImageDescription defaultContainerImage() {
        return new ImageDescription()
                .setMediumType(MediumType.HDD)
                .setPartitionTableType(PartitionTableType.NONE)
                .setFileSystemType(FileSystemType.EXT4)
                .setLabel("container")
                .setSizeInMb(1024 * 10); // 10 Gb virtual size
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

        dockerDataSource.imageArchiveHost = imageArchiveHost;
        dockerDataSource.digest = digest;
        entry.setDataSource(dockerDataSource);
        description.addContentEntry(entry);
        ImageBuilderResult result = this.createImageFromDescription(description);

        return result;
    }

    private CreateContainerImageResult createImage(CreateContainerImageRequest req) throws BWFLAException, MalformedURLException {
        ImageBuilderResult result = null;
        URL imageUrl = null;

        System.out.println("creating image...");

        switch(req.getContainerType())
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
                throw new BWFLAException("unknown imageType " + req.getContainerType());
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

        CreateContainerImageResult containerImageResult = new CreateContainerImageResult();
        containerImageResult.setContainerUrl(imageUrl.toString());
        CreateContainerImageResult.ContainerImageMetadata  md = new CreateContainerImageResult.ContainerImageMetadata();
        containerImageResult.setMetadata(md);
        
        if(result != null && req.getContainerType() == CreateContainerImageRequest.ContainerType.DOCKERHUB)
        {
            DockerImport dockerImport = (DockerImport)result.getMetadata();
            if(dockerImport != null)
            {
                containerImageResult.getMetadata().setTag(dockerImport.getTag());
                containerImageResult.getMetadata().setContainerDigest(dockerImport.getDigest());
                containerImageResult.getMetadata().setContainerSourceUrl(dockerImport.getImageRef());
                containerImageResult.getMetadata().setEntryProcesses(dockerImport.getEntryProcesses());
                containerImageResult.getMetadata().setEnvVariables(dockerImport.getEnvVariables());
                containerImageResult.getMetadata().setWorkingDir(dockerImport.getWorkingDir());

                /* deprecated --- compatlayer */
                containerImageResult.getMetadata().setEmulatorType(dockerImport.getEmulatorType());
                containerImageResult.getMetadata().setEmulatorVersion(dockerImport.getEmulatorVersion());
            }
            // TODO: other fmts...
        }

        return containerImageResult;
    }

    @Override
    protected Object execute() throws Exception {
        if (request.getUrlString() == null) {
            return new BWFLAException("invalid url: " + request.getUrlString());
        }

        if (request.getContainerType() == null)
            return new BWFLAException("missing image type");

        try {
            return createImage(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return e;
        }
    }
}
