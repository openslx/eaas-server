package de.bwl.bwfla.emucomp.components.emulators;

import java.io.IOException;

import de.bwl.bwfla.common.services.guacplay.util.FlagSet;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.ConfigID;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.ConfigType;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.MessageType;


/** EmulatorContainer's configuration settings. */
public class EmulatorConfig
{
	// Member fields
	private final FlagSet[] validmasks;
	private final FlagSet[] dirtymasks;
	private final String[]  svalues;
	private final byte[]    bvalues;
	private final float[]   fvalues;
	private final int[]     ivalues;

	// Numbers of predefined entries
	private static final int NUM_STRING_ENTRIES = 7;
	private static final int NUM_BYTE_ENTRIES   = 2;
	private static final int NUM_FLOAT_ENTRIES  = 0;
	private static final int NUM_INT_ENTRIES    = 1;

	// Precomputed constants
	private static final int TYPE_SHIFT  = 6;
	private static final int TYPE_MASK   = 3 << TYPE_SHIFT;
	private static final int INDEX_MASK  = 0xFF ^ TYPE_MASK;

	// Default keyboard settings
	public static final String DEFAULT_KEYBOARD_MODEL  = "pc105";
	public static final String DEFAULT_KEYBOARD_LAYOUT = "en";

	/** Constructor */
	public EmulatorConfig()
	{
		this.validmasks = new FlagSet[4];
		this.dirtymasks = new FlagSet[4];
		this.svalues = new String[NUM_STRING_ENTRIES];
		this.bvalues = new byte[NUM_BYTE_ENTRIES];
		this.fvalues = new float[NUM_FLOAT_ENTRIES];
		this.ivalues = new int[NUM_INT_ENTRIES];

		for (int i = 0; i < 4; ++i) {
			validmasks[i] = new FlagSet();
			dirtymasks[i] = new FlagSet();
		}
	}


	/* ========== Setter Methods ========== */

	public void setIoSocket(String name)
	{
		this.check(name, "Invalid IO-Socket specified!");
		this.set(ConfigID.IOSOCKET, name);
	}

	public void setHardTermination(boolean enabled)
	{
		byte value = (enabled) ? IpcDefs.TRUE : IpcDefs.FALSE;
		this.set(ConfigID.HARD_TERMINATION, value);
	}

	public void setRelativeMouse(boolean enabled)
	{
		byte value = (enabled) ? IpcDefs.TRUE : IpcDefs.FALSE;
		this.set(ConfigID.RELATIVE_MOUSE, value);
	}

	public void setKeyboardModel(String model)
	{
		this.check(model, "Invalid emulator's keyboard model specified!");
		this.set(ConfigID.KBD_MODEL, model);
	}

	public void setKeyboardLayout(String layout)
	{
		this.check(layout, "Invalid emulator's keyboard layout specified!");
		this.set(ConfigID.KBD_LAYOUT, layout);
	}

	public void setClientKeyboardModel(String model)
	{
		this.check(model, "Invalid client's keyboard model specified!");
		this.set(ConfigID.KBD_CLIENT_MODEL, model);
	}

	public void setClientKeyboardLayout(String layout)
	{
		this.check(layout, "Invalid client's keyboard layout specified!");
		this.set(ConfigID.KBD_CLIENT_LAYOUT, layout);
	}

	public void setCrtFilter(String filter)
	{
		this.check(filter, "Invalid CRT filter specified!");
		this.set(ConfigID.CRT_FILTER, filter);
	}

	public void setCrtPreset(String preset)
	{
		this.check(preset, "Invalid CRT preset specified!");
		this.set(ConfigID.CRT_PRESET, preset);
	}

	public void setInactivityTimeout(int timeout)
	{
		if (timeout < 0)
			throw new IllegalArgumentException("Invalid inactivity timeout specified!");

		this.set(ConfigID.INACTIVITY_TIMEOUT, timeout);
	}


	/* ========== Getter Methods ========== */

	public String getIoSocket()
	{
		return this.getString(ConfigID.IOSOCKET);
	}

	public boolean isHardTermination()
	{
		byte value = this.getByte(ConfigID.HARD_TERMINATION);
		return (value == IpcDefs.TRUE);
	}

	public boolean isRelativeMouse()
	{
		byte value = this.getByte(ConfigID.RELATIVE_MOUSE);
		return (value == IpcDefs.TRUE);
	}

	public String getKeyboardModel()
	{
		return this.getString(ConfigID.KBD_MODEL);
	}

	public String getKeyboardLayout()
	{
		return this.getString(ConfigID.KBD_LAYOUT);
	}

	public String getClientKeyboardModel()
	{
		return this.getString(ConfigID.KBD_CLIENT_MODEL);
	}

	public String getClientKeyboardLayout()
	{
		return this.getString(ConfigID.KBD_CLIENT_LAYOUT);
	}

	public String getCrtFilter()
	{
		return this.getString(ConfigID.CRT_FILTER);
	}

	public String getCrtPreset()
	{
		return this.getString(ConfigID.CRT_PRESET);
	}

	public int getInactivityTimeout()
	{
		return this.getInt(ConfigID.INACTIVITY_TIMEOUT);
	}

	public boolean isEntryValid(byte id)
	{
		final int type = id2type(id);
		final int tidx = type >> TYPE_SHIFT;
		final int flag = 1 << id2index(id);
		return validmasks[tidx].enabled(flag);
	}


	/* =============== Send API =============== */

	/**
	 * Sends all modified entries to the specified address.
	 * @param socket The output socket.
	 * @param address The destination address.
	 * @return The number of send entries.
	 */
	public int sendTo(IpcSocket socket, String address) throws IOException
	{
		return this.send(socket, address, true);
	}

	/**
	 * Sends all valid entries to the specified address.
	 * @param socket The output socket.
	 * @param address The destination address.
	 * @return The number of send entries.
	 */
	public int sendAllTo(IpcSocket socket, String address) throws IOException
	{
		return this.send(socket, address, false);
	}


	/* ========== Internal Helpers ========== */

	private static int id2type(byte id)
	{
		return (id & TYPE_MASK);
	}

	private static int id2type(byte id, byte expected)
	{
		final int type = id & TYPE_MASK;
		if (type != (expected & 0xFF))
			throw new IllegalArgumentException("Invalid config's type: " + Integer.toHexString(type));

		return type;
	}

	private static int id2index(byte id)
	{
		return (id & INDEX_MASK);
	}

	private static int id2index(byte id, int maxindex)
	{
		final int index = id & INDEX_MASK;
		if (index >= maxindex) {
			String message = "Invalid config's index: "
					+ index + " (max. " + maxindex + ")";
			throw new IllegalArgumentException(message);
		}

		return index;
	}

	private void check(String arg, String message)
	{
		if (arg == null || arg.isEmpty())
			throw new IllegalArgumentException(message);
	}

	private void mark(int type, int index)
	{
		final int tidx = type >> TYPE_SHIFT;
		final int flag = 1 << index;
		validmasks[tidx].set(flag);
		dirtymasks[tidx].set(flag);
	}

	private void set(byte id, String value)
	{
		final int type = id2type(id, ConfigType.STRING);
		final int index = id2index(id, NUM_STRING_ENTRIES);
		this.mark(type, index);
		svalues[index] = value;
	}

	private void set(byte id, byte value)
	{
		final int type = id2type(id, ConfigType.INT8);
		final int index = id2index(id, NUM_BYTE_ENTRIES);
		this.mark(type, index);
		bvalues[index] = value;
	}

	@SuppressWarnings("unused")
	private void set(byte id, float value)
	{
		final int type = id2type(id, ConfigType.FLOAT);
		final int index = id2index(id, NUM_FLOAT_ENTRIES);
		this.mark(type, index);
		fvalues[index] = value;
	}

	private void set(byte id, int value)
	{
		final int type = id2type(id, ConfigType.INT32);
		final int index = id2index(id, NUM_INT_ENTRIES);
		this.mark(type, index);
		ivalues[index] = value;
	}

	private String getString(byte id)
	{
		return svalues[id2index(id, NUM_STRING_ENTRIES)];
	}

	private byte getByte(byte id)
	{
		return bvalues[id2index(id, NUM_BYTE_ENTRIES)];
	}

	@SuppressWarnings("unused")
	private float getFloat(byte id)
	{
		return fvalues[id2index(id, NUM_FLOAT_ENTRIES)];
	}

	private int getInt(byte id)
	{
		return ivalues[id2index(id, NUM_INT_ENTRIES)];
	}

	private int sendString(byte id, boolean dirtycheck, IpcMessageWriter message, String address) throws IOException
	{
		final int type = id2type(id, ConfigType.STRING);
		final int vidx = id2index(id, NUM_STRING_ENTRIES);
		final int tidx = type >> TYPE_SHIFT;
		final int flag = 1 << vidx;
		if (!validmasks[tidx].enabled(flag))
			return 0;  // Entry not valid!

		if (dirtycheck && !dirtymasks[tidx].enabled(flag))
			return 0;  // Entry was not changed!

		message.begin(MessageType.CONFIG_ENTRY);
		message.write(id);
		message.write(svalues[vidx]);
		message.send(address);

		dirtymasks[tidx].reset(flag);
		return 1;
	}

	private int sendByte(byte id, boolean dirtycheck, IpcMessageWriter message, String address) throws IOException
	{
		final int type = id2type(id, ConfigType.INT8);
		final int vidx = id2index(id, NUM_BYTE_ENTRIES);
		final int tidx = type >> TYPE_SHIFT;
		final int flag = 1 << vidx;
		if (!validmasks[tidx].enabled(flag))
			return 0;  // Entry not valid!

		if (dirtycheck && !dirtymasks[tidx].enabled(flag))
			return 0;  // Entry was not changed!

		message.begin(MessageType.CONFIG_ENTRY);
		message.write(id);
		message.write(bvalues[vidx]);
		message.send(address);

		dirtymasks[tidx].reset(flag);
		return 1;
	}

	private int sendInt(byte id, boolean dirtycheck, IpcMessageWriter message, String address) throws IOException
	{
		final int type = id2type(id, ConfigType.INT32);
		final int vidx = id2index(id, NUM_INT_ENTRIES);
		final int tidx = type >> TYPE_SHIFT;
		final int flag = 1 << vidx;
		if (!validmasks[tidx].enabled(flag))
			return 0;  // Entry not valid!

		if (dirtycheck && !dirtymasks[tidx].enabled(flag))
			return 0;  // Entry was not changed!

		message.begin(MessageType.CONFIG_ENTRY);
		message.write(id);
		message.write(ivalues[vidx]);
		message.send(address);

		dirtymasks[tidx].reset(flag);
		return 1;
	}

	private int send(IpcSocket socket, String address, boolean dirtycheck) throws IOException
	{
		int counter = 0;

		IpcMessageWriter message = new IpcMessageWriter(socket);
		counter += this.sendByte(ConfigID.HARD_TERMINATION, dirtycheck, message, address);
		counter += this.sendByte(ConfigID.RELATIVE_MOUSE, dirtycheck, message, address);
		counter += this.sendInt(ConfigID.INACTIVITY_TIMEOUT, dirtycheck, message, address);
		counter += this.sendString(ConfigID.IOSOCKET, dirtycheck, message, address);
		counter += this.sendString(ConfigID.CRT_FILTER, dirtycheck, message, address);
		counter += this.sendString(ConfigID.CRT_PRESET, dirtycheck, message, address);
		counter += this.sendString(ConfigID.KBD_MODEL, dirtycheck, message, address);
		counter += this.sendString(ConfigID.KBD_LAYOUT, dirtycheck, message, address);
		counter += this.sendString(ConfigID.KBD_CLIENT_MODEL, dirtycheck, message, address);
		counter += this.sendString(ConfigID.KBD_CLIENT_LAYOUT, dirtycheck, message, address);
		
		if (counter > 0) {
			message.begin(MessageType.CONFIG_UPDATE);
			message.send(address, true);
		}
		
		return counter;
	}
}
