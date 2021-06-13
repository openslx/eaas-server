package de.bwl.bwfla.prov.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.datatypes.rest.*;


import de.bwl.bwfla.prov.client.EnvironmentContainerDetails;
import de.bwl.bwfla.prov.client.WorkflowClient;


import javax.ws.rs.WebApplicationException;
import java.util.logging.Logger;

public class WorkflowTask extends BlockingTask<Object> {

    private String environmentId;
    private String[] inputURLS;

    private WorkflowClient workflowClient;

    private static final Logger LOG = Logger.getLogger("");

    public WorkflowTask(String environmentId, String[] inputFileURLS) throws BWFLAException {

        this.environmentId = environmentId;
        this.inputURLS = inputFileURLS;

        this.workflowClient = new WorkflowClient();
    }

    @Override
    protected ProcessResultUrl execute() throws Exception {
        return executeTool();
    }

    private ProcessResultUrl executeTool() throws BWFLAException, InterruptedException {

        LOG.info("--- Starting tool with ID: " + environmentId);
        try {

            EnvironmentContainerDetails details = workflowClient.getEnvironmentDetails(environmentId);
            LOG.severe("Successfully got env details!");

            String runtimeId = details.getRuntimeId();
            ContainerNetworkingType networkingInfo = (ContainerNetworkingType) details.getNetworking();

            LOG.severe("Successfully got Networking Info details!");

            LinuxRuntimeContainerReq linuxRuntimeContainerReq = new LinuxRuntimeContainerReq();
            linuxRuntimeContainerReq.setUserContainerEnvironment(environmentId);
            linuxRuntimeContainerReq.setDHCPenabled(false);//networkingInfo.isDHCPenabled()); //FIXME this with null check
            linuxRuntimeContainerReq.setTelnetEnabled(false);//networkingInfo.isTelnetEnabled()); //FIXME this with null check

            LOG.severe("Successfully got LinuxRuntimeContainerReq!");


            MachineComponentRequest machineComponentRequest = new MachineComponentRequest();
            machineComponentRequest.setEnvironment(runtimeId);
            machineComponentRequest.setHeadless(true);
            machineComponentRequest.setLinuxRuntimeData(linuxRuntimeContainerReq);

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
