package fractalFlameV3;
/**
 * this class holds a mutable 2D vector 
 * 
 * @author aaron
 *
 */
public final class Vec2D {
	public double	x;
	public double	y;

	public Vec2D(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public Vec2D(final Vec2D v) {
		this(v.x, v.y);
	}

	/**
	 * adds vector 'v' to the current vector, returns updated self
	 * 
	 * @param v
	 * @return updated self
	 */
	public final Vec2D add(final Vec2D v) {
		x += v.x;
		y += v.y;

		return this;
	}

	/**
	 * scales self by the constant 'n', returns updated self
	 * 
	 * @param n
	 *            constant to scale by
	 * @return updated self
	 */
	public final Vec2D mul(final double n) {
		x *= n;
		y *= n;

		return this;
	}

	/**
	 * scales self by the constants 'x' and 'y', returns updated self
	 * 
	 * @param x
	 *            amount to scale vector's x by
	 * @param y
	 *            amount to scale vector's y by
	 * @return updated self
	 */
	public final Vec2D mul(final double x, final double y) {
		this.x *= x;
		this.y *= y;
		return this;
	}

	@Override
	public final String toString() {
		return ("(" + x + "," + y + ")");
	}

	/**
	 * sets current vector's x and y to input vector's x and y, returns updated self
	 * 
	 * @param v
	 *            input vector
	 * @return updated self
	 */
	public Vec2D set(final Vec2D v) {
		x = v.x;
		y = v.y;
		return this;
	}

	public Vec2D set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}
}
