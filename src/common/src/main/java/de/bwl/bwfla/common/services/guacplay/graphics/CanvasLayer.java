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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// For internal usage (Package-Private)


/** This class represents a layer in the {@link OffscreenCanvas}. */
final class CanvasLayer
{
	private final BufferedImage image;
	private final List<Shape> shapes;
	private Path2D curpath;
	
	
	/** Constructor */
	public CanvasLayer(BufferedImage image)
	{
		this.image = image;
		this.shapes = new ArrayList<Shape>();
		this.curpath = null;
	}
	
	/** Start a new subpath at the specified point. */
	public void startPath(double xpos, double ypos)
	{
		curpath = this.createNewPath();
		curpath.moveTo(xpos, ypos);
	}
	
	/**
	 * Closes the current path by connecting the 
	 * start and end points with a straight line.
	 */
	public void closeCurrentPath()
	{
		if (curpath == null)
			return;
		
		curpath.closePath();
		curpath = null;
	}
	
	/** Add a rectangular path to this layer. */
	public void addRectPath(Rectangle2D rectangle)
	{
		shapes.add(rectangle);
	}
	
	/** Add a cubic-bezier curve subpath to this layer. */
	public void addCurveSubpath(double cp1x, double cp1y, double cp2x, double cp2y, double epx, double epy)
	{
		if (curpath == null)
			throw new IllegalStateException("Attempt to add a curve-subpath to an empty path!");
		
		curpath.curveTo(cp1x, cp1y, cp2x, cp2y, epx, epy);
	}
	
	/** Add a line subpath to this layer. */
	public void addLineSubpath(double x, double y)
	{
		if (curpath == null)
			throw new IllegalStateException("Attempt to add a line-subpath to an empty path!");
		
		curpath.lineTo(x, y);
	}
	
	/** Add a subpath to the current path in this layer. */
	public void addSubpath(Shape subpath)
	{
		if (curpath == null)
			curpath = this.createNewPath();
		
		curpath.append(subpath, false);
	}
	
	/** Complete the current path and fill it with the specified color. */
	public void fillCurrentPath(AlphaComposite composite, int r, int g, int b, int a)
	{
		// Setup the renderer
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(new Color(r, g, b, a));
		graphics.setComposite(composite);
		
		// Fill all available paths
		for (Shape shape : shapes)
			graphics.fill(shape);
		
		graphics.dispose();
		this.reset();
	}
	
	/** Complete the current path and stroke it using the specified color and settings. */
	public void strokeCurrentPath(AlphaComposite composite, int cap, int join, int thickness, int r, int g, int b, int a)
	{
		// Setup the renderer
		Graphics2D graphics = image.createGraphics();
		graphics.setStroke(new BasicStroke(thickness, cap, join));
		graphics.setColor(new Color(r, g, b, a));
		graphics.setComposite(composite);

		// Fill all available paths
		for (Shape shape : shapes)
			graphics.draw(shape);

		graphics.dispose();
		this.reset();
	}
	
	/** Reset the paths of this layer. */
	public void reset()
	{
		shapes.clear();
		curpath = null;
	}
	
	/** Flushes all resources of this layer. */
	public void flush()
	{
		image.flush();
		this.reset();
	}
	
	/** Returns layer's image. */
	public BufferedImage image()
	{
		return image;
	}
	
	/** Returns layer's paths. */
	public List<Shape> shapes()
	{
		return shapes;
	}
	
	
	/* =============== Internal Methods =============== */
	
	private Path2D createNewPath()
	{
		Path2D newpath = new Path2D.Double();
		shapes.add(newpath);
		return newpath;
	}
}
