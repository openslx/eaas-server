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

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static de.bwl.bwfla.common.services.guacplay.png.PngDefs.FILTER_INVALID;
import static de.bwl.bwfla.common.services.guacplay.png.PngDefs.FILTER_NONE;
import static de.bwl.bwfla.common.services.guacplay.png.PngDefs.FILTER_PAETH;


/** Helper class for decompression and reconstruction of scanlines in an IDAT chunk. */
final class ScanlineReader
{	
	/** Stateless filter functors */
	private static final IScanlineFilter[] LUT_FILTERS = {
				new ScanlineFilterNone(),		// FILTER_NONE
				new ScanlineFilterSub(),		// FILTER_SUB
				new ScanlineFilterUp(),			// FILTER_UP
				new ScanlineFilterAverage(),	// FILTER_AVERAGE
				new ScanlineFilterPaeth()		// FILTER_PAETH
			};
	
	
	// Member fields
	
	private final ScanlineWrapper[] wrappers;
	private ScanlineWrapper wrapper;
	
	private int[][] scanlines;
	private int curid;
	private int delta;
	private int width;
	private int numScanlinesRead;
	
	private byte[] buffer;
	private byte ftype;
	private int offset;
	private int length;
	
	
	/** Constructor */
	public ScanlineReader()
	{
		// Look-up table for fast access to ScanlineWrappers
		this.wrappers = new ScanlineWrapper[3];
		wrappers[0] = new SampleReaderPacked();
		wrappers[1] = new SampleReader8Bit();
		wrappers[2] = new SampleReader16Bit();
				
		this.scanlines = new int[2][];
		this.curid = 0;
		this.delta = 0;
		this.width = 0;
		this.numScanlinesRead = 0;
		this.buffer = new byte[0];
		this.ftype = FILTER_INVALID;
		this.offset = 0;
		this.length = -1;
	}
	
	/**
	 * Prepare this {@link ScanlineReader} for reading new scanlines of specified
	 * width and with specified number of samples-per-pixel and bits-per-sample.
	 */
	public void prepare(int width, int spp, int bps)
	{
		final int n = bps >> 3;
		
		// Update the current ScanlineWrapper:
		// bps = 1,2,4 --> bps >> 3 = 0
		// bps = 8     --> bps >> 3 = 1
		// bps = 16    --> bps >> 3 = 2
		wrapper = wrappers[n];
		wrapper.prepare(bps);
		
		// For reconstruction of a byte of the pixel X the corresponding
		// byte in the pixel immediately before X should be used. But when
		// the bitdepth is less than 8, then simply use the previous byte.
		this.delta = (bps < 8) ? 1 : (spp * n);
		
		this.width = width;
		this.numScanlinesRead = 0;
		this.ftype = FILTER_INVALID;
		this.offset = 0;
		
		// Calculate the sanline's length in bytes
		final int numbits = spp * bps * width;
		this.length = (numbits >> 3);
		if ((numbits & 0x07) != 0) {
			// numbits is not a multiple of 8,
			// one additional byte is needed!
			++length;
		}
		
		// Update the buffers for scanlines
		if (buffer.length < length) {
			buffer = new byte[length];
			scanlines[0] = new int[length];
			scanlines[1] = new int[length];
		}
	}
	
	/**
	 * Read and decompress a new scanline from the specified {@link Inflater}.
	 * If more data is needed, then {@code null} is returned.
	 */
	public ScanlineWrapper readNext(Inflater inflater) throws IOException
	{
		try {
			// Decompress the filter-type first, that is
			// contained as first byte in a scanline
			if (ftype == FILTER_INVALID) {
				inflater.inflate(buffer, 0, 1);
				ftype = buffer[0];
			}
			
			// Decompress one scanline, skipping the already read bytes
			final int numBytesRequired = length - offset;
			final int numBytesInflated = inflater.inflate(buffer, offset, numBytesRequired);
			if (numBytesInflated != numBytesRequired) {
				// A full scanline could not be read or decompressed, save
				// the position inside the buffer and wait for more input
				offset = numBytesInflated;
				return null;
			}
			
			else offset = 0;
		}
		catch (DataFormatException dfe) {
			// Rethrow as IOException
			throw new IOException(dfe);
		}
		
		// Ensure a valid filter-type!
		if (ftype < FILTER_NONE || ftype > FILTER_PAETH)
			throw new IOException("Invalid filter type found: " + ftype);
		
		// Reconstruct the decompressed scanline using its filter
		final int[] curline = this.getCurScanline();
		final int[] preline = this.getPreScanline();
		final IScanlineFilter filter = LUT_FILTERS[ftype];
		filter.reconstruct(curline, preline, buffer, length, delta);
		
		// Prepare for next reconstruction
		ftype = FILTER_INVALID;
		
		// Swap the scanlines!
		curid = 1 - curid;
		++numScanlinesRead;
				
		// Return the reconstructed scanline
		wrapper.setScanline(curline, 0, width);
		return wrapper;
	}
	
	/** Returns the number of read scanlines. */
	public int getNumScanlinesRead()
	{
		return numScanlinesRead;
	}
	
	
	/* =============== Internal Methods =============== */
	
	private int[] getCurScanline()
	{
		return scanlines[curid];
	}
	
	private int[] getPreScanline()
	{
		return scanlines[1 - curid];
	}
}
