package de.bwl.bwfla.emucomp.components.emulators;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/** A simple class for message queueing. */
public class IpcMessageQueue
{
	// Member fields
	private final Map<Integer, BlockingQueue<byte[]>> messages;
	private final int msgCapacity;

	/** The maximum number of messages in a queue. */
	public static final int DEFAULT_MSGNUMBER_PER_TYPE = 4;
	
	
	/** Constructor */
	public IpcMessageQueue()
	{
		this(DEFAULT_MSGNUMBER_PER_TYPE);
	}
	
	/** Constructor */
	public IpcMessageQueue(int msgCapacityPerType)
	{
		this.messages = new TreeMap<Integer, BlockingQueue<byte[]>>();
		this.msgCapacity = msgCapacityPerType;
	}
	
	/** non-blocking */
	public boolean offer(int msgtype, byte[] msgdata)
	{
		return this.queue(msgtype).offer(msgdata);
	}
	
	/** blocking */
	public void put(int msgtype, byte[] msgdata) throws InterruptedException
	{
		this.queue(msgtype).put(msgdata);
	}
	
	/** non-blocking */
	public byte[] poll(int msgtype)
	{
		return this.queue(msgtype).poll();
	}
	
	/** blocking */
	public byte[] take(int msgtype) throws InterruptedException
	{
		return this.queue(msgtype).take();
	}
	
	public synchronized void clear()
	{
		for (Map.Entry<Integer, BlockingQueue<byte[]>> entry : messages.entrySet())
			entry.getValue().clear();
	}
	
	
	/* ==================== Internal Methods ==================== */
	
	private synchronized BlockingQueue<byte[]> queue(int msgtype)
	{
		BlockingQueue<byte[]> queue = messages.get(msgtype);
		if (queue == null) {
			queue = new ArrayBlockingQueue<byte[]>(msgCapacity);
			messages.put(msgtype, queue);
		}
		
		return queue;
	}
}
