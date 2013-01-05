package main.fractalGenome;

import static java.lang.Math.random;
import static main.Utils.map;
import static main.Utils.range;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import main.ColorSet;
import main.variations.Variation;

public final class FractalGenome {
	final public int	      nAffineTransformatioins;
	final public int[]	      affineProbabilities;
	final public double[][][]	affineMatrices;
	public ColorSet[]	      affineColor;
	public int	              currentMatrix	       = -1;

	public boolean	          variationToggle	   = true;
	public TreeSet<Integer>	  variations;
	final private int	      NUMBER_OF_VARIATIONS	= Variation.NUMBER_OF_VARIATIONS;
	public double[]	          variationWeights;
	private final int	      nVariations	       = (6 < NUMBER_OF_VARIATIONS) ? 6 : NUMBER_OF_VARIATIONS;

	public FractalGenome(final int minAffineTransforms, final int maxAffineTransforms) {
		nAffineTransformatioins = resetNAffineTransformations(minAffineTransforms, maxAffineTransforms);
		affineProbabilities = resetAffineProbabilities(nAffineTransformatioins);

		affineMatrices = resetAffineMatricies(nAffineTransformatioins);
		affineColor = resetAffineColor(nAffineTransformatioins);

		resetVariations();
	}

	private void resetVariations() {
		variations = new TreeSet<Integer>();

		while (variations.size() < nVariations) {
			variations.add(ThreadLocalRandom.current().nextInt(NUMBER_OF_VARIATIONS));
		}

		variationWeights = new double[NUMBER_OF_VARIATIONS];
		for (int i : range(variationWeights.length)) {
			variationWeights[i] = Math.random();
		}
	}

	public ColorSet[] resetAffineColor(final int nAffineTransformatioins) {
		final ColorSet[] cs = new ColorSet[nAffineTransformatioins];

		for (final int i : range(nAffineTransformatioins)) {
			cs[i] = new ColorSet(Math.random(), Math.random(), Math.random());
		}

		return cs;
	}

	private double[][][] resetAffineMatricies(final int nAffineTransformatioins) {
		final double[][][] matrices = new double[nAffineTransformatioins][2][3];
		for (int i = 0; i < matrices.length; i++) {
			for (int j = 0; j < matrices[i].length; j++) {
				for (int k = 0; k < matrices[i][j].length; k++) {
					matrices[i][j][k] = map(random(), 0, 1, -1, 1);
				}
			}
		}
		return matrices;
	}

	private int resetNAffineTransformations(int minAffineTransforms, int maxAffineTransforms) {
		// there must be 3 or more transformations to work properly
		minAffineTransforms = (minAffineTransforms >= 3) ? minAffineTransforms : 3;

		// makes sure max is > than min
		maxAffineTransforms = (maxAffineTransforms >= minAffineTransforms) ? maxAffineTransforms : minAffineTransforms;

		// picks a random number between max and min
		return (int) (random() * (maxAffineTransforms - minAffineTransforms)) + minAffineTransforms;
	}

	private int[] resetAffineProbabilities(final int nAffineTransformatioins) {
		final double[] affineProbabilities = new double[nAffineTransformatioins];
		System.out.println(nAffineTransformatioins);
		for (final int i : range(nAffineTransformatioins)) {
			affineProbabilities[i] = random();
		}
		Arrays.sort(affineProbabilities);

		final int[] jumpTable = new int[1000];
		int jumpPosition = 0;
		int affPosition = 0;
		while ((jumpPosition < jumpTable.length) && (affPosition < affineProbabilities.length)) {
			if (((double) jumpPosition / jumpTable.length) < affineProbabilities[affPosition]) {
				jumpTable[jumpPosition] = affPosition;
				jumpPosition++;
				continue;
			}
			affPosition++;
		}
		for (; jumpPosition < jumpTable.length; jumpPosition++) {
			jumpTable[jumpPosition] = nAffineTransformatioins - 1;
		}
		return jumpTable;
	}
}
