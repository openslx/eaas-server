package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.METS.MetsUtil;
import de.bwl.bwfla.emil.datatypes.rest.ImportObjectRequest;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import gov.loc.mets.Mets;

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
            properties.filename = info.getFilename();
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
