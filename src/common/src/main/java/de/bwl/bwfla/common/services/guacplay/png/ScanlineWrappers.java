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

package de.bwl.bwfla.common.services.guacplay.png;


/** An abstract class for reading/unpacking color-samples from a scanline. */
abstract class ScanlineWrapper
{
	// Member fileds
	protected int[] buffer;
	protected int bufpos;
	protected int width;
	
	protected ScanlineWrapper()
	{
		this.buffer = null;
		this.bufpos = 0;
		this.width = 0;
	}
	
	/** Returns the scanline's length. */
	public final int getWidth()
	{
		return width;
	}
	
	/** Set a new scanline data. */
	public void setScanline(int[] buffer, int offset, int width)
	{
		this.buffer = buffer;
		this.bufpos = offset;
		this.width = width;
	}
	
	/** Prepare the wrapper for reusing. */
	public void prepare(int bps)
	{
		// Do nothing!
	}
	
	/** Read/unpack the next sample from the underlying scanline. */
	public abstract int getNextSample();
}


/** SampleReader for 1, 2 and 4-bit samples, packed into one byte. */
class SampleReaderPacked extends ScanlineWrapper
{
	/** Look-up table for sample masks, indexed by the number of bits-per-sample. */
	private static final int[] LUT_SAMPLE_MASKS = {
			0x0,	// undefined
			0x1,	// 1 bps
			0x3,	// 2 bps
			0x0,	// undefined
			0xF		// 4 bps
		};

	// Member fileds
	private int samples;
	private int shift;
	private int mask;
	private int bps;
	
	/** Constructor */
	public SampleReaderPacked()
	{
		super();
		
		this.samples = 0;
		this.shift = 0;
		this.mask = 0;
		this.bps = 0;
	}
	
	@Override
	public void setScanline(int[] buffer, int offset, int width)
	{
		super.setScanline(buffer, offset, width);
		this.shift = 0;  // Resetting the reader
	}
	
	@Override
	public void prepare(int bps)
	{
		this.shift = 0;
		this.mask = LUT_SAMPLE_MASKS[bps];
		this.bps = bps;
	}
	
	@Override
	public int getNextSample()
	{
		// All samples in current byte unpacked?
		if (shift == 0) {
			// Yes, read the next byte
			samples = buffer[bufpos]; ++bufpos;
			shift = 8;
		}
		
		// Update the shift value
		shift -= bps;
		
		// Unpack the next sample
		return ((samples >> shift) & mask);
	}
}


/** SampleReader for 8-bit samples. */
class SampleReader8Bit extends ScanlineWrapper
{
	@Override
	public int getNextSample()
	{
		int sample = buffer[bufpos]; ++bufpos;
		return sample;
	}
}


/** SampleReader for 16-bit samples. */
class SampleReader16Bit extends ScanlineWrapper
{
	@Override
	public int getNextSample()
	{
		int msb = buffer[bufpos]; ++bufpos;
		int lsb = buffer[bufpos]; ++bufpos;
		return ((msb << 8) | lsb);
	}
}
