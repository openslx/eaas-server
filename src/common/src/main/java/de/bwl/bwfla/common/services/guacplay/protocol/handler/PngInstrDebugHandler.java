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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;


public class PngInstrDebugHandler extends PngInstrHandler
{	
	private String imgdir;
	private int imgnum;
	
	
	public PngInstrDebugHandler(OffscreenCanvas canvas)
	{
		super(canvas);
		this.imgnum = 0;
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		super.execute(desc, instruction);
		
		final String format = "png";
		final String suffix = ".png";
		
		// Write the delta-update image
		{	
			BufferedImage subimg = image.getSubimage(0, 0, imgsize.getWidth(), imgsize.getHeight());
			File file = new File(imgdir, "delta-" + imgnum + suffix);
			ImageIO.write(subimg, format, file);
		}
		
		// Write the canvas image
		synchronized (canvas)
		{
			canvas.render();
			
			File file = new File(imgdir, "canvas-" + imgnum + suffix);
			ImageIO.write(canvas.getBufferedImage(), format, file);
		}
		
		++imgnum;
	}
	
	public void setOutputDirectory(Path dir) throws IOException
	{
		this.imgdir = dir.toString();
		this.imgnum = 0;
		
		// Prepare the output directory
		if (Files.exists(dir)) {
			for (Path file : Files.newDirectoryStream(dir))
				Files.delete(file);
		}
		else Files.createDirectories(dir);
	}
}
