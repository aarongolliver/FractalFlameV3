package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public class Handkerchief6 extends Variation {

	public Handkerchief6(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 6;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		pOut.x = r * Math.sin(t + r);
		pOut.y = r * Math.cos(t - r);

		return pOut;
	}

}
