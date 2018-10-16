package de.bwl.bwfla.emucomp.components.emulators;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.MessageType;

/** Helper class for reading IPC messages. */
public class IpcMessageReader
{
	private final IpcSocket socket;
	private final ByteBuffer buffer;

	/** Constructor */
	public IpcMessageReader(IpcSocket socket)
	{
		this.socket = socket;
		this.buffer = ByteBuffer.allocate(socket.getMaxMsgSize());
	}
	
	public void read() throws IOException
	{
		socket.receive(buffer, true);
	}
	
	public boolean read(boolean blocking) throws IOException
	{
		return socket.receive(buffer, blocking);
	}
	
	public boolean read(int timeout) throws IOException
	{
		return socket.receive(buffer, timeout);
	}
	
	public byte getMessageType()
	{
		return buffer.get(0);
	}
	
	public byte[] getMessageData()
	{
		final int length = this.size() - 1;
		byte[] msgdata = new byte[length];
		System.arraycopy(this.array(), 1, msgdata, 0, length);
		return msgdata;
	}
	
	public boolean isNotification()
	{
		return (this.getMessageType() == MessageType.NOTIFICATION);
	}
	
	public byte getEventID()
	{
		return buffer.get(1);
	}
	
	/** Returns the underlying array. */
	public byte[] array()
	{
		return buffer.array();
	}
	
	/** Returns the message's length. */
	public int size()
	{
		return buffer.limit();
	}
}
