package de.bwl.bwfla.emil;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import org.apache.tamaya.inject.api.Config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@ApplicationScoped
@Path("/user-data-storage")
public class UserDataStorage extends EmilRest {

    @Inject
    @AuthenticatedUser
    private UserContext authenticatedUser = null;

    @Inject
    @Config(value = "storage.s3_user_bucket_url")
    private String bucketUrl;

    @Inject
    @Config(value = "storage.access_key_id")
    private String access_key_id;

    @Inject
    @Config(value = "storage.access_key_secret")
    private String access_key_secret;

    @GET
    @Path("/sts")
    @Secured(roles={Role.PUBLIC})
    @Produces("text/plain")
    public Response getS3Token() {
        try {
            LOG.severe("AWS_ACCESS_KEY_ID " + access_key_id);
            LOG.severe("AWS_SECRET_ACCESS_KEY " + access_key_secret);
            LOG.severe("AWS_DEFAULT_REGION us-east-1");
            LOG.severe("EAAS_S3_BUCKET_URL " + bucketUrl);
            final DeprecatedProcessRunner process = new DeprecatedProcessRunner("node")
                    .addArgument("/libexec/get-s3-token/get-s3-token.js")
                    .redirectStdErrToStdOut(false)

                    .addEnvVariable("AWS_ACCESS_KEY_ID", access_key_id)
                    .addEnvVariable("AWS_SECRET_ACCESS_KEY", access_key_secret)
                    .addEnvVariable("AWS_DEFAULT_REGION", "us-east-1")
                    .addEnvVariable("EAAS_S3_BUCKET_URL", bucketUrl)

                    .addArgument("klaus") // username

                    .setLogger(LOG);
            final DeprecatedProcessRunner.Result result = process.executeWithResult()
                    .orElseThrow(() -> new BWFLAException("Running get-s3-token failed!"));

            if (!result.successful())
                throw new BWFLAException("Creating STS token failed! " + result.stderr() + " " + result.stdout());

            return Response.status(Response.Status.OK)
                    .entity(result.stdout())
                    .build();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return UserDataStorage.internalErrorResponse(e);
        }
    }

}
