package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public final class Linear0 extends Variation {

	public Linear0(final FractalGenome currentGenome) {
		super(currentGenome);
		ID = 0;
	}

	@Override
	public Vec2D f(final Vec2D pIn, final Vec2D pOut) {
		final double x = pIn.x;
		final double y = pIn.y;

		pOut.x = x;
		pOut.y = y;

		return pOut;
	}

}
