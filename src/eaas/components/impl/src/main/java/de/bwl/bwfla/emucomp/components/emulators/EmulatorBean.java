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

package de.bwl.bwfla.emucomp.components.emulators;

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.client.endpoint.v2.util.EmulatorMetaHelperV2;
import com.openslx.eaas.imagearchive.databind.EmulatorMetaData;
import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.IllegalEmulatorStateException;
import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.capture.ScreenShooter;
import de.bwl.bwfla.common.services.guacplay.net.GuacInterceptorChain;
import de.bwl.bwfla.common.services.guacplay.net.GuacTunnel;
import de.bwl.bwfla.common.services.guacplay.net.TunnelConfig;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionBuilder;
import de.bwl.bwfla.common.services.guacplay.record.SessionRecorder;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.DiskDescription;
import de.bwl.bwfla.common.utils.ImageInformation;
import de.bwl.bwfla.common.utils.ProcessMonitor;
import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;
import de.bwl.bwfla.emucomp.api.EmulatorUtils.XmountOutputFormat;
import de.bwl.bwfla.emucomp.components.BindingsManager;
import de.bwl.bwfla.emucomp.components.EaasComponentBean;
import de.bwl.bwfla.emucomp.components.Tail;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.EventID;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.MessageType;
import de.bwl.bwfla.emucomp.control.connectors.*;
import de.bwl.bwfla.emucomp.xpra.IAudioStreamer;
import de.bwl.bwfla.emucomp.xpra.PulseAudioStreamer;
import org.apache.commons.io.FileUtils;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


/**
 * @author iv1004
 */
public abstract class EmulatorBean extends EaasComponentBean implements EmulatorComponent
{
    private EmulatorBeanMode emuBeanMode;

    @Inject
    @Config("emucomp.inactivitytimeout")
    public int inactivityTimeout;

    @Inject
    @Config("emucomp.alsa_card")
    public String alsa_card;

	@Inject
	@Config("emucomp.libfaketime")
	public String libfaketime;

	// this configurable should be removed.
	// currently it is only used to enable the fake clock
	@Inject
	@Config("components.emulator_containers.snapshot")
	public boolean isSnapshotEnabled = false;

	// allow beans to disable the fake clock preload
	protected boolean disableFakeClock = false;

	private boolean isPulseAudioEnabled = false;

	@Inject
	@Config("emucomp.blobstore_soap")
	private String blobStoreAddressSoap = null;

    @Resource(lookup = "java:jboss/ee/concurrency/executor/io")
    protected ExecutorService ioTaskExecutor;

	@Resource(lookup = "java:jboss/ee/concurrency/factory/default")
	protected ManagedThreadFactory workerThreadFactory;

	@Inject
	@Config(value = "rest.blobstore")
	private String blobStoreRestAddress;

	private final String containerOutput = "container-output";

	private static final String emulatorDataBase = "/home/bwfla/server-data/emulator-data/";
    
	protected final TunnelConfig tunnelConfig = new TunnelConfig();

	protected final EmulatorBeanState emuBeanState = new EmulatorBeanState(EmuCompState.EMULATOR_UNDEFINED, LOG);

	protected MachineConfiguration emuEnvironment;
	private String emuNativeConfig;
	protected final Map<Integer, File> containers = Collections.synchronizedMap(new HashMap<Integer, File>());

	protected final DeprecatedProcessRunner emuRunner = new DeprecatedProcessRunner();
	protected final ArrayList<DeprecatedProcessRunner> vdeProcesses = new ArrayList<DeprecatedProcessRunner>();

	protected final BindingsManager bindings = new BindingsManager(LOG);

	protected String protocol;

	/** Emulator's configuration settings */
	protected final EmulatorConfig emuConfig = new EmulatorConfig();

	/* IPC for control messages */
	private IpcSocket ctlSocket = null;
	protected IpcMessageWriter ctlMsgWriter = null;
	protected IpcMessageReader ctlMsgReader = null;
	private IpcMessageQueue ctlMsgQueue = new IpcMessageQueue();
	private IpcEventSet ctlEvents = new IpcEventSet();
	protected String emuCtlSocketName = null;

	/** Is a client attached to the emulator? */
	private final AtomicBoolean isClientAttachedFlag = new AtomicBoolean(false);

	/* Session recording + replay members */
	private SessionRecorder recorder = null;
	private SessionPlayerWrapper player = null;

	/** Tool for capturing of screenshots. */
	private ScreenShooter scrshooter = null;
	
	final boolean isScreenshotEnabled = ConfigurationProvider.getConfiguration().get("emucomp.enable_screenshooter", Boolean.class);

	protected PostScriptPrinter printer = null;

	/** Internal chain of IGuacInterceptors. */
	private final GuacInterceptorChain interceptors = new GuacInterceptorChain(2);

	/** Number of unprocessed messages, before message-processors start to block. */
	private static final int MESSAGE_BUFFER_CAPACITY = 4096;

	/** Filename for temporary trace-files. */
	private static final String TRACE_FILE = "session" + GuacDefs.TRACE_FILE_EXT;

	/* Supported protocol names */
	private static final String PROTOCOL_SDLONP  = "sdlonp";
	private static final String PROTOCOL_Y11     = "y11";

	/* Supported audio driver names */
	private static final String AUDIODRIVER_PULSE  = "pulse";

	/** Data directory inside of the emulator-containers */
	private static final String EMUCON_DATA_DIR = "/emucon/data";

	/** Binding ID for container's root filesystem */
	private static final String EMUCON_ROOTFS_BINDING_ID = "emucon-rootfs";

	private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

	@Inject
	@Config("components.emulator_containers.enabled")
	private boolean emuContainerModeEnabled = false;

	@Inject
	@Config("components.emulator_containers.uid")
	private String emuContainerUserId = null;

	@Inject
	@Config("components.emulator_containers.gid")
	private String emuContainerGroupId = null;

	/** Files to include into a container-checkpoint */
	protected List<String> emuContainerFilesToCheckpoint = new ArrayList<>();

	/** File extension for checkpoints */
	private static final String CHECKPOINT_FILE_EXTENSION = ".tar.gz";

	protected boolean isKvmDeviceEnabled = false;


	public static EmulatorBean createEmulatorBean(MachineConfiguration env) throws ClassNotFoundException
	{
		String targetBean = env.getEmulator().getBean() + "Bean";
		Class<?> beanClass = Class.forName(EmulatorBean.class.getPackage().getName() + "." + targetBean);
		return (EmulatorBean)CDI.current().select(beanClass).get();
	}

	public boolean isClientAttached()
	{
		return isClientAttachedFlag.get();
	}

	public boolean isSdlBackendEnabled()
	{
		return (emuBeanMode == EmulatorBeanMode.SDLONP);
	}

	public boolean isXpraBackendEnabled()
	{
		return (emuBeanMode == EmulatorBeanMode.XPRA);
	}

	public boolean isPulseAudioEnabled()
	{
		return isPulseAudioEnabled;
	}

	public boolean isLocalModeEnabled()
	{
		return (emuBeanMode == EmulatorBeanMode.Y11);
	}

	public boolean isHeadlessModeEnabled() { return (emuBeanMode == EmulatorBeanMode.HEADLESS); }

	public boolean isContainerModeEnabled()
	{
		return emuContainerModeEnabled;
	}

	public EmulatorBeanMode getEmuBeanMode()
	{
		return emuBeanMode;
	}

	public int getInactivityTimeout()
	{
		return inactivityTimeout;
	}

	@Override
	public String getComponentType()
	{
		return "machine";
	}

	boolean isHeadlessSupported() { return false; }

	boolean isBeanReady() {
		return true; // this is the default, if the bean has no internal state
	}

	@Override
	public ComponentState getState() throws BWFLAException
	{
		switch (this.getEmulatorState()) {
			case EMULATOR_RUNNING:
				return (this.isBeanReady()) ? ComponentState.RUNNING : ComponentState.INITIALIZING;
			case EMULATOR_INACTIVE:
				return ComponentState.INACTIVE;
			case EMULATOR_STOPPED:
				return ComponentState.STOPPED;
			case EMULATOR_FAILED:
				return ComponentState.FAILED;
			default:
				return ComponentState.INITIALIZING;
		}
	}

	public EmuCompState getEmulatorState()
	{
		final boolean isEmulatorInactive = ctlEvents.poll(EventID.CLIENT_INACTIVE);
		synchronized (emuBeanState) {
			if (isEmulatorInactive)
				emuBeanState.set(EmuCompState.EMULATOR_INACTIVE);
			return emuBeanState.get();
		}
	}

	public String getContainerId()
	{
		final String compid = this.getComponentId();
		return compid.substring(1 + compid.lastIndexOf("+"));
	}

	public String getContainerUserId()
	{
		return emuContainerUserId;
	}

	public String getContainerGroupId()
	{
		return emuContainerGroupId;
	}


	public Function<String, String> getContainerHostPathReplacer()
	{
		final String hostDataDir = this.getDataDir().toString();
		final String hostBindingsDir = this.getBindingsDir().toString();
		return (cmdarg) -> {
			return cmdarg.replaceAll(hostBindingsDir, EMUCON_DATA_DIR + "/bindings")
					.replaceAll(hostDataDir, EMUCON_DATA_DIR);
		};
	}

	public Path getDataDir()
	{
		final Path workdir = this.getWorkingDir();
		if (this.isContainerModeEnabled())
			return workdir.resolve("data");

		return workdir;
	}

	public Path getNetworksDir()
	{
		return this.getDataDir().resolve("networks");
	}

	public Path getSocketsDir()
	{
		return this.getDataDir().resolve("sockets");
	}

	public Path getUploadsDir()
	{
		return this.getDataDir().resolve("uploads");
	}

	public Path getStateDir()
	{
		return this.getWorkingDir().resolve("state");
	}

	public Path getPrinterDir()
	{
		return this.getDataDir().resolve("printer");
	}

	private void createWorkingSubDirs() throws IOException
	{
		// Currently, working directory in container-mode is structured as follows:
		//
		// <workdir>/
		//     state/          -> Container's memory dump
		//     data/           -> Session/emulator specific data
		//         networks/   -> Networking files
		//         sockets/    -> IO + CTRL sockets
		//         uploads/    -> Uploaded files

		// If container-mode is disabled:  <workdir>/ == data/

		Files.createDirectories(this.getDataDir());
		Files.createDirectories(this.getNetworksDir());
		Files.createDirectories(this.getSocketsDir());
		Files.createDirectories(this.getUploadsDir());
		Files.createDirectories(this.getPrinterDir());
	}

	private Path getXpraSocketPath()
	{
		return this.getSocketsDir().resolve("xpra-iosocket");
	}

	private Path getPulseAudioSocketPath()
	{
		return this.getSocketsDir().resolve("pulse-iosocket");
	}

	/** Returns emulator's runtime layer name. */
	protected String getEmuContainerName(MachineConfiguration machineConfiguration)
	{
		final String message = this.getClass().getSimpleName()
				+ " does not support container-mode!";

		throw new UnsupportedOperationException(message);
	}

	private String getEmulatorArchive() {
		String archive = ConfigurationProvider.getConfiguration().get("emucomp.emulator_archive");
		if(archive == null || archive.isEmpty())
			return EMULATOR_DEFAULT_ARCHIVE;
		return archive;
	}

	public void initialize(ComponentConfiguration compConfig) throws BWFLAException
	{
		synchronized (emuBeanState) {
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_UNDEFINED) {
				String message = "Cannot initialize EmulatorBean!";
				throw new IllegalEmulatorStateException(message, curstate)
						.setId(this.getComponentId());
			}

			emuBeanState.set(EmuCompState.EMULATOR_BUSY);
		}

		final MachineConfiguration env = (MachineConfiguration) compConfig;
		emuBeanMode = getEmuBeanMode(env);
		emuRunner.setLogger(LOG);

		try {
			this.createWorkingSubDirs();
		}
		catch (IOException error) {
			throw this.newInitFailureException("Creating working subdirs failed!", error);
		}

		if (this.isSdlBackendEnabled()) {
			// Create control sockets
			try {
				ctlSocket = IpcSocket.create(this.newCtlSocketName("srv"), IpcSocket.Type.DGRAM, true);
				ctlMsgWriter = new IpcMessageWriter(ctlSocket);
				ctlMsgReader = new IpcMessageReader(ctlSocket);
				emuCtlSocketName = this.newCtlSocketName("emu");
			}
			catch (Throwable exception) {
				throw this.newInitFailureException("Constructing control sockets failed!", exception);
			}

			// Prepare configuration for tunnels
			tunnelConfig.setGuacdHostname("127.0.0.1");
			tunnelConfig.setGuacdPort(TunnelConfig.GUACD_PORT);
			tunnelConfig.setInterceptor(interceptors);
		}

		emuConfig.setHardTermination(false);

		try
		{
			if (this.isContainerModeEnabled()) {
				// Check, that rootfs-image is specified!
				final boolean isRootFsFound = env.getAbstractDataResource().stream()
						.anyMatch((resource) -> resource.getId().contentEquals(EMUCON_ROOTFS_BINDING_ID));

				if (!isRootFsFound) {
					// Not found, try to get latest image ID as configured by the archive
					final var image = this.findEmulatorImage(env);
					env.getAbstractDataResource().add(image);
				}
			}

			this.setRuntimeConfiguration(env);
		}
		catch (Throwable error) {
			throw this.newInitFailureException("Initializing runtime configuration failed!", error);
		}

		LOG.info("Emulation session initialized in " + emuBeanMode.name() + " mode.");
		LOG.info("Working directory created at: " + this.getWorkingDir());
		emuBeanState.update(EmuCompState.EMULATOR_READY);
	}

	private void unmountBindings()
	{
		bindings.cleanup();
	}

	synchronized public void destroy()
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate == EmuCompState.EMULATOR_UNDEFINED)
				return;

			if (curstate == EmuCompState.EMULATOR_BUSY) {
				LOG.severe("Destroying EmulatorBean while other operation is in-flight!");
				return;
			}

			emuBeanState.set(EmuCompState.EMULATOR_UNDEFINED);
		}
		this.stopInternal();

		// free container IDs and remove corresp. files
		for(File container: containers.values())
			container.delete();

		containers.clear();

		// kill vde networking threads
		for(DeprecatedProcessRunner subprocess : this.vdeProcesses)
		{
			if(subprocess.isProcessRunning())
				subprocess.stop();

			subprocess.cleanup();
		}

		this.unmountBindings();

		// Stop screenshot-tool
		if (scrshooter != null)
			scrshooter.finish();

		// Stop and finalize session-recording
		if (recorder != null && !recorder.isFinished())
		{
			try {
				recorder.finish();
			}
			catch (IOException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		// Cleanup the control sockets
		try {
			if (ctlSocket != null)
				ctlSocket.close();
		}
		catch (IOException exception) {
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
		}

		// Cleanup emulator's runner here
		if (emuRunner.isProcessValid()) {
			emuRunner.printStdOut();
			emuRunner.printStdErr();
		}

		emuRunner.cleanup();

		LOG.info("EmulatorBean destroyed.");

		// Destroy base class!
		super.destroy();

		// Collect garbage
		System.gc();
	}


	@Override
	public void start() throws BWFLAException
	{
		synchronized (emuBeanState) {
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_READY && curstate != EmuCompState.EMULATOR_STOPPED) {
				throw new BWFLAException("Cannot start emulator! Wrong state detected: " + curstate.value())
						.setId(this.getComponentId());

			}
			emuBeanState.set(EmuCompState.EMULATOR_BUSY);
		}

		try {
			if (this.isSdlBackendEnabled() || this.isXpraBackendEnabled() || this.isHeadlessModeEnabled())
				this.startBackend();
			else {
				throw new BWFLAException("Trying to start emulator using unimplemented mode: " + this.getEmuBeanMode())
						.setId(this.getComponentId());
			}
		}
		catch(Throwable error) {
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			String message = "Starting emulator failed!";
			LOG.log(Level.SEVERE, message, error);
			if (error.getMessage() != null)
				message += " " + error.getMessage();

			throw new BWFLAException(message, error)
					.setId(this.getComponentId());
		}
	}

	private void startBackend() throws BWFLAException, IOException
	{
		if (this.isLocalModeEnabled())
			LOG.info("Local-mode enabled. Emulator will be started locally!");

		if(isSnapshotEnabled && !disableFakeClock) {
			LOG.info("initializing fake clock");
			emuRunner.addEnvVariable("LD_PRELOAD", "/usr/local/lib/LD_PRELOAD_clock_gettime.so");
		}
		if (this.isXpraBackendEnabled()) {
			// TODO: implement this, if needed!
			if (!this.isContainerModeEnabled()) {
				throw new BWFLAException("Non-containerized XPRA sessions are not supported!")
						.setId(this.getComponentId());
			}

			final boolean isGpuEnabled = ConfigurationProvider.getConfiguration()
					.get("components.xpra.enable_gpu", Boolean.class);

			if (isGpuEnabled) {
				emuRunner.getCommand()
						.add(0, "vglrun");
			}
		}

		if (this.isContainerModeEnabled()) {
			LOG.info("Container-mode enabled. Emulator will be started inside of a container!");

			final String cid = this.getContainerId();
			final String workdir = this.getWorkingDir().toString();
			final String rootfsdir = this.lookupResource(EMUCON_ROOTFS_BINDING_ID);

			// Generate container's config
			{
				final String conConfigPath = Paths.get(workdir, "config.json").toString();

				final DeprecatedProcessRunner cgen = new DeprecatedProcessRunner();
				cgen.setCommand("emucon-cgen");
				cgen.addArguments("--output", conConfigPath);
				cgen.addArguments("--user-id", emuContainerUserId);
				cgen.addArguments("--group-id", emuContainerGroupId);
				cgen.addArguments("--rootfs", rootfsdir);

				if (getEmulatorWorkdir() != null) {
					cgen.addArguments("--workdir", getEmulatorWorkdir());
				}

				final String hostDataDir = this.getDataDir().toString();

				final Function<String, String> hostPathReplacer = this.getContainerHostPathReplacer();

				// Mount emulator's data dir entries
				try (Stream<Path> entries = Files.list(this.getDataDir())) {
					entries.forEach((entry) -> {
								final String path = entry.toString();
								cgen.addArgument("--mount");
								cgen.addArgument(path, ":", hostPathReplacer.apply(path), ":bind:rw");

								if(path.contains("printer")) // compatibility for old snapshots
								{
									String target = hostPathReplacer.apply(path).replace("printer", "print");
									cgen.addArgument("--mount");
									cgen.addArgument(path, ":", target, ":bind:rw");
								}
							});
				}
				catch (Exception error) {
					LOG.log(Level.WARNING, "Listing '" + hostDataDir + "' failed!\n", error);
					emuBeanState.update(EmuCompState.EMULATOR_FAILED);
					return;
				}

				final List<String> bindingIdsToSkip = new ArrayList<>();
				bindingIdsToSkip.add(EMUCON_ROOTFS_BINDING_ID);
				if (emuEnvironment.hasCheckpointBindingId())
					bindingIdsToSkip.add(emuEnvironment.getCheckpointBindingId());

				// Mount fuse-mounted bindings separately
				bindings.paths().forEach((entry) -> {
					final String curid = entry.getKey();
					for (String idToSkip : bindingIdsToSkip) {
						if (curid.contentEquals(idToSkip))
							return;  // Skip it!
					}

					final String path = entry.getValue();
					cgen.addArgument("--mount");
					cgen.addArgument(path, ":", hostPathReplacer.apply(path), ":bind:rw");
				});

				// Add emulator's env-vars with replaced host data directory
				emuRunner.getEnvVariables()
						.forEach((name, value) -> {
							cgen.addArgument("--env");
							cgen.addArgument(name);
							if (value != null && !value.isEmpty())
								cgen.addArgValues("=", hostPathReplacer.apply(value));
						});

				// Enable KVM device (if needed)
				if (isKvmDeviceEnabled)
					cgen.addArgument("--enable-kvm");

				final String conNetDir = hostPathReplacer.apply(this.getNetworksDir().toString());

				// Add emulator's command with replaced host data directory
				cgen.addArguments("--", "/usr/bin/emucon-init", "--networks-dir", conNetDir);
				if (this.isXpraBackendEnabled()) {
					final String xprasock = this.getXpraSocketPath().toString();
					cgen.addArguments("--xpra-socket", hostPathReplacer.apply(xprasock));
				}

				if (this.isPulseAudioEnabled()) {
					final String pulsesock = this.getPulseAudioSocketPath().toString();
					cgen.addArguments("--pulse-socket", hostPathReplacer.apply(pulsesock));
				}

				cgen.addArgument("--");
				emuRunner.getCommand()
						.forEach((cmdarg) -> cgen.addArgument(hostPathReplacer.apply(cmdarg)));

				cgen.setLogger(LOG);
				if (!cgen.execute()) {
					LOG.warning("Generating container's config failed!");
					emuBeanState.update(EmuCompState.EMULATOR_FAILED);
					return;
				}

				if (this.isSdlBackendEnabled()) {
					// Replace host data directory in emulator's config
					emuConfig.setIoSocket(hostPathReplacer.apply(emuConfig.getIoSocket()));
				}
			}

			emuRunner.cleanup();

			// Replace host's emulator command line...
			emuRunner.setCommand("emucon-run");
			emuRunner.addArguments("--non-interactive");
			emuRunner.addArguments("--container-id", cid);
			emuRunner.addArguments("--working-dir", workdir);
			emuRunner.addArguments("--rootfs-type", "tree");
			emuRunner.addArguments("--rootfs-dir", rootfsdir);
			if (emuEnvironment.hasCheckpointBindingId()) {
				try {
					final String checkpointBindingId = emuEnvironment.getCheckpointBindingId();
					final String checkpoint = this.lookupResource(checkpointBindingId);
					emuRunner.addArguments("--checkpoint", checkpoint);

					LOG.info("Container state will be restored from checkpoint");
				}
				catch (Exception error) {
					throw new BWFLAException("Looking up checkpoint image failed!", error)
							.setId(this.getComponentId());
				}
			}
		}

		emuRunner.redirectStdErrToStdOut(true);

		if (!emuRunner.start()) {
			throw new BWFLAException("Starting emulator failed!")
					.setId(this.getComponentId());
		}

		if (this.isXpraBackendEnabled()) {
			if (emuEnvironment.hasCheckpointBindingId()) {
				this.waitUntilPathExists(this.getXpraSocketPath(), EmuCompState.EMULATOR_BUSY);
				this.waitUntilRestoreDone();
			}
			else {
				final String rootfs = bindings.lookup(BindingsManager.toBindingId(EMUCON_ROOTFS_BINDING_ID, BindingsManager.EntryType.FS_MOUNT));
				final Path path = Paths.get(rootfs, "tmp", "xpra-started");
				this.waitUntilPathExists(path, EmuCompState.EMULATOR_BUSY);
			}
		}
		else if (this.isSdlBackendEnabled()) {
			if (emuEnvironment.hasCheckpointBindingId()) {
				// Wait for socket re-creation after resuming from a checkpoint
				this.waitUntilEmulatorCtlSocketAvailable(EmuCompState.EMULATOR_BUSY);
			}
			else {
				// Perform the following steps only, when starting a new emulator!
				// Skip them, when resuming from a checkpoint.

				this.waitUntilEmulatorCtlSocketReady(EmuCompState.EMULATOR_BUSY);

				if (!this.sendEmulatorConfig())
					return;

				if (!this.waitUntilEmulatorReady(EmuCompState.EMULATOR_BUSY))
					return;
			}
		}

		if (this.isPulseAudioEnabled())
			this.waitUntilPathExists(this.getPulseAudioSocketPath(), EmuCompState.EMULATOR_BUSY);

		LOG.info("Emulator started in process " + emuRunner.getProcessId());

		if (this.isSdlBackendEnabled()) {
			final Thread ctlSockObserver = workerThreadFactory.newThread(() -> {
					while (emuBeanState.fetch() != EmuCompState.EMULATOR_UNDEFINED) {
						try {
							// Try to receive new message
							if (!ctlMsgReader.read(5000))
								continue;

							// Message could be read, queue it for further processing
							if (ctlMsgReader.isNotification())
								ctlEvents.add(ctlMsgReader.getEventID());
							else {
								final byte msgtype = ctlMsgReader.getMessageType();
								final byte[] msgdata = ctlMsgReader.getMessageData();
								ctlMsgQueue.put(msgtype, msgdata);
							}
						}
						catch (Exception exception) {
							if (emuBeanState.fetch() == EmuCompState.EMULATOR_UNDEFINED)
								break;  // Ignore problems when destroying session!

							LOG.warning("An error occured while reading from control-socket!");
							LOG.log(Level.SEVERE, exception.getMessage(), exception);
						}
					}
				}
			);

			ctlSockObserver.start();
		}

		final Thread emuObserver = workerThreadFactory.newThread(() -> {

				emuRunner.waitUntilFinished();

				// upload emulator's output
				if (this.isOutputAvailable()) {
					LOG.info("Output is available!");
					try {
						this.processEmulatorOutput();
					}
					catch (BWFLAException error) {
						LOG.log(Level.WARNING, "Processing emulator's output failed!", error);
					}
				}
				else{
					LOG.info("No emulator output available!");
				}

				// cleanup will be performed later by EmulatorBean.destroy()

				synchronized (emuBeanState) {
					if (EmulatorBean.this.isLocalModeEnabled()) {
						// In local-mode emulator will be terminated by the user,
						// without using our API. Set the correct state here!
						emuBeanState.set(EmuCompState.EMULATOR_STOPPED);
					} else {
						final EmuCompState curstate = emuBeanState.get();
						if (curstate == EmuCompState.EMULATOR_RUNNING) {
							LOG.warning("Emulator stopped unexpectedly!");
							// FIXME: setting here also to STOPPED, since there is currently no reliable way
							// to determine (un-)successful termination depending on application exit code
							emuBeanState.set(EmuCompState.EMULATOR_STOPPED);
						}
					}
				}
		});

		emuObserver.start();

		if (printer != null) {
			final Thread worker = workerThreadFactory.newThread(printer);
			printer.setWorkerThread(worker);
			worker.start();
		}

		if (this.isSdlBackendEnabled()) {
			// Not in local mode?
			if (!this.isLocalModeEnabled()) {
				// Initialize the screenshot-tool
				if (this.isScreenshotEnabled) {
					scrshooter = new ScreenShooter(this.getComponentId(), 256);
					scrshooter.prepare();

					// Register the screenshot-tool
					interceptors.addInterceptor(scrshooter);
				}
			}

			// Prepare the connector for guacamole connections
			{
				final IThrowingSupplier<GuacTunnel> clientTunnelCtor = () -> {
					if (emuBeanState.fetch() == EmuCompState.EMULATOR_STOPPED) {
						final var message = "Attaching client to stopped emulator failed!";
						LOG.warning(message);
						throw new IllegalStateException(message);
					}

					final Runnable waitTask = () -> {
						try {
							EmulatorBean.this.attachClientToEmulator();
							EmulatorBean.this.waitForAttachedClient();
						}
						catch (Exception exception) {
							emuBeanState.update(EmuCompState.EMULATOR_FAILED);
							LOG.log(Level.SEVERE, "Attaching client to emulator failed!", exception);
						}
					};

					ioTaskExecutor.execute(waitTask);

					// Construct the tunnel
					final GuacTunnel tunnel = GuacTunnel.construct(tunnelConfig);
					if (!this.isLocalModeEnabled() && (player != null))
						player.start(tunnel, this.getComponentId(), emuRunner.getProcessMonitor());

					return (player != null) ? player.getPlayerTunnel() : tunnel;
				};

				this.addControlConnector(new GuacamoleConnector(clientTunnelCtor, emuConfig.isRelativeMouse()));
			}
		}
		else if (this.isXpraBackendEnabled()) {
			this.addControlConnector(new XpraConnector(this.getXpraSocketPath()));
		}

		if (this.isPulseAudioEnabled()) {
			final String cid = this.getComponentId();
			final Path pulsesock = this.getPulseAudioSocketPath();
			this.addControlConnector(new AudioConnector(() -> new PulseAudioStreamer(cid, pulsesock)));
		}

		this.addControlConnector(new StdoutLogConnector(emuRunner.getStdOutPath()));
		this.addControlConnector(new StderrLogConnector(emuRunner.getStdOutPath()));

		emuBeanState.update(EmuCompState.EMULATOR_RUNNING);
	}

	@Override
	synchronized public String stop() throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_RUNNING) {
				LOG.warning("Cannot stop emulator! Wrong state detected: " + curstate.value());
				return this.getEmulatorOutputLocation();
			}

			emuBeanState.set(EmuCompState.EMULATOR_BUSY);
		}

		this.stopInternal();

		emuBeanState.update(EmuCompState.EMULATOR_STOPPED);
		return this.getEmulatorOutputLocation();
	}

	private boolean isOutputAvailable()
	{
		boolean hasOutput = (emuEnvironment.getOutputBindingId() != null && !emuEnvironment.getOutputBindingId().isEmpty());
		return hasOutput;
	}

	//TODO when should this be used, when getChanged files?
	private Path getAllFiles(BlobStoreBinding binding, Path cow, FileSystemType fsType) throws BWFLAException {
		try (final ImageMounter mounter = new ImageMounter(LOG)) {
			Path output;
			if( binding.getResourceType() != Binding.ResourceType.ISO) {

                final Path workdir = this.getWorkingDir().resolve("output");
                try {
                    Files.createDirectories(workdir);
                }
                catch (IOException e)
                {
                    throw new BWFLAException("failed creating workdir").setId(this.getComponentId());
                }

                output = workdir.resolve("output.zip");

                // Mount partition's filesystem
                final ImageMounter.Mount rawmnt = mounter.mount(cow,
                        workdir.resolve(cow.getFileName() + ".dd"), binding.getPartitionOffset());
                final ImageMounter.Mount fsmnt = mounter.mount(rawmnt, workdir.resolve("fs"), fsType);

                final Path srcdir = fsmnt.getMountPoint();
                if (emuEnvironment.isLinuxRuntime())
                    Zip32Utils.zip(output.toFile(), srcdir.resolve(containerOutput).toFile());
                else {
                    Set<String> exclude = new HashSet<>();
                    exclude.add("uvi.bat");
                    exclude.add("autorun.inf");

                    Zip32Utils.zip(output.toFile(), srcdir.toFile(), exclude);
                }
                return output;
            }
			else {
				throw new BWFLAException("Unsupported binding type").setId(this.getComponentId());
			}
		}
	}

	private Path getChangedFiles(Path cowImage) throws BWFLAException {
	    List<Path> partitionFiles = new ArrayList<>();
		QcowOptions qcowOptions = new QcowOptions();
		try (final ImageMounter mounter = new ImageMounter(LOG)) {
		    ImageInformation imageInformation = new ImageInformation(cowImage.toString(), LOG);
            String backingFileId = imageInformation.getBackingFile();

            qcowOptions.setBackingFile(backingFileId);

            final Path workdir = this.getWorkingDir().resolve("output-all");
            Files.createDirectories(workdir);

            Path lowerImgPath = workdir.resolve("lowerImageLayer.cow");
            EmulatorUtils.createCowFile(lowerImgPath, qcowOptions);

            ImageMounter.Mount rawmnt = mounter.mount(lowerImgPath, workdir.resolve("lowerImageLayer.dd"));
            ImageMounter.Mount rawmnt2 = mounter.mount(cowImage, workdir.resolve("upperDir.dd"));

			// load partition table
			final DiskDescription disk = DiskDescription.read(rawmnt.getTargetImage(), LOG);
			if (!disk.hasPartitions())
				throw new BWFLAException("Disk seems to be not partitioned!");

			LOG.info("separating data by partition");
			for (DiskDescription.Partition partition : disk.getPartitions()) {
				if (!partition.hasFileSystemType()) {
					LOG.info("Partition " + partition.getIndex() + " is not formatted, skip");
					continue;
				}
				rawmnt = mounter.remount(rawmnt, partition.getStartOffset(), partition.getSize());
				rawmnt2 = mounter.remount(rawmnt2, partition.getStartOffset(), partition.getSize());

				FileSystemType fsType;
				try {
					fsType = FileSystemType.fromString(partition.getFileSystemType());
				}
				catch (IllegalArgumentException e)
				{
					LOG.warning("filesystem " + partition.getFileSystemType() + " not yet support. please report.");
					continue;
				}

				final ImageMounter.Mount fsmnt = mounter.mount(rawmnt, workdir.resolve("backingFileId.fs"), fsType);
				final Path lowerDir = fsmnt.getMountPoint();

				final ImageMounter.Mount fsmnt2 = mounter.mount(rawmnt2, workdir.resolve("upperDir.fs"), fsType);
				final Path upperDir = fsmnt2.getMountPoint();

				final Path outputDir = this.getWorkingDir().resolve("partition-" + partition.getIndex());

				LOG.info("Executing RSYNC to determine changed files...");
				DeprecatedProcessRunner processRunner = new DeprecatedProcessRunner("rsync");
				processRunner.addArguments("-armxv"); //, "--progress"); when using --progress, rsync sometimes hangs...
				processRunner.addArguments("--exclude", "dev");
				processRunner.addArguments("--exclude", "proc");
				processRunner.addArguments("--compare-dest=" + lowerDir.toAbsolutePath().toString() + "/",
						upperDir.toAbsolutePath().toString() + "/",
						outputDir.toAbsolutePath().toString());
				processRunner.execute(true);
				processRunner.cleanup();

				LOG.info("Done with rsync!");


				partitionFiles.add(outputDir);

				DeprecatedProcessRunner cleaner = new DeprecatedProcessRunner("find");
				cleaner.addArguments(outputDir.toAbsolutePath().toString(), "-empty", "-delete");
				cleaner.execute(true);
				cleaner.cleanup();
			}

			Path outputTar = workdir.resolve("output.tgz");
			DeprecatedProcessRunner tarCollectProc = new DeprecatedProcessRunner("tar");
			tarCollectProc.addArguments("-czf");
			tarCollectProc.addArguments(outputTar.toAbsolutePath().toString());
			tarCollectProc.addArguments("-C", this.getWorkingDir().toAbsolutePath().toString());
			partitionFiles.forEach(p -> tarCollectProc.addArguments(p.getFileName().toString()));
			tarCollectProc.execute(true);
			tarCollectProc.cleanup();

			return outputTar;
		}
		catch (BWFLAException  cause) {
			cause.printStackTrace();
			throw cause;
		}
		catch (IOException ioException)
        {
            ioException.printStackTrace();
            final String message = "IO Exception in getChangedFiles";
			throw new BWFLAException(message, ioException)
					.setId(this.getComponentId());
        }
	}


	private void processEmulatorOutput() throws BWFLAException
	{
		LOG.info("Processing emulator output ...");

		final String bindingId = emuEnvironment.getOutputBindingId();
		Path outputTar = null;

		Binding b = bindings.get(bindingId);
		if(b == null) {
			final String message = "Unknown output bindingId " + bindingId;
			final BWFLAException error = new BWFLAException(message)
					.setId(this.getComponentId());

			this.result.completeExceptionally(error);
		}

		try {

			if(emuEnvironment.isLinuxRuntime()){

				for (AbstractDataResource r : emuEnvironment.getAbstractDataResource())
				{
					if(r.getId() != null && r.getId().equals("rootfs"))
					{
						LOG.info("Linux runtime recognized, processing with rootfs...");
						final String cowImage = bindings.lookup(BindingsManager.toBindingId("rootfs", BindingsManager.EntryType.IMAGE));
						this.unmountBindings();
						outputTar = getChangedFiles(Path.of(cowImage));

					}
				}
			}

			else {
				final String qcow = bindings.lookup(BindingsManager.toBindingId(bindingId, BindingsManager.EntryType.IMAGE));
				this.unmountBindings();
				outputTar = getChangedFiles(Path.of(qcow));
			}

			final BlobDescription blob = new BlobDescription()
				.setDescription("Output for session " + this.getComponentId())
				.setNamespace("emulator-outputs")
				.setName("output");

			blob.setDataFromFile(outputTar);
			blob.setType(".tgz");

			// Upload archive to the BlobStore
			BlobHandle handle = BlobStoreClient.get()
                    .getBlobStorePort(blobStoreAddressSoap)
                    .put(blob);

            if (handle == null) {
                throw new BWFLAException("Output result is null").setId(this.getComponentId());
			}
			this.result.complete(handle);
		}
		catch (BWFLAException cause) {
			final String message = "Creation of output.zip failed!";
			final BWFLAException error = new BWFLAException(message, cause)
					.setId(this.getComponentId());

			this.result.completeExceptionally(error);
			throw error;
		}
	}

	private String getEmulatorOutputLocation() throws BWFLAException
	{
		if (!this.isOutputAvailable())
			return null;

		try {
			final BlobHandle handle = super.result.get(2, TimeUnit.MINUTES);
			if (blobStoreRestAddress.contains("http://eaas:8080"))
				return handle.toRestUrl(blobStoreRestAddress.replace("http://eaas:8080", ""));
			else
				return handle.toRestUrl(blobStoreRestAddress);
		}
		catch (Exception error) {
			final String message = "Waiting for emulator's output failed!";
			LOG.log(Level.WARNING, message, error);
			throw new BWFLAException(message, error)
					.setId(this.getComponentId());
		}
	}

	void stopInternal()
	{
		if (player != null)
			player.stop();

		this.closeAllConnectors();

		if (emuRunner.isProcessRunning())
			this.stopProcessRunner(emuRunner);

		if (printer != null)
			printer.stop();

		final var stdoutCon = (LogConnector) this.getControlConnector(StdoutLogConnector.PROTOCOL);
		if(stdoutCon != null)
			stdoutCon.cleanup();

		final var stderrCon = (LogConnector) this.getControlConnector(StderrLogConnector.PROTOCOL);
		if(stderrCon != null)
			stderrCon.cleanup();

	}

	private void closeAllConnectors()
	{
		if (this.isSdlBackendEnabled()) {
			final GuacamoleConnector connector = (GuacamoleConnector) this.getControlConnector(GuacamoleConnector.PROTOCOL);
			final GuacTunnel tunnel = (connector != null) ? connector.getTunnel() : null;
			if (tunnel != null && tunnel.isOpen()) {
				try {
					tunnel.disconnect();
					tunnel.close();
				}
				catch (GuacamoleException error) {
					LOG.log(Level.SEVERE, "Closing Guacamole connector failed!", error);
				}
			}
		}
		else if (this.isXpraBackendEnabled()) {
			final XpraConnector connector = (XpraConnector) this.getControlConnector(XpraConnector.PROTOCOL);
			if (connector != null) {
				try {
					connector.disconnect();
				}
				catch (Exception error) {
					LOG.log(Level.SEVERE, "Closing Xpra connector failed!", error);
				}
			}
		}

		if (this.isPulseAudioEnabled()) {
			final AudioConnector connector = (AudioConnector) this.getControlConnector(AudioConnector.PROTOCOL);
			final IAudioStreamer streamer = (connector != null) ? connector.getAudioStreamer() : null;
			if (streamer != null && !streamer.isClosed()) {
				try {
					streamer.stop();
					streamer.close();
				}
				catch (Exception error) {
					LOG.log(Level.WARNING, "Stopping audio streamer failed!", error);
				}
			}
		}
	}

	void stopProcessRunner(DeprecatedProcessRunner runner)
	{
		final int emuProcessId = runner.getProcessId();
		LOG.info("Stopping emulator " + emuProcessId + "...");
		try {
			if (this.isSdlBackendEnabled()) {
				// Send termination message
				ctlMsgWriter.begin(MessageType.TERMINATE);
				ctlMsgWriter.send(emuCtlSocketName);

				// Give emulator a chance to shutdown cleanly
				for (int i = 0; i < 10; ++i) {
					if (runner.isProcessFinished()) {
						LOG.info("Emulator " + emuProcessId + " stopped.");
						return;
					}

					Thread.sleep(500);
				}
			}
			else if ((this.isXpraBackendEnabled() || this.isHeadlessModeEnabled()) && this.isContainerModeEnabled()) {
				final var killer = new DeprecatedProcessRunner();
				final var cmds = new ArrayList<List<String>>(2);
				cmds.add(List.of("runc", "kill", this.getContainerId(), "TERM"));
				cmds.add(List.of("runc", "kill", "-a", this.getContainerId(), "KILL"));
				for (final var args : cmds) {
					killer.setCommand("sudo");
					killer.addArguments(args);
					killer.setLogger(LOG);
					killer.execute();

					if (runner.waitUntilFinished(5, TimeUnit.SECONDS)) {
						LOG.info("Emulator " + emuProcessId + " stopped.");
						return;
					}
				}
			}
		}
		catch (Exception exception) {
			LOG.log(Level.SEVERE, "Stopping emulator failed!", exception);
		}

		LOG.warning("Emulator " + emuProcessId + " failed to shutdown cleanly! Killing it...");
		runner.stop(5, TimeUnit.SECONDS);  // Try to terminate the process
		runner.kill();  // Try to kill the process
	}

	@Override
	public String  getRuntimeConfiguration() throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			if (emuBeanState.get() == EmuCompState.EMULATOR_UNDEFINED) {
				String message = "Runtime configuration is not available in this state!";
				throw new IllegalEmulatorStateException(message, EmuCompState.EMULATOR_UNDEFINED)
						.setId(this.getComponentId());
			}
		}
		try {
			return this.emuEnvironment.value();
		} catch (JAXBException e) {
			throw new BWFLAException("Serializing environment description failed!", e)
					.setId(this.getComponentId());
		}
	}
	
	@Override
	public Set<String> getColdplugableDrives()
	{
		// TODO: here read result from corresponding metadata
		return new HashSet<String>();
	}

	@Override
	public Set<String> getHotplugableDrives()
	{
		// TODO: here read result from corresponding metadata
		return new HashSet<String>();
	}

	private void sync() throws BWFLAException
	{
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setCommand("sync");
		if (!process.execute()) {
			throw new BWFLAException("Syncing filesystem failed!")
					.setId(this.getComponentId());
		}

		LOG.info("filesystem synced");
	}

	@Override
	public List<BindingDataHandler> snapshot() throws BWFLAException
	{
		synchronized (emuBeanState) {
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_STOPPED) {
				String message = "Cannot save environment in this state!";
				throw new IllegalEmulatorStateException(message, curstate)
						.setId(this.getComponentId());
			}
		}

		// Collect all modified images for used/mounted bindings
		final Map<String, String> images = new LinkedHashMap<String, String>();
		for (AbstractDataResource resource : emuEnvironment.getAbstractDataResource()) {
			if (!(resource instanceof Binding))
				continue;

			final Binding binding = (Binding) resource;
			if(((Binding) resource).getAccess() == Binding.AccessType.COPY)
				continue;
			
			final String id = binding.getId();

			final String path = bindings.lookup(BindingsManager.toBindingId(id, BindingsManager.EntryType.IMAGE));
			if (path == null) {
				LOG.info("Binding not used/mounted! Skipping: " + id);
				continue;
			}

			images.put(id, path);
		}

		// Now it should be safe to unmount!
		this.unmountBindings();
		this.sync();

		// TODO: filter out all unchanged images with qemu-img compare!

		final BlobStore blobstore = BlobStoreClient.get()
				.getBlobStorePort(blobStoreAddressSoap);

		// Create one DataHandler per image
		final List<BindingDataHandler> handlers = new ArrayList<BindingDataHandler>();
		try {
			for (Map.Entry<String, String> entry : images.entrySet()) {
				final String id = entry.getKey();
				final String path = entry.getValue();

				final BlobDescription blob = new BlobDescription()
						.setDescription("Snapshot for session " + this.getComponentId())
						.setNamespace("emulator-snapshots")
						.setDataFromFile(Paths.get(path))
						.setType(".qcow")
						.setName(id);

				// Upload image to blobstore and register cleanup handler
				final BlobHandle handle = blobstore.put(blob);
				cleanups.push("delete-blob/" + handle.getId(), () -> {
					try {
						blobstore.delete(handle);
					}
					catch (Exception exception) {
						LOG.log(Level.WARNING, "Removing snapshot-image from blobstore failed!", exception);
					}
				});

				final String location = handle.toRestUrl(blobStoreRestAddress);
				final BindingDataHandler handler = new BindingDataHandler()
						.setUrl(location)
						.setId(id);

				handlers.add(handler);
			}
		}
		catch (BWFLAException error) {
			LOG.log(Level.WARNING, "Uploading images failed!", error);
			error.setId(this.getComponentId());
			throw error;
		}

		return handlers;
	}

	@Override
	public int changeMedium(int containerId, String objReference) throws BWFLAException
	{
		try {
			LOG.info("change medium: " + objReference);
			Drive drive = this.emuEnvironment.getDrive().get(containerId);
			// detach the current medium
			this.connectDrive(drive, false);

			if (objReference == null || objReference.isEmpty()) {
				return containerId;
			}

			drive.setData(objReference);
			boolean attachOk = (emuBeanState.fetch() == EmuCompState.EMULATOR_RUNNING) ? connectDrive(drive, true) : addDrive(drive);

			if (!attachOk) {
				throw new BWFLAException("error occured in the last phase of device attachment")
						.setId(this.getComponentId());
			}
		} catch (IndexOutOfBoundsException e) {
			throw new BWFLAException("Cannot change medium: invalid drive id given.", e)
					.setId(this.getComponentId());
		}
		// TODO: change disk in run-time
		return containerId;
	}

	@Override
	@Deprecated
	public int attachMedium(DataHandler data, String mediumType) throws BWFLAException
	{
		/*
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();

			if (curstate != EmuCompState.EMULATOR_READY && curstate != EmuCompState.EMULATOR_RUNNING)
			{
				String message = "Cannot attach medium to emulator!";
				throw new IllegalEmulatorStateException(message, curstate)
						.setId(this.getComponentId());
			}
		}

		if (data == null) {
			throw new BWFLAException("Data stream cannot be null!")
					.setId(this.getComponentId());
		}

		Drive.DriveType type = Drive.DriveType.valueOf(mediumType.toUpperCase());
		Drive drive = EmulationEnvironmentHelper.findEmptyDrive(this.emuEnvironment, type);
		if (drive == null) {
			throw new BWFLAException("No more free slots of this type are available: " + type)
					.setId(this.getComponentId());
		}

		File objFile;
		try
		{
			final File datadir = this.getUploadsDir().toFile();
			objFile = EaasFileUtils.streamToTmpFile(datadir, data.getInputStream(), "digital_object_");
		}
		catch(IOException e)
		{
			throw new BWFLAException("an error occured while opening data stream or writing it to file", e)
					.setId(this.getComponentId());
		}

		File container = objFile;
		synchronized(container)
		{
			int id = this.emuEnvironment.getDrive().indexOf(drive);
			if(id == -1) {
				throw new BWFLAException("could not determine container ID")
						.setId(this.getComponentId());
			}

			VolatileResource res = new VolatileResource();
			res.setUrl("file://" + objFile.getAbsolutePath());
			res.setId("attached_container_" + id);
			try {
				this.prepareResource(res);
			} catch (IllegalArgumentException | IOException e) {
				throw new BWFLAException("Could not prepare the resource for this medium.", e)
						.setId(this.getComponentId());
			}
			this.emuEnvironment.getAbstractDataResource().add(res);

			drive.setData("binding://" + res.getId());

			boolean attachOk = (emuBeanState.fetch() == EmuCompState.EMULATOR_RUNNING) ? connectDrive(drive, true) : addDrive(drive);

			if (!attachOk) {
				throw new BWFLAException("error occured in the last phase of device attachment")
						.setId(this.getComponentId());
			}

			return id;
		}
		 */
		return -1;
	}

	@Override
	public DataHandler detachMedium(int containerId) throws BWFLAException
	{
		/*
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_READY && curstate != EmuCompState.EMULATOR_RUNNING && curstate != EmuCompState.EMULATOR_STOPPED) {
				String message = "Cannot detach medium from emulator!";
				throw new IllegalEmulatorStateException(message, curstate)
						.setId(this.getComponentId());
			}
		}

		List<AbstractDataResource> bindings = this.emuEnvironment.getAbstractDataResource();

		if(bindings != null)
			for(AbstractDataResource aBinding: bindings)
			{
				if(!(aBinding instanceof VolatileResource))
					continue;
				VolatileResource binding = (VolatileResource) aBinding;
				String id = "attached_container_" + containerId;
				String bindingId = binding.getId();

				if(id.equalsIgnoreCase(bindingId))
					try {
						File containerFile = new File(binding.getResourcePath());
						if (containerFile.isDirectory()) {
							throw new BWFLAException("detached container is in format (FS-directory), which is currently not supported for detachment")
									.setId(this.getComponentId());
						}

						if(containerFile.isFile())
							return new DataHandler(new FileDataSource(containerFile));
						else
							LOG.warning("missing proper container file at this location: " + containerFile.getAbsolutePath());
					}
					catch(Exception e)
					{
						LOG.log(Level.SEVERE, e.getMessage(), e);
						throw new BWFLAException("a server-side error occured, please try again later (see logs for details)")
								.setId(this.getComponentId());
					}
			}

		throw new BWFLAException("could not find container by this container id: " + containerId)
				.setId(this.getComponentId());

		 */
		return null;
	}


	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Protected
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private EmulatorBeanMode getEmuBeanMode(MachineConfiguration config) throws IllegalArgumentException
	{
		final UiOptions options = config.getUiOptions();
		if (options != null && options.getForwarding_system() != null) {
			var ret = EmulatorBeanMode.valueOf(options.getForwarding_system());
			if(ret == EmulatorBeanMode.HEADLESS && !this.isHeadlessSupported())
				return EmulatorBeanMode.SDLONP;
			return ret;
		}
		else return EmulatorBeanMode.SDLONP;
	}

	protected void prepareUserData(MachineConfiguration environment) throws BWFLAException {
		File src = new File(emulatorDataBase + getEmuContainerName(environment));
		if(!src.exists()) {
			LOG.info("no user data folder found " + src.getAbsolutePath());
			return;
		}

		File userData = new File(getDataDir().toFile(), getEmuContainerName(environment));
		if(userData.exists())
			throw new BWFLAException("copied user data twice");

		DeprecatedProcessRunner cp = new DeprecatedProcessRunner();
		cp.setCommand("cp");
		cp.addArgument("-rv");
		cp.addArgument(src.getAbsolutePath());
		cp.addArgument(userData.getAbsolutePath());
		if(!cp.execute())
			throw new BWFLAException("preparing emulator user data failed.");
	}

	protected void setRuntimeConfiguration(MachineConfiguration environment) throws BWFLAException
	{
		try {
			this.emuEnvironment = environment;
			LOG.info(emuEnvironment.value());
			for (AbstractDataResource resource: emuEnvironment.getAbstractDataResource())
				this.prepareResource(resource);

			prepareUserData(environment);

			MachineConfiguration.NativeConfig nativeConfig = emuEnvironment.getNativeConfig();
			this.prepareNativeConfig(nativeConfig);

			UiOptions uiOptions = emuEnvironment.getUiOptions();
			if (uiOptions != null) {
				this.isPulseAudioEnabled = false;
				if(uiOptions.getAudio_system() != null
						&& !uiOptions.getAudio_system().isEmpty()
						&& uiOptions.getAudio_system().equalsIgnoreCase("webrtc")) {
					this.isPulseAudioEnabled = true;
				}
			}

			this.prepareEmulatorRunner();

			if (uiOptions != null) {
				TimeOptions timeOptions = uiOptions.getTime();
				if (timeOptions != null) {
					if (timeOptions.getEpoch() != null) {
						long epoch = Long.parseLong(timeOptions.getEpoch());
						this.setEmulatorTime(epoch);
					} else if (timeOptions.getOffset() != null) {
						long offset = Long.parseLong(timeOptions.getEpoch());
						this.setEmulatorTime(offset);
					}
				}
			}

			this.setupEmulatorBackend();
			
			for(Drive drive: emuEnvironment.getDrive())
				prepareDrive(drive);

			for(Nic nic: emuEnvironment.getNic())
				prepareNic(nic);

			this.finishRuntimeConfiguration();

		} catch (IllegalArgumentException | IOException | JAXBException e) {
			throw new BWFLAException("Could not set runtime information.", e)
					.setId(this.getComponentId());
		}
	}

	protected String getNativeConfig()
	{
		return emuNativeConfig;
	}

	/** Must be overriden by subclasses to initialize the emulator's command. */
	protected abstract void prepareEmulatorRunner() throws BWFLAException;

	/** Callback for performing actions, deferred during runtime configuration. */
	protected void finishRuntimeConfiguration() throws BWFLAException
	{
		// Do nothing!
	}

	/**
	 * Determine the image format for a specified drive type for the current
	 * emulator.
	 * <p>
	 * This method should be overridden by any emulator that has specific
	 * file format needs (e.g. VirtualBox (yuck)).
	 *
	 * @param driveType The drive type
	 * @return The desired image format for the specified drive type
	 */
	protected XmountOutputFormat getImageFormatForDriveType(DriveType driveType) {
		// as default, we use raw images for everything
		return XmountOutputFormat.RAW;
	}

	/** Setups the emulator's backend */
	private void setupEmulatorBackend()
	{
		switch (emuBeanMode)
		{
			case SDLONP:
				this.setupEmulatorForSDLONP();
				break;

			case Y11:
				this.setupEmulatorForY11();
				break;

			case XPRA:
				// Nothing to setup!
		}
	}

	/** Setups the emulator's environment variables for running locally. */
	private void setupEmulatorForY11()
	{
		protocol = PROTOCOL_Y11;

		final String emusocket = this.getSocketsDir()
				.resolve("sdlonp-iosocket-emu").toString();

		// Setup emulator's tunnel
		final GuacamoleConfiguration gconf = tunnelConfig.getGuacamoleConfiguration();
		gconf.setProtocol(PROTOCOL_SDLONP);
		gconf.setParameter("enable-audio", "false");
		gconf.setParameter("emu-iosocket", emusocket);

		// Setup emulator's environment
		emuRunner.addEnvVariable("SDL_VIDEODRIVER", protocol);
		if (this.isPulseAudioEnabled())
			emuRunner.addEnvVariable("SDL_AUDIODRIVER", AUDIODRIVER_PULSE);

		emuRunner.addEnvVariable("SDL_SRVCTLSOCKET", ctlSocket.getName());
		emuRunner.addEnvVariable("SDL_EMUCTLSOCKET", emuCtlSocketName);
		emuRunner.addEnvVariable("ALSA_CARD", alsa_card);
		emuConfig.setIoSocket(emusocket);

		// TODO: should this parameter be read from meta-data?
		emuConfig.setInactivityTimeout(this.getInactivityTimeout());

		UiOptions uiopts = emuEnvironment.getUiOptions();
		if (uiopts == null)
			return;

		Html5Options html5 = uiopts.getHtml5();
		if (html5 == null)
			return;

		if (html5.isPointerLock())
			emuConfig.setRelativeMouse(true);
	}

	private String fmtDate(long epoch)
	{
		Date d = new Date(epoch);
		DateFormat format = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
		String formatted = format.format(d);
		return formatted;
	}

	protected void setEmulatorTime(long epoch)
	{
		//LOG.info("set emulator time: "  + epoch + " fmtStr " + fmtDate(epoch));
		// emuRunner.addEnvVariable("FAKETIME", ""+ fmtDate(epoch));
		// emuRunner.addEnvVariable("LD_PRELOAD", libfaketime);
	}

	protected void setEmulatorTimeOffset(long offset)
	{
	//	emuRunner.addEnvVariable("FAKE_TIME_OFFSET", ""+offset);
	//	emuRunner.addEnvVariable("LD_PRELOAD", "/usr/lib/x86_64-linux-gnu/faketime/libfaketime.so.1");
	}

	/** Setups the emulator's environment variables and tunnel for SDLONP-Protocol. */
	private void setupEmulatorForSDLONP()
	{
		protocol = PROTOCOL_SDLONP;

		final String emusocket = this.getSocketsDir()
				.resolve("sdlonp-iosocket-emu").toString();

		// Setup emulator's tunnel
		final GuacamoleConfiguration gconf = tunnelConfig.getGuacamoleConfiguration();
		gconf.setProtocol(protocol);
		gconf.setParameter("enable-audio", Boolean.toString(!this.isPulseAudioEnabled()));
		gconf.setParameter("emu-iosocket", emusocket);

		// Setup client configuration
		if (!this.isPulseAudioEnabled()) {
			final GuacamoleClientInformation ginfo = tunnelConfig.getGuacamoleClientInformation();
			ginfo.getAudioMimetypes().add("audio/ogg");
		}

		// Setup emulator's environment
		emuRunner.addEnvVariable("SDL_AUDIODRIVER", (this.isPulseAudioEnabled()) ? AUDIODRIVER_PULSE : protocol);
		emuRunner.addEnvVariable("SDL_VIDEODRIVER", protocol);
		emuRunner.addEnvVariable("SDL_SRVCTLSOCKET", ctlSocket.getName());
		emuRunner.addEnvVariable("SDL_EMUCTLSOCKET", emuCtlSocketName);
		emuConfig.setIoSocket(emusocket);

		// HACK: Qemu uses a custom audio setup!
		if (this instanceof QemuBean && this.isPulseAudioEnabled()	) {
			emuRunner.getEnvVariables()
					.remove("SDL_AUDIODRIVER");
		}

		emuConfig.setInactivityTimeout(this.getInactivityTimeout());

		UiOptions uiopts = emuEnvironment.getUiOptions();
		if (uiopts != null) {
			Html5Options html5 = uiopts.getHtml5();
			if (html5 != null) {
				if (html5.isPointerLock())
					emuConfig.setRelativeMouse(true);

				String crtopt = html5.getCrt();
				if (crtopt != null && !crtopt.isEmpty()) {
					emuConfig.setCrtFilter("snes-ntsc");
					emuConfig.setCrtPreset("composite");
				}
			}

			InputOptions input = uiopts.getInput();
			if (input != null) {
				String kbdModel = input.getEmulatorKbdModel();
				if (kbdModel != null && !kbdModel.isEmpty())
					emuConfig.setKeyboardModel(kbdModel);

				String kbdLayout = input.getEmulatorKbdLayout();
				if (kbdLayout != null && !kbdLayout.isEmpty())
					emuConfig.setKeyboardLayout(kbdLayout);

				String clientKbdModel = input.getClientKbdModel();
				if (clientKbdModel != null && !clientKbdModel.isEmpty())
					emuConfig.setClientKeyboardModel(clientKbdModel);

				String clientKbdLayout = input.getClientKbdLayout();
				if (clientKbdLayout != null && !clientKbdLayout.isEmpty())
					emuConfig.setClientKeyboardLayout(clientKbdLayout);
			}
		}
	}


	/* ==================== EmuCon API ==================== */

	@Override
	public DataHandler checkpoint() throws BWFLAException
	{
		if (!this.isContainerModeEnabled()) {
			throw new BWFLAException("Container mode disabled! Checkpointing not possible.")
					.setId(this.getComponentId());
		}

		this.closeAllConnectors();
		if (this.isSdlBackendEnabled()) {
			LOG.info("Waiting for emulator's detach-notification...");
			try {
				this.waitForClientDetachAck(10, TimeUnit.SECONDS);
			} catch (Exception e) {
				throw new BWFLAException("Waiting for emulator's detach-notification failed!", e)
						.setId(this.getComponentId());
			}
		}

		final Path imgdir = this.getStateDir();
		try {
			Files.createDirectories(imgdir);
		}
		catch (Exception error) {
			throw new BWFLAException("Creating checkpoint directory failed!", error)
					.setId(this.getComponentId());
		}

		final Path workdir = this.getWorkingDir();
		final Function<Path, String> relativizer = (abspath) -> {
			final Path relpath = workdir.relativize(abspath);
			return relpath.toString();
		};

		final Path checkpoint = workdir.resolve("checkpoint" + CHECKPOINT_FILE_EXTENSION);
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();

		LOG.info("Checkpointing emulator-container " + this.getContainerId() + "...");

		// Try to checkpoint the container...
		process.setCommand("emucon-checkpoint");
		process.addArgument("--non-interactive");

		for (String file : emuContainerFilesToCheckpoint) {
			final Path path = Paths.get(file);
			process.addArguments("--include", relativizer.apply(path));
		}

		process.addArguments("--image-dir", relativizer.apply(imgdir));
		process.addArguments("--output", checkpoint.toString());
		process.addArgument(this.getContainerId());
		process.setWorkingDirectory(workdir);
		process.setLogger(LOG);
		if (!process.execute()) {
			throw new BWFLAException("Checkpointing emulator-container failed!")
					.setId(this.getComponentId());
		}

		return new DataHandler(new FileDataSource(checkpoint.toFile()));
	}


	/* ==================== Session Recording Helpers ==================== */

	public boolean prepareSessionRecorder() throws BWFLAException
	{
		if (recorder != null) {
			LOG.info("SessionRecorder already prepared.");
			return true;
		}

		if (player != null) {
			String message = "Initialization of SessionRecorder failed, "
					+ "because SessionReplayer is already running. "
					+ "Using both at the same time is not supported!";

			throw new BWFLAException(message)
					.setId(this.getComponentId());
		}

		// Create and initialize the recorder
		recorder = new SessionRecorder(this.getComponentId(), MESSAGE_BUFFER_CAPACITY);
		try {
			// Create and setup a temp-file for the recording
			Path tmpfile = this.getDataDir().resolve(TRACE_FILE);
			recorder.prepare(tmpfile);
		}
		catch (IOException exception) {
			LOG.severe("Creation of output file for session-recording failed!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
			recorder = null;
			return false;
		}

		// Register the recorder as interceptor
		interceptors.addInterceptor(recorder);

		return true;
	}

	public void startSessionRecording() throws BWFLAException
	{
		this.ensureRecorderIsInitialized();
		recorder.start();
	}

	public void stopSessionRecording() throws BWFLAException
	{
		this.ensureRecorderIsInitialized();
		recorder.stop();
	}

	public boolean isRecordModeEnabled() throws BWFLAException
	{
		if (recorder == null)
			return false;

		return recorder.isRecording();
	}

	public void addActionFinishedMark()
	{
		//		this.ensureRecorderIsInitialized();

		InstructionBuilder ibuilder = new InstructionBuilder(16);
		ibuilder.start(ExtOpCode.ACTION_FINISHED);
		ibuilder.finish();

		recorder.postMessage(SourceType.INTERNAL, ibuilder.array(), 0, ibuilder.length());
	}

	/** Add a new metadata chunk to the trace-file. */
	public void defineTraceMetadataChunk(String tag, String comment) throws BWFLAException
	{
		this.ensureRecorderIsInitialized();
		recorder.defineMetadataChunk(tag, comment);
	}

	/** Add a key/value pair as metadata to the trace-file. */
	public void addTraceMetadataEntry(String ctag, String key, String value) throws BWFLAException
	{
		this.ensureRecorderIsInitialized();
		recorder.addMetadataEntry(ctag, key, value);
	}

	public String getSessionTrace() throws BWFLAException
	{
		this.ensureRecorderIsInitialized();

		try {
			recorder.finish();
		}
		catch (IOException exception) {
			LOG.severe("Finishing session-recording failed!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
			return null;
		}

		return recorder.toString();
	}

	private void ensureRecorderIsInitialized() throws BWFLAException
	{
		if (recorder == null) {
			throw new BWFLAException("SessionRecorder is not initialized!")
					.setId(this.getComponentId());
		}
	}


	/* ==================== Session Replay Helpers ==================== */

	public boolean prepareSessionPlayer(String trace, boolean headless) throws BWFLAException
	{
		if (player != null) {
			LOG.info("SessionPlayer already prepared.");
			return true;
		}

		if (recorder != null) {
			String message = "Initialization of SessionPlayer failed, "
					+ "because SessionRecorder is already running. "
					+ "Using both at the same time is not supported!";

			throw new BWFLAException(message)
					.setId(this.getComponentId());
		}

		Path file = this.getDataDir().resolve(TRACE_FILE);
		try {
			FileUtils.writeStringToFile(file.toFile(), trace);
		}
		catch (IOException exception) {
			LOG.severe("An error occured while writing temporary session-trace!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
			return false;
		}

		player = new SessionPlayerWrapper(file, headless);

		return true;
	}

	public int getSessionPlayerProgress()
	{
		if (player == null)
			return 0;

		return player.getProgress();
	}

	public boolean isReplayModeEnabled()
	{
		if (player == null)
			return false;

		return player.isPlaying();
	}

	/* ==================== Monitoring API ==================== */

	@Override
	public boolean updateMonitorValues()
	{
		ProcessMonitor monitor = emuRunner.getProcessMonitor();
		if (monitor == null) {
			// Process is currently not running!
			return false;
		}

		return monitor.update();
	}

	@Override
	public String getMonitorValue(ProcessMonitorVID id)
	{
		ProcessMonitor monitor = emuRunner.getProcessMonitor();
		if (monitor == null) {
			// Process is currently not running!
			return ProcessMonitor.INVALID_VALUE;
		}

		return monitor.getValue(id);
	}

	@Override
	public List<String> getMonitorValues(Collection<ProcessMonitorVID> ids)
	{
		ProcessMonitor monitor = emuRunner.getProcessMonitor();
		if (monitor == null) {
			// Process is currently not running!
			return ProcessMonitor.INVALID_VALUE_LIST;
		}

		return monitor.getValues(ids);
	}

	@Override
	public List<String> getAllMonitorValues()
	{
		ProcessMonitor monitor = emuRunner.getProcessMonitor();
		if (monitor == null) {
			// Process is currently not running!
			return ProcessMonitor.INVALID_VALUE_LIST;
		}

		return monitor.getValues();
	}


	/* ==================== PostScriptPrinter API ==================== */

	public List<PrintJob> getPrintJobs() throws  BWFLAException{
		if(printer == null)
			return null;

		return printer.getPrintJobs();
	}

	/* ==================== Screenshot API ==================== */

	public void takeScreenshot()
	{
		if(scrshooter != null)
			scrshooter.takeScreenshot();
	}

	public DataHandler getNextScreenshot()
	{
		if(scrshooter == null)
			return null;

		byte[] data = scrshooter.getNextScreenshot();
		if (data == null)
			return null;

		return new DataHandler(data, "application/octet-stream");
	}


	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//
	//	private int allocateContainerId(Container container)
	//	{
	//		int freeId = -1;
	//		final int MAX_TRIES = 50;
	//		for(int i = 0; (i < MAX_TRIES) && (freeId == -1); ++i)
	//		{
	//			freeId = (new Random()).nextInt();
	//			if(containers.containsKey(freeId))
	//				freeId = -1;
	//			else
	//				containers.put(freeId, container);
	//		}
	//
	//		return freeId;
	//	}

	private boolean sendEmulatorConfig()
	{
		try {
			emuConfig.sendAllTo(ctlSocket, emuCtlSocketName);
		}
		catch (Exception exception) {
			LOG.warning("Sending configuration to emulator failed!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			return false;
		}

		return true;
	}

	private void ensureEmuCompState(EmuCompState expstate, String msgsuffix) throws BWFLAException
	{
		if (emuBeanState.get() != expstate) {
			throw new BWFLAException("Expected state changed, abort waiting for " + msgsuffix + "!")
					.setId(this.getComponentId());
		}
	}

	private void ensureEmulatorRunning(String msgsuffix) throws BWFLAException
	{
		if (!emuRunner.isProcessRunning()) {
			throw new BWFLAException("Emulator failed, abort waiting for " + msgsuffix + "!")
					.setId(this.getComponentId());
		}
	}

	private boolean waitForReadyNotification(int expevent, String message, int timeout, EmuCompState expstate)
	{
		LOG.info(message);

		try {
			final int waittime = 1000;  // in ms
			int numretries = (timeout > waittime) ? timeout / waittime : 1;
			boolean isMsgAvailable = false;
			while (numretries > 0) {
				isMsgAvailable = ctlMsgReader.read(waittime);
				if (isMsgAvailable)
					break;

				this.ensureEmulatorRunning("notification");

				if (emuBeanState.get() != expstate) {
					LOG.warning("Expected state changed, abort waiting for notification!");
					return false;
				}

				--numretries;
			}

			if (!isMsgAvailable) {
				LOG.warning("Reading from emulator timed out!");
				return false;
			}

			if (!ctlMsgReader.isNotification()) {
				LOG.warning("Received message was not a notification!");
				return false;
			}

			if (ctlMsgReader.getEventID() != expevent) {
				LOG.warning("Received an unexpected notification from emulator!");
				return false;
			}
		}
		catch (Exception exception) {
			LOG.warning("Failed to read a notification-message from emulator.");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
			return false;
		}

		LOG.info("Received a ready-notification from emulator.");

		return true;
	}

	private void waitUntilEmulatorCtlSocketAvailable(EmuCompState expstate) throws BWFLAException
	{
		LOG.info("Waiting for emulator's control-socket to become available...");

		final Path socket = Paths.get(emuCtlSocketName);
		final int timeout = 30000;  // in ms
		final int waittime = 1000;  // in ms
		for (int numretries = timeout / waittime; numretries > 0; --numretries) {
			if (Files.exists(socket)) {
				LOG.info("Emulator's control-socket is now available.");
				return;
			}

			try {
				Thread.sleep(waittime);
			}
			catch (Exception error) {
				// Ignore it!
			}

			final String msgsuffix = "emulator's control-socket";
			this.ensureEmuCompState(expstate, msgsuffix);
			this.ensureEmulatorRunning(msgsuffix);
		}

		emuBeanState.update(EmuCompState.EMULATOR_FAILED);
		throw new BWFLAException("Emulator's control socket is not available!")
				.setId(this.getComponentId());
	}

	private void waitUntilEmulatorCtlSocketReady(EmuCompState expstate) throws BWFLAException
	{
		final int timeout = 60000;  // in ms

		final String message = "Waiting for emulator's control-socket to become ready...";
		boolean ok = this.waitForReadyNotification(EventID.EMULATOR_CTLSOCK_READY, message, timeout, expstate);
		if (!ok) {
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			throw new BWFLAException("Emulator's control socket is not reachable!")
					.setId(this.getComponentId());
		}
	}

	private boolean waitUntilEmulatorReady(EmuCompState expstate) throws BWFLAException
	{
		final int timeout = 30000;  // in ms

		final String message = "Waiting for emulator to become ready...";
		boolean ok = this.waitForReadyNotification(EventID.EMULATOR_READY, message, timeout, expstate);
		if (!ok) {
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			throw new BWFLAException("Emulator was not started properly!")
					.setId(this.getComponentId());
		}

		return ok;
	}

	private void waitUntilPathExists(Path path, EmuCompState expstate) throws BWFLAException
	{
		LOG.info("Waiting for path '" + path.toString() +"'...");

		final int timeout = 60000;  // in ms
		final int waittime = 1000;  // in ms
		for (int numretries = timeout / waittime; numretries > 0; --numretries) {
			if (Files.exists(path)) {
				LOG.info("Path '" + path.toString() +"' exists now");
				return;
			}

			try {
				Thread.sleep(waittime);
			}
			catch (Exception error) {
				// Ignore it!
			}

			final String msgsuffix = "path";
			this.ensureEmuCompState(expstate, msgsuffix);
			this.ensureEmulatorRunning(msgsuffix);
		}

		throw new BWFLAException("Path '" + path.toString() +"' does not exist!")
				.setId(this.getComponentId());
	}

	private void waitUntilRestoreDone()
	{
		LOG.info("Waiting for CRIU restore-worker to exit...");

		final DeprecatedProcessRunner waiter = new DeprecatedProcessRunner("/bin/sh");
		waiter.addArguments("-c", "while ! sudo runc " +
				"ps "  + this.getContainerId() + "; do :; done; while sudo runc ps " + this.getContainerId() + " | grep -q criu; do :; done");
		waiter.execute();
	}

	private void attachClientToEmulator() throws IOException, InterruptedException
	{
		LOG.info("Attaching client to emulator...");
		Thread.sleep(1000);
		synchronized (ctlMsgWriter) {
			ctlMsgWriter.begin(MessageType.ATTACH_CLIENT);
			ctlMsgWriter.send(emuCtlSocketName);
		}
	}
	
	private void waitForAttachedClient() throws IOException, InterruptedException
	{
		final int timeout = 1000;
		int numretries = 30;

		// Wait for the attached-event from emulator
		while (numretries > 0) {
			if (ctlEvents.await(EventID.CLIENT_ATTACHED, timeout)) {
				LOG.info("Client attached to emulator.");
				isClientAttachedFlag.set(true);
				return;  // Notification received!
			}
			
			final EmuCompState state = emuBeanState.fetch();
			if (state != EmuCompState.EMULATOR_BUSY && state != EmuCompState.EMULATOR_RUNNING) {
				LOG.warning("Expected state changed, abort attaching client to emulator!");
				return;
			}

			--numretries;
		}

		throw new IOException("Attaching client to emulator failed!");
	}

	private void waitForClientDetachAck(long timeout, TimeUnit unit) throws IOException, InterruptedException
	{
		// Wait for the detached-event from emulator
		if (!ctlEvents.await(EventID.CLIENT_DETACHED, unit.toMillis(timeout)))
			throw new IOException("No detach-notification received from emulator!");

		isClientAttachedFlag.set(false);
	}

	private String newCtlSocketName(String suffix)
	{
		return this.getSocketsDir()
				.resolve("sdlonp-ctlsocket-" + suffix)
				.toString();
	}

	protected BWFLAException newNotSupportedException()
	{
		return new BWFLAException("Operation is not supported!")
				.setId(this.getComponentId());
	}

	protected BWFLAException newNotImplementedException()
	{
		return new BWFLAException("Operation is not implemented!")
				.setId(this.getComponentId());
	}

	private BWFLAException newInitFailureException(String message, Throwable error)
	{
		emuBeanState.update(EmuCompState.EMULATOR_FAILED);
		LOG.log(Level.SEVERE, message, error);
		return new BWFLAException(message, error)
				.setId(this.getComponentId());
	}

	private ImageArchiveBinding findEmulatorImage(MachineConfiguration env) throws Exception
	{
		final var imageArchiveAddress = ConfigurationProvider.getConfiguration()
				.get("rest.imagearchive");

		try (final var archive = ImageArchiveClient.create(imageArchiveAddress)) {
			final var emuMetaHelper = new EmulatorMetaHelperV2(archive, LOG);
			final var name = this.getEmuContainerName(env);
			var version = env.getEmulator()
					.getContainerVersion();

			if (version == null || version.isEmpty())
				version = EmulatorMetaData.DEFAULT_VERSION;

			LOG.info("Looking up image for emulator '" + name + " (" + version + ")'...");
			final var emulator = emuMetaHelper.fetch(name, version);
			final var image = emulator.image();
			final var binding = new ImageArchiveBinding();
			binding.setId(EMUCON_ROOTFS_BINDING_ID);
			binding.setAccess(Binding.AccessType.COW);
			binding.setBackendName(this.getEmulatorArchive());
			binding.setFileSystemType(image.fileSystemType());
			binding.setType(image.category());
			binding.setImageId(image.id());
			binding.setUrl("");

			LOG.info("Using emulator's image '" + image.id() + "'");
			return binding;
		}
		catch (Exception error) {
			throw new BWFLAException("Emulator's image not found!", error)
					.setId(this.getComponentId());
		}
	}


	/**************************************************************************
	 *
	 * Here be Bindings
	 *
	 **************************************************************************/

	/**
	 * Resolves a binding location of either the form
	 * binding://binding_id[/path/to/subres] or binding_id[/path/to/subres]. The
	 * binding_id is replaced with the actual filesystem location of the
	 * binding's mountpoint. The possible reference to the subresource is
	 * preserved in the returned string.
	 *
	 * @param binding
	 *            A binding location
	 * @return The resolved path or null, if the binding cannot
	 *         be found
	 */
	protected String lookupResource(String binding) throws BWFLAException, IOException
	{
		String mountpoint = bindings.lookup(binding);
		if (mountpoint == null)
			mountpoint = bindings.mount(this.getComponentId(), binding, this.getBindingsDir());

		return mountpoint;
	}

	@Deprecated
	protected String lookupResource(String binding, XmountOutputFormat unused) throws BWFLAException, IOException {
		return lookupResource(binding);
	}

	@Deprecated
	protected String lookupResource(String binding, DriveType driveType)
			throws BWFLAException, IOException {
		// this.getImageFormatForDriveType(driveType)
		return this.lookupResource(binding);
	}

	protected void prepareResource(AbstractDataResource resource) throws IllegalArgumentException, IOException, BWFLAException
	{
		bindings.register(resource);

		// NOTE: Premount all object's entries to allow media-changes inside containers...
		if (this.isContainerModeEnabled() && (resource instanceof ObjectArchiveBinding)) {
			bindings.find(resource.getId() + "/")
					.forEach((binding) -> {
						try {
							this.lookupResource(binding);
						}
						catch (Exception error) {
							throw new IllegalArgumentException(error);
						}
					});
		}
	}

	/**************************************************************************
	 *
	 * Here be Drives
	 *
	 **************************************************************************/

	/**
	 * @param drive
	 */
	protected void prepareDrive(Drive drive) throws BWFLAException
	{
		// All drives *directly* work on a resource (binding) that has been
		// set up earlier, so no mounting, cow-ing or other tricks
		// are necessary here.

		if(drive.getData() == null || drive.getData().isEmpty())
			return;

		addDrive(drive);

		// String img = null;
		//
		// FIXME: check if this is still necessary after refactoring (if yes,
		// refactor more)
		//
		// if (drive instanceof VolatileDrive) {
		// // The drive should be written to in-place, ignoring the
		// // value of getAccess(), as it is a temporary copy of user-data
		//
		// // (TODO) Currently only file: transport is allowed here
		// if (!drive.getData().startsWith("file:")) {
		// log.
		// warning("Only 'file:' transport is allowed for injected objects/VolatileDrives.");
		// continue;
		// }
		// // just use the file on the filesystem directly as is
		// img = drive.getData().replace("file://", "");
		// } else {

	}

	protected abstract boolean addDrive(Drive drive) throws BWFLAException;

	protected abstract boolean connectDrive(Drive drive, boolean attach) throws BWFLAException;

	/**************************************************************************
	 *
	 * Here be Networks
	 *
	 **************************************************************************/

	/**
	 * @param nic
	 */
	protected void prepareNic(Nic nic) throws BWFLAException, IOException
	{
		// create a vde_switch in hub mode
		// the switch can later be identified using the NIC's MAC address
		Path vdeHubName = this.getNetworksDir()
				.resolve("nic_" + nic.getHwaddress());

		if (this.isContainerModeEnabled()) {
			// Pre-create switch-directory to be mounted into container!
			Files.createDirectories(vdeHubName);

			// Compute switch-directory in container-space
			final Path hostDataDir = this.getDataDir();
			final Path conDataDir = Paths.get(EMUCON_DATA_DIR);
			vdeHubName = conDataDir.resolve(hostDataDir.relativize(vdeHubName));
		}
		else {
			DeprecatedProcessRunner process = new DeprecatedProcessRunner("vde_switch");
			process.addArgument("-hub");
			process.addArgument("-s");
			process.addArgument(vdeHubName.toString());
			if (!process.start())
				return; // Failure

			vdeProcesses.add(process);
		}

		this.addControlConnector(new EthernetConnector(nic.getHwaddress(), vdeHubName, this));
		this.addNic(nic);
	}

	protected abstract boolean addNic(Nic nic) throws BWFLAException;

	/**************************************************************************
	 *
	 * Here be native config
	 *
	 **************************************************************************/

	/**
	 * @param nativeConfig
	 */
	protected void prepareNativeConfig(MachineConfiguration.NativeConfig nativeConfig)
	{
		if(nativeConfig != null)
		{
			String nativeString = nativeConfig.getValue();
			if(nativeConfig.getLinebreak() != null)
			{
				nativeString = nativeString.replace("\n", "").replace("\r", "");
				nativeString = nativeString.replace(nativeConfig.getLinebreak(), "\n");
			}

			// search for binding:// and replace all occurrences with the
			// actual path
			// Pattern p = Pattern.compile("binding://(\\w*/?)");
			Pattern p = Pattern.compile("binding://(\\w*/?)|rom://(\\S+)");
			Matcher m = p.matcher(nativeString);
			StringBuffer sb = new StringBuffer();
			while(m.find())
			{
				String bindingPath;
				try {
					String res = m.group(1);
					if(res == null) // should be a rom. but check again
					{
						if(!m.group(0).startsWith("rom"))
						{
							LOG.info("could not resolve resource: " + m.group(0));
							continue;
						}
						res = m.group(0);
					}

					bindingPath = this.lookupResource(res.trim());
				} catch (Exception e) {
					LOG.severe("lookupResource with " + m.group(1) + " failed.");
					LOG.log(Level.SEVERE, e.getMessage(), e);
					continue;
				}
				if(bindingPath == null)
				{
					LOG.severe("lookupResource with " + m.group(1) + " failed.");
					continue;
				}
				LOG.info(m.group(1));
				LOG.info("Replacing " + m.group(0) + " by " + bindingPath);
				m.appendReplacement(sb, bindingPath);
			}
			m.appendTail(sb);

			emuNativeConfig = sb.toString();
		}
	}

	protected String getEmulatorWorkdir()
	{
		return null;
	}

	public Tail getEmulatorStdOut()
	{
		LogConnector logCon = (LogConnector)getControlConnector(StdoutLogConnector.PROTOCOL);
		if(logCon == null)
			return null;
		return logCon.connect();
	}

	public Tail getEmulatorStdErr()
	{
		LogConnector logCon = (LogConnector)getControlConnector(StderrLogConnector.PROTOCOL);
		if(logCon == null)
			return null;
		return logCon.connect();
	}
}
