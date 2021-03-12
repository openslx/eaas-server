package de.bwl.bwfla.emil.tasks;

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportRequestV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportStatusV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportTargetV2;
import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.CompletableTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class ImportImageTask extends CompletableTask<Object>
{
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

        @Deprecated
        public DatabaseEnvironmentsAdapter environmentHelper;
        public ImageArchiveClient imagearchive;
        public String destArchive;

        public void validate() throws BWFLAException
        {
            if(url == null || destArchive == null)
                throw new BWFLAException("ImportImageTaskRequest: input validation failed");

            if(environmentHelper == null )
                throw new BWFLAException("ImportImageTaskRequest: missing dependencies");

            if(!type.equals(ImageType.USER) && !type.equals(ImageType.ROMS) && !type.equals(ImageType.RUNTIME))
                throw new BWFLAException("Only ImageType ROMS / USER / RUNTIME are supported: " + type);
        }
    }

    @Override
    protected CompletableFuture<Object> execute() throws Exception
    {
        final var ireq = new ImportRequestV2();
        ireq.source()
                .setUrl(request.url.toString());

        final var target = ireq.target()
                .setLocation(request.destArchive);

        switch (request.type) {
            case ROMS:
                target.setKind(ImportTargetV2.Kind.ROM);
                break;
            default:
                target.setKind(ImportTargetV2.Kind.IMAGE);
                break;
        }

        final BiFunction<ImportStatusV2, Throwable, Object> handler = (status, error) -> {
            if (error != null)
                return (error instanceof BWFLAException) ? error : new BWFLAException(error);

            switch (status.state()) {
                case ABORTED:
                    return new BWFLAException("Image import was aborted!");

                case FAILED:
                    final var failure = status.failure();
                    if (failure != null && failure.detail() != null)
                        log.warning("Importing image failed! " + failure.detail());

                    return new BWFLAException("Importing image failed!");
            }

            final String imageId = status.target()
                    .name();

            try {
                ImageMetadata entry = new ImageMetadata();
                entry.setName(imageId);
                entry.setLabel(request.label);
                ImageDescription description = new ImageDescription();
                description.setType(request.type.value());
                description.setId(imageId);
                entry.setImage(description);

                request.environmentHelper.addNameIndexesEntry(request.destArchive, entry, null);
            }
            catch (BWFLAException exception) {
                return exception;
            }

            Map<String, String> userData = new HashMap<>();
            userData.put("imageId", imageId);
            return userData;
        };

        final var archive = request.imagearchive;
        final var taskid = archive.api()
                .v2()
                .imports()
                .insert(ireq);

        return archive.api()
                .v2()
                .imports()
                .watch(taskid)
                .handle(handler);
    }
}
