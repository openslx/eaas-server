package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.taskmanager.CompletableTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageResult;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emil.utils.ImportEmulatorUtil;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import org.apache.tamaya.ConfigurationProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class ImportEmulatorTask extends BlockingTask<Object> {

    private final DatabaseEnvironmentsAdapter envHelper;
    private final ImportEmulatorRequest emulatorRequest;

    public ImportEmulatorTask(ImportEmulatorRequest emulatorRequest, DatabaseEnvironmentsAdapter envHelper)
    {
        this.envHelper = envHelper;
        this.emulatorRequest = emulatorRequest;
    }

    @Override
    protected Object execute() throws Exception {
        ImportEmulatorUtil.doImport(emulatorRequest, envHelper);
        return null;
    }
}
