package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.InputStreamDataSource;
import de.bwl.bwfla.common.utils.METS.MetsUtil;
import de.bwl.bwfla.emil.datatypes.rest.ImportObjectRequest;
import de.bwl.bwfla.emil.datatypes.rest.TaskStateResponse;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import gov.loc.mets.Mets;
import org.apache.tamaya.ConfigurationProvider;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class ImportObjectTask extends BlockingTask<Object>
{

    private final ImportObjectRequest req;
    private final String archiveId;
    private final ObjectArchiveHelper objectArchiveHelper;

    public ImportObjectTask(ImportObjectRequest req, String archiveId, ObjectArchiveHelper objectArchiveHelper)
    {
        this.req = req;
        this.archiveId = archiveId;
        this.objectArchiveHelper = objectArchiveHelper;
    }

    private URL uploadToBlobstore(Path filepath) throws BWFLAException {
        final BlobDescription blob = new BlobDescription()
            .setDescription("upload")
            .setNamespace("user-upload")
            .setDataFromFile(filepath)
            .setName(UUID.randomUUID().toString());

        String blobStoreWsAddress  = ConfigurationProvider.getConfiguration().get("ws.blobstore");
        assert blobStoreWsAddress != null;

        String blobStoreRestAddress = ConfigurationProvider.getConfiguration().get("ws.blobstore");
        assert blobStoreRestAddress != null;

        BlobHandle handle = BlobStoreClient.get()
                        .getBlobStorePort(blobStoreWsAddress)
                        .put(blob);
        try {
            return new URL(handle.toRestUrl(blobStoreRestAddress));
        } catch (MalformedURLException e) {
            throw new BWFLAException(e);
        }
    }

    @Override
    protected Object execute() throws Exception {


        Mets m = MetsUtil.createMets(UUID.randomUUID().toString(), req.getLabel());
        for(ImportObjectRequest.ImportFileInfo info: req.getFiles()) {
            MetsUtil.FileTypeProperties properties = new MetsUtil.FileTypeProperties();
            // temp hack
            if(info.getDeviceId().equals("Q82753")) {
                properties.deviceId = null;
                properties.fileFmt = info.getDeviceId();
            }
            else {
                properties.deviceId = info.getDeviceId();
                properties.fileFmt = info.getFileFmt();
            }
            if(info.getFilename() != null)
                properties.filename = info.getFilename();
            else
                properties.filename = UUID.randomUUID().toString();

            URL url;
            try {
                url = new URL(info.getUrl());
                if(url.getProtocol().equalsIgnoreCase("file"))
                    throw new BWFLAException("invalid url format " + info.getUrl() + " , will not import object from that source.");
            } catch (MalformedURLException me) {
                log.info("Object url does not point to a HTTP(S) url, trying local import");
                String filename = info.getUrl();
                if (filename == null || filename.contains("/"))
                   throw new BWFLAException("filename must not be null/empty or contain '/' characters: " + filename);
                File image = new File("/eaas/import/", filename);
                if (!image.exists())
                    throw new BWFLAException( filename + " not found.");
                properties.filename = filename;
                info.setUrl(uploadToBlobstore(image.toPath()).toString());
            }

            MetsUtil.addFile(m, info.getUrl(), properties);
        }
        try {
            objectArchiveHelper.importFromMetadata(archiveId, m.toString());
        } catch (BWFLAException e) {
            e.printStackTrace();
            return new BWFLAException(e);
        }

        final Map<String, String> userdata = new TreeMap<>();
        userdata.put("objectId", m.getID());
        return userdata;
    }
}
