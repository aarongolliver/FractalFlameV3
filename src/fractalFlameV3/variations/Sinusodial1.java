package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public final class Sinusodial1 extends Variation {

	public Sinusodial1(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 1;
	}

	@Override
	public Vec2D v(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		pOut.x = Math.sin(x);
		pOut.y = Math.sin(y);

		return pOut;
	}
}
