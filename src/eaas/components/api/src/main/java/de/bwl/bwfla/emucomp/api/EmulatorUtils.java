package de.bwl.bwfla.emucomp.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.common.services.security.MachineTokenProvider;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.common.utils.ImageInformation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import org.apache.tamaya.ConfigurationProvider;

public class EmulatorUtils {
	protected static final Logger log = Logger.getLogger("EmulatorUtils");
	private static String curlProxySo = ConfigurationProvider.getConfiguration().get("emucomp.curl_proxy");

	public enum XmountOutputFormat {
		RAW("raw"),
		VDI("vdi"),
		VHD("vhd"),
		VMDK("vmdk");

		private final String format;

		private XmountOutputFormat(String s) {
			this.format = s;
		}

		public String toString() {
			return this.format;
		}
	}
	public enum XmountInputFormat {
		RAW("raw"),
		QEMU("qemu");

		private final String format;

		private XmountInputFormat(String s) {
			this.format = s;
		}

		public String toString() {
			return this.format;
		}
	}

	public static String connectBinding(Binding resource, Path resourceDir, MountOptions mountOpts)
			throws BWFLAException, IllegalArgumentException {

		if (resource == null
				|| resource.getId() == null
				|| resource.getId().isEmpty()
				|| resource.getUrl() == null
				|| resource.getUrl().isEmpty()) {
			throw new IllegalArgumentException(
					"Given resource is null, has invalid id or empty url.");
		}

		if (resource.getAccess() == null)
			resource.setAccess(Binding.AccessType.COW);

		switch (resource.getAccess()) {
			case COW:
				// create cow container
				// Qemu's block layer driver handles many transport protocols,
				// as long as we don't want to support yet another one, we can
				// safely ignore transports here and just pass the url to Qemu.
				Path cowPath = resourceDir.resolve(resource.getId() + ".cow");

				QcowOptions qcowOptions = new QcowOptions();
				qcowOptions.setBackingFile(resource.getUrl());

				if(MachineTokenProvider.getAuthenticationProxy() != null)
					qcowOptions.setProxyUrl(MachineTokenProvider.getAuthenticationProxy());
				else
					qcowOptions.setProxyUrl(MachineTokenProvider.getProxy());

				EmulatorUtils.createCowFile(cowPath, qcowOptions);

				Path imageMount = cowPath
						.resolveSibling(cowPath.getFileName() + ".dd");
				try {
					return mountCowFile(cowPath, imageMount, mountOpts)
							.toString();
				} catch (IOException e) {
					throw new BWFLAException("Could not fuse-mount image file.", e);
				}
			case COPY:
				// use qemu-imgs convert feature to create a new local raw copy
				Path imgCopy = resourceDir.resolve(resource.getId() + ".copy");
				copyRemoteUrl(resource, imgCopy, log);
				return imgCopy.toString();
			default:
				log.severe("This should never happen!");
				throw new BWFLAException("cannot connect COW transport not defined.");
		}
	}

	public static void copyRemoteUrl(Binding resource, Path dest) throws BWFLAException {
		EmulatorUtils.copyRemoteUrl(resource, dest, log);
	}

	public static void copyRemoteUrl(Binding resource, Path dest, Logger log) throws BWFLAException {
		String resUrl = resource.getUrl();
		// hack until qemu-img is fixed
		if (resUrl.startsWith("http") || resUrl.startsWith("https")) {
			DeprecatedProcessRunner process = new DeprecatedProcessRunner("curl");
			process.setLogger(log);
			process.addArgument(resUrl);
			process.addArgument("-s");
			process.addArgument("-k"); // insecure, disables SSL check
			process.addArgument("-g");
			process.addArgument("-f"); // fail on 404
			process.addArgument("-S");
			process.addArgument("-L");
			if (resource.getUsername() != null && resource.getPassword() != null) {
				process.addArgument("-u");
				process.addArgument(resource.getUsername() + ":" + resource.getPassword());
			}
			process.addArgument("-o");
			process.addArgument(dest.toString());
			if (!process.execute()) {
				throw new BWFLAException(
						"Cannot create local copy of " + resUrl + " the binding's data.");
			}
		}
		else if(resUrl.startsWith("file:") && Files.exists(Paths.get(resUrl.replace("file:", "")))) // shortcut to copy the file
		{
			try {
				Files.copy(Paths.get(resUrl.replace("file:", "")), dest);
			} catch (IOException e) {
				log.log(Level.SEVERE, e.getMessage(), e);
				throw new BWFLAException(e);
			}
		}
		else throw new BWFLAException("unsupported operation " + resUrl);
	}

	/**
	 * Creates a copy-on-write wrapper (in qcow2 file format) for imgUrl at the
	 * specified directory.
	 *
	 * @param cowPath Path where the qcow2 file will be created at.
	 */
	public static void createCowFile(Path cowPath, QcowOptions options) throws BWFLAException {
		EmulatorUtils.createCowFile(cowPath, options, log);
	}

	public static void createCowFile(Path cowPath, QcowOptions options, Logger log) throws BWFLAException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("qemu-img");
		process.addArguments("create", "-f", "qcow2");
		if(options != null && options.getBackingFile() != null)
		{
			process.addArgument("-o", "backing_file=", options.getBackingFile());
		}
		process.addArgument(cowPath.toString());
		if(options != null && options.getSize() != null) {
			process.addArgument(options.getSize());
		}

		if(options.getProxyUrl() != null) {
			log.severe("using proxy " +  options.getProxyUrl());
			// process.addEnvVariable("no_proxy", "localhost,127.0.0.1,.internal");
			// process.addEnvVariable("http_proxy", options.getProxyUrl());
			process.addEnvVariable("LD_PRELOAD", curlProxySo);
			process.addEnvVariable("prefix_proxy", options.getProxyUrl());
		}

		if (!process.execute()) {
			try {
				Files.deleteIfExists(cowPath);
			} catch (Exception e) {
				log.severe("Created a temporary file but cannot delete it after error. This is bad.");
			}
			throw new BWFLAException("Could not create local COW file. See log output for more information (maybe).");
		}
	}

	public static Path mountCowFile(Path image, Path mountpoint, Logger log)
			throws IllegalArgumentException, IOException, BWFLAException {
		return mountCowFile(image, mountpoint, null, log);
	}

	/**
	 * Mounts a QEMU qcow file (or any QEMU image file) to the specified
	 * mountpoint. The mountpoint is created if it does not exist.
	 *
	 * @param image      Path to the image file to be mounted.
	 * @param mountpoint Path where the qcow2 file will be mounted to.
	 * @return Path to the image within the mountpoint
	 * @throws BWFLAException           if the mounting fails (see cause for further info)
	 * @throws IllegalArgumentException if the image cannot be used as a mount source
	 * @throws IOException              If readonly is false and the image cannot be mounted read/write
	 */
	public static Path mountCowFile(Path image, Path mountpoint, MountOptions mountOpts) throws IllegalArgumentException,
			IOException, BWFLAException {
		return EmulatorUtils.mountCowFile(image, mountpoint, mountOpts, log);
	}

	public static Path mountCowFile(Path image, Path mountpoint, MountOptions mountOpts, Logger log)
			throws IllegalArgumentException, BWFLAException {

		if (image == null) {
			throw new IllegalArgumentException("Given image path was null");
		}

		if (!Files.isRegularFile(image)) {
			throw new IllegalArgumentException("Given image path \"" + image
					+ "\" is not a regular file.");
		}

		return nbdMount(image.toAbsolutePath().toString(), mountpoint, mountOpts, log);
	}

	public static Path nbdMount(String imagePath, Path mountpoint, MountOptions options, Logger log) throws BWFLAException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner("/libexec/fuseqemu/mount-qcow");
		process.addArgument(imagePath);
		process.addArgument(mountpoint.toAbsolutePath().toString());
		process.addArguments(options.getArgs());
		process.addArgument("--");
		process.addArguments("-o", "allow_root");

		if (!process.execute()) {
			throw new BWFLAException("Error mounting " + imagePath
					+ ". See log output for more information (maybe).");
		}
		return mountpoint;
	}

	public static void lklMount(Path path, Path dest, String fsType) throws BWFLAException {
		EmulatorUtils.lklMount(path, dest, fsType, log);
	}

	public static void lklMount(Path path, Path dest, String fsType, Logger log) throws BWFLAException {
		lklMount(path, dest, fsType, log, true);
	}

	public static void lklMount(Path path, Path dest, String fsType, Logger log, boolean isReadOnly) throws BWFLAException {

		if(fsType != null && fsType.equalsIgnoreCase("fat32"))
			fsType = "vfat";

		if (path == null)
			throw new BWFLAException("mount failed: path = null");

		if (!Files.exists(dest)) {
			try {
				Files.createDirectories(dest);
			}
			catch (Exception error) {
				throw new BWFLAException("Creating mountpoint failed!", error);
			}
		}

		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);

		process.setCommand("lklfuse");
		if (fsType != null) {
			process.addArgument("-o");
			if (isReadOnly)
				process.addArgValue("type=" + fsType + ",ro");
			else
				process.addArgValue("type=" + fsType);
		}
		process.addArguments("-o", "allow_root");
		process.addArguments("-o", "use_ino");

		process.addArguments("-o", 
				"uid=" + ConfigurationProvider.getConfiguration().get("components.emulator_containers.uid"));


		process.addArgument(path.toString());
		process.addArgument(dest.toString());

		if (!process.execute()) {
			throw new BWFLAException("mount failed");
		}
	}

	public static Path lklMount(Path path, String fsType) throws BWFLAException {
		return EmulatorUtils.lklMount(path, fsType, log);
	}

	public static Path lklMount(Path path, String fsType, Logger log) throws BWFLAException {
		File tempDir = null;
		try {
			tempDir = Files.createTempDirectory("mount-").toFile();
			EmulatorUtils.lklMount(path, tempDir.toPath(), fsType, log);
			return tempDir.toPath();
		} catch (Exception e) {
			if (tempDir != null)
				try {
					FileUtils.deleteDirectory(tempDir);
				} catch (IOException e1) {
					// don't care
				}
			throw new BWFLAException(e);
		}
	}

	public static void unmount(Path tempDir) throws BWFLAException, IOException {
		unmount(tempDir, log);
	}

	public static void unmount(Path tempDir, Logger log) throws BWFLAException, IOException {
		unmountFuse(tempDir, log);
	}

	public static void unmountFuse(Path tempDir) throws BWFLAException, IOException {
		EmulatorUtils.unmountFuse(tempDir, log);
	}

	public static void ntfsMount(Path src, Path dst, String options, Logger log) throws BWFLAException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner("sudo")
				.addArgument("--non-interactive")
				.addArgument("ntfs-3g")
				.setLogger(log);

		if (options != null)
			process.addArgument(options);

		process.addArgument(src.toString());
		process.addArgument(dst.toString());
		process.redirectStdErrToStdOut(false);
		if (!process.execute()) {
			throw new BWFLAException("Mounting NTFS-filesystem failed!");
		}
	}

	public static void sysMount(Path src, Path dst, String fs, String options, Logger log) throws BWFLAException, IOException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("mount");
		if (fs != null)
			process.addArguments("-t", fs);
		if (options != null)
			process.addArgument(options);

		process.addArgument(src.toString());
		process.addArgument(dst.toString());
		process.redirectStdErrToStdOut(false);
		if (!process.execute()) {
			throw new BWFLAException(process.getCommandString() + " failed: " + process.getStdErrString());
		}
	}

	public static void changeBackingFile(Path image, String backingFile, Logger log) throws BWFLAException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("qemu-img");
		process.addArgument("rebase");
		process.addArgument("-u");
		process.addArguments("-b", backingFile);
		process.addArgument(image.toString());

		if (!process.execute()) {
			throw new BWFLAException("qemu-img rebase " + image.toString() + " failed");
		}
	}

	public static void convertImage(Path inFile, Path outFile, ImageInformation.QemuImageFormat fmt, Logger log) throws BWFLAException {
		// NOTE: our patched qemu-img is relatively old and seems to silently produce
		//       images with incorrect size when applied to some newer VHD files!
		//       As a workaround, use upstream qemu-img just for converting.

		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("/eaas/workarounds/qemu-utils/usr/bin/qemu-img");
		process.addArguments("convert");
		process.addArguments("-O", fmt.toString());
		process.addArgument(inFile.toString());
		process.addArgument(outFile.toString());

		if (!process.execute()) {
			throw new BWFLAException("converting " + inFile.toString() + " failed");
		}
	}

	public static boolean isMountpoint(Path mountpoint, Logger log)
	{
		DeprecatedProcessRunner process = new DeprecatedProcessRunner("mountpoint");
		process.setLogger(log);
		process.addArgument(mountpoint.toAbsolutePath().toString());
		return process.execute();
	}

	private static void _umountNbd(String cowFile, Logger log)
	{
		DeprecatedProcessRunner process = new DeprecatedProcessRunner("/libexec/fuseqemu/umount-qcow-lazy");
		process.setLogger(log);
		process.addArgument(cowFile);
		if(!process.execute())
			log.severe("failed to exec unmount");

		String lockFile = cowFile + ".lock";
		Path lockFilePath = Paths.get(lockFile);
		log.severe("checking if " + lockFile + "is still present");
		for (int i = 0; i < 20; ++i) {
			if(!Files.exists(lockFilePath))
				return;

			try {
				log.severe("waiting for " + lockFile + " to disappear");
				Thread.sleep(1000L);
			}
			catch (Exception error) {
				// Ignore it!
			}
		}
		log.severe("failed to unmount. Lockfile " + lockFile + "still exists");
	}

	@Deprecated
	private static void _unmount(Path mntpoint, Logger log) throws BWFLAException, IOException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);

		// NOTE: Current versions of xmount and lklfuse require
		//       special unmount treatment to prevent data loss!

		// Try multiple times, until unmount is successfull
		{
			boolean failed = true;
			for (int i = 0; i < 20; ++i) {
				// Since we may have mounted with sudo,
				// unmount with sudo too (see ntfs-3g)!
				process.setCommand("sudo");
				process.addArgument("--non-interactive");
				process.addArgument("fusermount");
				process.addArguments("-u");
				process.addArgument(mntpoint.toString());
				if (process.execute()) {
					failed = false;
					break;
				}

				try {
					Thread.sleep(500L);
				}
				catch (Exception error) {
					// Ignore it!
				}
			}

			if (failed)
				throw new BWFLAException("Unmounting " + mntpoint.toString() + " failed!");
		}

		// Wait until xmount- or lklfuse-daemons are actually terminated
		{
			final String check = "ps -o command -C xmount,lklfuse | grep -q " + mntpoint.toString() + "$";

			boolean failed = true;
			for (int i = 0; i < 20; ++i) {
				process.setCommand("/bin/sh");
				process.addArguments("-c", check);
				if (!process.execute()) {
					// Daemon not running!
					failed = false;
					break;
				}

				try {
					Thread.sleep(1000L);
				}
				catch (Exception error) {
					// Ignore it!
				}
			}

			if (failed)
				throw new BWFLAException("Stopping fuse-daemon for " + mntpoint.toString() + " failed!");
		}

		FileUtils.deleteDirectory(mntpoint.toFile());
	}

	public static void unmountFuse(Path mntpoint, Logger log) throws BWFLAException, IOException {

		log.severe("unmounting " + mntpoint);
		if (mntpoint == null)
			return;

		String imagePathString = mntpoint.toAbsolutePath().toString() + ".lock";
		Path nbdMountPath = Paths.get(imagePathString);
		log.severe(imagePathString + " " + Files.exists(nbdMountPath));
		if(Files.exists(nbdMountPath))
		{
			log.severe("using nbd unmount");
			String cowPathString = imagePathString.replace(".lock", "");
			_umountNbd(cowPathString, log);
		}
		else { // legacy unmount
			log.severe("checking mountpoint... " + mntpoint);
			if (!isMountpoint(mntpoint, log)) {
				log.severe(mntpoint + " is not a mountpoint. abort");
				return;
			}
			_unmount(mntpoint, log);
		}
	}

	public static void checkAndUnmount(Path... paths) throws BWFLAException, IOException {
		for (Path path : paths) {
			if (path == null || !Files.exists(path))
				continue;

			try {
				EmulatorUtils.unmountFuse(path);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Unmounting " + path.toString() + " failed!", error);
			}
		}
	}

	public static void mountFileSystem(Path device, Path dest, FileSystemType fsType)
			throws BWFLAException, IOException
	{
		EmulatorUtils.mountFileSystem(device, dest, fsType, log);
	}

	public static void mountFileSystem(Path device, Path dest, FileSystemType fsType, Logger log)
			throws BWFLAException, IOException
	{
		if (!Files.exists(dest)) {
			log.info("Directory '" + dest + "' does not exist. Creating it...");
			Files.createDirectories(dest);
		}

		switch (fsType) {
			case NTFS:
				EmulatorUtils.ntfsMount(device, dest, null, log);
				break;

			default:
				EmulatorUtils.lklMount(device, dest, fsType.toString().toLowerCase(), log, false);
				break;
		}
	}
}
