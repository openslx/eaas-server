package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.datatypes.snapshot.SnapshotRequest;
import de.bwl.bwfla.emil.utils.Snapshot;

public class CreateSnapshotTask extends BlockingTask<Object> {

    private final Snapshot snapshot;
    private final UserContext userContext;
    private final String componentId;
    private SnapshotRequest request;
    private boolean checkpoint;

    public CreateSnapshotTask(Snapshot snapshot, String componentId, SnapshotRequest request, boolean checkpoint, UserContext userContext)
    {
        this.request = request;
        this.componentId = componentId;
        this.checkpoint = checkpoint;
        this.userContext = userContext;
        this.snapshot = snapshot;
    }

    @Override
    protected Object execute() throws Exception {
        try {
            return snapshot.handleSnapshotRequest(componentId, request, checkpoint, userContext);
        }
        catch(Exception e) {
            return new BWFLAException(e);
        }
    }
}
