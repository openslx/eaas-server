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

package de.bwl.bwfla.common.services.guacplay.protocol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import de.bwl.bwfla.common.services.guacplay.BaseTest;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.CompositeMode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.graphics.AwtUtils;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CFillInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CStrokeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CloseInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CurveInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.LineInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.RectInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.StartInstrHandler;


public class DrawingInstrHandlerTest extends BaseTest
{
	private static final int NUMBER_ITERATIONS = 100;
	private static final int MAIN_LAYER        = 0;
	private static final int CANVAS_WIDTH      = 600;
	private static final int CANVAS_HEIGHT     = 480;
	
	private static final int DEFAULT_COMPOSITE_MODE = CompositeMode.SRC_OVER;
	
	// Member fields
	private final InstructionBuilder ibuilder = new InstructionBuilder(1024);
	private final OffscreenCanvas canvas = new OffscreenCanvas();
	private final CStrokeInstrHandler cstrokeInstrHandler = new CStrokeInstrHandler(canvas);
	private final CFillInstrHandler cfillInstrHandler = new CFillInstrHandler(canvas);
	private final Random random = new Random();
	

	@Test
	public void testCurveInstr() throws Exception
	{
		log.info("Testing curve-instruction...");
		
		for (int i = 0; i < NUMBER_ITERATIONS; ++i)
			this.runCurveInstrTest();
		
		this.markAsPassed();
	}
	
	@Test
	public void testLineInstr() throws Exception
	{
		log.info("Testing line-instruction...");
		
		for (int i = 0; i < NUMBER_ITERATIONS; ++i)
			this.runLineInstrTest();
		
		this.markAsPassed();
	}
	
	@Test
	public void testRectInstr() throws Exception
	{
		log.info("Testing rect-instruction...");
		
		for (int i = 0; i < NUMBER_ITERATIONS; ++i)
			this.runRectInstrTest();
		
		this.markAsPassed();
	}
	
	@Test
	public void testGenericPath() throws Exception
	{
		log.info("Testing generic path...");
		
		for (int i = 0; i < NUMBER_ITERATIONS; ++i)
			this.runGenericPathTest();
		
		this.markAsPassed();
	}
	
	
	/* ==================== TEST RUNNERS ==================== */
	
	public void runCurveInstrTest() throws Exception
	{
		final int dx = CANVAS_WIDTH  / 4;
		final int dy = CANVAS_HEIGHT / 4;
		final int ry = 2 * dy;
		
		// Curve's parameters
		final int x1  = random.nextInt(dx);
		final int y1  = random.nextInt(CANVAS_HEIGHT);
		final int cx1 = dx + random.nextInt(dx);
		final int cy1 = dy + random.nextInt(ry);
		final int cx2 = 2*dx + random.nextInt(dx);
		final int cy2 = dy + random.nextInt(ry);
		final int x2  = 3*dx + random.nextInt(dx);
		final int y2  = dy + random.nextInt(ry);
		
		// Stroke's parameters
		final int thickness = this.nextThickness();
		final int cap = this.nextCapStyle();
		final int join = this.nextJoinStyle();
		final int r = this.nextColorSample();
		final int g = this.nextColorSample();
		final int b = this.nextColorSample();
		final int a = this.nextColorSample();

		// Construct curve-instruction
		final Instruction curveInstr = new Instruction(7);
		ibuilder.start(OpCode.CURVE, curveInstr);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.addArgument(cx1);
		ibuilder.addArgument(cy1);
		ibuilder.addArgument(cx2);
		ibuilder.addArgument(cy2);
		ibuilder.addArgument(x2);
		ibuilder.addArgument(y2);
		ibuilder.finish(false);

		final CubicCurve2D curveShape = new CubicCurve2D.Double(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
		final CurveInstrHandler curveHandler = new CurveInstrHandler(canvas);
		
		// Stroke path
		{
			this.resetCanvas();
			this.startPath(x1, y1);
			curveHandler.execute(null, curveInstr);
			
			BufferedImage actimg = this.stroke(thickness, cap, join, r, g, b, a);
			BufferedImage expimg = this.stroke(thickness, cap, join, r, g, b, a, curveShape);
			DrawingInstrHandlerTest.compare(expimg, actimg);
		}
		
		// Fill path
		{
			this.resetCanvas();
			this.startPath(x1, y1);
			curveHandler.execute(null, curveInstr);
			
			BufferedImage actimg = this.fill(r, g, b, a);
			BufferedImage expimg = this.fill(r, g, b, a, curveShape);
			DrawingInstrHandlerTest.compare(expimg, actimg);
		}
	}
	
	public void runLineInstrTest() throws Exception
	{
		final int dx = CANVAS_WIDTH / 2;
		
		// Line's parameters
		final int x1 = random.nextInt(dx);
		final int y1 = random.nextInt(CANVAS_HEIGHT);
		final int x2 = dx + random.nextInt(dx);
		final int y2 = random.nextInt(CANVAS_HEIGHT);

		// Stroke's parameters
		final int thickness = this.nextThickness();
		final int cap = this.nextCapStyle();
		final int join = this.nextJoinStyle();
		final int r = this.nextColorSample();
		final int g = this.nextColorSample();
		final int b = this.nextColorSample();
		final int a = this.nextColorSample();

		// Construct line-instruction
		final Instruction lineInstr = new Instruction(7);
		ibuilder.start(OpCode.LINE, lineInstr);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.addArgument(x2);
		ibuilder.addArgument(y2);
		ibuilder.finish(false);

		final LineInstrHandler lineHandler = new LineInstrHandler(canvas);
		final Line2D lineShape = new Line2D.Double(x1, y1, x2, y2);
		
		// Stroke path
		{
			this.resetCanvas();
			this.startPath(x1, y1);
			lineHandler.execute(null, lineInstr);
			
			BufferedImage actimg = this.stroke(thickness, cap, join, r, g, b, a);
			BufferedImage expimg = this.stroke(thickness, cap, join, r, g, b, a, lineShape);
			DrawingInstrHandlerTest.compare(expimg, actimg);
		}
	}
	
	public void runRectInstrTest() throws Exception
	{
		// Rectangle's parameters
		final int x = random.nextInt(CANVAS_WIDTH  / 2);
		final int y = random.nextInt(CANVAS_HEIGHT / 2);
		final int width = random.nextInt(CANVAS_WIDTH - x);
		final int height = random.nextInt(CANVAS_HEIGHT - y);

		// Stroke's parameters
		final int thickness = this.nextThickness();
		final int cap = this.nextCapStyle();
		final int join = this.nextJoinStyle();
		final int r = this.nextColorSample();
		final int g = this.nextColorSample();
		final int b = this.nextColorSample();
		final int a = this.nextColorSample();

		// Construct rect-instruction
		final Instruction rectInstr = new Instruction(6);
		ibuilder.start(OpCode.RECT, rectInstr);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.addArgument(x);
		ibuilder.addArgument(y);
		ibuilder.addArgument(width);
		ibuilder.addArgument(height);
		ibuilder.finish(false);

		final RectInstrHandler rectHandler = new RectInstrHandler(canvas);
		final Rectangle2D rectShape = new Rectangle2D.Double(x, y, width, height);
		
		// Stroke path
		{
			this.resetCanvas();
			rectHandler.execute(null, rectInstr);
			
			BufferedImage actimg = this.stroke(thickness, cap, join, r, g, b, a);
			BufferedImage expimg = this.stroke(thickness, cap, join, r, g, b, a, rectShape);
			DrawingInstrHandlerTest.compare(expimg, actimg);
		}

		// Fill path
		{
			this.resetCanvas();
			rectHandler.execute(null, rectInstr);

			BufferedImage actimg = this.fill(r, g, b, a);
			BufferedImage expimg = this.fill(r, g, b, a, rectShape);
			DrawingInstrHandlerTest.compare(expimg, actimg);
		}
	}
	
	public void runGenericPathTest() throws Exception
	{
		final int dx = CANVAS_WIDTH  / 4;
		final int dy = CANVAS_HEIGHT / 4;
		final int ry = 2 * dy;
		
		// Curve's parameters
		final int x1  = random.nextInt(dx);
		final int y1  = random.nextInt(CANVAS_HEIGHT);
		final int cx1 = dx + random.nextInt(dx);
		final int cy1 = dy + random.nextInt(ry);
		final int cx2 = 2*dx + random.nextInt(dx);
		final int cy2 = dy + random.nextInt(ry);
		final int x2  = 3*dx + random.nextInt(dx);
		final int y2  = dy + random.nextInt(ry);
		
		// Line's parameters
		final int x3 = random.nextInt(CANVAS_WIDTH);
		final int y3 = random.nextInt(CANVAS_HEIGHT);
		final int x4 = random.nextInt(CANVAS_WIDTH);
		final int y4 = random.nextInt(CANVAS_HEIGHT);
				
		// Stroke's parameters
		final int thickness = this.nextThickness();
		final int cap = this.nextCapStyle();
		final int join = this.nextJoinStyle();
		final int r = this.nextColorSample();
		final int g = this.nextColorSample();
		final int b = this.nextColorSample();
		final int a = this.nextColorSample();

		// Construct curve-instruction
		final Instruction curveInstr = new Instruction(7);
		ibuilder.start(OpCode.CURVE, curveInstr);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.addArgument(cx1);
		ibuilder.addArgument(cy1);
		ibuilder.addArgument(cx2);
		ibuilder.addArgument(cy2);
		ibuilder.addArgument(x2);
		ibuilder.addArgument(y2);
		ibuilder.finish(false);

		// Construct 1. line-instruction
		final Instruction lineInstr1 = new Instruction(3);
		ibuilder.start(OpCode.CURVE, lineInstr1);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.addArgument(x3);
		ibuilder.addArgument(y3);
		ibuilder.finish(false);

		// Construct 2. line-instruction
		final Instruction lineInstr2 = new Instruction(3);
		ibuilder.start(OpCode.CURVE, lineInstr2);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.addArgument(x4);
		ibuilder.addArgument(y4);
		ibuilder.finish(false);

		final CubicCurve2D curveShape = new CubicCurve2D.Double(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
		final CurveInstrHandler curveHandler = new CurveInstrHandler(canvas);
		final LineInstrHandler lineHandler = new LineInstrHandler(canvas);
		
		// Construct path
		final Path2D shape = new Path2D.Double();
		shape.append(curveShape, false);
		shape.lineTo(x3, y3);
		shape.lineTo(x4, y4);
		
		// Stroke path
		{
			this.resetCanvas();
			this.startPath(x1, y1);
			curveHandler.execute(null, curveInstr);
			lineHandler.execute(null, lineInstr1);
			lineHandler.execute(null, lineInstr2);
			
			BufferedImage actimg = this.stroke(thickness, cap, join, r, g, b, a);
			BufferedImage expimg = this.stroke(thickness, cap, join, r, g, b, a, shape);
			DrawingInstrHandlerTest.compare(expimg, actimg);
		}
		
		// Fill path
		{
			this.resetCanvas();
			this.startPath(x1, y1);
			curveHandler.execute(null, curveInstr);
			lineHandler.execute(null, lineInstr1);
			lineHandler.execute(null, lineInstr2);
			this.closePath();
			shape.closePath();
			
			BufferedImage actimg = this.fill(r, g, b, a);
			BufferedImage expimg = this.fill(r, g, b, a, shape);
			DrawingInstrHandlerTest.compare(expimg, actimg);
		}
	}
	
	
	/* ==================== INTERNAL METHODS ==================== */
	
	private void resetCanvas()
	{
		canvas.resize(MAIN_LAYER, CANVAS_WIDTH, CANVAS_HEIGHT);
	}
	
	private void startPath(int x, int y) throws Exception
	{
		Instruction instr = new Instruction(3);
		ibuilder.start(OpCode.START, instr);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.addArgument(x);
		ibuilder.addArgument(y);
		ibuilder.finish();
		
		StartInstrHandler handler = new StartInstrHandler(canvas);
		handler.execute(null, instr);
	}
	
	private void closePath() throws Exception
	{
		Instruction instr = new Instruction(1);
		ibuilder.start(OpCode.CLOSE, instr);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.finish();
		
		CloseInstrHandler handler = new CloseInstrHandler(canvas);
		handler.execute(null, instr);
	}
	
	private BufferedImage stroke(int thickness, int cap, int join, int r, int g, int b, int a, Shape shape)
	{
		BufferedImage image = canvas.newBufferedImage();
		Graphics2D graphics = image.createGraphics();
		graphics.setStroke(new BasicStroke(thickness, AwtUtils.toCapStyle(cap), AwtUtils.toJoinStyle(join)));
		graphics.setColor(new Color(r, g, b, a));
		graphics.setComposite(AwtUtils.toAlphaComposite(DEFAULT_COMPOSITE_MODE));
		graphics.draw(shape);
		graphics.dispose();
		
		return image;
	}
	
	private BufferedImage stroke(int thickness, int cap, int join, int r, int g, int b, int a) throws Exception
	{
		Instruction instr = new Instruction(9);
		ibuilder.start(OpCode.CSTROKE, instr);
		ibuilder.addArgument(DEFAULT_COMPOSITE_MODE);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.addArgument(cap);
		ibuilder.addArgument(join);
		ibuilder.addArgument(thickness);
		ibuilder.addArgument(r);
		ibuilder.addArgument(g);
		ibuilder.addArgument(b);
		ibuilder.addArgument(a);
		ibuilder.finish();
		
		cstrokeInstrHandler.execute(null, instr);
		canvas.render();
		return canvas.getBufferedImage();
	}
	
	private BufferedImage fill(int r, int g, int b, int a, Shape shape)
	{
		BufferedImage image = canvas.newBufferedImage();
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(new Color(r, g, b, a));
		graphics.setComposite(AwtUtils.toAlphaComposite(DEFAULT_COMPOSITE_MODE));
		graphics.fill(shape);
		graphics.dispose();
		
		return image;
	}
	
	private BufferedImage fill(int r, int g, int b, int a) throws Exception
	{
		Instruction instr = new Instruction(6);
		ibuilder.start(OpCode.CFILL, instr);
		ibuilder.addArgument(DEFAULT_COMPOSITE_MODE);
		ibuilder.addArgument(MAIN_LAYER);
		ibuilder.addArgument(r);
		ibuilder.addArgument(g);
		ibuilder.addArgument(b);
		ibuilder.addArgument(a);
		ibuilder.finish();
		
		cfillInstrHandler.execute(null, instr);
		canvas.render();
		return canvas.getBufferedImage();
	}
	
	private int nextThickness()
	{
		return (1 + random.nextInt(8));
	}
	
	private int nextCapStyle()
	{
		return random.nextInt(3);
	}
	
	private int nextJoinStyle()
	{
		return random.nextInt(3);
	}
	
	private int nextColorSample()
	{
		return random.nextInt(256);
	}
	
	private static void compare(BufferedImage expimg, BufferedImage actimg)
	{
		for (int y = 0; y < CANVAS_HEIGHT; ++y) {
			for (int x = 0; x < CANVAS_WIDTH; ++x)
				Assert.assertTrue("Pixels are different!", expimg.getRGB(x, y) == actimg.getRGB(x, y));
		}
	}
}
