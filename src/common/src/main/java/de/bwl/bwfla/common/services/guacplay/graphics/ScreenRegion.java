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

package de.bwl.bwfla.common.services.guacplay.graphics;


/** This class describes a rectangular screen-region. */
public final class ScreenRegion
{
	/** Constant, indicating that no match was found. */
	private static final float NO_MATCH = 0.0F;
	
	// Member fields
	private int x1, y1;
	private int x2, y2;
	private float area;
	private boolean empty;
	
	
	/** Constructor for an emty region. */
	public ScreenRegion()
	{
		this.set(0, 0, 0, 0);
	}
	
	/** Copy-Constructor */
	public ScreenRegion(ScreenRegion other)
	{
		this.set(other);
	}
	
	/** Constructor */
	public ScreenRegion(int x, int y, int width, int height)
	{
		this.set(x, y, width, height);
	}
	
	/**
	 * Set new bounds of this region.
	 * @param x The x-position of the upper left corner.
	 * @param y The y-position of the upper left corner.
	 * @param width The region's width.
	 * @param height The region's height.
	 */
	public void set(int x, int y, int width, int height)
	{
		this.x1 = x;
		this.y1 = y;
		this.x2 = x + width;
		this.y2 = y + height;
		this.area = (float) (width * height);
		this.empty = (x1 == x2) || (y1 == y2);
	}
	
	/**
	 * Set new bounds of this region.
	 * @param other The new bounds to set.
	 */
	public void set(ScreenRegion other)
	{
		this.x1 = other.x1;
		this.y1 = other.y1;
		this.x2 = other.x2;
		this.y2 = other.y2;
		this.area = other.area;
		this.empty = other.empty;
	}
	
	/**
	 * Create a union of this and other regions.
	 * @param x The x-position of the upper left corner.
	 * @param y The y-position of the upper left corner.
	 * @param width The region's width.
	 * @param height The region's height.
	 */
	public void add(int x, int y, int width, int height)
	{
		// Is the other rectangle empty?
		if ((width == 0) || (height == 0))
			return;  // Yes
		
		// Is this empty?
		if (empty) {
			this.set(x, y, width, height);
			return;  // Yes
		}
		
		// Update coordinates
		
		final int ox2 = x + width;
		final int oy2 = y + height;
		
		if (x1 > x)
			x1 = x;
		
		if (y1 > y)
			y1 = y;
		
		if (x2 < ox2)
			x2 = ox2;
		
		if (y2 < oy2)
			y2 = oy2;
		
		// Update the new area
		area = (x2 - x1) * (y2 - y1);
	}
	
	/** Create a union of this and other regions. */
	public void add(ScreenRegion other)
	{
		// Is the rectangle empty?
		if (other.empty)
			return;  // Yes
		
		// Is this empty?
		if (empty) {
			this.set(other);
			return;  // Yes
		}
		
		// Update coordinates
		
		if (x1 > other.x1)
			x1 = other.x1;
		
		if (y1 > other.y1)
			y1 = other.y1;
		
		if (x2 < other.x2)
			x2 = other.x2;
		
		if (y2 < other.y2)
			y2 = other.y2;
		
		// Update the new area
		area = (x2 - x1) * (y2 - y1);
	}
	
	/**
	 * Check, whether this and other regions intersect.
	 * @param other The region to check.
	 * @return true if the intersection occures, else false. 
	 */
	public boolean intersects(ScreenRegion other)
	{
		// Is any of the rectangles empty?
		if (empty || other.empty)
			return false;  // Yes
		
		// Do both rectangles intersect?
		return (y1 < other.y2) && (y2 > other.y1) && (x1 < other.x2) && (x2 > other.x1);
	}
	
	/**
	 * Compute the matching-score of other with this region.
	 * @param other The test-region.
	 * @return Percentage of other's area intersected with this region's area.
	 */
	public float match(ScreenRegion other)
	{
		// Is any of the rectangles empty?
		if (empty || other.empty)
			return NO_MATCH;  // Yes
				
		// Do both rectangles intersect?
		if ((y1 > other.y2) || (y2 < other.y1) || (x1 > other.x2) || (x2 < other.x1))
			return NO_MATCH;  // No
		
		// Construct the intersection-segments
		final int ix1 = Math.max(x1, other.x1);
		final int ix2 = Math.min(x2, other.x2);
		final int iy1 = Math.max(y1, other.y1);
		final int iy2 = Math.min(y2, other.y2);
		
		// Compute the intersected area and its score
		final int iarea = (ix2 - ix1) * (iy2 - iy1);
		return (float) (iarea / other.area);
	}
	
	/** Reset this region to an empty one. */
	public void reset()
	{
		this.set(0, 0, 0, 0);
	}
}
