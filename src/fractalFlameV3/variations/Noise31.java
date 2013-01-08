package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public final class Noise31 extends Variation {

	public Noise31(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 31;
	}

	@Override
	public Vec2D v(final Vec2D pIn, final Vec2D pOut) {
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

		final double psi1 = Math.random();
		final double psi2 = Math.random();

		pOut.x = psi1 * (x * Math.cos(2 * Math.PI * psi2));
		pOut.y = psi1 * (y * Math.sin(2 * Math.PI * psi2));

		return pOut;
	}

}
