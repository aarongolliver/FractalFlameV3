package main;

import processing.core.PApplet;
import processing.core.PGraphics;

public final class Utils {
	static final String	ERROR_MIN_MAX	= "Cannot use min() or max() on an empty array.";

	public final static int[] range(final int n) {
		final int[] r = new int[n];
		for (int i = 0; i < n; i++) {
			r[i] = i;
		}
		return r;
	}

	/**
	 * ( begin auto-generated from max.xml ) Determines the largest value in a sequence of numbers.
	 * ( end auto-generated )
	 * 
	 * @webref math:calculation
	 * @param a
	 *            first number to compare
	 * @param b
	 *            second number to compare
	 * @see PApplet#min(double, double, double)
	 */
	static public final int max(final int a, final int b) {
		return (a > b) ? a : b;
	}

	static public final long max(final long a, final long b) {
		return (a > b) ? a : b;
	}

	static public final double max(final double a, final double b) {
		return (a > b) ? a : b;
	}

	/*
	 * static public final double max(double a, double b) { return (a > b) ? a : b; }
	 */

	/**
	 * @param c
	 *            third number to compare
	 */
	static public final int max(final int a, final int b, final int c) {
		return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
	}

	static public final long max(final long a, final long b, final long c) {
		return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
	}

	static public final double max(final double a, final double b, final double c) {
		return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
	}

	/**
	 * @param list
	 *            array of numbers to compare
	 */
	static public final int max(final int[] list) {
		if (list.length == 0) { throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX); }
		int max = list[0];
		for (int i = 1; i < list.length; i++) {
			if (list[i] > max) {
				max = list[i];
			}
		}
		return max;
	}

	static public final long max(final long[] list) {
		if (list.length == 0) { throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX); }
		long max = list[0];
		for (int i = 1; i < list.length; i++) {
			if (list[i] > max) {
				max = list[i];
			}
		}
		return max;
	}

	static public final double max(final double[] list) {
		if (list.length == 0) { throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX); }
		double max = list[0];
		for (int i = 1; i < list.length; i++) {
			if (list[i] > max) {
				max = list[i];
			}
		}
		return max;
	}

	/**
	 * Find the maximum value in an array. Throws an ArrayIndexOutOfBoundsException if the array is
	 * length 0.
	 * 
	 * @param list
	 *            the source array
	 * @return The maximum value
	 */
	/*
	 * static public final double max(double[] list) { if (list.length == 0) { throw new
	 * ArrayIndexOutOfBoundsException(ERROR_MIN_MAX); } double max = list[0]; for (int i = 1; i <
	 * list.length; i++) { if (list[i] > max) max = list[i]; } return max; }
	 */

	static public final int min(final int a, final int b) {
		return (a < b) ? a : b;
	}

	static public final long min(final long a, final long b) {
		return (a < b) ? a : b;
	}

	static public final double min(final double a, final double b) {
		return (a < b) ? a : b;
	}

	/*
	 * static public final double min(double a, double b) { return (a < b) ? a : b; }
	 */

	static public final int min(final int a, final int b, final int c) {
		return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
	}

	static public final long min(final long a, final long b, final long c) {
		return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
	}

	/**
	 * ( begin auto-generated from min.xml ) Determines the smallest value in a sequence of numbers.
	 * ( end auto-generated )
	 * 
	 * @webref math:calculation
	 * @param a
	 *            first number
	 * @param b
	 *            second number
	 * @param c
	 *            third number
	 * @see PApplet#max(double, double, double)
	 */

	static public final double min(final double a, final double b, final double c) {
		return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
	}

	/*
	 * static public final double min(double a, double b, double c) { return (a < b) ? ((a < c) ? a
	 * : c) : ((b < c) ? b : c); }
	 */

	/**
	 * @param list
	 *            array of numbers to compare
	 */
	static public final int min(final int[] list) {
		if (list.length == 0) { throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX); }
		int min = list[0];
		for (int i = 1; i < list.length; i++) {
			if (list[i] < min) {
				min = list[i];
			}
		}
		return min;
	}

	static public final long min(final long[] list) {
		if (list.length == 0) { throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX); }
		long min = list[0];
		for (int i = 1; i < list.length; i++) {
			if (list[i] < min) {
				min = list[i];
			}
		}
		return min;
	}

	static public final double min(final double[] list) {
		if (list.length == 0) { throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX); }
		double min = list[0];
		for (int i = 1; i < list.length; i++) {
			if (list[i] < min) {
				min = list[i];
			}
		}
		return min;
	}

	/**
	 * Find the minimum value in an array. Throws an ArrayIndexOutOfBoundsException if the array is
	 * length 0.
	 * 
	 * @param list
	 *            the source array
	 * @return The minimum value
	 */
	/*
	 * static public final double min(double[] list) { if (list.length == 0) { throw new
	 * ArrayIndexOutOfBoundsException(ERROR_MIN_MAX); } double min = list[0]; for (int i = 1; i <
	 * list.length; i++) { if (list[i] < min) min = list[i]; } return min; }
	 */

	static public final int constrain(final int amt, final int low, final int high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}

	static public final long constrain(final long amt, final long low, final long high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}

	/**
	 * ( begin auto-generated from constrain.xml ) Constrains a value to not exceed a maximum and
	 * minimum value. ( end auto-generated )
	 * 
	 * @webref math:calculation
	 * @param amt
	 *            the value to constrain
	 * @param low
	 *            minimum limit
	 * @param high
	 *            maximum limit
	 * @see PApplet#max(double, double, double)
	 * @see PApplet#min(double, double, double)
	 */

	static public final double constrain(final double amt, final double low, final double high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}

	static public final double mag(final double a, final double b) {
		return Math.sqrt((a * a) + (b * b));
	}

	/**
	 * ( begin auto-generated from mag.xml ) Calculates the magnitude (or length) of a vector. A
	 * vector is a direction in space commonly used in computer graphics and linear algebra. Because
	 * it has no "start" position, the magnitude of a vector can be thought of as the distance from
	 * coordinate (0,0) to its (x,y) value. Therefore, mag() is a shortcut for writing
	 * "dist(0, 0, x, y)". ( end auto-generated )
	 * 
	 * @webref math:calculation
	 * @param a
	 *            first value
	 * @param b
	 *            second value
	 * @param c
	 *            third value
	 * @see PApplet#dist(double, double, double, double)
	 */
	static public final double mag(final double a, final double b, final double c) {
		return Math.sqrt((a * a) + (b * b) + (c * c));
	}

	static public final double dist(final double x1, final double y1, final double x2, final double y2) {
		final double dx = x2 - x1;
		final double dy = y2 - y1;

		return Math.sqrt((dx * dx) + (dy * dy));
	}

	/**
	 * ( begin auto-generated from dist.xml ) Calculates the distance between two points. ( end
	 * auto-generated )
	 * 
	 * @webref math:calculation
	 * @param x1
	 *            x-coordinate of the first point
	 * @param y1
	 *            y-coordinate of the first point
	 * @param z1
	 *            z-coordinate of the first point
	 * @param x2
	 *            x-coordinate of the second point
	 * @param y2
	 *            y-coordinate of the second point
	 * @param z2
	 *            z-coordinate of the second point
	 */
	static public final double dist(final double x1, final double y1, final double z1, final double x2,
	        final double y2, final double z2) {
		final double dx = x2 - x1;
		final double dy = y2 - y1;
		final double dz = z2 - z1;
		return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}

	/**
	 * ( begin auto-generated from lerp.xml ) Calculates a number between two numbers at a specific
	 * increment. The <b>amt</b> parameter is the amount to interpolate between the two values where
	 * 0.0 equal to the first point, 0.1 is very near the first point, 0.5 is half-way in between,
	 * etc. The lerp function is convenient for creating motion along a straight path and for
	 * drawing dotted lines. ( end auto-generated )
	 * 
	 * @webref math:calculation
	 * @param start
	 *            first value
	 * @param stop
	 *            second value
	 * @param amt
	 *            double between 0.0 and 1.0
	 * @see PGraphics#curvePoint(double, double, double, double, double)
	 * @see PGraphics#bezierPoint(double, double, double, double, double)
	 */
	static public final double lerp(final double start, final double stop, final double amt) {
		return start + ((stop - start) * amt);
	}

	/**
	 * ( begin auto-generated from norm.xml ) Normalizes a number from another range into a value
	 * between 0 and 1. <br/>
	 * <br/>
	 * Identical to map(value, low, high, 0, 1); <br/>
	 * <br/>
	 * Numbers outside the range are not clamped to 0 and 1, because out-of-range values are often
	 * intentional and useful. ( end auto-generated )
	 * 
	 * @webref math:calculation
	 * @param value
	 *            the incoming value to be converted
	 * @param start
	 *            lower bound of the value's current range
	 * @param stop
	 *            upper bound of the value's current range
	 * @see PApplet#map(double, double, double, double, double)
	 * @see PApplet#lerp(double, double, double)
	 */
	static public final double norm(final double value, final double start, final double stop) {
		return (value - start) / (stop - start);
	}

	/**
	 * ( begin auto-generated from map.xml ) Re-maps a number from one range to another. In the
	 * example above, the number '25' is converted from a value in the range 0..100 into a value
	 * that ranges from the left edge (0) to the right edge (width) of the screen. <br/>
	 * <br/>
	 * Numbers outside the range are not clamped to 0 and 1, because out-of-range values are often
	 * intentional and useful. ( end auto-generated )
	 * 
	 * @webref math:calculation
	 * @param value
	 *            the incoming value to be converted
	 * @param start1
	 *            lower bound of the value's current range
	 * @param stop1
	 *            upper bound of the value's current range
	 * @param start2
	 *            lower bound of the value's target range
	 * @param stop2
	 *            upper bound of the value's target range
	 * @see PApplet#norm(double, double, double)
	 * @see PApplet#lerp(double, double, double)
	 */
	static public final double map(final double value, final double start1, final double stop1, final double start2,
	        final double stop2) {
		return start2 + ((stop2 - start2) * ((value - start1) / (stop1 - start1)));
	}
}
