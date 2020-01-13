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

	public static String computeMd5(File file) {
		String result = new String();

		try {
			OutputStream out = new NullOutputStream();
			MessageDigest md = MessageDigest.getInstance("MD5");
			InputStream is = new DigestInputStream(new FileInputStream(file), md);
			IOUtils.copy(is, out);
			out.flush();
			out.close();
			is.close();
			byte[] digest = md.digest();
			for (int i = 0; i < digest.length; i++) {
				result += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			log.warning(e.getMessage());
		}

		return result;
	}

	public static String connectBinding(Binding resource, Path resourceDir,
										XmountOptions xmountOpts)
			throws BWFLAException, IllegalArgumentException {
		String resUrl = resource.getUrl();

		// TODO handle.net resolution according to used transport

		if (resource == null || resource.getId() == null
				|| resource.getId().isEmpty()) {
			throw new IllegalArgumentException(
					"Given resource is null or has invalid id.");
		}

		if (resource.getAccess() == null)
			resource.setAccess(Binding.AccessType.COW);

		if (resource.getId() == null || resUrl == null
				|| resource.getId().isEmpty() || resUrl.isEmpty()) {
			throw new IllegalArgumentException(
					"Given id is null or has invalid id.");
		}

		String resFile = null;
		switch (resource.getAccess()) {
			case COW:
				// create cow container
				// Qemu's block layer driver handles many transport protocols,
				// as long as we don't want to support yet another one, we can
				// safely ignore transports here and just pass the url to Qemu.
				Path cowPath = resourceDir.resolve(resource.getId() + ".cow");

				QcowOptions qcowOptions = new QcowOptions();
				qcowOptions.setBackingFile(resUrl);

				if(MachineTokenProvider.getAuthenticationProxy() != null)
					qcowOptions.setProxyUrl(MachineTokenProvider.getAuthenticationProxy());
				else
					qcowOptions.setProxyUrl(MachineTokenProvider.getProxy());

				EmulatorUtils.createCowFile(cowPath, qcowOptions);

				Path fuseMountpoint = cowPath
						.resolveSibling(cowPath.getFileName() + ".fuse");
				try {
					resFile = mountCowFile(cowPath, fuseMountpoint, xmountOpts)
							.toString();
				} catch (IOException e) {
					throw new BWFLAException("Could not fuse-mount image file.", e);
				}
				break;
			case COPY:
				// use qemu-imgs convert feature to create a new local raw copy
				Path imgCopy = resourceDir.resolve(resource.getId() + ".copy");
				copyRemoteUrl(resource, imgCopy, xmountOpts);
				resFile = imgCopy.toString();
				break;
			default:
				log.severe("This should never happen!");
		}

		return resFile;
	}

	public static void copyRemoteUrl(Binding resource, Path dest, XmountOptions xmountOpts) throws BWFLAException {
		EmulatorUtils.copyRemoteUrl(resource, dest, xmountOpts, log);
	}

	public static void copyRemoteUrl(Binding resource, Path dest, XmountOptions xmountOpts, Logger log) throws BWFLAException {
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
		else {
			DeprecatedProcessRunner process = new DeprecatedProcessRunner("qemu-img");
			process.setLogger(log);
			process.addArgument("convert");
			process.addArgument("-fraw");
			if (xmountOpts != null)
				process.addArgument("-O" + xmountOpts.getOutFmt().toString().toLowerCase());
			process.addArgument(resUrl);
			process.addArgument(dest.toString());
			if (!process.execute()) {
				throw new BWFLAException(
						"Cannot create local copy of the binding's data, connecting binding cancelled.");
			}
		}
	}

	public static Path prepareSoftwareCollection(String handle, Path tempPath) {
		try {

			Path image = handleToPath(handle, tempPath);

			Path mountpoint = java.nio.file.Files.createTempDirectory(tempPath, "fuse-");
			DeprecatedProcessRunner process = new DeprecatedProcessRunner("fuseiso");
			process.addArgument(image.toString());
			process.addArgument(mountpoint.toString());
			if (!process.execute())
				return null;

			return mountpoint;

		} catch (Exception e) {
			log.severe("Temporary mountpoint cannot be created: " + e.getMessage());
		}
		return null;
	}

	private static Path handleToPath(String handle, Path tempPath) throws Exception {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner();

		// creating local cow image file
		Path cowFile = java.nio.file.Files.createTempFile(tempPath, "cow-", ".qcow2");
		process.setCommand("qemu-img");
		process.addArguments("create", "-f", "qcow2", "-o");
		process.addArgument("backing_file=", handle, ",backing_fmt=raw");
		process.addArgument(cowFile.toString());
		process.execute();

		// loop-mounting cow image to produce raw block stream
		// representation
		Path mountpoint = Files.createTempDirectory(tempPath, "fuse-");
		process.setCommand("qemu-fuse");
		process.addArguments("-o", "kernel_cache",
				"-o", "noforget",
				"-o", "large_read",
				"-o", "max_readahead=131072",
				"-o", "allow_root");
		process.addArgument(cowFile.toString());
		process.addArgument(mountpoint.toString());
		process.execute();

		return mountpoint.resolve(cowFile.getFileName());
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

	public static Path mountCowFile(Path image, Path mountpoint)
			throws IllegalArgumentException, IOException, BWFLAException {
		return mountCowFile(image, mountpoint, null, log);
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
	public static Path mountCowFile(Path image, Path mountpoint, XmountOptions xmountOpts) throws IllegalArgumentException,
			IOException, BWFLAException {
		return EmulatorUtils.mountCowFile(image, mountpoint, xmountOpts, log);
	}

	public static Path mountCowFile(Path image, Path mountpoint, XmountOptions xmountOpts, Logger log)
			throws IllegalArgumentException, IOException, BWFLAException {
		if (image == null) {
			throw new IllegalArgumentException("Given image path was null");
		}
		if (!Files.isRegularFile(image)) {
			throw new IllegalArgumentException("Given image path \"" + image
					+ "\" is not a regular file.");
		}
		if (xmountOpts != null && !xmountOpts.isReadonly() && !Files.isWritable(image)) {
			throw new IOException("Given image path \"" + image
					+ "\" is not writable but rw access was requested.");
		}

		return xmount(image.toAbsolutePath().toString(),
				mountpoint, xmountOpts, log);
	}

	public static Path xmount(String imagePath, Path mountpoint,
							  XmountOptions xmountOpts)
			throws IllegalArgumentException, IOException, BWFLAException {
		return EmulatorUtils.xmount(imagePath, mountpoint, xmountOpts, log);
	}

	public static Path xmount(String imagePath, Path mountpoint, XmountOptions xmountOpts, Logger log)
			throws IllegalArgumentException, IOException, BWFLAException {
		if (xmountOpts == null)
			xmountOpts = new XmountOptions();

		// This mimicks the behavior of xmount which looks for the
		// last slash in the string and takes everything up until
		// the last dot in the string as the base name for the image
		String baseName = imagePath.substring(imagePath.lastIndexOf('/') + 1);
		if (baseName.lastIndexOf('.') > 0)
			baseName = baseName.substring(0, baseName.lastIndexOf('.'));

		try {
			// create mountpoint if necessary
			Files.createDirectories(mountpoint);
			log.info("created subdirectories up to " + mountpoint.toString());

			DeprecatedProcessRunner process = new DeprecatedProcessRunner("xmount");
			process.setLogger(log);
			// process.addArgument("-d");
			process.addArguments("--in" , xmountOpts.getInFmt().toString(),
					imagePath);

			xmountOpts.setXmountOptions(process);
			process.addArguments(mountpoint.toAbsolutePath().toString());

			if (!process.execute()) {
			// if (!process.start()) {
				try {
					Files.deleteIfExists(mountpoint);
				} catch (IOException e) {
					log.severe(
							"Created a temporary file but cannot delete it after error. This is bad.");
				}
				throw new BWFLAException("Error mounting " + imagePath
						+ ". See log output for more information (maybe).");
			}

//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			switch (xmountOpts.getOutFmt()) {
				case RAW:
					return mountpoint.resolve(baseName + ".dd");
				case VDI:
				case VHD:
				case VMDK:
					return mountpoint.resolve(baseName + "."
							+ xmountOpts.getOutFmt().toString().toLowerCase());
				default:
					return null;
			}
		} catch (IOException e) {
			throw new BWFLAException(
					"Error mounting " + imagePath + ".", e);
		}
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
		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("ntfs-3g");

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

	@Deprecated
	public static ImageInformation.QemuImageFormat getImageFormat(Path inFile, Logger log) throws BWFLAException, IOException {
		ImageInformation info = new ImageInformation(inFile.toString(), log);
		return info.getFileFormat();

//		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
//		process.setLogger(log);
//		process.setCommand("qemu-img");
//		process.addArguments("info");
//		process.addArgument(inFile.toString());
//
//		try {
//			if (!process.execute(false, false)) {
//				throw new BWFLAException("qemu-img info " + inFile.toString() + " failed");
//			}
//
//			String output = process.getStdOutString();
//			for (ImageInformation.QemuImageFormat fmt : ImageInformation.QemuImageFormat.values()) {
//				if (output.contains("file format: " + fmt.toString()))
//					return fmt;
//			}
//			return null;
//		} finally {
//			process.cleanup();
//		}
	}



	public static void convertImage(Path inFile, Path outFile, ImageInformation.QemuImageFormat fmt, Logger log) throws BWFLAException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("qemu-img");
		process.addArguments("convert");
		process.addArguments("-O", fmt.toString());
		process.addArgument(inFile.toString());
		process.addArgument(outFile.toString());

		if (!process.execute()) {
			throw new BWFLAException("converting " + inFile.toString() + " failed");
		}
	}

	public static String getLoopDev(Logger log) throws BWFLAException, IOException {
		return LoopDeviceManager.getLoopDevice();
	}

	public void  createNewImg(java.nio.file.Path destFile, String bs, long sizeMb) throws BWFLAException {
		DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
		runner.setCommand("dd");
		runner.addArgument("if=/dev/zero");
		runner.addArgument("of=" + destFile);
		runner.addArgument("bs=" + bs);
		runner.addArgument("count=" + sizeMb);
		if (!runner.execute())
			throw new BWFLAException("dd file creation failed!");
	}

	public static void connectLoop(String dev, File img, long offset, long sizelimit, Logger log) throws BWFLAException, IOException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("losetup");
		process.addArguments("-o", offset + "");
		process.addArguments("--sizelimit", sizelimit + "");
		process.addArgument(dev);
		process.addArgument(img.getAbsolutePath());
		process.redirectStdErrToStdOut(false);
		try {
			if (!process.execute(false, false)) {
				throw new BWFLAException(process.getCommandString() + " failed: " + process.getStdErrString());
			}
		} finally {
			process.cleanup();
		}
	}

	public static void connectLoop(String dev, File img, Logger log) throws BWFLAException, IOException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("/sbin/losetup");
		process.addArgument(dev);
		process.addArgument(img.getAbsolutePath());
		process.redirectStdErrToStdOut(false);
		try {
			if (!process.execute(true, false)) {
				throw new BWFLAException(process.getCommandString() + " failed: " + process.getStdErrString());
			}
		} finally {
			process.cleanup();
		}
	}

	public static void detachLoop(String dev) throws BWFLAException, IOException {
		LoopDeviceManager.detachLoop(dev);
	}

	public static void unmountFuse(Path mntpoint, Logger log) throws BWFLAException, IOException {
		if (mntpoint == null)
			return;

		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);

		// NOTE: Current versions of xmount and lklfuse require
		//       special unmount treatment to prevent data loss!

		// Try multiple times, until unmount is successfull
		{
			boolean failed = true;
			for (int i = 0; i < 20; ++i) {
				process.setCommand("fusermount");
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

	public static boolean padFile(File f, int blocksize) {
		if (!f.exists())
			return false;

		long fileSize = f.length();
		if (fileSize == 0 || fileSize % blocksize == 0)
			return false;

		int padding = (int) (blocksize - (fileSize % blocksize)) % blocksize;
		log.info("Warn: padding file: " + f.getName());

		byte[] bytes = new byte[padding];
		Arrays.fill(bytes, (byte) 0);
		try {
			FileOutputStream output = new FileOutputStream(f, true);
			output.write(bytes);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}

		return true;
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

	public static void stopXpraServer(DeprecatedProcessRunner runner) throws BWFLAException {
		final int xpraProcessId = runner.getProcessId();
		log.info("Stopping Xpra server " + xpraProcessId + "...");

		// We need to send INT signal to gracefully stop Xpra server
		DeprecatedProcessRunner xpraKiller = new DeprecatedProcessRunner();
		xpraKiller.setCommand("kill");
		xpraKiller.addArgument("-SIGINT");
		xpraKiller.addArgument("" + xpraProcessId);
		xpraKiller.execute();

		try {
			// Give Xpra server a chance to shutdown cleanly
			for (int i = 0; i < 10; ++i) {
				if (runner.isProcessFinished()) {
					log.info("Xpra server " + xpraProcessId + " stopped.");
					return;
				}
				Thread.sleep(500);
			}
		}
		catch (Exception exception) {
			throw new BWFLAException(exception.getMessage());
		}

		log.warning("Xpra server " + xpraProcessId + " failed to shutdown cleanly! Zomby processes may be left.");
	}

	public static void makeFs(String device, String type, Integer partitionNumber ) {
		DeprecatedProcessRunner runner = new DeprecatedProcessRunner();

		runner.setCommand("mkfs" + "." + type);
		if(type.equals("ntfs") && partitionNumber == -1)
			runner.addArgument("-F");
		if (partitionNumber > 0)
			runner.addArguments(device + "p" + partitionNumber);
		else
			runner.addArguments(device);
		runner.execute();
	}
	public static void makeFs(String loopDev, String type ) {
		makeFs(loopDev, type, -1);
	}

	public static void cleanUpMount(File tempMountDir, String loopDev, Logger log) throws BWFLAException, IOException {
		if (tempMountDir != null)
			if (tempMountDir.exists())
				EmulatorUtils.unmountFuse(tempMountDir.toPath());

		if (loopDev != null) {
			detachLoop(loopDev);
		}
	}

	public static Path createWorkingDir(String basedir) throws BWFLAException
	{
		try {
			return EmulatorUtils.createWorkingDir(Paths.get(basedir));
		}
		catch (Exception error) {
			throw new BWFLAException("Creating working directory failed!\n", error);
		}
	}
	public static Path createWorkingDir(Path basedir) throws BWFLAException
	{
		try {
			return EaasFileUtils.createTempDirectory(basedir, "build-");
		}
		catch (Exception error) {
			throw new BWFLAException("Creating working directory failed!\n", error);
		}
	}

    public static void tarDirectory(Path directoryToTar, Path outputTar) throws BWFLAException {
        DeprecatedProcessRunner tarRunner = new DeprecatedProcessRunner();
        tarRunner.setCommand("tar");
        tarRunner.addArgument("-C");
        tarRunner.addArgument(directoryToTar.toAbsolutePath().toString());
        tarRunner.addArgument("-zcvf");
        tarRunner.addArgument(outputTar.toAbsolutePath().toString());
        tarRunner.addArgument(".");
        if(!tarRunner.execute())
        	throw new BWFLAException("Tar failed!");
    }
}
