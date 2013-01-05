/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 * Part of the Processing project - http://processing.org Copyright (c) 2005-08 Ben Fry and Casey
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

/**
 * 3x2 affine matrix implementation.
 */
public class PMatrix2D implements PMatrix {

	public double	m00, m01, m02;
	public double	m10, m11, m12;

	public PMatrix2D() {
		reset();
	}

	public PMatrix2D(final double m00, final double m01, final double m02, final double m10, final double m11,
	        final double m12) {
		set(m00, m01, m02, m10, m11, m12);
	}

	public PMatrix2D(final PMatrix matrix) {
		set(matrix);
	}

	public void reset() {
		set(1, 0, 0, 0, 1, 0);
	}

	/**
	 * Returns a copy of this PMatrix.
	 */
	public PMatrix2D get() {
		final PMatrix2D outgoing = new PMatrix2D();
		outgoing.set(this);
		return outgoing;
	}

	/**
	 * Copies the matrix contents into a 6 entry double array. If target is null (or not the correct
	 * size), a new array will be created.
	 */
	public double[] get(double[] target) {
		if ((target == null) || (target.length != 6)) {
			target = new double[6];
		}
		target[0] = m00;
		target[1] = m01;
		target[2] = m02;

		target[3] = m10;
		target[4] = m11;
		target[5] = m12;

		return target;
	}

	public void set(final PMatrix matrix) {
		if (matrix instanceof PMatrix2D) {
			final PMatrix2D src = (PMatrix2D) matrix;
			set(src.m00, src.m01, src.m02, src.m10, src.m11, src.m12);
		} else {
			throw new IllegalArgumentException("PMatrix2D.set() only accepts PMatrix2D objects.");
		}
	}

	public void set(final PMatrix3D src) {
	}

	public void set(final double[] source) {
		m00 = source[0];
		m01 = source[1];
		m02 = source[2];

		m10 = source[3];
		m11 = source[4];
		m12 = source[5];
	}

	public void set(final double m00, final double m01, final double m02, final double m10, final double m11,
	        final double m12) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
	}

	public void set(final double m00, final double m01, final double m02, final double m03, final double m10,
	        final double m11, final double m12, final double m13, final double m20, final double m21, final double m22,
	        final double m23, final double m30, final double m31, final double m32, final double m33) {

	}

	public void translate(final double tx, final double ty) {
		m02 = (tx * m00) + (ty * m01) + m02;
		m12 = (tx * m10) + (ty * m11) + m12;
	}

	public void translate(final double x, final double y, final double z) {
		throw new IllegalArgumentException("Cannot use translate(x, y, z) on a PMatrix2D.");
	}

	// Implementation roughly based on AffineTransform.
	public void rotate(final double angle) {
		final double s = sin(angle);
		final double c = cos(angle);

		double temp1 = m00;
		double temp2 = m01;
		m00 = (c * temp1) + (s * temp2);
		m01 = (-s * temp1) + (c * temp2);
		temp1 = m10;
		temp2 = m11;
		m10 = (c * temp1) + (s * temp2);
		m11 = (-s * temp1) + (c * temp2);
	}

	public void rotateX(final double angle) {
		throw new IllegalArgumentException("Cannot use rotateX() on a PMatrix2D.");
	}

	public void rotateY(final double angle) {
		throw new IllegalArgumentException("Cannot use rotateY() on a PMatrix2D.");
	}

	public void rotateZ(final double angle) {
		rotate(angle);
	}

	public void rotate(final double angle, final double v0, final double v1, final double v2) {
		throw new IllegalArgumentException("Cannot use this version of rotate() on a PMatrix2D.");
	}

	public void scale(final double s) {
		scale(s, s);
	}

	public void scale(final double sx, final double sy) {
		m00 *= sx;
		m01 *= sy;
		m10 *= sx;
		m11 *= sy;
	}

	public void scale(final double x, final double y, final double z) {
		throw new IllegalArgumentException("Cannot use this version of scale() on a PMatrix2D.");
	}

	public void shearX(final double angle) {
		apply(1, 0, 1, tan(angle), 0, 0);
	}

	public void shearY(final double angle) {
		apply(1, 0, 1, 0, tan(angle), 0);
	}

	public void apply(final PMatrix source) {
		if (source instanceof PMatrix2D) {
			apply((PMatrix2D) source);
		} else if (source instanceof PMatrix3D) {
			apply((PMatrix3D) source);
		}
	}

	public void apply(final PMatrix2D source) {
		apply(source.m00, source.m01, source.m02, source.m10, source.m11, source.m12);
	}

	public void apply(final PMatrix3D source) {
		throw new IllegalArgumentException("Cannot use apply(PMatrix3D) on a PMatrix2D.");
	}

	public void apply(final double n00, final double n01, final double n02, final double n10, final double n11,
	        final double n12) {
		double t0 = m00;
		double t1 = m01;
		m00 = (n00 * t0) + (n10 * t1);
		m01 = (n01 * t0) + (n11 * t1);
		m02 += (n02 * t0) + (n12 * t1);

		t0 = m10;
		t1 = m11;
		m10 = (n00 * t0) + (n10 * t1);
		m11 = (n01 * t0) + (n11 * t1);
		m12 += (n02 * t0) + (n12 * t1);
	}

	public void apply(final double n00, final double n01, final double n02, final double n03, final double n10,
	        final double n11, final double n12, final double n13, final double n20, final double n21, final double n22,
	        final double n23, final double n30, final double n31, final double n32, final double n33) {
		throw new IllegalArgumentException("Cannot use this version of apply() on a PMatrix2D.");
	}

	/**
	 * Apply another matrix to the left of this one.
	 */
	public void preApply(final PMatrix2D left) {
		preApply(left.m00, left.m01, left.m02, left.m10, left.m11, left.m12);
	}

	public void preApply(final PMatrix3D left) {
		throw new IllegalArgumentException("Cannot use preApply(PMatrix3D) on a PMatrix2D.");
	}

	public void preApply(final double n00, final double n01, double n02, final double n10, final double n11, double n12) {
		double t0 = m02;
		double t1 = m12;
		n02 += (t0 * n00) + (t1 * n01);
		n12 += (t0 * n10) + (t1 * n11);

		m02 = n02;
		m12 = n12;

		t0 = m00;
		t1 = m10;
		m00 = (t0 * n00) + (t1 * n01);
		m10 = (t0 * n10) + (t1 * n11);

		t0 = m01;
		t1 = m11;
		m01 = (t0 * n00) + (t1 * n01);
		m11 = (t0 * n10) + (t1 * n11);
	}

	public void preApply(final double n00, final double n01, final double n02, final double n03, final double n10,
	        final double n11, final double n12, final double n13, final double n20, final double n21, final double n22,
	        final double n23, final double n30, final double n31, final double n32, final double n33) {
		throw new IllegalArgumentException("Cannot use this version of preApply() on a PMatrix2D.");
	}

	// ////////////////////////////////////////////////////////////

	/**
	 * Multiply the x and y coordinates of a PVector against this matrix.
	 */
	public PVector mult(final PVector source, PVector target) {
		if (target == null) {
			target = new PVector();
		}
		target.x = (m00 * source.x) + (m01 * source.y) + m02;
		target.y = (m10 * source.x) + (m11 * source.y) + m12;
		return target;
	}

	/**
	 * Multiply a two element vector against this matrix. If out is null or not length four, a new
	 * double array will be returned. The values for vec and out can be the same (though that's less
	 * efficient).
	 */
	public double[] mult(final double vec[], double out[]) {
		if ((out == null) || (out.length != 2)) {
			out = new double[2];
		}

		if (vec == out) {
			final double tx = (m00 * vec[0]) + (m01 * vec[1]) + m02;
			final double ty = (m10 * vec[0]) + (m11 * vec[1]) + m12;

			out[0] = tx;
			out[1] = ty;

		} else {
			out[0] = (m00 * vec[0]) + (m01 * vec[1]) + m02;
			out[1] = (m10 * vec[0]) + (m11 * vec[1]) + m12;
		}

		return out;
	}

	public double multX(final double x, final double y) {
		return (m00 * x) + (m01 * y) + m02;
	}

	public double multY(final double x, final double y) {
		return (m10 * x) + (m11 * y) + m12;
	}

	/**
	 * Transpose this matrix.
	 */
	public void transpose() {
	}

	/**
	 * Invert this matrix. Implementation stolen from OpenJDK.
	 * 
	 * @return true if successful
	 */
	public boolean invert() {
		final double determinant = determinant();
		if (Math.abs(determinant) <= Float.MIN_VALUE) { return false; }

		final double t00 = m00;
		final double t01 = m01;
		final double t02 = m02;
		final double t10 = m10;
		final double t11 = m11;
		final double t12 = m12;

		m00 = t11 / determinant;
		m10 = -t10 / determinant;
		m01 = -t01 / determinant;
		m11 = t00 / determinant;
		m02 = ((t01 * t12) - (t11 * t02)) / determinant;
		m12 = ((t10 * t02) - (t00 * t12)) / determinant;

		return true;
	}

	/**
	 * @return the determinant of the matrix
	 */
	public double determinant() {
		return (m00 * m11) - (m01 * m10);
	}

	// ////////////////////////////////////////////////////////////

	public void print() {
		int big = (int) abs(max(PApplet.max(abs(m00), abs(m01), abs(m02)), PApplet.max(abs(m10), abs(m11), abs(m12))));

		int digits = 1;
		if (Float.isNaN(big) || Float.isInfinite(big)) { // avoid infinite loop
			digits = 5;
		} else {
			while ((big /= 10) != 0) {
				digits++; // cheap log()
			}
		}

		System.out.println(PApplet.nfs(m00, digits, 4) + " " + PApplet.nfs(m01, digits, 4) + " "
		        + PApplet.nfs(m02, digits, 4));

		System.out.println(PApplet.nfs(m10, digits, 4) + " " + PApplet.nfs(m11, digits, 4) + " "
		        + PApplet.nfs(m12, digits, 4));

		System.out.println();
	}

	// ////////////////////////////////////////////////////////////

	// TODO these need to be added as regular API, but the naming and
	// implementation needs to be improved first. (e.g. actually keeping track
	// of whether the matrix is in fact identity internally.)

	protected boolean isIdentity() {
		return ((m00 == 1) && (m01 == 0) && (m02 == 0) && (m10 == 0) && (m11 == 1) && (m12 == 0));
	}

	// TODO make this more efficient, or move into PMatrix2D
	protected boolean isWarped() {
		return ((m00 != 1) || ((m01 != 0) && (m10 != 0)) || (m11 != 1));
	}

	// ////////////////////////////////////////////////////////////

	static private final double max(final double a, final double b) {
		return (a > b) ? a : b;
	}

	static private final double abs(final double a) {
		return (a < 0) ? -a : a;
	}

	static private final double sin(final double angle) {
		return Math.sin(angle);
	}

	static private final double cos(final double angle) {
		return Math.cos(angle);
	}

	static private final double tan(final double angle) {
		return Math.tan(angle);
	}
}
