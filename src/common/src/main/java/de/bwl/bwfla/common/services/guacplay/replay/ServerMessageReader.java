package de.bwl.bwfla.common.services.guacplay.replay;

import de.bwl.bwfla.common.services.guacplay.net.GuacReader;
import de.bwl.bwfla.common.services.guacplay.protocol.AsyncWorker;
import de.bwl.bwfla.common.services.guacplay.protocol.MessageProcessor;
import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;

// Internal class (package-private)


final class ServerMessageReader extends AsyncWorker
{
	// Member fields
	private final ICharArrayConsumer output;
	private final GuacReader input;
	private final String name;
	
	/** Timeout for waiting, when nothing can be read. */
	private static final long TIMEOUT_ON_UNAVAILABLE = 250L;
	

	/** Constructor */
	public ServerMessageReader(String name, MessageProcessor processor, GuacReader input, ICharArrayConsumer output)
	{
		super();
		
		this.output = output;
		this.input = input;
		this.name = name;
	}
	
	@Override
	protected void execute() throws Exception
	{
		// Can something be read?
		if (!input.available()) {
			condition.await(TIMEOUT_ON_UNAVAILABLE);
			return;  // No, retry later
		}
		
		// Yes, then read the new message
		final char[] data = input.read();
		if (data == null)
			return;  // End-of-stream reached!
		
		output.consume(data, 0, data.length);
	}

	@Override
	protected void finish() throws Exception
	{
		// Do nothing!
	}

	@Override
	protected String getName()
	{
		return name;
	}
}
