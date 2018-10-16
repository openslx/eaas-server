package de.bwl.bwfla.emucomp.components.emulators;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.naming.InitialContext;
import javax.websocket.CloseReason;

import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;
import de.bwl.bwfla.emucomp.api.Nic;
import de.bwl.bwfla.emucomp.control.ResumableSocket;
import de.bwl.bwfla.emucomp.control.ResumableSocket.MessageHandlerBoth;
import de.bwl.bwfla.emucomp.control.connectors.WebEmulatorConnector;

/**
 * @author rafael@gieschke.de
 */
public class WebEmulatorBean extends EmulatorBean implements MessageHandlerBoth {

	/** Currently open raw files corresponding to attached drives, index is used as ID. */
	public List<RandomAccessFile> openFiles = Collections.synchronizedList(new ArrayList<RandomAccessFile>());

	public ResumableSocket socket = new ResumableSocket(this);
	protected Thread workerThread;
	public ArrayBlockingQueue<JsonObject> requests = new ArrayBlockingQueue<JsonObject>(100);

	protected String emulator = "webemulator";

	@Override
	public void start() {
		try {
			ManagedThreadFactory threadFactory = InitialContext.doLookup("java:jboss/ee/concurrency/factory/default");
			workerThread = threadFactory.newThread(() -> {
				for (;;) {
					try {
						final JsonObject request = requests.take();
						messageFromClient(request);
					} catch (Exception e) {
					}
				}
			});
			workerThread.start();
		} catch (Exception e) {
		}

		JsonObjectBuilder command = Json.createObjectBuilder();
		command.add("type", "start");
		socket.sendText(command.build().toString());
		emuBeanState.update(EmuCompState.EMULATOR_RUNNING);
		addControlConnector(new WebEmulatorConnector(emulator));
	}

	@Override
	public void stop() throws BWFLAException {
		super.stop();

		JsonObjectBuilder command = Json.createObjectBuilder();
		command.add("type", "terminate");
		socket.sendText(command.build().toString());
		socket.terminate(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "terminate"));

		workerThread.stop();
		for (RandomAccessFile file : openFiles) {
			try {
				file.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	protected void prepareEmulatorRunner() throws BWFLAException {
		JsonObjectBuilder command = Json.createObjectBuilder();
		command.add("type", "prepareEmulatorRunner");
		if (getNativeConfig() != null)
			command.add("config", getNativeConfig());
		command.add("emulator", emulator);
		socket.sendText(command.build().toString());
	}

	@Override
	public Set<String> getHotplugableDrives() {
		HashSet<String> set = new HashSet<String>();
		set.add(DriveType.CDROM.name());
		set.add(DriveType.DISK.name());
		set.add(DriveType.FLOPPY.name());
		return set;
	}

	public void onMessage(String message) {
		JsonObject messageJson = Json.createReader(new StringReader(message)).readObject();
		try {
			requests.offer(messageJson);
		} catch (IllegalStateException e) {
			socket.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "queue size exceeded"));
		}
	}

	public void onMessage(byte[] message) {
		// DEBUG
		socket.sendText("echo back " + socket.lastSentEventId + " " + socket.lastReceivedEventId);
	}

	public void messageFromClient(JsonObject message) {
		try {
			int fd = message.getInt("fd");
			RandomAccessFile file = openFiles.get(fd);
			file.seek(message.getInt("pos"));
			int size = Math.min(message.getInt("size"), 1024 * 1024);
			byte[] bytes = new byte[size];
			file.read(bytes);
			socket.sendBinary(ByteBuffer.wrap(bytes));
		} catch (Exception e) {
			socket.sendBinary(ByteBuffer.allocate(0));
		}
	}

	private JsonObjectBuilder getDriveInfo(Drive drive) {
		JsonObjectBuilder ret = Json.createObjectBuilder();
		if (drive == null)
			return ret;
		if (drive.getBus() != null)
			ret.add("bus", drive.getBus());
		if (drive.getData() != null)
			ret.add("data", drive.getData());
		if (drive.getFilesystem() != null)
			ret.add("filesystem", drive.getFilesystem());
		if (drive.getIface() != null)
			ret.add("iface", drive.getIface());
		if (drive.getType() != null)
			ret.add("type", drive.getType().toString());
		if (drive.getUnit() != null)
			ret.add("unit", drive.getUnit());
		ret.add("boot", drive.isBoot());
		ret.add("plugged", drive.isPlugged());
		return ret;
	}

	@Override
	public boolean addDrive(Drive drive) {
		return connectDrive(drive, true);
	}

	@Override
	public boolean connectDrive(Drive drive, boolean connect) {
		JsonObjectBuilder command = Json.createObjectBuilder();
		command.add("type", "connectDrive");
		command.add("connect", connect);
		command.add("drive", getDriveInfo(drive));

		if (connect) {
			if (drive == null) {
				LOG.warning("Drive doesn't contain an image, attach canceled.");
				return false;
			}

			Path imagePath = null;
			try {
				imagePath = Paths
						.get(lookupResource(drive.getData(), getImageFormatForDriveType(drive.getType())));
			} catch (Exception e) {
				LOG.warning("Drive doesn't reference a valid binding, attach cancelled.");
				return false;
			}

			try {
				// TODO: Is sync I/O okay on this thread?
				RandomAccessFile file = new RandomAccessFile(imagePath.toString(), "rw");
				synchronized (openFiles) {
					openFiles.add(file);
					int fd = openFiles.size() - 1;
					command.add("fd", fd);
				}
			} catch (Exception e) {
				LOG.warning("Could not open image file, attach cancelled.");
				return false;
			}
		}
		socket.sendText(command.build().toString());
		return true;
	}

	protected boolean addNic(Nic nic) {
		if (nic == null) {
			LOG.warning("NIC is null, attach canceled.");
			return false;
		}

		JsonObjectBuilder command = Json.createObjectBuilder();
		command.add("type", "addNic");
		command.add("macaddr", nic.getHwaddress());
		socket.sendText(command.build().toString());
		return true;
	}
}
