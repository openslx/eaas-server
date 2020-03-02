package de.bwl.bwfla.emil.utils;

import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.InputStreamDataSource;
import de.bwl.bwfla.emil.datatypes.rest.UploadResponse;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import org.apache.tamaya.inject.api.Config;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.activation.DataHandler;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/upload")
@ApplicationScoped
public class Upload  {

    @Inject
    @Config(value = "rest.blobstore")
    private String blobStoreRestAddress;

    @Inject
    @Config(value = "ws.blobstore")
    private String blobStoreWsAddress;

    private static final String HTTP_FORM_HEADER_FILE = "file";

    @Secured({Role.PUBLIC})
    @POST
    @Path("/")
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public UploadResponse upload(MultipartFormDataInput input)
    {
        InputStream inputFile =  null;

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputPartsFiles = uploadForm.get(HTTP_FORM_HEADER_FILE);

        if(inputPartsFiles == null)
            return new UploadResponse(new BWFLAException("invalid form data"));

        UploadResponse response = new UploadResponse();

        for (InputPart inputPart : inputPartsFiles) {
            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                inputFile = inputPart.getBody(InputStream.class,null);

                final BlobDescription blob = new BlobDescription()
                        .setDescription("upload")
                        .setNamespace("user-upload")
                        .setData(new DataHandler(new InputStreamDataSource(inputFile)))
                        .setName(UUID.randomUUID().toString());

                BlobHandle handle = BlobStoreClient.get()
                        .getBlobStorePort(blobStoreWsAddress)
                        .put(blob);

                response.getUploads().add(handle.toRestUrl(blobStoreRestAddress));

            } catch (IOException | BWFLAException e) {
                return new UploadResponse(new BWFLAException(e));
            }
        }
        System.out.println(response.toString());
        return response;
    }
}
