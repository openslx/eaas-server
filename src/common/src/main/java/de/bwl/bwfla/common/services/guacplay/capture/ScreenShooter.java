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

package de.bwl.bwfla.common.services.guacplay.capture;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.net.IGuacInterceptor;
import de.bwl.bwfla.common.services.guacplay.protocol.BufferedMessageProcessor;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionBuilder;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.ArcInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CFillInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CStrokeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CloseInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CurveInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.DisposeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.InstructionSkipper;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.LineInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.PngInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.RectInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.SizeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.StartInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.InstructionTrap;
import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;
import de.bwl.bwfla.common.services.guacplay.util.StopWatch;


/** A helper class for capturing screenshots on the server-side. */
public class ScreenShooter implements IGuacInterceptor
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(ScreenShooter.class);

	// Member fields
	private final StopWatch stopwatch;
	private final OffscreenCanvas canvas;
	private final ScrShotInstrHandler screenshots;
	private final BufferedMessageProcessor msgProcessor;
	private final ServerMessageProcessor msgWorker;
	
	// Members read/written by different threads
	private volatile State state;
	
	/** Possible States */
	private static enum State
	{
		READY,
		PREPARED,
		FINISHED
	}
	
	/** Serialized screenshot instruction. */
	private static final char[] SCREENSHOT_MESSAGE;
	static {
		InstructionBuilder builder = new InstructionBuilder(128);
		builder.start(ExtOpCode.SCREENSHOT);
		builder.finish();
		
		SCREENSHOT_MESSAGE = builder.toCharArray();
	}
	
	/** Constructor */
	public ScreenShooter(String id, int msgBufferCapacity)
	{
		this.stopwatch = new StopWatch();
		this.canvas = new OffscreenCanvas();
		this.screenshots = new ScrShotInstrHandler(canvas);
		this.msgProcessor = new BufferedMessageProcessor("SSP-" + id, msgBufferCapacity);
		this.msgWorker = new ServerMessageProcessor(msgProcessor);

		// Construct the handlers for drawing-messages
		{
			PngInstrHandler pngInstrHandler = new PngInstrHandler(canvas);
			
			SizeInstrHandler sizeInstrHandler = new SizeInstrHandler(2);
			sizeInstrHandler.addListener(pngInstrHandler);
			sizeInstrHandler.addListener(canvas);
			
			// Add implemented handlers
			msgProcessor.addInstructionHandler(OpCode.ARC, new ArcInstrHandler(canvas));
			msgProcessor.addInstructionHandler(OpCode.CFILL, new CFillInstrHandler(canvas));
			msgProcessor.addInstructionHandler(OpCode.CLOSE, new CloseInstrHandler(canvas));
			msgProcessor.addInstructionHandler(OpCode.CSTROKE, new CStrokeInstrHandler(canvas));
			msgProcessor.addInstructionHandler(OpCode.CURVE, new CurveInstrHandler(canvas));
			msgProcessor.addInstructionHandler(OpCode.DISPOSE, new DisposeInstrHandler(canvas));
			msgProcessor.addInstructionHandler(OpCode.LINE, new LineInstrHandler(canvas));
			msgProcessor.addInstructionHandler(OpCode.PNG, pngInstrHandler);
			msgProcessor.addInstructionHandler(OpCode.RECT, new RectInstrHandler(canvas));
			msgProcessor.addInstructionHandler(OpCode.SIZE, sizeInstrHandler);
			msgProcessor.addInstructionHandler(OpCode.START, new StartInstrHandler(canvas));
			msgProcessor.addInstructionHandler(ExtOpCode.SCREENSHOT, screenshots);
			
			// Mark instructions to ignore
			final InstructionSkipper iskipper = new InstructionSkipper();
			msgProcessor.addInstructionHandler(OpCode.ARGS, iskipper);
			msgProcessor.addInstructionHandler(OpCode.CURSOR, iskipper);
			msgProcessor.addInstructionHandler(OpCode.NAME, iskipper);
			msgProcessor.addInstructionHandler(OpCode.AUDIO, iskipper);
			msgProcessor.addInstructionHandler(OpCode.VIDEO, iskipper);
			msgProcessor.addInstructionHandler(OpCode.ACK, iskipper);
			msgProcessor.addInstructionHandler(OpCode.BLOB, iskipper);
			msgProcessor.addInstructionHandler(OpCode.END, iskipper);
			msgProcessor.addInstructionHandler(OpCode.FILE, iskipper);
			msgProcessor.addInstructionHandler(OpCode.NEST, iskipper);
			msgProcessor.addInstructionHandler(OpCode.PIPE, iskipper);
			msgProcessor.addInstructionHandler(OpCode.SYNC, iskipper);
			msgProcessor.addInstructionHandler(OpCode.READY, iskipper);

			// Mark important, but not implemented, handlers
			final InstructionTrap itrap = new InstructionTrap();
			msgProcessor.addInstructionHandler(OpCode.CLIP, itrap);
			msgProcessor.addInstructionHandler(OpCode.COPY, itrap);
			msgProcessor.addInstructionHandler(OpCode.DISTORT, itrap);
			msgProcessor.addInstructionHandler(OpCode.IDENTITY, itrap);
			msgProcessor.addInstructionHandler(OpCode.LFILL, itrap);
			msgProcessor.addInstructionHandler(OpCode.LSTROKE, itrap);
			msgProcessor.addInstructionHandler(OpCode.MOVE, itrap);
			msgProcessor.addInstructionHandler(OpCode.POP, itrap);
			msgProcessor.addInstructionHandler(OpCode.PUSH, itrap);
			msgProcessor.addInstructionHandler(OpCode.RESET, itrap);
			msgProcessor.addInstructionHandler(OpCode.SET, itrap);
			msgProcessor.addInstructionHandler(OpCode.SHADE, itrap);
			msgProcessor.addInstructionHandler(OpCode.TRANSFER, itrap);
			msgProcessor.addInstructionHandler(OpCode.TRANSFORM, itrap);
		}

		this.state = State.READY;
	}
	
	/** Prepare this screen-shooter for message-processing. */
	public void prepare()
	{
		if (state != State.READY)
			throw new IllegalStateException("Attempt to call ScreenShooter.prepare() multiple times!");
		
		log.info("Start capturing data for offscreen-canvas.");
		
		// All timestamps for recieved messages will
		// be calculated relative to this timepoint!
		stopwatch.start();
		
		msgWorker.start();
		state = State.PREPARED;
	}
	
	/** Terminate the message-processing. */
	public void finish()
	{
		if (state != State.PREPARED) {
			log.warn("Skip finishing an unprepared screen-shooter!");
			return;
		}
		
		msgWorker.terminate(true);
		state = State.FINISHED;
	}

	/** Returns true when screen-shooter was finished, else false. */
	public final boolean isFinished()
	{
		return (state == State.FINISHED);
	}
	
	/** Request a new screenshot. */
	public void takeScreenshot()
	{
		msgProcessor.postMessage(SourceType.INTERNAL, stopwatch.timems(), SCREENSHOT_MESSAGE);
	}
	
	/** Returns true, when next screenshot is available, else false. */
	public boolean hasNextScreenshot()
	{
		return screenshots.hasNextScreenshot();
	}
	
	/** Returns new screenshot when available, else null. */
	public byte[] getNextScreenshot()
	{
		return screenshots.getNextScreenshot();
	}
	
	
	/* ========== IGuacInterceptor Implementation ========== */
	
	@Override
	public void onBeginConnection() throws IOException
	{
		// Do nothing!
	}

	@Override
	public void onEndConnection() throws IOException
	{
		// Do nothing!
	}

	@Override
	public boolean onClientMessage(CharArrayWrapper message) throws Exception
	{
		return true;  // Forward the message unmodified
	}

	@Override
	public boolean onServerMessage(CharArrayWrapper message) throws Exception
	{
		// Pass the message unmodified to the processor and forward it
		if (msgProcessor.postMessage(SourceType.SERVER, stopwatch.timems(), message) == 1)
			msgWorker.wakeup();
		
		return true;
	}
}
