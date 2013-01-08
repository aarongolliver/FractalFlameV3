package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Swirl3 extends Variation {

	public Swirl3(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 3;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double srsq = Math.sin(rsq);
		final double crsq = Math.cos(rsq);

		pOut.x = (x * srsq) - (y * crsq);
		pOut.y = (x * crsq) + (y * srsq);

		return pOut;
	}

}
