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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.bwl.bwfla.common.services.guacplay.protocol.handler.ISizeInstrListener;


/** A class representing an off-screen canvas. */
public class OffscreenCanvas implements ISizeInstrListener
{
	/** The type of the images, representing the layers in the canvas. */
	private static final int IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;
	
	/** The ID, representing an invalid layer. */
	private static final int INVALID_LAYERID = Integer.MIN_VALUE;
	
	// Member fields
	private final Map<Integer, CanvasLayer> buffers;        // Layers with ID < 0
	private final Map<Integer, CanvasLayer> layers;         // Layers with ID >= 0
	private final Set<ScreenObserver> observers;
	private BufferedImage canvas;
	
	// Members for fast-path access
	private CanvasLayer curLayer;
	private AlphaComposite curComposite;
	private int curLayerId;
	private int curMaskId;
	
	
	/** Constructor */
	public OffscreenCanvas()
	{
		this.buffers = new HashMap<Integer, CanvasLayer>();
		this.layers = new TreeMap<Integer, CanvasLayer>();
		this.observers = new HashSet<ScreenObserver>();
		this.canvas = this.newBufferedImage(1024, 768);
		this.curLayer = null;
		this.curComposite = null;
		this.curLayerId = INVALID_LAYERID;
		this.curMaskId = Integer.MIN_VALUE;
	}
	
	/**
	 * Draw the image into the specified layer at the specified position, using the composite mode.
	 * @param layerid The layer to draw into.
	 * @param maskid The ID of composite mode to use (as defined in the Guacamole protocol).
	 * @param x The x position to draw the image at.
	 * @param y The y position to draw the image at.
	 * @param image The image, that should be drawn.
	 */
	public void drawImage(int layerid, int maskid, int x, int y, BufferedImage image)
	{
		// Find the layer and composite
		CanvasLayer layer = this.findLayer(layerid);
		AlphaComposite composite = this.findComposite(maskid);
		
		// Draw with the specified composite mode
		Graphics2D graphics = layer.image().createGraphics();
		graphics.setComposite(composite);
		graphics.drawImage(image, x, y, null);
		graphics.dispose();
		
		this.updateScreenObservers(x, y, x + image.getWidth(), y + image.getHeight());
	}
	
	/**
	 * Draw the subimage into the specified layer at the specified position, using the composite mode.
	 * @param layerid The layer to draw into.
	 * @param maskid The ID of composite mode to use (as defined in the Guacamole protocol).
	 * @param x The x position to draw the image at.
	 * @param y The y position to draw the image at.
	 * @param image The image, that should be drawn.
	 * @param width The width of the subimage to draw.
	 * @param height The height of the subimage to draw.
	 */
	public void drawImage(int layerid, int maskid, int x, int y, BufferedImage image, int width, int height)
	{
		// Find the layer and composite
		CanvasLayer layer = this.findLayer(layerid);
		AlphaComposite composite = this.findComposite(maskid);
		
		// Coordinates of the destination rectangle
		final int dx1 = x;
		final int dy1 = y;
		final int dx2 = x + width;
		final int dy2 = y + height;
		
		// Draw with the specified composite mode
		Graphics2D graphics = layer.image().createGraphics();
		graphics.setComposite(composite);
		graphics.drawImage(image, dx1, dy1, dx2, dy2, 0, 0, width, height, null);
		graphics.dispose();
		
		this.updateScreenObservers(dx1, dy1, dx2, dy2);
	}
	
	/**
	 * Add a rectangular path to the specified layer.
	 * @param layerid The layer, whose path should be modified.
	 * @param x The x coordinate of the rectangle's upper-left corner.
	 * @param y The y coordinate of the rectangle's upper-left corner.
	 * @param width The rectangle's width.
	 * @param height The rectangle's height.
	 */
	public void addRectPath(int layerid, int x, int y, int width, int height)
	{
		CanvasLayer layer = this.findLayer(layerid);
		layer.addRectPath(new Rectangle2D.Double(x, y, width, height));
	}
	
	/**
	 * Add an arc subpath to the specified layer.
	 * @param layerid The layer, whose path should be modified.
	 * @param x The x coordinate of the circle's center, containing the arc.
	 * @param y The y coordinate of the circle's center, containing the arc.
	 * @param radius The radius of the circle containing the arc.
	 * @param start The arc's starting angle, in radians. 
	 * @param end The arc's ending angle, in radians.
	 * @param negative If 0 then draw the arc in clockwise direction, else in counter-clockwise.
	 */
	public void addArcSubpath(int layerid, int x, int y, int radius, double start, double end, int negative)
	{
		// INFO: Canvas in HTML5 and Java2D use different directions for increasing angles!
		//       HTML5  -> clockwise on the arc's circle.
		//       Java2D -> counter-clockwise on the arc's circle.
		
		// Convert radians to degrees
		start = Math.toDegrees(start);
		end   = Math.toDegrees(end);
		
		// Compute arc's angle
		double extent = 0.0;
		if (start < end)
			extent = end - start;
		else extent = (360.0 - start) + end;
		
		// Drawing-Order, adjusting for different angle directions
		if (negative != 0)
			extent = 360.0 - extent;  // counter-clockwise
		else extent = -extent;        // clockwise
		
		// Finally construct the arc-subpath
		final Arc2D arcpath = new Arc2D.Double();
		arcpath.setArcByCenter(x, y, radius, start, extent, Arc2D.OPEN);
		this.findLayer(layerid).addSubpath(arcpath);
	}
	
	/**
	 * Add a line subpath to the specified layer.
	 * @param layerid The layer, whose path should be modified.
	 * @param x The x position of the line's endpoint.
	 * @param y The y position of the line's endpoint.
	 */
	public void addLineSubpath(int layerid, int x, int y)
	{
		CanvasLayer layer = this.findLayer(layerid);
		layer.addLineSubpath(x, y);
	}
	
	/**
	 * Add a cubic-bezier curve subpath to the specified layer.
	 * @param layerid The layer, whose path should be modified.
	 * @param cp1x The x position of the 1. control point.
	 * @param cp1y The y position of the 1. control point.
	 * @param cp2x The x position of the 2. control point.
	 * @param cp2y The y position of the 2. control point.
	 * @param x The x position of the curve's endpoint.
	 * @param y The y position of the curve's endpoint.
	 */
	public void addCurveSubpath(int layerid, int cp1x, int cp1y, int cp2x, int cp2y, int x, int y)
	{
		CanvasLayer layer = this.findLayer(layerid);
		layer.addCurveSubpath(cp1x, cp1y, cp2x, cp2y, x, y);
	}
	
	/**
	 * Start a new path at the specified point.
	 * @param layerid The layer, whose path should be modified.
	 * @param x The x coordinate of the path's first point.
	 * @param y The y coordinate of the path's first point.
	 */
	public void startNewPath(int layerid, int x, int y)
	{
		CanvasLayer layer = this.findLayer(layerid);
		layer.startPath(x, y);
	}
	
	/**
	 * Closes the current path by connecting the start and end points with a straight line.
	 * @param layerid The layer, whose path should be closed.
	 */
	public void closeCurrentPath(int layerid)
	{
		CanvasLayer layer = this.findLayer(layerid);
		layer.closeCurrentPath();
	}
	
	/**
	 * Complete the current path and fill it with the specified color.
	 * @param layerid The layer, whose path should be completed and filled.
	 * @param maskid The ID of composite mode to use (as defined in the Guacamole protocol).
	 * @param r The red sample of the color.
	 * @param g The green sample of the color.
	 * @param b The blue sample of the color.
	 * @param a The alpha sample of the color.
	 */
	public void fillCurrentPath(int layerid, int maskid, int r, int g, int b, int a)
	{
		AlphaComposite composite = this.findComposite(maskid);
		CanvasLayer layer = this.findLayer(layerid);
		layer.fillCurrentPath(composite, r, g, b, a);
	}
	
	/**
	 * Complete the current path and stroke it using the specified color and settings. 
	 * @param layerid The layer, whose path should be completed and stroked.
	 * @param maskid The ID of composite mode to use (as defined in the Guacamole protocol).
	 * @param cap The line's cap-style to use (as defined in the Guacamole protocol).
	 * @param join The line's join-style to use (as defined in the Guacamole protocol).
	 * @param thickness The thickness of the stroke in pixels.
	 * @param r The red sample of the color.
	 * @param g The green sample of the color.
	 * @param b The blue sample of the color.
	 * @param a The alpha sample of the color.
	 */
	public void strokeCurrentPath(int layerid, int maskid, int cap, int join, int thickness, int r, int g, int b, int a)
	{
		// Mapping: Guacamole -> Java2D
		cap  = AwtUtils.toCapStyle(cap);
		join = AwtUtils.toJoinStyle(join);
		
		AlphaComposite composite = this.findComposite(maskid);
		CanvasLayer layer = this.findLayer(layerid);
		layer.strokeCurrentPath(composite, cap, join, thickness, r, g, b, a);
	}
	
	/** Render all visible layers in this canvas. */
	public void render()
	{
		this.render(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	/**
	 * Render all visible layers in this canvas, using the specified clipping area. 
	 * @param x The x coordinate of the clipping rectangle
	 * @param y The y coordinate of the clipping rectangle
	 * @param width The width of the clipping rectangle
	 * @param height The height of the clipping rectangle
	 */
	public void render(int x, int y, int width, int height)
	{
		Graphics2D graphics = canvas.createGraphics();
		
		// Set the clipping region
		graphics.setClip(x, y, width, height);
		
		// Draw all visible layers from 0 to N
		for (Entry<Integer, CanvasLayer> entry : layers.entrySet()) {
			final CanvasLayer layer = entry.getValue();
			graphics.drawImage(layer.image(), 0, 0, null);
		}
		
		graphics.dispose();
	}
	
	/**
	 * Render all visible layers in this canvas, using the specified clipping area. 
	 * @param x The x coordinate of the clipping rectangle
	 * @param y The y coordinate of the clipping rectangle
	 * @param width The width of the clipping rectangle
	 * @param height The height of the clipping rectangle
	 * @param output The output-image, that should be drawn into.
	 */
	public void render(int x, int y, int width, int height, BufferedImage output)
	{
		// Coordinates of the source rectangle
		final int sx1 = x;
		final int sy1 = y;
		final int sx2 = x + width;
		final int sy2 = y + height;
		
		// Draw all visible layers from 0 to N
		Graphics2D graphics = output.createGraphics();
		for (Entry<Integer, CanvasLayer> entry : layers.entrySet()) {
			final CanvasLayer layer = entry.getValue();
			graphics.drawImage(layer.image(), 0, 0, width, height, sx1, sy1, sx2, sy2, null);
		}
		
		graphics.dispose();
	}
	
	@Override
	public void resize(int layerid, int width, int height)
	{		
		// Remove layer/buffer
		this.remove(layerid);
		
		// Create a new one
		Map<Integer, CanvasLayer> collection = this.getLayerCollection(layerid);
		collection.put(layerid, this.newLayer(width, height));
		
		// Default layer resized?
		if (layerid == 0) {
			// Recreate also the final canvas image
			if (canvas != null)
				canvas.flush();
			
			canvas = this.newBufferedImage(width, height);
		}
	}
	
	/** Remove the layer with specified ID. */
	public void remove(int layerid)
	{
		// Remove the layer from its list and free used resources
		Map<Integer, CanvasLayer> collection = this.getLayerCollection(layerid);
		CanvasLayer layer = collection.remove(layerid);
		if (layer != null)
			layer.flush();
		
		// Remove current references if needed
		if (curLayerId == layerid) {
			curLayerId = INVALID_LAYERID;
			curLayer = null;
		}
	}
	
	/**
	 * Creates a new {@link BufferedImage} for use with this canvas.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @return A newly created image of the specified size.
	 */
	public BufferedImage newBufferedImage(int width, int height)
	{
		BufferedImage image = new BufferedImage(width, height, IMAGE_TYPE);
					
		// Fill the image with black color
		Graphics2D graphics = image.createGraphics();
		graphics.setBackground(Color.BLACK);
		graphics.clearRect(0, 0, width, height);
		graphics.dispose();
		
		return image;
	}
	
	/**
	 * Creates a new {@link BufferedImage} for use with this canvas.
	 * @return A newly created image with the same size as this canvas.
	 */
	public BufferedImage newBufferedImage()
	{
		int width = this.getWidth();
		int height = this.getHeight();
		return this.newBufferedImage(width, height);
	}
	
	/**
	 * Returns the underlying {@link BufferedImage} of this canvas. 
	 * @return The image, this canvas renders into.
	 */
	public BufferedImage getBufferedImage()
	{
		return canvas;
	}
	
	/** Returns the width of this canvas. */
	public int getWidth()
	{
		return canvas.getWidth();
	}
	
	/** Returns the height of this canvas. */
	public int getHeight()
	{
		return canvas.getHeight();
	}
	
	/**
	 * Add a new observer to this canvas.
	 * @param observer The instance to add.
	 * @return true when the observer was not already added, else false.
	 */
	public boolean addObserver(ScreenObserver observer)
	{
		return observers.add(observer);
	}
	
	/**
	 * Remove the specified observer from this canvas.
	 * @param observer The instance to remove.
	 * @return true if observer was removed, else if it was not present.
	 */
	public boolean removeObserver(ScreenObserver observer)
	{
		return observers.remove(observer);
	}
	
	
	/* ==================== Internal methods ==================== */
	
	private final CanvasLayer newLayer(int width, int height)
	{
		return new CanvasLayer(this.newBufferedImage(width, height));
	}
	
	private Map<Integer, CanvasLayer> getLayerCollection(int layerid)
	{
		return ((layerid >= 0) ? layers : buffers);
	}
	
	private CanvasLayer findLayer(int layerid)
	{
		// Fast path: requested layer used in the last call?
		if ((layerid == curLayerId) && (curLayer != null))
			return curLayer;
		
		Map<Integer, CanvasLayer> collection = this.getLayerCollection(layerid); 
		
		// Find or create the layer
		curLayer = collection.get(layerid);
		if (curLayer == null) {
			final int width = canvas.getWidth();
			final int height = canvas.getHeight();
			
			// Layer was not found, create a new one
			curLayer = this.newLayer(width, height);
			collection.put(layerid, curLayer);
		}
		
		curLayerId = layerid;
		return curLayer;
	}
	
	private AlphaComposite findComposite(int maskid)
	{
		// Fast path: requested AlphaComposite used in the last call?
		if ((maskid == curMaskId) && (curComposite != null))
			return curComposite;
		
		// Find the AlphaComposite instance
		curComposite = AwtUtils.toAlphaComposite(maskid);
		if (curComposite == null)
			throw new IllegalArgumentException("Unsupported mask specified: " + maskid);
		
		curMaskId = maskid;
		return curComposite;
	}
	
	private void updateScreenObservers(int x1, int y1, int x2, int y2)
	{
		for (ScreenObserver observer : observers)
			observer.update(x1, y1, x2, y2);
	}
}
