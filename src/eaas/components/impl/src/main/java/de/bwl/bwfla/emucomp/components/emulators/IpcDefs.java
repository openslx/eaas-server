package de.bwl.bwfla.emucomp.components.emulators;


/** Definitons for IPC using the {@link IpcSocket}s. */
public final class IpcDefs
{
	public static final byte FALSE = 0;
	public static final byte TRUE  = 1;
	
	/** Types of the supported messages */
	public static final class MessageType
	{
		public static final byte INVALID        = -1;
		public static final byte CONFIG_ENTRY   = 1;
		public static final byte CONFIG_UPDATE  = 2;
		public static final byte NOTIFICATION   = 3;
		public static final byte ATTACH_CLIENT  = 4;
		public static final byte DETACH_CLIENT  = 5;
		public static final byte TERMINATE      = 6;
		
		/** EmulatorContainer specific command */
		public static final byte EMULATOR_COMMAND = 127;
	}
	
	public static final class EventID
	{
		public static final byte EMULATOR_CTLSOCK_READY  = 1;
		public static final byte EMULATOR_READY          = 2;
		public static final byte CLIENT_ATTACHED         = 3;
		public static final byte CLIENT_DETACHED         = 4;
		public static final byte CLIENT_INACTIVE         = 5;
	}
	
	/** Types of the supported configuration values */
	public static final class ConfigType
	{
		public static final byte INT8    = 0x0 << 6;
		public static final byte INT32   = 0x1 << 6;
		public static final byte FLOAT   = (byte) (0x2 << 6);
		public static final byte STRING  = (byte) (0x3 << 6);
	}

	/** IDs of the supported configuration values */
	public static final class ConfigID
	{
		// String values
		public static final byte IOSOCKET           = ConfigType.STRING | 0;
		public static final byte CRT_FILTER         = ConfigType.STRING | 1;
		public static final byte CRT_PRESET         = ConfigType.STRING | 2;
		public static final byte KBD_MODEL          = ConfigType.STRING | 3;
		public static final byte KBD_LAYOUT         = ConfigType.STRING | 4;
		public static final byte KBD_CLIENT_MODEL   = ConfigType.STRING | 5;
		public static final byte KBD_CLIENT_LAYOUT  = ConfigType.STRING | 6;

		// Int8 values
		public static final byte RELATIVE_MOUSE     = ConfigType.INT8 | 0;
		public static final byte HARD_TERMINATION   = ConfigType.INT8 | 1;

		// Int32 values
		public static final byte INACTIVITY_TIMEOUT = ConfigType.INT32 | 0;
	}
	
	
	private IpcDefs() { }
}
