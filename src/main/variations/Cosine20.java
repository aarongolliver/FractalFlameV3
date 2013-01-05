package main.variations;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.cosh;
import static java.lang.Math.sin;
import static java.lang.Math.sinh;
import static java.lang.Math.sqrt;
import main.Vec2D;
import main.fractalGenome.FractalGenome;

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
		final double r = sqrt(rsq);
		final double t = atan2(x, y);
		final double p = atan2(y, x);

		final double piX = PI * x;

		pOut.x = cos(piX) * cosh(y);
		pOut.y = sin(piX) * sinh(y);

		return pOut;
	}

}
