package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.CompletableTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
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

        public DatabaseEnvironmentsAdapter environmentHelper;

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
        try {
            ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
            iaMd.setType(request.type);

            final TaskState importState = request.environmentHelper.importImage(request.url, iaMd, true);
            final ImportStatePoller poller = new ImportStatePoller(importState);
            this.schedule(poller);

            final BiFunction<TaskState, Throwable, Object> handler = (state, error) -> {
                if (error != null)
                    return (error instanceof BWFLAException) ? error : new BWFLAException(error);

                if (state.isFailed())
                    return new BWFLAException("task failed");

                final String imageId = importState.getResult();

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

            return poller.completion()
                    .handle(handler);
        }
        catch (BWFLAException error) {
            log.log(Level.SEVERE, "Importing image failed!", error);
            return CompletableFuture.completedFuture(error);
        }
    }

    private class ImportStatePoller implements Runnable
    {
        private final CompletableFuture<TaskState> completion;
        private TaskState state;

        public ImportStatePoller(TaskState state)
        {
            this.completion = new CompletableFuture<>();
            this.state = state;
        }

        public CompletableFuture<TaskState> completion()
        {
            return completion;
        }

        @Override
        public void run()
        {
            try {
                state = request.environmentHelper.getState(state.getTaskId());
            }
            catch (Exception error) {
                completion.completeExceptionally(error);
                return;
            }

            if (state.isDone()) {
                completion.complete(state);
                return;
            }

            // Import is not finished, retry later...
            ImportImageTask.this.schedule(this);
        }
    }

    private void schedule(Runnable task)
    {
        CompletableFuture.delayedExecutor(2L, TimeUnit.SECONDS, this.executor())
                .execute(task);
    }
}
