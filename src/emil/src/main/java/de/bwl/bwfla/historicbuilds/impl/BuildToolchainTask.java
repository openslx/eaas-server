package de.bwl.bwfla.historicbuilds.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.common.taskmanager.TaskManager;
import de.bwl.bwfla.emil.Components;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.rest.ComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.ComponentResponse;
import de.bwl.bwfla.emil.datatypes.rest.ContainerComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.MachineComponentRequest;
import de.bwl.bwfla.emil.utils.components.ContainerComponent;
import de.bwl.bwfla.historicbuilds.HistoricBuilds;
import de.bwl.bwfla.historicbuilds.api.BuildToolchainRequest;
import de.bwl.bwfla.historicbuilds.api.BuildToolchainTaskResponse;
import de.bwl.bwfla.historicbuilds.api.SoftwareHeritageTaskResponse;
import de.bwl.bwfla.restutils.ResponseUtils;

import javax.ws.rs.core.Response;
import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class BuildToolchainTask extends BlockingTask<Object> {


    private final String environmentID;
    private final String inputDirectory;
    private final String outputDirectory;
    private final String execFile;
    private final String[] prerequisites;
    private final String mail;
    private final String mode;

    private final Components components;
    private final String softwareHeritageTaskId;
    private final TaskManager taskManager;
    private final String envType;

    private static final Logger LOG = Logger.getLogger("BUILD-TOOLCHAIN-TASK");

    public BuildToolchainTask(BuildToolchainRequest request, Components components, String softwareHeritageTaskId, TaskManager taskManager, String envType) {

        this.environmentID = request.getEnvironmentID();
        this.inputDirectory = request.getInputDirectory();
        this.outputDirectory = request.getOutputDirectory();
        this.execFile = request.getExecFile();
        this.prerequisites = request.getPrerequisites();
        this.mail = request.getMail();
        this.mode = request.getMode(); // TODO probably use 2 classes that inherit from this class for the different modes

        this.components = components;
        this.softwareHeritageTaskId = softwareHeritageTaskId;
        this.taskManager = taskManager;
        this.envType = envType;

    }


    @Override
    protected BuildToolchainTaskResponse execute() throws Exception {

        boolean isSoftwareHeritageTaskDone = false;
        TaskInfo info = null;

        while (!isSoftwareHeritageTaskDone) {

            info = taskManager.lookup(softwareHeritageTaskId);
            if (info.result().isDone()) {
                isSoftwareHeritageTaskDone = true;
            } else {
                Thread.sleep(10000);
            }
        }

        LOG.info("SoftwareHeritageTask is done, resuming with the build process.");

        SoftwareHeritageTaskResponse swhData = (SoftwareHeritageTaskResponse) info.result().get();
        //TODO remove id from taskmgr?

        String swhPath = swhData.getPath();
        LOG.info("Will use Data present at" + swhPath);
        boolean isExtracted = swhData.isExtracted();
        ComponentResponse componentResponse = null;

        if (envType.equals("base")) {
            MachineComponentRequest componentRequest = new MachineComponentRequest();
            componentRequest.setEnvironment(environmentID);
            //TODO inject Data, by using swhPath to access SWH Data
            //TODO check if error should be thrown when extract is true and envType is machine
            componentResponse = components.createComponent(componentRequest);

        } else if (envType.equals("container")) {

            ContainerComponentRequest componentRequest = new ContainerComponentRequest();
            //TODO create mapping from swhPath -> inputFolder
            componentResponse = components.createComponent(componentRequest);
        } else {
            LOG.warning("Got unsupported environment type."); //TODO throw exception
            throw new BWFLAException("Got unsupported environment type.");
        }

        String startedMachineId = componentResponse.getId();

        BuildToolchainTaskResponse response = new BuildToolchainTaskResponse();
        response.setId(startedMachineId);
        response.setEnvType(envType);

        return response;
    }

}