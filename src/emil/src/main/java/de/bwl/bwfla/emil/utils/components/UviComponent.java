package de.bwl.bwfla.emil.utils.components;

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.Components;
import de.bwl.bwfla.emil.datatypes.rest.ComponentWithExternalFilesRequest;
import de.bwl.bwfla.emil.datatypes.rest.MachineComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.UviComponentRequest;
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

@ApplicationScoped
public class UviComponent {

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
    public void init() {
        try {
            this.blobstore = blobStoreClient.getBlobStorePort(blobStoreWsAddress);
        } catch (BWFLAException e) {
            e.printStackTrace();
            throw new RuntimeException("Constructing web-services failed!", e);
        }
    }

    BlobHandle createAutostart(String filename, String applicationName) throws BWFLAException {
        String autostart = "[autorun]\r\n" + "open=start " + "\"" + filename + "\"";
        File tmpfile = null;
        try {
            tmpfile = File.createTempFile("metadata.json", null, null);
            Files.write(tmpfile.toPath(), autostart.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BWFLAException(e);
        }

        BlobDescription blobDescription = new BlobDescription();
        blobDescription.setDataFromFile(tmpfile.toPath())
                .setNamespace("random")
                .setDescription("random")
                .setName("metadata")
                .setType(".json");

        return blobstore.put(blobDescription);
    }

    public MachineComponentRequest createUVIComponent(UviComponentRequest request) throws BWFLAException {
        ArrayList<ComponentWithExternalFilesRequest.InputMedium> media;

        BlobHandle blobHandle = createAutostart(request.getUviFilename(), null);

        ComponentWithExternalFilesRequest.InputMedium m = new ComponentWithExternalFilesRequest.InputMedium();
        m.setMediumType(MediumType.CDROM);

        ComponentWithExternalFilesRequest.FileURL inputFile =
                new ComponentWithExternalFilesRequest.FileURL("copy",
                        request.getUviUrl(), request.getUviFilename());
        ComponentWithExternalFilesRequest.FileURL autoRun =
                new ComponentWithExternalFilesRequest.FileURL("copy", blobHandle.toRestUrl(blobStoreRestAddress, false),
                        "autorun.inf");

        m.getExtFiles().add(inputFile);
        m.getExtFiles().add(autoRun);

        request.getInputMedia().add(m);
        return (MachineComponentRequest)request;
    }
}
