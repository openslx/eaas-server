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

package de.bwl.bwfla.imageclassifier.client;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.bwl.bwfla.emucomp.api.FileCollection;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class ImageClassifier {
    public final static String DEFAULT_SERVICE_URL = "http://localhost:8080/imageclassifier";

    protected final Client client;
    protected final String serviceUrl;

    public ImageClassifier() {
        this(ImageClassifier.DEFAULT_SERVICE_URL);
    }

    public ImageClassifier(String serviceUrl) {
        this.client = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder())
                .connectionPoolSize(10)
                .build();

        this.serviceUrl = serviceUrl;
    }

    public Identification<ClassificationEntry> getClassification(
            FileCollection fc) throws InterruptedException,
            IllegalArgumentException, ServiceUnavailableException,
            ServerErrorException, InternalServerErrorException {
        return getClassification(new IdentificationRequest(fc, null));
    }

    public Identification<ClassificationEntry> getClassification(
            FileCollection fc, String policyUrl) throws InterruptedException,
            IllegalArgumentException, ServiceUnavailableException,
            ServerErrorException, InternalServerErrorException {
        return getClassification(
                new IdentificationRequest(fc, policyUrl));
    }

    public Identification<ClassificationEntry> getClassification(
            IdentificationRequest request) throws InterruptedException,
            IllegalArgumentException, ServiceUnavailableException,
            ServerErrorException, InternalServerErrorException {
        final WebTarget target = client.target(serviceUrl + "/api/v1");
        Response response = null;
        Builder restRequest = target.path("classifications")
                .request(IdentificationResponse.MEDIATYPE_AS_JSON);

        // send identification request to service
        response = restRequest.post(Entity.entity(request,
                IdentificationRequest.MEDIATYPE_AS_JSON));
        switch (Status.fromStatusCode(response.getStatus())) {
        case ACCEPTED:
            // this is the expected success value
            break;
        case BAD_REQUEST:
            String message = response.readEntity(IdentificationResponse.class)
                    .getMessage();
            throw new IllegalArgumentException(message);
        case SERVICE_UNAVAILABLE:
            message = response.readEntity(IdentificationResponse.class)
                    .getMessage();
            throw new ServiceUnavailableException(
                    "The ImageClassifier service is not available right now. Server message was:\""
                            + response.readEntity(IdentificationResponse.class)
                                    .getMessage()
                            + "\"");
        case INTERNAL_SERVER_ERROR:
            message = response.readEntity(IdentificationResponse.class)
                    .getMessage();
            throw new InternalServerErrorException(message);
        default:
            throw new ServerErrorException(
                    "The ImageClassifier service responded with an unexpected return code.",
                    response.getStatus());
        }
        response.close();

        WebTarget waitqueue = client
                .target(response.getHeaderString("Location"));
        Builder waitRequest = waitqueue
                .request(MediaType.APPLICATION_JSON);

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
                    "The ImageClassifier service responded with an unexpected return code.",
                    response.getStatus());
        }

        WebTarget result = client.target(response.getHeaderString("Location"));
        Builder resultRequest = result
                .request(Identification.MEDIATYPE_CLASSIFICATION_AS_JSON);
        response = resultRequest.get();

        switch (Status.fromStatusCode(response.getStatus())) {
        case OK:
            // this is the expected success value
            break;
        case BAD_REQUEST:
        case NOT_FOUND:
        default:
            throw new ServerErrorException(
                    "The ImageClassifier service responded with an unexpected return code.",
                    response.getStatus());
        }

        Identification<ClassificationEntry> id = response.readEntity(
                new GenericType<Identification<ClassificationEntry>>() {
                });
        return id;
    }

    public Identification<HistogramEntry> getHistogram(FileCollection fc)
            throws InterruptedException, IllegalArgumentException,
            ServiceUnavailableException, ServerErrorException,
            InternalServerErrorException {
        return getHistogram(new IdentificationRequest(fc, null));
    }

    public Identification<HistogramEntry> getHistogram(FileCollection fc,
            String policyUrl) throws InterruptedException,
            IllegalArgumentException, ServiceUnavailableException,
            ServerErrorException, InternalServerErrorException {
        return getHistogram(new IdentificationRequest(fc, policyUrl));
    }

    public Identification<HistogramEntry> getHistogram(
            IdentificationRequest request) throws InterruptedException,
            IllegalArgumentException, ServiceUnavailableException,
            ServerErrorException, InternalServerErrorException {
        final WebTarget target = client.target(serviceUrl + "/api/v1");
        Response response = null;
        Builder restRequest = target.path("histograms")
                .request(IdentificationResponse.MEDIATYPE_AS_JSON);

        // send identification request to service
        response = restRequest.post(Entity.entity(request,
                IdentificationRequest.MEDIATYPE_AS_JSON));
        switch (Status.fromStatusCode(response.getStatus())) {
        case ACCEPTED:
            // this is the expected success value
            break;
        case BAD_REQUEST:
            String message = response.readEntity(IdentificationResponse.class)
                    .getMessage();
            throw new IllegalArgumentException(message);
        case SERVICE_UNAVAILABLE:
            message = response.readEntity(IdentificationResponse.class)
                    .getMessage();
            throw new ServiceUnavailableException(
                    "The ImageClassifier service is not available right now. Server message was:\""
                            + response.readEntity(IdentificationResponse.class)
                                    .getMessage()
                            + "\"");
        case INTERNAL_SERVER_ERROR:
            message = response.readEntity(IdentificationResponse.class)
                    .getMessage();
            throw new InternalServerErrorException(message);
        default:
            throw new ServerErrorException(
                    "The ImageClassifier service responded with an unexpected return code.",
                    response.getStatus());
        }
        response.close();

        WebTarget waitqueue = client
                .target(response.getHeaderString("Location"));
        Builder waitRequest = waitqueue
                .request(IdentificationResponse.MEDIATYPE_AS_JSON);

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
                    "The ImageClassifier service responded with an unexpected return code.",
                    response.getStatus());
        }

        WebTarget result = client.target(response.getHeaderString("Location"));
        Builder resultRequest = result
                .request(Identification.MEDIATYPE_HISTOGRAM_AS_JSON);
        response = resultRequest.get();

        switch (Status.fromStatusCode(response.getStatus())) {
        case OK:
            // this is the expected success value
            break;
        case BAD_REQUEST:
        case NOT_FOUND:
        default:
            throw new ServerErrorException(
                    "The ImageClassifier service responded with an unexpected return code.",
                    response.getStatus());
        }
        response.bufferEntity();
        System.out.println(response.readEntity(String.class));

        Identification<HistogramEntry> id = response
                .readEntity(new GenericType<Identification<HistogramEntry>>() {
                });
        return id;
    }
}
