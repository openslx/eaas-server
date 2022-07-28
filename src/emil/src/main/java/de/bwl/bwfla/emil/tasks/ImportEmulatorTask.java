package de.bwl.bwfla.emil.tasks;

import com.openslx.eaas.imagearchive.client.endpoint.ImageArchive;
import com.openslx.eaas.imagearchive.client.endpoint.v2.util.EmulatorMetaHelperV2;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emil.utils.ImportEmulatorUtil;

import java.util.logging.Logger;


public class ImportEmulatorTask extends BlockingTask<Object> {


    private final ImageArchive archive;
    private final EmulatorMetaHelperV2 emuMetaHelper;
    private final ImportEmulatorRequest emulatorRequest;
    private final Logger log;

    public ImportEmulatorTask(ImportEmulatorRequest emulatorRequest, EmulatorMetaHelperV2 emuMetaHelper,
                              ImageArchive archive, Logger log)
    {
        this.archive = archive;
        this.emuMetaHelper = emuMetaHelper;
        this.emulatorRequest = emulatorRequest;
        this.log = log;
    }

    @Override
    protected Object execute() throws Exception {
        ImportEmulatorUtil.execute(emulatorRequest, emuMetaHelper, archive, log);
        return null;
    }
}
