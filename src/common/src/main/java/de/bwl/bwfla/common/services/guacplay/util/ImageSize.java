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

package de.bwl.bwfla.common.services.guacplay.util;


/** A helper class representing the size of an image. */
public final class ImageSize
{
	private int width;
	private int height;
	
	/** Default constructor. */
	public ImageSize()
	{
		this(0, 0);
	}
	
	/** Constructor */
	public ImageSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	/** Set the new size. */
	public void set(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	/** Set a new width. */
	public void setWidth(int width)
	{
		this.width = width;
	}
	
	/** Set a new height. */
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	/** Returns the width. */
	public int getWidth()
	{
		return width;
	}
	
	/** Returns the height. */
	public int getHeight()
	{
		return height;
	}
}
