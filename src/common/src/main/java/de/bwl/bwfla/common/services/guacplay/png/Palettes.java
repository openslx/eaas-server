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


/** A base class for Color/Alpha palettes in an PNG image. */
abstract class Palette
{
	protected int[] buffer;
	protected int count;
	protected int capacity;
	

	protected Palette(int capacity)
	{
		this.buffer = new int[capacity];
		this.count = 0;
		this.capacity = capacity;
	}
	
	/** Set the entries from the specified {@link ByteBuffer}. */
	public abstract void setEntries(ByteBuffer pngbuf, int length) throws IOException;
	
	/** Get an entry at specified index. */
	public abstract int get(int index);
	
	/** Reset this palette for reusing. */
	public final void reset()
	{
		count = 0;
	}
	
	/** Returns true, if this palette constains valid entries, else false. */
	public final boolean isValid()
	{
		return (count > 0);
	}
}


/** This class represents the palette for color values in an PNG image. */
final class ColorPalette extends Palette
{
	public ColorPalette()
	{
		super(256);
	}

	@Override
	public int get(int index)
	{
		return buffer[index];
	}

	@Override
	public void setEntries(ByteBuffer pngbuf, int length) throws IOException
	{
		// Palette contains as entries the R, G and B
		// samples of a pixel as subsequent bytes.
		
		// TODO: optimize the division!
		count = length / 3;
		if (count > 256 || (length % 3) != 0)
			throw new IOException("PLTE chunk has wrong length!");
		
		// Construct the palette entries
		int red, green, blue;
		for (int i = 0; i < count; ++i) {
			red   = MathUtils.asUByte(pngbuf.get());
			green = MathUtils.asUByte(pngbuf.get());
			blue  = MathUtils.asUByte(pngbuf.get());
			buffer[i] = PngDefs.toArgb(red, green, blue);
		}
	}
}


/** This class represents the palette for alpha values in an PNG image. */
final class AlphaPalette extends Palette
{
	public AlphaPalette()
	{
		super(256);
	}

	@Override
	public int get(int index)
	{	
		// The palette can contain fewer entries than requsted.
		// If no alpha value is available, assume it as opaque.
		
		if (index < count)
			return buffer[index];
		
		return ALPHA_OPAQUE;
	}
	
	@Override
	public void setEntries(ByteBuffer pngbuf, int length) throws IOException
	{
		// Palette contains as entries the alpha sample of a pixel.
		
		count = length;
		
		// Construct the palette entries
		for (int i = 0; i < count; ++i)
			buffer[i] = MathUtils.asUByte(pngbuf.get());
	}
}

