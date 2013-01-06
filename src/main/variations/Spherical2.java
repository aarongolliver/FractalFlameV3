package main.variations;

import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Spherical2 extends Variation {

	public Spherical2(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 2;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = sqrt(rsq);
		final double t = atan2(x, y);
		final double p = atan2(y, x);

		pOut.x = x / rsq;
		pOut.y = y / rsq;

		return pOut;
	}
}
