package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Spiral9 extends Variation {

	public Spiral9(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 9;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		pOut.x = (Math.cos(t) + Math.sin(r)) / r;
		pOut.y = (Math.sin(t) - Math.cos(r)) / r;

		return pOut;
	}
}
