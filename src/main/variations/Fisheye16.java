package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Fisheye16 extends Variation {

	public Fisheye16(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 16;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double multiplier = 2 / (r + 1);

		pOut.x = multiplier * y;
		pOut.y = multiplier * x;

		return pOut;
	}

}
