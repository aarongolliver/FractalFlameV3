package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Ex12 extends Variation {

	public Ex12(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 12;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double p0 = Math.sin(t + r);
		final double p1 = Math.cos(t - r);

		pOut.x = Math.pow(p0, 3) + Math.pow(p1, 3);
		pOut.y = Math.pow(p0, 3) - Math.pow(p1, 3);

		return pOut;
	}

}
