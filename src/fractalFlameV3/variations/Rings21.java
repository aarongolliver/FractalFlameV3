package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public final class Rings21 extends Variation {
	public Rings21(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 21;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = Math.sqrt(rsq);
		final double t = Math.atan2(x, y);
		final double p = Math.atan2(y, x);

		final double[][] currentMatrix = currentGenome.affineMatrices[currentGenome.currentMatrix];
		final double a = currentMatrix[0][0];
		final double b = currentMatrix[0][1];
		final double c = currentMatrix[0][2];
		final double d = currentMatrix[1][0];
		final double e = currentMatrix[1][1];
		final double f = currentMatrix[1][2];

		final double multiplier = ((((r + (c * c)) % (2 * c * c)) - (c * c)) + (r * (1 - (c * c))));
		pOut.x = multiplier * Math.cos(t);
		pOut.y = multiplier * Math.sin(t);

		return pOut;
	}

}
