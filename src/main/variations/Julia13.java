package main.variations;

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
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double sqrtR = Math.sqrt(r);

		final double omega = (ThreadLocalRandom.current().nextDouble() >= .5) ? 0 : Math.PI;

		pOut.x = sqrtR * Math.cos((t / 2) + omega);
		pOut.y = sqrtR * Math.sin((t / 2) + omega);

		return pOut;
	}
}
