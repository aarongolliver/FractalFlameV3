package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public class Horseshoe4 extends Variation {

	public Horseshoe4(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 4;
	}

	@Override
	public Vec2D v(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		pOut.x = ((x - y) * (x + y)) / r;
		pOut.y = 2 * x * y;

		return pOut;
	}

}
