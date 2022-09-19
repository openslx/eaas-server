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

package de.bwl.bwfla.emil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;

import com.openslx.eaas.imagearchive.api.v2.common.ResolveOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.databind.AccessMethodV2;
import com.openslx.eaas.resolver.DataResolver;
import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.api.eaas.OutOfResourcesException_Exception;
import de.bwl.bwfla.api.eaas.QuotaExceededException_Exception;
import de.bwl.bwfla.api.eaas.SessionOptions;
import de.bwl.bwfla.api.emucomp.Container;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.common.services.rest.ErrorInformation;
import de.bwl.bwfla.common.services.sse.EventSink;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.common.utils.TaskStack;
import de.bwl.bwfla.configuration.converters.DurationPropertyConverter;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.emil.datatypes.snapshot.*;
import de.bwl.bwfla.emil.session.SessionManager;
import de.bwl.bwfla.emil.tasks.CreateSnapshotTask;
import de.bwl.bwfla.emil.utils.EventObserver;
import de.bwl.bwfla.emil.utils.TaskManager;
import de.bwl.bwfla.emil.utils.components.ContainerComponent;
import de.bwl.bwfla.emil.utils.components.UviComponent;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.emucomp.api.MediumType;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.api.emucomp.Machine;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.client.EaasClient;
import de.bwl.bwfla.emucomp.client.ComponentClient;
import de.bwl.bwfla.emil.utils.Snapshot;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;
import org.apache.tamaya.inject.api.WithPropertyConverter;


@ApplicationScoped
@Path("/components")
public class Components {
    @Inject
    private EaasClient eaasClient;
    
    @Inject
    private ComponentClient componentClient;

    @Inject
    private BlobStoreClient blobStoreClient;

    @Inject
    private SessionManager sessionManager = null;

    @Inject
    @Config(value = "ws.eaasgw")
    private String eaasGw;

    @Inject
    @Config(value = "ws.objectarchive")
    protected String objectArchive;
    
    @Inject
    @Config(value = "ws.softwarearchive")
    private String softwareArchive;

    @Inject
    @Config(value = "ws.blobstore")
    private String blobStoreWsAddress;

    @Inject
    @Config(value = "rest.blobstore")
    private String blobStoreRestAddress;

    @Inject
    @Config(value = "commonconf.serverdatadir")
    protected String serverdatadir;

    @Inject
    @Config(value = "ws.imagebuilder")
    private String imageBuilderAddress;

    @Inject
    @Config("components.client_timeout")
    protected Duration sessionExpirationTimeout;

    @Inject
    @Config("components.session_statistics.flush_delay")
    protected Duration sessionStatsFlushDelay;

    @Inject
    @Config(value = "emil.containerdata.imagebuilder.delay")
    @WithPropertyConverter(DurationPropertyConverter.class)
    private Duration imageBuilderDelay = null;

    @Inject
    @Config(value = "emil.containerdata.imagebuilder.timeout")
    @WithPropertyConverter(DurationPropertyConverter.class)
    private Duration imageBuilderTimeout = null;

    @Inject
    @Config(value = "emil.max_session_duration")
    @WithPropertyConverter(DurationPropertyConverter.class)
    private Duration maxSessionDuration = null;

    @Inject
    @Config(value = "emucomp.enable_pulseaudio", defaultValue = "false")
    private boolean pulseAudioAvailable = false;

    private SoftwareArchiveHelper swHelper;

    @Inject
    private ResourceProviderSelection resourceProviderSelection;

    protected static final Logger LOG = Logger.getLogger("eaas/components");

    @Inject
    private EmilEnvironmentRepository emilEnvRepo;

    /** Path to session statistics log */
    private java.nio.file.Path sessionStatsPath;

    /** Async writer for session statistics */
    private ComponentSessionStatsWriter sessionStatsWriter;

    private static ConcurrentHashMap<String, ComponentSession> sessions;

    @Inject
    private EmilObjectData objects;

    @Inject
	private UserSessions userSessions;

    @Inject
    private ContainerComponent containerHelper;

    @Inject
    private UviComponent uviHelper;

    @Inject
    @AuthenticatedUser
    private UserContext authenticatedUser;

    @Inject
    private ObjectRepository objectRepository;

    @Inject
    private TaskManager taskManager;

    @Inject
	@Config(value="objectarchive.user_archive_enabled")
	private boolean userArchiveEnabled;

    /** EaasWS web-service */
    private EaasWS eaas;

    /** ComponentProxy web-service */
    private Component component;

    @Resource(lookup = "java:jboss/ee/concurrency/scheduler/default")
    protected ManagedScheduledExecutorService scheduler;

    @Resource(lookup = "java:jboss/ee/concurrency/executor/io")
    protected ExecutorService executor;

    private ImageBuilder imagebuilder;
    private BlobStore blobstore;

    @PostConstruct
    private void init() {
        swHelper = new SoftwareArchiveHelper(softwareArchive);
        sessions = new ConcurrentHashMap<>();

        this.sessionStatsPath = Paths.get(serverdatadir).resolve("sessions.csv");
        try {
            this.sessionStatsWriter = new ComponentSessionStatsWriter(sessionStatsFlushDelay);
            this.sessionStatsWriter.schedule();
        }
        catch (Exception error) {
            throw new RuntimeException("Initializing session-statistics writer failed!", error);
        }

        LOG.info("Session statistics output: " + sessionStatsPath.toString());

        try {
            this.eaas = eaasClient.getEaasWSPort(eaasGw);
            this.component = componentClient.getPort(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"), Component.class);

            this.imagebuilder = ImageBuilderClient.get().getImageBuilderPort(imageBuilderAddress);
            this.blobstore = blobStoreClient.getBlobStorePort(blobStoreWsAddress);

        }
        catch (Exception error) {
            throw new RuntimeException("Constructing web-services failed!", error);
        }

        if (maxSessionDuration.isZero()) {
            LOG.info("Session duration control is disabled");
            maxSessionDuration = Duration.ofMillis(Long.MAX_VALUE);
        }
        else LOG.info("Session duration control is enabled! Max. session lifetime: " + maxSessionDuration.toString());
    }

    @PreDestroy
    private void destroy()
    {
        LOG.info("Terminating components frontend...");

        try {
            LOG.info("Closing session-statistics writer...");
            sessionStatsWriter.close();
        }
        catch (Exception error) {
            LOG.log(Level.WARNING, "Closing session-statistics writer failed!", error);
        }
    }

    protected void register(ComponentSession session)
    {
        sessions.put(session.getId(), session);
    }

    protected void unregister(ComponentSession session)
    {
        sessions.remove(session.getId());
        sessionStatsWriter.append(session);
    }
    
    ComponentResponse createComponent(ComponentRequest request) throws WebApplicationException
    {
        ComponentResponse result;

        final TaskStack cleanups = new TaskStack(LOG);
        final List<EventObserver> observer = new ArrayList<>();
        if (request.getUserId() == null)
            request.setUserId((authenticatedUser != null) ? authenticatedUser.getUserId() : null);

        if (request.getClass().equals(UviComponentRequest.class)) {
            try {
                MachineComponentRequest machineComponentRequest = uviHelper.prepare((UviComponentRequest) request, LOG);
                result = this.createMachineComponent(machineComponentRequest, cleanups, observer);
            }
            catch (BWFLAException error) {
                throw Components.newInternalError(error);
            }
        }
        else if (request.getClass().equals(MachineComponentRequest.class)) {
            result = this.createMachineComponent((MachineComponentRequest) request, cleanups, observer);
        } else if (request.getClass().equals(ContainerComponentRequest.class)) {
            result = this.createContainerComponent((ContainerComponentRequest) request, cleanups, observer);
        } else if (request.getClass().equals(SwitchComponentRequest.class)) {
            result = this.createSwitchComponent((SwitchComponentRequest) request, cleanups, observer);
        } else if (request.getClass().equals(NodeTcpComponentRequest.class)) {
            result = this.createNodeTcpComponent((NodeTcpComponentRequest) request, cleanups, observer);
        } else if (request.getClass().equals(SlirpComponentRequest.class)) {
            result = this.createSlirpComponent((SlirpComponentRequest) request, cleanups, observer);
        } else {
            throw new BadRequestException("Invalid component request");
        }

        final String cid = result.getId();
        final ComponentSession session = new ComponentSession(cid, request, cleanups, observer);
        cleanups.push("unregister-component/" + cid, () -> this.unregister(session));

        this.register(session);

        // Submit a trigger for session cleanup
        final Runnable trigger = new ComponentSessionCleanupTrigger(session, sessionExpirationTimeout);
        scheduler.schedule(trigger, sessionExpirationTimeout.toMillis(), TimeUnit.MILLISECONDS);

        return result;
    }
    
    /**
     * Creates and starts a new component (e.g emulator) the EaaS framework.
     * <p>
     * The type of the component is determined by the {@link ComponentRequest}'s
     * type annotation provided in the POSTed configuration.
     * 
     * @param request The configuration for the component to be created.
     * @return
     * 
     * @HTTP 400 if the given component configuration cannot be interpreted
     * @HTTP 400 if the request does not contain an environment id
     * @HTTP 400 if the given environment id is invalid or cannot be associated
     *       with an existing environment
     * @HTTP 429 if backend resources are exhausted or quota limits are reached
     * @HTTP 500 if the backend was not able to instantiate a new component
     * @HTTP 500 if any other internal server error occured
     * 
     * @documentationType de.bwl.bwfla.emil.datatypes.rest.MachineComponentResponse
     */
    @POST
    @Secured(roles={Role.PUBLIC})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentResponse createComponent(ComponentRequest request, @Context final HttpServletResponse response)
    {
        final ComponentResponse result = this.createComponent(request);
        response.setStatus(Response.Status.CREATED.getStatusCode());
        response.addHeader("Location", result.getId());
        return result;
    }

    protected ComponentResponse createSwitchComponent(SwitchComponentRequest desc, TaskStack cleanups, List<EventObserver> observer)
            throws WebApplicationException
    {
        try {
            String switchId = eaasClient.getEaasWSPort(eaasGw).createSession(desc.getConfig().value(false));
            cleanups.push("release-component/" + switchId, () -> eaas.releaseSession(switchId));
            return new ComponentResponse(switchId);
        }
        catch (Exception error) {
            cleanups.execute();
            throw Components.newInternalError(error);
        }
    }

    protected ComponentResponse createNodeTcpComponent(NodeTcpComponentRequest desc, TaskStack cleanups, List<EventObserver> observer)
        throws WebApplicationException {
        try {
            String nodeTcpId = eaasClient.getEaasWSPort(eaasGw).createSession(desc.getConfig().value(false));
            cleanups.push("release-component/" + nodeTcpId, () -> eaas.releaseSession(nodeTcpId));
            return new ComponentResponse(nodeTcpId);
        }
        catch (Exception error) {
            cleanups.execute();
            throw Components.newInternalError(error);
        }
    }

    protected ComponentResponse createSlirpComponent(SlirpComponentRequest desc, TaskStack cleanups, List<EventObserver> observer)
            throws WebApplicationException {
        try {
            VdeSlirpConfiguration slirpConfig = new VdeSlirpConfiguration();
            
            if (desc.getHwAddress() != null && !desc.getHwAddress().isEmpty()) {
                slirpConfig.setHwAddress(desc.getHwAddress());
            }

            slirpConfig.setNetwork(desc.getNetwork());

            if (desc.getNetmask() != null) {
                slirpConfig.setNetmask(desc.getNetmask());
            }

            if(desc.getGateway() != null)
                slirpConfig.setGateway(desc.getGateway());

            if (desc.getDnsServer() != null && !desc.getDnsServer().isEmpty()) {
                slirpConfig.setDnsServer(desc.getDnsServer());
            }
            slirpConfig.setDhcpEnabled(desc.isDhcp());

            String slirpId = eaasClient.getEaasWSPort(eaasGw).createSession(slirpConfig.value(false));
            cleanups.push("release-component/" + slirpId, () -> eaas.releaseSession(slirpId));
            return new ComponentResponse(slirpId);
        }
        catch (Exception error) {
            // Trigger cleanup tasks
            cleanups.execute();

            // Return error to the client...
            throw Components.newInternalError(error);
        }
    }

    @Deprecated
    protected ComponentResponse createContainerComponent(ContainerComponentRequest desc, TaskStack cleanups, List<EventObserver> observer)
            throws WebApplicationException
    {
        if (desc.getEnvironment() == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation("No environment id was given"))
                    .build());
        }

        try {
            final Environment chosenEnv = emilEnvRepo.getImageArchive()
                    .api()
                    .v2()
                    .containers()
                    .fetch(desc.getEnvironment());

            if (chosenEnv == null || !(chosenEnv instanceof ContainerConfiguration)) {
                throw new BadRequestException(Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorInformation("could not find environment: " + desc.getEnvironment()))
                        .build());
            }

            final ContainerConfiguration config = (ContainerConfiguration) chosenEnv;

            int numInputImages = 1;

            // Wrap external input files into images
            for (ComponentWithExternalFilesRequest.InputMedium medium : desc.getInputMedia()) {
                final FileSystemType fileSystemType = FileSystemType.EXT4;
                int sizeInMb = medium.getSizeInMb();
                if (sizeInMb <= 0)
                    sizeInMb = 1024;

                final ImageDescription description = new ImageDescription()
                        .setMediumType(MediumType.HDD)
                        .setPartitionTableType(PartitionTableType.NONE)
                        .setPartitionStartBlock(0)
                        .setFileSystemType(fileSystemType)
                        .setSizeInMb(sizeInMb);

                for (ComponentWithExternalFilesRequest.FileURL extfile : medium.getExtFiles()) {
                    final URL url = new URL(extfile.getUrl());
                    final ImageContentDescription entry = new ImageContentDescription()
                            .setAction(extfile.getAction())
                            .setArchiveFormat(ImageContentDescription.ArchiveFormat.TAR)
                            .setUrlDataSource(url);

                    if (extfile.hasName())
                        entry.setName(extfile.getName());
                    else entry.setName(Components.getFileName(url));

                    description.addContentEntry(entry);
                }

                for (ComponentWithExternalFilesRequest.FileData inlfile : medium.getInlineFiles()) {
                    final ImageContentDescription entry = new ImageContentDescription()
                            .setAction(inlfile.getAction())
                            .setArchiveFormat(inlfile.getCompressionFormat())
                            .setName(inlfile.getName())
                            .setByteArrayDataSource(inlfile.getData());

                    description.addContentEntry(entry);
                }

                // Build input image
                final BlobHandle blob = ImageBuilderClient.build(imagebuilder, description, imageBuilderTimeout, imageBuilderDelay).getBlobHandle();


                    final TaskStack.IRunnable cleanup = () -> {
                        try {
                            blobstore.delete(blob);
                        } catch (Exception error) {
                            LOG.log(Level.WARNING, "Deleting container's input image failed!\n", error);
                        }
                    };

                    cleanups.push("delete-blob/" + blob.getId(), cleanup);

                // Add input image to container's config

                final String bindingId = "input-" + numInputImages++;

                final ContainerConfiguration.Input input = new ContainerConfiguration.Input()
                        .setDestination(medium.getDestination())
                        .setBinding(bindingId);

                config.getInputs().add(input);

                final BlobStoreBinding binding = new BlobStoreBinding();
                binding.setUrl(blob.toRestUrl(blobStoreRestAddress, false));
                binding.setPartitionOffset(description.getPartitionOffset());
                binding.setFileSystemType(fileSystemType);
                binding.setId(bindingId);
                binding.setMountFS(true);

                config.getDataResources().add(binding);
            }

            final List<String> selectors = resourceProviderSelection.getSelectors(chosenEnv.getId());
            SessionOptions options = new SessionOptions();
            if(selectors != null && !selectors.isEmpty())
                options.getSelectors().addAll(selectors);

            if (authenticatedUser.getTenantId() != null)
                options.setTenantId(authenticatedUser.getTenantId());

            final String sessionId = eaas.createSessionWithOptions(chosenEnv.value(false), options);
            if (sessionId == null) {
                throw new InternalServerErrorException(Response.serverError()
                        .entity(new ErrorInformation("Session initialization has failed, obtained 'null' as session id."))
                        .build());
            }

            cleanups.push("release-component/" + sessionId, () -> eaas.releaseSession(sessionId));

            Container container = componentClient.getPort(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"), Container.class);
            container.startContainer(sessionId);
            return new ComponentResponse(sessionId);
        }
        catch (Exception error) {
            // Trigger cleanup tasks
            cleanups.execute();

            // Return error to the client...
            throw Components.newInternalError(error);
        }
    }

    private BlobStoreBinding buildExternalFilesMedia(ComponentWithExternalFilesRequest.InputMedium medium,
                                           TaskStack cleanups,
                                           int index) throws BWFLAException {
        int sizeInMb = medium.getSizeInMb();
        if (sizeInMb <= 0)
            sizeInMb = 1024;

        final ImageDescription description = new ImageDescription();
        description.setMediumType(medium.getMediumType());

        if (description.getMediumType() == MediumType.HDD) {
            description.setPartitionTableType(medium.getPartitiionTableType())
                    .setPartitionStartBlock(2048)
                    .setFileSystemType(medium.getFileSystemType())
                    .setSizeInMb(sizeInMb);
        }

        for (ComponentWithExternalFilesRequest.FileURL extfile : medium.getExtFiles()) {
            final ImageContentDescription entry = new ImageContentDescription()
                    .disableStrictNameChecks()
                    .setAction(extfile.getAction())
                    .setArchiveFormat(extfile.getCompressionFormat());

            try {
                final URL url = new URL(extfile.getUrl());
                entry.setUrlDataSource(url);
                if (extfile.hasName())
                    entry.setName(extfile.getName());
                else entry.setName(Components.getFileName(url));

            }
            catch (MalformedURLException error) {
                throw new BWFLAException(error);
            }

            description.addContentEntry(entry);
        }

        for (ComponentWithExternalFilesRequest.FileData inlfile : medium.getInlineFiles()) {
            final ImageContentDescription entry = new ImageContentDescription()
                    .setAction(inlfile.getAction())
                    .setArchiveFormat(inlfile.getCompressionFormat())
                    .setName(inlfile.getName())
                    .setByteArrayDataSource(inlfile.getData());

            description.addContentEntry(entry);
        }

        // Build input image
        final BlobHandle blob = ImageBuilderClient.build(imagebuilder, description, imageBuilderTimeout, imageBuilderDelay).getBlobHandle();
        {
            final TaskStack.IRunnable cleanup = () -> {
                try {
                    blobstore.delete(blob);
                } catch (Exception error) {
                    LOG.log(Level.WARNING, "Deleting machine's input image failed!\n", error);
                }
            };

            cleanups.push("delete-blob/" + blob.getId(), cleanup);
        }

        // Add input image to machine's config
        final BlobStoreBinding binding = new BlobStoreBinding();
        binding.setUrl(blob.toRestUrl(blobStoreRestAddress, false));
        if (description.getMediumType() != MediumType.CDROM)
            binding.setPartitionOffset(description.getPartitionOffset());
        binding.setFileSystemType(description.getFileSystemType());
        binding.setId("input-" + index);

        //FIXME remove if else and make it more elegant (maybe unite?)
        if (medium.getMediumType() == MediumType.CDROM)
            binding.setResourceType(Binding.ResourceType.ISO);
        else if (medium.getMediumType() == MediumType.HDD) {
            binding.setResourceType(Binding.ResourceType.DISK);
        }

        return binding;
    }

    public static String getFileName(URL url)
    {
        return Paths.get(url.getPath())
                .getFileName()
                .toString();
    }


    // vde switch identifies sessions by ethUrl, we need to store these
    protected void registerNetworkCleanupTask(String componentId, String switchId, String ethUrl) throws BWFLAException
    {
        LOG.info("disconnecting " + ethUrl);
        ComponentSession componentSession = sessions.get(componentId);
        if(componentSession == null)
            throw new BWFLAException("Component not registered " + componentId);

        TaskStack cleanups = componentSession.getCleanupTasks();
        cleanups.push( "disconnect/" + ethUrl,  () -> {
            try {
                componentClient.getNetworkSwitchPort(eaasGw).disconnect(switchId, ethUrl);
            } catch (BWFLAException error) {
                final String message = "Disconnecting component '" + componentId + "' from switch '" + switchId + "' failed!";
                LOG.log(Level.WARNING, message, error);
            }
        });
    }

    protected ComponentResponse createMachineComponent(MachineComponentRequest machineDescription, TaskStack cleanups, List<EventObserver> observer)
            throws WebApplicationException
    {
        if (machineDescription.getEnvironment() == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation("No environment id was given"))
                    .build());
        }

        try {
            EmilEnvironment emilEnv = this.emilEnvRepo.getEmilEnvironmentById(machineDescription.getEnvironment());
            Environment chosenEnv = emilEnvRepo.getImageArchive()
                    .api()
                    .v2()
                    .environments()
                    .fetch(machineDescription.getEnvironment());

            if (chosenEnv == null) {
                throw new BadRequestException(Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorInformation("could not find environment: " + machineDescription.getEnvironment()))
                        .build());
            }

            final MachineConfiguration config = (MachineConfiguration) chosenEnv;

            EmulationEnvironmentHelper.setKbdConfig(config,
                    machineDescription.getKeyboardLayout(),
                    machineDescription.getKeyboardModel());

            if(emilEnv != null && emilEnv.getTimeContext() != null)
                EmulationEnvironmentHelper.setTimeContext(config, emilEnv.getTimeContext());

            if(emilEnv != null && emilEnv.isEnableRelativeMouse())
                EmulationEnvironmentHelper.enableRelativeMouse(config);

            int numInputImages = 1;

            if(machineDescription.getLinuxRuntimeData() != null && machineDescription.getLinuxRuntimeData().getUserContainerEnvironment() != null && !machineDescription.getLinuxRuntimeData().getUserContainerEnvironment().isEmpty())
            {
                final var runtime = machineDescription.getLinuxRuntimeData();
                final var ociConf = (OciContainerConfiguration) emilEnvRepo.getImageArchive()
                        .api()
                        .v2()
                        .containers()
                        .fetch(runtime.getUserContainerEnvironment());

                LOG.warning(ociConf.jsonValueWithoutRoot(true));

                ImageDescription imageDescription = null;
                try {
                    imageDescription = containerHelper.prepareContainerRuntimeImage(ociConf, machineDescription.getLinuxRuntimeData(), machineDescription.getInputMedia());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final BlobHandle blob = ImageBuilderClient.build(imagebuilder, imageDescription, imageBuilderTimeout, imageBuilderDelay).getBlobHandle();

                final BlobStoreBinding binding = new BlobStoreBinding();
                binding.setUrl(blob.toRestUrl(blobStoreRestAddress, false));
                binding.setPartitionOffset(imageDescription.getPartitionOffset());
                binding.setFileSystemType(imageDescription.getFileSystemType());
                binding.setId("eaas-job");

                ImageArchiveBinding rootfs = null;
                for(AbstractDataResource r : ociConf.getDataResources())
                {
                    if(r.getId().equals("rootfs"))
                        rootfs = ((ImageArchiveBinding)r);
                }
                if(rootfs == null)
                    throw new BadRequestException(Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorInformation("could not find rootfs "))
                            .build());
                rootfs.setFileSystemType(null);
                this.addBindingToEnvironment(config, rootfs, this.toDriveType(MediumType.HDD));


                this.addBindingToEnvironment(config, binding, this.toDriveType(MediumType.HDD));
                config.setOutputBindingId(binding.getId());
            }
            else {

                // audio should only be set for non container instances.
                checkAndUpdateEnvironmentDefaults(chosenEnv);

                // Wrap external input files into images
                for (ComponentWithExternalFilesRequest.InputMedium medium : machineDescription.getInputMedia())
                {
                    if(medium == null) // handle old landing-page/UI bug
                        continue;

                    BlobStoreBinding binding = buildExternalFilesMedia(medium, cleanups, numInputImages++);
                    this.addBindingToEnvironment(config, binding, this.toDriveType(medium.getMediumType()));
                    config.setOutputBindingId(binding.getId());

                    // TODO: Extend MachineConfiguration to support multiple input/output-images!
                    break;
                }
            }

            for(MachineComponentRequest.UserMedium uMedia : machineDescription.getUserMedia())
            {
                connectMedia(config, uMedia);
            }

            Integer driveId = null;
            // hack: we need to initialize the user archive:
            objectRepository.archives().list();
            if (machineDescription.getObject() != null) {
                driveId = addObjectToEnvironment(chosenEnv, machineDescription.getObjectArchive(), machineDescription.getObject());
            } else if (machineDescription.getSoftware() != null) {
                final var software = this.getSoftwarePackage(machineDescription.getSoftware());
                driveId = addObjectToEnvironment(chosenEnv, software.getArchive(), software.getObjectId());
            }

            final List<String> selectors = resourceProviderSelection.getSelectors(chosenEnv.getId());
            SessionOptions options = new SessionOptions();
            if(selectors != null && !selectors.isEmpty())
                options.getSelectors().addAll(selectors);

            if((!((MachineConfiguration) chosenEnv).hasCheckpointBindingId() && emilEnv.getNetworking() != null && emilEnv.getNetworking().isConnectEnvs())
                    || ((MachineConfiguration) chosenEnv).isLinuxRuntime()) {
                String hwAddress;
                if (machineDescription.getNic() == null) {
                    LOG.warning("HWAddress is null! Using random..." );
                    hwAddress = NetworkUtils.getRandomHWAddress();
                } else {
                    hwAddress = machineDescription.getNic();
                }

                List<Nic> nics = ((MachineConfiguration) chosenEnv).getNic();
                Nic nic = new Nic();
                nic.setHwaddress(hwAddress);
                nics.clear();
                nics.add(nic);
            }

            if(machineDescription.isLockEnvironment()) {
                options.setLockEnvironment(true);
            }

            if (authenticatedUser.getTenantId() != null)
                options.setTenantId(authenticatedUser.getTenantId());

            if(machineDescription.isHeadless())
            {
                MachineConfiguration conf = (MachineConfiguration) chosenEnv;
                if(conf.getUiOptions() == null)
                    conf.setUiOptions(new UiOptions());

                conf.getUiOptions().setForwarding_system("HEADLESS");
            }

            if (machineDescription.hasOutput()){
                EmulationEnvironmentHelper.registerDriveForOutput((MachineConfiguration) chosenEnv, machineDescription.getOutputDriveId());
            }

            final String sessionId = eaas.createSessionWithOptions(chosenEnv.value(false), options);
            if (sessionId == null) {
                throw new InternalServerErrorException(Response.serverError()
                        .entity(new ErrorInformation("Session initialization has failed, obtained 'null' as session id."))
                        .build());
            }

            cleanups.push("release-component/" + sessionId, () -> eaas.releaseSession(sessionId));

            Machine machine = componentClient.getPort(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"), Machine.class);
            machine.start(sessionId);

            List<MachineComponentResponse.RemovableMedia> removableMedia = getRemovableMedialist((MachineConfiguration)chosenEnv);

            // Register server-sent-event source
            {
                final String srcurl = component.getEventSourceUrl(sessionId);
                observer.add(new EventObserver(srcurl, LOG));
            }

            return new MachineComponentResponse(sessionId, removableMedia);
        }
        catch (Exception error) {
            // Trigger cleanup tasks
            cleanups.execute();
            LOG.log(Level.SEVERE, "Components create machine failed", error);
            // Return error to the client...
            throw Components.newInternalError(error);
        }
    }

    private void checkAndUpdateEnvironmentDefaults(Environment env)
    {
        MachineConfiguration mc = (MachineConfiguration) env;
        if (mc.getUiOptions() != null) {
            if((mc.getUiOptions().getAudio_system() == null
                    || mc.getUiOptions().getAudio_system().isEmpty()) && this.pulseAudioAvailable)
                mc.getUiOptions().setAudio_system("webrtc");
        }
    }

    private void connectMedia(MachineConfiguration env, MachineComponentRequest.UserMedium userMedium) throws BWFLAException {
        if(userMedium.getMediumType() != MediumType.CDROM && userMedium.getMediumType() != MediumType.HDD)
        {
            throw new BWFLAException("user media has limited support. mediaType: " + userMedium.getMediumType() + " not supported yet");
        }

        final BlobStoreBinding binding = new BlobStoreBinding();
        binding.setId(UUID.randomUUID().toString());

        String url = userMedium.getUrl();
        if(url == null)
            throw new BWFLAException("user media must contain a url");

        String name = userMedium.getName();

        binding.setUrl(url);
        binding.setLocalAlias(name);

        try {
            addBindingToEnvironment(env, binding, this.toDriveType(userMedium.getMediumType()));
        } catch (JAXBException e) {
            throw new BWFLAException(e);
        }
    }

    protected SoftwarePackage getSoftwarePackage(String softwareId)
            throws BWFLAException {
        // Start with object ID referenced by the passed software ID.
        final SoftwarePackage software = swHelper
                .getSoftwarePackageById(softwareId);
        if (software == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation(
                            "Could not find software with ID: " + softwareId))
                    .build());
        }

        return software;
    }

    protected int addObjectToEnvironment(Environment chosenEnv, String archiveId, String objectId)
            throws BWFLAException, JAXBException {

        LOG.info("adding object id: " + objectId);
        if(EmulationEnvironmentHelper.hasObjectBinding((MachineConfiguration)chosenEnv, objectId))
        {
            return EmulationEnvironmentHelper.getDriveId((MachineConfiguration)chosenEnv, objectId);
        }

        if(userArchiveEnabled && (archiveId == null || archiveId.equals("default")))
        {
            if(authenticatedUser == null || authenticatedUser.getUserId() == null)
                archiveId = "default";
            else
                archiveId = authenticatedUser.getUserId();
        }

        FileCollection fc = objects.getFileCollection(archiveId, objectId);
        ObjectArchiveBinding binding = new ObjectArchiveBinding(objectArchive, archiveId, objectId);

        int driveId = EmulationEnvironmentHelper.addArchiveBinding((MachineConfiguration) chosenEnv, binding, fc);
        return driveId;
    }

    protected int addBindingToEnvironment(MachineConfiguration config, Binding binding, Drive.DriveType drive)
            throws BWFLAException, JAXBException
    {
        config.getAbstractDataResource().add(binding);
        return EmulationEnvironmentHelper.registerDrive(config, binding.getId(), null, drive);
    }

    protected Drive.DriveType toDriveType(MediumType medium)
    {
        switch (medium) {
            case HDD:
                return Drive.DriveType.DISK;
            case CDROM:
                return Drive.DriveType.CDROM;
        }

        throw new IllegalStateException();
    }

    /**
     * Returns the API representation of the given component.
     * <p>
     * @implNote Note that due to the way media attachment is implemented right
     *           now, there is no way to determine the driveId field of
     *           ComponentResponse
     * 
     * @param componentId The component id to query
     * @return The API representation of the component.
     * 
     * @HTTP 500 on any error, even if the component does not exist
     * 
     * @documentationType de.bwl.bwfla.emil.datatypes.rest.MachineComponentResponse
     */
    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}")
    public ComponentResponse getComponent(
            @PathParam("componentId") String componentId) {
        try {
            if (!component.getComponentType(componentId)
                    .equals("machine")) {
                return new ComponentResponse(componentId);
            }

            // TODO: find a way to get the correct driveId here
            return new MachineComponentResponse(componentId, new ArrayList<>());
        } catch (BWFLAException e) {
            throw new InternalServerErrorException(
                    "Server has encountered an internal error: "
                            + e.getMessage(),
                    e);
        }
    }

    private interface IResolver
    {
        String resolve(String id, ResolveOptionsV2 options) throws BWFLAException;
    }

    private String resolveImage(String resourceId, ResolveOptionsV2 options)
    {
        final var archive = emilEnvRepo.getImageArchive()
                .api()
                .v2();

        // resource can be located at any of the following endpoints
        final var resolvers = new IResolver[] {
                archive.images()::resolve,
                archive.roms()::resolve,
                archive.checkpoints()::resolve,
        };

        for (var resolver : resolvers) {
            try {
                return resolver.resolve(resourceId, options);
            }
            catch (Exception error) {
                // Try next one!
            }
        }

        throw new NotFoundException();
    }

    private String resolveObjectResource(String resourceId, ResolveOptionsV2 options) throws Exception
    {
        // Expected: <archive-id>/<object-id>/<resource-id>
        final var ids = resourceId.split("/");
        if (ids.length != 3)
            throw new BadRequestException();

        return objectRepository.helper()
                .resolveObjectResource(ids[0], ids[1], ids[2], options.method().name());
    }

    private String resolveResource(String kind, String resourceId, ResolveOptionsV2 options) throws Exception
    {
        resourceId = DataResolver.decode(resourceId);
        switch (kind) {
            case "images":
                return this.resolveImage(resourceId, options);
            case "objects":
                return this.resolveObjectResource(resourceId, options);
            default:
                throw new BadRequestException();
        }
    }

    private Response resolveResource(String componentId, String kind, String resourceId, AccessMethodV2 method)
    {
        // FIXME: currently, components access images already during session
        //        initialization and before we know their component IDs here!
        //if (!sessions.containsKey(componentId)) {
        //    throw new NotFoundException();

        // TODO: check if requested resource is allowed for given component!

        final var options = new ResolveOptionsV2()
                .setLifetime(1L, TimeUnit.HOURS)
                .setMethod(method);

        try {
            final var location = this.resolveResource(kind, resourceId, options);
            LOG.info("Resolving '" + resourceId + "' -> " + method.name() + " " + location);
            return Response.temporaryRedirect(new URI(location))
                    .build();
        }
        catch (Exception error) {
            LOG.log(Level.WARNING, "Resolving '" + resourceId + "' failed!", error);
            throw new NotFoundException();
        }
    }

    @GET
    @Path("/{compid}/{kind}/{resource: .+}/url")
    public Response resolveResourceGET(@PathParam("compid") String compid,
                                       @PathParam("kind") String kind,
                                       @PathParam("resource") String resource)
    {
        return this.resolveResource(compid, kind, resource, AccessMethodV2.GET);
    }

    @HEAD
    @Path("/{compid}/{kind}/{resource: .+}/url")
    public Response resolveResourceHEAD(@PathParam("compid") String compid,
                                        @PathParam("kind") String kind,
                                        @PathParam("resource") String resource)
    {
        return this.resolveResource(compid, kind, resource, AccessMethodV2.HEAD);
    }


    public boolean hasComponentSession(String componentId)
    {
        ComponentSession session = sessions.get(componentId);
        if(session == null)
            return false;

        return true;
    }

    /**
     * Sends a keepalive request to the component. Keepalives have to be sent
     * in regular intervals or the component will automatically terminate.
     * The interval at which the keepalive heartbeat is expected is determined
     * by the current backend's configuration parameters.
     * There is currently no way to access this configuration parameter from
     * the frontend.
     * 
     * @param componentId The component id to send the keepalive to
     * 
     * @HTTP 404 if the component id cannot be resolved to a concrete component
     */
    @POST
    @Path("/{componentId}/keepalive")
    public void keepalive(@PathParam("componentId") String componentId) {
        final ComponentSession session = sessions.get(componentId);
        if (session == null)
            throw new NotFoundException();

        try {
            session.keepalive();
        }
        catch (BWFLAException error) {
            throw new NotFoundException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorInformation("Could not forward keepalive request.", error.getMessage()))
                    .build());
        }
    }

    /**
     * Determines the OK state of the component. As component are automatically
     * set in a usable state when they are started, there is currently no other
     * state than "OK", in which case an empty string is returned from this
     * call. Any other result indicates a failed component and will result in
     * a 500 Internal Server Error status code.
     * 
     * @param componentId The component id to query
     * @return An empty string if the component is up and running
     * 
     * @HTTP 500 if the component has failed or cannot be found
     */
    @GET
    @Path("/{componentId}/state")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentResponse getState(@PathParam("componentId") String componentId) {
        try {
            final String state = component.getState(componentId);
            return new ComponentStateResponse(componentId, state);
        } catch (BWFLAException e) {
            // TODO: 400 if the component id is syntactically wrong
            // TODO: 404 if the component cannot be found
            throw new InternalServerErrorException(Response.serverError()
                    .entity("The component associated with your session has failed.")
                    .build(), e);
        }
    }

    /**
     * Returns a JSON object that represents the available control URLs for 
     * the given component. Control URLs can be used by a UI implementation
     * to access certain input devices. The return object's members correspond
     * to available input channels, e.g. "guacamole" for user-interactive access
     * to screen and keyboard. The associated URL is channel-specific and
     * represents a network endpoint to which a UI implementing this channel
     * can talk to. 
     * 
     * @param componentId The component id to get the control urls for
     * @return an mapping from channel names to URLs
     * 
     * @HTTP 500 if any error occurs
     */
    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/controlurls")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, URI> getControlUrls(@PathParam("componentId") String componentId) {
        try {
            return component.getControlUrls(componentId).getEntry().stream().collect(Collectors.toMap(e -> e.getKey(), e -> URI.create(e.getValue())));
        } catch (BWFLAException e) {
            throw new InternalServerErrorException(
                    "Server has encountered an internal error: "
                            + e.getMessage(),
                    e);
        }
    }

    /**
     * Returns the result of the running session, if any is available.
     *
     * @param componentId The session ID to get the result for.
     * @return A redirection to the result.
     *
     * @HTTP 500 if any error occurs
     */
    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/result")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResult(@PathParam("componentId") String componentId)
    {
        try {
            final BlobHandle handle = component.getResult(componentId);
            if (handle == null)
                throw new NotFoundException();

            // Actual result's location
            String location = "";
            if (blobStoreRestAddress.contains("http://eaas:8080"))
                location = handle.toRestUrl(blobStoreRestAddress.replace("http://eaas:8080", ""));
            else
                location = handle.toRestUrl(blobStoreRestAddress);

            // Set response headers...
            final Response response = Response.status(Response.Status.TEMPORARY_REDIRECT)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Location", new URI(location))
                    .build();

            return response;
        }
        catch (BWFLAException | URISyntaxException exception) {
            throw new InternalServerErrorException("Retrieving the result failed!", exception);
        }
    }

    /**
     * Takes and returns a screenshot of the running emulation session.
     * The response will automatically set the Content-Disposition header
     * to instruct clients to save the file as "screenshot.png"
     * 
     * @param componentId The session ID to take the screenshot of.
     * @return A HTML response containing the PNG encoded screenshot.
     *
     * @HTTP 500 if any error occurs
     * @HTTP 408 if no screenshot could be retrieved from the emulator
     */
    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/screenshot")
    @Produces("image/png")
    public InputStream screenshot(@PathParam("componentId") String componentId,
            @Context HttpServletResponse servletResponse) {
        try {
            final Machine machine = componentClient.getMachinePort(eaasGw);
            String state = machine.getEmulatorState(componentId);
            if (!state.equalsIgnoreCase(EmuCompState.EMULATOR_RUNNING.value())) {
                throw new NotFoundException();
            } else {
                int numRetries = 20;
                DataHandler dh = null;
                machine.takeScreenshot(componentId);

                // Wait for the screenshot to become available
                for (; numRetries > 0 && dh == null; --numRetries) {
                    dh = machine.getNextScreenshot(componentId);
                    Thread.sleep(250);
                }
                if (numRetries <= 0) {
                    throw new ServerErrorException(Response.Status.BAD_REQUEST);
                }

                servletResponse.setHeader("Content-Disposition",
                        "attachment; filename=\"screenshot.png\"");
                return dh.getInputStream(); 
            }
        } catch (Exception exception) {
            throw new InternalServerErrorException(exception);
        }
    }

    /**
     * Stops a machine component.
     *
     * @param componentId The session ID to take the screenshot of.
     * @return JSON with "status=0"
     *
     * @HTTP 500 if any error occurs
     */
    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public ProcessResultUrl stop(@PathParam("componentId") String componentId,
                             @Context HttpServletResponse servletResponse) {
        try {
            final ComponentSession session = sessions.get(componentId);
            if(session == null)
                throw new BWFLAException("session not found");
            final ComponentRequest request = session.getRequest();
            if (request instanceof MachineComponentRequest) {
                final Machine machine = componentClient.getMachinePort(eaasGw);
                String url = machine.stop(componentId);
                ProcessResultUrl result = new ProcessResultUrl();
                result.setUrl(url);
                return result;
            }
            else if (request instanceof ContainerComponentRequest) {
                final Container container = componentClient.getContainerPort(eaasGw);
                container.stopContainer(componentId);
            }
        } catch (BWFLAException e) {
            return null;
        }
        return null;
    }

    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/downloadPrintJob")
    @Produces("application/pdf")
    public InputStream downloadPrintJob(@PathParam("componentId") String componentId,
                                        @QueryParam("label")  String label,
                                        @Context HttpServletResponse servletResponse)
    {
        try {
            final Machine machine = componentClient.getMachinePort(eaasGw);
            List<de.bwl.bwfla.api.emucomp.PrintJob> pj = machine.getPrintJobs(componentId);
            for(de.bwl.bwfla.api.emucomp.PrintJob p : pj)
            {
                String _label = URLDecoder.decode(label, "UTF-8");
                if(p.getLabel().equals(_label))
                {
                    servletResponse.setHeader("Content-Disposition",
                        "attachment; filename=\"" + p.getLabel() + "\"");
                    return p.getDataHandler().getInputStream();
                }
            }
            throw new InternalServerErrorException("inconsistent data or query. req: " + label + " pj.size " + pj.size());
        } catch (BWFLAException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalServerErrorException(e);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalServerErrorException(e);
        }
    }

    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void events(@PathParam("componentId") String componentId, @Context SseEventSink sink, @Context Sse sse)
    {
        final ComponentSession session = sessions.get(componentId);
        if (session == null)
            throw new NotFoundException("Session not found: " + componentId);

        if (session.hasEventSink()) {
            LOG.info("An event-sink is already registered! Updating...");
            session.getEventSink()
                    .reset(sink, sse);
        }
        else {
            LOG.warning("Start forwarding server-sent-events for session " + componentId);
            session.setEventSink(sink, sse);
        }
    }

    @GET
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/printJobs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> printJobs(@PathParam("componentId") String componentId) {
        List<String> result = new ArrayList<>();
        try {
            final Machine machine = componentClient.getMachinePort(eaasGw);
            List<de.bwl.bwfla.api.emucomp.PrintJob> pj = machine.getPrintJobs(componentId);
            for(de.bwl.bwfla.api.emucomp.PrintJob p : pj)
            {
                result.add(p.getLabel());
            }

        } catch (BWFLAException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return result;
    }


    public TaskStateResponse updateTask(TaskStateResponse taskStateResponse) {
        return taskManager.lookup(taskStateResponse.getTaskId(), true);
    }

    public TaskStateResponse snapshotAsync(String componentId, SnapshotRequest request, UserContext userContext) throws Exception {
         Snapshot snapshot = new Snapshot(componentClient.getMachinePort(eaasGw), emilEnvRepo, objects, userSessions);
         return new TaskStateResponse(taskManager.submitTask(new CreateSnapshotTask(snapshot, componentId, request, false, userContext)));
    }

    public SnapshotResponse snapshot(String componentId, SnapshotRequest request, UserContext userContext) throws Exception{
        Snapshot snapshot = new Snapshot(componentClient.getMachinePort(eaasGw), emilEnvRepo, objects, userSessions);
        return snapshot.handleSnapshotRequest(componentId, request, false, userContext);
    }

    /**
     * Save environment
     * Saves the current disk state in a given image archive
     * @param componentId The component's ID to snapshot.
     * @param request {@link SnapshotRequest}
     * @return A JSON response containing the result message.
     */
    @POST
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/async/snapshot")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse snapshotAsync(@PathParam("componentId") String componentId, SnapshotRequest request)
    {
        try {
            return snapshotAsync(componentId, request, getUserContext());
        } catch (Exception e) {
             final BWFLAException error = (e instanceof BWFLAException) ?
                    (BWFLAException) e : new BWFLAException(e);
            return new TaskStateResponse(error);
        }
    }

    /**
     * Save environment
     * Saves the current disk state in a given image archive
     * @param componentId The component's ID to snapshot.
     * @param request {@link SnapshotRequest}
     * @return A JSON response containing the result message.
     */
    @POST
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/snapshot")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SnapshotResponse snapshot(@PathParam("componentId") String componentId, SnapshotRequest request)
    {
        try {
            Snapshot snapshot = new Snapshot(componentClient.getMachinePort(eaasGw), emilEnvRepo, objects, userSessions);
            return snapshot.handleSnapshotRequest(componentId, request, false, getUserContext());
        } catch (Exception e) {
            final BWFLAException error = (e instanceof BWFLAException) ?
                    (BWFLAException) e : new BWFLAException(e);
            return new SnapshotResponse(error);
        }
    }

    /**
     * Creates a checkpoint of a running emulation session.
     * @param componentId The component's ID to checkpoint.
     * @param request {@link SnapshotRequest}
     * @return A JSON response containing the result message.
     */
    @POST
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/async/checkpoint")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaskStateResponse checkpointAsync(@PathParam("componentId") String componentId, SnapshotRequest request)
    {
        try {
            Snapshot snapshot = new Snapshot(componentClient.getMachinePort(eaasGw), emilEnvRepo, objects, userSessions);
            return new TaskStateResponse(taskManager.submitTask(new CreateSnapshotTask(snapshot, componentId, request, true, getUserContext())));
        } catch (Exception e) {
             final BWFLAException error = (e instanceof BWFLAException) ?
                    (BWFLAException) e : new BWFLAException(e);
            return new TaskStateResponse(error);
        }
    }

    /**
     * Creates a checkpoint of a running emulation session.
     * @param componentId The component's ID to checkpoint.
     * @param request {@link SnapshotRequest}
     * @return A JSON response containing the result message.
     */
    @POST
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/checkpoint")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SnapshotResponse checkpoint(@PathParam("componentId") String componentId, SnapshotRequest request)
    {
        try {
            Snapshot snapshot = new Snapshot(componentClient.getMachinePort(eaasGw), emilEnvRepo, objects, userSessions);
            return snapshot.handleSnapshotRequest(componentId, request, true, getUserContext());
        } catch (Exception e) {
            final BWFLAException error = (e instanceof BWFLAException) ?
                    (BWFLAException) e : new BWFLAException(e);
            return new SnapshotResponse(error);
        }
    }


    /**
     * Changes digital object's medium in an emulation session. Since a digital object
     * can be composed of multiple media, a medium's name must also be specified.
     * @param changeRequest {@link MediaChangeRequest}
     * @return A JSON response containing the result message.
     */
    @POST
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}/changeMedia")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeMedia(@PathParam("componentId") String componentId,
                                MediaChangeRequest changeRequest)
    {
        String chosenObjRef;

        if(changeRequest.getLabel() == null || changeRequest.getLabel().isEmpty()){
            try {
                final Machine machine = componentClient.getMachinePort(eaasGw);
                machine.changeMedium(componentId, Integer.parseInt(changeRequest.getDriveId()), null);
            } catch (NumberFormatException | BWFLAException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
                return Emil.internalErrorResponse("could not initialize eaas gateway: " + e.getMessage());
            }
            return Emil.successMessageResponse("change to empty drive successful");
        }

        String objurl = null;
        try {
            FileCollection fc  = objects.getFileCollection(changeRequest.getArchiveId(),
                    changeRequest.getObjectId());

            for(FileCollectionEntry fce : fc.files)
                if(fce.getId().equals(changeRequest.getLabel()))
                {
                    objurl = fce.getId();
                    break;
                }
        } catch (NoSuchElementException | BWFLAException e1) {
            LOG.log(Level.SEVERE, e1.getMessage(), e1);
            return Emil.internalErrorResponse("failed loading object meta data");
        }

        if(objurl == null)
            return Emil.internalErrorResponse("could not resolve object label");

        try {
            final Machine machine = componentClient.getMachinePort(eaasGw);
            machine.changeMedium(componentId, Integer.parseInt(changeRequest.getDriveId()),
                    "binding://" + changeRequest.getObjectId() + "/" + objurl);
        } catch (NumberFormatException | BWFLAException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return Emil.internalErrorResponse("Failed to submit change media request " + e.getMessage());
        }

        return Emil.successMessageResponse("");
    }

    /**
     * Release a stopped component session.
     * @param componentId The component's ID to release.
     */
    @DELETE
    @Secured(roles={Role.PUBLIC})
    @Path("/{componentId}")
    public void releaseComponent(@PathParam("componentId") String componentId) {
        ComponentSession session = sessions.get(componentId);
        if (session == null) {
            final Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorInformation("Could not find component."))
                    .build();

            throw new NotFoundException(response);
        }

        session.release();
    }


    private List<MachineComponentResponse.RemovableMedia> getRemovableMedialist(MachineConfiguration env)
    {
        List<MachineComponentResponse.RemovableMedia> result = new ArrayList<>();
        for(AbstractDataResource binding : env.getAbstractDataResource()) {
            if (!(binding instanceof ObjectArchiveBinding))
                continue;

            ObjectArchiveBinding objectArchiveBinding = (ObjectArchiveBinding) binding;
            int driveIndex = EmulationEnvironmentHelper.getDriveId(env, objectArchiveBinding.getObjectId());
            Drive d = EmulationEnvironmentHelper.getDrive(env, driveIndex);
            if (d == null) {
                LOG.warning("could not resolve drive for objectId: " + objectArchiveBinding.getObjectId());
                continue;
            }

            if (d.getType() != Drive.DriveType.FLOPPY && d.getType() != Drive.DriveType.CDROM)
            {
                LOG.warning("unsupported drive type: " + d.getType().value() + " for objectId " + objectArchiveBinding.getObjectId());
                continue;
            }

            MachineComponentResponse.RemovableMedia rm = new MachineComponentResponse.RemovableMedia();
            rm.setArchive(objectArchiveBinding.getArchive());
            rm.setDriveIndex(driveIndex + "");
            rm.setId(objectArchiveBinding.getObjectId());
            result.add(rm);
        }
        return result;
    }

    /* ==================== Internal Helpers ==================== */

    static WebApplicationException newInternalError(Exception error)
    {
        Response.Status status = null;
        String message = null;

        if (error instanceof OutOfResourcesException_Exception || error instanceof QuotaExceededException_Exception) {
            status = Response.Status.TOO_MANY_REQUESTS;
            message = "RESOURCES-EXHAUSTED-ERROR: " + error.getMessage();
        }
        else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
            message = "INTERNAL-SERVER-ERROR: " + error.getMessage();
        }

        final Response response = Response.status(status)
                .entity(new ErrorInformation(message))
                .build();

        return new WebApplicationException(error, response);
    }

    private UserContext getUserContext() {
		return (authenticatedUser != null) ? authenticatedUser.clone() : new UserContext();
	}

    private static long timestamp()
    {
        return System.currentTimeMillis();
    }

    private class ComponentSession
    {
        private final AtomicLong keepaliveTimestamp;
        private final long startTimestamp;
        private final AtomicBoolean released;

        private final String id;
        private final ComponentRequest request;
        private TaskStack tasks;
        private final List<EventObserver> observers;
        private EventSink esink;


        public ComponentSession(String id, ComponentRequest request, TaskStack tasks, List<EventObserver> observers)
        {
            this.keepaliveTimestamp = new AtomicLong(0);
            this.startTimestamp = Components.timestamp();
            this.released = new AtomicBoolean(false);

            this.id = id;
            this.request = request;
            this.tasks = tasks;
            this.observers = observers;

            LOG.info("Session for component ID '" + id + "' created");
        }

        public TaskStack getCleanupTasks()
        {
            if(tasks == null)
                tasks = new TaskStack(LOG);

            return tasks;
        }

        public void keepalive() throws BWFLAException
        {
            keepaliveTimestamp.set(Components.timestamp());
            component.keepalive(id);
        }

        public void release()
        {
            if (released.getAndSet(true))
                return;  // Release was already called!

            if (!observers.isEmpty())
                LOG.info("Stopping " + observers.size() + " event-observer(s) for session '" + id + "'...");

            for (EventObserver observer : observers)
                observer.stop();

//             if(request.getUserContext() != null && request instanceof MachineComponentRequest)
//            {
//                MachineComponentRequest machineRequest = (MachineComponentRequest)request;
//                sendKeepAlive();
//                try {
//                    Snapshot snapshot = createSnapshot(this.id);
//                    SaveUserSessionRequest userReq = new SaveUserSessionRequest(machineRequest.getEnvironment(), machineRequest.getObject());
//                    userReq.setUserContext(request.getUserContext());
//                    emilEnvRepo.saveAsUserSession(snapshot, userReq);
//                } catch (BWFLAException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            // Run all tasks in reverse order
            LOG.info("Releasing component '" + id + "'...");
            if (tasks.execute())
                LOG.info("Component '" + id + "' released");
            else LOG.log(Level.WARNING, "Releasing component '" + id + "' failed!");
        }

        public String getId()
        {
            return id;
        }

        public ComponentRequest getRequest()
        {
            return request;
        }

        public long getKeepaliveTimestamp()
        {
            return keepaliveTimestamp.get();
        }

        public long getStartTimestamp()
        {
            return startTimestamp;
        }

        public void setEventSink(SseEventSink sink, Sse sse)
        {
            this.esink = new EventSink(sink, sse);

            final Consumer<InboundSseEvent> forwarder = (input) -> {
                final OutboundSseEvent output = esink.newEventBuilder()
                        .name(input.getName())
                        .data(input.readData())
                        .build();

                esink.send(output);
            };

            if (!observers.isEmpty())
                LOG.info("Starting " + observers.size() + " event-observer(s) for session '" + id + "'...");

            for (EventObserver observer : observers) {
                observer.register(forwarder)
                        .start();
            }

            // Send client notification when this session will expire...
            if (maxSessionDuration.toMillis() < Long.MAX_VALUE) {
                final Runnable task = () -> {
                    final long duration = Components.timestamp() - this.getStartTimestamp();
                    final long lifetime = maxSessionDuration.toMillis();
                    final SessionWillExpireNotification notification = new SessionWillExpireNotification(duration, lifetime);
                    final OutboundSseEvent event = esink.newEventBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .name(SessionWillExpireNotification.name())
                            .data(notification)
                            .build();

                    esink.send(event);
                };

                final Runnable trigger = () -> executor.execute(task);
                scheduler.schedule(trigger, 10, TimeUnit.SECONDS);
            }
        }

        public EventSink getEventSink()
        {
            return esink;
        }

        public boolean hasEventSink()
        {
            return (esink != null);
        }
    }

    private class ComponentSessionStatsWriter implements Runnable
    {
        private static final String VALUE_SEPARATOR = ";";
        private static final String UNKNOWN_VALUE = "null";

        private final Queue<ComponentSession> entries;
        private final BufferedWriter output;
        private final Duration delay;

        private boolean isOutputClosed;

        public ComponentSessionStatsWriter(Duration delay) throws IOException
        {
            this.entries = new ConcurrentLinkedQueue<ComponentSession>();

            final OpenOption[] options = new OpenOption[] {
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            };

            this.output = Files.newBufferedWriter(sessionStatsPath, StandardCharsets.UTF_8, options);
            this.isOutputClosed = false;
            this.delay = delay;
        }

        public void append(ComponentSession session)
        {
            entries.add(session);
        }

        public void schedule()
        {
            // Since scheduler tasks should complete quickly and this.run() can
            // take longer, submit a new task to an unscheduled executor for it.
            final Runnable trigger = () -> executor.execute(this);
            scheduler.schedule(trigger, delay.toMillis(), TimeUnit.MILLISECONDS);
        }

        public synchronized boolean isClosed()
        {
            return isOutputClosed;
        }

        public synchronized void close() throws IOException
        {
            try {
                this.write();
                this.flush();
            }
            finally {
                isOutputClosed = true;
                output.close();
            }
        }

        @Override
        public synchronized void run()
        {
            if (this.isClosed())
                return;

            try {
                // Write all entries...
                this.write();
                this.flush();
            }
            catch (Exception error) {
                LOG.log(Level.WARNING, "Writing session statistics failed!", error);
            }

            // Re-submit itself
            this.schedule();
        }

        private void write(long value) throws IOException
        {
            output.write(Long.toString(value));
            output.write(VALUE_SEPARATOR);
        }

        private void write(String value) throws IOException
        {
            if (value == null)
                value = UNKNOWN_VALUE;

            output.write(value);
            output.write(VALUE_SEPARATOR);
        }

        private void writeln() throws IOException
        {
            output.newLine();
        }

        private void write() throws IOException
        {
            while (!entries.isEmpty()) {
                final ComponentSession session = entries.remove();
                final ComponentRequest request = session.getRequest();

                this.write(session.getStartTimestamp());
                this.write(Components.timestamp());
                this.write(request.getUserId());

                // TODO: fix this for different session types (having different values)!
                if (request instanceof MachineComponentRequest) {
                    MachineComponentRequest mcr = (MachineComponentRequest) request;
                    this.write(mcr.getEnvironment());
                    this.write(mcr.getObject());
                }
                else if (request instanceof ContainerComponentRequest) {
                    ContainerComponentRequest ccr = (ContainerComponentRequest) request;
                    this.write(ccr.getEnvironment());
                    this.write(UNKNOWN_VALUE);
                }
                else {
                    // Ensure same number of values per entry!
                    for (int i = 0; i < 2; ++i)
                        this.write(UNKNOWN_VALUE);
                }

                this.writeln();
            }
        }

        public void flush() throws IOException
        {
            output.flush();
        }
    }

    private class ComponentSessionCleanupTrigger implements Runnable
    {
        private final ComponentSession session;
        private final long timeout;
        private final long lifetime;
        private long nextNotificationTimeout;

        public ComponentSessionCleanupTrigger(ComponentSession session, Duration timeout)
        {
            this.session = session;
            this.timeout = timeout.toMillis();
            this.lifetime = maxSessionDuration.toMillis();
            this.nextNotificationTimeout = maxSessionDuration.minus(Duration.ofSeconds(90L)).toMillis();
        }

        @Override
        public void run()
        {
            final long curts = Components.timestamp();
            final long prevts = session.getKeepaliveTimestamp();
            final long duration = curts - session.getStartTimestamp();
            final long elapsed = curts - prevts;
            if ((elapsed < timeout) && (duration < lifetime)) {
                // Send client notification if session is about to expire...
                if (session.hasEventSink() && (duration >= nextNotificationTimeout)) {
                    final SessionWillExpireNotification notification = new SessionWillExpireNotification(duration, lifetime);
                    final EventSink esink = session.getEventSink();
                    final OutboundSseEvent event = esink.newEventBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .name(SessionWillExpireNotification.name())
                            .data(notification)
                            .build();

                    esink.send(event);

                    nextNotificationTimeout += Duration.ofSeconds(30L).toMillis();
                }

                // Component should be kept alive! Schedule this task again.
                long delay = (duration < nextNotificationTimeout) ? nextNotificationTimeout - duration : lifetime - duration;
                scheduler.schedule(this, Math.min(delay, timeout - elapsed) + 10L, TimeUnit.MILLISECONDS);
            }
            else {
                // Send client notification...
                if (session.hasEventSink() && (duration >= lifetime)) {
                    final SessionExpiredNotification notification = new SessionExpiredNotification(duration, lifetime);
                    final EventSink esink = session.getEventSink();
                    final OutboundSseEvent event = esink.newEventBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .name(SessionExpiredNotification.name())
                            .data(notification)
                            .build();

                    esink.send(event);

                    LOG.info("Session '" + session.getId() + "' expired after " + notification.getDuration());
                }

                // Since scheduler tasks should complete quickly and session.release()
                // can take longer, submit a new task to an unscheduled executor for it.
                executor.execute(() -> session.release());
            }
        }
    }

    private static class SessionDurationNotification
    {
        private String duration;
        private String maxDuration;

        protected SessionDurationNotification(long duration, long maxDuration)
        {
            this.duration = SessionExpiredNotification.toDurationString(duration);
            this.maxDuration = SessionExpiredNotification.toDurationString(maxDuration);
        }

        @XmlElement(name = "duration")
        public String getDuration()
        {
            return duration;
        }

        @XmlElement(name = "max_duration")
        public String getMaxDuration()
        {
            return maxDuration;
        }

        public static String toDurationString(long duration)
        {
            // Duration is serialized as 'PTnHnMnS', but this method returns 'nhnmns'
            return Duration.ofSeconds(TimeUnit.MILLISECONDS.toSeconds(duration))
                    .toString()
                    .substring(2)
                    .toLowerCase();
        }
    }

    private static class SessionWillExpireNotification extends SessionDurationNotification
    {
        private String message;

        public SessionWillExpireNotification(long duration, long maxDuration)
        {
            super(duration, maxDuration);

            final String timeout = SessionDurationNotification.toDurationString(maxDuration - duration);
            this.message = "Your session will expire in " + timeout + "!";
        }

        @XmlElement(name = "message")
        public String getMessage()
        {
            return message;
        }

        public static String name()
        {
            return "session-will-expire";
        }
    }

    private static class SessionExpiredNotification extends SessionDurationNotification
    {
        private String message;

        public SessionExpiredNotification(long duration, long maxDuration)
        {
            super(duration, maxDuration);
            this.message = "Your session has expired after " + super.getDuration() + "!";
        }

        @XmlElement(name = "message")
        public String getMessage()
        {
            return message;
        }

        public static String name()
        {
            return "session-expired";
        }
    }
}
