package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public final class Heart7 extends Variation {

	public Heart7(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 7;
	}

	@Override
	public Vec2D v(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double tr = t * r;

		pOut.x = r * Math.sin(tr);
		pOut.y = r * Math.cos(tr);

		return pOut;
	}
}
