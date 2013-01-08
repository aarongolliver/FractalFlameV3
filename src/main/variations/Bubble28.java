package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Bubble28 extends Variation {

	public Bubble28(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 28;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		pOut.x = (4 / ((r * r) + 4)) * x;
		pOut.y = (4 / ((r * r) + 4)) * y;

		return pOut;
	}

}
