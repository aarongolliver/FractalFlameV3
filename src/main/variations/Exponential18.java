package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Exponential18 extends Variation {
	public Exponential18(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 18;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double e = Math.exp(x - 1);
		final double piY = Math.PI * y;

		pOut.x = e * Math.cos(piY);
		pOut.y = e * Math.sin(piY);

		return pOut;
	}
}
