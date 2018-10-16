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

package de.bwl.bwfla.common.services.guacplay.protocol.handler;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.nio.IntBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.CompositeMode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.LineCapStyle;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.LineJoinStyle;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.VSyncType;
import de.bwl.bwfla.common.services.guacplay.events.EventSink;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.events.IGuacEventListener;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.graphics.ScreenObserver;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionBuilder;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionParserException;
import de.bwl.bwfla.common.services.guacplay.util.Base64;
import de.bwl.bwfla.common.services.guacplay.util.CharArrayBuffer;
import de.bwl.bwfla.common.services.guacplay.util.ConditionVariable;
import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;


/** A handler for the custom vsync-instruction. */
public class VSyncInstrHandler extends InstructionHandler implements IGuacEventListener
{
	// Member fields
	private final EventSink esink;
	private final ICharArrayConsumer client;
	private final OffscreenCanvas canvas;
	private final BufferedImage image;
	private final InstructionBuilder ibuilder;
	private final ConditionVariable waitcond;
	private final ScreenObserver observer;
	private final GuacEvent vsbegin;
	private final GuacEvent vsend;
	private VSyncInstrParser iparser;
	private volatile boolean exitflag;
	
	
	/** Tiemout for pixel-comparissons (in ms). */
	private static final long RETRY_TIMEOUT = 250L;
	
	/** ID of the overlay layer. */
	private static final int OVERLAY_LAYER = 3;
	
	// Create reusable raw-instructions
	private static final char[] INSTR_CLEAR_OVERLAY;
	private static final char[] INSTR_CSTROKE_RED;
	private static final char[] INSTR_CSTROKE_GREEN;
	static {
		final InstructionBuilder ibuilder = new InstructionBuilder(512);
		final int thickness = 4;
		
		// Construct an instruction for clearing the overlay's content
		ibuilder.start(OpCode.DISPOSE);
		ibuilder.addArgument(OVERLAY_LAYER);
		ibuilder.finish();
		
		INSTR_CLEAR_OVERLAY = ibuilder.toCharArray();
		
		// Construct a cstroke-instruction for red rectangle
		ibuilder.start(OpCode.CSTROKE);
		ibuilder.addArgument(CompositeMode.SRC_OVER);
		ibuilder.addArgument(OVERLAY_LAYER);
		ibuilder.addArgument(LineCapStyle.ROUND);
		ibuilder.addArgument(LineJoinStyle.ROUND);
		ibuilder.addArgument(thickness);
		ibuilder.addArgument(255);
		ibuilder.addArgument(0);
		ibuilder.addArgument(0);
		ibuilder.addArgument(255);
		ibuilder.finish();
		
		INSTR_CSTROKE_RED = ibuilder.toCharArray();

		// Construct a cstroke-instruction for green rectangle
		ibuilder.start(OpCode.CSTROKE);
		ibuilder.addArgument(CompositeMode.SRC_OVER);
		ibuilder.addArgument(OVERLAY_LAYER);
		ibuilder.addArgument(LineCapStyle.ROUND);
		ibuilder.addArgument(LineJoinStyle.ROUND);
		ibuilder.addArgument(thickness);
		ibuilder.addArgument(0);
		ibuilder.addArgument(255);
		ibuilder.addArgument(0);
		ibuilder.addArgument(255);
		ibuilder.finish();

		INSTR_CSTROKE_GREEN = ibuilder.toCharArray();
	}
	
	
	/** Constructor */
	public VSyncInstrHandler(OffscreenCanvas canvas, ICharArrayConsumer client, EventSink esink)
	{
		super(ExtOpCode.VSYNC);
		
		this.esink = esink;
		this.client = client;
		this.canvas = canvas;
		this.image = canvas.newBufferedImage(GuacDefs.VSYNC_RECT_WIDTH, GuacDefs.VSYNC_RECT_HEIGHT);
		this.ibuilder = new InstructionBuilder(512);
		this.waitcond = new ConditionVariable();
		this.observer = new ScreenObserver(waitcond);
		this.vsbegin = new GuacEvent(EventType.VSYNC_BEGIN, this);
		this.vsend = new GuacEvent(EventType.VSYNC_END, this);
		this.iparser = null;
		this.exitflag = false;
		
		canvas.addObserver(observer);
	}
	
	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		// Begin of vsync-processing
		esink.consume(vsbegin);
		
		// Create parser from typeid
		final int typeid = instruction.argAsInt(0);
		if ((iparser == null) || (iparser.getTypeId() != typeid))
			iparser = VSyncInstrHandler.newVSyncInstrParser(typeid);
		
		// Parse vsync's arguments
		iparser.parse(instruction);
		
		final int xpos = iparser.getSyncRectPosX();
		final int ypos = iparser.getSyncRectPosY();
		final int width = iparser.getSyncRectWidth();
		final int height = iparser.getSyncRectHeight();
		
		char[] rectinstr = null;
		
		// Send visual feedback, when client connected
		if (client != null) {
			// Mark the vsync-area
			ibuilder.start(OpCode.RECT);
			ibuilder.addArgument(OVERLAY_LAYER);
			ibuilder.addArgument(xpos);
			ibuilder.addArgument(ypos);
			ibuilder.addArgument(width);
			ibuilder.addArgument(height);
			ibuilder.finish();
			
			rectinstr = ibuilder.toCharArray();
			
			// Clear the overlay-layer's content and send red rectangle to client
			client.consume(INSTR_CLEAR_OVERLAY, 0, INSTR_CLEAR_OVERLAY.length);
			client.consume(rectinstr, 0, rectinstr.length);
			client.consume(INSTR_CSTROKE_RED, 0, INSTR_CSTROKE_RED.length);
		}
		
		boolean enabled = false;
		
		// Wait, until vsync matches or exit requested
		while (!exitflag) {
			// Render the current content
			synchronized (canvas) {
				canvas.render(xpos, ypos, width, height, image);
			}
			
			// Compare the rendered image with target
			if (iparser.match(image, 0, 0, width, height))
				break;  // Match detected!
			
			// No match, retry later!
			
			if (!enabled) {
				// Enable the screen-observer and update its rectangle once
				observer.setScreenArea(xpos, ypos, xpos + width, ypos + height);
				enabled = true;
			}
			
			waitcond.await(RETRY_TIMEOUT);  // Wait and retry
		}
		
		// Disable the observer
		if (enabled)
			observer.reset();
		
		if (client != null) {
			// Send green rectangle to client
			client.consume(rectinstr, 0, rectinstr.length);
			client.consume(INSTR_CSTROKE_GREEN, 0, INSTR_CSTROKE_GREEN.length);
		}
		
		// End of vsync-processing
		esink.consume(vsend);
	}

	@Override
	public void onGuacEvent(GuacEvent event)
	{
		final int type = event.getType();
		
		if (type == EventType.TERMINATION)
			exitflag = true;
		
		else if ((type == EventType.TRACE_END) && (client != null)) {
			try {
				// Send an instruction to clear the overlay of this handler.
				client.consume(INSTR_CLEAR_OVERLAY, 0, INSTR_CLEAR_OVERLAY.length);
			}
			catch (Exception exception) {
				// Ignore it!
			}
		}
	}
	
	
	/** Construct a new parser from specified typeid. */
	private static VSyncInstrParser newVSyncInstrParser(int typeid)
	{
		VSyncInstrParser parser = null;
		
		switch (typeid)
		{
			case VSyncType.EQUAL_PIXELS: {
				parser = new EqualPixelsVSyncInstrParser(GuacDefs.VSYNC_PIXEL_THRESHOLD);
				break;
			}
			
			case VSyncType.AVERAGE_COLOR: {
				parser = new AverageColorVSyncInstrParser(GuacDefs.VSYNC_COLOR_THRESHOLD);
				break;
			}
			
			default:
				throw new IllegalArgumentException("Not supported type argument specified: " + typeid);	
		}
		
		final Logger log = LoggerFactory.getLogger(VSyncInstrHandler.class);
		log.info("Using vsync-handler implementing '{}' algorithm.", parser.getTypeName());
		
		return parser;
	}
}


/** Internal base class for vsync-instruction parsers */
abstract class VSyncInstrParser
{
	private final String typename;
	private final int typeid;
	
	// Sync rectangle arguments
	protected int syncRectPosX;
	protected int syncRectPosY;
	protected int syncRectWidth;
	protected int syncRectHeight;
	
	/** Constructor */
	protected VSyncInstrParser(String typename, int typeid)
	{
		this.typename = typename;
		this.typeid = typeid;
	}
	
	public final String getTypeName()
	{
		return typename;
	}
	
	public final int getTypeId()
	{
		return typeid;
	}
	
	public int getSyncRectPosX()
	{
		return syncRectPosX;
	}

	public int getSyncRectPosY()
	{
		return syncRectPosY;
	}

	public int getSyncRectWidth()
	{
		return syncRectWidth;
	}

	public int getSyncRectHeight()
	{
		return syncRectHeight;
	}

	/** Parse the instruction's arguments 
	 * @throws InstructionParserException */
	public abstract void parse(Instruction instr) throws IOException, InstructionParserException;
	
	/**
	 * Compare the internal vsync-data with the pixels inside of the specified rectangle.
	 * @param image The image to compare with.
	 * @param xpos The x-position of the rectangle.
	 * @param ypos The y-position of the rectangle.
	 * @param width The rectangle's width.
	 * @param height The rectangle's height.
	 * @return true when the pixels match with vsync-data, else false.
	 */
	public abstract boolean match(BufferedImage image, int xpos, int ypos, int width, int height);
}


/** Parser for vsync-instructions using EqualPixels algorithm. */
final class EqualPixelsVSyncInstrParser extends VSyncInstrParser
{
	private final CharArrayBuffer wrapper;
	private final float threshold;
	private int neqmax;
	
	// Instruction's data
	private IntBuffer syncRectPixels;
	
	/** Constructor */
	public EqualPixelsVSyncInstrParser(float threshold)
	{
		super("EqualPixels", VSyncType.EQUAL_PIXELS);
		
		this.wrapper = new CharArrayBuffer();
		this.threshold = threshold;
		this.neqmax = 0;
		this.syncRectPixels = null;
	}

	@Override
	public void parse(Instruction instruction) throws IOException, InstructionParserException
	{
		this.syncRectPosX = instruction.argAsInt(1);
		this.syncRectPosY = instruction.argAsInt(2);
		this.syncRectWidth = instruction.argAsInt(3);
		this.syncRectHeight = instruction.argAsInt(4);
		
		instruction.argAsCharArray(5, wrapper);
		this.syncRectPixels = Base64.decode(wrapper, syncRectPixels);
		
		// Update the threshold values
		final int length = syncRectPixels.remaining();
		final float eqmin = threshold * ((float) length);
		this.neqmax = length - ((int) eqmin);
	}
	
	@Override
	public boolean match(BufferedImage image, int xpos, int ypos, int width, int height)
	{
		final int[] buf1 = syncRectPixels.array();
		final int length = syncRectPixels.remaining();
		
		// Ensure the correct number of pixels!
		if ((width * height) != length) {
			String message = "Invalid number of pixels for the visual-sync! "
					+ "Expected: " + length + ", Actual: " + (width * height);
			throw new IllegalArgumentException(message);
		}

		// Get image's pixel-data, bypassing the whole Java2D abstraction!
		final DataBufferInt imgbuf = (DataBufferInt) image.getRaster().getDataBuffer();
		final int[] buf2 = imgbuf.getData();
		final int skip = image.getWidth() - width;
		int off2 = imgbuf.getOffset() + (ypos * image.getWidth()) + xpos;
		int off1 = 0;
		int neqnum = 0;

		// Compare the pixels inside the specified rectangle
		for (int i = 0; i < height; ++i) {
			// Compare y-th scanline
			final int maxoff = off1 + width;
			while (off1 < maxoff) {
				if (buf1[off1] != buf2[off2])
					++neqnum;  // Not equal pixel found!

				++off1;
				++off2;
			}

			off2 += skip;  // Goto next scanline
		}

		// Are enough pixels equal?
		return (neqnum < neqmax);
	}
}


/** Parser for vsync-instructions using AverageColor algorithm. */
final class AverageColorVSyncInstrParser extends VSyncInstrParser
{
	private final float threshold;
	private final float[] avgs;
	private final int[] sums;
	private int[] samples;
	
	public static final int NUM_SAMPLES = 3;
	
	/** Constructor */
	public AverageColorVSyncInstrParser(float threshold)
	{
		super("AverageColor", VSyncType.AVERAGE_COLOR);

		this.threshold = threshold;
		this.avgs = new float[NUM_SAMPLES];
		this.sums = new int[NUM_SAMPLES];
		this.samples = null;
	}

	@Override
	public void parse(Instruction instruction) throws IOException, InstructionParserException
	{
		syncRectPosX = instruction.argAsInt(1);
		syncRectPosY = instruction.argAsInt(2);
		syncRectWidth = instruction.argAsInt(3);
		syncRectHeight = instruction.argAsInt(4);
		
		// Parse the average samples
		for (int i = 0; i < NUM_SAMPLES; ++i) {
			String hexstr = instruction.argAsString(i + 5);
			avgs[i] = AverageColorVSyncInstrParser.hexToFloat(hexstr);
		}
	}
	
	@Override
	public boolean match(BufferedImage image, int xpos, int ypos, int width, int height)
	{
		final int xmax = xpos + width;
		final int ymax = ypos + height;
		
		final DataBuffer buffer = image.getRaster().getDataBuffer();
		final SampleModel model = image.getSampleModel();

		for (int i = 0; i < NUM_SAMPLES; ++i)
			sums[i] = 0;
		
		// For all pixels, sum all samples
		for (int y = ypos; y < ymax; ++y) {
			for (int x = xpos; x < xmax; ++x) {
				samples = model.getPixel(x, y, samples, buffer);
				for (int i = 0; i < NUM_SAMPLES; ++i)
					sums[i] += samples[i];
			}
		}

		// Compute and compare averages
		final float count = (float) (width * height);
		for (int i = 0; i < NUM_SAMPLES; ++i) {
			final float avg = (float) sums[i] / count;
			final float diff = avgs[i] - avg;
			if (Math.abs(diff) > threshold)
				return false;
		}

		return true;
	}
	
	private static float hexToFloat(String hexstr)
	{
		int bits = Integer.parseInt(hexstr, 16);
		return Float.intBitsToFloat(bits);
	}
}
