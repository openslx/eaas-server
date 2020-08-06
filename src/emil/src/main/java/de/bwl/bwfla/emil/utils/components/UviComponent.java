package de.bwl.bwfla.emil.utils.components;

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.Components;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.ComponentWithExternalFilesRequest;
import de.bwl.bwfla.emil.datatypes.rest.MachineComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.UviComponentRequest;

import de.bwl.bwfla.emil.utils.AutoRunScripts;
import de.bwl.bwfla.emucomp.api.*;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;


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

    @Inject
    private DatabaseEnvironmentsAdapter envHelper;

    private BlobStore blobstore;

    @Inject
    private BlobStoreClient blobStoreClient;

    @Inject
    private AutoRunScripts scripts = null;

    @PostConstruct
    public void init() {
        try {
            this.blobstore = blobStoreClient.getBlobStorePort(blobStoreWsAddress);
        } catch (BWFLAException e) {
            e.printStackTrace();
            throw new RuntimeException("Constructing web-services failed!", e);
        }
    }

    private BlobHandle createAutostart(AutoRunScripts.Template template, String filename)
            throws BWFLAException
    {
        final Map<String, Object> context = new HashMap<>();
        context.put(AutoRunScripts.Variables.FILENAME, filename);

        File tmpfile = null;
        try {
            final Writer autostart = new StringWriter();
            template.evaluate(autostart, context);

            tmpfile = File.createTempFile("metadata.json", null, null);
            Files.write(tmpfile.toPath(), autostart.toString().getBytes(), StandardOpenOption.CREATE);
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
        Environment chosenEnv = envHelper.getEnvironmentById(request.getArchive(), request.getEnvironment());
        MachineConfiguration config = (MachineConfiguration)chosenEnv;

        String osId = config.getOperatingSystemId();
        if(osId == null)
            osId = "UNKNOWN";

        final MediumType mtype = (request.isUviWriteable()) ? MediumType.HDD : MediumType.CDROM;

        final AutoRunScripts.Template template = scripts.lookup(osId, mtype);
        if (template == null)
            throw new BWFLAException("No autorun-script template found for " + osId + "+" + mtype.name() + "!");

        BlobHandle blobHandle = createAutostart(template, request.getUviFilename());

        ComponentWithExternalFilesRequest.InputMedium medium = new ComponentWithExternalFilesRequest.InputMedium();
        medium.setMediumType(mtype);
        if (mtype == MediumType.HDD) {
            medium.setPartitionTableType(PartitionTableType.MBR);
            medium.setFileSystemType(FileSystemType.FAT32);
        }

        ComponentWithExternalFilesRequest.FileURL inputFile =
                new ComponentWithExternalFilesRequest.FileURL("copy", request.getUviUrl(), request.getUviFilename());

        medium.getExtFiles()
                .add(inputFile);

        ComponentWithExternalFilesRequest.FileURL autoRunScript =
                new ComponentWithExternalFilesRequest.FileURL("copy", blobHandle.toRestUrl(blobStoreRestAddress, false),
                            template.getFileName());

        medium.getExtFiles()
                .add(autoRunScript);

        for(UviComponentRequest.UviFile auxFile : request.getAuxFiles())
        {
            ComponentWithExternalFilesRequest.FileURL _inputFile =
                    new ComponentWithExternalFilesRequest.FileURL("copy",
                            auxFile.getUrl(), auxFile.getFilename());

            medium.getExtFiles()
                    .add(_inputFile);
        }

        request.getInputMedia()
                .add(medium);

        return (MachineComponentRequest)request;
    }
}
