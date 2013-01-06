package main.variations;

import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import main.Vec2D;
import main.fractalGenome.FractalGenome;

public class Horseshoe4 extends Variation {

	public Horseshoe4(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 4;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = sqrt(rsq);
		final double t = atan2(x, y);
		final double p = atan2(y, x);

		pOut.x = ((x - y) * (x + y)) / r;
		pOut.y = 2 * x * y;

		return pOut;
	}

}
