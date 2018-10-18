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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.Map;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.CompositeMode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.LineCapStyle;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.LineJoinStyle;


/** Helper class, containing utility methods for use with the {@link java.awt} package. */
public final class AwtUtils
{
	/** Mapping from Guacamole's cap-styles to AWT's cap-styles. */
	private static final int LINE_CAP_STYLES[] = new int[3];
	static {
		LINE_CAP_STYLES[LineCapStyle.BUTT]   = BasicStroke.CAP_BUTT;
		LINE_CAP_STYLES[LineCapStyle.ROUND]  = BasicStroke.CAP_ROUND;
		LINE_CAP_STYLES[LineCapStyle.SQUARE] = BasicStroke.CAP_SQUARE;
	};
	
	/** Mapping from Guacamole's join-styles to AWT's join-styles. */
	private static final int LINE_JOIN_STYLES[] = new int[3];
	static {
		LINE_JOIN_STYLES[LineJoinStyle.BEVEL] = BasicStroke.JOIN_BEVEL;
		LINE_JOIN_STYLES[LineJoinStyle.MITER] = BasicStroke.JOIN_MITER;
		LINE_JOIN_STYLES[LineJoinStyle.ROUND] = BasicStroke.JOIN_ROUND;
	};
	
	/** Mapping from Guacamole's mask values to AWT's AlphaComposite instances. */
	private static final Map<Integer, AlphaComposite> COMPOSITES = new HashMap<Integer, AlphaComposite>(16);
	static {
		COMPOSITES.put(CompositeMode.SRC		, AlphaComposite.Src	 );
		COMPOSITES.put(CompositeMode.SRC_IN		, AlphaComposite.SrcIn	 );
		COMPOSITES.put(CompositeMode.SRC_OUT	, AlphaComposite.SrcOut	 );
		COMPOSITES.put(CompositeMode.SRC_ATOP	, AlphaComposite.SrcAtop );
		COMPOSITES.put(CompositeMode.SRC_OVER	, AlphaComposite.SrcOver );
		COMPOSITES.put(CompositeMode.DST_IN		, AlphaComposite.DstIn	 );
		COMPOSITES.put(CompositeMode.DST_OUT	, AlphaComposite.DstOut	 );
		COMPOSITES.put(CompositeMode.DST_ATOP	, AlphaComposite.DstAtop );
		COMPOSITES.put(CompositeMode.DST_OVER	, AlphaComposite.DstOver );
		COMPOSITES.put(CompositeMode.XOR		, AlphaComposite.Xor	 );
	}

	
	/**
	 * Map the Guacamole's mask to the corresponding AWT composite mode.
	 * @param maskid The Guacamole's mask to map.
	 * @return The corresponding {@link AlphaComposite} instance if valid, else null.
	 */
	public static AlphaComposite toAlphaComposite(int maskid)
	{
		return COMPOSITES.get(maskid);
	}
	
	/**
	 * Map the Guacamole's cap-style to the corresponding AWT's cap-style.
	 * @param style The Guacamole's cap-style to map.
	 * @return The corresponding AWT's cap-style.
	 */
	public static int toCapStyle(int style)
	{
		return LINE_CAP_STYLES[style];
	}
	
	/**
	 * Map the Guacamole's join-style to the corresponding AWT's join-style.
	 * @param style The Guacamole's join-style to map.
	 * @return The corresponding AWT's join-style.
	 */
	public static int toJoinStyle(int style)
	{
		return LINE_JOIN_STYLES[style];
	}
}
