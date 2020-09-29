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
import java.net.InetAddress;
import java.net.UnknownHostException;

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
    @Config(value = "storage.s3_default_user_data_host")
    private String s3_default_user_data_host;

    @Inject
    @Config(value = "storage.s3_default_user_data_protocol")
    private String s3_default_user_data_protocol;

    @Inject
    @Config(value = "storage.s3_default_user_data_port")
    private String s3_default_user_data_port;

    @Inject
    @Config(value = "storage.s3_default_user_data_bucket")
    private String s3_default_user_data_bucket;

    @Inject
    @Config(value = "storage.s3_user_access_key_id")
    private String access_key_id;

    @Inject
    @Config(value = "storage.s3_user_access_key_secret")
    private String access_key_secret;

    @GET
    @Path("/sts")
    @Secured(roles={Role.RESTRICTED})
    @Produces("text/plain")
    public Response getS3Token() {

        String userId = "shared";
        if(authenticatedUser != null && authenticatedUser.getUserId() != null)
            userId = authenticatedUser.getUserId();

        String eaasBucketUrl = null;

        if(bucketUrl.isEmpty())
        {
            // the default host is a docker name
            // we need to access this host via SLIRP,
            // in case of a local/non public deployment we need the internal docker IP
            String resolvedHost;
            try {
                resolvedHost = InetAddress.getByName(s3_default_user_data_host).getHostAddress();
            } catch (UnknownHostException e) {
                resolvedHost = s3_default_user_data_host;
                e.printStackTrace();
            }
            eaasBucketUrl = s3_default_user_data_protocol + "://"
                    + s3_default_user_data_host + ":" + s3_default_user_data_port + "/" + s3_default_user_data_bucket;
         }
        else
            eaasBucketUrl = bucketUrl;

        try {
            final DeprecatedProcessRunner process = new DeprecatedProcessRunner("node")
                    .addArgument("/libexec/get-s3-token/get-s3-token.js")
                    .redirectStdErrToStdOut(false)

                    .addEnvVariable("AWS_ACCESS_KEY_ID", access_key_id)
                    .addEnvVariable("AWS_SECRET_ACCESS_KEY", access_key_secret)
                    .addEnvVariable("AWS_DEFAULT_REGION", "us-east-1")
                    .addEnvVariable("EAAS_S3_BUCKET_URL", eaasBucketUrl)
                    .addArgument(userId) // username
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
