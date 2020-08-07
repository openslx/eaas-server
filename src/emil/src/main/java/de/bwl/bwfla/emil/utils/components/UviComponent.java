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

import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
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

        final Writer writer = new StringWriter();
        try {
            template.evaluate(writer, context);

        }
        catch (IOException error) {
            throw new BWFLAException("Rendering autorun-script failed!", error);
        }

        BlobDescription blob = new BlobDescription()
                .setData(new StringDataSource(writer.toString()))
                .setNamespace("autorun-scripts")
                .setDescription("Rendered autorun-script")
                .setName("metadata")
                .setType(".json");

        return blobstore.put(blob);
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

    private static class StringDataSource implements DataSource
    {
        private final byte[] data;


        public StringDataSource(String data)
        {
            this(data.getBytes());
        }

        public StringDataSource(byte[] data)
        {
            this.data = data;
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
            return new ByteArrayInputStream(data);
        }

        @Override
        public OutputStream getOutputStream() throws IOException
        {
            return null;
        }

        @Override
        public String getContentType()
        {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        @Override
        public String getName()
        {
            return null;
        }
    }
}
