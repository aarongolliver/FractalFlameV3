package fractalFlameV3;

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

	public final Vec2D add(final Vec2D v) {
		x += v.x;
		y += v.y;

		return this;
	}

	public final Vec2D mul(final double n) {
		x *= n;
		y *= n;

		return this;
	}

	public final Vec2D mul(final double x, final double y) {
		this.x *= x;
		this.y *= y;
		return this;
	}

	@Override
	public final String toString() {
		return ("(" + x + "," + y + ")");
	}

	public void set(final Vec2D v) {
		x = v.x;
		y = v.y;
	}

	public void set(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
}
