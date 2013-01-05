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

public interface PMatrix {

	public void reset();

	/**
	 * Returns a copy of this PMatrix.
	 */
	public PMatrix get();

	/**
	 * Copies the matrix contents into a double array. If target is null (or not the correct size),
	 * a new array will be created.
	 */
	public double[] get(double[] target);

	public void set(PMatrix src);

	public void set(double[] source);

	public void set(double m00, double m01, double m02, double m10, double m11, double m12);

	public void set(double m00, double m01, double m02, double m03, double m10, double m11, double m12, double m13,
	        double m20, double m21, double m22, double m23, double m30, double m31, double m32, double m33);

	public void translate(double tx, double ty);

	public void translate(double tx, double ty, double tz);

	public void rotate(double angle);

	public void rotateX(double angle);

	public void rotateY(double angle);

	public void rotateZ(double angle);

	public void rotate(double angle, double v0, double v1, double v2);

	public void scale(double s);

	public void scale(double sx, double sy);

	public void scale(double x, double y, double z);

	public void shearX(double angle);

	public void shearY(double angle);

	/**
	 * Multiply this matrix by another.
	 */
	public void apply(PMatrix source);

	public void apply(PMatrix2D source);

	public void apply(PMatrix3D source);

	public void apply(double n00, double n01, double n02, double n10, double n11, double n12);

	public void apply(double n00, double n01, double n02, double n03, double n10, double n11, double n12, double n13,
	        double n20, double n21, double n22, double n23, double n30, double n31, double n32, double n33);

	/**
	 * Apply another matrix to the left of this one.
	 */
	public void preApply(PMatrix2D left);

	public void preApply(PMatrix3D left);

	public void preApply(double n00, double n01, double n02, double n10, double n11, double n12);

	public void preApply(double n00, double n01, double n02, double n03, double n10, double n11, double n12,
	        double n13, double n20, double n21, double n22, double n23, double n30, double n31, double n32, double n33);

	/**
	 * Multiply a PVector by this matrix.
	 */
	public PVector mult(PVector source, PVector target);

	/**
	 * Multiply a multi-element vector against this matrix.
	 */
	public double[] mult(double[] source, double[] target);

	// public double multX(double x, double y);
	// public double multY(double x, double y);

	// public double multX(double x, double y, double z);
	// public double multY(double x, double y, double z);
	// public double multZ(double x, double y, double z);

	/**
	 * Transpose this matrix.
	 */
	public void transpose();

	/**
	 * Invert this matrix.
	 * 
	 * @return true if successful
	 */
	public boolean invert();

	/**
	 * @return the determinant of the matrix
	 */
	public double determinant();
}