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

package de.bwl.bwfla.common.services.guacplay.record;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.MetadataTag;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.VSyncType;
import de.bwl.bwfla.common.services.guacplay.events.EventSink;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.events.SessionBeginEvent;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.io.MetadataChunk;
import de.bwl.bwfla.common.services.guacplay.io.TraceBlockWriter;
import de.bwl.bwfla.common.services.guacplay.io.Metadata;
import de.bwl.bwfla.common.services.guacplay.io.TraceFile;
import de.bwl.bwfla.common.services.guacplay.io.TraceFileWriter;
import de.bwl.bwfla.common.services.guacplay.net.IGuacInterceptor;
import de.bwl.bwfla.common.services.guacplay.protocol.BufferedMessageProcessor;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.IGuacInstructionConsumer;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionParser;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionSink;
import de.bwl.bwfla.common.services.guacplay.protocol.VSyncInstrGenerator;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.ArcInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CFillInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CStrokeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CloseInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CurveInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.DisposeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.InstructionSkipper;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.LineInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.MouseInstrHandlerREC;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.PngInstrHandlerREC;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.RectInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.SizeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.StartInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.InstructionTrap;
import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;


public class SessionRecorder implements IGuacInterceptor, IGuacInstructionConsumer
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(SessionRecorder.class);

	// Member fields
	private final OffscreenCanvas canvas;
	private final InstructionSink isink;
	private final EventSink esink;
	private final AtomicLong timestamp;
	private final InstructionParser iparser;
	private final TraceBlockWriter tblock;
	private final BufferedTraceWriter tbuffer;
	private final BufferedMessageProcessor clientMsgProcessor;
	private final BufferedMessageProcessor serverMsgProcessor;
	private TraceFile tfile;
	private TraceFileWriter twriter;
	
	// Members read/written by different threads
	private volatile long startRecTimestamp;
	private volatile long stopRecTimestamp;
	private volatile boolean isRecordingEnabled;
	private volatile State state;
	
	/** Possible States */
	private static enum State
	{
		READY,
		PREPARED,
		FINISHED
	}
	
	/** The metadata-key for the number of trace entries written */
	public static final String MDKEY_NUM_TRACE_ENTRIES = "num-trace-entries";
	
	
	/** Constructor */
	public SessionRecorder(String winid, int msgBufferCapacity)
	{
		winid = winid.toUpperCase();
		
		this.canvas = new OffscreenCanvas();
		this.isink = new InstructionSink(1);
		this.esink = new EventSink(4);
		this.timestamp = new AtomicLong(0);
		this.iparser = new InstructionParser();
		this.tblock = new TraceBlockWriter();
		this.serverMsgProcessor = new BufferedMessageProcessor("SMP-" + winid, msgBufferCapacity);
		this.clientMsgProcessor = new BufferedMessageProcessor("CMP-" + winid, msgBufferCapacity);
		this.tbuffer = new BufferedTraceWriter("BTW-" + winid, clientMsgProcessor, serverMsgProcessor);
		
		final InstructionForwarder iforwarder = new InstructionForwarder(isink);
		final InstructionSkipper iskipper = new InstructionSkipper();
		final InstructionTrap itrap = new InstructionTrap();
		
		// Construct the handlers for client messages
		{
			VSyncInstrGenerator vsyncgen = VSyncInstrGenerator.construct(canvas, VSyncType.AVERAGE_COLOR, true);
			
			// Add implemented handlers
			clientMsgProcessor.addInstructionHandler(OpCode.KEY, iforwarder);
			clientMsgProcessor.addInstructionHandler(OpCode.MOUSE, new MouseInstrHandlerREC(isink, vsyncgen));
			clientMsgProcessor.addInstructionHandler(ExtOpCode.ACTION_FINISHED, iforwarder);
			
			// Mark instructions to ignore
			clientMsgProcessor.addInstructionHandler(OpCode.SELECT, iskipper);
			clientMsgProcessor.addInstructionHandler(OpCode.SIZE, iskipper);
			clientMsgProcessor.addInstructionHandler(OpCode.AUDIO, iskipper);
			clientMsgProcessor.addInstructionHandler(OpCode.VIDEO, iskipper);
			clientMsgProcessor.addInstructionHandler(OpCode.CONNECT, iskipper);
			clientMsgProcessor.addInstructionHandler(OpCode.DISCONNECT, iskipper);
			clientMsgProcessor.addInstructionHandler(OpCode.ACK, iskipper);
			clientMsgProcessor.addInstructionHandler(OpCode.SYNC, iskipper);
		}
		
		// Construct the handlers for server messages
		{
			PngInstrHandlerREC pngInstrHandler = new PngInstrHandlerREC(isink, canvas);
			
			SizeInstrHandler sizeInstrHandler = new SizeInstrHandler(2);
			sizeInstrHandler.addListener(pngInstrHandler);
			sizeInstrHandler.addListener(canvas);
			
			// Add implemented handlers
			serverMsgProcessor.addInstructionHandler(OpCode.ARC, new ArcInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.CFILL, new CFillInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.CLOSE, new CloseInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.CSTROKE, new CStrokeInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.CURVE, new CurveInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.DISPOSE, new DisposeInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.LINE, new LineInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.PNG, pngInstrHandler);
			serverMsgProcessor.addInstructionHandler(OpCode.RECT, new RectInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.SIZE, sizeInstrHandler);
			serverMsgProcessor.addInstructionHandler(OpCode.START, new StartInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(ExtOpCode.ACTION_FINISHED, iforwarder);
			
			// Mark instructions to ignore
			serverMsgProcessor.addInstructionHandler(OpCode.ARGS, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.CURSOR, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.NAME, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.AUDIO, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.VIDEO, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.ACK, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.BLOB, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.END, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.FILE, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.NEST, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.PIPE, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.NEST, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.READY, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.SYNC, iskipper);

			// Mark important, but not implemented, handlers
			serverMsgProcessor.addInstructionHandler(OpCode.CLIP, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.COPY, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.DISTORT, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.IDENTITY, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.LFILL, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.LSTROKE, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.MOVE, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.POP, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.PUSH, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.RESET, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.SET, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.SHADE, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.TRANSFER, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.TRANSFORM, itrap);
		}

		// Register listeners + event-consumers
		isink.addConsumer(this);
				
		this.tfile = null;
		this.twriter = null;
		this.startRecTimestamp = Long.MAX_VALUE;
		this.stopRecTimestamp = Long.MIN_VALUE;
		this.isRecordingEnabled = false;
		this.state = State.READY;
	}
	
	/** Post a new message to the internal message-processors. */
	public void postMessage(SourceType source, char[] data, int offset, int length)
	{
		BufferedMessageProcessor processor = (source == SourceType.CLIENT) ? clientMsgProcessor : serverMsgProcessor;
		processor.postMessage(source, timestamp.get(), data, offset, length);
	}
	
	/** Add a new metadata chunk to the trace-file. */
	public void defineMetadataChunk(String tag, String comment)
	{
		if (state != State.PREPARED)
			throw new IllegalStateException("Cannot add metadata chunks to unprepared or finished recording!");
		
		final Metadata metadata = tfile.getMetadata();
		metadata.addChunk(new MetadataChunk(tag, comment));
	}
	
	/** Add a key/value pair as metadata to the trace-file. */
	public void addMetadataEntry(String ctag, String key, String value)
	{
		if (state != State.PREPARED)
			throw new IllegalStateException("Cannot add metadata entries to unprepared or finished recording!");
		
		final Metadata metadata = tfile.getMetadata();
		MetadataChunk chunk = metadata.getChunk(ctag);
		if (chunk == null) {
			// Not found, create a new chunk
			chunk = new MetadataChunk(ctag);
			metadata.addChunk(chunk);
		}
		
		chunk.put(key, value);
	}
	
	/** Start the recording. */
	public void start()
	{
		if (state != State.PREPARED)
			throw new IllegalStateException("Recorder is not prepared!");
		
		if (isRecordingEnabled)
			throw new IllegalStateException("Attempt to start recording multiple times!");
		
		try {
			this.writeInitialSyncPoints();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
		
		// Update the timestamps
		startRecTimestamp = timestamp.get();
		stopRecTimestamp = Long.MAX_VALUE;
		
		isRecordingEnabled = true;
	}
	
	/** Stop the recording. */
	public void stop()
	{
		if (!isRecordingEnabled)
			return;
		
		// Update the timestamps
		stopRecTimestamp = timestamp.get();
		
		isRecordingEnabled = false;
	}
	
	/** Returns true when in recording mode, else false. */
	public final boolean isRecording()
	{
		return isRecordingEnabled;
	}
	
	/** Prepare the trace-file for writing. */
	public void prepare(Path dstpath) throws IOException
	{
		if (state != State.READY)
			throw new IllegalStateException("Attempt to call SessionRecorder.prepare() multiple times!");
		
		// Create a writer using the specified path
		tfile = new TraceFile(dstpath, StandardCharsets.UTF_8);
		twriter = tfile.newBufferedWriter();
		twriter.prepare();
		
		log.info("Recording into trace-file:  {}", dstpath.toString());
		
		twriter.comment("User's input-events and server's updates.");
		twriter.comment("Format: " + tblock.format());
		twriter.begin(tblock);
		
		// Start processing
		tbuffer.start();
		
		state = State.PREPARED;
	}
	
	/** Finish the recording-process and terminate all message-processors. */
	public void finish() throws IOException
	{
		if (state != State.PREPARED) {
			log.warn("Skip finishing an unprepared recorder!");
			return;
		}
		
		if (this.isRecording()) {
			log.warn("Recording was not stopped properly!");
			this.stop();
		}
		
		// Notify about the termination of the processor
		esink.consume(new GuacEvent(EventType.TERMINATION, this));
		
		// Flush buffered trace-entries
		tbuffer.terminate(true);
		
		// At this point all pending messages are processed and the connection is closed.
		
		final String numEntries = Integer.toString(tblock.getNumEntriesWritten());
		this.addMetadataEntry(MetadataTag.INTERNAL, MDKEY_NUM_TRACE_ENTRIES, numEntries);
		
		// Finish writing to the trace-file and close it too!
		twriter.finish();
		twriter.close();
		
		final String path = tfile.getPath().toString();
		log.info("{} bytes written to trace-file:  {}", twriter.getNumBytesWritten(), path);
		
		state = State.FINISHED;
	}

	/** Returns true when recording was finished, else false. */
	public final boolean isFinished()
	{
		return (state == State.FINISHED);
	}
	
	
	/* ========== IGuacSessionListener Implementation ========== */
	
	@Override
	public void onBeginConnection() throws IOException
	{
		// Notify about the session start!
		esink.consume(new SessionBeginEvent(this, timestamp.get()));
	}

	@Override
	public void onEndConnection() throws IOException
	{
		// Do nothing!
	}

	@Override
	public boolean onClientMessage(CharArrayWrapper message) throws Exception
	{
		// Find the timestamp of the last received instruction
		
		final char[] array = message.array();
		final int offset = message.offset();
		int length = message.length();
		int index = offset + length - 2;
		
		// Search from message's back to front
		// the begin of the last instruction...
		while (index >= offset) {
			final char c = array[index];
			if (c == GuacDefs.INSTRUCTION_TERMINATOR)
				break;
			
			--index;
		}
		
		++index;
		length -= index - offset;

		long newTimestamp = -1;
		
		// Try to parse the timestamp only
		iparser.setInput(array, index, length);
		final String tsval = iparser.parseOpcode();
		final char c = tsval.charAt(0);
		if (c >= '0' && c <= '9')
			newTimestamp = Long.parseLong(tsval);
		
		// Pass the message unmodified to the processor
		if (clientMsgProcessor.postMessage(SourceType.CLIENT, timestamp.get(), message) == 1)
			tbuffer.wakeup();
		
		// Now we can update the timestamp
		if (newTimestamp > 0)
			timestamp.set(newTimestamp + 1);
		
		return true;
	}

	@Override
	public boolean onServerMessage(CharArrayWrapper message) throws Exception
	{
		// Pass the message unmodified to the processor
		if (serverMsgProcessor.postMessage(SourceType.SERVER, timestamp.get(), message) == 1)
			tbuffer.wakeup();
		
		return true;
	}

	
	/* ========== IGuacInstructionConsumer Implementation ========== */
	
	@Override
	public void consume(InstructionDescription desc, Instruction instr) throws IOException
	{
		final long timestamp = desc.getTimestamp();
		
		// Should the instruction be recorded to file?
		final boolean record = (timestamp >= startRecTimestamp) && (timestamp <= stopRecTimestamp)
				|| instr.getOpcode().startsWith(ExtOpCode.SCREEN_UPDATE);
		
		if (record)
			tblock.write(desc, instr);
	}
	
	
	/* =============== INTERNAL METHODS =============== */
	
	private void writeInitialSyncPoints() throws IOException
	{
		final VSyncInstrGenerator vsyncgen = VSyncInstrGenerator.construct(canvas, VSyncType.AVERAGE_COLOR, false);
		final InstructionDescription idesc = new InstructionDescription(SourceType.INTERNAL);
		final Instruction vsync = new Instruction(5);
		
		// Canvas-points used for visual-sync:
		//
		//     -----------------------    ---
		//    |      x         x      |    |
		//    | x         x         x |    |
		//    |      x         x      |  height
		//    | x         x         x |    |
		//    |      x         x      |    |
		//     -----------------------    ---
		//
		//    |-------- width --------|
		
		final int width = canvas.getWidth();
		final int height = canvas.getHeight();
		final int ymargin = 5 + GuacDefs.VSYNC_RECT_HEIGHT / 2;
		final int xmargin = 5 + GuacDefs.VSYNC_RECT_WIDTH  / 2;
		final int dy = (height - ymargin - ymargin) / 4;
		final int dx = (width  - xmargin - xmargin) / 4;
		final int xstep = dx + dx;
		int xpos, jmax;
		
		// Write instructions to file
		synchronized (twriter) {
			
			// From top to bottom (y-direction)
			for (int i = 0, ypos = ymargin; i < 5; ++i, ypos += dy) {
				if (i % 2 == 0) {
					xpos = xmargin + dx;
					jmax = 2;
				}
				else {
					xpos = xmargin;
					jmax = 3;
				}
				
				// From left to right (x-direction)
				for (int j = 0; j < jmax; ++j, xpos += xstep) {
					vsyncgen.generate(xpos, ypos, vsync);
					idesc.setTimestamp(timestamp.get() + 1);
					tblock.write(idesc, vsync);
				}
			}
		}
	}
	
	public String toString()
	{
		if (state != State.FINISHED)
			return null;
		
		try {
			return FileUtils.readFileToString(tfile.getPath().toFile());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
