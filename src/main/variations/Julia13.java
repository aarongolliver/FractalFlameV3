package main.variations;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.concurrent.ThreadLocalRandom;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Julia13 extends Variation {

	public Julia13(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 13;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = sqrt(rsq);
		final double t = atan2(x, y);
		final double p = atan2(y, x);

		final double sqrtR = sqrt(r);

		final double omega = (ThreadLocalRandom.current().nextDouble() >= .5) ? 0 : PI;

		pOut.x = sqrtR * cos((t / 2) + omega);
		pOut.y = sqrtR * sin((t / 2) + omega);

		return pOut;
	}
}
