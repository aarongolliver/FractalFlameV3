package main.variations;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.concurrent.ThreadLocalRandom;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class FanTwo25 extends Variation {
	final double	p1, p2;

	public FanTwo25(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 25;
		p1 = currentGenome.variationParameters[ID][0];
		p2 = currentGenome.variationParameters[ID][1];
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;
		final double rsq = (x * x) + (y * y);
		final double r = sqrt(rsq);
		final double t = atan2(x, y);
		final double p = atan2(y, x);

		final double[][] currentMatrix = currentGenome.affineMatrices[currentGenome.currentMatrix];
		final double a = currentMatrix[0][0];
		final double b = currentMatrix[0][1];
		final double c = currentMatrix[0][2];
		final double d = currentMatrix[1][0];
		final double e = currentMatrix[1][1];
		final double f = currentMatrix[1][2];

		final double T = (t + p2) - (p1 * floor((2 * t * p2) / p1));
		if (T > (p1 / 2)) {
			pOut.x = r * sin(t - (p1 / 2));
			pOut.y = r * cos(t - (p1 / 2));
		} else {
			pOut.x = r * sin(t + (p1 / 2));
			pOut.y = r * cos(t + (p1 / 2));
		}

		return pOut;
	}
}
