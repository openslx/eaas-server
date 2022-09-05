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

package de.bwl.bwfla.emucomp.components.emulators;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Structure;

import de.bwl.bwfla.emucomp.components.emulators.SocketAPI.pollfd;
import de.bwl.bwfla.emucomp.components.emulators.SocketAPI.sockaddr_un;


/** A wrapper class for a native domain-socket. */
public class IpcSocket
{
	/** Logger instance. */
	private static final Logger LOG = Logger.getLogger("IpcSocket");
	
	// Member fields
	private int sockfd;
	private final String sockname;
	private final int msgsize;
	private final boolean unlink;
	private final Type socktype;

	public static final int DEFAULT_MSGBUFFER_CAPACITY = 8120;

	/** Socket's type */
	public enum Type
	{
		STREAM,
		DGRAM
	}

	/** Creates a new domain-socket using the specified name. */
	public static IpcSocket create(String sockname, Type socktype) throws IOException
	{
		return IpcSocket.create(sockname, socktype, true);
	}
	
	/** Creates a new domain-socket using the specified name. */
	public static IpcSocket create(String sockname, Type socktype, boolean unlink) throws IOException
	{
		IpcSocket socket = null;
		try {
			final int sockfd = SocketAPI.socket(SocketAPI.AF_UNIX, SocketAPI.socktype(socktype), 0);
			sockaddr_un addr = new sockaddr_un(SocketAPI.AF_UNIX, sockname);
			SocketAPI.bind(sockfd, addr, sockaddr_un.length());
			IpcSocket.checkSocketName(sockfd, sockname);
			
			final int msgsize = IpcSocket.getBufferSize(sockfd);
			socket = new IpcSocket(sockname, socktype, sockfd, msgsize, unlink);
			LOG.info("New IPC socket " + sockname + " created.");
		}
		catch (LastErrorException exception) {
			IpcSocket.rethrow("Creating", sockname, exception);
		}
		
		return socket;
	}

	/** Connects to an existing domain-socket using the specified name. */
	public static IpcSocket connect(String sockname, Type socktype) throws IOException
	{
		return IpcSocket.connect(sockname, socktype, 120, TimeUnit.SECONDS);
	}

	/** Connects to an existing domain-socket using the specified name. */
	public static IpcSocket connect(String sockname, Type socktype, long timeout, TimeUnit timeunit) throws IOException
	{
		IpcSocket socket = null;
		try {
			final int sockfd = SocketAPI.socket(SocketAPI.AF_UNIX, SocketAPI.socktype(socktype), 0);
			sockaddr_un addr = new sockaddr_un(SocketAPI.AF_UNIX, sockname);

			final long waittime = 1000L;  // in ms
			long numretries = timeunit.toMillis(timeout) / waittime;
			LastErrorException error = null;
			do {
				try {
					SocketAPI.connect(sockfd, addr, sockaddr_un.length());
					error = null;
					break;
				}
				catch (LastErrorException exception) {
					// try one more time!
					error = exception;
				}

				try {
					Thread.sleep(waittime);
				}
				catch (Exception exception) {
					// Ignore it!
				}

				--numretries;
			}
			while (numretries > 0);

			if (error != null)
				throw error;

			final int msgsize = IpcSocket.getBufferSize(sockfd);
			socket = new IpcSocket(sockname, socktype, sockfd, msgsize, false);
			LOG.info("IPC socket " + sockname + " connected.");
		}
		catch (LastErrorException exception) {
			IpcSocket.rethrow("Connecting", sockname, exception);
		}

		return socket;
	}

	/** Sends specified message. */
	public boolean send(byte[] message, boolean blocking) throws IOException
	{
		return this.send(message, message.length, blocking);
	}

	/** Sends specified message. */
	public boolean send(byte[] message, int length, boolean blocking) throws IOException
	{
		try {
			final int flags = (blocking) ? 0 : SocketAPI.MSG_DONTWAIT;
			SocketAPI.send(sockfd, message, length, flags);
		}
		catch (LastErrorException exception) {
			if (!blocking && (exception.getErrorCode() == SocketAPI.EAGAIN))
				return false;  // Don't throw on EAGAIN

			IpcSocket.rethrow("Sending using", sockname, exception);
		}

		return true;
	}

	/** Sends specified message to the destination address. */
	public boolean sendto(String destsock, byte[] message, boolean blocking) throws IOException
	{
		return this.sendto(destsock, message, message.length, blocking);
	}
	
	/** Sends specified message to the destination address. */
	public boolean sendto(String destsock, byte[] message, int length, boolean blocking) throws IOException
	{
		try {
			final int flags = (blocking) ? 0 : SocketAPI.MSG_DONTWAIT;
			final sockaddr_un addr = new sockaddr_un(SocketAPI.AF_UNIX, destsock);
			SocketAPI.sendto(sockfd, message, length, flags, addr, sockaddr_un.length());
		}
		catch (LastErrorException exception) {
			if (!blocking && (exception.getErrorCode() == SocketAPI.EAGAIN))
				return false;  // Don't throw on EAGAIN
			
			IpcSocket.rethrow("Sending to", destsock, exception);
		}
		
		return true;
	}
	
	/**
	 * Try to receive a new message using this socket.
	 * @param buffer The buffer for received message.
	 * @return true if the message was received, else false.
	 */
	public boolean receive(ByteBuffer buffer, boolean blocking) throws IOException
	{
		buffer.clear();
		
		try {
			final int flags = (blocking) ? 0 : SocketAPI.MSG_DONTWAIT;
			int numbytes = SocketAPI.recv(sockfd, buffer.array(), buffer.capacity(), flags);
			if (numbytes == 0)
				throw new IOException("Socket reached end-of-stream!");
			
			// Update buffer
			buffer.position(numbytes);
			buffer.flip();
		}
		catch (LastErrorException exception) {
			if (!blocking && (exception.getErrorCode() == SocketAPI.EAGAIN))
				return false;  // Don't throw on EAGAIN
			
			IpcSocket.rethrow("Receiving from", sockname, exception);
		}
		
		return true;
	}
	
	/**
	 * Try to receive a new message using this socket.
	 * @param buffer The buffer for received message.
	 * @param timeout The time to wait in msec, when no data is available.
	 * @return true if the message was received, else false.
	 */
	public boolean receive(ByteBuffer buffer, int timeout) throws IOException
	{
		buffer.clear();
		
		try {
			final NativeLong numfds = new NativeLong(1);
			final pollfd fds = new pollfd(sockfd, SocketAPI.POLLIN, (short) 0);
			if (SocketAPI.poll(fds, numfds, timeout) == 0)
				return false;  // Call timed out
		}
		catch (LastErrorException exception) {
			IpcSocket.rethrow("Polling on", sockname, exception);
		}
		
		return this.receive(buffer, true);
	}
	
	/** Closes this socket. */
	public void close() throws IOException
	{
		try {
			SocketAPI.close(sockfd);
			this.sockfd = -1;
			
			if (unlink)
				SocketAPI.unlink(sockname);
			
			LOG.info("IPC socket " + sockname + " closed.");
		}
		catch (LastErrorException exception) {
			IpcSocket.rethrow("Closing", sockname, exception);
		}
	}
	
	/** Returns the socket's name. */
	public String getName()
	{
		return sockname;
	}
	
	/** Returns the message size attribute. */
	public int getMaxMsgSize()
	{
		return msgsize;
	}
	
	
	/* ==================== Internal Methods ==================== */

	/** Constructor */
	private IpcSocket(String sockname, Type socktype, int sockfd, int msgsize, boolean unlink)
	{
		this.sockfd = sockfd;
		this.sockname = sockname;
		this.msgsize = msgsize;
		this.unlink = unlink;
		this.socktype = socktype;
	}

	private static void rethrow(String prefix, String name, LastErrorException exception) throws IOException
	{
		String cause = SocketAPI.strerror(exception);
		String message = String.format("%1$s IPC socket '%2$s' failed! Cause: %3$s.", prefix, name, cause);
		
		// HACK: Passed LastErrorException often contains unprintable chars, breaking console outputs.
		//       Recreate a new Exception instance, containing only the return code.
		LastErrorException error = new LastErrorException(exception.getErrorCode());
		error.setStackTrace(exception.getStackTrace());
		throw new IOException(message, error);
	}
	
	private static int getOptionAsInt(int sockfd, int optname, IntBuffer optval, IntBuffer optlen)
	{
		optval.clear();
		optlen.clear();
		optlen.put(4);
		optlen.flip();

		SocketAPI.getsockopt(sockfd, SocketAPI.SOL_SOCKET, optname, optval, optlen);
		return optval.get();
	}
	
	private static int getBufferSize(int sockfd)
	{
		IntBuffer optval = IntBuffer.allocate(1);
		IntBuffer optlen = IntBuffer.allocate(1);

		final int sndbufsize = IpcSocket.getOptionAsInt(sockfd, SocketAPI.SO_SNDBUF, optval, optlen);
		final int rcvbufsize = IpcSocket.getOptionAsInt(sockfd, SocketAPI.SO_RCVBUF, optval, optlen);
		return (sndbufsize < rcvbufsize) ? sndbufsize : rcvbufsize;
	}
	
	private static void checkSocketName(int sockfd, String expname) throws IOException
	{
		sockaddr_un addr = new sockaddr_un();
		IntBuffer addrlen = IntBuffer.allocate(1);
		addrlen.put(sockaddr_un.length());
		addrlen.flip();
		
		try {
			SocketAPI.getsockname(sockfd, addr, addrlen);
			
			// Path's length = addrlen - sizeof(addr.sun_family) - sizeof('\0')
			final int namelen = addrlen.get() - 2 - 1;
			final String retname = new String(addr.sun_path, 0, namelen);
			if (!retname.contentEquals(expname)) {
				String details = "Expected: '" + expname + "', Returned: '" + retname + "'";
				throw new IOException("Returned socket name is wrong! " + details);
			}
		}
		catch (LastErrorException exception) {
			IpcSocket.rethrow("Getting name of", expname, exception);
		}
	}
}


/** Definitions for native API */
final class SocketAPI
{
	/** Returns the error string corresponding to the error code. */
	public static String strerror(LastErrorException exception)
	{
		int errno = exception.getErrorCode();
		return SocketAPI.strerror(errno);
	}

	public static int socktype(IpcSocket.Type socktype)
	{
		switch (socktype)
		{
			case STREAM:
				return SOCK_STREAM;

			case DGRAM:
				return SOCK_DGRAM;
		}

		return -1;
	}


	/* ========== Constants from socket.h ========== */

	// Socket's domain
	public static final int AF_UNIX = 1;

	// Socket's type
	public static final int SOCK_STREAM = 1;
	public static final int SOCK_DGRAM  = 2;

	// {get,set}sockopt's level
	public static final int SOL_SOCKET = 1;

	// Socket's options
	public static final int SO_SNDBUF = 7;
	public static final int SO_RCVBUF = 8;

	// Flag for nonblocking IO
	public static final int MSG_DONTWAIT = 0x40;

	// Error ID for "IO operation would block."
	public static final int EAGAIN = 11;


	/* ========== Constants from poll.h ========== */

	public static final short POLLIN   = 0x01;
	public static final short POLLOUT  = 0x04;
	public static final short POLLERR  = 0x08;
	public static final short POLLNVAL = 0x20;
	
	
	/** sockaddr_un struct definition */
	public static class sockaddr_un extends Structure
	{
		public static final int MAX_PATH_LENGTH = 108;
		
		public short sun_family;
		public byte[] sun_path;
		
		/** Constructor */
		public sockaddr_un()
		{
			this(AF_UNIX, "");
		}
		
		/** Constructor */
		public sockaddr_un(int family, String path)
		{
			super();
			
			this.sun_family = (short) family;
			this.sun_path = new byte[MAX_PATH_LENGTH];
			
			// Setup socket's name
			final int length = Math.min(path.length(), MAX_PATH_LENGTH);
			System.arraycopy(path.getBytes(), 0, sun_path, 0, length);
			for (int i = length; i < MAX_PATH_LENGTH; ++i)
				sun_path[i] = '\0';
			
			super.allocateMemory();
		}
		
		@Override
		protected List<String> getFieldOrder()
		{
			List<String> fields = new ArrayList<String>(2);
			fields.add("sun_family");
			fields.add("sun_path");
			return fields;
		}
		
		public static int length()
		{
			// length = sizeof(short) + sizeof(char[108])
			return (2 + MAX_PATH_LENGTH);
		}
	}

	/** pollfd struct definition */
	public static class pollfd extends Structure
	{
		public int fd;
		public short events;
		public short revents;
		
		/** Constructor */
		public pollfd()
		{
			this(-1, (short) 0, (short) 0);
		}
		
		/** Constructor */
		public pollfd(int fd, short events, short revents)
		{
			super();
			
			this.fd = fd;
			this.events = events;
			this.revents = revents;
		}
		
		@Override
		protected List<String> getFieldOrder()
		{
			List<String> fields = new ArrayList<String>(2);
			fields.add("fd");
			fields.add("events");
			fields.add("revents");
			return fields;
		}
		
		public static int length()
		{
			return (4 + 2 + 2);
		}
	}
	
	
	/* =============== Native API =============== */
	
	public static native String strerror(int errnum);
	
	public static native int socket(int socket_family, int socket_type, int protocol) throws LastErrorException;
	public static native int getsockname(int sockfd, sockaddr_un addr, Buffer addrlen) throws LastErrorException;
	public static native int getsockopt(int sockfd, int level, int optname, Buffer optval, Buffer optlen) throws LastErrorException;
	public static native int connect(int sockfd, sockaddr_un addr, int addrlen) throws LastErrorException;
	public static native int bind(int sockfd, sockaddr_un addr, int addrlen) throws LastErrorException;
	public static native int send(int sockfd, byte[] buf, int len, int flags) throws LastErrorException;
	public static native int sendto(int sockfd, byte[] buf, int len, int flags, sockaddr_un destaddr, int addrlen) throws LastErrorException;
	public static native int recv(int sockfd, byte[] buf, int len, int flags) throws LastErrorException;
	public static native int recvfrom(int sockfd, byte[] buf, int len, int flags, sockaddr_un srcaddr, Buffer addrlen) throws LastErrorException;
	public static native int close(int fd) throws LastErrorException;
	public static native int unlink(String name) throws LastErrorException;
	public static native int poll(pollfd fds, NativeLong nfds, int timeout) throws LastErrorException;
	
	static {
		if (!Platform.isLinux())
			throw new UnsupportedOperationException("The current platform is not supported!");

		Native.register(Platform.C_LIBRARY_NAME);
		Native.register("rt");
	}
}