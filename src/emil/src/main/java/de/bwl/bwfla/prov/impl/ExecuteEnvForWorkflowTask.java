package de.bwl.bwfla.prov.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.emil.Components;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.rest.MachineComponentRequest;
import de.bwl.bwfla.prov.api.InjectedEnvironmentResponse;
import de.bwl.bwfla.prov.api.WorkflowRequest;


import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.ContainerComponentRequest;
import de.bwl.bwfla.emucomp.api.*;
import org.apache.tamaya.ConfigurationProvider;




import java.util.ArrayList;
import java.util.logging.Logger;

public class ExecuteEnvForWorkflowTask extends BlockingTask<Object> {


    private String environmentId;
    private String[] inputURLS;
    private Components components;

    private static final Logger LOG = Logger.getLogger("");

    public ExecuteEnvForWorkflowTask(String environmentId, String[] inputFileURLS, Components components, DatabaseEnvironmentsAdapter environmentsAdapter) throws BWFLAException {

        //TODO
        // 1. Download inputFiles from blob store
        // 2. Inject inputFiles into environment
        // 3. Start environment
        // 4. Wait til tool in environment is done
        // 5. Tool uploads output to blob store
        // 6. Return blobstore url

        this.environmentId = environmentId;
        this.inputURLS = inputFileURLS;
        this.components = components;

    }


    @Override
    protected InjectedEnvironmentResponse execute() throws Exception {

        return prepareEnvironmentInjects(this.environmentId);
    }


    private InjectedEnvironmentResponse prepareEnvironmentInjects(String envId) throws BWFLAException {

        if (this.inputURLS == null || this.inputURLS.length == 0) {
            LOG.info("No additional injects requested returning environment: " + envId);
            InjectedEnvironmentResponse response = new InjectedEnvironmentResponse();
            response.setEnvironmentId(envId);
            return response;
        } else {

            MachineComponentRequest x = new MachineComponentRequest();
            x.setEnvironment(envId);
            x.setH

            components.createComponent(x);


//            List<ImageModificationRequest> requestList = new ArrayList<>();
//            for (AdditionalInjectRequest entry : this.additionalInjects) {
//
//                String url = entry.getUrl();
//                String action = entry.getAction();
//                String name = entry.getName();
//
//                LOG.info("Adding File with url" + url);
//                LOG.info("Applying action" + action);
//
//                ImageModificationRequest request = new ImageModificationRequest();
//                ImageModificationCondition imageModificationCondition = new ImageModificationCondition();
//                imageModificationCondition.getPaths().add(inputDirectory);
//                request.setCondition(imageModificationCondition);
//                request.setDataUrl(url);
//
//                switch (action) {
//                    case "copy":
//                        request.setAction(ImageModificationAction.COPY);
//                        request.setDestination(Paths.get(inputDirectory).resolve(name).toString());
//                        break;
//                    case "extract":
//                        request.setAction(ImageModificationAction.EXTRACT_TAR);
//                        request.setDestination(inputDirectory);
//                        break;
//                }
//
//                requestList.add(request);
//            }
//
//            String newEnvId = injectDataIntoImage(envId, requestList);
            InjectedEnvironmentResponse response = new InjectedEnvironmentResponse();
            response.setEnvironmentId(envId);
            return response;
        }


    }



}
