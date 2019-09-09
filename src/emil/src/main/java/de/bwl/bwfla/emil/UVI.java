package de.bwl.bwfla.emil;

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.rest.ComponentWithExternalFilesRequest;
import de.bwl.bwfla.emil.datatypes.rest.MachineComponentRequest;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.MediumType;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

@Path("/uvi")
@ApplicationScoped
public class UVI {

    @Inject
    private Components components;

    @Inject
    @Config(value = "ws.blobstore")
    private String blobStoreWsAddress;

    @Inject
    @Config(value = "rest.blobstore")
    private String blobStoreRestAddress;

    private BlobStore blobstore;

    @Inject
    private BlobStoreClient blobStoreClient;

    @PostConstruct
    public void init()
    {
        this.blobstore = blobStoreClient.getBlobStorePort(blobStoreWsAddress);
    }

    BlobHandle createAutostart(String filename, String applicationName) throws IOException, BWFLAException {
        String autostart = "[autorun]\n\r" + "open=start " + filename;
        File tmpfile = File.createTempFile("metadata.json", null, null);
        Files.write(tmpfile.toPath(), autostart.getBytes(), StandardOpenOption.CREATE);

        BlobDescription blobDescription = new BlobDescription();
        blobDescription.setDataFromFile(tmpfile.toPath())
                .setNamespace("random")
                .setDescription("random")
                .setName("metadata")
                .setType(".json");

        return blobstore.put(blobDescription);
    }

    @Path("/")
    public Response createUVIComponent(MachineComponentRequest request, url )
    {
        ArrayList<ComponentWithExternalFilesRequest.InputMedium> media;
        if(request.getInputMedia() == null)
        {
            media = new ArrayList<>();
        }

        BlobHandle blobHandle = createAutostart(filename, null);

        ComponentWithExternalFilesRequest.InputMedium m = new ComponentWithExternalFilesRequest.InputMedium();
        m.setMediumType(MediumType.CDROM);

        ComponentWithExternalFilesRequest.FileURL inputFile = new ComponentWithExternalFilesRequest.FileURL("copy", url, filename);
        ComponentWithExternalFilesRequest.FileURL autoRun = new ComponentWithExternalFilesRequest.FileURL("copy", blobHandle.toRestUrl(blobStoreRestAddress, false), "autorun.inf");
    }
}
