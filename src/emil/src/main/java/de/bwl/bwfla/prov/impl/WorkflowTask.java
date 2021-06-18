package de.bwl.bwfla.prov.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.datatypes.rest.*;


import de.bwl.bwfla.prov.api.EnvironmentContainerDetails;
import de.bwl.bwfla.prov.api.MachineComponentRequestWithInput;
import de.bwl.bwfla.prov.client.WorkflowClient;


import javax.ws.rs.WebApplicationException;
import java.util.*;
import java.util.logging.Logger;

public class WorkflowTask extends BlockingTask<Object> {

    private final String environmentId;
    private final Map<String, String> inputURLS;
    private final Map<Integer, String> params;

    private final WorkflowClient workflowClient;

    private static final Logger LOG = Logger.getLogger("");

    public WorkflowTask(String environmentId, Map<String, String> inputFileURLS, Map<Integer, String> params) throws BWFLAException {

        this.environmentId = environmentId;
        this.inputURLS = inputFileURLS;
        this.params = params;

        this.workflowClient = new WorkflowClient();
    }

    @Override
    protected ProcessResultUrl execute() throws Exception {
        return executeTool();
    }

    private ProcessResultUrl executeTool() throws BWFLAException, InterruptedException {

        LOG.info("--- Starting Workflow execution for tool with ID: " + environmentId);
        try {

            EnvironmentContainerDetails details = workflowClient.getEnvironmentDetails(environmentId);
            LOG.severe("Successfully got env details!");


            String runtimeId = details.getRuntimeId();
            ContainerNetworkingType networkingInfo = (ContainerNetworkingType) details.getNetworking();
            LOG.severe("Successfully got Networking Info details!");

            SortedSet<Integer> sortedArgs = new TreeSet<>(params.keySet());
            ArrayList<String> processArgs = new ArrayList<>();
            for (int key : sortedArgs) {
                processArgs.add(params.get(key));
            }

            UpdateContainerRequest updateContainerRequest = new UpdateContainerRequest();
            updateContainerRequest.setId(details.getEnvId());

            updateContainerRequest.setTitle(details.getTitle());
            updateContainerRequest.setInputFolder(details.getInput());
            updateContainerRequest.setDescription(details.getDescription());
            updateContainerRequest.setOutputFolder(details.getOutput());
            updateContainerRequest.setProcessArgs(processArgs);
            updateContainerRequest.setProcessEnvs((ArrayList<String>) details.getProcessEnvs());
            updateContainerRequest.setAuthor(details.getAuthor());
            updateContainerRequest.setContainerRuntimeId(runtimeId);
            updateContainerRequest.setNetworking(networkingInfo);

            LOG.severe("Successfully set up UpdateContainerRequest!");


            workflowClient.updateContainerWithNewProcessArgs(updateContainerRequest);

            LOG.severe("Successfully sent UpdateContainerRequest!");


            LinuxRuntimeContainerReq linuxRuntimeContainerReq = new LinuxRuntimeContainerReq();
            linuxRuntimeContainerReq.setUserContainerEnvironment(environmentId);
            linuxRuntimeContainerReq.setDHCPenabled(false);//networkingInfo.isDHCPenabled()); //FIXME this with null check
            linuxRuntimeContainerReq.setTelnetEnabled(false);//networkingInfo.isTelnetEnabled()); //FIXME this with null check

            LOG.severe("Successfully got LinuxRuntimeContainerReq!");


            MachineComponentRequest machineComponentRequest = new MachineComponentRequest();
            machineComponentRequest.setEnvironment(runtimeId);
            machineComponentRequest.setHeadless(true);
            machineComponentRequest.setLinuxRuntimeData(linuxRuntimeContainerReq);

            ComponentWithExternalFilesRequest.InputMedium inputMedium = new ComponentWithExternalFilesRequest.InputMedium();
            inputMedium.setDestination(details.getInput());
            inputMedium.setSizeInMb(512);

            ArrayList<ComponentWithExternalFilesRequest.FileURL> inputData = new ArrayList<>();

            for (Map.Entry<String, String> entry : inputURLS.entrySet()) {

                LOG.severe("Got file: " + entry.getKey());

                ComponentWithExternalFilesRequest.FileURL data = new ComponentWithExternalFilesRequest.FileURL();
                data.setAction("copy");
                data.setUrl(entry.getValue());
                data.setName(entry.getKey());
                data.setCompressionFormat("tar");
                inputData.add(data);
            }
            inputMedium.setExtFiles(inputData);

            ArrayList<ComponentWithExternalFilesRequest.InputMedium> inputs = new ArrayList<>();
            inputs.add(inputMedium);
            machineComponentRequest.setInputs(inputs);

            LOG.severe("Successfully created machine Component Request!");


            MachineComponentResponse response = workflowClient.startComponentHeadless(machineComponentRequest);
            String sessionId = response.getId();

            LOG.severe("Successfully started component and got session Id: " + sessionId);


            workflowClient.sendKeepAlive(sessionId);

            //TODO remove keepalive if not necessary
            while (true) {
                Thread.sleep(5000);
                workflowClient.sendKeepAlive(sessionId);
                ComponentStateResponse stateResponse = workflowClient.checkState(sessionId);
                String state = stateResponse.getState();

                if (state.equals("OK")) {
                    LOG.info("Session is still running!");
                } else if (state.equals("STOPPED")) {
                    LOG.info("Session is stopped! Sending stop request!");
                    break;
                }
            }
            LOG.severe("DONE WITH SLEEPS/keepalive, session should be stopped soon!");
            return workflowClient.stopComponent(sessionId);

        } catch (WebApplicationException | InterruptedException exception) {
            LOG.severe("Create Component Error: " + exception.getMessage());
            throw exception;

        }
    }
}
