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

import de.bwl.bwfla.common.services.guacplay.util.ConditionVariable;


/**
 * An observer, that notifies a set of threads waiting on
 * a condition when the specified screen-area is updated.
 */
public final class ScreenObserver
{
	// Member fields
	private final ConditionVariable condition;
	private boolean empty;
	private int sx1, sy1;
	private int sx2, sy2;
	
	
	/** Constructor */
	public ScreenObserver(ConditionVariable condition)
	{
		this(condition, 0, 0, 0, 0);
	}
	
	/** Constructor */
	public ScreenObserver(ConditionVariable condition, int x1, int y1, int x2, int y2)
	{
		this.condition = condition;
		this.setScreenArea(x1, y1, x2, y2);
	}
	
	/** Set the screen rectangle, that should be observed. */
	public synchronized void setScreenArea(int x1, int y1, int x2, int y2)
	{
		this.empty = (x1 == x2) || (y1 == y2);
		this.sx1 = x1;
		this.sy1 = y1;
		this.sx2 = x2;
		this.sy2 = y2;
	}
	
	/**
	 * Returns true when the screen rectangle of
	 * this observer is not empty, else false.
	 */
	public synchronized boolean isEnabled()
	{
		return !empty;
	}
	
	/**
	 * Intersect the specified rectangle with the observed screen area and
	 * notify the waiting threads, when this intersection is not empty.
	 */
	public synchronized void update(int x1, int y1, int x2, int y2)
	{
		// Is any of the rectangles empty?
		if (empty || (x1 == x2) || (y1 == y2))
			return;  // Yes
		
		// Do both rectangles intersect?
		if ((x2 > sx1) && (y2 > sy1) && (x1 < sx2) && (y1 < sy2))
			condition.signalAll();  // Yes
	}
	
	/** Reset the observable screen-area. */
	public void reset()
	{
		this.setScreenArea(0, 0, 0, 0);
	}
}
