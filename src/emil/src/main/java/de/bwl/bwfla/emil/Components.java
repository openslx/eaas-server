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
import java.io.File;
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
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.api.eaas.SessionOptions;
import de.bwl.bwfla.api.emucomp.Container;
import de.bwl.bwfla.api.imagearchive.ImageArchiveMetadata;
import de.bwl.bwfla.api.imagearchive.ImageType;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.configuration.converters.DurationPropertyConverter;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.*;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import de.bwl.bwfla.emil.datatypes.snapshot.*;
import de.bwl.bwfla.emil.utils.EventObserver;
import de.bwl.bwfla.emil.utils.PrintJobObserver;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.emucomp.api.MediumType;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.api.emucomp.Machine;
import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.client.EaasClient;
import de.bwl.bwfla.emil.classification.ArchiveAdapter;
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
    @Config(value = "ws.eaasgw")
    private String eaasGw;
    
    @Inject
    @Config(value = "ws.imagearchive")
    private String imageArchive;

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
    private DatabaseEnvironmentsAdapter envHelper;
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
    private ArchiveAdapter archive;

    ObjectArchiveHelper objectArchiveHelper;

    @Inject
    @AuthenticatedUser
    private UserContext authenticatedUser;

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
        objectArchiveHelper = new ObjectArchiveHelper(objectArchive);
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
     * @HTTP 500 if the backend was not able to instantiate a new component
     * @HTTP 500 if any other internal server error occured
     * 
     * @documentationType de.bwl.bwfla.emil.datatypes.rest.MachineComponentResponse
     */
    @POST
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentResponse createComponent(ComponentRequest request,
                                             @Context final HttpServletResponse response) {
        ComponentResponse result;

        final TaskStack cleanups = new TaskStack();
        final List<EventObserver> observer = new ArrayList<>();

        if (request.getClass().equals(MachineComponentRequest.class)) {
            result = this.createMachineComponent((MachineComponentRequest) request, cleanups, observer);
        } else if (request.getClass().equals(ContainerComponentRequest.class)) {
            result = this.createContainerComponent((ContainerComponentRequest) request, cleanups, observer);
        } else if (request.getClass().equals(SlirpComponentRequest.class)) {
            result = this.createSlirpComponent((SlirpComponentRequest) request, cleanups, observer);
        } else if (request.getClass().equals(SocksComponentRequest.class)) {
            result = this.createSocksComponent((SocksComponentRequest) request, cleanups, observer);
        }  else {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation("Invalid component request"))
                    .build());
        }

        final String cid = result.getId();
        final ComponentSession session = new ComponentSession(cid, request, cleanups, observer);
        cleanups.push(() -> this.unregister(session));

        this.register(session);

        // Submit a trigger for session cleanup
        final Runnable trigger = new ComponentSessionCleanupTrigger(session, sessionExpirationTimeout);
        scheduler.schedule(trigger, sessionExpirationTimeout.toMillis(), TimeUnit.MILLISECONDS);

        response.setStatus(Response.Status.CREATED.getStatusCode());
        response.addHeader("Location", result.getId());
        return result;
    }

    protected ComponentResponse createSlirpComponent(SlirpComponentRequest desc, TaskStack tasks, List<EventObserver> observer) {
        try {
            VdeSlirpConfiguration slirpConfig = new VdeSlirpConfiguration();
            
            if (desc.getHwAddress() != null && !desc.getHwAddress().isEmpty()) {
                slirpConfig.setHwAddress(desc.getHwAddress());
            }
            if (desc.getIp4Address() != null && !desc.getIp4Address().isEmpty()) {
                slirpConfig.setIp4Address(desc.getIp4Address());
            }
            if (desc.getNetmask() != null && desc.getNetmask() != 0) {
                slirpConfig.setNetmask(desc.getNetmask());
            }
            if (desc.getDnsServer() != null && !desc.getDnsServer().isEmpty()) {
                slirpConfig.setDnsServer(desc.getDnsServer());
            }
            slirpConfig.setDhcpEnabled(desc.isDhcp());

            String slirpId = eaasClient.getEaasWSPort(eaasGw).createSession(slirpConfig.value(false));

            return new ComponentResponse(slirpId);
        } catch (Throwable e) {
            throw new InternalServerErrorException(
                    "Server has encountered an internal error: "
                            + e.getMessage(),
                    e);
        }
    }

    protected ComponentResponse createSocksComponent(SocksComponentRequest desc, TaskStack tasks, List<EventObserver> observer) {
        try {
            VdeSocksConfiguration socksConfig = new VdeSocksConfiguration();
            
            if (desc.getHwAddress() != null && !desc.getHwAddress().isEmpty()) {
                socksConfig.setHwAddress(desc.getHwAddress());
            }
            if (desc.getIp4Address() != null && !desc.getIp4Address().isEmpty()) {
                socksConfig.setIp4Address(desc.getIp4Address());
            }
            if (desc.getNetmask() != null && desc.getNetmask() != 0) {
                socksConfig.setNetmask(desc.getNetmask());
            }
            String socksId = eaasClient.getEaasWSPort(eaasGw).createSession(socksConfig.value(false));

            return new ComponentResponse(socksId);
        } catch (Throwable e) {
            throw new InternalServerErrorException(
                    "Server has encountered an internal error: "
                            + e.getMessage(),
                    e);
        }
    }

    String createContainerMetadata(OciContainerConfiguration config) throws BWFLAException {
        ArrayList<String> args = new ArrayList<String>();
        ContainerMetadata metadata = new ContainerMetadata();
        final String inputDir = "container-input";
        final String outputDir = "container-output";

        metadata.setProcess("/bin/sh");
        args.add("-c");
        args.add("/bin/pwd && mkdir " + outputDir + " && emucon-cgen \"$@\"; runc run eaas-job > " + outputDir + "/container-log-" + UUID.randomUUID() + ".log");
        args.add("");

        // cgen args...
        // add more stuff here

        args.add("--output");
        args.add("config.json");

        args.add("--mount");
        args.add(getMountStr(inputDir, config.getInput(), true));
        args.add("--mount");
        args.add(getMountStr(outputDir, config.getOutputPath(), false));

        // Add environment variables
        if(config.getProcess().getEnvironmentVariables() != null) {
            for (String env : config.getProcess().getEnvironmentVariables()) {
                args.add("--env");
                args.add(env);
            }
        }

        // Add emulator's command
        args.add("--");
        for (String arg : config.getProcess().getArguments())
            args.add(arg);

        metadata.setArgs(args);

        return metadata.jsonValueWithoutRoot(true);
    }

    private String getMountStr(String src, String dst, boolean isReadonly) throws BWFLAException {
        if (src != null && dst != null) {
            return src + ":" + dst + ":bind:" + (isReadonly ? "ro" : "rw");
        } else {
            throw new BWFLAException("src or dst is null! src:" + src + " dst:" + dst);
        }
    }

    private BlobHandle prepareMetadata(OciContainerConfiguration config) throws IOException, BWFLAException {
        String metadata = createContainerMetadata(config);
        File tmpfile = File.createTempFile("metadata.json", null, null);
        Files.write( tmpfile.toPath(), metadata.getBytes(), StandardOpenOption.CREATE);

        BlobDescription blobDescription = new BlobDescription();
        blobDescription.setDataFromFile(tmpfile.toPath())
                .setNamespace("random")
                .setDescription("random")
                .setName("metadata")
                .setType(".json");

        return blobstore.put(blobDescription);
    }

    private ImageDescription prepareContainerRuntimeImage(OciContainerConfiguration config, ArrayList<ComponentWithExternalFilesRequest.InputMedium> inputMedia) throws IOException, BWFLAException {
        if (inputMedia.size() != 1)
            throw new BWFLAException("Size of Input drives cannot exceed 1");

        ComponentWithExternalFilesRequest.InputMedium medium = inputMedia.get(0);
        final FileSystemType fileSystemType = FileSystemType.EXT4;
        int sizeInMb = medium.getSizeInMb();
        if (sizeInMb <= 0)
            sizeInMb = 1024;

        final ImageDescription description = new ImageDescription()
                .setMediumType(MediumType.HDD)
                .setPartitionTableType(PartitionTableType.NONE)
                .setPartitionStartBlock(0)
                .setFileSystemType(fileSystemType)
                .setLabel("eaas-job")
                .setSizeInMb(sizeInMb);

        BlobHandle mdBlob = prepareMetadata(config);
        final ImageContentDescription metadataEntry = new ImageContentDescription();
        metadataEntry.setAction(ImageContentDescription.Action.COPY)
                .setDataFromUrl(new URL(mdBlob.toRestUrl(blobStoreRestAddress)))
                .setName("metadata.json");
        description.addContentEntry(metadataEntry);


        for (ComponentWithExternalFilesRequest.FileURL extfile : medium.getExtFiles()) {
            final ImageContentDescription entry = new ImageContentDescription()
                    .setAction(extfile.getAction())
                    .setArchiveFormat(ImageContentDescription.ArchiveFormat.TAR)
                    .setURL(new URL(extfile.getUrl()))
                    .setSubdir("container-input");


            if (extfile.getName() == null || extfile.getName().isEmpty())
                entry.setName(FilenameUtils.getName(entry.getURL().getPath()));
            else
                entry.setName(extfile.getName());

            description.addContentEntry(entry);
        }

        return description;
    }

    protected ComponentResponse createContainerComponent(ContainerComponentRequest desc, TaskStack cleanups, List<EventObserver> observer) {
        if (desc.getEnvironment() == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation("No environment id was given"))
                    .build());
        }

        try {
            final Environment chosenEnv = envHelper.getEnvironmentById(desc.getArchive(), desc.getEnvironment());
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
                    final ImageContentDescription entry = new ImageContentDescription()
                            .setAction(extfile.getAction())
                            .setArchiveFormat(ImageContentDescription.ArchiveFormat.TAR)
                            .setURL(new URL(extfile.getUrl()));


                    if (extfile.getName() == null || extfile.getName().isEmpty())
                        entry.setName(FilenameUtils.getName(entry.getURL().getPath()));
                    else
                        entry.setName(extfile.getName());

                    description.addContentEntry(entry);
                }

                // Build input image
                final BlobHandle blob = ImageBuilderClient.build(imagebuilder, description, imageBuilderTimeout, imageBuilderDelay).getBlobHandle();


                    final Runnable cleanup = () -> {
                        try {
                            blobstore.delete(blob);
                        } catch (Exception error) {
                            LOG.log(Level.WARNING, "Deleting container's input image failed!\n", error);
                        }
                    };

                    cleanups.push(cleanup);

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
            final String sessionId = eaas.createSessionWithOptions(chosenEnv.value(false), options);

            if (sessionId == null) {
                throw new InternalServerErrorException(Response.serverError()
                        .entity(new ErrorInformation("Session initialization has failed, obtained 'null' as session id."))
                        .build());
            }

            Container container = componentClient.getPort(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"), Container.class);
            container.startContainer(sessionId);
            return new ComponentResponse(sessionId);
        }
        catch (BWFLAException | JAXBException | IOException error) {

            TaskStack.run(cleanups);

            throw new InternalServerErrorException(
                    Response.serverError()
                            .entity(new ErrorInformation("Server has encountered an internal error: " + error.getMessage()))
                            .build(), error);
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
                    .setAction(extfile.getAction())
                    .setArchiveFormat(extfile.getCompressionFormat());
//                            .setDataFromUrl(new URL(extfile.getUrl()));

//                    if (new UrlValidator().isValid(extfile.getUrl()) && !extfile.getUrl().contains("file://"))

            try {
                entry.setURL(new URL(extfile.getUrl()));
            } catch (MalformedURLException e) {
                throw new BWFLAException(e);
            }
//                    else
//                        entry.setURL(null);

            if (extfile.getName() == null || extfile.getName().isEmpty())
                entry.setName(FilenameUtils.getName(entry.getURL().getPath()));
            else
                entry.setName(extfile.getName());

            description.addContentEntry(entry);
        }

        // Build input image
        final BlobHandle blob = ImageBuilderClient.build(imagebuilder, description, imageBuilderTimeout, imageBuilderDelay).getBlobHandle();
        // since cdrom is read-only entity, we return user ISO directly
        if (description.getMediumType() != MediumType.CDROM) {
            final Runnable cleanup = () -> {
                try {
                    blobstore.delete(blob);
                } catch (Exception error) {
                    LOG.log(Level.WARNING, "Deleting container's input image failed!\n", error);
                }
            };

            cleanups.push(cleanup);
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

    protected ComponentResponse createMachineComponent(MachineComponentRequest machineDescription, TaskStack cleanups, List<EventObserver> observer) {
        if (machineDescription.getEnvironment() == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation("No environment id was given"))
                    .build());
        }

        try {
            EmilEnvironment emilEnv = this.emilEnvRepo.getEmilEnvironmentById(machineDescription.getEnvironment());
            Environment chosenEnv = envHelper.getEnvironmentById(machineDescription.getArchive(), machineDescription.getEnvironment());

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

            if(machineDescription.getUserContainerEnvironment() != null && !machineDescription.getUserContainerEnvironment().isEmpty())
            {
                OciContainerConfiguration ociConf = (OciContainerConfiguration)envHelper.getEnvironmentById(machineDescription.getUserContainerArchive(),
                        machineDescription.getUserContainerEnvironment());
                LOG.warning(ociConf.jsonValueWithoutRoot(true));

                ImageDescription imageDescription = null;
                try {
                    imageDescription = prepareContainerRuntimeImage(ociConf, machineDescription.getInputMedia());
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
                            .entity(new ErrorInformation("coud not find rootfs "))
                            .build());
                rootfs.setFileSystemType(null);
                this.addBindingToEnvironment(config, rootfs, this.toDriveType(MediumType.HDD));


                this.addBindingToEnvironment(config, binding, this.toDriveType(MediumType.HDD));
                config.setOutputBindingId(binding.getId());
            }
            else {
                // Wrap external input files into images
                for (ComponentWithExternalFilesRequest.InputMedium medium : machineDescription.getInputMedia()) {

                    BlobStoreBinding binding = buildExternalFilesMedia(medium, cleanups, numInputImages++);
                    this.addBindingToEnvironment(config, binding, this.toDriveType(medium.getMediumType()));
                    config.setOutputBindingId(binding.getId());

                    // TODO: Extend MachineConfiguration to support multiple input/output-images!
                    break;
                }
            }

            Integer driveId = null;
            if (machineDescription.getObject() != null) {
                driveId = addObjectToEnvironment(chosenEnv, machineDescription.getObjectArchive(), machineDescription.getObject());
            } else if (machineDescription.getSoftware() != null) {
                String objectId = getObjectIdForSoftware(machineDescription.getSoftware());
                String archiveId = getArchiveIdForSoftware(machineDescription.getSoftware());
                driveId = addObjectToEnvironment(chosenEnv, archiveId, objectId);
            }

            final List<String> selectors = resourceProviderSelection.getSelectors(chosenEnv.getId());
            SessionOptions options = new SessionOptions();
            if(selectors != null && !selectors.isEmpty())
                options.getSelectors().addAll(selectors);

            if(machineDescription.isLockEnvironment()) {
                options.setLockEnvironment(true);
            }

            final String sessionId = eaas.createSessionWithOptions(chosenEnv.value(false), options);

            if (sessionId == null) {
                throw new InternalServerErrorException(Response.serverError()
                        .entity(new ErrorInformation("Session initialization has failed, obtained 'null' as session id."))
                        .build());
            }

            Machine machine = componentClient.getPort(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"), Machine.class);
            machine.start(sessionId);

            if(emilEnv != null && emilEnv.isEnablePrinting())
                observer.add(new PrintJobObserver(componentClient.getMachinePort(eaasGw), sessionId));

            return new MachineComponentResponse(sessionId, driveId);
        }
        catch (BWFLAException | JAXBException | MalformedURLException e) {

            TaskStack.run(cleanups);
            e.printStackTrace();
            throw new InternalServerErrorException(
            		Response.serverError()
                    .entity(new ErrorInformation("Server has encountered an internal error: " + e.getMessage()))
                    .build(),e);
        }
    }

    protected String getObjectIdForSoftware(String softwareId)
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
        String objectId = software.getObjectId();
        return objectId;
    }
    
    protected String getArchiveIdForSoftware(String softwareId)
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
        String archiveId = software.getArchive();
        return archiveId;
    }

    protected int addObjectToEnvironment(Environment chosenEnv, String archiveId, String objectId)
            throws BWFLAException, JAXBException {

        LOG.info("adding object id: " + objectId);
        if(EmulationEnvironmentHelper.hasObjectBinding((MachineConfiguration)chosenEnv, objectId))
        {
            return EmulationEnvironmentHelper.getDriveId((MachineConfiguration)chosenEnv, objectId);
        }

        if(archiveId == null || archiveId.equals("default"))
        {
            if(authenticatedUser == null || authenticatedUser.getUsername() == null)
                archiveId = "default";
            else
                archiveId = authenticatedUser.getUsername();
        }

        String chosenObjRef = archive.getFileCollectionForObject(archiveId, objectId);
        if (chosenObjRef == null) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation(
                            "Could not find object with ID: " + objectId))
                    .build());
        }

        ObjectArchiveBinding binding = new ObjectArchiveBinding(envHelper.toString(), archiveId, objectId);
        FileCollection fc = FileCollection.fromValue(chosenObjRef);

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
    @Secured
    @Path("/{componentId}")
    public ComponentResponse getComponent(
            @PathParam("componentId") String componentId) {
        try {
            if (!this.componentClient.getPort(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"), Component.class).getComponentType(componentId)
                    .equals("machine")) {
                return new ComponentResponse(componentId);
            }

            // TODO: find a way to get the correct driveId here
            return new MachineComponentResponse(componentId, null);
        } catch (BWFLAException | MalformedURLException e) {
            throw new InternalServerErrorException(
                    "Server has encountered an internal error: "
                            + e.getMessage(),
                    e);
        }
    }

    public boolean hasComponentSession(String componentId)
    {
        ComponentSession session = sessions.get(componentId);
        if(session == null)
            return false;

        return true;
    }

    public boolean keepalive(String componentId, boolean ignoreMissing) throws BWFLAException {
        ComponentSession session = sessions.get(componentId);
        if(session == null) {
            if(!ignoreMissing)
                LOG.info("Component Session null! Should throw instead. " + componentId);
            return false;
        }

        session.keepalive();
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
    @Secured
    @Path("/{componentId}/keepalive")
    public void keepalive(@PathParam("componentId") String componentId) {

        try {
            keepalive(componentId, false);
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
    @Secured
    @Path("/{componentId}/state")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentResponse getState(@PathParam("componentId") String componentId) {
        try {
            String state = this.componentClient.getPort(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"), Component.class).getState(componentId);
            if (state.equals(ComponentState.OK.toString()) || state.equals(ComponentState.INACTIVE.toString())) {
                return new ComponentStateResponse(componentId, state);
            } else if (state.equals(ComponentState.STOPPED.toString()) || state.equals(ComponentState.FAILED.toString())) {
                LOG.fine("emulator is " + state + "!");
                return new ComponentStateResponse(componentId, state);
            } else {
                throw new InternalServerErrorException(Response.serverError()
                        .entity("The component associated with your session has failed.")
                        .build());
            }
        } catch (BWFLAException | MalformedURLException e) {
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
    @Secured
    @Path("/{componentId}/controlurls")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, URI> getControlUrls(@PathParam("componentId") String componentId) {
        try {
            Component component = this.componentClient.getPort(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"), Component.class);
            return component.getControlUrls(componentId).getEntry().stream().collect(Collectors.toMap(e -> e.getKey(), e -> URI.create(e.getValue())));
        } catch (BWFLAException | MalformedURLException e) {
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
    @Secured
    @Path("/{componentId}/result")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResult(@PathParam("componentId") String componentId)
    {
        try {
            final BlobHandle handle = component.getResult(componentId);
            if (handle == null)
                throw new NotFoundException();

            // Actual result's location
            final String location = handle.toRestUrl(blobStoreRestAddress);

            // Set response headers...
            final Response response = Response.status(Response.Status.TEMPORARY_REDIRECT)
                    .header("Access-Control-Allow-Origin", "*")
                    .location(new URI(location))
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
    @Secured
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
    @Secured
    @Path("/{componentId}/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stop(@PathParam("componentId") String componentId,
                             @Context HttpServletResponse servletResponse) {
        try {
            final ComponentSession session = sessions.get(componentId);
            final ComponentRequest request = session.getRequest();
            if (request instanceof MachineComponentRequest) {
                final Machine machine = componentClient.getMachinePort(eaasGw);
                machine.stop(componentId);
            }
            else if (request instanceof ContainerComponentRequest) {
                final Container container = componentClient.getContainerPort(eaasGw);
                container.stopContainer(componentId);
            }
        } catch (BWFLAException e) {
            return Emil.errorMessageResponse("stop failed: " + e.getMessage());
        }
        return Emil.successMessageResponse("stopped component " + componentId);
    }

    @GET
    @Secured
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
    @Secured
    @Path("/{componentId}/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void events(@PathParam("componentId") String componentId, @Context SseEventSink sink, @Context Sse sse)
    {

        LOG.warning("registering events");
        final ComponentSession session = sessions.get(componentId);
        if(session == null)
            return;

        session.initializeEventLoop(sink, sse);
    }

    @GET
    @Secured
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

    /**
     * Save environment
     * Saves the current disk state in a given image archive
     * @param componentId The component's ID to snapshot.
     * @param request {@link SnapshotRequest}
     * @return A JSON response containing the result message.
     */
    @POST
    @Secured
    @Path("/{componentId}/snapshot")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SnapshotResponse snapshot(@PathParam("componentId") String componentId, SnapshotRequest request)
    {
        return this.handleSnapshotRequest(componentId, request, false);
    }

    /**
     * Creates a checkpoint of a running emulation session.
     * @param componentId The component's ID to checkpoint.
     * @param request {@link SnapshotRequest}
     * @return A JSON response containing the result message.
     */
    @POST
    @Secured
    @Path("/{componentId}/checkpoint")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SnapshotResponse checkpoint(@PathParam("componentId") String componentId, SnapshotRequest request)
    {
        return this.handleSnapshotRequest(componentId, request, true);
    }


    /**
     * Changes digital object's medium in an emulation session. Since a digital object
     * can be composed of multiple media, a medium's name must also be specified.
     * @param changeRequest {@link MediaChangeRequest}
     * @return A JSON response containing the result message.
     */
    @POST
    @Secured
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
            chosenObjRef = archive.getFileCollectionForObject(changeRequest.getArchiveId(),
                    changeRequest.getObjectId());
            if(chosenObjRef == null)
                return Emil.errorMessageResponse("no file collection found for object " + changeRequest.getObjectId());

            FileCollection fc = FileCollection.fromValue(chosenObjRef);
            for(FileCollectionEntry fce : fc.files)
                if(fce.getId().equals(changeRequest.getLabel()))
                {
                    objurl = fce.getId();
                    break;
                }
        } catch (NoSuchElementException | BWFLAException | JAXBException e1) {
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
            return Emil.internalErrorResponse("could not initialize eaas gateway: " + e.getMessage());
        }

        return Emil.successMessageResponse("");
    }

    /**
     * Release a stopped component session.
     * @param componentId The component's ID to release.
     */
    @DELETE
    @Secured
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


    /* ==================== Internal Helpers ==================== */

    private Snapshot createSnapshot(String componentId, boolean checkpoint)
            throws BWFLAException, InterruptedException, JAXBException
    {
        final Machine machine = componentClient.getMachinePort(eaasGw);
        final MachineConfiguration config = MachineConfiguration.fromValue(machine.getRuntimeConfiguration(componentId));
        String state = machine.getEmulatorState(componentId);
        if (checkpoint) {
            // Make a checkpoint + snapshot
            LOG.info("Preparing session " + componentId + " for checkpointing...");
            if (!state.equalsIgnoreCase(EaasState.SESSION_RUNNING.value())) {
                LOG.warning("Preparing session " + componentId + " for checkpointing failed! Invalid state: " + state);
                return null;
            }

            // Import checkpoint data into archive
            final DataHandler checkpointData = machine.checkpoint(componentId);
            final ImageArchiveMetadata metadata = new ImageArchiveMetadata();
            metadata.setImageId(UUID.randomUUID().toString());
            metadata.setType(ImageType.CHECKPOINTS);

            LOG.info("Saving checkpointed environment in image-archive...");

            final ImageArchiveBinding binding = envHelper.importImage("default", checkpointData, metadata).getBinding(15);
            binding.setId("checkpoint");
            binding.setLocalAlias("checkpoint.tar.gz");
            binding.setAccess(Binding.AccessType.COPY);

            // Update machine's configuration
            config.setCheckpointBindingId("binding://" + binding.getId());
            config.getAbstractDataResource().add(binding);
        }
        else {
            // Make a snapshot only!
            if (state.equalsIgnoreCase(EaasState.SESSION_RUNNING.value())) {
                LOG.info("Preparing session " + componentId + " for snapshotting...");
                machine.stop(componentId);

                final String expState = EaasState.SESSION_STOPPED.value();
                for (int i = 0; i < 30; ++i) {
                    state = machine.getEmulatorState(componentId);
                    if (state.equalsIgnoreCase(expState))
                        break;

                    Thread.sleep(500);
                }
            }

            state = machine.getEmulatorState(componentId);
            if (!state.equalsIgnoreCase(EaasState.SESSION_STOPPED.value())) {
                LOG.warning("Preparing session " + componentId + " for snapshotting failed!");
                return null;
            }
        }

        try {
            return new Snapshot(config, machine.snapshot(componentId));
        }
        catch (BWFLAException e)
        {
            LOG.warning("failed to retrieve snapshot. assuming floppy env...");
            return new Snapshot(config);
        }
    }

    private SnapshotResponse handleSnapshotRequest(String componentId, SnapshotRequest request, boolean checkpoint)
    {
        try {
            final Snapshot snapshot = this.createSnapshot(componentId, checkpoint);
            if (snapshot == null) {
                final String message = "Creating " + ((checkpoint) ? "checkpoint" : "snapshot") + " failed!";
                return new SnapshotResponse(new BWFLAException(message));
            }

            if (request instanceof SaveObjectEnvironmentRequest) {
                return new SnapshotResponse(emilEnvRepo.saveAsObjectEnvironment(snapshot, (SaveObjectEnvironmentRequest) request));
            }
            else if (request instanceof SaveImportRequest) {
                return new SnapshotResponse(emilEnvRepo.saveImport(snapshot, (SaveImportRequest) request));
            }
            else if (request instanceof SaveDerivateRequest) { // implies SaveCreatedEnvironmentRequest && newEnvironmentRequest
                return new SnapshotResponse(emilEnvRepo.saveAsRevision(snapshot, (SaveDerivateRequest) request, checkpoint));
            }
            else if (request instanceof SaveUserSessionRequest) {
                return new SnapshotResponse(emilEnvRepo.saveAsUserSession(snapshot, (SaveUserSessionRequest) request));
            }
            else {
                return new SnapshotResponse(new BWFLAException("Unknown request type!"));
            }
        }
        catch (Exception exception) {
            final BWFLAException error = (exception instanceof BWFLAException) ?
                    (BWFLAException) exception : new BWFLAException(exception);

            final String message = "Handling " + ((checkpoint) ? "checkpoint" : "snapshot") + " request failed!";
            LOG.log(Level.WARNING, message, error);
            return new SnapshotResponse(error);
        }
    }

    private static long timestamp()
    {
        return System.currentTimeMillis();
    }

    public static class TaskStack
    {
        private final Deque<Runnable> tasks = new ArrayDeque<Runnable>();

        public void push(Runnable task)
        {
            tasks.push(task);
        }

        public Runnable pop()
        {
            return tasks.pop();
        }

        public boolean isEmpty()
        {
            return tasks.isEmpty();
        }

        public static void run(TaskStack tasks)
        {
            while (!tasks.isEmpty()) {
                try {
                    tasks.pop().run();
                }
                catch (Exception error) {
                    LOG.log(Level.WARNING, "Running task failed!\n", error);
                }
            }
        }
    }

    private class ComponentSession
    {
        private final AtomicLong keepaliveTimestamp;
        private final long startTimestamp;
        private final AtomicBoolean released;

        private final String id;
        private final ComponentRequest request;
        private final TaskStack tasks;
        private final List<EventObserver> observerList;
        private ScheduledFuture<?> eventTask;
        private boolean finished = false;

        public ComponentSession(String id, ComponentRequest request, TaskStack tasks, List<EventObserver> observer)
        {
            this.keepaliveTimestamp = new AtomicLong(0);
            this.startTimestamp = Components.timestamp();
            this.released = new AtomicBoolean(false);

            this.id = id;
            this.request = request;
            this.tasks = tasks;
            this.observerList = observer;

            LOG.info("Session for component ID '" + id + "' created");
        }


        public void keepalive() throws BWFLAException
        {
            keepaliveTimestamp.set(Components.timestamp());
            component.keepalive(id);
        }

        public void release()
        {
            finished = true;

            if(eventTask != null)
                eventTask.cancel(true);

            if (released.getAndSet(true))
                return;  // Release was already called!

            LOG.info("Releasing session '" + id + "'...");
            try {
                eaas.releaseSession(id);
            }
            catch (Exception error) {
                LOG.log(Level.WARNING, "Releasing session '" + id + "' failed!\n", error);
            }

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
            TaskStack.run(tasks);

            LOG.info("Session '" + id + "' released");
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

        public void initializeEventLoop(SseEventSink sink, Sse sse)
        {
            if(finished || eventTask != null)
                return;

            for(EventObserver eo : observerList) {
                eventTask = scheduler.scheduleAtFixedRate(
                () -> {
                    System.out.println(eo.getName());
                    for(String m : eo.messages()) {
                        sink.send(sse.newEventBuilder().name(eo.getName())
                                .data(String.class, m).build());
                    }
                }, 0, 5, TimeUnit.SECONDS);
            }
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

        public ComponentSessionCleanupTrigger(ComponentSession session, Duration timeout)
        {
            this.session = session;
            this.timeout = timeout.toMillis();
        }

        @Override
        public void run()
        {
            final long curts = Components.timestamp();
            final long prevts = session.getKeepaliveTimestamp();
            final long elapsed = curts - prevts;
            if (elapsed < timeout) {
                // Component should be kept alive! Schedule this task again.
                final long delay = timeout - elapsed + 10L;
                scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
            }
            else {
                // Timeout expired!

                // Since scheduler tasks should complete quickly and session.release()
                // can take longer, submit a new task to an unscheduled executor for it.
                executor.execute(() -> session.release());
            }
        }
    }
}
