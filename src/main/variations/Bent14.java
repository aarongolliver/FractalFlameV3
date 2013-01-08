package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Bent14 extends Variation {

	public Bent14(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 14;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		pOut.x = x;
		pOut.y = y;

		if ((x < 0) && (y >= 0)) {
			pOut.x = 2 * x;
			pOut.y = y;
		} else if ((x >= 0) && (y < 0)) {
			pOut.x = x;
			pOut.y = y / 2;
		} else if ((x < 0) && (y < 0)) {
			pOut.x = x;
			pOut.y = y;
		}

		return pOut;
	}

}
