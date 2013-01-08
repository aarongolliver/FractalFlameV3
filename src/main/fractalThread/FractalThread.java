package main.fractalThread;

import java.util.concurrent.ThreadLocalRandom;

import main.ColorSet;
import main.Histogram;
import main.Vec2D;
import main.fractalGenome.FractalGenome;
import main.variations.Variation;

public final class FractalThread extends Thread {
	/*
	 * bind this thread's random instance to 'r' for easy access.
	 */
	final ThreadLocalRandom	    r	         = ThreadLocalRandom.current();

	/*
	 * the point in space the simulation is currently pointing at.
	 */
	private final Vec2D	        p	         = new Vec2D(r.nextDouble(-1, 1), r.nextDouble(-1, 1));

	/*
	 * the color of the point being pointed at (RGB).
	 */
	private final ColorSet	    currentColor	= new ColorSet(0);

	/*
	 * the thread exits when this turns false.
	 */
	private final ThreadSignal	signal;

	/*
	 * genome of the thread being worked on.
	 */
	private final FractalGenome	genome;

	/*
	 * variations to be used by the thread
	 */
	private final Variation[]	variations;

	/*
	 * histogram being worked on
	 */
	private final Histogram	    histogram;

	@Override
	public final void run() {
		final Vec2D tmpVec = new Vec2D(0, 0);
		final Vec2D addVec = new Vec2D(0, 0);
		final Vec2D pAffined = new Vec2D(0, 0);
		int iters = 0;
		while (signal.running) {
			addVec.set(0, 0);
			// randomly selects the affine transformation to be applied to p
			int j = r.nextInt(0, genome.affineProbabilities.length);
			j = genome.affineProbabilities[j];
			genome.currentMatrix = j;
			// apply the matrix
			pAffined.set(affine(p, genome.affineMatrices[j]));

			// change the color of the point
			currentColor.hit(genome.affineColor[j]);

			// apply non-linear variations
			if (genome.variationToggle) {
				for (final Variation v : variations) {
					addVec.add(v.f(pAffined, tmpVec).mul(genome.variationWeights[v.ID]));
				}
				pAffined.set(addVec);
			}

			// apply the final transformation
			if (genome.finalTransformToggle) {
				pAffined.set(affine(pAffined, genome.finalTransformMatrices[j]));
				currentColor.hit(genome.finalColor[j]);
			}


			p.set(pAffined);

			if (Double.isInfinite(p.x) || Double.isInfinite(p.y) || Double.isNaN(p.x) || Double.isNaN(p.y)) {
				p.x = r.nextDouble(-1, 1);
				p.y = r.nextDouble(-1, 1);

				currentColor.set(0, 0, 0);
				iters = 0;
			} else {
				if (++iters >= 50) {
					histogram.hit(p, currentColor);
				}
			}
		}

	}

	public FractalThread(final FractalGenome genome, final ThreadSignal signal, final Histogram histogram) {
		this.genome = new FractalGenome(genome);
		this.signal = signal;
		this.histogram = histogram;
		variations = genome.getVariationObjects(this.genome);

	}

	private final Vec2D affine(final Vec2D p, final double[][] a) {

		final double x = (p.x * a[0][0]) + (p.y * a[0][1]) + (a[0][2]);
		final double y = (p.x * a[1][0]) + (p.y * a[1][1]) + (a[1][2]);

		return new Vec2D(x, y);
	}
}
