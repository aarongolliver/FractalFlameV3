package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Fan22 extends Variation {
	public Fan22(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 22;
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

		final double T = Math.PI * c * c;

		if (((t + f) % (T)) > (T / 2)) {
			pOut.x = r * (Math.cos(t - (T / 2)));
			pOut.y = r * (Math.sin(t - (T / 2)));
		} else {
			pOut.x = r * (Math.cos(t + (T / 2)));
			pOut.y = r * (Math.sin(t + (T / 2)));
		}

		return pOut;
	}

}
