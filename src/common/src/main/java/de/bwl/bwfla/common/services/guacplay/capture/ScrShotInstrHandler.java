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

package de.bwl.bwfla.common.services.guacplay.capture;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;

/* Internal class (package-private) */


/** Handler for the internal screenshot-instruction. */
class ScrShotInstrHandler extends InstructionHandler
{
	// Member fields
	private final ConcurrentLinkedQueue<byte[]> screenshots;
	private final ByteArrayOutputStream outbuf;
	private final OffscreenCanvas canvas;
	
	/** Format of the generated screenshot */
	private static final String OUTPUT_FORMAT = "png";
	
	/** Constructor */
	public ScrShotInstrHandler(OffscreenCanvas canvas)
	{
		super(ExtOpCode.SCREENSHOT);
		
		this.screenshots = new ConcurrentLinkedQueue<byte[]>();
		this.canvas = canvas;
		
		final int size = 4 * canvas.getWidth() * canvas.getHeight();
		this.outbuf = new ByteArrayOutputStream(size);
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		synchronized (canvas)
		{
			canvas.render();
			outbuf.reset();
			
			// Create the final output-image
			ImageIO.write(canvas.getBufferedImage(), OUTPUT_FORMAT, outbuf);
			screenshots.add(outbuf.toByteArray());
		}
	}
	
	/** Returns true when next screenshot is available, else false. */
	public boolean hasNextScreenshot()
	{
		return !screenshots.isEmpty();
	}
	
	/** Returns the next screenshot-data if available, else null. */
	public byte[] getNextScreenshot()
	{
		return screenshots.poll();
	}
}
