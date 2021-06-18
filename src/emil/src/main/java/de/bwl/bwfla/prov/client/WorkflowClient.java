/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.prov.client;

import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.prov.api.EnvironmentContainerDetails;
import de.bwl.bwfla.prov.api.MachineComponentRequestWithInput;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class WorkflowClient {
    public final static String DEFAULT_EMIL_URL = "https://historic-builds.emulation.cloud/emil"; //TODO baseurl from config

    protected final Client client;
    protected final String baseUrl;

    public WorkflowClient() {
        this(WorkflowClient.DEFAULT_EMIL_URL);
    }

    public WorkflowClient(String serviceUrl) throws IllegalArgumentException {
        if (serviceUrl == null)
            throw new IllegalArgumentException("Base URL URL not configured");

        this.client = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder())
                .connectionPoolSize(10)
                .build();

        this.baseUrl = serviceUrl;
    }

    public MachineComponentResponse startComponentHeadless(MachineComponentRequest machineComponentRequest) {

        machineComponentRequest.setHeadless(true);


        System.out.println("---------------------------------- REQUEST: -------------------------------- ");
        System.out.println(machineComponentRequest.toString());
        System.out.println(Entity.entity(machineComponentRequest, MediaType.APPLICATION_JSON_TYPE).toString());

        final WebTarget target = client.target(baseUrl);
        Response response = null;
        Builder restRequest = target.path("components").request(MediaType.APPLICATION_JSON_TYPE);
        try {
            response = restRequest.post(Entity.entity(machineComponentRequest, MediaType.APPLICATION_JSON_TYPE));

        } catch (Exception e) {
            System.out.println("ERROR WHILE SENDING REQUEST: ");
            e.printStackTrace();
        }

        System.out.println("SENT REQUEST TO START COMPONENT HEADLESS!");
        switch (Status.fromStatusCode(response.getStatus())) {
            case OK:
                System.out.println("COMPONENTS RESPONDED WITH OK");

                MachineComponentResponse machineComponentResponse = response.readEntity(MachineComponentResponse.class);
                System.out.println("SESSION ID: " + machineComponentResponse.getId());
                return machineComponentResponse;
            default:
                throw new ServerErrorException(
                        "The ImageProposer service responded with an unexpected return code.",
                        response.getStatus());
        }

    }

    public EnvironmentContainerDetails getEnvironmentDetails(String environmentId) {


        final WebTarget target = client.target(baseUrl);
        Response response = null;
        System.out.println("PATH: " + baseUrl + "/environment-repository/environments/" + environmentId);
        WebTarget restRequest = target.path("environment-repository/environments/" + environmentId).queryParam("access_token", "undefined");
        System.out.println("Full REQ: " + restRequest.toString());
        System.out.println("URI: " + restRequest.getUri());

        Builder v = restRequest.request(MediaType.APPLICATION_JSON_TYPE);
        try {
            response = v.get();

        } catch (Exception e) {
            System.out.println("ERROR WHILE SENDING REQUEST: ");
            e.printStackTrace();
        }

        System.out.println("SENT REQUEST TO GET ENV DETAILS!");
        switch (Status.fromStatusCode(response.getStatus())) {
            case OK:
                System.out.println("ENVIRONMENTS RESPONDED WITH OK");

                EnvironmentContainerDetails environmentDetails = response.readEntity(EnvironmentContainerDetails.class);
                System.out.println("Runtime Id: " + environmentDetails.getRuntimeId());
                return environmentDetails;
            default:
                System.out.println("STATUS CODE WAS NOT 200 BUT: " + response.getStatus());
                throw new ServerErrorException(
                        "The ImageProposer service responded with an unexpected return code.",
                        response.getStatus());
        }

    }

    public void updateContainerWithNewProcessArgs(UpdateContainerRequest updateContainerRequest) {

        final WebTarget target = client.target(baseUrl);
        Response response = null;
        WebTarget restRequest = target.path("/EmilContainerData/updateContainer").queryParam("access_token", "undefined");

        Builder v = restRequest.request(MediaType.APPLICATION_JSON_TYPE);

        try {
            response = v.post(Entity.entity(updateContainerRequest, MediaType.APPLICATION_JSON_TYPE));

        } catch (Exception e) {
            System.out.println("ERROR WHILE SENDING REQUEST: ");
            e.printStackTrace();
        }

        System.out.println("SENT REQUEST TO UPDATE CONTAINER!");
        switch (Status.fromStatusCode(response.getStatus())) {
            case OK:
                System.out.println("CONTAINER UPDATE RESPONDED WITH OK");
                return;
            default:
                System.out.println("STATUS CODE WAS NOT 200 BUT: " + response.getStatus());
                throw new ServerErrorException(
                        "The ImageProposer service responded with an unexpected return code.",
                        response.getStatus());
        }

    }


    public void sendKeepAlive(String sessionId) {

        final WebTarget target = client.target(baseUrl);
        Response response = null;
        Builder restRequest = target.path("components/" + sessionId + "/keepalive").request();
        try {
            response = restRequest.post(null);

        } catch (Exception e) {
            System.out.println("ERROR WHILE SENDING REQUEST: ");
            e.printStackTrace();
        }

        System.out.println("SENT KEEPALIVE!");
        switch (Status.fromStatusCode(response.getStatus())) {
            case NO_CONTENT:
                System.out.println("KEEPALIVE RESPONDED WITH NO CONTENT");

                return;
            default:
                System.out.println("STATUS CODE WAS NOT 204 BUT: " + response.getStatus());

                throw new ServerErrorException(
                        "The ImageProposer service responded with an unexpected return code.",
                        response.getStatus());
        }

    }

    public ComponentStateResponse checkState(String sessionId) {

        final WebTarget target = client.target(baseUrl);
        Response response = null;
        Builder restRequest = target.path("components/" + sessionId + "/state").request();
        try {
            response = restRequest.get();

        } catch (Exception e) {
            System.out.println("ERROR WHILE SENDING REQUEST: ");
            e.printStackTrace();
        }

        System.out.println("SENT State Check!");
        switch (Status.fromStatusCode(response.getStatus())) {
            case OK:
                System.out.println("STATE RESPONDED WITH OK");
                ComponentStateResponse componentStateResponse = response.readEntity(ComponentStateResponse.class);

                return componentStateResponse;
            default:
                System.out.println("STATUS CODE WAS NOT 200 BUT: " + response.getStatus());

                throw new ServerErrorException(
                        "The ImageProposer service responded with an unexpected return code.",
                        response.getStatus());
        }

    }

    public ProcessResultUrl stopComponent(String sessionId) {

        final WebTarget target = client.target(baseUrl);
        Response response = null;
        Builder restRequest = target.path("components/" + sessionId + "/stop").request();
        try {
            response = restRequest.get();

        } catch (Exception e) {
            System.out.println("ERROR WHILE SENDING REQUEST: ");
            e.printStackTrace();
        }

        System.out.println("SENT State Check!");
        switch (Status.fromStatusCode(response.getStatus())) {
            case OK:
                System.out.println("STATE RESPONDED WITH OK");
                ProcessResultUrl resultUrl = response.readEntity(ProcessResultUrl.class);

                return resultUrl;
            default:
                System.out.println("STATUS CODE WAS NOT 200 BUT: " + response.getStatus());

                throw new ServerErrorException(
                        "The ImageProposer service responded with an unexpected return code.",
                        response.getStatus());
        }

    }


}
