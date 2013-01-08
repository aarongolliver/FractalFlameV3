package fractalFlameV3.variations;

import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;

public abstract class Variation {

	final FractalGenome	    currentGenome;

	public int	            ID	                 = -1;

	public static final int	NUMBER_OF_VARIATIONS	= (31) + 1;

	public Variation(final FractalGenome currentGenome) {
		this.currentGenome = currentGenome;
	}

	public abstract Vec2D v(Vec2D pIn, Vec2D pOut);
}
