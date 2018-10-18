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

package de.bwl.bwfla.common.services.guacplay.protocol;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.nio.CharBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.VSyncType;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.util.Base64;


/** A generator for the custom visual-sync instruction. */
public abstract class VSyncInstrGenerator
{
	// Member fields
	protected final OffscreenCanvas canvas;
	protected final InstructionBuilder ibuilder;
	protected final int typeid;
	
	/** Constructor */
	protected VSyncInstrGenerator(OffscreenCanvas canvas, int typeid)
	{
		this.canvas = canvas;
		this.ibuilder = new InstructionBuilder(1024);
		this.typeid = typeid;
	}

	/** Returns the generator's name */
	protected abstract String name();
	
	/**
	 * Generate and return the visual-sync instruction.
	 * @param syncPosX The x-position of the sync-point.
	 * @param syncPosY The y-position of the sync-point.
	 * @param outinstr Contains the generated instruction.
	 */
	public abstract void generate(int syncPosX, int syncPosY, Instruction outinstr);

	
	/** Construct a new vsync-generator of the specified type. */
	public static VSyncInstrGenerator construct(OffscreenCanvas canvas, int typeid, boolean logging)
	{
		VSyncInstrGenerator generator = null;
		
		switch (typeid)
		{
			case VSyncType.EQUAL_PIXELS: {
				generator = new EqualPixelsVSyncInstrGenerator(canvas);
				break;
			}
			
			case VSyncType.AVERAGE_COLOR: {
				generator = new AverageColorVSyncInstrGenerator(canvas);
				break;
			}
			
			default:
				throw new IllegalArgumentException("Not supported type argument specified: " + typeid);	
		}
		
		if (logging) {
			final Logger log = LoggerFactory.getLogger(VSyncInstrGenerator.class);
			log.info("Using vsync-generator implementing '{}' algorithm.", generator.name());
		}
		
		return generator;
	}
}


/** Generator for vsync-instructions using the EqualPixels algorithm. */
final class EqualPixelsVSyncInstrGenerator extends VSyncInstrGenerator
{
	private final BufferedImage vsimage;
	private final VSyncData vsdata;
	
	// Internal Constants
	private static final int VSYNC_RECT_HALF_WIDTH  = GuacDefs.VSYNC_RECT_WIDTH  / 2;
	private static final int VSYNC_RECT_HALF_HEIGHT = GuacDefs.VSYNC_RECT_HEIGHT / 2;
	
	/** Constructor */
	public EqualPixelsVSyncInstrGenerator(OffscreenCanvas canvas)
	{
		super(canvas, VSyncType.EQUAL_PIXELS);
		
		this.vsimage = canvas.newBufferedImage(GuacDefs.VSYNC_RECT_WIDTH, GuacDefs.VSYNC_RECT_HEIGHT);
		this.vsdata = new VSyncData();
	}

	@Override
	protected String name()
	{
		return "EqualPixels";
	}
	
	@Override
	public void generate(int syncPosX, int syncPosY, Instruction outinstr)
	{
		// Compute the vsync rectangle
		final int xpos = Math.max(0, syncPosX - VSYNC_RECT_HALF_WIDTH);
		final int ypos = Math.max(0, syncPosY - VSYNC_RECT_HALF_HEIGHT);
		final int xmax = Math.min(canvas.getWidth() , syncPosX + VSYNC_RECT_HALF_WIDTH);
		final int ymax = Math.min(canvas.getHeight(), syncPosY + VSYNC_RECT_HALF_HEIGHT);
		final int width  = xmax - xpos;
		final int height = ymax - ypos;

		// Compose the final image inside the sync-area
		synchronized (canvas) {
			canvas.render(xpos, ypos, width, height, vsimage);
		}

		// Encode the vsync-data to Base64
		vsdata.encode(vsimage, 0, 0, width, height);

		// Serialize the instruction
		ibuilder.start(ExtOpCode.VSYNC, outinstr);
		ibuilder.addArgument(typeid);
		ibuilder.addArgument(xpos);
		ibuilder.addArgument(ypos);
		ibuilder.addArgument(width);
		ibuilder.addArgument(height);
		ibuilder.addArgument(vsdata.array(), 0, vsdata.length());
		ibuilder.finish();
	}
	
	
	/** This class represents the data, needed for the visual-synchronization. */
	private static final class VSyncData
	{
		private CharBuffer charbuf;
		private int[] intbuf;
		
		/** Constructor */
		public VSyncData()
		{
			this.charbuf = null;
			this.intbuf = null;
		}
		
		/** Copy the specified image and encode it to Base64. */
		public void encode(BufferedImage image, int xpos, int ypos, int width, int height)
		{
			final int length = width * height;
			if ((intbuf == null) || (intbuf.length < length)) {
				// Resize the buffer for pixels
				intbuf = new int[length];
			}
			
			// Get pixel-data, bypassing the whole Java2D abstraction!
			final DataBufferInt pixbuf = (DataBufferInt) image.getRaster().getDataBuffer();
			final int[] pixels = pixbuf.getData();
			final int imageWidth = image.getWidth();
			int pixoff = pixbuf.getOffset() + (ypos * imageWidth) + xpos;
			int intoff = 0;
			
			// Copy the pixels inside the specified rectangle
			for (int i = 0; i < height; ++i) {
				// Copy y-th scanline
				System.arraycopy(pixels, pixoff, intbuf, intoff, width);
				
				// Offsets for the next scanline
				pixoff += imageWidth;
				intoff += width;
			}
			
			// Encode the pixel-data into Base64 format
			charbuf = Base64.encode(intbuf, 0, length, charbuf);
		}
		
		/** Returns the encoded data. */
		public char[] array()
		{
			return charbuf.array();
		}
		
		/** Returns the encoded length. */
		public int length()
		{
			return charbuf.length();
		}
	}
}


/** Generator for vsync-instructions using the AverageColor algorithm. */
final class AverageColorVSyncInstrGenerator extends VSyncInstrGenerator
{
	private final BufferedImage vsimage;
	private final VSyncData vsdata;
	
	// Internal Constants
	private static final int VSYNC_RECT_HALF_WIDTH  = GuacDefs.VSYNC_RECT_WIDTH  / 2;
	private static final int VSYNC_RECT_HALF_HEIGHT = GuacDefs.VSYNC_RECT_HEIGHT / 2;
	
	/** Constructor */
	public AverageColorVSyncInstrGenerator(OffscreenCanvas canvas)
	{
		super(canvas, VSyncType.AVERAGE_COLOR);
		
		this.vsimage = canvas.newBufferedImage(GuacDefs.VSYNC_RECT_WIDTH, GuacDefs.VSYNC_RECT_HEIGHT);
		this.vsdata = new VSyncData();
	}

	@Override
	protected String name()
	{
		return "AverageColor";
	}
	
	@Override
	public void generate(int syncPosX, int syncPosY, Instruction outinstr)
	{
		// Compute the vsync rectangle
		final int xpos = Math.max(0, syncPosX - VSYNC_RECT_HALF_WIDTH);
		final int ypos = Math.max(0, syncPosY - VSYNC_RECT_HALF_HEIGHT);
		final int xmax = Math.min(canvas.getWidth() , syncPosX + VSYNC_RECT_HALF_WIDTH);
		final int ymax = Math.min(canvas.getHeight(), syncPosY + VSYNC_RECT_HALF_HEIGHT);
		final int width  = xmax - xpos;
		final int height = ymax - ypos;

		// Compose the final image inside the sync-area
		synchronized (canvas) {
			canvas.render(xpos, ypos, width, height, vsimage);
		}

		// Compute the vsync-data
		vsdata.compute(vsimage, 0, 0, width, height);

		// Serialize the instruction
		ibuilder.start(ExtOpCode.VSYNC, outinstr);
		ibuilder.addArgument(typeid);
		ibuilder.addArgument(xpos);
		ibuilder.addArgument(ypos);
		ibuilder.addArgument(width);
		ibuilder.addArgument(height);
		for (int i = 0; i < VSyncData.NUM_SAMPLES; ++i)
			ibuilder.addArgument(vsdata.sample(i));
		
		ibuilder.finish();
	}
	
	
	/** This class represents the data, needed for the visual-synchronization. */
	private static final class VSyncData
	{
		private final int[] sums;
		private final float[] avgs;
		
		public static final int NUM_SAMPLES = 3;
		
		/** Constructor */
		public VSyncData()
		{
			this.sums = new int[NUM_SAMPLES];
			this.avgs = new float[NUM_SAMPLES];
		}
		
		/** Compute the average color of the specified image. */
		public void compute(BufferedImage image, int xpos, int ypos, int width, int height)
		{
			final int xmax = xpos + width;
			final int ymax = ypos + height;
			
			final DataBuffer buffer = image.getRaster().getDataBuffer();
			final SampleModel model = image.getSampleModel();
			int[] samples = null;

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
			
			// Compute the averages
			final float count = (float) (width * height);
			for (int i = 0; i < NUM_SAMPLES; ++i)
				avgs[i] = (float) sums[i] / count;
		}
		
		/** Returns the average sample, as hex-string. */
		public String sample(int i)
		{
			int bits = Float.floatToIntBits(avgs[i]);
			return Integer.toHexString(bits);
		}
	}
}
