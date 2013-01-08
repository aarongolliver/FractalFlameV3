package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public final class Eyefish27 extends Variation {

	public Eyefish27(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 27;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		pOut.x = (2 / (r + 1)) * x;
		pOut.y = (2 / (r + 1)) * y;

		return pOut;
	}

}
