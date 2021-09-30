package de.bwl.bwfla.emil.tasks;

import com.openslx.eaas.imagearchive.client.endpoint.v2.util.EmulatorMetaHelperV2;
import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emil.utils.ImportEmulatorUtil;


public class ImportEmulatorTask extends BlockingTask<Object> {

    private final EmulatorMetaHelperV2 emuMetaHelper;
    private final DatabaseEnvironmentsAdapter envHelper;
    private final ImportEmulatorRequest emulatorRequest;

    public ImportEmulatorTask(ImportEmulatorRequest emulatorRequest, EmulatorMetaHelperV2 emuMetaHelper, DatabaseEnvironmentsAdapter envHelper)
    {
        this.emuMetaHelper = emuMetaHelper;
        this.envHelper = envHelper;
        this.emulatorRequest = emulatorRequest;
    }

    @Override
    protected Object execute() throws Exception {
        ImportEmulatorUtil.doImport(emulatorRequest, emuMetaHelper, envHelper);
        return null;
    }
}
