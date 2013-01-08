package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public final class Power19 extends Variation {

	public Power19(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 19;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double sinT = Math.sin(t);

		final double multiplier = Math.pow(r, sinT);

		pOut.x = multiplier * Math.cos(t);
		pOut.y = multiplier * sinT;

		return pOut;
	}

}
