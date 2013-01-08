package main.variations;

import main.Vec2D;
import main.fractalGenome.FractalGenome;

public abstract class Variation {

	final FractalGenome	    currentGenome;

	public int	            ID	                 = -1;

	public static final int	NUMBER_OF_VARIATIONS	= (31) + 1;

	public Variation(final FractalGenome currentGenome) {
		this.currentGenome = currentGenome;
	}

	public abstract Vec2D f(Vec2D pIn, Vec2D pOut);
}
