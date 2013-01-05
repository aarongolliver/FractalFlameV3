package main.fractalThread;

import java.util.concurrent.ThreadLocalRandom;

import main.ColorSet;
import main.Histogram;
import main.Vec2D;
import main.fractalGenome.FractalGenome;
import main.variations.Bent14;
import main.variations.Cosine20;
import main.variations.Diamond11;
import main.variations.Disc8;
import main.variations.Ex12;
import main.variations.Exponential18;
import main.variations.Fisheye16;
import main.variations.Handkerchief6;
import main.variations.Heart7;
import main.variations.Horseshoe4;
import main.variations.Hyperbolic10;
import main.variations.Julia13;
import main.variations.Linear0;
import main.variations.Polar5;
import main.variations.Popcorn17;
import main.variations.Power19;
import main.variations.Sinusodial1;
import main.variations.Spherical2;
import main.variations.Spiral9;
import main.variations.Swirl3;
import main.variations.Variation;
import main.variations.Waves15;

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
		System.out.println("Creating thread");
		final Vec2D tmpVec = new Vec2D(0, 0);
		final Vec2D addVec = new Vec2D(0, 0);
		final Vec2D pAffined = new Vec2D(0, 0);
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

			p.set(pAffined);

			if (Double.isInfinite(p.x) || Double.isInfinite(p.y) || Double.isNaN(p.x) || Double.isNaN(p.y)) {
				p.x = r.nextDouble(-1, 1);
				p.y = r.nextDouble(-1, 1);

				currentColor.set(0, 0, 0);
			} else {
				histogram.hit(p, currentColor);
			}
		}

	}

	public FractalThread(final FractalGenome genome, final ThreadSignal signal, final Histogram histogram) {
		this.genome = genome;
		this.signal = signal;
		this.histogram = histogram;
		variations = new Variation[genome.variations.size()];
		int i = 0;
		for (final int variation : genome.variations) {
			switch (variation) {
			case 0:
				variations[i++] = new Linear0(genome);
				break;
			case 1:
				variations[i++] = new Sinusodial1(genome);
				break;
			case 2:
				variations[i++] = new Spherical2(genome);
				break;
			case 3:
				variations[i++] = new Swirl3(genome);
				break;
			case 4:
				variations[i++] = new Horseshoe4(genome);
				break;
			case 5:
				variations[i++] = new Polar5(genome);
				break;
			case 6:
				variations[i++] = new Handkerchief6(genome);
				break;
			case 7:
				variations[i++] = new Heart7(genome);
				break;
			case 8:
				variations[i++] = new Disc8(genome);
				break;
			case 9:
				variations[i++] = new Spiral9(genome);
				break;
			case 10:
				variations[i++] = new Hyperbolic10(genome);
				break;
			case 11:
				variations[i++] = new Diamond11(genome);
				break;
			case 12:
				variations[i++] = new Ex12(genome);
				break;
			case 13:
				variations[i++] = new Julia13(genome);
				break;
			case 14:
				variations[i++] = new Bent14(genome);
				break;
			case 15:
				variations[i++] = new Waves15(genome);
				break;
			case 16:
				variations[i++] = new Fisheye16(genome);
				break;
			case 17:
				variations[i++] = new Popcorn17(genome);
				break;
			case 18:
				variations[i++] = new Exponential18(genome);
				break;
			case 19:
				variations[i++] = new Power19(genome);
				break;
			case 20:
				variations[i++] = new Cosine20(genome);
				break;
			}
		}
	}

	private final Vec2D affine(final Vec2D p, final double[][] a) {

		final double x = (p.x * a[0][0]) + (p.y * a[0][1]) + (a[0][2]);
		final double y = (p.x * a[1][0]) + (p.y * a[1][1]) + (a[1][2]);

		return new Vec2D(x, y);
	}
}
