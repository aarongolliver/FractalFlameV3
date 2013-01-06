package main.variations;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
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
		final double r = sqrt(rsq);
		final double t = atan2(x, y);
		final double p = atan2(y, x);

		final double e = exp(x - 1);
		final double piY = PI * y;

		pOut.x = e * cos(piY);
		pOut.y = e * sin(piY);

		return pOut;
	}
}
