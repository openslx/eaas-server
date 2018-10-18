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
import java.nio.ByteBuffer;

import de.bwl.bwfla.common.services.guacplay.util.MathUtils;
import static de.bwl.bwfla.common.services.guacplay.png.PngDefs.ALPHA_OPAQUE;
import static de.bwl.bwfla.common.services.guacplay.png.PngDefs.ALPHA_TRANSPARENT;


/** A common interface for all color-type coverters. */
abstract class ScanlineConverter
{
	public abstract void convert(ScanlineWrapper scanline, BufferedImageWrapper outimg);
	public void setTransparency(ByteBuffer pngbuf, int length) throws IOException { };
	public void reset() { };
}


/** The implementation of the converter for COLOR_GREY color type. */
final class ConverterGreyToArgb extends ScanlineConverter
{
	private int tpixel;    // Transparent pixel
	
	@Override
	public void convert(ScanlineWrapper scanline, BufferedImageWrapper outimg)
	{
		final int width = scanline.getWidth();
		int grey, alpha;
		
		if (tpixel < 0) {
			// No transparency information available,
			// assume that all pixels are opaque.
			for (int i = 0; i < width; ++i) {
				grey = scanline.getNextSample();
				outimg.setNextPixel(grey, grey, grey);
			}
		}
		else {
			// Transparency information is available!
			for (int i = 0; i < width; ++i) {
				grey = scanline.getNextSample();
				alpha = (grey != tpixel) ? ALPHA_OPAQUE : ALPHA_TRANSPARENT;
				outimg.setNextPixel(grey, grey, grey, alpha);
			}
		}
	}

	@Override
	public void setTransparency(ByteBuffer pngbuf, int length) throws IOException
	{
		if (length != 2)
			throw new IOException("Expected a length of 2 bytes, but recieved " + length + " bytes!");
		
		// Set the transparent pixel value
		final int msb = MathUtils.asUByte(pngbuf.get());
		final int lsb = MathUtils.asUByte(pngbuf.get());
		tpixel = (msb << 8) | lsb;
	}

	@Override
	public void reset()
	{
		tpixel = -1;
	}
}


/** The implementation of the converter for COLOR_GREY_ALPHA color type. */
final class ConverterGreyAlphaToArgb extends ScanlineConverter
{
	@Override
	public void convert(ScanlineWrapper scanline, BufferedImageWrapper outimg)
	{
		final int width = scanline.getWidth();
		int grey, alpha;
		
		for (int i = 0; i < width; ++i) {
			grey  = scanline.getNextSample();
			alpha = scanline.getNextSample();
			outimg.setNextPixel(grey, grey, grey, alpha);
		}
	}
}


/** The implementation of the converter for COLOR_RGB color type. */
final class ConverterRgbToArgb extends ScanlineConverter
{
	private int tpixel;    // Transparent pixel
	
	@Override
	public void convert(ScanlineWrapper scanline, BufferedImageWrapper outimg)
	{
		final int width = scanline.getWidth();
		int red, green, blue;
		
		if (tpixel < 0) {
			// No transparency information available,
			// assume that all pixels are opaque.
			for (int i = 0; i < width; ++i) {
				red   = scanline.getNextSample();
				green = scanline.getNextSample();
				blue  = scanline.getNextSample();
				outimg.setNextPixel(red, green, blue);
			}
		}
		else {
			// Transparency information is available!
			int rgb;
			for (int i = 0; i < width; ++i) {
				red   = scanline.getNextSample();
				green = scanline.getNextSample();
				blue  = scanline.getNextSample();
				rgb = PngDefs.toArgb(red, green, blue, ALPHA_TRANSPARENT);
				if (tpixel != rgb)
					outimg.setNextPixel(rgb, ALPHA_OPAQUE);
				else outimg.setNextPixel(rgb);
			}
		}
	}

	@Override
	public void setTransparency(ByteBuffer pngbuf, int length) throws IOException
	{
		if (length != 6)
			throw new IOException("Expected a length of 6 bytes, but recieved " + length + " bytes!");
		
		// Set the transparent pixel value
		final int red = pngbuf.getShort();
		final int green = pngbuf.getShort();
		final int blue = pngbuf.getShort();
		tpixel = PngDefs.toArgb(red, green, blue, ALPHA_TRANSPARENT);
	}

	@Override
	public void reset()
	{
		tpixel = -1;
	}
}


/** The implementation of the converter for COLOR_RGB_ALPHA color type. */
final class ConverterRgbaToArgb extends ScanlineConverter
{
	@Override
	public void convert(ScanlineWrapper scanline, BufferedImageWrapper outimg)
	{
		final int width = scanline.getWidth();
		int red, green, blue, alpha;
		
		for (int i = 0; i < width; ++i) {
			red   = scanline.getNextSample();
			green = scanline.getNextSample();
			blue  = scanline.getNextSample();
			alpha = scanline.getNextSample();
			outimg.setNextPixel(red, green, blue, alpha);
		}
	}
}


/** The implementation of the converter for COLOR_INDEXED color type. */
final class ConverterIndexedToArgb extends ScanlineConverter
{
	private ColorPalette cpalette;
	private AlphaPalette apalette;
	
	public ConverterIndexedToArgb()
	{
		this.cpalette = new ColorPalette();
		this.apalette = new AlphaPalette();
	}
	
	@Override
	public void convert(ScanlineWrapper scanline, BufferedImageWrapper outimg)
	{
		final int width = scanline.getWidth();
		int index, color, alpha;
		
		if (apalette.isValid()) {
			// Alpha values are available
			for (int i = 0; i < width; ++i) {
				index = scanline.getNextSample();
				color = cpalette.get(index);
				alpha = apalette.get(index);
				outimg.setNextPixel(color, alpha);
			}
		}
		else {
			// Assume opaque pixel values
			for (int i = 0; i < width; ++i) {
				index = scanline.getNextSample();
				color = cpalette.get(index);
				outimg.setNextPixel(color);
			}
		}
	}

	@Override
	public void setTransparency(ByteBuffer buffer, int length) throws IOException
	{
		apalette.setEntries(buffer, length);
	}

	public void setColorPalette(ByteBuffer buffer, int length) throws IOException
	{
		cpalette.setEntries(buffer, length);
	}
	
	@Override
	public void reset()
	{
		cpalette.reset();
		apalette.reset();
	}
}
