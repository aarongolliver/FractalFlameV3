package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public final class Cosine20 extends Variation {

	public Cosine20(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 20;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double piX = Math.PI * x;

		pOut.x = Math.cos(piX) * Math.cosh(y);
		pOut.y = Math.sin(piX) * Math.sinh(y);

		return pOut;
	}

}
