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

package de.bwl.bwfla.common.services.guacplay.protocol.handler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.png.PngDecoder;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.util.Base64;
import de.bwl.bwfla.common.services.guacplay.util.CharArrayBuffer;
import de.bwl.bwfla.common.services.guacplay.util.ImageSize;


/**
 * Handler for Guacamole's <i>png-</i> instruction.
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#png-instruction">
 *          Guacamole's protocol reference
 *      </a>
 */
public class PngInstrHandler extends DrawingInstrHandler implements ISizeInstrListener
{
	/* Member fields */
	private final PngDecoder decoder;
	private final CharArrayBuffer charbuf;
	private ByteBuffer bytebuf;
	protected BufferedImage image;
	protected final ImageSize imgsize;
	
	/* Instruction's arguments */
	protected int mask;
	protected int layer;
	protected int xpos;
	protected int ypos;
	
	
	/** Constructor */
	public PngInstrHandler(OffscreenCanvas canvas)
	{
		super(OpCode.PNG, canvas);
		
		this.decoder = new PngDecoder();
		this.charbuf = new CharArrayBuffer();
		this.bytebuf = ByteBuffer.allocate(1024);
		this.image = canvas.newBufferedImage();
		this.imgsize = new ImageSize();
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		// Parse the arguments
		mask  = instruction.argAsInt(0);
		layer = instruction.argAsInt(1);
		xpos  = instruction.argAsInt(2);
		ypos  = instruction.argAsInt(3);
		
		// Get the Base64-encoded PNG-data and decode it
		instruction.argAsCharArray(4, charbuf);
		bytebuf = Base64.decode(charbuf.array(), charbuf.position(), charbuf.length(), bytebuf);
		
		// Decode the PNG image and draw it
		try {
			decoder.decode(bytebuf, image, imgsize);
		}
		catch (IOException exception) {
			Logger log = Logger.getLogger(PngInstrHandler.class.getName());
			log.severe("Decoding screen-update at ("+ xpos+  "," + ypos+ ") failed! Skip it.");
			log.log(Level.SEVERE, exception.getMessage(), exception);
			return;
		}
		
		synchronized (canvas) {
			canvas.drawImage(layer, mask, xpos, ypos, image, imgsize.getWidth(), imgsize.getHeight());
		}
	}

	@Override
	public void resize(int layerid, int width, int height)
	{
		// Is a new image buffer needed?
		if ((image.getWidth() < width) || (image.getHeight() < height))
			image = canvas.newBufferedImage(width, height);
	}
}
