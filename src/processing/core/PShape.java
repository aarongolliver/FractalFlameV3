/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 * Part of the Processing project - http://processing.org Copyright (c) 2006-10 Ben Fry and Casey
 * Reas This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received a copy of the GNU Lesser
 * General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package processing.core;

import java.util.HashMap;

/**
 * ( begin auto-generated from PShape.xml ) Datatype for storing shapes. Processing can currently
 * load and display SVG (Scalable Vector Graphics) shapes. Before a shape is used, it must be loaded
 * with the <b>loadShape()</b> function. The <b>shape()</b> function is used to draw the shape to
 * the display window. The <b>PShape</b> object contain a group of methods, linked below, that can
 * operate on the shape data. <br />
 * <br />
 * The <b>loadShape()</b> function supports SVG files created with Inkscape and Adobe Illustrator.
 * It is not a full SVG implementation, but offers some straightforward support for handling vector
 * data. ( end auto-generated ) <h3>Advanced</h3> In-progress class to handle shape data, currently
 * to be considered of alpha or beta quality. Major structural work may be performed on this class
 * after the release of Processing 1.0. Such changes may include:
 * <ul>
 * <li>addition of proper accessors to read shape vertex and coloring data (this is the second most
 * important part of having a PShape class after all).
 * <li>a means of creating PShape objects ala beginShape() and endShape().
 * <li>load(), update(), and cache methods ala PImage, so that shapes can have renderer-specific
 * optimizations, such as vertex arrays in OpenGL.
 * <li>splitting this class into multiple classes to handle different varieties of shape data
 * (primitives vs collections of vertices vs paths)
 * <li>change of package declaration, for instance moving the code into package processing.shape (if
 * the code grows too much).
 * </ul>
 * <p>
 * For the time being, this class and its shape() and loadShape() friends in PApplet exist as
 * placeholders for more exciting things to come. If you'd like to work with this class, make a
 * subclass (see how PShapeSVG works) and you can play with its internal methods all you like.
 * </p>
 * <p>
 * Library developers are encouraged to create PShape objects when loading shape data, so that they
 * can eventually hook into the bounty that will be the PShape interface, and the ease of
 * loadShape() and shape().
 * </p>
 * 
 * @webref shape
 * @usage Web &amp; Application
 * @see PApplet#loadShape(String)
 * @see PApplet#createShape()
 * @see PApplet#shapeMode(int)
 * @instanceName sh any variable of type PShape
 */
public class PShape implements PConstants {
	protected String	              name;
	protected HashMap<String, PShape>	nameTable;

	// /** Generic, only draws its child objects. */
	// static public final int GROUP = 0;
	// GROUP now inherited from PConstants
	/** A line, ellipse, arc, image, etc. */
	static public final int	          PRIMITIVE	= 1;
	/** A series of vertex, curveVertex, and bezierVertex calls. */
	static public final int	          PATH	    = 2;
	/** Collections of vertices created with beginShape(). */
	static public final int	          GEOMETRY	= 3;
	/** The shape type, one of GROUP, PRIMITIVE, PATH, or GEOMETRY. */
	protected int	                  family;

	/** ELLIPSE, LINE, QUAD; TRIANGLE_FAN, QUAD_STRIP; etc. */
	protected int	                  kind;

	protected PMatrix	              matrix;

	/** Texture or image data associated with this shape. */
	protected PImage	              image;

	// boundary box of this shape
	// protected double x;
	// protected double y;
	// protected double width;
	// protected double height;
	/**
	 * ( begin auto-generated from PShape_width.xml ) The width of the PShape document. ( end
	 * auto-generated )
	 * 
	 * @webref pshape:field
	 * @usage web_application
	 * @brief Shape document width
	 * @see PShape#height
	 */
	public double	                  width;
	/**
	 * ( begin auto-generated from PShape_height.xml ) The height of the PShape document. ( end
	 * auto-generated )
	 * 
	 * @webref pshape:field
	 * @usage web_application
	 * @brief Shape document height
	 * @see PShape#width
	 */
	public double	                  height;

	public double	                  depth;

	// set to false if the object is hidden in the layers palette
	protected boolean	              visible	= true;

	protected boolean	              stroke;
	protected int	                  strokeColor;
	protected double	              strokeWeight;	// default is 1
	protected int	                  strokeCap;
	protected int	                  strokeJoin;

	protected boolean	              fill;
	protected int	                  fillColor;

	protected boolean	              tint;
	protected int	                  tintColor;

	protected int	                  ambientColor;
	protected boolean	              setAmbient;
	protected int	                  specularColor;
	protected int	                  emissiveColor;
	protected double	              shininess;

	/** Temporary toggle for whether styles should be honored. */
	protected boolean	              style	    = true;

	/** For primitive shapes in particular, params like x/y/w/h or x1/y1/x2/y2. */
	protected double[]	              params;

	protected int	                  vertexCount;
	/**
	 * When drawing POLYGON shapes, the second param is an array of length VERTEX_FIELD_COUNT. When
	 * drawing PATH shapes, the second param has only two variables.
	 */
	protected double[][]	          vertices;

	protected PShape	              parent;
	protected int	                  childCount;
	protected PShape[]	              children;

	/** Array of VERTEX, BEZIER_VERTEX, and CURVE_VERTEX calls. */
	protected int	                  vertexCodeCount;
	protected int[]	                  vertexCodes;
	/** True if this is a closed path. */
	protected boolean	              close;

	// ........................................................

	// internal color for setting/calculating
	protected double	              calcR, calcG, calcB, calcA;
	protected int	                  calcRi, calcGi, calcBi, calcAi;
	protected int	                  calcColor;
	protected boolean	              calcAlpha;

	/** The current colorMode */
	public int	                      colorMode;	                  // = RGB;

	/** Max value for red (or hue) set by colorMode */
	public double	                  colorModeX;	                  // = 255;

	/** Max value for green (or saturation) set by colorMode */
	public double	                  colorModeY;	                  // = 255;

	/** Max value for blue (or value) set by colorMode */
	public double	                  colorModeZ;	                  // = 255;

	/** Max value for alpha set by colorMode */
	public double	                  colorModeA;	                  // = 255;

	/** True if colors are not in the range 0..1 */
	boolean	                          colorModeScale;	              // = true;

	/** True if colorMode(RGB, 255) */
	boolean	                          colorModeDefault;	          // = true;

	/** True if contains 3D data */
	protected boolean	              is3D	    = false;

	// should this be called vertices (consistent with PGraphics internals)
	// or does that hurt flexibility?

	// POINTS, LINES, xLINE_STRIP, xLINE_LOOP
	// TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN
	// QUADS, QUAD_STRIP
	// xPOLYGON
	// static final int PATH = 1; // POLYGON, LINE_LOOP, LINE_STRIP
	// static final int GROUP = 2;

	// how to handle rectmode/ellipsemode?
	// are they bitshifted into the constant?
	// CORNER, CORNERS, CENTER, (CENTER_RADIUS?)
	// static final int RECT = 3; // could just be QUAD, but would be
	// x1/y1/x2/y2
	// static final int ELLIPSE = 4;
	//
	// static final int VERTEX = 7;
	// static final int CURVE = 5;
	// static final int BEZIER = 6;

	// fill and stroke functions will need a pointer to the parent
	// PGraphics object.. may need some kind of createShape() fxn
	// or maybe the values are stored until draw() is called?

	// attaching images is very tricky.. it's a different type of data

	// material parameters will be thrown out,
	// except those currently supported (kinds of lights)

	// pivot point for transformations
	// public double px;
	// public double py;

	public PShape() {
		family = PConstants.GROUP;
	}

	/**
	 * @nowebref
	 */
	public PShape(final int family) {
		this.family = family;
	}

	public void setKind(final int kind) {
		this.kind = kind;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * ( begin auto-generated from PShape_isVisible.xml ) Returns a boolean value "true" if the
	 * image is set to be visible, "false" if not. This is modified with the <b>setVisible()</b>
	 * parameter. <br/>
	 * <br/>
	 * The visibility of a shape is usually controlled by whatever program created the SVG file. For
	 * instance, this parameter is controlled by showing or hiding the shape in the layers palette
	 * in Adobe Illustrator. ( end auto-generated )
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Returns a boolean value "true" if the image is set to be visible, "false" if not
	 * @see PShape#setVisible(boolean)
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * ( begin auto-generated from PShape_setVisible.xml ) Sets the shape to be visible or
	 * invisible. This is determined by the value of the <b>visible</b> parameter. <br/>
	 * <br/>
	 * The visibility of a shape is usually controlled by whatever program created the SVG file. For
	 * instance, this parameter is controlled by showing or hiding the shape in the layers palette
	 * in Adobe Illustrator. ( end auto-generated )
	 * 
	 * @webref pshape:mathod
	 * @usage web_application
	 * @brief Sets the shape to be visible or invisible
	 * @param visible
	 *            "false" makes the shape invisible and "true" makes it visible
	 * @see PShape#isVisible()
	 */
	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	/**
	 * ( begin auto-generated from PShape_disableStyle.xml ) Disables the shape's style data and
	 * uses Processing's current styles. Styles include attributes such as colors, stroke weight,
	 * and stroke joints. ( end auto-generated ) <h3>Advanced</h3> Overrides this shape's style
	 * information and uses PGraphics styles and colors. Identical to ignoreStyles(true). Also
	 * disables styles for all child shapes.
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Disables the shape's style data and uses Processing styles
	 * @see PShape#enableStyle()
	 */
	public void disableStyle() {
		style = false;

		for (int i = 0; i < childCount; i++) {
			children[i].disableStyle();
		}
	}

	/**
	 * ( begin auto-generated from PShape_enableStyle.xml ) Enables the shape's style data and
	 * ignores Processing's current styles. Styles include attributes such as colors, stroke weight,
	 * and stroke joints. ( end auto-generated )
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Enables the shape's style data and ignores the Processing styles
	 * @see PShape#disableStyle()
	 */
	public void enableStyle() {
		style = true;

		for (int i = 0; i < childCount; i++) {
			children[i].enableStyle();
		}
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	// protected void checkBounds() {
	// if (width == 0 || height == 0) {
	// // calculate bounds here (also take kids into account)
	// width = 1;
	// height = 1;
	// }
	// }

	/**
	 * Get the width of the drawing area (not necessarily the shape boundary).
	 */
	public double getWidth() {
		// checkBounds();
		return width;
	}

	/**
	 * Get the height of the drawing area (not necessarily the shape boundary).
	 */
	public double getHeight() {
		// checkBounds();
		return height;
	}

	/**
	 * Get the depth of the shape area (not necessarily the shape boundary). Only makes sense for 3D
	 * PShape subclasses, such as PShape3D.
	 */
	public double getDepth() {
		// checkBounds();
		return depth;
	}

	/*
	 * // TODO unapproved protected PVector getTop() { return getTop(null); } protected PVector
	 * getTop(PVector top) { if (top == null) { top = new PVector(); } return top; } protected
	 * PVector getBottom() { return getBottom(null); } protected PVector getBottom(PVector bottom) {
	 * if (bottom == null) { bottom = new PVector(); } return bottom; }
	 */

	/**
	 * Return true if this shape is 2D. Defaults to true.
	 */
	public boolean is2D() {
		return !is3D;
	}

	/**
	 * Return true if this shape is 3D. Defaults to false.
	 */
	public boolean is3D() {
		return is3D;
	}

	public void is3D(final boolean val) {
		is3D = val;
	}

	// /**
	// * Return true if this shape requires rendering through OpenGL. Defaults
	// to false.
	// */
	// // TODO unapproved
	// public boolean isGL() {
	// return false;
	// }

	// /////////////////////////////////////////////////////////

	//

	// Drawing methods

	public void texture(final PImage tex) {
	}

	public void noTexture() {
	}

	// TODO unapproved
	protected void solid(final boolean solid) {
	}

	/**
	 * @webref shape:vertex
	 * @brief Starts a new contour
	 * @see PShape#endContour()
	 */
	public void beginContour() {
	}

	/**
	 * @webref shape:vertex
	 * @brief Ends a contour
	 * @see PShape#beginContour()
	 */
	public void endContour() {
	}

	public void vertex(final double x, final double y) {
	}

	public void vertex(final double x, final double y, final double u, final double v) {
	}

	public void vertex(final double x, final double y, final double z) {
	}

	public void vertex(final double x, final double y, final double z, final double u, final double v) {
	}

	public void normal(final double nx, final double ny, final double nz) {
	}

	/**
	 * @webref pshape:method
	 * @brief Finishes the creation of a new PShape
	 * @see PApplet#createShape()
	 */
	public void end() {
	}

	public void end(final int mode) {
	}

	// ////////////////////////////////////////////////////////////

	// STROKE CAP/JOIN/WEIGHT

	public void strokeWeight(final double weight) {
	}

	public void strokeJoin(final int join) {
	}

	public void strokeCap(final int cap) {
	}

	// ////////////////////////////////////////////////////////////

	// FILL COLOR

	public void noFill() {
	}

	public void fill(final int rgb) {
	}

	public void fill(final int rgb, final double alpha) {
	}

	public void fill(final double gray) {
	}

	public void fill(final double gray, final double alpha) {
	}

	public void fill(final double x, final double y, final double z) {
	}

	public void fill(final double x, final double y, final double z, final double a) {
	}

	// ////////////////////////////////////////////////////////////

	// STROKE COLOR

	public void noStroke() {
	}

	public void stroke(final int rgb) {
	}

	public void stroke(final int rgb, final double alpha) {
	}

	public void stroke(final double gray) {
	}

	public void stroke(final double gray, final double alpha) {
	}

	public void stroke(final double x, final double y, final double z) {
	}

	public void stroke(final double x, final double y, final double z, final double alpha) {
	}

	// ////////////////////////////////////////////////////////////

	// TINT COLOR

	public void noTint() {
	}

	public void tint(final int rgb) {
	}

	public void tint(final int rgb, final double alpha) {
	}

	public void tint(final double gray) {
	}

	public void tint(final double gray, final double alpha) {
	}

	public void tint(final double x, final double y, final double z) {
	}

	public void tint(final double x, final double y, final double z, final double alpha) {
	}

	// ////////////////////////////////////////////////////////////

	// Ambient set/update

	public void ambient(final int rgb) {
	}

	public void ambient(final double gray) {
	}

	public void ambient(final double x, final double y, final double z) {
	}

	// ////////////////////////////////////////////////////////////

	// Specular set/update

	public void specular(final int rgb) {
	}

	public void specular(final double gray) {
	}

	public void specular(final double x, final double y, final double z) {
	}

	// ////////////////////////////////////////////////////////////

	// Emissive set/update

	public void emissive(final int rgb) {
	}

	public void emissive(final double gray) {
	}

	public void emissive(final double x, final double y, final double z) {
	}

	// ////////////////////////////////////////////////////////////

	// Shininess set/update

	public void shininess(final double shine) {
	}

	// /////////////////////////////////////////////////////////

	//

	// Bezier curves

	public void bezierDetail(final int detail) {
	}

	public void bezierVertex(final double x2, final double y2, final double x3, final double y3, final double x4,
	        final double y4) {
	}

	public void bezierVertex(final double x2, final double y2, final double z2, final double x3, final double y3,
	        final double z3, final double x4, final double y4, final double z4) {
	}

	public void quadraticVertex(final double cx, final double cy, final double x3, final double y3) {
	}

	public void quadraticVertex(final double cx, final double cy, final double cz, final double x3, final double y3,
	        final double z3) {
	}

	// /////////////////////////////////////////////////////////

	//

	// Catmull-Rom curves

	public void curveDetail(final int detail) {
	}

	public void curveTightness(final double tightness) {
	}

	public void curveVertex(final double x, final double y) {
	}

	public void curveVertex(final double x, final double y, final double z) {
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/*
	 * boolean strokeSaved; int strokeColorSaved; double strokeWeightSaved; int strokeCapSaved; int
	 * strokeJoinSaved; boolean fillSaved; int fillColorSaved; int rectModeSaved; int
	 * ellipseModeSaved; int shapeModeSaved;
	 */

	protected void pre(final PGraphics g) {
		if (matrix != null) {
			g.pushMatrix();
			g.applyMatrix(matrix);
		}

		/*
		 * strokeSaved = g.stroke; strokeColorSaved = g.strokeColor; strokeWeightSaved =
		 * g.strokeWeight; strokeCapSaved = g.strokeCap; strokeJoinSaved = g.strokeJoin; fillSaved =
		 * g.fill; fillColorSaved = g.fillColor; rectModeSaved = g.rectMode; ellipseModeSaved =
		 * g.ellipseMode; shapeModeSaved = g.shapeMode;
		 */
		if (style) {
			g.pushStyle();
			styles(g);
		}
	}

	protected void styles(final PGraphics g) {
		// should not be necessary because using only the int version of color
		// parent.colorMode(PConstants.RGB, 255);

		if (stroke) {
			g.stroke(strokeColor);
			g.strokeWeight(strokeWeight);
			g.strokeCap(strokeCap);
			g.strokeJoin(strokeJoin);
		} else {
			g.noStroke();
		}

		if (fill) {
			// System.out.println("filling " + PApplet.hex(fillColor));
			g.fill(fillColor);
		} else {
			g.noFill();
		}
	}

	protected void post(final PGraphics g) {
		// for (int i = 0; i < childCount; i++) {
		// children[i].draw(g);
		// }

		/*
		 * // TODO this is not sufficient, since not saving fillR et al. g.stroke = strokeSaved;
		 * g.strokeColor = strokeColorSaved; g.strokeWeight = strokeWeightSaved; g.strokeCap =
		 * strokeCapSaved; g.strokeJoin = strokeJoinSaved; g.fill = fillSaved; g.fillColor =
		 * fillColorSaved; g.ellipseMode = ellipseModeSaved;
		 */

		if (matrix != null) {
			g.popMatrix();
		}

		if (style) {
			g.popStyle();
		}
	}

	// //////////////////////////////////////////////////////////////////////
	//
	// Shape copy

	// TODO unapproved
	static protected PShape createShape(final PApplet parent, final PShape src) {
		PShape dest = null;
		if (src.family == PConstants.GROUP) {
			dest = parent.createShape(PConstants.GROUP);
			PShape.copyGroup(parent, src, dest);
		} else if (src.family == PShape.PRIMITIVE) {
			dest = parent.createShape(src.kind, src.params);
			PShape.copyPrimitive(src, dest);
		} else if (src.family == PShape.GEOMETRY) {
			dest = parent.createShape(src.kind);
			PShape.copyGeometry(src, dest);
		} else if (src.family == PShape.PATH) {
			dest = parent.createShape(PShape.PATH);
			PShape.copyPath(src, dest);
		}
		dest.setName(src.name);
		return dest;
	}

	// TODO unapproved
	static protected void copyGroup(final PApplet parent, final PShape src, final PShape dest) {
		PShape.copyMatrix(src, dest);
		PShape.copyStyles(src, dest);
		PShape.copyImage(src, dest);
		for (int i = 0; i < src.childCount; i++) {
			final PShape c = PShape.createShape(parent, src.children[i]);
			dest.addChild(c);
		}
	}

	// TODO unapproved
	static protected void copyPrimitive(final PShape src, final PShape dest) {
		PShape.copyMatrix(src, dest);
		PShape.copyStyles(src, dest);
		PShape.copyImage(src, dest);
	}

	// TODO unapproved
	static protected void copyGeometry(final PShape src, final PShape dest) {
		PShape.copyMatrix(src, dest);
		PShape.copyStyles(src, dest);
		PShape.copyImage(src, dest);

		if (src.style) {
			for (int i = 0; i < src.vertexCount; i++) {
				final double[] vert = src.vertices[i];

				dest.fill(vert[PGraphics.R] * 255, vert[PGraphics.G] * 255, vert[PGraphics.B] * 255,
				        vert[PGraphics.A] * 255);

				// Do we need to copy these as well?
				// dest.ambient(vert[PGraphics.AR] * 255, vert[PGraphics.AG] *
				// 255,
				// vert[PGraphics.AB] * 255);
				// dest.specular(vert[PGraphics.SPR] * 255, vert[PGraphics.SPG]
				// * 255,
				// vert[PGraphics.SPB] * 255);
				// dest.emissive(vert[PGraphics.ER] * 255, vert[PGraphics.EG] *
				// 255,
				// vert[PGraphics.EB] * 255);
				// dest.shininess(vert[PGraphics.SHINE]);

				if (0 < PApplet.dist(vert[PGraphics.NX], vert[PGraphics.NY], vert[PGraphics.NZ], 0, 0, 0)) {
					dest.normal(vert[PGraphics.NX], vert[PGraphics.NY], vert[PGraphics.NZ]);
				}
				dest.vertex(vert[PConstants.X], vert[PConstants.Y], vert[PConstants.Z], vert[PGraphics.U],
				        vert[PGraphics.V]);
			}
		} else {
			for (int i = 0; i < src.vertexCount; i++) {
				final double[] vert = src.vertices[i];
				if (vert[PConstants.Z] == 0) {
					dest.vertex(vert[PConstants.X], vert[PConstants.Y]);
				} else {
					dest.vertex(vert[PConstants.X], vert[PConstants.Y], vert[PConstants.Z]);
				}
			}
		}

		dest.end();
	}

	// TODO unapproved
	static protected void copyPath(final PShape src, final PShape dest) {
		PShape.copyMatrix(src, dest);
		PShape.copyStyles(src, dest);
		PShape.copyImage(src, dest);
		dest.close = src.close;
		dest.setPath(src.vertexCount, src.vertices, src.vertexCodeCount, src.vertexCodes);
	}

	// TODO unapproved
	static protected void copyMatrix(final PShape src, final PShape dest) {
		if (src.matrix != null) {
			dest.applyMatrix(src.matrix);
		}
	}

	// TODO unapproved
	static protected void copyStyles(final PShape src, final PShape dest) {
		if (src.stroke) {
			dest.stroke = true;
			dest.strokeColor = src.strokeColor;
			dest.strokeWeight = src.strokeWeight;
			dest.strokeCap = src.strokeCap;
			dest.strokeJoin = src.strokeJoin;
		} else {
			dest.stroke = false;
		}

		if (src.fill) {
			dest.fill = true;
			dest.fillColor = src.fillColor;
		} else {
			dest.fill = false;
		}
	}

	// TODO unapproved
	static protected void copyImage(final PShape src, final PShape dest) {
		if (src.image != null) {
			dest.texture(src.image);
		}
	}

	// //////////////////////////////////////////////////////////////////////

	/**
	 * Called by the following (the shape() command adds the g) PShape s = loadShape("blah.svg");
	 * shape(s);
	 */
	public void draw(final PGraphics g) {
		if (visible) {
			pre(g);
			drawImpl(g);
			post(g);
		}
	}

	/**
	 * Draws the SVG document.
	 */
	public void drawImpl(final PGraphics g) {
		// System.out.println("drawing " + family);
		if (family == PConstants.GROUP) {
			drawGroup(g);
		} else if (family == PShape.PRIMITIVE) {
			drawPrimitive(g);
		} else if (family == PShape.GEOMETRY) {
			drawGeometry(g);
		} else if (family == PShape.PATH) {
			drawPath(g);
		}
	}

	protected void drawGroup(final PGraphics g) {
		for (int i = 0; i < childCount; i++) {
			children[i].draw(g);
		}
	}

	protected void drawPrimitive(final PGraphics g) {
		if (kind == PConstants.POINT) {
			g.point(params[0], params[1]);

		} else if (kind == PConstants.LINE) {
			if (params.length == 4) { // 2D
				g.line(params[0], params[1], params[2], params[3]);
			} else { // 3D
				g.line(params[0], params[1], params[2], params[3], params[4], params[5]);
			}

		} else if (kind == PConstants.TRIANGLE) {
			g.triangle(params[0], params[1], params[2], params[3], params[4], params[5]);

		} else if (kind == PConstants.QUAD) {
			g.quad(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]);

		} else if (kind == PConstants.RECT) {
			if (image != null) {
				g.imageMode(PConstants.CORNER);
				g.image(image, params[0], params[1], params[2], params[3]);
			} else {
				g.rectMode(PConstants.CORNER);
				g.rect(params[0], params[1], params[2], params[3]);
			}

		} else if (kind == PConstants.ELLIPSE) {
			g.ellipseMode(PConstants.CORNER);
			g.ellipse(params[0], params[1], params[2], params[3]);

		} else if (kind == PConstants.ARC) {
			g.ellipseMode(PConstants.CORNER);
			g.arc(params[0], params[1], params[2], params[3], params[4], params[5]);

		} else if (kind == PConstants.BOX) {
			if (params.length == 1) {
				g.box(params[0]);
			} else {
				g.box(params[0], params[1], params[2]);
			}

		} else if (kind == PConstants.SPHERE) {
			g.sphere(params[0]);
		}
	}

	protected void drawGeometry(final PGraphics g) {
		// get cache object using g.
		g.beginShape(kind);
		if (style) {
			for (int i = 0; i < vertexCount; i++) {
				g.vertex(vertices[i]);
			}
		} else {
			for (int i = 0; i < vertexCount; i++) {
				final double[] vert = vertices[i];
				if (vert[PConstants.Z] == 0) {
					g.vertex(vert[PConstants.X], vert[PConstants.Y]);
				} else {
					g.vertex(vert[PConstants.X], vert[PConstants.Y], vert[PConstants.Z]);
				}
			}
		}
		g.endShape();
	}

	/*
	 * protected void drawPath(PGraphics g) { g.beginShape(); for (int j = 0; j < childCount; j++) {
	 * if (j > 0) g.breakShape(); int count = children[j].vertexCount; double[][] vert =
	 * children[j].vertices; int[] code = children[j].vertexCodes; for (int i = 0; i < count; i++) {
	 * if (style) { if (children[j].fill) { g.fill(vert[i][R], vert[i][G], vert[i][B]); } else {
	 * g.noFill(); } if (children[j].stroke) { g.stroke(vert[i][R], vert[i][G], vert[i][B]); } else
	 * { g.noStroke(); } } g.edge(vert[i][EDGE] == 1); if (code[i] == VERTEX) { g.vertex(vert[i]); }
	 * else if (code[i] == BEZIER_VERTEX) { double z0 = vert[i+0][Z]; double z1 = vert[i+1][Z];
	 * double z2 = vert[i+2][Z]; if (z0 == 0 && z1 == 0 && z2 == 0) { g.bezierVertex(vert[i+0][X],
	 * vert[i+0][Y], z0, vert[i+1][X], vert[i+1][Y], z1, vert[i+2][X], vert[i+2][Y], z2); } else {
	 * g.bezierVertex(vert[i+0][X], vert[i+0][Y], vert[i+1][X], vert[i+1][Y], vert[i+2][X],
	 * vert[i+2][Y]); } } else if (code[i] == CURVE_VERTEX) { double z = vert[i][Z]; if (z == 0) {
	 * g.curveVertex(vert[i][X], vert[i][Y]); } else { g.curveVertex(vert[i][X], vert[i][Y], z); } }
	 * } } g.endShape(); }
	 */

	protected void drawPath(final PGraphics g) {
		// Paths might be empty (go figure)
		// http://dev.processing.org/bugs/show_bug.cgi?id=982
		if (vertices == null) {
			return;
		}

		boolean insideContour = false;
		g.beginShape();

		if (vertexCodeCount == 0) { // each point is a simple vertex
			if (vertices[0].length == 2) { // drawing 2D vertices
				for (int i = 0; i < vertexCount; i++) {
					g.vertex(vertices[i][PConstants.X], vertices[i][PConstants.Y]);
				}
			} else { // drawing 3D vertices
				for (int i = 0; i < vertexCount; i++) {
					g.vertex(vertices[i][PConstants.X], vertices[i][PConstants.Y], vertices[i][PConstants.Z]);
				}
			}

		} else { // coded set of vertices
			int index = 0;

			if (vertices[0].length == 2) { // drawing a 2D path
				for (int j = 0; j < vertexCodeCount; j++) {
					switch (vertexCodes[j]) {

					case VERTEX:
						g.vertex(vertices[index][PConstants.X], vertices[index][PConstants.Y]);
						// cx = vertices[index][X];
						// cy = vertices[index][Y];
						index++;
						break;

					case QUAD_BEZIER_VERTEX:
						g.quadraticVertex(vertices[index + 0][PConstants.X], vertices[index + 0][PConstants.Y],
						        vertices[index + 1][PConstants.X], vertices[index + 1][PConstants.Y]);
						// double x1 = vertices[index+0][X];
						// double y1 = vertices[index+0][Y];
						// double x2 = vertices[index+1][X];
						// double y2 = vertices[index+1][Y];
						// g.bezierVertex(x1 + ((cx-x1)*2/3.0f), y1 +
						// ((cy-y1)*2/3.0f),
						// x2 + ((cx-x2)*2/3.0f), y2 + ((cy-y2)*2/3.0f),
						// x2, y2);
						// cx = vertices[index+1][X];
						// cy = vertices[index+1][Y];
						index += 2;
						break;

					case BEZIER_VERTEX:
						g.bezierVertex(vertices[index + 0][PConstants.X], vertices[index + 0][PConstants.Y],
						        vertices[index + 1][PConstants.X], vertices[index + 1][PConstants.Y],
						        vertices[index + 2][PConstants.X], vertices[index + 2][PConstants.Y]);
						// cx = vertices[index+2][X];
						// cy = vertices[index+2][Y];
						index += 3;
						break;

					case CURVE_VERTEX:
						g.curveVertex(vertices[index][PConstants.X], vertices[index][PConstants.Y]);
						index++;
						break;

					case BREAK:
						if (insideContour) {
							g.endContour();
						}
						g.beginContour();
						insideContour = true;
					}
				}
			} else { // drawing a 3D path
				for (int j = 0; j < vertexCodeCount; j++) {
					switch (vertexCodes[j]) {

					case VERTEX:
						g.vertex(vertices[index][PConstants.X], vertices[index][PConstants.Y],
						        vertices[index][PConstants.Z]);
						// cx = vertices[index][X];
						// cy = vertices[index][Y];
						// cz = vertices[index][Z];
						index++;
						break;

					case QUAD_BEZIER_VERTEX:
						g.quadraticVertex(vertices[index + 0][PConstants.X], vertices[index + 0][PConstants.Y],
						        vertices[index + 0][PConstants.Z], vertices[index + 1][PConstants.X],
						        vertices[index + 1][PConstants.Y], vertices[index + 0][PConstants.Z]);
						index += 2;
						break;

					case BEZIER_VERTEX:
						g.bezierVertex(vertices[index + 0][PConstants.X], vertices[index + 0][PConstants.Y],
						        vertices[index + 0][PConstants.Z], vertices[index + 1][PConstants.X],
						        vertices[index + 1][PConstants.Y], vertices[index + 1][PConstants.Z],
						        vertices[index + 2][PConstants.X], vertices[index + 2][PConstants.Y],
						        vertices[index + 2][PConstants.Z]);
						index += 3;
						break;

					case CURVE_VERTEX:
						g.curveVertex(vertices[index][PConstants.X], vertices[index][PConstants.Y],
						        vertices[index][PConstants.Z]);
						index++;
						break;

					case BREAK:
						if (insideContour) {
							g.endContour();
						}
						g.beginContour();
						insideContour = true;
					}
				}
			}
		}
		if (insideContour) {
			g.endContour();
		}
		g.endShape(close ? PConstants.CLOSE : PConstants.OPEN);
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public PShape getParent() {
		return parent;
	}

	public int getChildCount() {
		return childCount;
	}

	public PShape[] getChildren() {
		return children;
	}

	/**
	 * ( begin auto-generated from PShape_getChild.xml ) Extracts a child shape from a parent shape.
	 * Specify the name of the shape with the <b>target</b> parameter. The shape is returned as a
	 * <b>PShape</b> object, or <b>null</b> is returned if there is an error. ( end auto-generated )
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Returns a child element of a shape as a PShape object
	 * @param index
	 *            the layer position of the shape to get
	 * @see PShape#addChild(PShape)
	 */
	public PShape getChild(final int index) {
		return children[index];
	}

	/**
	 * @param target
	 *            the name of the shape to get
	 */
	public PShape getChild(final String target) {
		if ((name != null) && name.equals(target)) {
			return this;
		}
		if (nameTable != null) {
			final PShape found = nameTable.get(target);
			if (found != null) {
				return found;
			}
		}
		for (int i = 0; i < childCount; i++) {
			final PShape found = children[i].getChild(target);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Same as getChild(name), except that it first walks all the way up the hierarchy to the eldest
	 * grandparent, so that children can be found anywhere.
	 */
	public PShape findChild(final String target) {
		if (parent == null) {
			return this.getChild(target);

		} else {
			return parent.findChild(target);
		}
	}

	// can't be just 'add' because that suggests additive geometry
	/**
	 * @webref pshape:method
	 * @brief Adds a new child
	 * @param who
	 *            any variable of type PShape
	 * @see PShape#getChild(int)
	 */
	public void addChild(final PShape who) {
		if (children == null) {
			children = new PShape[1];
		}
		if (childCount == children.length) {
			children = (PShape[]) PApplet.expand(children);
		}
		children[childCount++] = who;
		who.parent = this;

		if (who.getName() != null) {
			addName(who.getName(), who);
		}
	}

	// adds child who exactly at position idx in the array of children.
	/**
	 * @param idx
	 *            the layer position in which to insert the new child
	 */
	public void addChild(final PShape who, final int idx) {
		if (idx < childCount) {
			if (childCount == children.length) {
				children = (PShape[]) PApplet.expand(children);
			}

			// Copy [idx, childCount - 1] to [idx + 1, childCount]
			for (int i = childCount - 1; i >= idx; i--) {
				children[i + 1] = children[i];
			}
			childCount++;

			children[idx] = who;

			who.parent = this;

			if (who.getName() != null) {
				addName(who.getName(), who);
			}
		}
	}

	/**
	 * Remove the child shape with index idx.
	 */
	public void removeChild(final int idx) {
		if (idx < childCount) {
			final PShape child = children[idx];

			// Copy [idx + 1, childCount - 1] to [idx, childCount - 2]
			for (int i = idx; i < (childCount - 1); i++) {
				children[i] = children[i + 1];
			}
			childCount--;

			if ((child.getName() != null) && (nameTable != null)) {
				nameTable.remove(child.getName());
			}
		}
	}

	/**
	 * Add a shape to the name lookup table.
	 */
	public void addName(final String nom, final PShape shape) {
		if (parent != null) {
			parent.addName(nom, shape);
		} else {
			if (nameTable == null) {
				nameTable = new HashMap<String, PShape>();
			}
			nameTable.put(nom, shape);
		}
	}

	/**
	 * Returns the index of child who.
	 */
	public int getChildIndex(final PShape who) {
		for (int i = 0; i < childCount; i++) {
			if (children[i] == who) {
				return i;
			}
		}
		return -1;
	}

	public PShape getTessellation() {
		return null;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/** The shape type, one of GROUP, PRIMITIVE, PATH, or GEOMETRY. */
	public int getFamily() {
		return family;
	}

	public int getKind() {
		return kind;
	}

	public double[] getParams() {
		return this.getParams(null);
	}

	public double[] getParams(double[] target) {
		if ((target == null) || (target.length != params.length)) {
			target = new double[params.length];
		}
		PApplet.arrayCopy(params, target);
		return target;
	}

	public double getParam(final int index) {
		return params[index];
	}

	protected void setParams(final double[] source) {
		if (params == null) {
			params = new double[source.length];
		}
		if (source.length != params.length) {
			PGraphics.showWarning("Wrong number of parameters");
			return;
		}
		PApplet.arrayCopy(source, params);
	}

	public void setPath(final int vcount, final double[][] verts) {
		this.setPath(vcount, verts, 0, null);
	}

	protected void setPath(final int vcount, final double[][] verts, final int ccount, final int[] codes) {
		if ((verts == null) || (verts.length < vcount)) {
			return;
		}
		if ((0 < ccount) && ((codes == null) || (codes.length < ccount))) {
			return;
		}

		final int ndim = verts[0].length;
		vertexCount = vcount;
		vertices = new double[vertexCount][ndim];
		for (int i = 0; i < vertexCount; i++) {
			PApplet.arrayCopy(verts[i], vertices[i]);
		}

		vertexCodeCount = ccount;
		if (0 < vertexCodeCount) {
			vertexCodes = new int[vertexCodeCount];
			PApplet.arrayCopy(codes, vertexCodes, vertexCodeCount);
		}
	}

	/**
	 * @webref pshape:method
	 * @brief Returns the total number of vertices as an int
	 * @see PShape#getVertex(int)
	 * @see PShape#setVertex(int, double, double)
	 */
	public int getVertexCount() {
		return vertexCount;
	}

	/**
	 * @webref pshape:method
	 * @brief Returns the vertex at the index position
	 * @param index
	 *            the location of the vertex
	 * @see PShape#setVertex(int, double, double)
	 * @see PShape#getVertexCount()
	 */
	public PVector getVertex(final int index) {
		return this.getVertex(index, null);
	}

	/**
	 * @param vec
	 *            PVector to assign the data to
	 */
	public PVector getVertex(final int index, PVector vec) {
		if (vec == null) {
			vec = new PVector();
		}
		vec.x = vertices[index][PConstants.X];
		vec.y = vertices[index][PConstants.Y];
		vec.z = vertices[index][PConstants.Z];
		return vec;
	}

	public double getVertexX(final int index) {
		return vertices[index][PConstants.X];
	}

	public double getVertexY(final int index) {
		return vertices[index][PConstants.Y];
	}

	public double getVertexZ(final int index) {
		return vertices[index][PConstants.Z];
	}

	/**
	 * @webref pshape:method
	 * @brief Sets the vertex at the index position
	 * @param index
	 *            the location of the vertex
	 * @param x
	 *            the x value for the vertex
	 * @param y
	 *            the y value for the vertex
	 * @see PShape#getVertex(int)
	 * @see PShape#getVertexCount()
	 */
	public void setVertex(final int index, final double x, final double y) {
		vertices[index][PConstants.X] = x;
		vertices[index][PConstants.Y] = y;
	}

	/**
	 * @param z
	 *            the z value for the vertex
	 */
	public void setVertex(final int index, final double x, final double y, final double z) {
		vertices[index][PConstants.X] = x;
		vertices[index][PConstants.Y] = y;
		vertices[index][PConstants.Z] = z;
	}

	/**
	 * @param vec
	 *            the PVector to define the x, y, z coordinates
	 */
	public void setVertex(final int index, final PVector vec) {
		vertices[index][PConstants.X] = vec.x;
		vertices[index][PConstants.Y] = vec.y;
		vertices[index][PConstants.Z] = vec.z;
	}

	public PVector getNormal(final int index) {
		return this.getNormal(index, null);
	}

	public PVector getNormal(final int index, PVector vec) {
		if (vec == null) {
			vec = new PVector();
		}
		vec.x = vertices[index][PGraphics.NX];
		vec.y = vertices[index][PGraphics.NY];
		vec.z = vertices[index][PGraphics.NZ];
		return vec;
	}

	public double getNormalX(final int index) {
		return vertices[index][PGraphics.NX];
	}

	public double getNormalY(final int index) {
		return vertices[index][PGraphics.NY];
	}

	public double getNormalZ(final int index) {
		return vertices[index][PGraphics.NZ];
	}

	public void setNormal(final int index, final double nx, final double ny, final double nz) {
		vertices[index][PGraphics.NX] = nx;
		vertices[index][PGraphics.NY] = ny;
		vertices[index][PGraphics.NZ] = nz;
	}

	public double getTextureU(final int index) {
		return vertices[index][PGraphics.U];
	}

	public double getTextureV(final int index) {
		return vertices[index][PGraphics.V];
	}

	public void setTextureUV(final int index, final double u, final double v) {
		vertices[index][PGraphics.U] = u;
		vertices[index][PGraphics.V] = v;
	}

	public int getFill(final int index) {
		final int a = (int) (vertices[index][PGraphics.A] * 255);
		final int r = (int) (vertices[index][PGraphics.R] * 255);
		final int g = (int) (vertices[index][PGraphics.G] * 255);
		final int b = (int) (vertices[index][PGraphics.B] * 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public void setFill(final int index, final int fill) {
		vertices[index][PGraphics.A] = ((fill >> 24) & 0xFF) / 255.0f;
		vertices[index][PGraphics.R] = ((fill >> 16) & 0xFF) / 255.0f;
		vertices[index][PGraphics.G] = ((fill >> 8) & 0xFF) / 255.0f;
		vertices[index][PGraphics.B] = ((fill >> 0) & 0xFF) / 255.0f;
	}

	public int getStroke(final int index) {
		final int a = (int) (vertices[index][PGraphics.SA] * 255);
		final int r = (int) (vertices[index][PGraphics.SR] * 255);
		final int g = (int) (vertices[index][PGraphics.SG] * 255);
		final int b = (int) (vertices[index][PGraphics.SB] * 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public void setStroke(final int index, final int stroke) {
		vertices[index][PGraphics.SA] = ((stroke >> 24) & 0xFF) / 255.0f;
		vertices[index][PGraphics.SR] = ((stroke >> 16) & 0xFF) / 255.0f;
		vertices[index][PGraphics.SG] = ((stroke >> 8) & 0xFF) / 255.0f;
		vertices[index][PGraphics.SB] = ((stroke >> 0) & 0xFF) / 255.0f;
	}

	protected double getStrokeWeight(final int index) {
		return vertices[index][PGraphics.SW];
	}

	protected void setStrokeWeight(final int index, final double weight) {
		vertices[index][PGraphics.SW] = weight;
	}

	protected int getAmbient(final int index) {
		final int r = (int) (vertices[index][PGraphics.AR] * 255);
		final int g = (int) (vertices[index][PGraphics.AG] * 255);
		final int b = (int) (vertices[index][PGraphics.AB] * 255);
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}

	protected void setAmbient(final int index, final int ambient) {
		vertices[index][PGraphics.AR] = ((ambient >> 16) & 0xFF) / 255.0f;
		vertices[index][PGraphics.AG] = ((ambient >> 8) & 0xFF) / 255.0f;
		vertices[index][PGraphics.AB] = ((ambient >> 0) & 0xFF) / 255.0f;
	}

	protected int getSpecular(final int index) {
		final int r = (int) (vertices[index][PGraphics.SPR] * 255);
		final int g = (int) (vertices[index][PGraphics.SPG] * 255);
		final int b = (int) (vertices[index][PGraphics.SPB] * 255);
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}

	protected void setSpecular(final int index, final int specular) {
		vertices[index][PGraphics.SPR] = ((specular >> 16) & 0xFF) / 255.0f;
		vertices[index][PGraphics.SPG] = ((specular >> 8) & 0xFF) / 255.0f;
		vertices[index][PGraphics.SPB] = ((specular >> 0) & 0xFF) / 255.0f;
	}

	protected int getEmissive(final int index) {
		final int r = (int) (vertices[index][PGraphics.ER] * 255);
		final int g = (int) (vertices[index][PGraphics.EG] * 255);
		final int b = (int) (vertices[index][PGraphics.EB] * 255);
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}

	protected void setEmissive(final int index, final int emissive) {
		vertices[index][PGraphics.ER] = ((emissive >> 16) & 0xFF) / 255.0f;
		vertices[index][PGraphics.EG] = ((emissive >> 8) & 0xFF) / 255.0f;
		vertices[index][PGraphics.EB] = ((emissive >> 0) & 0xFF) / 255.0f;
	}

	protected double getShininess(final int index) {
		return vertices[index][PGraphics.SHINE];
	}

	protected void setShininess(final int index, final double shine) {
		vertices[index][PGraphics.SHINE] = shine;
	}

	public int[] getVertexCodes() {
		if (vertexCodes == null) {
			return null;
		}
		if (vertexCodes.length != vertexCodeCount) {
			vertexCodes = PApplet.subset(vertexCodes, 0, vertexCodeCount);
		}
		return vertexCodes;
	}

	public int getVertexCodeCount() {
		return vertexCodeCount;
	}

	/**
	 * One of VERTEX, BEZIER_VERTEX, CURVE_VERTEX, or BREAK.
	 */
	public int getVertexCode(final int index) {
		return vertexCodes[index];
	}

	public boolean isClosed() {
		return close;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	// http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
	public boolean contains(final double x, final double y) {
		if (family == PShape.PATH) {
			boolean c = false;
			for (int i = 0, j = vertexCount - 1; i < vertexCount; j = i++) {
				if (((vertices[i][PConstants.Y] > y) != (vertices[j][PConstants.Y] > y))
				        && (x < ((((vertices[j][PConstants.X] - vertices[i][PConstants.X]) * (y - vertices[i][PConstants.Y])) / (vertices[j][1] - vertices[i][PConstants.Y])) + vertices[i][PConstants.X]))) {
					c = !c;
				}
			}
			return c;
		} else {
			throw new IllegalArgumentException("The contains() method is only implemented for paths.");
		}
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	// translate, rotate, scale, apply (no push/pop)
	// these each call matrix.translate, etc
	// if matrix is null when one is called,
	// it is created and set to identity

	/**
	 * ( begin auto-generated from PShape_translate.xml ) Specifies an amount to displace the shape.
	 * The <b>x</b> parameter specifies left/right translation, the <b>y</b> parameter specifies
	 * up/down translation, and the <b>z</b> parameter specifies translations toward/away from the
	 * screen. Subsequent calls to the method accumulates the effect. For example, calling
	 * <b>translate(50, 0)</b> and then <b>translate(20, 0)</b> is the same as <b>translate(70,
	 * 0)</b>. This transformation is applied directly to the shape, it's not refreshed each time
	 * <b>draw()</b> is run. <br />
	 * <br />
	 * Using this method with the <b>z</b> parameter requires using the P3D parameter in combination
	 * with size. ( end auto-generated )
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Displaces the shape
	 * @param tx
	 *            left/right translation
	 * @param ty
	 *            up/down translation
	 * @see PShape#rotate(double)
	 * @see PShape#scale(double)
	 * @see PShape#resetMatrix()
	 */
	public void translate(final double x, final double y) {
		checkMatrix(2);
		matrix.translate(x, y);
	}

	/**
	 * @param tz
	 *            forward/back translation
	 */
	public void translate(final double x, final double y, final double z) {
		checkMatrix(3);
		matrix.translate(x, y, z);
	}

	/**
	 * ( begin auto-generated from PShape_rotateX.xml ) Rotates a shape around the x-axis the amount
	 * specified by the <b>angle</b> parameter. Angles should be specified in radians (values from 0
	 * to TWO_PI) or converted to radians with the <b>radians()</b> method. <br />
	 * <br />
	 * Shapes are always rotated around the upper-left corner of their bounding box. Positive
	 * numbers rotate objects in a clockwise direction. Subsequent calls to the method accumulates
	 * the effect. For example, calling <b>rotateX(HALF_PI)</b> and then <b>rotateX(HALF_PI)</b> is
	 * the same as <b>rotateX(PI)</b>. This transformation is applied directly to the shape, it's
	 * not refreshed each time <b>draw()</b> is run. <br />
	 * <br />
	 * This method requires a 3D renderer. You need to use P3D as a third parameter for the
	 * <b>size()</b> function as shown in the example above. ( end auto-generated )
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Rotates the shape around the x-axis
	 * @param angle
	 *            angle of rotation specified in radians
	 * @see PShape#rotate(double)
	 * @see PShape#rotateY(double)
	 * @see PShape#rotateZ(double)
	 * @see PShape#scale(double)
	 * @see PShape#translate(double, double)
	 * @see PShape#resetMatrix()
	 */
	public void rotateX(final double angle) {
		this.rotate(angle, 1, 0, 0);
	}

	/**
	 * ( begin auto-generated from PShape_rotateY.xml ) Rotates a shape around the y-axis the amount
	 * specified by the <b>angle</b> parameter. Angles should be specified in radians (values from 0
	 * to TWO_PI) or converted to radians with the <b>radians()</b> method. <br />
	 * <br />
	 * Shapes are always rotated around the upper-left corner of their bounding box. Positive
	 * numbers rotate objects in a clockwise direction. Subsequent calls to the method accumulates
	 * the effect. For example, calling <b>rotateY(HALF_PI)</b> and then <b>rotateY(HALF_PI)</b> is
	 * the same as <b>rotateY(PI)</b>. This transformation is applied directly to the shape, it's
	 * not refreshed each time <b>draw()</b> is run. <br />
	 * <br />
	 * This method requires a 3D renderer. You need to use P3D as a third parameter for the
	 * <b>size()</b> function as shown in the example above. ( end auto-generated )
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Rotates the shape around the y-axis
	 * @param angle
	 *            angle of rotation specified in radians
	 * @see PShape#rotate(double)
	 * @see PShape#rotateX(double)
	 * @see PShape#rotateZ(double)
	 * @see PShape#scale(double)
	 * @see PShape#translate(double, double)
	 * @see PShape#resetMatrix()
	 */
	public void rotateY(final double angle) {
		this.rotate(angle, 0, 1, 0);
	}

	/**
	 * ( begin auto-generated from PShape_rotateZ.xml ) Rotates a shape around the z-axis the amount
	 * specified by the <b>angle</b> parameter. Angles should be specified in radians (values from 0
	 * to TWO_PI) or converted to radians with the <b>radians()</b> method. <br />
	 * <br />
	 * Shapes are always rotated around the upper-left corner of their bounding box. Positive
	 * numbers rotate objects in a clockwise direction. Subsequent calls to the method accumulates
	 * the effect. For example, calling <b>rotateZ(HALF_PI)</b> and then <b>rotateZ(HALF_PI)</b> is
	 * the same as <b>rotateZ(PI)</b>. This transformation is applied directly to the shape, it's
	 * not refreshed each time <b>draw()</b> is run. <br />
	 * <br />
	 * This method requires a 3D renderer. You need to use P3D as a third parameter for the
	 * <b>size()</b> function as shown in the example above. ( end auto-generated )
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Rotates the shape around the z-axis
	 * @param angle
	 *            angle of rotation specified in radians
	 * @see PShape#rotate(double)
	 * @see PShape#rotateX(double)
	 * @see PShape#rotateY(double)
	 * @see PShape#scale(double)
	 * @see PShape#translate(double, double)
	 * @see PShape#resetMatrix()
	 */
	public void rotateZ(final double angle) {
		this.rotate(angle, 0, 0, 1);
	}

	/**
	 * ( begin auto-generated from PShape_rotate.xml ) Rotates a shape the amount specified by the
	 * <b>angle</b> parameter. Angles should be specified in radians (values from 0 to TWO_PI) or
	 * converted to radians with the <b>radians()</b> method. <br />
	 * <br />
	 * Shapes are always rotated around the upper-left corner of their bounding box. Positive
	 * numbers rotate objects in a clockwise direction. Transformations apply to everything that
	 * happens after and subsequent calls to the method accumulates the effect. For example, calling
	 * <b>rotate(HALF_PI)</b> and then <b>rotate(HALF_PI)</b> is the same as <b>rotate(PI)</b>. This
	 * transformation is applied directly to the shape, it's not refreshed each time <b>draw()</b>
	 * is run. ( end auto-generated )
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Rotates the shape
	 * @param angle
	 *            angle of rotation specified in radians
	 * @see PShape#rotateX(double)
	 * @see PShape#rotateY(double)
	 * @see PShape#rotateZ(double)
	 * @see PShape#scale(double)
	 * @see PShape#translate(double, double)
	 * @see PShape#resetMatrix()
	 */
	public void rotate(final double angle) {
		checkMatrix(2); // at least 2...
		matrix.rotate(angle);
	}

	/**
	 * @nowebref
	 */
	public void rotate(final double angle, double v0, double v1, double v2) {
		checkMatrix(3);
		final double norm2 = (v0 * v0) + (v1 * v1) + (v2 * v2);
		if (Math.abs(norm2 - 1) > PConstants.EPSILON) {
			// The rotation vector is not normalized.
			final double norm = PApplet.sqrt(norm2);
			v0 /= norm;
			v1 /= norm;
			v2 /= norm;
		}
		matrix.rotate(angle, v0, v1, v2);
	}

	//

	/**
	 * ( begin auto-generated from PShape_scale.xml ) Increases or decreases the size of a shape by
	 * expanding and contracting vertices. Shapes always scale from the relative origin of their
	 * bounding box. Scale values are specified as decimal percentages. For example, the method call
	 * <b>scale(2.0)</b> increases the dimension of a shape by 200%. Subsequent calls to the method
	 * multiply the effect. For example, calling <b>scale(2.0)</b> and then <b>scale(1.5)</b> is the
	 * same as <b>scale(3.0)</b>. This transformation is applied directly to the shape, it's not
	 * refreshed each time <b>draw()</b> is run. <br />
	 * <br />
	 * Using this method with the <b>z</b> parameter requires using the P3D parameter in combination
	 * with size. ( end auto-generated )
	 * 
	 * @webref pshape:method
	 * @usage web_application
	 * @brief Increases and decreases the size of a shape
	 * @param s
	 *            percentate to scale the object
	 * @see PShape#rotate(double)
	 * @see PShape#translate(double, double)
	 * @see PShape#resetMatrix()
	 */
	public void scale(final double s) {
		checkMatrix(2); // at least 2...
		matrix.scale(s);
	}

	public void scale(final double x, final double y) {
		checkMatrix(2);
		matrix.scale(x, y);
	}

	/**
	 * @param x
	 *            percentage to scale the object in the x-axis
	 * @param y
	 *            percentage to scale the object in the y-axis
	 * @param z
	 *            percentage to scale the object in the z-axis
	 */
	public void scale(final double x, final double y, final double z) {
		checkMatrix(3);
		matrix.scale(x, y, z);
	}

	//

	/**
	 * ( begin auto-generated from PShape_resetMatrix.xml ) Replaces the current matrix of a shape
	 * with the identity matrix. The equivalent function in OpenGL is glLoadIdentity(). ( end
	 * auto-generated )
	 * 
	 * @webref pshape:method
	 * @brief Replaces the current matrix of a shape with the identity matrix
	 * @usage web_application
	 * @see PShape#rotate(double)
	 * @see PShape#scale(double)
	 * @see PShape#translate(double, double)
	 */
	public void resetMatrix() {
		checkMatrix(2);
		matrix.reset();
	}

	public void applyMatrix(final PMatrix source) {
		if (source instanceof PMatrix2D) {
			this.applyMatrix((PMatrix2D) source);
		} else if (source instanceof PMatrix3D) {
			this.applyMatrix((PMatrix3D) source);
		}
	}

	public void applyMatrix(final PMatrix2D source) {
		this.applyMatrix(source.m00, source.m01, 0, source.m02, source.m10, source.m11, 0, source.m12, 0, 0, 1, 0, 0,
		        0, 0, 1);
	}

	public void applyMatrix(final double n00, final double n01, final double n02, final double n10, final double n11,
	        final double n12) {
		checkMatrix(2);
		matrix.apply(n00, n01, n02, n10, n11, n12);
	}

	public void applyMatrix(final PMatrix3D source) {
		this.applyMatrix(source.m00, source.m01, source.m02, source.m03, source.m10, source.m11, source.m12,
		        source.m13, source.m20, source.m21, source.m22, source.m23, source.m30, source.m31, source.m32,
		        source.m33);
	}

	public void applyMatrix(final double n00, final double n01, final double n02, final double n03, final double n10,
	        final double n11, final double n12, final double n13, final double n20, final double n21, final double n22,
	        final double n23, final double n30, final double n31, final double n32, final double n33) {
		checkMatrix(3);
		matrix.apply(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
	}

	//

	/**
	 * Make sure that the shape's matrix is 1) not null, and 2) has a matrix that can handle
	 * <em>at least</em> the specified number of dimensions.
	 */
	protected void checkMatrix(final int dimensions) {
		if (matrix == null) {
			if (dimensions == 2) {
				matrix = new PMatrix2D();
			} else {
				matrix = new PMatrix3D();
			}
		} else if ((dimensions == 3) && (matrix instanceof PMatrix2D)) {
			// time for an upgrayedd for a double dose of my pimpin'
			matrix = new PMatrix3D(matrix);
		}
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Center the shape based on its bounding box. Can't assume that the bounding box is 0, 0,
	 * width, height. Common case will be opening a letter size document in Illustrator, and drawing
	 * something in the middle, then reading it in as an svg file. This will also need to flip the y
	 * axis (scale(1, -1)) in cases like Adobe Illustrator where the coordinates start at the
	 * bottom.
	 */
	// public void center() {
	// }

	/**
	 * Set the pivot point for all transformations.
	 */
	// public void pivot(double x, double y) {
	// px = x;
	// py = y;
	// }

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public void colorMode(final int mode) {
		this.colorMode(mode, colorModeX, colorModeY, colorModeZ, colorModeA);
	}

	/**
	 * @param max
	 *            range for all color elements
	 */
	public void colorMode(final int mode, final double max) {
		this.colorMode(mode, max, max, max, max);
	}

	/**
	 * @param maxX
	 *            range for the red or hue depending on the current color mode
	 * @param maxY
	 *            range for the green or saturation depending on the current color mode
	 * @param maxZ
	 *            range for the blue or brightness depending on the current color mode
	 */
	public void colorMode(final int mode, final double maxX, final double maxY, final double maxZ) {
		this.colorMode(mode, maxX, maxY, maxZ, colorModeA);
	}

	/**
	 * @param maxA
	 *            range for the alpha
	 */
	public void colorMode(final int mode, final double maxX, final double maxY, final double maxZ, final double maxA) {
		colorMode = mode;

		colorModeX = maxX; // still needs to be set for hsb
		colorModeY = maxY;
		colorModeZ = maxZ;
		colorModeA = maxA;

		// if color max values are all 1, then no need to scale
		colorModeScale = ((maxA != 1) || (maxX != maxY) || (maxY != maxZ) || (maxZ != maxA));

		// if color is rgb/0..255 this will make it easier for the
		// red() green() etc functions
		colorModeDefault = (colorMode == PConstants.RGB) && (colorModeA == 255) && (colorModeX == 255)
		        && (colorModeY == 255) && (colorModeZ == 255);
	}

	protected void colorCalc(final int rgb) {
		if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
			this.colorCalc((double) rgb);

		} else {
			colorCalcARGB(rgb, colorModeA);
		}
	}

	protected void colorCalc(final int rgb, final double alpha) {
		if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) { // see
			                                                    // above
			this.colorCalc((double) rgb, alpha);

		} else {
			colorCalcARGB(rgb, alpha);
		}
	}

	protected void colorCalc(final double gray) {
		this.colorCalc(gray, colorModeA);
	}

	protected void colorCalc(double gray, double alpha) {
		if (gray > colorModeX) {
			gray = colorModeX;
		}
		if (alpha > colorModeA) {
			alpha = colorModeA;
		}

		if (gray < 0) {
			gray = 0;
		}
		if (alpha < 0) {
			alpha = 0;
		}

		calcR = colorModeScale ? (gray / colorModeX) : gray;
		calcG = calcR;
		calcB = calcR;
		calcA = colorModeScale ? (alpha / colorModeA) : alpha;

		calcRi = (int) (calcR * 255);
		calcGi = (int) (calcG * 255);
		calcBi = (int) (calcB * 255);
		calcAi = (int) (calcA * 255);
		calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
		calcAlpha = (calcAi != 255);
	}

	protected void colorCalc(final double x, final double y, final double z) {
		this.colorCalc(x, y, z, colorModeA);
	}

	protected void colorCalc(double x, double y, double z, double a) {
		if (x > colorModeX) {
			x = colorModeX;
		}
		if (y > colorModeY) {
			y = colorModeY;
		}
		if (z > colorModeZ) {
			z = colorModeZ;
		}
		if (a > colorModeA) {
			a = colorModeA;
		}

		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		if (z < 0) {
			z = 0;
		}
		if (a < 0) {
			a = 0;
		}

		switch (colorMode) {
		case RGB:
			if (colorModeScale) {
				calcR = x / colorModeX;
				calcG = y / colorModeY;
				calcB = z / colorModeZ;
				calcA = a / colorModeA;
			} else {
				calcR = x;
				calcG = y;
				calcB = z;
				calcA = a;
			}
			break;

		case HSB:
			x /= colorModeX; // h
			y /= colorModeY; // s
			z /= colorModeZ; // b

			calcA = colorModeScale ? (a / colorModeA) : a;

			if (y == 0) { // saturation == 0
				calcR = calcG = calcB = z;

			} else {
				final double which = (x - (int) x) * 6.0f;
				final double f = which - (int) which;
				final double p = z * (1.0f - y);
				final double q = z * (1.0f - (y * f));
				final double t = z * (1.0f - (y * (1.0f - f)));

				switch ((int) which) {
				case 0:
					calcR = z;
					calcG = t;
					calcB = p;
					break;
				case 1:
					calcR = q;
					calcG = z;
					calcB = p;
					break;
				case 2:
					calcR = p;
					calcG = z;
					calcB = t;
					break;
				case 3:
					calcR = p;
					calcG = q;
					calcB = z;
					break;
				case 4:
					calcR = t;
					calcG = p;
					calcB = z;
					break;
				case 5:
					calcR = z;
					calcG = p;
					calcB = q;
					break;
				}
			}
			break;
		}
		calcRi = (int) (255 * calcR);
		calcGi = (int) (255 * calcG);
		calcBi = (int) (255 * calcB);
		calcAi = (int) (255 * calcA);
		calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
		calcAlpha = (calcAi != 255);
	}

	protected void colorCalcARGB(final int argb, final double alpha) {
		if (alpha == colorModeA) {
			calcAi = (argb >> 24) & 0xff;
			calcColor = argb;
		} else {
			calcAi = (int) (((argb >> 24) & 0xff) * (alpha / colorModeA));
			calcColor = (calcAi << 24) | (argb & 0xFFFFFF);
		}
		calcRi = (argb >> 16) & 0xff;
		calcGi = (argb >> 8) & 0xff;
		calcBi = argb & 0xff;
		calcA = calcAi / 255.0f;
		calcR = calcRi / 255.0f;
		calcG = calcGi / 255.0f;
		calcB = calcBi / 255.0f;
		calcAlpha = (calcAi != 255);
	}

}