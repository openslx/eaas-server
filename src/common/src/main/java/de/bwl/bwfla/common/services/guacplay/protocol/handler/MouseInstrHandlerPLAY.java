package de.bwl.bwfla.common.services.guacplay.protocol.handler;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.MouseButton;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;
import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;


/**
 * Handler for Guacamole's <i>mouse-</i> instruction (Replay-Version).
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#mouse-instruction">
 *          Guacamole's protocol reference
 *      </a>
 */
public class MouseInstrHandlerPLAY extends InstructionHandler
{
	// Member fields
	private final SupdInstrHandler scrupdate;
	private final ICharArrayConsumer output;
	private int lastButtons;

	/** Mask for mouse buttons only! (excluding scroll-up and scroll-down buttons) */
	private static final int BUTTONS_MASK = MouseButton.LEFT | MouseButton.MIDDLE | MouseButton.RIGHT;
	
	/** Timeout for disabling screen-update matching (in ms) */
	private static final long TIMEOUT = 250L;
	
	
	/** Constructor */
	public MouseInstrHandlerPLAY(ICharArrayConsumer output, SupdInstrHandler scrupdate)
	{
		super(OpCode.MOUSE);
		this.scrupdate = scrupdate;
		this.output = output;
		this.lastButtons = 0;
	}
	
	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		// Get the mouse-button mask
		final int buttons = instruction.argAsInt(2) & BUTTONS_MASK;

		// Any button pressed, but mouse not moved?
		if ((buttons != 0) && (buttons != lastButtons)) {
			// Yes! Then disable screen-update matching briefly,
			// preventing next mouse-events from being delayed.
			// This is especially important for double-clicks.
			long timestamp = desc.getTimestamp() + TIMEOUT;
			scrupdate.setMinTimestamp(timestamp);
		}
		
		lastButtons = buttons;  // Update
		
		// Forward the instruction unmodified
		final char[] data = instruction.array();
		final int offset = instruction.offset();
		final int length = instruction.length();
		output.consume(data, offset, length);
	}
}
