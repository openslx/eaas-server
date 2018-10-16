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


/**
 * A compacted collection of {@link ScreenRegion}s,
 * where all intersecting regions are joined together.
 */
public final class ScreenRegionList
{
	// Member fields
	private final ScreenRegion region;
	private final ScreenRegion bounds;
	private ScreenRegion[] entries;
	private int size;
	
	
	/** Constructor */
	public ScreenRegionList(int capacity)
	{
		this.region = new ScreenRegion();
		this.bounds = new ScreenRegion();
		this.entries = new ScreenRegion[capacity];
		this.size = 0;
		
		for (int i = 0; i < capacity; ++i)
			entries[i] = new ScreenRegion();
	}
	
	/**
	 * Add a new region to this list.
	 * @param x The x-position of the upper left corner.
	 * @param y The y-position of the upper left corner.
	 * @param width The region's width.
	 * @param height The region's height.
	 */
	public void add(int x, int y, int width, int height)
	{
		region.set(x, y, width, height);
		bounds.add(region);
		
		// Join the new region with any region in the list,
		// if an intersection between them can be found
		for (int i = 0; i < size; ++i) {
			if (entries[i].intersects(region)) {
				entries[i].add(region);
				return;
			}
		}
		
		// No intersections were found!
		// Add a new entry into the list.
		
		final int index = size;
		
		// Enough space left?
		if (++size >= entries.length) {
			// No, resize the current array
			ScreenRegion[] newarray = new ScreenRegion[entries.length << 1];
			System.arraycopy(entries, 0, newarray, 0, index);
			entries = newarray;
		}
		
		// Add the new region
		if (entries[index] != null)
			entries[index].set(region);
		else entries[index] = new ScreenRegion(region);
	}
	
	/**
	 * Check, if the percentage of target's area intersected
	 * with regions in this list is above the threshold.
	 * @param target The region to test for intersection.
	 * @param threshold The minimal intersection-percentage.
	 * @return true if the intersected area is above the threshold, else false.
	 */
	public boolean matches(ScreenRegion target, float threshold)
	{
		float curscore = 0.0F;
		
		// Compute the match-score, while below threshold
		for (int i = 0, imax = size; i < imax; ++i) {
			ScreenRegion current = entries[i];
			curscore += current.match(target);
			if (curscore > threshold)
				return true;
		}

		return false;
	}
	
	/**
	 * Check, if the percentage of target-regions intersected
	 * with regions in this list is above the threshold.
	 * @param targets The list of all region to test for intersection.
	 * @param threshold The minimal intersection-percentage.
	 * @return true if the intersected area is above the threshold, else false.
	 */
	public boolean matches(ScreenRegionList targets, float threshold)
	{
		// Adjustment according to list's size
		threshold *= (float) targets.size();
		
		float curscore = 0.0F;
		
		// Compute the match-score, while below threshold
		for (int i = 0, imax = targets.size(); i < imax; ++i) {
			final ScreenRegion target = targets.get(i);
			for (int j = 0, jmax = size; j < jmax; ++j) {
				ScreenRegion current = entries[j];
				curscore += current.match(target);
				if (curscore > threshold)
					return true;
			}
		}
		
		return false;
	}
	
	/** Returns the entry at the specified index. */
	public ScreenRegion get(int index)
	{
		return entries[index];
	}
	
	/** Returns the bounds of this list. */
	public ScreenRegion bounds()
	{
		return bounds;
	}
	
	/** Returns the entries in this list. */
	public ScreenRegion[] entries()
	{
		return entries;
	}
	
	/** Returns the number of entries in this list. */
	public int size()
	{
		return size;
	}
	
	/** Clear the content of this list. */
	public void clear()
	{
		bounds.reset();
		size = 0;
	}
}
