package main.variations;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
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
		final double r = sqrt(rsq);
		final double t = atan2(x, y);
		final double p = atan2(y, x);

		final double p0 = sin(t + r);
		final double p1 = cos(t - r);

		pOut.x = pow(p0, 3) + pow(p1, 3);
		pOut.y = pow(p0, 3) - pow(p1, 3);

		return pOut;
	}

}
