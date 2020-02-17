package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportImageTask extends AbstractTask<Object> {
    private ImportImageTaskRequest request;
    private Logger log;

    public ImportImageTask(ImportImageTaskRequest request, Logger log)
    {
       this.request = request;
       this.log = log;
    }

    public static class ImportImageTaskRequest
    {
        public String label;
        public URL url;
        public ImageType type;

        public DatabaseEnvironmentsAdapter environmentHelper;

        public String destArchive;

        public void validate() throws BWFLAException
        {
            if(url == null || destArchive == null)
                throw new BWFLAException("ImportImageTaskRequest: input validation failed");

            if(environmentHelper == null )
                throw new BWFLAException("ImportImageTaskRequest: missing dependencies");

            if(!type.equals(ImageType.USER) && !type.equals(ImageType.ROMS))
                throw new BWFLAException("Only ImageType ROMS / USER are supported: " + type);
        }
    }

    @Override
    protected Object execute() throws Exception {
        try {
            ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
            iaMd.setType(request.type);

            TaskState importState = request.environmentHelper.importImage(request.url, iaMd, true);
            while(!importState.isDone())
            {
                importState = request.environmentHelper.getState(importState.getTaskId());
            }
            if(importState.isFailed())
            {
                return new BWFLAException("task failed");
            }

            Map<String, String> userData = new HashMap<>();
            String imageId = importState.getResult();
            userData.put("imageId", imageId);

            ImageMetadata entry = new ImageMetadata();
            entry.setName(imageId);
            entry.setLabel(request.label);
            ImageDescription description = new ImageDescription();
            description.setType(request.type.value());
            description.setId(imageId);
            entry.setImage(description);

            request.environmentHelper.addNameIndexesEntry(request.destArchive, entry,null);

            return userData;
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new BWFLAException(e);
        }
    }
}
