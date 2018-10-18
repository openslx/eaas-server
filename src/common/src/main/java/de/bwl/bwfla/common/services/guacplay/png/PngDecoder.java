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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Inflater;

import de.bwl.bwfla.common.services.guacplay.util.ImageSize;
import static de.bwl.bwfla.common.services.guacplay.png.PngDefs.*;


/** A reusable decoder for PNG images. */
public class PngDecoder
{
	/** Lookup table for number of samples per pixel, indexed by PNG color-type. */
	private static final int[] LUT_SPP = {
				1,	// COLOR_GREY
				0,	// undefined
				3,	// COLOR_RGB
				1,	// COLOR_INDEXED
				2,	// COLOR_GREY_ALPHA
				0,	// undefined
				4	// COLOR_RGB_ALPHA
			};
	
	// Member fields
	
	private final Inflater inflater;
	private final CRC32 crc32;
	private final ScanlineReader reader;
	private final ScanlineConverter[] converters;
	private final BufferedImageWrapper image;
	
	private boolean doCheckSignature;
	private boolean doCheckCRC;
	
	// IHDR fields
	private int width;
	private int height;
	private byte bps;
	private byte coltype;
	
	
	/** Constructor */
	public PngDecoder()
	{
		this.inflater = new Inflater();
		this.crc32 = new CRC32();
		this.reader = new ScanlineReader();
		this.converters = new ScanlineConverter[NUM_COLOR_TYPES];
		this.image = new BufferedImageWrapper();
		this.doCheckSignature = true;
		this.doCheckCRC = true;
		
		// Create the lookup-table for converters, indexed by color-type
		converters[COLOR_GREY] = new ConverterGreyToArgb();
		converters[COLOR_RGB] = new ConverterRgbToArgb();
		converters[COLOR_INDEXED] = new ConverterIndexedToArgb();
		converters[COLOR_GREY_ALPHA] = new ConverterGreyAlphaToArgb();
		converters[COLOR_RGB_ALPHA] = new ConverterRgbaToArgb();
	}

	/**
	 * Decodes the PNG image contained in the {@link ByteBuffer} into the {@link BufferedImage}.
	 * @param buffer The binary image encoded in the PNG format.
	 * @param outimg The output, containing the decoded image.
	 * @param size The size of subimage, representing the decoded image.
	 */
	public void decode(ByteBuffer buffer, BufferedImage outimg, ImageSize size) throws IOException
	{
		this.parseSignature(buffer);
		
		// Update the output image 
		image.setBufferedImage(outimg);
		
		// Prepare decompression of a single stream,
		// possibly spread over multiple IDAT chunks
		inflater.reset();
		
		// Process all chunks
		int clength, ctype;
		do {
			// Parse and check the length of the chunk
			clength = buffer.getInt();
			if (buffer.remaining() < clength) {
				String message = "An incomplete png-chunk encountered! Expected " + clength 
						+ " bytes, but only " + buffer.remaining() + " bytes are available.";
				throw new IOException(message);
			}
			
			// Check the chunk's CRC32 checksum
			if (doCheckCRC)
				this.verifyChecksum(buffer, clength);
			
			// Parse chunk type and process supported chunks
			ctype = buffer.getInt();
			switch (ctype) {
				case CHUNK_IDAT:
					this.onChunkIDAT(buffer, clength);
					break;
				case CHUNK_IHDR:
					this.onChunkIHDR(buffer, clength);
					break;
				case CHUNK_PLTE:
					this.onChunkPLTE(buffer, clength);
					break;
				case CHUNK_tRNS:
					this.onChunktRNS(buffer, clength);
					break;
				default:
					// The chunk is not supported, ignore it!
					this.skip(buffer, clength);
			}
			
			// Skip the CRC32 checksum
			this.skip(buffer, CHUNK_CHECKSUM_LENGTH);
			
			// Stop, if the IEND chunk found.
		} while (ctype != CHUNK_IEND);
		
		// Update the decoded image's size
		size.set(width, height);
	}

	
	/* ==================== Internal Methods ==================== */

	private void skip(ByteBuffer buffer, int count)
	{
		buffer.position(buffer.position() + count);
	}
	
	private void verifyChecksum(ByteBuffer buffer, int clength) throws IOException
	{
		final int offset = buffer.position();
		final int length = CHUNK_TYPE_LENGTH + clength;
		
		crc32.reset();
		crc32.update(buffer.array(), offset, length);
		
		final int expChecksum = buffer.getInt(offset + length);
		final int curChecksum = (int) crc32.getValue();
		if (curChecksum != expChecksum) {
			String type = Integer.toHexString(buffer.getInt(offset));
			String message = "The CRC32-checksum of chunk " + type + " is invalid!";
			throw new IOException(message);
		}
	}
	
	private void parseSignature(ByteBuffer buffer) throws IOException
	{	
		// Should the signature be checked?
		if (!doCheckSignature) {
			this.skip(buffer, PNG_SIGNATURE_LENGTH);
			return;
		}

		// Check every byte of the signature
		for (int i = 0; i < PNG_SIGNATURE_LENGTH; ++i) {
			if (buffer.get() != PNG_SIGNATURE[i])
				throw new IOException("Invalid png-signature encountered!");
		}
	}
	
	private void onChunkIHDR(ByteBuffer buffer, int clength) throws IOException
	{
		// Ensure the correct size
		if (clength != CHUNK_IHDR_LENGTH)
			throw new IOException("Invalid png-chunk! An IHDR chunk was expected.");
					
		// Read the image parameters
		width = buffer.getInt();
		height = buffer.getInt();
		bps = buffer.get();
		coltype = buffer.get();
		
		// Ensure, that color type is valid
		if ((coltype < COLOR_GREY) || (coltype > COLOR_RGB_ALPHA) || (LUT_SPP[coltype] == 0))
			throw new IOException("Invalid color type found: " + coltype);
		
		// Ensure correct bit-depth (number of bits-per-sample)
		if (bps < 1 || bps > 16) {
			String message = "Requested " + bps + " bits per sample, but only "
					+ "1, 2, 4, 8 and 16 bits are currently supported!";
			throw new UnsupportedOperationException(message);
		}

		// Skip compression method and filter method types,
		// since they don't change in the current standard!
		this.skip(buffer, 2);
		
		// Ensure that interlacing mode is disabled
		if (buffer.get() != INTERLACING_DISABLED) {
			String message = "Interlacing requested. This is currently not supported!";
			throw new UnsupportedOperationException(message);
		}
		
		// Prepare the internal helpers for reuse
		final int spp = LUT_SPP[coltype];
		reader.prepare(width, spp, bps);
		converters[coltype].reset();
	}
	
	private void onChunkPLTE(ByteBuffer buffer, int clength) throws IOException
	{
		if (coltype != COLOR_INDEXED) {
			// Ignore this chunk!
			this.skip(buffer, clength);
			return;
		}
		
		// Update the color-palette
		ConverterIndexedToArgb converter = (ConverterIndexedToArgb) converters[COLOR_INDEXED];
		converter.setColorPalette(buffer, clength);
	}
	
	private void onChunktRNS(ByteBuffer buffer, int clength) throws IOException
	{
		// tRNS chunk is supported only for color-types:
		// COLOR_GREY, COLOR_RGB and COLOR_INDEXED
		
		if (coltype > COLOR_INDEXED) {
			// Ignore this chunk!
			this.skip(buffer, clength);
			return;
		}
		
		// Update the transparency information
		ScanlineConverter converter = converters[coltype];
		converter.setTransparency(buffer, clength);
	}
	
	private void onChunkIDAT(ByteBuffer buffer, final int clength) throws IOException
	{
		// Update the inflater's input
		inflater.setInput(buffer.array(), buffer.arrayOffset() + buffer.position(), clength);
		
		final ScanlineConverter converter = converters[coltype];
		do {
			// Decompress and process one scanline at a time
			final ScanlineWrapper scanline = reader.readNext(inflater);
			if (scanline == null) {
				// More data is needed, wait
				// for the next IDAT chunk!
				break;
			}
			
			// One scanline was decompressed successfully
			converter.convert(scanline, image);
			
			// Update the position of the next scanline!
			image.setPosition(0, reader.getNumScanlinesRead());
			
			// Stop, if the whole chunk was processed
		} while (inflater.getRemaining() != 0);
		
		// Safety check
		if (inflater.getRemaining() != 0)
			throw new IOException("Could not decompress an IDAT chunk!");
		
		// Reposition the buffer
		this.skip(buffer, clength);
	}
}
