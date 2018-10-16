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
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.common.utils.ProcessMonitor;
import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.common.utils.net.ConfigKey;
import de.bwl.bwfla.common.utils.net.PortRangeProvider;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;
import de.bwl.bwfla.emucomp.api.EmulatorUtils.XmountOutputFormat;
import de.bwl.bwfla.emucomp.components.BindingsManager;
import de.bwl.bwfla.emucomp.components.EaasComponentBean;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.EventID;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.MessageType;
import de.bwl.bwfla.emucomp.control.connectors.EthernetConnector;
import de.bwl.bwfla.emucomp.control.connectors.GuacamoleConnector;
import de.bwl.bwfla.emucomp.control.connectors.IThrowingSupplier;
import de.bwl.bwfla.emucomp.control.connectors.XpraConnector;
import de.bwl.bwfla.emucomp.xpra.XpraUtils;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
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

	@Inject
	@Config("emucomp.blobstore_soap")
	private String blobStoreAddressSoap = null;
	@Inject
	@Config("emucomp.blobstore_rest")
	private String blobStoreAddressRest = null;

	@Inject
	@Config(value = "ws.imagearchive")
	private String imageArchiveAddress = null;

    @Resource(lookup = "java:jboss/ee/concurrency/executor/io")
    protected ExecutorService ioTaskExecutor;

	@Resource(lookup = "java:jboss/ee/concurrency/factory/default")
	protected ManagedThreadFactory workerThreadFactory;
    
	protected final TunnelConfig tunnelConfig = new TunnelConfig();

	protected final EmulatorBeanState emuBeanState = new EmulatorBeanState(EmuCompState.EMULATOR_UNDEFINED);

	protected MachineConfiguration emuEnvironment;
	private String emuNativeConfig;
	protected final Map<Integer, File> containers = Collections.synchronizedMap(new HashMap<Integer, File>());

	protected final DeprecatedProcessRunner emuRunner = new DeprecatedProcessRunner();
	protected final ArrayList<DeprecatedProcessRunner> vdeProcesses = new ArrayList<DeprecatedProcessRunner>();

	protected final DeprecatedProcessRunner xpraRunner = new DeprecatedProcessRunner();

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

	/** Data directory inside of the emulator-containers */
	private static final String EMUCON_DATA_DIR = "/emucon/data";

	/** Binding ID for container's root filesystem */
	private static final String EMUCON_ROOTFS_BINDING_ID = "emucon-rootfs";

	@Inject
	@ConfigKey("components.xpra.ports")
	protected PortRangeProvider.Port listenPort;

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
	private List<String> emuContainerFilesToCheckpoint = null;

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
		return (emuBeanMode != EmulatorBeanMode.XPRA);
	}

	public boolean isXpraBackendEnabled()
	{
		return (emuBeanMode == EmulatorBeanMode.XPRA);
	}

	public boolean isLocalModeEnabled()
	{
		return (emuBeanMode == EmulatorBeanMode.Y11);
	}

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

	@Override
	public ComponentState getState() throws BWFLAException
	{
		String emulatorBeanState = getEmulatorState();
		// TODO proper state return
		if(emulatorBeanState.equals(EmuCompState.EMULATOR_INACTIVE.value()))
			return ComponentState.INACTIVE;
		if (emulatorBeanState.equals(EmuCompState.EMULATOR_STOPPED.value()))
			return ComponentState.STOPPED;
		if (emulatorBeanState.equals(EmuCompState.EMULATOR_FAILED.value()))
			return ComponentState.FAILED;
		return ComponentState.OK;
	}

	public String getEmulatorState()
	{
		if(printer != null)
			printer.update();

		final boolean isEmulatorInactive = ctlEvents.poll(EventID.CLIENT_INACTIVE);
		synchronized (emuBeanState) {
			if (isEmulatorInactive)
				emuBeanState.set(EmuCompState.EMULATOR_INACTIVE);
			return emuBeanState.get().value();
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

	public Path getDataDir()
	{
		final Path workdir = this.getWorkingDir();
		if (this.isContainerModeEnabled())
			return workdir.resolve("data");

		return workdir;
	}

	public Path getBindingsDir()
	{
		return this.getDataDir().resolve("bindings");
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

	private void createWorkingSubDirs() throws IOException
	{
		// Currently, working directory in container-mode is structured as follows:
		//
		// <workdir>/
		//     state/          -> Container's memory dump
		//     data/           -> Session/emulator specific data
		//         bindings/   -> Object/image bindings
		//         networks/   -> Networking files
		//         sockets/    -> IO + CTRL sockets
		//         uploads/    -> Uploaded files

		// If container-mode is disabled:  <workdir>/ == data/

		Files.createDirectories(this.getDataDir());
		Files.createDirectories(this.getBindingsDir());
		Files.createDirectories(this.getNetworksDir());
		Files.createDirectories(this.getSocketsDir());
		Files.createDirectories(this.getUploadsDir());
	}

	/** Returns emulator's runtime layer name. */
	protected String getEmuContainerName(MachineConfiguration machineConfiguration)
	{
		final String message = this.getClass().getSimpleName()
				+ " does not support container-mode!";

		throw new UnsupportedOperationException(message);
	}

	public void initialize(ComponentConfiguration compConfig) throws BWFLAException
	{
		synchronized (emuBeanState) {
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_UNDEFINED) {
				String message = "Cannot initialize EmulatorBean!";
				throw new IllegalEmulatorStateException(message, curstate);
			}

			emuBeanState.set(EmuCompState.EMULATOR_BUSY);
		}

		emuRunner.setLogger(LOG);

		try {
			this.createWorkingSubDirs();
		}
		catch (IOException error) {
			LOG.log(Level.WARNING, "Creating working subdirs failed!\n", error);
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			return;
		}

		if (this.isSdlBackendEnabled()) {
			// Create control sockets
			try {
				ctlSocket = IpcSocket.create(this.newCtlSocketName("srv"), true);
				ctlMsgWriter = new IpcMessageWriter(ctlSocket);
				ctlMsgReader = new IpcMessageReader(ctlSocket);
				emuCtlSocketName = this.newCtlSocketName("emu");
			}
			catch (Throwable exception) {
				LOG.warning("Constructing control sockets failed!");
				LOG.log(Level.SEVERE, exception.getMessage(), exception);
				emuBeanState.update(EmuCompState.EMULATOR_FAILED);
				return;
			}

			// Prepare configuration for tunnels
			tunnelConfig.setGuacdHostname("localhost");
			tunnelConfig.setGuacdPort(TunnelConfig.GUACD_PORT);
			tunnelConfig.setInterceptor(interceptors);
		}

		emuConfig.setHardTermination(false);

		try
		{
			final MachineConfiguration env = (MachineConfiguration) compConfig;
			if (this.isContainerModeEnabled()) {
				// Check, that rootfs-image is specified!
				final boolean isRootFsFound = env.getAbstractDataResource().stream()
						.anyMatch((resource) -> resource.getId().contentEquals(EMUCON_ROOTFS_BINDING_ID));

				if (!isRootFsFound) {
					// Not found, try to get latest image ID as configured by the archive
					final EnvironmentsAdapter archive = new EnvironmentsAdapter(imageArchiveAddress);
					final String name = EMUCON_ROOTFS_BINDING_ID + "/" + this.getEmuContainerName(env);
					final ImageArchiveBinding image = archive.getImageBinding(name, "latest");
					if (image == null)
						throw new BWFLAException("Emulator's rootfs-image not found!");

					// Add rootfs binding
					image.setId(EMUCON_ROOTFS_BINDING_ID);
					env.getAbstractDataResource().add(image);
				}
			}

			this.setRuntimeConfiguration(env);
		}
		catch(IllegalArgumentException e)
		{
			emuBeanState.update(EmuCompState.EMULATOR_CLIENT_FAULT);
			return;
		}
		catch(Throwable e)
		{
			LOG.log(Level.SEVERE, e.getMessage(), e);
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			return;
		}

		final String compid = this.getComponentId();
		LOG.info("Emulation session '" + compid + "' initialized in " + emuBeanMode.name() + " mode.");
		LOG.info("Working directory for session '" + compid + "' created at: " + this.getWorkingDir());
		emuBeanState.update(EmuCompState.EMULATOR_READY);
	}

	private void unmountBindings()
	{
		bindings.cleanup();
	}

	public void destroy()
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
		if (this.isSdlBackendEnabled()) {
			emuRunner.printStdOut();
			emuRunner.printStdErr();
			emuRunner.cleanup();
		}
		else if (this.isXpraBackendEnabled()) {
			xpraRunner.printStdOut();
			xpraRunner.printStdErr();
			xpraRunner.cleanup();
			listenPort.release();
		}

		LOG.info("EmulatorBean for session " + this.getComponentId() + " destroyed.");

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
				throw new BWFLAException("Cannot start emulator! Wrong state detected: " + curstate.value());
			}
			emuBeanState.set(EmuCompState.EMULATOR_BUSY);
		}
		try {
			if (this.isSdlBackendEnabled())
				this.startWithSdlBackend();
			else if (this.isXpraBackendEnabled())
				this.startWithXpraBackend();
			else {
				emuBeanState.update(EmuCompState.EMULATOR_FAILED);
				throw new BWFLAException("Trying to start emulator using unimplemented mode: " + this.getEmuBeanMode());
			}
		} catch(Throwable error){
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			LOG.log(Level.SEVERE, error.getMessage(), error);
			throw new BWFLAException(error);
		}
	}

	protected void startWithXpraBackend() throws BWFLAException {
		try {
			final int port = listenPort.get();
			XpraUtils.startXpraSession(xpraRunner, emuRunner.getCommandString(), port, LOG);
			if (!XpraUtils.waitUntilReady(port, 50000)) {
				emuBeanState.update(EmuCompState.EMULATOR_FAILED);
				throw new BWFLAException("Connecting to Xpra server failed!");
			}

			LOG.info("Xpra server started in process " + xpraRunner.getProcessId());

			final Thread xpraObserver = workerThreadFactory.newThread(() -> {

					xpraRunner.waitUntilFinished();

					// cleanup will be performed later by EmulatorBean.destroy()
					synchronized (emuBeanState) {
						final EmuCompState curstate = emuBeanState.get();
						if (curstate == EmuCompState.EMULATOR_RUNNING) {
							LOG.info("Xpra server stopped unexpectedly!");
							emuBeanState.set(EmuCompState.EMULATOR_STOPPED);
						}
					}
				}
			);

			xpraObserver.start();

			this.addControlConnector(new XpraConnector(port));
			emuBeanState.update(EmuCompState.EMULATOR_RUNNING);
		}
		catch (Exception error) {
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			throw new BWFLAException("Starting Xpra server failed!", error);
		}
	}

	private void startWithSdlBackend() throws BWFLAException, IOException {
		if (this.isLocalModeEnabled())
			LOG.info("Local-mode enabled. Emulator will be started locally!");

		if (this.isContainerModeEnabled()) {
			LOG.info("Container-mode enabled. Emulator will be started inside of a container!");

			final String cid = this.getContainerId();
			final String workdir = this.getWorkingDir().toString();
			final String rootfsdir = this.lookupResource(EMUCON_ROOTFS_BINDING_ID, XmountOutputFormat.RAW);

			emuContainerFilesToCheckpoint = new ArrayList<String>();

			// Generate container's config
			{
				final String conConfigPath = Paths.get(workdir, "config.json").toString();

				final DeprecatedProcessRunner cgen = new DeprecatedProcessRunner();
				cgen.setCommand("emucon-cgen");
				cgen.addArguments("--output", conConfigPath);
				cgen.addArguments("--user-id", emuContainerUserId);
				cgen.addArguments("--group-id", emuContainerGroupId);
				cgen.addArguments("--rootfs", rootfsdir);

				final String hostDataDir = this.getDataDir().toString();

				final Function<String, String> hostPathReplacer =
						(cmdarg) -> cmdarg.replace(hostDataDir, EMUCON_DATA_DIR);

				// Safety check. Should never fail!
				if (!this.getBindingsDir().startsWith(this.getDataDir())) {
					final String message = "Assumption failed: '" + this.getBindingsDir()
							+ "' must be a subdir of '" + this.getDataDir() + "'!";

					LOG.warning(message);
					emuBeanState.update(EmuCompState.EMULATOR_FAILED);
					return;
				}

				// Mount emulator's data dir entries, skipping bindings
				try (Stream<Path> entries = Files.list(this.getDataDir())) {
					final Path bindingsDirName = this.getBindingsDir().getFileName();
					entries.filter((entry) -> !entry.getFileName().equals(bindingsDirName))
							.forEach((entry) -> {
								final String path = entry.toString();
								cgen.addArgument("--mount");
								cgen.addArgument(path, ":", hostPathReplacer.apply(path), ":bind:rw");
							});
				}
				catch (Exception error) {
					LOG.log(Level.WARNING, "Listing '" + hostDataDir + "' failed!\n", error);
					emuBeanState.update(EmuCompState.EMULATOR_FAILED);
					return;
				}

				final List<String> bindingIdsToSkip = new ArrayList<String>();
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
				cgen.addArguments("--", "/usr/bin/emucon-init", "--networks-dir", conNetDir, "--");
				emuRunner.getCommand()
						.forEach((cmdarg) -> cgen.addArgument(hostPathReplacer.apply(cmdarg)));

				cgen.setLogger(LOG);
				if (!cgen.execute()) {
					LOG.warning("Generating container's config failed!");
					emuBeanState.update(EmuCompState.EMULATOR_FAILED);
					return;
				}

				// Replace host data directory in emulator's config
				emuConfig.setIoSocket(hostPathReplacer.apply(emuConfig.getIoSocket()));
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
					final String checkpoint = this.lookupResource(checkpointBindingId, XmountOutputFormat.RAW);
					emuRunner.addArguments("--checkpoint", checkpoint);

					LOG.info("Container state will be restored from checkpoint");
				}
				catch (Exception error) {
					emuBeanState.update(EmuCompState.EMULATOR_FAILED);
					throw new BWFLAException("Looking up checkpoint image failed!");
				}
			}
		}

		if (!emuRunner.start()) {
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			throw new BWFLAException("Starting emulator failed!");
		}

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

		LOG.info("Emulator started in process " + emuRunner.getProcessId());

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
					} catch (Exception exception) {
						if (emuBeanState.fetch() == EmuCompState.EMULATOR_UNDEFINED)
							break;  // Ignore problems when destroying session!

						LOG.warning("An error occured while reading from control-socket!");
						LOG.log(Level.SEVERE, exception.getMessage(), exception);
					}
				}
			}
		);

		final Thread emuObserver = workerThreadFactory.newThread(() -> {

				emuRunner.waitUntilFinished();

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

		ctlSockObserver.start();
		emuObserver.start();

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

		emuBeanState.update(EmuCompState.EMULATOR_RUNNING);
	}

	@Override
	public void stop() throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_RUNNING) {
				LOG.warning("Cannot stop emulator! Wrong state detected: " + curstate.value());
				return;
			}

			emuBeanState.set(EmuCompState.EMULATOR_BUSY);
		}

		this.stopInternal();

		if (this.isOutputAvailable())
			this.processOutput();

		emuBeanState.update(EmuCompState.EMULATOR_STOPPED);
	}

	private boolean isOutputAvailable()
	{
		return emuEnvironment.getOutputBindingId() != null;
	}

	private void processOutput() throws BWFLAException
	{
		final String bindingId = emuEnvironment.getOutputBindingId();
		final BlobStoreBinding binding = (BlobStoreBinding) bindings.get(bindingId);
		final FileSystemType fsType = binding.getFileSystemType();

		// HACK: Compute the name of the binding's qcow-file
		final String mountpoint = bindings.lookup(bindingId);
		final String qcow = mountpoint.substring(0, mountpoint.indexOf(".fuse"));

		this.unmountBindings();

		Path output = null;
		String type = null;

		Path rawmnt = null, fusemnt = null;
		final BlobDescription blob = new BlobDescription()
				.setDescription("Output for session " + this.getComponentId())
				.setNamespace("emulator-outputs")
				.setName("output");

		try {
			BlobHandle handle = null;
			if( binding.getResourceType() != Binding.ResourceType.ISO) {

				final Path workdir = EaasFileUtils.createTempDirectory(this.getWorkingDir(), "output-");
				rawmnt = workdir.resolve("raw");
				fusemnt = workdir.resolve("fuse");

				// Mount partition only
				final XmountOptions options = new XmountOptions();
				options.setOffset(binding.getPartitionOffset());
				final Path rawimg = EmulatorUtils.xmount(qcow, rawmnt, options, LOG);

				// Mount partition's filesystem
				EmulatorUtils.mountFileSystem(rawimg, fusemnt, fsType);

				 output = workdir.resolve("output.zip");
				Zip32Utils.zip(output.toFile(), fusemnt.toFile());
				type = ".zip";

				blob.setDataFromFile(output);
				blob.setType(type);
				// Upload archive to the BlobStore
				handle = BlobStoreClient.get()
						.getBlobStorePort(blobStoreAddressSoap)
						.put(blob);
			} else {
				handle = BlobHandle.fromUrl(binding.getUrl());
			}

			if(handle == null)
				throw new BWFLAException("Output result is null");

			this.result.complete(handle);
		}
		catch (BWFLAException | IOException error) {
			final String message = "Creation of output.zip failed!";
			LOG.log(Level.WARNING, message, error);
			throw new BWFLAException(message, error);
		}
		finally {
			try {
				EmulatorUtils.checkAndUnmount(fusemnt, rawmnt);
			}
			catch (IOException error) {
				LOG.log(Level.WARNING, "Could not unmount after creation of output.zip!", error);
			}
		}
	}

	private void stopInternal()
	{
		if (player != null)
			player.stop();

		if(printer != null)
			printer.release();

		final GuacamoleConnector connector = (GuacamoleConnector) this.getControlConnector(GuacamoleConnector.PROTOCOL);
		final GuacTunnel tunnel = (connector != null) ? connector.getTunnel() : null;
		if (tunnel != null && tunnel.isOpen()) {
			try {
				tunnel.disconnect();
				tunnel.close();
			} catch (GuacamoleException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		if (emuRunner.isProcessRunning())
			this.stopProcessRunner(emuRunner);

		if (xpraRunner.isProcessRunning())
			this.stopXpraServer(xpraRunner);
	}

	private void stopProcessRunner(DeprecatedProcessRunner runner)
	{
		final int emuProcessId = runner.getProcessId();
		LOG.info("Stopping emulator " + emuProcessId + "...");

		if(ctlMsgWriter != null) {
			try {
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
			} catch (Exception exception) {
				LOG.log(Level.SEVERE, exception.getMessage(), exception);
			}
		}

		LOG.warning("Emulator " + emuProcessId + " failed to shutdown cleanly! Killing it...");
		runner.stop(5, TimeUnit.SECONDS);  // Try to terminate the process
		runner.kill();  // Try to kill the process
	}

	private void stopXpraServer(DeprecatedProcessRunner runner)
	{
		final int xpraProcessId = runner.getProcessId();
		LOG.info("Stopping Xpra server " + xpraProcessId + "...");

		// We need to send INT signal to gracefully stop Xpra server
		DeprecatedProcessRunner xpraKiller = new DeprecatedProcessRunner();
		xpraKiller.setCommand("kill");
		xpraKiller.addArgument("-SIGINT");
		xpraKiller.addArgument("" + xpraRunner.getProcessId());
		xpraKiller.execute();

		try {
			// Give Xpra server a chance to shutdown cleanly
			for (int i = 0; i < 10; ++i) {
				if (runner.isProcessFinished()) {
					LOG.info("Xpra server " + xpraProcessId + " stopped.");
					return;
				}

				Thread.sleep(500);
			}
		}
		catch (Exception exception) {
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
		}

		LOG.warning("Xpra server " + xpraProcessId + " failed to shutdown cleanly! Zomby processes may be left.");
	}

	@Override
	public String  getRuntimeConfiguration() throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			if (emuBeanState.get() == EmuCompState.EMULATOR_UNDEFINED) {
				String message = "Runtime configuration is not available in this state!";
				throw new IllegalEmulatorStateException(message, EmuCompState.EMULATOR_UNDEFINED);
			}
		}
		try {
			return this.emuEnvironment.value();
		} catch (JAXBException e) {
			throw new BWFLAException("an error occured at server during return data serialization", e);
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
		if (!process.execute())
			throw new BWFLAException("Syncing filesystem failed!");

		LOG.info("filesystem synced");
	}

	@Override
	public List<BindingDataHandler> snapshot() throws BWFLAException
	{
		synchronized (emuBeanState) {
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_STOPPED) {
				String message = "Cannot save environment in this state!";
				throw new IllegalEmulatorStateException(message, curstate);
			}
		}

		// Collect all modified images for used/mounted bindings
		final Map<String, String> images = new LinkedHashMap<String, String>();
		for (AbstractDataResource resource : emuEnvironment.getAbstractDataResource()) {
			if (!(resource instanceof Binding))
				continue;

			final Binding binding = (Binding) resource;
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

		// Create one DataHandler per image
		final List<BindingDataHandler> handlers = new ArrayList<BindingDataHandler>();
		images.forEach((id, path) -> {
			final BindingDataHandler handler = new BindingDataHandler()
					.setDataFromFile(Paths.get(path))
					.setId(id);

			handlers.add(handler);
		});

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

			//            Resource res = new VolatileResource();
			//            res.setUrl(objReference);
			//            res.setId("attached_container_" + containerId);
			//            this.prepareResource(res);
			//            this.emuEnvironment.getBinding().add(res);


			drive.setData(objReference);
			//            this.emuEnvironment.getDrive().add(drive);

			boolean attachOk = (emuBeanState.fetch() == EmuCompState.EMULATOR_RUNNING) ? connectDrive(drive, true) : addDrive(drive);

			if(!attachOk)
				throw new BWFLAException("error occured in the last phase of device attachment");
		} catch (IndexOutOfBoundsException e) {
			throw new BWFLAException("Cannot change medium: invalid drive id given.", e);
		}
		// TODO: change disk in run-time
		return containerId;
	}

	@Override
	public int attachMedium(DataHandler data, String mediumType) throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();

			if (curstate != EmuCompState.EMULATOR_READY && curstate != EmuCompState.EMULATOR_RUNNING)
			{
				String message = "Cannot attach medium to emulator!";
				throw new IllegalEmulatorStateException(message, curstate);
			}
		}

		if (data == null)
			throw new BWFLAException("Data stream cannot be null!");

		Drive.DriveType type = Drive.DriveType.valueOf(mediumType.toUpperCase());
		Drive drive = EmulationEnvironmentHelper.findEmptyDrive(this.emuEnvironment, type);
		if (drive == null)
			throw new BWFLAException("No more free slots of this type are available: " + type);

		File objFile;
		try
		{
			final File datadir = this.getUploadsDir().toFile();
			objFile = EaasFileUtils.streamToTmpFile(datadir, data.getInputStream(), "digital_object_");
		}
		catch(IOException e)
		{
			throw new BWFLAException("an error occured while opening data stream or writing it to file", e);
		}

		File container = objFile;
		synchronized(container)
		{
			int id = this.emuEnvironment.getDrive().indexOf(drive);
			if(id == -1)
				throw new BWFLAException("could not determine container ID");

			VolatileResource res = new VolatileResource();
			res.setUrl("file://" + objFile.getAbsolutePath());
			res.setId("attached_container_" + id);
			try {
				this.prepareResource(res);
			} catch (IllegalArgumentException | IOException | JAXBException e) {
				throw new BWFLAException("Could not prepare the resource for this medium.", e);
			}
			this.emuEnvironment.getAbstractDataResource().add(res);

			drive.setData("binding://" + res.getId());

			boolean attachOk = (emuBeanState.fetch() == EmuCompState.EMULATOR_RUNNING) ? connectDrive(drive, true) : addDrive(drive);

			if(!attachOk)
				throw new BWFLAException("error occured in the last phase of device attachment");

			return id;
		}
	}

	@Override
	public DataHandler detachMedium(int containerId) throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_READY && curstate != EmuCompState.EMULATOR_RUNNING && curstate != EmuCompState.EMULATOR_STOPPED) {
				String message = "Cannot detach medium from emulator!";
				throw new IllegalEmulatorStateException(message, curstate);
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
						if(containerFile.isDirectory())
							throw new BWFLAException("detached container is in format (FS-directory), which is currently not supported for detachment");

						if(containerFile.isFile())
							return new DataHandler(new FileDataSource(containerFile));
						else
							LOG.warning("missing proper container file at this location: " + containerFile.getAbsolutePath());
					}
					catch(Exception e)
					{
						LOG.log(Level.SEVERE, e.getMessage(), e);
						throw new BWFLAException("a server-side error occured, please try again later (see logs for details)");
					}
			}

		throw new BWFLAException("could not find container by this container id: " + containerId);
	}


	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Protected
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected void setRuntimeConfiguration(MachineConfiguration environment) throws BWFLAException
	{
		try {
			this.emuEnvironment = environment;
			LOG.info(emuEnvironment.value());
			for (AbstractDataResource resource: emuEnvironment.getAbstractDataResource())
				this.prepareResource(resource);

			MachineConfiguration.NativeConfig nativeConfig = emuEnvironment.getNativeConfig();
			this.prepareNativeConfig(nativeConfig);
			this.prepareEmulatorRunner();

			UiOptions uiOptions = emuEnvironment.getUiOptions();
			if (uiOptions != null) {
				if (uiOptions.getForwarding_system() != null)
					emuBeanMode = EmulatorBeanMode.valueOf(uiOptions.getForwarding_system());
				else
					emuBeanMode = EmulatorBeanMode.SDLONP;
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

		} catch (IllegalArgumentException | IOException e) {
			throw new BWFLAException("Could not set runtime information.", e);
		} catch (JAXBException e) {

			LOG.log(Level.SEVERE, e.getMessage(), e);
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
		LOG.info("set emulator time: "  + epoch + " fmtStr " + fmtDate(epoch));
		emuRunner.addEnvVariable("FAKETIME", ""+ fmtDate(epoch));
		emuRunner.addEnvVariable("LD_PRELOAD", libfaketime);
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
		gconf.setParameter("enable-audio", "true");
		gconf.setParameter("emu-iosocket", emusocket);

		// Setup client configuration
		final GuacamoleClientInformation ginfo = tunnelConfig.getGuacamoleClientInformation();
		ginfo.getAudioMimetypes().add("audio/ogg");

		// Setup emulator's environment
		emuRunner.addEnvVariable("SDL_AUDIODRIVER", protocol);
		emuRunner.addEnvVariable("SDL_VIDEODRIVER", protocol);
		emuRunner.addEnvVariable("SDL_SRVCTLSOCKET", ctlSocket.getName());
		emuRunner.addEnvVariable("SDL_EMUCTLSOCKET", emuCtlSocketName);
		emuConfig.setIoSocket(emusocket);

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
		if (!this.isContainerModeEnabled())
			throw new BWFLAException("Container mode disabled! Checkpointing not possible.");

		try {
			// Client must be disconnected from emulator for checkpointing to succeed!
			LOG.info("Waiting for emulator's detach-notification...");
			this.waitForClientDetachAck(10, TimeUnit.SECONDS);
		}
		catch (Exception error) {
			final String message = "Waiting for emulator's detach-notification failed!"
					+ " Checkpointing not possible with connected clients.";

			throw new BWFLAException(message, error);
		}

		final Path imgdir = this.getStateDir();
		try {
			Files.createDirectories(imgdir);
		}
		catch (Exception error) {
			throw new BWFLAException("Creating checkpoint directory failed!", error);
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
		if (!process.execute())
			throw new BWFLAException("Checkpointing emulator-container failed!");

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

			throw new BWFLAException(message);
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
		if (recorder == null)
			throw new BWFLAException("SessionRecorder is not initialized!");
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

			throw new BWFLAException(message);
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

			if (emuBeanState.get() != expstate) {
				final String message = "Expected state changed, abort waiting for emulator's control-socket!";
				LOG.warning(message);
				throw new BWFLAException(message);
			}
		}

		emuBeanState.update(EmuCompState.EMULATOR_FAILED);
		throw new BWFLAException("Emulator's control socket is not available!");
	}

	private void waitUntilEmulatorCtlSocketReady(EmuCompState expstate) throws BWFLAException
	{
		final int timeout = 30000;  // in ms

		final String message = "Waiting for emulator's control-socket to become ready...";
		boolean ok = this.waitForReadyNotification(EventID.EMULATOR_CTLSOCK_READY, message, timeout, expstate);
		if (!ok) {
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			throw new BWFLAException("Emulator's control socket is not reachable!");
		}
	}

	private boolean waitUntilEmulatorReady(EmuCompState expstate) throws BWFLAException
	{
		final int timeout = 30000;  // in ms

		final String message = "Waiting for emulator to become ready...";
		boolean ok = this.waitForReadyNotification(EventID.EMULATOR_READY, message, timeout, expstate);
		if (!ok) {
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			throw new BWFLAException("Emulator was not started properly!");
		}

		return ok;
	}

	private void attachClientToEmulator() throws IOException, InterruptedException
	{
		LOG.info("Attaching client to emulator...");

		synchronized (ctlMsgWriter) {
			ctlMsgWriter.begin(MessageType.ATTACH_CLIENT);
			ctlMsgWriter.send(emuCtlSocketName);
		}
	}
	
	private void waitForAttachedClient() throws IOException, InterruptedException
	{
		final int timeout = 1000;
		int numretries = 15;

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
	protected String lookupResource(String binding, XmountOutputFormat outputFormat)
			throws BWFLAException, IOException
	{
		String mountpoint = bindings.lookup(binding);
		if (mountpoint == null)
			mountpoint = bindings.mount(binding, this.getBindingsDir(), outputFormat);

		return mountpoint;
	}

	protected String lookupResource(String binding, DriveType driveType)
			throws BWFLAException, IOException {
		return this.lookupResource(binding, this.getImageFormatForDriveType(driveType));
	}

	protected void prepareResource(AbstractDataResource resource) throws IllegalArgumentException, IOException, BWFLAException, JAXBException
	{
		bindings.register(resource);
	}

	/**************************************************************************
	 *
	 * Here be Drives
	 *
	 **************************************************************************/

	/**
	 * @param drive
	 */
	protected void prepareDrive(Drive drive)
	{
		// All drives *directly* work on a resource (binding) that has been
		// set up earlier, so no mounting, cow-ing or other tricks
		// are necessary here.

		if(drive.getData().isEmpty())
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

	protected abstract boolean addDrive(Drive drive);

	protected abstract boolean connectDrive(Drive drive, boolean attach);

	/**************************************************************************
	 *
	 * Here be Networks
	 *
	 **************************************************************************/

	/**
	 * @param nic
	 */
	protected void prepareNic(Nic nic) throws IOException
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

	protected abstract boolean addNic(Nic nic);

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
			Pattern p = Pattern.compile("binding://(\\w*/?)|rom://(.*)");
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

					bindingPath = this.lookupResource(res, XmountOutputFormat.RAW);
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
}
