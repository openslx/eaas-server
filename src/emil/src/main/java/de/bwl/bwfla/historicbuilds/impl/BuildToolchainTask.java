package de.bwl.bwfla.historicbuilds.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.Components;
import de.bwl.bwfla.emil.datatypes.rest.ComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.ComponentResponse;
import de.bwl.bwfla.emil.datatypes.rest.ContainerComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.MachineComponentRequest;
import de.bwl.bwfla.emil.utils.components.ContainerComponent;
import de.bwl.bwfla.historicbuilds.api.BuildToolchainRequest;

import java.nio.file.Path;
import java.util.logging.Logger;

public class BuildToolchainTask extends BlockingTask<Object> {


    final private String environmentID;
    final private String inputDirectory;
    final private String outputDirectory;
    final private String execFile;
    final private String[] prerequisites;
    final private String mail;
    final private String mode;
    final private Components components;

    private static final Logger LOG = Logger.getLogger("BUILD-TOOLCHAIN-TASK");

    public BuildToolchainTask(BuildToolchainRequest request, Components components) {

        this.environmentID = request.getEnvironmentID();
        this.inputDirectory = request.getInputDirectory();
        this.outputDirectory = request.getOutputDirectory();
        this.execFile = request.getExecFile();
        this.prerequisites = request.getPrerequisites();
        this.mail = request.getMail();
        this.mode = request.getMode(); // TODO probably use 2 classes that inherit from this class for the different modes

        this.components = components;


    }


    @Override
    protected Object execute() throws Exception {

        String type = "machine"; //TODO get actual type from envID, could be container

        if (type.equals("machine")) {
            MachineComponentRequest componentRequest = new MachineComponentRequest();
            componentRequest.setEnvironment("environmentID");
            ComponentResponse componentResponse =  components.createComponent(componentRequest);

        } else if (type.equals("container")) {

            ContainerComponentRequest componentRequest = new ContainerComponentRequest();

            ComponentResponse componentResponse =  components.createComponent(componentRequest);
        } else {
            LOG.warning("Unsupported environment type specified."); //TODO throw exception
        }



        return null;
    }

}