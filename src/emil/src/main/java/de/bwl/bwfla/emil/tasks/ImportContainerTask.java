package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.ImportContainerRequest;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emil.utils.ContainerUtil;

import java.util.HashMap;
import java.util.Map;

public class ImportContainerTask extends BlockingTask<Object>
{

    private final ContainerUtil containerUtil;
    private final DatabaseEnvironmentsAdapter envHelper;
    private final ImportContainerRequest containerRequest;

    public ImportContainerTask(ImportContainerRequest containerRequest, ContainerUtil containerUtil, DatabaseEnvironmentsAdapter envHelper) {
        this.containerRequest = containerRequest;
        this.containerUtil = containerUtil;
        this.envHelper = envHelper;
    }

    @Override
    protected Object execute() throws Exception {
        if (containerRequest.getUrlString() == null && !(containerRequest instanceof ImportEmulatorRequest)) {
            return new BWFLAException("invalid url: " + containerRequest.getUrlString());
        }
        if ((containerRequest.getProcessArgs() == null || containerRequest.getProcessArgs().size() == 0) &&
                containerRequest.getImageType() != ImportContainerRequest.ContainerImageType.DOCKERHUB &&
                !(containerRequest instanceof ImportEmulatorRequest)) {
            return new BWFLAException("missing process args");
        }

        if (containerRequest.getImageType() == null)
            return new BWFLAException("missing image type");


        if (containerRequest instanceof ImportEmulatorRequest) {

            ImportEmulatorRequest request = (ImportEmulatorRequest) containerRequest;
            containerUtil.importEmulator(request);
            return new HashMap<>();
        } else {
            String newEnvironmentId = containerUtil.importContainer(containerRequest);
            Map<String, String> userData = new HashMap<>();
            envHelper.sync();
            userData.put("environmentId", newEnvironmentId);
            return userData;
        }
    }
}
