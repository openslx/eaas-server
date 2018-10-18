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


/** This class contains all constants and helper functions for PNG decoding. */
final class PngDefs
{
	/** The signature of a PNG image */
	public static final byte[] PNG_SIGNATURE = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
	
	/** Length of the png-signature in bytes */
	public static final int PNG_SIGNATURE_LENGTH = PNG_SIGNATURE.length;
	
	/** Length of the IHDR chunk in bytes */
	public static final int CHUNK_IHDR_LENGTH = 13;
	
	/** Length of the chunk's checksum in bytes */
	public static final int CHUNK_TYPE_LENGTH = 4;
	
	/** Length of the chunk's checksum in bytes */
	public static final int CHUNK_CHECKSUM_LENGTH = 4;
	
	/* Supported chunk types */
	public static final int CHUNK_IHDR  = 0x49484452;
	public static final int CHUNK_PLTE  = 0x504C5445;
	public static final int CHUNK_IDAT  = 0x49444154;
	public static final int CHUNK_IEND  = 0x49454E44;
	public static final int CHUNK_tRNS  = 0x74524E53;
	
	/* Supported color types */
	public static final byte COLOR_GREY			= 0;
	public static final byte COLOR_RGB			= 2;
	public static final byte COLOR_INDEXED		= 3;
	public static final byte COLOR_GREY_ALPHA	= 4;
	public static final byte COLOR_RGB_ALPHA	= 6;
	public static final byte NUM_COLOR_TYPES	= 7;
	
	/* Supported filter types */
	public static final byte FILTER_INVALID	 	= -1;
	public static final byte FILTER_NONE	 	= 0;
	public static final byte FILTER_SUB 	 	= 1;
	public static final byte FILTER_UP	 	 	= 2;
	public static final byte FILTER_AVERAGE	 	= 3;
	public static final byte FILTER_PAETH	 	= 4;
	public static final byte NUM_FILTER_TYPES	= 5;
	
	/* The interlace methods as defined by the standard. */
	public static final byte INTERLACING_DISABLED  = 0;
	public static final byte INTERLACING_ADAM7	   = 1;
	
	/* Shift values for 32-bit packed R, G, B and A samples. */
	public static final int SHIFT_ALPHA		= 8 * 3;
	public static final int SHIFT_RED		= 8 * 2;
	public static final int SHIFT_GREEN		= 8;
	public static final int SHIFT_BLUE		= 0;
	
	/* Special alpha values. */
	public static final int ALPHA_TRANSPARENT	  = 0;
	public static final int ALPHA_OPAQUE		  = 255;
	public static final int ALPHA_OPAQUE_SHIFTED  = ALPHA_OPAQUE << SHIFT_ALPHA;
	
	/* Masks */
	public static final int MASK_RGB = ~ALPHA_OPAQUE_SHIFTED;
	
	
	/** Returns the specified samples as packed ARGB value. */
	public static int toArgb(int rgb, int alpha)
	{
		return ((alpha << SHIFT_ALPHA) | (rgb & MASK_RGB));
	}
	
	/** Returns the specified samples as packed ARGB value. */
	public static int toArgb(int red, int green, int blue)
	{
		return (ALPHA_OPAQUE_SHIFTED | (red << SHIFT_RED) | (green << SHIFT_GREEN) | blue);
	}
	
	/** Returns the specified samples as packed ARGB value. */
	public static int toArgb(int red, int green, int blue, int alpha)
	{
		return ((alpha << SHIFT_ALPHA) | (red << SHIFT_RED) | (green << SHIFT_GREEN) | blue);
	}
}
