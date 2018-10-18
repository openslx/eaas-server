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

package de.bwl.bwfla.common.services.guacplay;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import de.bwl.bwfla.common.services.guacplay.util.TimeUtils;


public final class GuacDefs
{
	/* Special Characters in Guacamole's Protocol */
	public static final char LENGTH_SEPARATOR        = '.';
	public static final char VALUE_SEPARATOR         = ',';
	public static final char INSTRUCTION_TERMINATOR  = ';';
	
	/* Visual-Synchronization Constants */
	public static final long  VSYNC_TIMEOUT_MS      = 150L;  // milliseconds
	public static final long  VSYNC_TIMEOUT_NS      = TimeUtils.convert(VSYNC_TIMEOUT_MS, MILLISECONDS, NANOSECONDS);
	public static final int   VSYNC_RECT_WIDTH      = 40;    // pixels
	public static final int   VSYNC_RECT_HEIGHT     = 30;    // pixels
	public static final float VSYNC_PIXEL_THRESHOLD = 0.9F;  // percentage of pixels, that must be equal
	public static final float VSYNC_COLOR_THRESHOLD = 5.0F;  // average color difference threshold
	
	/* File Extensions */
	public static final String TRACE_FILE_EXT  = ".trace";
	
	/** Guacamole OpCodes */
	public static final class OpCode
	{
		// Drawing Instructions
		public static final String ARC         = "arc";
		public static final String CFILL       = "cfill";
		public static final String CLIP        = "clip";
		public static final String CLOSE       = "close";
		public static final String COPY        = "copy";
		public static final String CSTROKE     = "cstroke";
		public static final String CURSOR      = "cursor";
		public static final String CURVE       = "curve";
		public static final String DISPOSE     = "dispose";
		public static final String DISTORT     = "distort";
		public static final String IDENTITY    = "identity";
		public static final String LFILL       = "lfill";
		public static final String LINE        = "line";
		public static final String LSTROKE     = "lstroke";
		public static final String MOVE        = "move";
		public static final String PNG         = "png";
		public static final String POP         = "pop";
		public static final String PUSH        = "push";
		public static final String RECT        = "rect";
		public static final String RESET       = "reset";
		public static final String SET         = "set";
		public static final String SHADE       = "shade";
		public static final String SIZE        = "size";
		public static final String START       = "start";
		public static final String TRANSFER    = "transfer";
		public static final String TRANSFORM   = "transform";
		
		// Client/Server Control Instructions
		public static final String ARGS        = "args";
		public static final String AUDIO       = "audio";
		public static final String CONNECT     = "connect";
		public static final String DISCONNECT  = "disconnect";
		public static final String NAME        = "name";
		public static final String READY       = "ready";
		public static final String SELECT      = "select";
		public static final String SYNC        = "sync";
		public static final String VIDEO       = "video";
		
		
		// Client Events
		public static final String CLIPBOARD   = "clipboard";
		public static final String KEY         = "key";
		public static final String MOUSE       = "mouse";
		
		// Streaming Instructions
		public static final String ACK         = "ack";
		public static final String BLOB        = "blob";
		public static final String END         = "end";
		public static final String FILE        = "file";
		public static final String NEST        = "nest";
		public static final String PIPE        = "pipe";
	}
	
	/** Custom OpCodes (Extensions) */
	public static final class ExtOpCode
	{
		public static final String SCREEN_UPDATE    = "supd";    // args: <xpos>,<ypos>,<width>,<heigth>
		public static final String VSYNC            = "vsync";   // args: type specific!
		public static final String ACTION_FINISHED  = "actfin";  // args: type specific!
		
		// Internal opcodes
		public static final String SCREENSHOT       = "__scrshot";  // no args!
	}
	
	/** Supported types of VisualSync-Algorithms */
	public static final class VSyncType
	{
		public static final int EQUAL_PIXELS   = 0;
		public static final int AVERAGE_COLOR  = 1;
	}
	
	/** Composite mode values as defined in Guacamole's source! */
	public static final class CompositeMode
	{
		public static final int SRC        = 0xC;    // GUAC_COMP_SRC
		public static final int SRC_IN     = 0x4;    // GUAC_COMP_IN
		public static final int SRC_OUT    = 0x8;    // GUAC_COMP_OUT
		public static final int SRC_ATOP   = 0x6;    // GUAC_COMP_ATOP
		public static final int SRC_OVER   = 0xE;    // GUAC_COMP_OVER
		public static final int DST_IN     = 0x1;    // GUAC_COMP_RIN
		public static final int DST_OUT    = 0x2;    // GUAC_COMP_ROUT
		public static final int DST_ATOP   = 0x9;    // GUAC_COMP_RATOP
		public static final int DST_OVER   = 0xB;    // GUAC_COMP_ROVER
		public static final int XOR        = 0xA;    // GUAC_COMP_XOR
	}
	
	/** Line cap-style values as defined in Guacamole's source! */
	public static final class LineCapStyle
	{
		public static final int BUTT   = 0x0;    // GUAC_LINE_CAP_BUTT
		public static final int ROUND  = 0x1;    // GUAC_LINE_CAP_ROUND
		public static final int SQUARE = 0x2;    // GUAC_LINE_CAP_SQUARE
	}
	
	/** Line join-style values as defined in Guacamole's source! */
	public static final class LineJoinStyle
	{
		public static final int BEVEL  = 0x0;    // GUAC_LINE_JOIN_BEVEL
		public static final int MITER  = 0x1;    // GUAC_LINE_JOIN_MITER
		public static final int ROUND  = 0x2;    // GUAC_LINE_JOIN_ROUND
	}
	
	/** Mouse button masks as defined in Guacamole's source! */
	public static final class MouseButton
	{
		public static final int LEFT         = 0x01;    // GUAC_CLIENT_MOUSE_LEFT
		public static final int MIDDLE       = 0x02;    // GUAC_CLIENT_MOUSE_MIDDLE
		public static final int RIGHT        = 0x04;    // GUAC_CLIENT_MOUSE_RIGHT
		public static final int SCROLL_UP    = 0x08;    // GUAC_CLIENT_MOUSE_SCROLL_UP
		public static final int SCROLL_DOWN  = 0x10;    // GUAC_CLIENT_MOUSE_SCROLL_DOWN
	}
	
	/** State of the keyboard's key. */
	public static final class KeyState
	{
		public static final int RELEASED = 0;
		public static final int PRESSED  = 1;
	}
	
	/** X11 keysym-codes. */
	public static final class KeyCode
	{
		public static final int BACKSPACE = 0xFF08;
		public static final int TAB       = 0xFF09;
		public static final int RETURN    = 0xFF0D;
		public static final int ESCAPE    = 0xFF1B;
	}
	
	/** Types for possible message/instruction sources. */
	public static enum SourceType
	{
		CLIENT,     // Sent by Guacamole client
		SERVER,     // Sent by Guacamole server (eg. GUACD)
		INTERNAL,   // Sent by backend server
		UNKNOWN     // Source is undefined
	}
	
	/** Predefined event-types. */
	public static final class EventType
	{
		public static final int VSYNC_BEGIN    = 1;
		public static final int VSYNC_END      = 2;
		public static final int SESSION_BEGIN  = 3;
		public static final int TERMINATION    = 4;
		public static final int TRACE_END      = 5;
	}
	
	/** Predefined names of the metadata-chunks */
	public static final class MetadataTag
	{
		public static final String INTERNAL  = "internal";
		public static final String PUBLIC    = "public";
	}
}
