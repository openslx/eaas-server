package de.bwl.bwfla.prov.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emil.datatypes.rest.*;


import de.bwl.bwfla.emucomp.api.ImageModificationRequest;
import de.bwl.bwfla.prov.api.EnvironmentContainerDetails;
import de.bwl.bwfla.prov.api.WorkflowRequest;
import de.bwl.bwfla.prov.client.WorkflowClient;


import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WorkflowTask extends BlockingTask<Object> {

    private final String environmentId;
    private final String workdirTarURL;
    private final Map<String, String> arguments;
    private final Map<String, String> envVars;

    private final String inputTarURL;
    private final String outputFolder; // this is the folder where CWL would expect output (TBD)

    private final WorkflowClient workflowClient;

    private static final Logger LOG = Logger.getLogger("");


    public WorkflowTask(WorkflowRequest request) {


        this.environmentId = request.getEnvironmentId();
        this.workdirTarURL = request.getWorkdirTarURL();
        this.inputTarURL = request.getInputTarURL();
        this.arguments = request.getArguments();
        this.envVars = request.getEnvironmentVariables();
        this.outputFolder = request.getOutputFolder();


        this.workflowClient = new WorkflowClient();
    }

    @Override
    protected ProcessResultUrl execute() throws Exception {
        return executeTool();
    }

    private void createProvExecutionFile() {

        //TODO this needs to move to the actual mount/inject
        var inputsBuilder = Json.createArrayBuilder();
//        for (var input : workdirTarURL.keySet()) {
//            inputsBuilder.add(input);
//        }

        JsonObject jsonExecution = Json.createObjectBuilder()
                .add("environmentId", environmentId)
                .add("startedAt", String.valueOf(LocalDateTime.now()))
                .add("inputBlobstoreURL", inputTarURL)
//                .add("arguments", arguments)  TODO this
                .add("user", "getFromUserCtx")
                .build();

        LOG.severe("Created JSON Execution:" + jsonExecution);

        try {

            FileWriter myWriter = new FileWriter("/tmp/exec_input_" + environmentId + ".json");
            myWriter.write(jsonExecution.toString());
            myWriter.close();
            LOG.severe("Successfully wrote json file.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ProcessResultUrl executeTool() throws BWFLAException, InterruptedException {

        LOG.info("--- Starting Workflow execution for tool with ID: " + environmentId);
        try {

            EnvironmentContainerDetails details = workflowClient.getEnvironmentDetails(environmentId);
            LOG.severe("Successfully got env details!");

            String runtimeId = details.getRuntimeId();
            ContainerNetworkingType networkingInfo = (ContainerNetworkingType) details.getNetworking();
            LOG.severe("Successfully got Networking Info details!");

            Map<Integer, String> casted = arguments.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> Integer.parseInt(e.getKey()),
                            Map.Entry::getValue
                    ));

            SortedSet<Integer> sortedArgs = new TreeSet<>(casted.keySet());
            ArrayList<String> processArgs = new ArrayList<>();
            for (int key : sortedArgs) {
                processArgs.add(casted.get(key));
            }

            // Keep old envVars, but overwrite those with the same name
            ArrayList<String> envVarsToSet;
            if (null == envVars || envVars.isEmpty()) {
                envVarsToSet = (ArrayList<String>) details.getProcessEnvs();
            } else {
                envVarsToSet = new ArrayList<>();
                ArrayList<String> oldVars = (ArrayList<String>) details.getProcessEnvs();
                for (String oldVar : oldVars) {
                    String varName = oldVar.split("=")[0];
                    if (!envVars.containsKey(varName)) {
                        envVarsToSet.add(oldVar);
                    }
                }
                for (var entry : envVars.entrySet()) {
                    envVarsToSet.add(entry.getKey() + "=" + entry.getValue());
                }

            }


            UpdateContainerRequest updateContainerRequest = new UpdateContainerRequest();
            updateContainerRequest.setId(details.getEnvId());

            updateContainerRequest.setTitle(details.getTitle());
            updateContainerRequest.setInputFolder("/input"); //TODO remove according to new IO implementation
            updateContainerRequest.setOutputFolder("/output"); //TODO remove according to new IO implementation
            updateContainerRequest.setDescription(details.getDescription());
            updateContainerRequest.setProcessArgs(processArgs);
            updateContainerRequest.setProcessEnvs(envVarsToSet);
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


            List<ImageModificationRequest> imageModificationRequestList = new ArrayList<>();

            ImageModificationRequest imageModificationRequest = new ImageModificationRequest();
            imageModificationRequest.setAction(ImageModificationRequest.ImageModificationAction.EXTRACT_TAR);
            imageModificationRequest.setDestination("/");
            imageModificationRequest.setDataUrl(inputTarURL);

            imageModificationRequestList.add(imageModificationRequest);

            machineComponentRequest.setImageModificationRequestList(imageModificationRequestList);

            ComponentWithExternalFilesRequest.InputMedium inputMedium = new ComponentWithExternalFilesRequest.InputMedium();
            inputMedium.setDestination(details.getInput());
            inputMedium.setSizeInMb(512);

            ArrayList<ComponentWithExternalFilesRequest.FileURL> inputList = new ArrayList<>();

            if (inputTarURL != null) {
                inputList.add(createFileURL(inputTarURL));

            }
            if (workdirTarURL != null) {
                inputList.add(createFileURL(workdirTarURL));

            }

            inputMedium.setExtFiles(inputList);

            ArrayList<ComponentWithExternalFilesRequest.InputMedium> inputs = new ArrayList<>();
            inputs.add(inputMedium);
            machineComponentRequest.setInputs(inputs);

            LOG.severe("Successfully created machine Component Request!");

            // ---- PROV ----
            createProvExecutionFile();

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
            var stopComp = workflowClient.stopComponent(sessionId);

            var outPutURL = stopComp.getUrl();

            LOG.severe("Starting PROV subprocess");
            DeprecatedProcessRunner provRunner = new DeprecatedProcessRunner("python3");
            provRunner.setWorkingDirectory(Path.of("/tmp"));
            provRunner.addArgument("/libexec/eaas-prov/provone.py");
            provRunner.addArgument("/tmp/" + environmentId + ".json");
            provRunner.addArgument("/tmp/exec_input_" + environmentId + ".json"); //TODO potentially remove json, because everything is in workflowtask now
            provRunner.addArgument(outPutURL);
            provRunner.execute(true);
            provRunner.cleanup();
            LOG.severe("PROV is done!");

            return stopComp;


        } catch (WebApplicationException | InterruptedException exception) {
            LOG.severe("Create Component Error: " + exception.getMessage());
            throw exception;

        }
    }

    private ComponentWithExternalFilesRequest.FileURL createFileURL(String inputTarURL) {

        ComponentWithExternalFilesRequest.FileURL data = new ComponentWithExternalFilesRequest.FileURL();
        data.setAction("extract");
        data.setUrl(inputTarURL);
        data.setName("workdirInput");
        data.setCompressionFormat("tar");

        return data;
    }
}
