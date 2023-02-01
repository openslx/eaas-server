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

package de.bwl.bwfla.emucomp.components.containers;

import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emucomp.api.AbstractDataResource;
import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import de.bwl.bwfla.emucomp.api.ComponentState;
import de.bwl.bwfla.emucomp.api.ContainerComponent;
import de.bwl.bwfla.emucomp.api.ContainerConfiguration;
import de.bwl.bwfla.emucomp.api.DockerContainerConfiguration;
import de.bwl.bwfla.emucomp.api.OciContainerConfiguration;
import de.bwl.bwfla.emucomp.components.BindingsManager;
import de.bwl.bwfla.emucomp.components.EaasComponentBean;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

@Deprecated
public abstract class ContainerBean extends EaasComponentBean implements ContainerComponent
{
	protected final ContainerBeanState conBeanState = new ContainerBeanState(ContainerState.UNDEFINED);

	protected ContainerConfiguration config;

	protected final DeprecatedProcessRunner conRunner = new DeprecatedProcessRunner();

	protected final BindingsManager bindings = new BindingsManager();

	@Resource(lookup = "java:jboss/ee/concurrency/factory/default")
	protected ManagedThreadFactory workerThreadFactory;

	@Inject
	@Config("components.containers.blobstore")
	private String blobStoreAddress = null;

	@Inject
	@Config("components.containers.usernamespace.enabled")
	private boolean conUserNamespaceEnabled = false;

	@Inject
	@Config("components.containers.usernamespace.user")
	private String conRuntimeUser = null;

	@Inject
	@Config("components.containers.usernamespace.group")
	private String conRuntimeGroup = null;


	public static ContainerBean createContainerBean(ContainerConfiguration config) throws ClassNotFoundException
	{
		Class<?> clazz = null;
		if (config instanceof OciContainerConfiguration)
			clazz = RuncBean.class;
		else if (config instanceof DockerContainerConfiguration)
			clazz = DockerBean.class;
		else throw new ClassNotFoundException("Unsupported container configuration type: " + config.getClass().getName());

        return (ContainerBean) CDI.current().select(clazz).get();
	}

	public Path getDataDir()
	{
		return this.getWorkingDir().resolve("data");
	}

	public Path getBindingsDir()
	{
		return this.getDataDir().resolve("bindings");
	}

	public Path getOutputDir()
	{
		return this.getDataDir().resolve("output");
	}

	@Override
	public String getComponentType()
	{
	    return "container";
	}

	@Override
	public ComponentState getState()
	{
		final ContainerState state = conBeanState.fetch();
		switch (state) {
			case RUNNING:
				return ComponentState.RUNNING;
			case FAILED:
				return ComponentState.FAILED;
			case STOPPED:
				return ComponentState.STOPPED;
			default:
				return ComponentState.INITIALIZING;
		}
	}

	public static void sync() throws BWFLAException
	{
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setCommand("sync");
		if (!process.execute())
			throw new BWFLAException("Syncing filesystem failed!");
	}


	@Override
	public void initialize(ComponentConfiguration compConfig) throws BWFLAException
	{
		synchronized (conBeanState) {
			final ContainerState curstate = conBeanState.get();
			if (curstate != ContainerState.UNDEFINED)
				this.abort("Cannot initialize ContainerBean! Wrong state detected: " + curstate.value());

			conBeanState.set(ContainerState.BUSY);
		}

		try {
			this.createWorkingSubDirs();
		}
		catch (IOException error) {
			this.fail("Creating working subdirs failed!", error);
		}

		conRunner.setLogger(LOG);

		try {
			this.config = (ContainerConfiguration) compConfig;
		}
		catch (Exception error) {
			this.fail("Parsing container's configuration failed!", error);
		}

		try {
			// Register all specified resources
			for (AbstractDataResource resource : config.getDataResources())
				bindings.register(resource);

			final Path outdir = this.getBindingsDir();

			// Resolve and mount all bindings
			if (config.hasInputs()) {
				for (ContainerConfiguration.Input input : config.getInputs())
					bindings.mount(this.getComponentId(), input.getBinding(), outdir);
			}

			this.prepare();
		}
		catch (Exception error) {
			this.fail("Preparing container's resources failed!", error);
			error.printStackTrace();
		}

		final String compid = this.getComponentId();
		LOG.info("Container session '" + compid + "' initialized");
		LOG.info("Working directory for session '" + compid + "' created at: " + this.getWorkingDir());
		conBeanState.update(ContainerState.READY);
	}

	@Override
	public void start() throws BWFLAException
	{
		synchronized (conBeanState) {
			final ContainerState curstate = conBeanState.get();
			if (curstate != ContainerState.READY && curstate != ContainerState.STOPPED)
				this.abort("Cannot start container! Wrong state detected: " + curstate.value());

			conBeanState.set(ContainerState.BUSY);
		}

		if (!conRunner.start())
			this.fail("Starting container failed!");

		LOG.info("Container started in process " + conRunner.getProcessId());

		final Thread conObserver = workerThreadFactory.newThread(() -> {

				conRunner.waitUntilFinished();

				LOG.info("Preparing container's output...");
				try {
					final String name = "output";
					final String extention = ".tar.gz";

					final Path outdir = this.getOutputDir();
					final Path workdir = this.getWorkingDir();
					final Path archive = workdir.resolve(name + extention);

					// Always include container's stdout/err to output archive!
					ContainerBean.copy(conRunner.getStdOutPath(), outdir);
					ContainerBean.copy(conRunner.getStdErrPath(), outdir);

					// Create an archive file
					final DeprecatedProcessRunner tar = new DeprecatedProcessRunner("tar");
					tar.addArguments("--create", "--auto-compress", "--totals");
					tar.addArguments("--file", archive.toString());
					tar.addArguments("--directory", outdir.toString());
					try (Stream<Path> stream = Files.list(outdir)) {
						// Add each file to the archive...
						stream.forEach((path) -> {
							final Path file = path.getFileName();
							tar.addArgument(file.toString());
						});
					}

					sync();

					tar.setWorkingDirectory(outdir);
					tar.setLogger(LOG);
					if (!tar.execute())
						throw new IOException("Creating output archive failed!");

					LOG.info("Uploading container's output to blobstore at " + blobStoreAddress + "...");

					// Create a BLOB description for the created archive
					final BlobDescription blob = new BlobDescription()
							.setDescription("Container's output for session " + this.getComponentId())
							.setNamespace("container-outputs")
							.setAccessToken(UUID.randomUUID().toString())
							.setDataFromFile(archive)
							.setType(extention)
							.setName(name);

					// Upload the archive to the BlobStore
					final BlobHandle handle = BlobStoreClient.get()
							.getBlobStorePort(blobStoreAddress)
							.put(blob);

					LOG.info("Container's output uploaded to blobstore");

					result.complete(handle);
				}
				catch (Exception error) {
					this.failNoThrow("Preparing container's output failed!\n", error);
					return;
				}

				// cleanup will be performed later by ContainerBean.destroy()

				synchronized (conBeanState) {
					final ContainerState curstate = conBeanState.get();
					if (curstate != ContainerState.RUNNING)
						return;

					if (conRunner.getReturnCode() == 0) {
						LOG.info("Container stopped normally");
						conBeanState.set(ContainerState.STOPPED);
					}
					else {
						LOG.warning("Container stopped unexpectedly, returning code: " + conRunner.getReturnCode());
						conBeanState.set(ContainerState.FAILED);
					}
				}
			}
		);

		conObserver.start();
		conBeanState.update(ContainerState.RUNNING);
	}

	@Override
	public void stop() throws BWFLAException
	{
		synchronized (conBeanState) {
			final ContainerState curstate = conBeanState.get();
			if (curstate != ContainerState.RUNNING)
				this.abort("Cannot stop container! Wrong state detected: " + curstate.value());

			conBeanState.set(ContainerState.BUSY);
		}

		this.stopInternal();

		conBeanState.update(ContainerState.STOPPED);
	}

	@Override
	public void destroy()
	{
		synchronized (conBeanState) {
			final ContainerState curstate = conBeanState.get();
			if (curstate == ContainerState.UNDEFINED)
				return;

			if (curstate == ContainerState.BUSY) {
				LOG.severe("Destroying ContainerBean while other operation is in-flight!");
				return;
			}

			conBeanState.set(ContainerState.UNDEFINED);
		}

		this.stopInternal();
		bindings.cleanup();

		// Cleanup container's runner here
		conRunner.printStdOut();
		conRunner.printStdErr();
		conRunner.cleanup();

		{
			DeprecatedProcessRunner runner = new DeprecatedProcessRunner("sudo");
			runner.addArguments("--non-interactive", "--", "rm", "-r", "-f");
			runner.addArgument(this.getOutputDir().toString());
			if(!runner.execute()){
				LOG.warning("Deleting of output dir failed!");
			}
		}

		LOG.info("ContainerBean for session " + this.getComponentId() + " destroyed.");

		// Destroy base class!
		super.destroy();
	}


	/* =============== Internal Helpers =============== */

	protected void abort(String message) throws BWFLAException
	{
		LOG.warning(message);
		throw new BWFLAException(message);
	}

	protected void abort(String message, Exception error) throws BWFLAException
	{
		LOG.log(Level.WARNING, message + "\n", error);
		throw new BWFLAException(message, error);
	}

	protected void fail(String message) throws BWFLAException
	{
		conBeanState.update(ContainerState.FAILED);
		this.abort(message);
	}

	protected void fail(String message, Exception error) throws BWFLAException
	{
		conBeanState.update(ContainerState.FAILED);
		this.abort(message, error);
	}

	protected void failNoThrow(String message, Exception error)
	{
		conBeanState.update(ContainerState.FAILED);
		LOG.log(Level.WARNING, message + "\n", error);
	}

	protected String getContainerId()
	{
		final String compid = this.getComponentId();
		return compid.substring(1 + compid.lastIndexOf("+"));
	}

	protected String getContainerRuntimeUser()
	{
		return conRuntimeUser;
	}

	protected String getContainerRuntimeGroup()
	{
		return conRuntimeGroup;
	}

	protected boolean isUserNamespaceEnabled()
	{
		return conUserNamespaceEnabled;
	}

	/** Prepare container runtime. Should be overridden by subclasses. */
	protected void prepare() throws Exception
	{
		// Do nothing!
	}

	private void createWorkingSubDirs() throws IOException
	{
		// Currently, working directory is structured as follows:
		//
		// <workdir>/
		//     data/           -> Session specific data
		//         bindings/   -> Object/image bindings
		//         output/     -> Output files

		Files.createDirectories(this.getDataDir());
		Files.createDirectories(this.getBindingsDir());
		Files.createDirectories(this.getOutputDir());
	}

	private void stopInternal()
	{
		if (conRunner.isProcessRunning()) {
			final String cid = this.getContainerId();
			LOG.info("Stopping container " + cid + "...");
			conRunner.stop();
			LOG.info("Container " + cid + " stopped");
		}
	}

	private static void copy(Path source, Path dstdir, CopyOption... options) throws IOException
	{
		Files.copy(source, dstdir.resolve(source.getFileName()), options);
	}
}
