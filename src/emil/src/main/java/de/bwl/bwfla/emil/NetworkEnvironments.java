package de.bwl.bwfla.emil;

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.utils.JsonBuilder;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.NetworkEnvironment;
import de.bwl.bwfla.emil.datatypes.NetworkEnvironmentElement;
import de.bwl.bwfla.emil.datatypes.ErrorInformation;
import de.bwl.bwfla.emucomp.api.NetworkConfiguration;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Path("network-environments")
@ApplicationScoped
public class NetworkEnvironments extends EmilRest {
    
    @Inject
    private DatabaseEnvironmentsAdapter envHelper;
    @Inject
    private EmilEnvironmentRepository emilEnvRepo;

    private BlobStore blobstore;

    @Inject
    @Config(value = "rest.blobstore")
    private String blobStoreRestAddress;


    @Inject
    @Config(value = "ws.blobstore")
    private String blobStoreWsAddress;

    @Inject
    private BlobStoreClient blobStoreClient;

    @PostConstruct
    public void init()
    {
        try {
            this.blobstore = blobStoreClient.getBlobStorePort(blobStoreWsAddress);
        } catch (BWFLAException e) {
            e.printStackTrace();
        }
    }

    @Secured({Role.RESTRCITED})
    @PUT
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createNetworkEnvironment(NetworkEnvironment envNetworkEnv) {
        try {
            envNetworkEnv.getEmilEnvironments().forEach((networkElement -> {
                    if(networkElement.getMacAddress() != null && networkElement.getMacAddress().equals(""))
                        networkElement.setMacAddress(NetworkUtils.getRandomHWAddress());
            }));
            emilEnvRepo.saveNetworkEnvironemnt(envNetworkEnv);
            JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
            json.beginObject();
            json.add("status", "0");
            json.endObject();
            json.finish();
            return Emil.createResponse(Response.Status.OK, json.toString());

        } catch (Throwable t) {
            t.printStackTrace();
            return Emil.errorMessageResponse(t.getMessage());
        }
    }

    @Secured({Role.PUBLIC})
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNetworkEnvironments(@Context final HttpServletResponse response) {
        try {
            List<NetworkEnvironment> environments = emilEnvRepo.getNetworkEnvironments();
            return Response.status(Response.Status.OK).entity(environments).build();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation(t.getMessage()))
                    .build());
        }
    }

    @Secured({Role.PUBLIC})
    @GET
    @Path("/{envId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNetworkEnvironment(@PathParam("envId") String envId,
                                          @QueryParam("jsonUrl") boolean jsonUrl,
                                          @Context final HttpServletResponse response ) {
        try {
            NetworkEnvironment env = emilEnvRepo.getEmilNetworkEnvironmentById(envId);

            if(jsonUrl)
            {
                NetworkConfiguration config = new NetworkConfiguration();
                config.setNetwork(env.getNetwork());
                config.setUpstream_dns(env.getUpstream_dns());
                config.setGateway(env.getGateway());
                config.setArchived_internet_date(env.getNetworking().getArchiveInternetDate());
                NetworkConfiguration.DHCPConfiguration dhcp = new NetworkConfiguration.DHCPConfiguration();
                dhcp.setIp(env.getNetworking().getDhcpNetworkAddress());
                config.setDhcp(dhcp);

                List<NetworkConfiguration.EnvironmentNetworkConfiguration> ecs = new ArrayList<>();
                for(NetworkEnvironmentElement _env : env.getEmilEnvironments())
                {
                    NetworkConfiguration.EnvironmentNetworkConfiguration ec = new NetworkConfiguration.EnvironmentNetworkConfiguration();
                    ec.setMac(_env.getMacAddress());
                    ec.setIp(_env.getServerIp());
                    ec.setWildcard(_env.isWildcard());
                    if (_env.getFqdn() != null)
                        ec.getHostnames().add(_env.getFqdn());
                    ecs.add(ec);
                }
                config.setEnvironments(ecs);
                String networkJson = config.jsonValueWithoutRoot(true);

                File tmpfile = File.createTempFile("network.json", null, null);
                Files.write(tmpfile.toPath(), networkJson.getBytes() , StandardOpenOption.CREATE);

                BlobDescription blobDescription = new BlobDescription();
                blobDescription.setDataFromFile(tmpfile.toPath())
                        .setNamespace("random")
                        .setDescription("random")
                        .setName("network")
                        .setType(".json");

                BlobHandle handle = blobstore.put(blobDescription);
                String url = handle.toRestUrl(blobStoreRestAddress);

                JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
                json.beginObject();
                json.add("url", url);
                json.endObject();
                json.finish();
                return Emil.createResponse(Response.Status.OK, json.toString());
            }
            else
                return Response.status(Response.Status.OK).entity(env).build();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation(t.getMessage()))
                    .build());
        }
    }

    @Secured({Role.PUBLIC})
    @DELETE
    @Path("/{envId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteNetworkEnvironment(@PathParam("envId") String envId, @Context final HttpServletResponse response ) {
        try {
            emilEnvRepo.deleteEmilNetworkEnvironment(emilEnvRepo.getEmilNetworkEnvironmentById(envId));
            return Emil.createResponse(Response.Status.OK, "{\"data\": {\"status\": 0}}");
        } catch (Throwable t) {
            t.printStackTrace();
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation(t.getMessage()))
                    .build());
        }
    }

    @Secured({Role.PUBLIC})
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateNetworkEnvironment(NetworkEnvironment envNetworkEnv, @Context final HttpServletResponse response) {
        try {
            envNetworkEnv.getEmilEnvironments().forEach((networkElement -> {
                if(networkElement.getMacAddress() != null && networkElement.getMacAddress().equals(""))
                    networkElement.setMacAddress(NetworkUtils.getRandomHWAddress());
            }));
            emilEnvRepo.saveNetworkEnvironemnt(envNetworkEnv);
            return Emil.createResponse(Response.Status.OK, "{\"status\":\"0\"}");
        } catch (Throwable t) {
            t.printStackTrace();
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation(t.getMessage()))
                    .build());
        }
    }

}
