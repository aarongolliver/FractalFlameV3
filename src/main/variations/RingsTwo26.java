package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class RingsTwo26 extends Variation {
	final double	p1;

	public RingsTwo26(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 26;
		p1 = Math.pow(currentGenome.variationParameters[ID][0], 1);
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

		final double T = (r - (2 * p1 * p1 * Math.floor((r + (p1 * p1)) / (2 * p1 * p1)))) + (r * (1 - (p1 * p1)));

		pOut.x = T * Math.sin(t);
		pOut.y = T * Math.cos(t);

		return pOut;
	}

}
