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
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;


/**
 * A wrapper class for directly setting the pixel values in a {@link BufferedImage},
 * without going through the whole abstraction intefaces provided by the underlying 
 * {@link java.awt.image.SampleModel SampleModel}s.
 * <p>
 * <b> WARNING: </b>
 * This implementation supports only the {@link java.awt.image.DirectColorModel DirectColorModel} 
 * and the underlying pixel-storage must be {@link java.awt.image.DataBufferInt DataBufferInt}.
 */
final class BufferedImageWrapper
{
	private DataBuffer pixels;
	private int width;
	private int offset;
	
	/** Constructor */
	public BufferedImageWrapper()
	{
		this.pixels = null;
		this.width = 0;
		this.offset = 0;
	}
	
	/** Set the underlying {@link BufferedImage}, whose pixels should be modified. */
	public void setBufferedImage(BufferedImage image)
	{
		WritableRaster raster = image.getRaster();
		pixels = raster.getDataBuffer();
		width = raster.getWidth();
		offset = 0;
	}
	
	/** Set the start position, from where the pixel values will be modified. */
	public void setPosition(int xpos, int ypos)
	{
		offset = ypos * width + xpos;
	}
	
	/** Set the pixel color to the specified samples. */
	public void setNextPixel(int red, int green, int blue, int alpha)
	{
		int value = PngDefs.toArgb(red, green, blue, alpha);
		pixels.setElem(offset, value); ++offset;
	}
	
	/** Set an opaque pixel color to the specified samples. */
	public void setNextPixel(int red, int green, int blue)
	{
		int value = PngDefs.toArgb(red, green, blue);
		pixels.setElem(offset, value); ++offset;
	}
	
	/** Set the pixel color to the specified values. */
	public void setNextPixel(int rgb, int alpha)
	{
		int value = PngDefs.toArgb(rgb, alpha);
		pixels.setElem(offset, value); ++offset;
	}
	
	/** Set the pixel color to the specified value. */
	public void setNextPixel(int color)
	{
		pixels.setElem(offset, color);
		++offset;
	}
}
