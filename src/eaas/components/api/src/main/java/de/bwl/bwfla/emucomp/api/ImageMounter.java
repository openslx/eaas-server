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

package de.bwl.bwfla.emucomp.api;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import org.apache.tamaya.ConfigurationProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/** A helper class for managing image-mounts. */
public class ImageMounter implements AutoCloseable
{
	private final Logger log;
	private final Map<Path, Mount> mounts;
	private final Set<Path> workdirs;

	public static final long OFFSET_MIN_BOUND = 0L;
	public static final long SIZE_MIN_BOUND   = -1L;

	public ImageMounter(Logger log)
	{
		this.log = log;
		this.mounts = new LinkedHashMap<>();
		this.workdirs = new LinkedHashSet<>();
	}

	/** Register a working directory for automatic cleanup. */
	public ImageMounter addWorkingDirectory(Path path)
	{
		workdirs.add(path);
		return this;
	}

	/** Look up mounted image by path. */
	public Mount lookup(Path image)
	{
		return mounts.get(image);
	}

	/** Mount image at mountpoint. */
	public Mount mount(Path image, Path mountpoint) throws BWFLAException, IllegalArgumentException
	{
		return this.mount(image, mountpoint, new XmountOptions());
	}

	/** Mount image, beginning at offset, at specified mountpoint. */
	public Mount mount(Path image, Path mountpoint, long offset) throws BWFLAException, IllegalArgumentException
	{
		return this.mount(image, mountpoint, offset, SIZE_MIN_BOUND);
	}

	/** Mount image of given size, beginning at offset, at specified mountpoint. */
	public Mount mount(Path image, Path mountpoint, long offset, long size)
			throws BWFLAException, IllegalArgumentException
	{
		this.check(offset, OFFSET_MIN_BOUND, "offset");
		this.check(size, SIZE_MIN_BOUND, "size");

		final XmountOptions options = new XmountOptions();
		options.setOffset(offset);
		options.setSize(size);

		return this.mount(image, mountpoint, options);
	}

	/** Mount image at mountpoint with options. */
	public Mount mount(Path image, Path mountpoint, XmountOptions options)
			throws BWFLAException, IllegalArgumentException
	{
		this.check(image);

		final Path target = EmulatorUtils.xmount(image.toString(), mountpoint, options, log);
		final Mount mount = new Mount(image, target, mountpoint);
		this.register(mount);
		return mount;
	}

	/** Mount image's filesystem at mountpoint. */
	public Mount mount(Path image, Path mountpoint, FileSystemType fstype)
			throws BWFLAException, IllegalArgumentException
	{
		this.check(image);

		try {
			EmulatorUtils.mountFileSystem(image, mountpoint, fstype, log);
		}
		catch (IOException error) {
			throw new BWFLAException(error);
		}

		final Mount mount = new Mount(image, mountpoint);
		this.register(mount);
		return mount;
	}

	/** Mount image's filesystem at mountpoint. */
	public Mount mount(Mount mount, Path mountpoint, FileSystemType fstype)
			throws BWFLAException, IllegalArgumentException
	{
		return this.mount(mount.getTargetImage(), mountpoint, fstype);
	}

	/**
	 * Re-mount an already mounted image, beginning at offset.
	 * The previous mount will be unmounted and invalidated!
	 */
	public Mount remount(Mount mount, long offset) throws BWFLAException, IllegalArgumentException
	{
		return this.remount(mount, offset, SIZE_MIN_BOUND);
	}

	/**
	 * Re-mount an already mounted image, beginning at offset and with new size.
	 * The previous mount will be unmounted and invalidated!
	 */
	public Mount remount(Mount mount, long offset, long size) throws BWFLAException, IllegalArgumentException
	{
		this.check(mount);

		final Path image = mount.getSourceImage();
		final Path mountpoint = mount.getMountPoint();
		if (!this.unmount(mount, false))
			throw new BWFLAException("Unmounting image failed!");

		return this.mount(image, mountpoint, offset, size);
	}

	/** Unmount a mounted image. */
	public boolean unmount(Mount mount) throws IllegalArgumentException
	{
		return this.unmount(mount, true);
	}

	/** Unmount a mounted image, optionally removing its mountpoint. */
	public boolean unmount(Mount mount, boolean rmdirs) throws IllegalArgumentException
	{
		this.check(mount);

		final boolean unmounted = this.unmount(mount.getMountPoint(), rmdirs);
		if (unmounted)
			this.unregister(mount);

		return unmounted;
	}

	/** Unmount all registered images. */
	public boolean unmount() throws IllegalArgumentException
	{
		// Try to unmount in LIFO order, dependent mounts could exist!
		// See underlying LinkedHashMap's javadocs for more details.

		final Mount[] mountpoints = mounts.values()
				.toArray(new Mount[0]);

		boolean result = true;
		for (int i = mountpoints.length - 1; i >= 0; --i)
			result &= this.unmount(mountpoints[i]);

		return result;
	}

	/** Try to remove all registered workdirs. */
	public boolean clean()
	{
		workdirs.removeIf((wd) -> ImageMounter.delete(wd, log));
		return workdirs.isEmpty();
	}

	/**
	 * Unmount all registered images. Helper for use in try() statements.
	 * @see #unmount()
	 * @see #clean()
	 */
	@Override
	public void close() throws BWFLAException
	{
		try {
			if (!this.unmount())
				throw new BWFLAException("Unmounting failed!");
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Error(s) occured while unmounting image(s)!", error);
			throw error;
		}
		finally {
			if (!this.clean())
				log.warning("Error(s) occured during cleanup!");
		}
	}


	/** A simple handle for a mounted image. */
	public static class Mount
	{
		private ImageMounter mounter;
		private Path mountpoint;
		private Path source;
		private Path target;

		private Mount(Path image, Path mountpoint)
		{
			this(image, image, mountpoint);
		}

		private Mount(Path source, Path target, Path mountpoint)
		{
			this.mountpoint = mountpoint;
			this.source = source;
			this.target = target;
		}

		/** Get image's source path. */
		public Path getSourceImage()
		{
			return source;
		}

		/**
		 * Get image's target path, which can point to the source-image or
		 * its logical raw-representation's subpath, produced by xmount.
		 */
		public Path getTargetImage()
		{
			return target;
		}

		/** Get image's mountpoint. */
		public Path getMountPoint()
		{
			return mountpoint;
		}

		/** Get image's owning mounter. */
		public ImageMounter mounter()
		{
			return mounter;
		}

		/**
		 * Re-mount the mounted image, beginning at offset.
		 * @see ImageMounter#remount(Mount, long)
		 */
		public Mount remount(long offset) throws BWFLAException, IllegalArgumentException
		{
			return mounter.remount(this, offset);
		}

		/**
		 * Unmount this image, also removing its mountpoint.
		 * @see ImageMounter#unmount(Mount)
		 */
		public boolean unmount() throws IllegalArgumentException
		{
			return this.unmount(true);
		}

		/**
		 * Unmount this image, optionally removing its mountpoint.
		 * @see ImageMounter#unmount(Mount, boolean)
		 */
		public boolean unmount(boolean rmdirs) throws IllegalArgumentException
		{
			return mounter.unmount(this, rmdirs);
		}

		private void register(ImageMounter mounter)
		{
			this.mounter = mounter;
		}

		private void invalidate()
		{
			this.mounter = null;
			this.mountpoint = null;
			this.source = null;
			this.target = null;
		}

		private Path id()
		{
			return source;
		}
	}


	// ===== Public Helpers ==============================

	public static Path createWorkingDirectory() throws BWFLAException
	{
		final String basedir = ConfigurationProvider.getConfiguration()
				.get("imagemounter.basedir");

		return ImageMounter.createWorkingDirectory(Path.of(basedir));
	}

	public static Path createWorkingDirectory(Path basedir) throws BWFLAException
	{
		try {
			return EaasFileUtils.createTempDirectory(basedir, "");
		}
		catch (Exception error) {
			throw new BWFLAException("Creating working directory failed!", error);
		}
	}

	public static boolean sync(Path path, Logger log)
	{
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner("sync")
				.setLogger(log);

		final boolean synced = process.execute();
		if (!synced)
			log.warning("Syncing filesystem for '" + path.toString() + "' failed!");

		return synced;
	}

	public static boolean mount(Path device, Path mountpoint, FileSystemType fstype, Logger log)
	{
		try {
			EmulatorUtils.mountFileSystem(device, mountpoint, fstype, log);
			return true;
		}
		catch (Exception error) {
			log.warning("Mounting " + fstype.name() + " filesystem from '" + device.toString() + "' failed!");
			return false;
		}
	}

	public static boolean unmount(Path path, Logger log)
	{
		try {
			EmulatorUtils.unmount(path, log);
			return true;
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Unmounting '" + path.toString() + "' failed!\n", error);
			return false;
		}
	}

	public static boolean delete(Path path, Logger log)
	{
		// Delete file or a directory recursively
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner("sudo")
				.addArgument("--non-interactive")
				.addArguments("rm", "-r", "-f")
				.addArgument(path.toString())
				.setLogger(log);

		final boolean deleted = process.execute();
		if (!deleted)
			log.warning("Deleting '" + path.toString() + "' recursively failed!");

		return deleted;
	}


	// ===== Internal Helpers ==============================

	private void check(Path image) throws BWFLAException
	{
		final Mount mount = mounts.get(image);
		if (mount == null)
			return;

		final String message = "Image '" + image.toString() + "' seems to be already mounted"
				+ " at '" + mount.getMountPoint().toString() + "'!";

		throw new BWFLAException(message);
	}

	private void check(Mount mount) throws IllegalArgumentException
	{
		if (mount.mounter() != this)
			throw new IllegalArgumentException("Mount's owning mounter does not match!");
	}

	private void check(long value, long minbound, String what) throws IllegalArgumentException
	{
		if (value < minbound)
			throw new IllegalArgumentException("Invalid image " + what + ": " + value + " < " + minbound);
	}

	private void register(Mount mount)
	{
		mounts.put(mount.id(), mount);
		mount.register(this);
	}

	private void unregister(Mount mount)
	{
		mounts.remove(mount.id());
		mount.invalidate();
	}

	private boolean unmount(Path mountpoint, boolean rmdirs)
	{
		if (mountpoint == null)
			return true;

		// TODO: find a more elegant way for flushing caches!
		ImageMounter.sync(mountpoint, log);

		boolean unmounted = ImageMounter.unmount(mountpoint, log);
		if (rmdirs && unmounted) {
			// Skip mountpoint deletion, if unmount failed!
			unmounted = ImageMounter.delete(mountpoint, log);
		}

		return unmounted;
	}
}
