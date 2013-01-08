package main.variations;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.*;

import java.util.concurrent.ThreadLocalRandom;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Perspective30 extends Variation {
	final double	p1, p2;

	public Perspective30(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 30;
		p1 = currentGenome.variationParameters[ID][0] * PI * 2;
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

		pOut.x = p2 / (p2 - y * sin(p1 * PI / 4)) * x;
		pOut.y = p2 / (p2 - y * cos(p1 * PI / 4)) * y * cos(p1);

		return pOut;
	}

}
