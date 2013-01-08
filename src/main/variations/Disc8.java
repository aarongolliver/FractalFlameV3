package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public class Disc8 extends Variation {

	public Disc8(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 8;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double tOverPi = t / Math.PI;
		final double piR = Math.PI * r;

		pOut.x = tOverPi * Math.sin(piR);
		pOut.y = tOverPi * Math.cos(piR);

		return pOut;
	}
}
