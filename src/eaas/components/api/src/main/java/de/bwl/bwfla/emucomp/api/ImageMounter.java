package de.bwl.bwfla.emucomp.api;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.MachineTokenProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageMounter {

	protected final Logger log = Logger.getLogger(this.getClass().getName());

	private Path imagePath;
	private long offset = -1;
	private long size = -1;
	private final Deque<Runnable> unmountTasks = new ArrayDeque<Runnable>();
	private boolean isReverseUnmount = true;

	private Path ddFile = null;
	private Path fsDir = null;

	Path workdir = null;
	Path ddDir = null;
	
	public ImageMounter(Path imagePath, int offset) throws BWFLAException {
		this.imagePath = imagePath;
		this.offset = offset;
		initializeWorkingDir();
	}

	public ImageMounter(Path imagePath) throws BWFLAException {
		this.imagePath = imagePath;
		initializeWorkingDir();
	}

	public Path mountDD() throws BWFLAException {

		sanityCheck(ddFile, false);

		try {
			XmountOptions xmountOptions = new XmountOptions();
			if(offset >= 0)
				xmountOptions.setOffset(offset);
			if(size >= 0)
				xmountOptions.setSize(size);

			ddFile = EmulatorUtils.xmount(imagePath.toString(), ddDir, xmountOptions);
			unmountTasks.add(() -> unmount(ddDir));

		} catch (IOException e) {
			completeUnmount();
			throw new BWFLAException("mount failed! \n" + e.getMessage());
		}

		sanityCheck(ddFile, true);

		return ddFile;
	}

	public void mountDD(long offset, long size) throws BWFLAException {
		if (offset < 0) {
			completeUnmount();
			throw new BWFLAException("Give offset: " + offset + " or given size: " + size + " are incorrect!");
		}

		if (ddFile.toFile().exists()) {
			unmountWithException("ddFile is already mounted!");
		}

		this.offset = offset;
		this.size = size;
		mountDD();
	}

	public void mountDD(long offset) throws BWFLAException {
		mountDD(offset, -1);
	}

	public Path remountDDWithOffset(long offset) throws BWFLAException {
		return remountDDWithOffset(offset, -1);
	}

	public Path remountDDWithOffset(long offset, long size) throws BWFLAException {
		if(offset < 0)
			throw new BWFLAException("offset: " + offset + " is incorrect!");

		this.offset = offset;
		this.size = size;

		completeUnmount();
		initializeWorkingDir();
		mountDD();

		//we don't need a sanitycheck, because it was already done in mountDD()
		return ddFile;
	}


	public Path mountFileSystem(FileSystemType fsType) throws BWFLAException {

		sanityCheck(ddFile, true);

		try {
			EmulatorUtils.mountFileSystem(ddFile, fsDir, fsType);
			unmountTasks.add(() -> unmount(fsDir));
		} catch (IOException e) {
			completeUnmount();
			throw new BWFLAException("mount failed! \n" + e.getMessage());
		}

		sanityCheck(fsDir, true);
		return fsDir;
	}

	public void completeUnmount() throws BWFLAException {
		while (!unmountTasks.isEmpty()) {
			final Runnable task = (isReverseUnmount) ?
					unmountTasks.removeLast() : unmountTasks.removeFirst();

			task.run();
		}
		if(!workdir.toFile().delete())
			throw new BWFLAException("Cannot delete working dir at " + workdir);

	}

	/*
	Inner Helpers
	 */
	private void initializeWorkingDir() throws BWFLAException {
		workdir = EmulatorUtils.createWorkingDir("/tmp/eaas/imagemounter");
		ddDir = workdir.resolve("dd");
		fsDir = workdir.resolve("filesystem");
	}

	private void unmount(Path path)
	{
		try {
			EmulatorUtils.unmount(path, log);
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Unmounting '" + path.toString() + "' failed!\n", error);
		}
	}

	private void sanityCheck(Path path, boolean mustExist) throws BWFLAException {

		if (path == null)
			if (!mustExist)
				return;
			else
				unmountWithException("ddFile is null!");

		sanityCheck(path.toFile(), mustExist);
	}

	private void sanityCheck(File file, boolean mustExist) throws BWFLAException {
		if (file == null)
			if (!mustExist)
				return;
			else
				unmountWithException("ddFile is null!");

		if (file.exists() == mustExist) {
			return;
		}

		unmountWithException("File " + file.getAbsolutePath() + " exist:" + file.exists() + " mustExist:" + mustExist);
	}

	private void unmountWithException(String message) throws BWFLAException {
		completeUnmount();

		throw new BWFLAException(message);
	}



	/*
	Setters and Getters
	 */

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Path getDdFile() {
		return ddFile;
	}

	public Path getFsDir() {
		return fsDir;
	}

	public Path getImagePath() {
		return imagePath;
	}

	public void setImagePath(Path imagePath) {
		this.imagePath = imagePath;
	}


	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public boolean isReverseUnmount() {
		return isReverseUnmount;
	}

	public void setReverseUnmount(boolean reverseUnmount) {
		this.isReverseUnmount = reverseUnmount;
	}
}
