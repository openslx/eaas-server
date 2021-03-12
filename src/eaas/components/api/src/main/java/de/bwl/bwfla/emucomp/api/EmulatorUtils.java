package de.bwl.bwfla.emucomp.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.common.utils.ImageInformation;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;


public class EmulatorUtils {
	protected static final Logger log = Logger.getLogger("EmulatorUtils");

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

	public static void copyRemoteUrl(Binding resource, Path dest) throws BWFLAException {
		EmulatorUtils.copyRemoteUrl(resource, dest, log);
	}

	public static void copyRemoteUrl(Binding resource, Path dest, Logger log) throws BWFLAException {
		String resUrl = resource.getUrl();
		// hack until qemu-img is fixed
		if (resUrl.startsWith("http") || resUrl.startsWith("https")) {
			DeprecatedProcessRunner process = new DeprecatedProcessRunner("curl");
			if(log != null)
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
		else if(resUrl.startsWith("file:")) // shortcut to copy the file
		{
			// handle legacy 'file:/some/file' and new 'file:///some/file' URLs
			final var prefix = (resUrl.startsWith("file://")) ? "file://" : "file:";
			final var source = Path.of(resUrl.substring(prefix.length()));
			try {
				Files.copy(source, dest);
			} catch (IOException e) {
				log.log(Level.SEVERE, "Copying local file failed!", e);
				throw new BWFLAException(e);
			}
		}
		else {
			throw new BWFLAException(
					"Cannot create local copy of the binding's data, unsupported url schema: " + resUrl);
		}
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

		// we need to check if the subpath is there
		Path parent = cowPath.getParent();
		log.severe("checking if " + parent.toString() + " path exists.");
		if(!Files.exists(parent)) {
			try {
				Files.createDirectories(parent);
			} catch (IOException e) {
				throw new BWFLAException(e);
			}
		}

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

		if (!process.execute()) {
			try {
				Files.deleteIfExists(cowPath);
			} catch (Exception e) {
				log.severe("Created a temporary file but cannot delete it after error. This is bad.");
			}
			throw new BWFLAException("Could not create local COW file. See log output for more information (maybe).");
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
		EmulatorUtils.convertImage(inFile.toString(), outFile, fmt, log);
	}

	public static void convertImage(String source, Path target, ImageInformation.QemuImageFormat fmt, Logger log)
			throws BWFLAException
	{
		// NOTE: our patched qemu-img is relatively old and seems to silently produce
		//       images with incorrect size when applied to some newer VHD files!
		//       As a workaround, use upstream qemu-img just for converting.

		DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setLogger(log);
		process.setCommand("/eaas/workarounds/qemu-utils/usr/bin/qemu-img");
		process.addArguments("convert");
		process.addArguments("-O", fmt.toString());
		process.addArgument(source);
		process.addArgument(target.toString());
		if (!process.execute())
			throw new BWFLAException("Converting '" + source + "' failed!");
	}
}
