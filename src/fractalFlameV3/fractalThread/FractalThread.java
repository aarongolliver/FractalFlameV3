package fractalFlameV3.fractalThread;

import java.util.concurrent.ThreadLocalRandom;

import fractalFlameV3.ColorSet;
import fractalFlameV3.Histogram;
import fractalFlameV3.Vec2D;
import fractalFlameV3.fractalGenome.FractalGenome;
import fractalFlameV3.variations.Variation;

public final class FractalThread extends Thread {
	/*
	 * bind this thread's random instance to 'r' for easy access.
	 */
	final ThreadLocalRandom	    r	= ThreadLocalRandom.current();

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

	public final void run() {
		// set the thread to minimum priority so it doesn't make the system unusable
		setPriority(Thread.MIN_PRIORITY);

		/*
		 * create some vectors used during the calculation. I try to avoid creating new objects at
		 * all costs during the generation loop to make it a bit faster
		 */

		// the point in space the simulation is currently pointing at.
		final Vec2D p = new Vec2D(r.nextDouble(-1, 1), r.nextDouble(-1, 1));

		// This vector is used to hold the value of calculations so I don't modify the original
		// value
		final Vec2D tmpVec = new Vec2D(0, 0);

		// accumlator for the variation functions
		final Vec2D addVec = new Vec2D(0, 0);

		/*
		 * the color of the point being pointed at (RGB).
		 */
		final ColorSet currentColor = new ColorSet(0);

		/*
		 * number of squential iterations the simulation has completed successfully
		 */
		int iters = 0;

		// the generation thread will run until it's signaled to stop through 'signal'
		while (signal.running) {

			// randomly selects the affine transformation to be applied to p
			int j = r.nextInt(0, genome.affineProbabilities.length);
			j = genome.affineProbabilities[j];
			genome.currentMatrix = j;

			// apply the affine matrix
			p.set(affine(genome.affineMatrices[j], p, tmpVec));

			// update the color with the color associated with this affine transformation
			currentColor.hit(genome.affineColor[j]);

			// apply non-linear variations
			if (genome.variationToggle) {
				// zero the accumlator because it doesn't reset between iterations
				addVec.set(0, 0);

				// apply the variations, multiplying each by it's variation weight
				for (final Variation v : variations) {
					addVec.add(v.f(p, tmpVec).mul(genome.variationWeights[v.ID]));
				}

				// update the point
				p.set(addVec);
			}

			if (genome.finalTransformToggle) {
				// apply the "Final" transformation
				p.set(affine(genome.finalTransformMatrices[j], p, tmpVec));

				// update the color with the color associated with this final transformation
				currentColor.hit(genome.finalColor[j]);
			}

			/*
			 * if the point escaped to infinity, or ir NaN, reset the point, color, and number of
			 * iterations and restart the simulation
			 */
			if (Double.isInfinite(p.x) || Double.isInfinite(p.y) || Double.isNaN(p.x) || Double.isNaN(p.y)) {
				p.x = r.nextDouble(-1, 1);
				p.y = r.nextDouble(-1, 1);

				currentColor.set(0, 0, 0);
				iters = 0;

				continue;
			} else {
				// discard the first 50 iterations to allow the point to converge towards the set
				// that makes up the attractor
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

	/**
	 * applies the affine matrix 'a' to the point 'pin', saving the result into pout
	 * 
	 * @param a
	 *            affine matrix to apply
	 * @param vectorIn
	 *            vector to apply the matrix to
	 * @param vectorOut
	 *            vector to save the result
	 * @return
	 */
	private final Vec2D affine(final double[][] a, final Vec2D vectorIn, final Vec2D vectorOut) {

		final double x = (vectorIn.x * a[0][0]) + (vectorIn.y * a[0][1]) + (a[0][2]);
		final double y = (vectorIn.x * a[1][0]) + (vectorIn.y * a[1][1]) + (a[1][2]);

		return vectorOut.set(x, y);
	}
}
