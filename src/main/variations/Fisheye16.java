package main.variations;

import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
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
		final double r = sqrt(rsq);
		final double t = atan2(x, y);
		final double p = atan2(y, x);

		final double multiplier = 2 / (r + 1);

		pOut.x = multiplier * y;
		pOut.y = multiplier * x;

		return pOut;
	}

}
