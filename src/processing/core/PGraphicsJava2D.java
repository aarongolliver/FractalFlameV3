/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 * Part of the Processing project - http://processing.org Copyright (c) 2005-11 Ben Fry and Casey
 * Reas This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version. This library is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite
 * 330, Boston, MA 02111-1307 USA
 */

package processing.core;

import java.awt.BasicStroke;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import processing.data.XML;

/**
 * Subclass for PGraphics that implements the graphics API using Java2D.
 * <p>
 * Pixel operations too slow? As of release 0085 (the first beta), the default renderer uses Java2D.
 * It's more accurate than the renderer used in alpha releases of Processing (it handles stroke caps
 * and joins, and has better polygon tessellation), but it's super slow for handling pixels. At
 * least until we get a chance to get the old 2D renderer (now called P2D) working in a similar
 * fashion, you can use <TT>size(w, h, P3D)</TT> instead of <TT>size(w, h)</TT> which will be faster
 * for general pixel flipping madness.
 * </p>
 * <p>
 * To get access to the Java 2D "Graphics2D" object for the default renderer, use:
 * 
 * <PRE>
 * Graphics2D g2 = ((PGraphicsJava2D) g).g2;
 * </PRE>
 * 
 * This will let you do Java 2D stuff directly, but is not supported in any way shape or form. Which
 * just means "have fun, but don't complain if it breaks."
 * </p>
 */
public class PGraphicsJava2D extends PGraphics /* PGraphics2D */{
	BufferStrategy	        strategy;
	BufferedImage	        bimage;
	VolatileImage	        vimage;
	Canvas	                canvas;
	// boolean useCanvas = true;
	boolean	                useCanvas	     = false;

	public Graphics2D	    g2;
	protected BufferedImage	offscreen;

	Composite	            defaultComposite;

	GeneralPath	            gpath;

	// / break the shape at the next vertex (next vertex() call is a moveto())
	boolean	                breakShape;

	// / coordinates for internal curve calculation
	double[]	            curveCoordX;
	double[]	            curveCoordY;
	double[]	            curveDrawX;
	double[]	            curveDrawY;

	int	                    transformCount;
	AffineTransform	        transformStack[]	= new AffineTransform[PGraphics.MATRIX_STACK_DEPTH];
	double[]	            transform	     = new double[6];

	Line2D.Float	        line	         = new Line2D.Float();
	Ellipse2D.Float	        ellipse	         = new Ellipse2D.Float();
	Rectangle2D.Float	    rect	         = new Rectangle2D.Float();
	Arc2D.Float	            arc	             = new Arc2D.Float();

	protected Color	        tintColorObject;

	protected Color	        fillColorObject;
	public boolean	        fillGradient;
	public Paint	        fillGradientObject;

	protected Color	        strokeColorObject;
	public boolean	        strokeGradient;
	public Paint	        strokeGradientObject;

	// ////////////////////////////////////////////////////////////

	// INTERNAL

	public PGraphicsJava2D() {
	}

	// public void setParent(PApplet parent)

	// public void setPrimary(boolean primary)

	// public void setPath(String path)

	/**
	 * Called in response to a resize event, handles setting the new width and height internally, as
	 * well as re-allocating the pixel buffer for the new size. Note that this will nuke any
	 * cameraMode() settings.
	 */
	@Override
	public void setSize(final int iwidth, final int iheight) { // ignore
		width = iwidth;
		height = iheight;
		// width1 = width - 1;
		// height1 = height - 1;

		allocate();
		reapplySettings();
	}

	// broken out because of subclassing for opengl
	@Override
	protected void allocate() {
		// Tried this with RGB instead of ARGB for the primarySurface version,
		// but didn't see any performance difference (OS X 10.6, Java 6u24).
		// For 0196, also attempted RGB instead of ARGB, but that causes
		// strange things to happen with blending.
		// image = new BufferedImage(width, height,
		// BufferedImage.TYPE_INT_ARGB);
		if (primarySurface) {
			if (useCanvas) {
				if (canvas != null) {
					parent.removeListeners(canvas);
					parent.remove(canvas);
				}
				canvas = new Canvas();
				canvas.setIgnoreRepaint(true);

				// parent.setLayout(new BorderLayout());
				// parent.add(canvas, BorderLayout.CENTER);
				parent.add(canvas);

				if ((canvas.getWidth() != width) || (canvas.getHeight() != height)) {
					PApplet.debug("PGraphicsJava2D comp size being set to " + width + "x" + height);
					canvas.setSize(width, height);
				} else {
					PApplet.debug("PGraphicsJava2D comp size already " + width + "x" + height);
				}

				parent.addListeners(canvas);
				// canvas.createBufferStrategy(1);
				// g2 = (Graphics2D) canvas.getGraphics();

			} else {
				parent.updateListeners(parent); // in case they're
				                                // already there

				// using a compatible image here doesn't seem to provide any
				// performance boost

				// Needs to be RGB otherwise there's a major performance hit
				// [0204]
				// http://code.google.com/p/processing/issues/detail?id=729
				image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				// GraphicsConfiguration gc = parent.getGraphicsConfiguration();
				// image = gc.createCompatibleImage(width, height);
				offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				// offscreen = gc.createCompatibleImage(width, height);
				g2 = (Graphics2D) offscreen.getGraphics();
			}
		} else {
			// Since this buffer's offscreen anyway, no need for the extra
			// offscreen
			// buffer. However, unlike the primary surface, this feller needs to
			// be
			// ARGB so that blending ("alpha" compositing) will work properly.
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			g2 = (Graphics2D) image.getGraphics();
		}
		if (!useCanvas) {
			defaultComposite = g2.getComposite();
		}

		// can't un-set this because this may be only a resize
		// http://dev.processing.org/bugs/show_bug.cgi?id=463
		// defaultsInited = false;
		// checkSettings();
		// reapplySettings = true;
	}

	// public void dispose()

	// ////////////////////////////////////////////////////////////

	// FRAME

	@Override
	public boolean canDraw() {
		return true;
	}

	@Override
	public void requestDraw() {
		// EventQueue.invokeLater(new Runnable() {
		// public void run() {
		parent.handleDraw();
		// }
		// });
	}

	@Override
	public void beginDraw() {
		if (useCanvas && primarySurface) {
			if (parent.frameCount == 0) {
				canvas.createBufferStrategy(2);
				strategy = canvas.getBufferStrategy();
				PApplet.debug("PGraphicsJava2D.beginDraw() strategy is " + strategy);
				BufferCapabilities caps = strategy.getCapabilities();
				caps = strategy.getCapabilities();
				PApplet.debug("PGraphicsJava2D.beginDraw() caps are " + " flipping: " + caps.isPageFlipping()
				        + " front/back accel: " + caps.getFrontBufferCapabilities().isAccelerated() + " " + "/"
				        + caps.getBackBufferCapabilities().isAccelerated());
			}
			final GraphicsConfiguration gc = canvas.getGraphicsConfiguration();
			// if (vimage == null || vimage.validate(gc) ==
			// VolatileImage.IMAGE_INCOMPATIBLE) {
			// vimage = gc.createCompatibleVolatileImage(width, height);
			// }
			// g2 = (Graphics2D) vimage.getGraphics();

			if ((bimage == null) || (bimage.getWidth() != width) || (bimage.getHeight() != height)) {
				PApplet.debug("PGraphicsJava2D creating new image");
				bimage = gc.createCompatibleImage(width, height);
				// image = new BufferedImage(width, height,
				// BufferedImage.TYPE_INT_ARGB);
				g2 = bimage.createGraphics();
				defaultComposite = g2.getComposite();
			}
		}

		checkSettings();
		resetMatrix(); // reset model matrix
		vertexCount = 0;
	}

	@Override
	public void endDraw() {
		// hm, mark pixels as changed, because this will instantly do a full
		// copy of all the pixels to the surface.. so that's kind of a mess.
		// updatePixels();

		if (primarySurface) {
			// if (canvas != null) {
			if (useCanvas) {
				// System.out.println(canvas);

				// alternate version
				// canvas.repaint(); // ?? what to do for swapping buffers

				redraw();

			} else {
				// don't copy the pixels/data elements of the buffered image
				// directly,
				// since it'll disable the nice speedy pipeline stuff, sending
				// all drawing
				// into a world of suck that's rough 6 trillion times slower.
				synchronized (image) {
					// System.out.println("inside j2d sync");
					image.getGraphics().drawImage(offscreen, 0, 0, null);
				}
			}
		} else {
			// TODO this is probably overkill for most tasks...
			loadPixels();
		}

		// Marking as modified, and then calling updatePixels() in
		// the super class, which just sets the mx1, my1, mx2, my2
		// coordinates of the modified area. This avoids doing the
		// full copy of the pixels to the surface in this.updatePixels().
		this.setModified();
		super.updatePixels();
	}

	private void redraw() {
		// only need this check if the validate() call will use redraw()
		// if (strategy == null) return;
		do {
			PApplet.debug("PGraphicsJava2D.redraw() top of outer do { } block");
			do {
				PApplet.debug("PGraphicsJava2D.redraw() top of inner do { } block");
				System.out.println("strategy is " + strategy);
				final Graphics bsg = strategy.getDrawGraphics();
				if (vimage != null) {
					bsg.drawImage(vimage, 0, 0, null);
				} else {
					bsg.drawImage(bimage, 0, 0, null);
					// if (parent.frameCount == 0) {
					// try {
					// ImageIO.write(image, "jpg", new
					// java.io.File("/Users/fry/Desktop/buff.jpg"));
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
					// }
				}
				bsg.dispose();

				// the strategy version
				// g2.dispose();
				// if (!strategy.contentsLost()) {
				// if (parent.frameCount != 0) {
				// Toolkit.getDefaultToolkit().sync();
				// }
				// } else {
				// System.out.println("XXXXX strategy contents lost");
				// }
				// }
				// }
			} while (strategy.contentsRestored());

			PApplet.debug("PGraphicsJava2D.redraw() showing strategy");
			strategy.show();

		} while (strategy.contentsLost());
		PApplet.debug("PGraphicsJava2D.redraw() out of do { } block");
	}

	// ////////////////////////////////////////////////////////////

	// SETTINGS

	// protected void checkSettings()

	// protected void defaultSettings()

	// protected void reapplySettings()

	// ////////////////////////////////////////////////////////////

	// HINT

	@Override
	public void hint(final int which) {
		// take care of setting the hint
		super.hint(which);

		// Avoid badness when drawing shorter strokes.
		// http://code.google.com/p/processing/issues/detail?id=1068
		// Unfortunately cannot always be enabled, because it makes the
		// stroke in many standard Processing examples really gross.
		if (which == PConstants.ENABLE_STROKE_PURE) {
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		} else if (which == PConstants.DISABLE_STROKE_PURE) {
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
		}
	}

	// ////////////////////////////////////////////////////////////

	// SHAPES

	// public void beginShape(int kind)

	@Override
	public void beginShape(final int kind) {
		// super.beginShape(kind);
		shape = kind;
		vertexCount = 0;
		curveVertexCount = 0;

		// set gpath to null, because when mixing curves and straight
		// lines, vertexCount will be set back to zero, so vertexCount == 1
		// is no longer a good indicator of whether the shape is new.
		// this way, just check to see if gpath is null, and if it isn't
		// then just use it to continue the shape.
		gpath = null;
	}

	// public boolean edge(boolean e)

	// public void normal(double nx, double ny, double nz) {

	// public void textureMode(int mode)

	@Override
	public void texture(final PImage image) {
		PGraphics.showMethodWarning("texture");
	}

	@Override
	public void vertex(final double x, final double y) {
		curveVertexCount = 0;
		// double vertex[];

		if (vertexCount == vertices.length) {
			final double temp[][] = new double[vertexCount << 1][PGraphics.VERTEX_FIELD_COUNT];
			System.arraycopy(vertices, 0, temp, 0, vertexCount);
			vertices = temp;
			// message(CHATTER, "allocating more vertices " + vertices.length);
		}
		// not everyone needs this, but just easier to store rather
		// than adding another moving part to the code...
		vertices[vertexCount][PConstants.X] = x;
		vertices[vertexCount][PConstants.Y] = y;
		vertexCount++;

		switch (shape) {

		case POINTS:
			this.point(x, y);
			break;

		case LINES:
			if ((vertexCount % 2) == 0) {
				this.line(vertices[vertexCount - 2][PConstants.X], vertices[vertexCount - 2][PConstants.Y], x, y);
			}
			break;

		case TRIANGLES:
			if ((vertexCount % 3) == 0) {
				triangle(vertices[vertexCount - 3][PConstants.X], vertices[vertexCount - 3][PConstants.Y],
				        vertices[vertexCount - 2][PConstants.X], vertices[vertexCount - 2][PConstants.Y], x, y);
			}
			break;

		case TRIANGLE_STRIP:
			if (vertexCount >= 3) {
				triangle(vertices[vertexCount - 2][PConstants.X], vertices[vertexCount - 2][PConstants.Y],
				        vertices[vertexCount - 1][PConstants.X], vertices[vertexCount - 1][PConstants.Y],
				        vertices[vertexCount - 3][PConstants.X], vertices[vertexCount - 3][PConstants.Y]);
			}
			break;

		case TRIANGLE_FAN:
			if (vertexCount >= 3) {
				// This is an unfortunate implementation because the stroke for
				// an
				// adjacent triangle will be repeated. However, if the stroke is
				// not
				// redrawn, it will replace the adjacent line (when it lines up
				// perfectly) or show a faint line (when off by a small amount).
				// The alternative would be to wait, then draw the shape as a
				// polygon fill, followed by a series of vertices. But that's a
				// poor method when used with PDF, DXF, or other recording
				// objects,
				// since discrete triangles would likely be preferred.
				triangle(vertices[0][PConstants.X], vertices[0][PConstants.Y], vertices[vertexCount - 2][PConstants.X],
				        vertices[vertexCount - 2][PConstants.Y], x, y);
			}
			break;

		case QUAD:
		case QUADS:
			if ((vertexCount % 4) == 0) {
				quad(vertices[vertexCount - 4][PConstants.X], vertices[vertexCount - 4][PConstants.Y],
				        vertices[vertexCount - 3][PConstants.X], vertices[vertexCount - 3][PConstants.Y],
				        vertices[vertexCount - 2][PConstants.X], vertices[vertexCount - 2][PConstants.Y], x, y);
			}
			break;

		case QUAD_STRIP:
			// 0---2---4
			// | | |
			// 1---3---5
			if ((vertexCount >= 4) && ((vertexCount % 2) == 0)) {
				quad(vertices[vertexCount - 4][PConstants.X], vertices[vertexCount - 4][PConstants.Y],
				        vertices[vertexCount - 2][PConstants.X], vertices[vertexCount - 2][PConstants.Y], x, y,
				        vertices[vertexCount - 3][PConstants.X], vertices[vertexCount - 3][PConstants.Y]);
			}
			break;

		case POLYGON:
			if (gpath == null) {
				gpath = new GeneralPath();
				gpath.moveTo(x, y);
			} else if (breakShape) {
				gpath.moveTo(x, y);
				breakShape = false;
			} else {
				gpath.lineTo(x, y);
			}
			break;
		}
	}

	@Override
	public void vertex(final double x, final double y, final double z) {
		PGraphics.showDepthWarningXYZ("vertex");
	}

	@Override
	public void vertex(final double[] v) {
		this.vertex(v[PConstants.X], v[PConstants.Y]);
	}

	@Override
	public void vertex(final double x, final double y, final double u, final double v) {
		PGraphics.showVariationWarning("vertex(x, y, u, v)");
	}

	@Override
	public void vertex(final double x, final double y, final double z, final double u, final double v) {
		PGraphics.showDepthWarningXYZ("vertex");
	}

	@Override
	public void beginContour() {
		breakShape = true;
	}

	@Override
	public void endContour() {
		// does nothing, just need the break in beginContour()
	}

	@Override
	public void endShape(final int mode) {
		if (gpath != null) { // make sure something has been drawn
			if (shape == PConstants.POLYGON) {
				if (mode == PConstants.CLOSE) {
					gpath.closePath();
				}
				drawShape(gpath);
			}
		}
		shape = 0;
	}

	// ////////////////////////////////////////////////////////////

	// CLIPPING

	@Override
	protected void clipImpl(final double x1, final double y1, final double x2, final double y2) {
		g2.setClip(new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1));
	}

	@Override
	public void noClip() {
		g2.setClip(null);
	}

	// ////////////////////////////////////////////////////////////

	// BLEND

	/**
	 * ( begin auto-generated from blendMode.xml ) This is a new reference entry for Processing 2.0.
	 * It will be updated shortly. ( end auto-generated )
	 * 
	 * @webref Rendering
	 * @param mode
	 *            the blending mode to use
	 */
	@Override
	public void blendMode(final int mode) {
		if (mode == PConstants.BLEND) {
			g2.setComposite(defaultComposite);

		} else {
			g2.setComposite(new Composite() {

				@Override
				public CompositeContext createContext(final ColorModel srcColorModel, final ColorModel dstColorModel,
				        final RenderingHints hints) {
					return new BlendingContext(mode);
				}
			});
		}
	}

	// Blending implementation cribbed from portions of Romain Guy's
	// demo and terrific writeup on blending modes in Java 2D.
	// http://www.curious-creature.org/2006/09/20/new-blendings-modes-for-java2d/
	private static final class BlendingContext implements CompositeContext {
		private final int	mode;

		private BlendingContext(final int mode) {
			this.mode = mode;
		}

		public void dispose() {
		}

		public void compose(final Raster src, final Raster dstIn, final WritableRaster dstOut) {
			// not sure if this is really necessary, since we control our
			// buffers
			if ((src.getSampleModel().getDataType() != DataBuffer.TYPE_INT)
			        || (dstIn.getSampleModel().getDataType() != DataBuffer.TYPE_INT)
			        || (dstOut.getSampleModel().getDataType() != DataBuffer.TYPE_INT)) {
				throw new IllegalStateException("Source and destination must store pixels as INT.");
			}

			final int width = Math.min(src.getWidth(), dstIn.getWidth());
			final int height = Math.min(src.getHeight(), dstIn.getHeight());

			final int[] srcPixels = new int[width];
			final int[] dstPixels = new int[width];

			for (int y = 0; y < height; y++) {
				src.getDataElements(0, y, width, 1, srcPixels);
				dstIn.getDataElements(0, y, width, 1, dstPixels);
				for (int x = 0; x < width; x++) {
					dstPixels[x] = PImage.blendColor(srcPixels[x], dstPixels[x], mode);
				}
				dstOut.setDataElements(0, y, width, 1, dstPixels);
			}
		}
	}

	// ////////////////////////////////////////////////////////////

	// BEZIER VERTICES

	@Override
	public void bezierVertex(final double x1, final double y1, final double x2, final double y2, final double x3,
	        final double y3) {
		this.bezierVertexCheck();
		gpath.curveTo(x1, y1, x2, y2, x3, y3);
	}

	@Override
	public void bezierVertex(final double x2, final double y2, final double z2, final double x3, final double y3,
	        final double z3, final double x4, final double y4, final double z4) {
		PGraphics.showDepthWarningXYZ("bezierVertex");
	}

	// ////////////////////////////////////////////////////////////

	// QUADRATIC BEZIER VERTICES

	@Override
	public void quadraticVertex(final double ctrlX, final double ctrlY, final double endX, final double endY) {
		this.bezierVertexCheck();
		final Point2D cur = gpath.getCurrentPoint();

		final double x1 = cur.getX();
		final double y1 = cur.getY();

		this.bezierVertex(x1 + (((ctrlX - x1) * 2) / 3.0f), y1 + (((ctrlY - y1) * 2) / 3.0f), endX
		        + (((ctrlX - endX) * 2) / 3.0f), endY + (((ctrlY - endY) * 2) / 3.0f), endX, endY);
	}

	@Override
	public void quadraticVertex(final double x2, final double y2, final double z2, final double x4, final double y4,
	        final double z4) {
		PGraphics.showDepthWarningXYZ("quadVertex");
	}

	// ////////////////////////////////////////////////////////////

	// CURVE VERTICES

	@Override
	protected void curveVertexCheck() {
		super.curveVertexCheck();

		if (curveCoordX == null) {
			curveCoordX = new double[4];
			curveCoordY = new double[4];
			curveDrawX = new double[4];
			curveDrawY = new double[4];
		}
	}

	@Override
	protected void curveVertexSegment(final double x1, final double y1, final double x2, final double y2,
	        final double x3, final double y3, final double x4, final double y4) {
		curveCoordX[0] = x1;
		curveCoordY[0] = y1;

		curveCoordX[1] = x2;
		curveCoordY[1] = y2;

		curveCoordX[2] = x3;
		curveCoordY[2] = y3;

		curveCoordX[3] = x4;
		curveCoordY[3] = y4;

		curveToBezierMatrix.mult(curveCoordX, curveDrawX);
		curveToBezierMatrix.mult(curveCoordY, curveDrawY);

		// since the paths are continuous,
		// only the first point needs the actual moveto
		if (gpath == null) {
			gpath = new GeneralPath();
			gpath.moveTo(curveDrawX[0], curveDrawY[0]);
		}

		gpath.curveTo(curveDrawX[1], curveDrawY[1], curveDrawX[2], curveDrawY[2], curveDrawX[3], curveDrawY[3]);
	}

	@Override
	public void curveVertex(final double x, final double y, final double z) {
		PGraphics.showDepthWarningXYZ("curveVertex");
	}

	// ////////////////////////////////////////////////////////////

	// RENDERER

	// public void flush()

	// ////////////////////////////////////////////////////////////

	// POINT, LINE, TRIANGLE, QUAD

	@Override
	public void point(final double x, final double y) {
		if (stroke) {
			// if (strokeWeight > 1) {
			this.line(x, y, x + PConstants.EPSILON, y + PConstants.EPSILON);
			// } else {
			// set((int) screenX(x, y), (int) screenY(x, y), strokeColor);
			// }
		}
	}

	@Override
	public void line(final double x1, final double y1, final double x2, final double y2) {
		line.setLine(x1, y1, x2, y2);
		strokeShape(line);
	}

	@Override
	public void triangle(final double x1, final double y1, final double x2, final double y2, final double x3,
	        final double y3) {
		gpath = new GeneralPath();
		gpath.moveTo(x1, y1);
		gpath.lineTo(x2, y2);
		gpath.lineTo(x3, y3);
		gpath.closePath();
		drawShape(gpath);
	}

	@Override
	public void quad(final double x1, final double y1, final double x2, final double y2, final double x3,
	        final double y3, final double x4, final double y4) {
		final GeneralPath gp = new GeneralPath();
		gp.moveTo(x1, y1);
		gp.lineTo(x2, y2);
		gp.lineTo(x3, y3);
		gp.lineTo(x4, y4);
		gp.closePath();
		drawShape(gp);
	}

	// ////////////////////////////////////////////////////////////

	// RECT

	// public void rectMode(int mode)

	// public void rect(double a, double b, double c, double d)

	@Override
	protected void rectImpl(final double x1, final double y1, final double x2, final double y2) {
		rect.setFrame(x1, y1, x2 - x1, y2 - y1);
		drawShape(rect);
	}

	// ////////////////////////////////////////////////////////////

	// ELLIPSE

	// public void ellipseMode(int mode)

	// public void ellipse(double a, double b, double c, double d)

	@Override
	protected void ellipseImpl(final double x, final double y, final double w, final double h) {
		ellipse.setFrame(x, y, w, h);
		drawShape(ellipse);
	}

	// ////////////////////////////////////////////////////////////

	// ARC

	// public void arc(double a, double b, double c, double d,
	// double start, double stop)

	@Override
	protected void arcImpl(final double x, final double y, final double w, final double h, double start, double stop,
	        final int mode) {
		// 0 to 90 in java would be 0 to -90 for p5 renderer
		// but that won't work, so -90 to 0?

		start = -start * PConstants.RAD_TO_DEG;
		stop = -stop * PConstants.RAD_TO_DEG;

		// ok to do this because already checked for NaN
		// while (start < 0) {
		// start += 360;
		// stop += 360;
		// }
		// if (start > stop) {
		// double temp = start;
		// start = stop;
		// stop = temp;
		// }
		final double sweep = stop - start;

		// The defaults, before 2.0b7, were to stroke as Arc2D.OPEN, and then
		// fill
		// using Arc2D.PIE. That's a little wonky, but it's here for
		// compatability.
		int fillMode = Arc2D.PIE;
		int strokeMode = Arc2D.OPEN;

		if (mode == PConstants.OPEN) {
			fillMode = Arc2D.OPEN;
			// strokeMode = Arc2D.OPEN;

		} else if (mode == PConstants.PIE) {
			// fillMode = Arc2D.PIE;
			strokeMode = Arc2D.PIE;

		} else if (mode == PConstants.CHORD) {
			fillMode = Arc2D.CHORD;
			strokeMode = Arc2D.CHORD;
		}

		if (fill) {
			// System.out.println("filla");
			arc.setArc(x, y, w, h, start, sweep, fillMode);
			fillShape(arc);
		}
		if (stroke) {
			// System.out.println("strokey");
			arc.setArc(x, y, w, h, start, sweep, strokeMode);
			strokeShape(arc);
		}
	}

	// ////////////////////////////////////////////////////////////

	// JAVA2D SHAPE/PATH HANDLING

	protected void fillShape(final Shape s) {
		if (fillGradient) {
			g2.setPaint(fillGradientObject);
			g2.fill(s);
		} else if (fill) {
			g2.setColor(fillColorObject);
			g2.fill(s);
		}
	}

	protected void strokeShape(final Shape s) {
		if (strokeGradient) {
			g2.setPaint(strokeGradientObject);
			g2.draw(s);
		} else if (stroke) {
			g2.setColor(strokeColorObject);
			g2.draw(s);
		}
	}

	protected void drawShape(final Shape s) {
		if (fillGradient) {
			g2.setPaint(fillGradientObject);
			g2.fill(s);
		} else if (fill) {
			g2.setColor(fillColorObject);
			g2.fill(s);
		}
		if (strokeGradient) {
			g2.setPaint(strokeGradientObject);
			g2.draw(s);
		} else if (stroke) {
			g2.setColor(strokeColorObject);
			g2.draw(s);
		}
	}

	// ////////////////////////////////////////////////////////////

	// BOX

	// public void box(double size)

	@Override
	public void box(final double w, final double h, final double d) {
		PGraphics.showMethodWarning("box");
	}

	// ////////////////////////////////////////////////////////////

	// SPHERE

	// public void sphereDetail(int res)

	// public void sphereDetail(int ures, int vres)

	@Override
	public void sphere(final double r) {
		PGraphics.showMethodWarning("sphere");
	}

	// ////////////////////////////////////////////////////////////

	// BEZIER

	// public double bezierPoint(double a, double b, double c, double d, double
	// t)

	// public double bezierTangent(double a, double b, double c, double d,
	// double t)

	// protected void bezierInitCheck()

	// protected void bezierInit()

	/** Ignored (not needed) in Java 2D. */
	@Override
	public void bezierDetail(final int detail) {
	}

	// public void bezier(double x1, double y1,
	// double x2, double y2,
	// double x3, double y3,
	// double x4, double y4)

	// public void bezier(double x1, double y1, double z1,
	// double x2, double y2, double z2,
	// double x3, double y3, double z3,
	// double x4, double y4, double z4)

	// ////////////////////////////////////////////////////////////

	// CURVE

	// public double curvePoint(double a, double b, double c, double d, double
	// t)

	// public double curveTangent(double a, double b, double c, double d, double
	// t)

	/** Ignored (not needed) in Java 2D. */
	@Override
	public void curveDetail(final int detail) {
	}

	// public void curveTightness(double tightness)

	// protected void curveInitCheck()

	// protected void curveInit()

	// public void curve(double x1, double y1,
	// double x2, double y2,
	// double x3, double y3,
	// double x4, double y4)

	// public void curve(double x1, double y1, double z1,
	// double x2, double y2, double z2,
	// double x3, double y3, double z3,
	// double x4, double y4, double z4)

	// ////////////////////////////////////////////////////////////

	// SMOOTH

	@Override
	public void smooth() {
		smooth = true;

		if (quality == 0) {
			quality = 4; // change back to bicubic
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, quality == 4 ? RenderingHints.VALUE_INTERPOLATION_BICUBIC
		        : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		// g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
		// RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	}

	@Override
	public void smooth(final int antialias) {
		quality = antialias;
		if (antialias == 0) {
			noSmooth();
		} else {
			this.smooth();
		}
	}

	@Override
	public void noSmooth() {
		smooth = false;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}

	// ////////////////////////////////////////////////////////////

	// IMAGE

	// public void imageMode(int mode)

	// public void image(PImage image, double x, double y)

	// public void image(PImage image, double x, double y, double c, double d)

	// public void image(PImage image,
	// double a, double b, double c, double d,
	// int u1, int v1, int u2, int v2)

	/**
	 * Handle renderer-specific image drawing.
	 */
	@Override
	protected void imageImpl(final PImage who, final double x1, final double y1, final double x2, final double y2,
	        final int u1, final int v1, final int u2, final int v2) {
		// Image not ready yet, or an error
		if ((who.width <= 0) || (who.height <= 0)) {
			return;
		}

		if (getCache(who) == null) {
			// System.out.println("making new image cache");
			setCache(who, new ImageCache(who));
			who.updatePixels(); // mark the whole thing for update
			who.modified = true;
		}

		final ImageCache cash = (ImageCache) getCache(who);
		// if image previously was tinted, or the color changed
		// or the image was tinted, and tint is now disabled
		if ((tint && !cash.tinted) || (tint && (cash.tintedColor != tintColor)) || (!tint && cash.tinted)) {
			// for tint change, mark all pixels as needing update
			who.updatePixels();
		}

		if (who.modified) {
			cash.update(tint, tintColor);
			who.modified = false;
		}

		g2.drawImage(((ImageCache) getCache(who)).image, (int) x1, (int) y1, (int) x2, (int) y2, u1, v1, u2, v2, null);

		// every few years I think "nah, Java2D couldn't possibly be that
		// f*king slow, why are we doing this by hand? then comes the
		// affirmation.
		// Composite oldComp = null;
		// if (false && tint) {
		// oldComp = g2.getComposite();
		// int alpha = (tintColor >> 24) & 0xff;
		// System.out.println("using alpha composite");
		// Composite alphaComp =
		// AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f);
		// g2.setComposite(alphaComp);
		// }
		//
		// long t = System.currentTimeMillis();
		// g2.drawImage(who.getImage(),
		// (int) x1, (int) y1, (int) x2, (int) y2,
		// u1, v1, u2, v2, null);
		// System.out.println(System.currentTimeMillis() - t);
		//
		// if (oldComp != null) {
		// g2.setComposite(oldComp);
		// }
	}

	class ImageCache {
		PImage		  source;
		boolean		  tinted;
		int		      tintedColor;
		int		      tintedPixels[]; // one row of tinted pixels
		BufferedImage	image;

		public ImageCache(final PImage source) {
			this.source = source;
			// even if RGB, set the image type to ARGB, because the
			// image may have an alpha value for its tint().
			// int type = BufferedImage.TYPE_INT_ARGB;
			// System.out.println("making new buffered image");
			// image = new BufferedImage(source.width, source.height, type);
		}

		/**
		 * Update the pixels of the cache image. Already determined that the tint has changed, or
		 * the pixels have changed, so should just go through with the update without further
		 * checks.
		 */
		public void update(final boolean tint, final int tintColor) {
			int bufferType = BufferedImage.TYPE_INT_ARGB;
			final boolean opaque = (tintColor & 0xFF000000) == 0xFF000000;
			if (source.format == PConstants.RGB) {
				if (!tint || (tint && opaque)) {
					bufferType = BufferedImage.TYPE_INT_RGB;
				}
			}
			final boolean wrongType = (image != null) && (image.getType() != bufferType);
			if ((image == null) || wrongType) {
				image = new BufferedImage(source.width, source.height, bufferType);
			}

			final WritableRaster wr = image.getRaster();
			if (tint) {
				if ((tintedPixels == null) || (tintedPixels.length != source.width)) {
					tintedPixels = new int[source.width];
				}
				final int a2 = (tintColor >> 24) & 0xff;
				final int r2 = (tintColor >> 16) & 0xff;
				final int g2 = (tintColor >> 8) & 0xff;
				final int b2 = (tintColor) & 0xff;

				if (bufferType == BufferedImage.TYPE_INT_RGB) {
					// The target image is opaque, meaning that the source image
					// has no
					// alpha (is not ARGB), and the tint has no alpha.
					int index = 0;
					for (int y = 0; y < source.height; y++) {
						for (int x = 0; x < source.width; x++) {
							final int argb1 = source.pixels[index++];
							final int r1 = (argb1 >> 16) & 0xff;
							final int g1 = (argb1 >> 8) & 0xff;
							final int b1 = (argb1) & 0xff;

							tintedPixels[x] = // 0xFF000000 |
							(((r2 * r1) & 0xff00) << 8) | ((g2 * g1) & 0xff00) | (((b2 * b1) & 0xff00) >> 8);
						}
						wr.setDataElements(0, y, source.width, 1, tintedPixels);
					}
					// could this be any slower?
					// double[] scales = { tintR, tintG, tintB };
					// double[] offsets = new double[3];
					// RescaleOp op = new RescaleOp(scales, offsets, null);
					// op.filter(image, image);

				} else if (bufferType == BufferedImage.TYPE_INT_ARGB) {
					if ((source.format == PConstants.RGB) && ((tintColor & 0xffffff) == 0xffffff)) {
						final int hi = tintColor & 0xff000000;
						int index = 0;
						for (int y = 0; y < source.height; y++) {
							for (int x = 0; x < source.width; x++) {
								tintedPixels[x] = hi | (source.pixels[index++] & 0xFFFFFF);
							}
							wr.setDataElements(0, y, source.width, 1, tintedPixels);
						}
					} else {
						int index = 0;
						for (int y = 0; y < source.height; y++) {
							if (source.format == PConstants.RGB) {
								final int alpha = tintColor & 0xFF000000;
								for (int x = 0; x < source.width; x++) {
									final int argb1 = source.pixels[index++];
									final int r1 = (argb1 >> 16) & 0xff;
									final int g1 = (argb1 >> 8) & 0xff;
									final int b1 = (argb1) & 0xff;
									tintedPixels[x] = alpha | (((r2 * r1) & 0xff00) << 8) | ((g2 * g1) & 0xff00)
									        | (((b2 * b1) & 0xff00) >> 8);
								}
							} else if (source.format == PConstants.ARGB) {
								for (int x = 0; x < source.width; x++) {
									final int argb1 = source.pixels[index++];
									final int a1 = (argb1 >> 24) & 0xff;
									final int r1 = (argb1 >> 16) & 0xff;
									final int g1 = (argb1 >> 8) & 0xff;
									final int b1 = (argb1) & 0xff;
									tintedPixels[x] = (((a2 * a1) & 0xff00) << 16) | (((r2 * r1) & 0xff00) << 8)
									        | ((g2 * g1) & 0xff00) | (((b2 * b1) & 0xff00) >> 8);
								}
							} else if (source.format == PConstants.ALPHA) {
								final int lower = tintColor & 0xFFFFFF;
								for (int x = 0; x < source.width; x++) {
									final int a1 = source.pixels[index++];
									tintedPixels[x] = (((a2 * a1) & 0xff00) << 16) | lower;
								}
							}
							wr.setDataElements(0, y, source.width, 1, tintedPixels);
						}
					}
					// Not sure why ARGB images take the scales in this order...
					// double[] scales = { tintR, tintG, tintB, tintA };
					// double[] offsets = new double[4];
					// RescaleOp op = new RescaleOp(scales, offsets, null);
					// op.filter(image, image);
				}
			} else {
				wr.setDataElements(0, 0, source.width, source.height, source.pixels);
			}
			tinted = tint;
			tintedColor = tintColor;
		}
	}

	// ////////////////////////////////////////////////////////////

	// SHAPE

	// public void shapeMode(int mode)

	// public void shape(PShape shape)

	// public void shape(PShape shape, double x, double y)

	// public void shape(PShape shape, double x, double y, double c, double d)

	// ////////////////////////////////////////////////////////////

	// SHAPE I/O

	@Override
	public PShape loadShape(final String filename) {
		return this.loadShape(filename, null);
	}

	@Override
	public PShape loadShape(final String filename, final String options) {
		final String extension = PApplet.getExtension(filename);

		PShapeSVG svg = null;

		if (extension.equals("svg")) {
			svg = new PShapeSVG(parent.loadXML(filename));

		} else if (extension.equals("svgz")) {
			try {
				final InputStream input = new GZIPInputStream(parent.createInput(filename));
				final XML xml = new XML(input, options);
				svg = new PShapeSVG(xml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else {
			PGraphics.showWarning("Unsupported format: " + filename);
		}

		return svg;
	}

	// ////////////////////////////////////////////////////////////

	// TEXT ATTRIBTUES

	// public void textAlign(int align)

	// public void textAlign(int alignX, int alignY)

	@Override
	public double textAscent() {
		if (textFont == null) {
			this.defaultFontOrDeath("textAscent");
		}

		final Font font = (Font) textFont.getNative();
		// if (font != null && (textFont.isStream() ||
		// hints[ENABLE_NATIVE_FONTS])) {
		if (font != null) {
			final FontMetrics metrics = parent.getFontMetrics(font);
			return metrics.getAscent();
		}
		return super.textAscent();
	}

	@Override
	public double textDescent() {
		if (textFont == null) {
			this.defaultFontOrDeath("textAscent");
		}
		final Font font = (Font) textFont.getNative();
		// if (font != null && (textFont.isStream() ||
		// hints[ENABLE_NATIVE_FONTS])) {
		if (font != null) {
			final FontMetrics metrics = parent.getFontMetrics(font);
			return metrics.getDescent();
		}
		return super.textDescent();
	}

	// public void textFont(PFont which)

	// public void textFont(PFont which, double size)

	// public void textLeading(double leading)

	// public void textMode(int mode)

	@Override
	protected boolean textModeCheck(final int mode) {
		return mode == PConstants.MODEL;
	}

	/**
	 * Same as parent, but override for native version of the font.
	 * <p/>
	 * Also gets called by textFont, so the metrics will get recorded properly.
	 */
	@Override
	public void textSize(final double size) {
		if (textFont == null) {
			this.defaultFontOrDeath("textAscent", size);
		}

		// if a native version available, derive this font
		// if (textFontNative != null) {
		// textFontNative = textFontNative.deriveFont(size);
		// g2.setFont(textFontNative);
		// textFontNativeMetrics = g2.getFontMetrics(textFontNative);
		// }
		final Font font = (Font) textFont.getNative();
		// if (font != null && (textFont.isStream() ||
		// hints[ENABLE_NATIVE_FONTS])) {
		if (font != null) {
			final Font dfont = font.deriveFont((float) size);
			g2.setFont(dfont);
			textFont.setNative(dfont);
		}

		// take care of setting the textSize and textLeading vars
		// this has to happen second, because it calls textAscent()
		// (which requires the native font metrics to be set)
		super.textSize(size);
	}

	// public double textWidth(char c)

	// public double textWidth(String str)

	@Override
	protected double textWidthImpl(final char buffer[], final int start, final int stop) {
		final Font font = (Font) textFont.getNative();
		// if (font != null && (textFont.isStream() ||
		// hints[ENABLE_NATIVE_FONTS])) {
		if (font != null) {
			// maybe should use one of the newer/fancier functions for this?
			final int length = stop - start;
			final FontMetrics metrics = g2.getFontMetrics(font);
			// Using fractional metrics makes the measurement worse, not better,
			// at least on OS X 10.6 (November, 2010).
			// TextLayout returns the same value as charsWidth().
			// System.err.println("using native");
			// g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
			// RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			// double m1 = metrics.charsWidth(buffer, start, length);
			// //double m2 = (double) metrics.getStringBounds(buffer, start,
			// stop, g2).getWidth();
			// TextLayout tl = new TextLayout(new String(buffer, start, length),
			// font,
			// g2.getFontRenderContext());
			// double m2 = (double) tl.getBounds().getWidth();
			// System.err.println(m1 + " " + m2);
			// // return m1;
			// return m2;
			return metrics.charsWidth(buffer, start, length);
		}
		// System.err.println("not native");
		return super.textWidthImpl(buffer, start, stop);
	}

	protected void beginTextScreenMode() {
		loadPixels();
	}

	protected void endTextScreenMode() {
		this.updatePixels();
	}

	// ////////////////////////////////////////////////////////////

	// TEXT

	// None of the variations of text() are overridden from PGraphics.

	// ////////////////////////////////////////////////////////////

	// TEXT IMPL

	// protected void textLineAlignImpl(char buffer[], int start, int stop,
	// double x, double y)

	@Override
	protected void textLineImpl(final char buffer[], final int start, final int stop, final double x, final double y) {
		final Font font = (Font) textFont.getNative();
		// if (font != null && (textFont.isStream() ||
		// hints[ENABLE_NATIVE_FONTS])) {
		if (font != null) {
			/*
			 * // save the current setting for text smoothing. note that this is // different from
			 * the smooth() function, because the font smoothing // is controlled when the font is
			 * created, not now as it's drawn. // fixed a bug in 0116 that handled this incorrectly.
			 * Object textAntialias = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING); //
			 * override the current text smoothing setting based on the font // (don't change the
			 * global smoothing settings) g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			 * textFont.smooth ? RenderingHints.VALUE_ANTIALIAS_ON :
			 * RenderingHints.VALUE_ANTIALIAS_OFF);
			 */
			Object antialias = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			if (antialias == null) {
				// if smooth() and noSmooth() not called, this will be null
				// (0120)
				antialias = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
			}

			// override the current smoothing setting based on the font
			// also changes global setting for antialiasing, but this is because
			// it's
			// not possible to enable/disable them independently in some
			// situations.
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, textFont.smooth ? RenderingHints.VALUE_ANTIALIAS_ON
			        : RenderingHints.VALUE_ANTIALIAS_OFF);

			// System.out.println("setting frac metrics");
			// g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
			// RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			g2.setColor(fillColorObject);
			final int length = stop - start;
			g2.drawChars(buffer, start, length, (int) (x + 0.5f), (int) (y + 0.5f));
			// better to use drawString() with floats? (nope, draws the same)
			// g2.drawString(new String(buffer, start, length), x, y);

			// this didn't seem to help the scaling issue
			// and creates garbage because of the new temporary object
			// java.awt.font.GlyphVector gv =
			// textFontNative.createGlyphVector(g2.getFontRenderContext(), new
			// String(buffer, start,
			// stop));
			// g2.drawGlyphVector(gv, x, y);

			// System.out.println("text() " + new String(buffer, start, stop));

			// return to previous smoothing state if it was changed
			// g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// textAntialias);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialias);

			// textX = x + textWidthImpl(buffer, start, stop);
			// textY = y;
			// textZ = 0; // this will get set by the caller if non-zero

		} else { // otherwise just do the default
			super.textLineImpl(buffer, start, stop, x, y);
		}
	}

	// ////////////////////////////////////////////////////////////

	// MATRIX STACK

	@Override
	public void pushMatrix() {
		if (transformCount == transformStack.length) {
			throw new RuntimeException("pushMatrix() cannot use push more than " + transformStack.length + " times");
		}
		transformStack[transformCount] = g2.getTransform();
		transformCount++;
	}

	@Override
	public void popMatrix() {
		if (transformCount == 0) {
			throw new RuntimeException("missing a pushMatrix() " + "to go with that popMatrix()");
		}
		transformCount--;
		g2.setTransform(transformStack[transformCount]);
	}

	// ////////////////////////////////////////////////////////////

	// MATRIX TRANSFORMS

	@Override
	public void translate(final double tx, final double ty) {
		g2.translate(tx, ty);
	}

	// public void translate(double tx, double ty, double tz)

	@Override
	public void rotate(final double angle) {
		g2.rotate(angle);
	}

	@Override
	public void rotateX(final double angle) {
		PGraphics.showDepthWarning("rotateX");
	}

	@Override
	public void rotateY(final double angle) {
		PGraphics.showDepthWarning("rotateY");
	}

	@Override
	public void rotateZ(final double angle) {
		PGraphics.showDepthWarning("rotateZ");
	}

	@Override
	public void rotate(final double angle, final double vx, final double vy, final double vz) {
		PGraphics.showVariationWarning("rotate");
	}

	@Override
	public void scale(final double s) {
		g2.scale(s, s);
	}

	@Override
	public void scale(final double sx, final double sy) {
		g2.scale(sx, sy);
	}

	@Override
	public void scale(final double sx, final double sy, final double sz) {
		PGraphics.showDepthWarningXYZ("scale");
	}

	@Override
	public void shearX(final double angle) {
		g2.shear(Math.tan(angle), 0);
	}

	@Override
	public void shearY(final double angle) {
		g2.shear(0, Math.tan(angle));
	}

	// ////////////////////////////////////////////////////////////

	// MATRIX MORE

	@Override
	public void resetMatrix() {
		g2.setTransform(new AffineTransform());
	}

	// public void applyMatrix(PMatrix2D source)

	@Override
	public void applyMatrix(final double n00, final double n01, final double n02, final double n10, final double n11,
	        final double n12) {
		// System.out.println("PGraphicsJava2D.applyMatrix()");
		// System.out.println(new AffineTransform(n00, n10, n01, n11, n02,
		// n12));
		g2.transform(new AffineTransform(n00, n10, n01, n11, n02, n12));
		// g2.transform(new AffineTransform(n00, n01, n02, n10, n11, n12));
	}

	// public void applyMatrix(PMatrix3D source)

	@Override
	public void applyMatrix(final double n00, final double n01, final double n02, final double n03, final double n10,
	        final double n11, final double n12, final double n13, final double n20, final double n21, final double n22,
	        final double n23, final double n30, final double n31, final double n32, final double n33) {
		PGraphics.showVariationWarning("applyMatrix");
	}

	// ////////////////////////////////////////////////////////////

	// MATRIX GET/SET

	@Override
	public PMatrix getMatrix() {
		return this.getMatrix((PMatrix2D) null);
	}

	@Override
	public PMatrix2D getMatrix(PMatrix2D target) {
		if (target == null) {
			target = new PMatrix2D();
		}
		g2.getTransform().getMatrix(transform);
		target.set(transform[0], transform[2], transform[4], transform[1], transform[3], transform[5]);
		return target;
	}

	@Override
	public PMatrix3D getMatrix(final PMatrix3D target) {
		PGraphics.showVariationWarning("getMatrix");
		return target;
	}

	// public void setMatrix(PMatrix source)

	@Override
	public void setMatrix(final PMatrix2D source) {
		g2.setTransform(new AffineTransform(source.m00, source.m10, source.m01, source.m11, source.m02, source.m12));
	}

	@Override
	public void setMatrix(final PMatrix3D source) {
		PGraphics.showVariationWarning("setMatrix");
	}

	@Override
	public void printMatrix() {
		this.getMatrix((PMatrix2D) null).print();
	}

	// ////////////////////////////////////////////////////////////

	// CAMERA and PROJECTION

	// Inherit the plaintive warnings from PGraphics

	// public void beginCamera()
	// public void endCamera()
	// public void camera()
	// public void camera(double eyeX, double eyeY, double eyeZ,
	// double centerX, double centerY, double centerZ,
	// double upX, double upY, double upZ)
	// public void printCamera()

	// public void ortho()
	// public void ortho(double left, double right,
	// double bottom, double top,
	// double near, double far)
	// public void perspective()
	// public void perspective(double fov, double aspect, double near, double
	// far)
	// public void frustum(double left, double right,
	// double bottom, double top,
	// double near, double far)
	// public void printProjection()

	// ////////////////////////////////////////////////////////////

	// SCREEN and MODEL transforms

	@Override
	public double screenX(final double x, final double y) {
		g2.getTransform().getMatrix(transform);
		return (transform[0] * x) + (transform[2] * y) + transform[4];
	}

	@Override
	public double screenY(final double x, final double y) {
		g2.getTransform().getMatrix(transform);
		return (transform[1] * x) + (transform[3] * y) + transform[5];
	}

	@Override
	public double screenX(final double x, final double y, final double z) {
		PGraphics.showDepthWarningXYZ("screenX");
		return 0;
	}

	@Override
	public double screenY(final double x, final double y, final double z) {
		PGraphics.showDepthWarningXYZ("screenY");
		return 0;
	}

	@Override
	public double screenZ(final double x, final double y, final double z) {
		PGraphics.showDepthWarningXYZ("screenZ");
		return 0;
	}

	// public double modelX(double x, double y, double z)

	// public double modelY(double x, double y, double z)

	// public double modelZ(double x, double y, double z)

	// ////////////////////////////////////////////////////////////

	// STYLE

	// pushStyle(), popStyle(), style() and getStyle() inherited.

	// ////////////////////////////////////////////////////////////

	// STROKE CAP/JOIN/WEIGHT

	@Override
	public void strokeCap(final int cap) {
		super.strokeCap(cap);
		strokeImpl();
	}

	@Override
	public void strokeJoin(final int join) {
		super.strokeJoin(join);
		strokeImpl();
	}

	@Override
	public void strokeWeight(final double weight) {
		super.strokeWeight(weight);
		strokeImpl();
	}

	protected void strokeImpl() {
		int cap = BasicStroke.CAP_BUTT;
		if (strokeCap == PConstants.ROUND) {
			cap = BasicStroke.CAP_ROUND;
		} else if (strokeCap == PConstants.PROJECT) {
			cap = BasicStroke.CAP_SQUARE;
		}

		int join = BasicStroke.JOIN_BEVEL;
		if (strokeJoin == PConstants.MITER) {
			join = BasicStroke.JOIN_MITER;
		} else if (strokeJoin == PConstants.ROUND) {
			join = BasicStroke.JOIN_ROUND;
		}

		g2.setStroke(new BasicStroke((float) strokeWeight, cap, join));
	}

	// ////////////////////////////////////////////////////////////

	// STROKE

	// noStroke() and stroke() inherited from PGraphics.

	@Override
	protected void strokeFromCalc() {
		super.strokeFromCalc();
		strokeColorObject = new Color(strokeColor, true);
		strokeGradient = false;
	}

	// ////////////////////////////////////////////////////////////

	// TINT

	// noTint() and tint() inherited from PGraphics.

	@Override
	protected void tintFromCalc() {
		super.tintFromCalc();
		// TODO actually implement tinted images
		tintColorObject = new Color(tintColor, true);
	}

	// ////////////////////////////////////////////////////////////

	// FILL

	// noFill() and fill() inherited from PGraphics.

	@Override
	protected void fillFromCalc() {
		super.fillFromCalc();
		fillColorObject = new Color(fillColor, true);
		fillGradient = false;
	}

	// ////////////////////////////////////////////////////////////

	// MATERIAL PROPERTIES

	// public void ambient(int rgb)
	// public void ambient(double gray)
	// public void ambient(double x, double y, double z)
	// protected void ambientFromCalc()
	// public void specular(int rgb)
	// public void specular(double gray)
	// public void specular(double x, double y, double z)
	// protected void specularFromCalc()
	// public void shininess(double shine)
	// public void emissive(int rgb)
	// public void emissive(double gray)
	// public void emissive(double x, double y, double z )
	// protected void emissiveFromCalc()

	// ////////////////////////////////////////////////////////////

	// LIGHTS

	// public void lights()
	// public void noLights()
	// public void ambientLight(double red, double green, double blue)
	// public void ambientLight(double red, double green, double blue,
	// double x, double y, double z)
	// public void directionalLight(double red, double green, double blue,
	// double nx, double ny, double nz)
	// public void pointLight(double red, double green, double blue,
	// double x, double y, double z)
	// public void spotLight(double red, double green, double blue,
	// double x, double y, double z,
	// double nx, double ny, double nz,
	// double angle, double concentration)
	// public void lightFalloff(double constant, double linear, double
	// quadratic)
	// public void lightSpecular(double x, double y, double z)
	// protected void lightPosition(int num, double x, double y, double z)
	// protected void lightDirection(int num, double x, double y, double z)

	// ////////////////////////////////////////////////////////////

	// BACKGROUND

	int[]	clearPixels;

	protected void clearPixels(final int color) {
		// Create a small array that can be used to set the pixels several
		// times.
		// Using a single-pixel line of length 'width' is a tradeoff between
		// speed (setting each pixel individually is too slow) and memory
		// (an array for width*height would waste lots of memory if it stayed
		// resident, and would terrify the gc if it were re-created on each trip
		// to background().
		final WritableRaster raster = ((BufferedImage) image).getRaster();
		// WritableRaster raster = image.getRaster();
		if ((clearPixels == null) || (clearPixels.length < width)) {
			clearPixels = new int[width];
		}
		Arrays.fill(clearPixels, backgroundColor);
		for (int i = 0; i < height; i++) {
			raster.setDataElements(0, i, width, 1, clearPixels);
		}
	}

	// background() methods inherited from PGraphics, along with the
	// PImage version of backgroundImpl(), since it just calls set().

	// public void backgroundImpl(PImage image)

	@Override
	public void backgroundImpl() {
		if (backgroundAlpha) {
			clearPixels(backgroundColor);

		} else {
			final Color bgColor = new Color(backgroundColor);
			// seems to fire an additional event that causes flickering,
			// like an extra background erase on OS X
			// if (canvas != null) {
			// canvas.setBackground(bgColor);
			// }
			// new Exception().printStackTrace(System.out);
			// in case people do transformations before background(),
			// need to handle this with a push/reset/pop
			final Composite oldComposite = g2.getComposite();
			g2.setComposite(defaultComposite);

			pushMatrix();
			resetMatrix();
			g2.setColor(bgColor); // , backgroundAlpha));
			g2.fillRect(0, 0, width, height);
			popMatrix();

			g2.setComposite(oldComposite);
		}
	}

	// ////////////////////////////////////////////////////////////

	// COLOR MODE

	// All colorMode() variations are inherited from PGraphics.

	// ////////////////////////////////////////////////////////////

	// COLOR CALC

	// colorCalc() and colorCalcARGB() inherited from PGraphics.

	// ////////////////////////////////////////////////////////////

	// COLOR DATATYPE STUFFING

	// final color() variations inherited.

	// ////////////////////////////////////////////////////////////

	// COLOR DATATYPE EXTRACTION

	// final methods alpha, red, green, blue,
	// hue, saturation, and brightness all inherited.

	// ////////////////////////////////////////////////////////////

	// COLOR DATATYPE INTERPOLATION

	// both lerpColor variants inherited.

	// ////////////////////////////////////////////////////////////

	// BEGIN/END RAW

	@Override
	public void beginRaw(final PGraphics recorderRaw) {
		PGraphics.showMethodWarning("beginRaw");
	}

	@Override
	public void endRaw() {
		PGraphics.showMethodWarning("endRaw");
	}

	// ////////////////////////////////////////////////////////////

	// WARNINGS and EXCEPTIONS

	// showWarning and showException inherited.

	// ////////////////////////////////////////////////////////////

	// RENDERER SUPPORT QUERIES

	// public boolean displayable() // true

	// public boolean is2D() // true

	// public boolean is3D() // false

	// ////////////////////////////////////////////////////////////

	// PIMAGE METHODS

	// getImage, setCache, getCache, removeCache, isModified, setModified

	@Override
	public void loadPixels() {
		if ((pixels == null) || (pixels.length != (width * height))) {
			pixels = new int[width * height];
		}
		// ((BufferedImage) image).getRGB(0, 0, width, height, pixels, 0,
		// width);
		final WritableRaster raster = ((BufferedImage) (primarySurface ? offscreen : image)).getRaster();
		// WritableRaster raster = image.getRaster();
		raster.getDataElements(0, 0, width, height, pixels);
	}

	/**
	 * Update the pixels[] buffer to the PGraphics image.
	 * <P>
	 * Unlike in PImage, where updatePixels() only requests that the update happens, in
	 * PGraphicsJava2D, this will happen immediately.
	 */
	@Override
	public void updatePixels() {
		// updatePixels(0, 0, width, height);
		final WritableRaster raster = ((BufferedImage) (primarySurface ? offscreen : image)).getRaster();
		// WritableRaster raster = image.getRaster();
		raster.setDataElements(0, 0, width, height, pixels);
	}

	/**
	 * Update the pixels[] buffer to the PGraphics image.
	 * <P>
	 * Unlike in PImage, where updatePixels() only requests that the update happens, in
	 * PGraphicsJava2D, this will happen immediately.
	 */
	@Override
	public void updatePixels(final int x, final int y, final int c, final int d) {
		// if ((x == 0) && (y == 0) && (c == width) && (d == height)) {
		if ((x != 0) || (y != 0) || (c != width) || (d != height)) {
			// Show a warning message, but continue anyway.
			PGraphics.showVariationWarning("updatePixels(x, y, w, h)");
		}
		this.updatePixels();
	}

	// ////////////////////////////////////////////////////////////

	// GET/SET

	static int	getset[]	= new int[1];

	@Override
	public int get(final int x, final int y) {
		if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
			return 0;
		}
		// return ((BufferedImage) image).getRGB(x, y);
		final WritableRaster raster = ((BufferedImage) (primarySurface ? offscreen : image)).getRaster();
		// WritableRaster raster = image.getRaster();
		raster.getDataElements(x, y, PGraphicsJava2D.getset);
		return PGraphicsJava2D.getset[0];
	}

	// public PImage get(int x, int y, int w, int h)

	@Override
	// public PImage getImpl(int x, int y, int w, int h) {
	protected void getImpl(final int sourceX, final int sourceY, final int sourceWidth, final int sourceHeight,
	        final PImage target, final int targetX, final int targetY) {
		// last parameter to getRGB() is the scan size of the *target* buffer
		// ((BufferedImage) image).getRGB(x, y, w, h, output.pixels, 0, w);
		final WritableRaster raster = ((BufferedImage) (primarySurface ? offscreen : image)).getRaster();

		if ((sourceWidth == target.width) && (sourceHeight == target.height)) {
			raster.getDataElements(sourceX, sourceY, sourceWidth, sourceHeight, target.pixels);

		} else {
			// TODO optimize, incredibly inefficient to reallocate this much
			// memory
			final int[] temp = new int[sourceWidth * sourceHeight];
			raster.getDataElements(sourceX, sourceY, sourceWidth, sourceHeight, temp);

			// Copy the temporary output pixels over to the outgoing image
			int sourceOffset = 0;
			int targetOffset = (targetY * target.width) + targetX;
			for (int y = 0; y < sourceHeight; y++) {
				System.arraycopy(temp, sourceOffset, target.pixels, targetOffset, sourceWidth);
				sourceOffset += sourceWidth;
				targetOffset += target.width;
			}
		}
	}

	@Override
	public PImage get() {
		return this.get(0, 0, width, height);
	}

	@Override
	public void set(final int x, final int y, final int argb) {
		if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
			return;
		}
		// ((BufferedImage) image).setRGB(x, y, argb);
		PGraphicsJava2D.getset[0] = argb;
		final WritableRaster raster = ((BufferedImage) (primarySurface ? offscreen : image)).getRaster();
		// WritableRaster raster = image.getRaster();
		raster.setDataElements(x, y, PGraphicsJava2D.getset);
	}

	// protected void setImpl(int dx, int dy, int sx, int sy, int sw, int sh,
	// PImage src) {
	@Override
	protected void setImpl(final PImage sourceImage, final int sourceX, final int sourceY, final int sourceWidth,
	        final int sourceHeight, final int targetX, final int targetY) {
		final WritableRaster raster = ((BufferedImage) (primarySurface ? offscreen : image)).getRaster();

		if ((sourceX == 0) && (sourceY == 0) && (sourceWidth == sourceImage.width)
		        && (sourceHeight == sourceImage.height)) {
			raster.setDataElements(targetX, targetY, sourceImage.width, sourceImage.height, sourceImage.pixels);
		} else {
			// TODO optimize, incredibly inefficient to reallocate this much
			// memory
			final PImage temp = sourceImage.get(sourceX, sourceY, sourceWidth, sourceHeight);
			raster.setDataElements(targetX, targetY, temp.width, temp.height, temp.pixels);
		}
	}

	// ////////////////////////////////////////////////////////////

	// MASK

	@Override
	public void mask(final int alpha[]) {
		PGraphics.showMethodWarning("mask");
	}

	@Override
	public void mask(final PImage alpha) {
		PGraphics.showMethodWarning("mask");
	}

	// ////////////////////////////////////////////////////////////

	// FILTER

	// Because the PImage versions call loadPixels() and
	// updatePixels(), no need to override anything here.

	// public void filter(int kind)

	// public void filter(int kind, double param)

	// ////////////////////////////////////////////////////////////

	// COPY

	@Override
	public void copy(final int sx, final int sy, final int sw, final int sh, int dx, int dy, final int dw, final int dh) {
		if ((sw != dw) || (sh != dh)) {
			// Image img = primarySurface ? offscreen : image;
			// g2.drawImage(img, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy +
			// sh, null);
			g2.drawImage(image, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);

		} else {
			dx = dx - sx; // java2d's "dx" is the delta, not dest
			dy = dy - sy;
			g2.copyArea(sx, sy, sw, sh, dx, dy);
		}
	}

	@Override
	public void copy(final PImage src, final int sx, final int sy, final int sw, final int sh, final int dx,
	        final int dy, final int dw, final int dh) {
		g2.drawImage((Image) src.getNative(), dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
	}

	// ////////////////////////////////////////////////////////////

	// BLEND

	// static public int blendColor(int c1, int c2, int mode)

	// public void blend(int sx, int sy, int sw, int sh,
	// int dx, int dy, int dw, int dh, int mode)

	// public void blend(PImage src,
	// int sx, int sy, int sw, int sh,
	// int dx, int dy, int dw, int dh, int mode)

	// ////////////////////////////////////////////////////////////

	// SAVE

	// public void save(String filename) {
	// loadPixels();
	// super.save(filename);
	// }
}
