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

package de.bwl.bwfla.imageproposer.client;

import java.util.List;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.bwl.bwfla.common.datatypes.identification.DiskType;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class ImageProposer {
    public final static String DEFAULT_SERVICE_URL = "http://localhost:8080/imageproposer";

    protected final Client client;
    protected final String serviceUrl;

    public ImageProposer() {
        this(ImageProposer.DEFAULT_SERVICE_URL);
    }

    public ImageProposer(String serviceUrl) throws IllegalArgumentException {
        if(serviceUrl == null)
            throw new IllegalArgumentException("ImageProposer URL not configured");

        this.client = new ResteasyClientBuilder().connectionPoolSize(10)
                .build();
        this.serviceUrl = serviceUrl;
    }

    public void refreshIndex()
    {
        final WebTarget target = client.target(serviceUrl + "/api/v1");
        Response response = null;
        Builder restRequest = target.path("refreshIndex").request();
        response = restRequest.post(null);
        System.out.println("request refreshIndex()");
        switch (Status.fromStatusCode(response.getStatus())) {
            case OK:
                System.out.println("OK");
                return;
            default:
                throw new ServerErrorException(
                        "The ImageProposer service responded with an unexpected return code.",
                        response.getStatus());
            }
    }

    public Proposal propose(ProposalRequest request)
            throws InterruptedException, IllegalArgumentException,
            ServiceUnavailableException, ServerErrorException,
            InternalServerErrorException {
        final WebTarget target = client.target(serviceUrl + "/api/v1");
        Response response = null;
        Builder restRequest = target.path("proposals")
                .request(ProposalResponse.MEDIATYPE_AS_JSON);

        // send proposal request to service
        response = restRequest.post(
                Entity.entity(request, ProposalRequest.MEDIATYPE_AS_JSON));
        switch (Status.fromStatusCode(response.getStatus())) {
        case ACCEPTED:
            // this is the expected success value
            break;
        case BAD_REQUEST:
            String message = response.readEntity(ProposalResponse.class)
                    .getMessage();
            throw new IllegalArgumentException(message);
        case SERVICE_UNAVAILABLE:
            message = response.readEntity(ProposalResponse.class).getMessage();
            throw new ServiceUnavailableException(
                    "The ImageProposer service is not available right now. Server message was:\""
                            + response.readEntity(ProposalResponse.class)
                                    .getMessage()
                            + "\"");
        case INTERNAL_SERVER_ERROR:
            message = response.readEntity(ProposalResponse.class).getMessage();
            throw new InternalServerErrorException(message);
        default:
            throw new ServerErrorException(
                    "The ImageProposer service responded with an unexpected return code.",
                    response.getStatus());
        }
        response.close();

        WebTarget waitqueue = client
                .target(response.getHeaderString("Location"));
        Builder waitRequest = waitqueue.request(MediaType.APPLICATION_JSON);

        // wait for response to be ready
        for (response = waitRequest.get(); response.getStatus() == Status.OK
                .getStatusCode(); response
                        .close(), response = waitRequest.get()) {
            Thread.sleep(1000);
        }
        response.close();

        switch (Status.fromStatusCode(response.getStatus())) {
        case SEE_OTHER:
            // this is the expected success value
            break;
        case INTERNAL_SERVER_ERROR:
        case NOT_FOUND:
        default:
            throw new ServerErrorException(
                    "The ImageProposer service responded with an unexpected return code.",
                    response.getStatus());
        }

        WebTarget result = client.target(response.getHeaderString("Location"));
        Builder resultRequest = result.request(Proposal.MEDIATYPE_AS_JSON);
        response = resultRequest.get();

        switch (Status.fromStatusCode(response.getStatus())) {
        case OK:
            // this is the expected success value
            break;
        case BAD_REQUEST:
        case NOT_FOUND:
        default:
            throw new ServerErrorException(
                    "The ImageProposer service responded with an unexpected return code.",
                    response.getStatus());
        }

        Proposal proposal = response.readEntity(Proposal.class);
        return proposal;
    }
}
