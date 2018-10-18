package de.bwl.bwfla.emucomp.components.emulators;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Helper class for writing IPC messages. */
public class IpcMessageWriter
{
	private final IpcSocket socket;
	private final ByteBuffer buffer;

	/** Charset used to encode all config values */
	public static final Charset CHARSET = StandardCharsets.US_ASCII;
	
	/** Constructor */
	public IpcMessageWriter(IpcSocket socket)
	{
		this.socket = socket;
		this.buffer = ByteBuffer.allocate(socket.getMaxMsgSize());
	}
	
	/** Resets this writer and begins a new message. */
	public void begin(byte msgtype)
	{
		buffer.clear();
		buffer.put(msgtype);
	}
	
	/** Finishes writing the current message and sends it. */
	public boolean send(String destsock) throws IOException
	{
		return this.send(destsock, true);
	}
	
	/** Finishes writing the current message and sends it. */
	public boolean send(String destsock, boolean blocking) throws IOException
	{
		return socket.send(destsock, this.array(), this.size(), blocking);
	}
	
	/** Writes a byte value. */
	public void write(byte value)
	{
		buffer.put(value);
	}
	
	/** Writes an array of bytes. */
	public void write(byte[] value)
	{
		this.write(value, 0, value.length);
	}
	
	/** Writes a subset of bytes. */
	public void write(byte[] value, int offset, int length)
	{
		buffer.put(value, offset, length);
	}
	
	/** Writes a int value. */
	public void write(int value)
	{
		buffer.putInt(value);
	}
	
	/** Writes a string value, using the default charset. */
	public void write(String value)
	{
		this.write(value, CHARSET);
	}
	
	/** Writes a string value, using the default charset. */
	public void write(String value, boolean lengthPrefixed)
	{
		this.write(value, CHARSET, lengthPrefixed);
	}
	
	/** Writes a string value, using specified charset. */
	public void write(String value, Charset charset)
	{
		this.write(value, CHARSET, true);
	}
	
	/** Writes a string value, using specified charset. */
	public void write(String value, Charset charset, boolean lengthPrefixed)
	{
		byte[] data = value.getBytes(charset);
		if (lengthPrefixed)
			buffer.putShort((short) data.length);
		
		buffer.put(data);
	}
	
	/** Returns the underlying array. */
	public byte[] array()
	{
		return buffer.array();
	}
	
	/** Returns the message's length. */
	public int size()
	{
		return buffer.position();
	}
}
