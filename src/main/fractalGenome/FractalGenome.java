package main.fractalGenome;

import static java.lang.Math.*;
import static main.Utils.*;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import main.ColorSet;
import main.variations.Bent14;
import main.variations.Blob23;
import main.variations.*;
import main.variations.Diamond11;
import main.variations.Disc8;
import main.variations.Ex12;
import main.variations.Exponential18;
import main.variations.Fan22;
import main.variations.FanTwo25;
import main.variations.Fisheye16;
import main.variations.Handkerchief6;
import main.variations.Heart7;
import main.variations.Horseshoe4;
import main.variations.Hyperbolic10;
import main.variations.Julia13;
import main.variations.Linear0;
import main.variations.PDJ24;
import main.variations.Polar5;
import main.variations.Popcorn17;
import main.variations.Power19;
import main.variations.Rings21;
import main.variations.Sinusodial1;
import main.variations.Spherical2;
import main.variations.Spiral9;
import main.variations.Swirl3;
import main.variations.Variation;
import main.variations.Waves15;

public final class FractalGenome {
	final public int	       nAffineTransformatioins;
	final public int[]	       affineProbabilities;
	final public double[][][]	affineMatrices;
	final public double[][][]	finalTransformMatrices;

	public ColorSet[]	       affineColor;
	public ColorSet[]	       finalColor;
	public int	               currentMatrix	    = -1;

	public boolean	           variationToggle	    = true;
	protected TreeSet<Integer>	variations;
	public double[]	           variationWeights;
	private final int	       nVariations	        = (int) (random() * 12 + 2);

	final public double[][]	   variationParameters;
	public boolean	           finalTransformToggle	= true;

	static public double	   cameraXOffset	    = 0;
	static public double	   cameraYOffset	    = 0;
	static public double	   cameraXShrink	    = 10;
	static public double	   cameraYShrink	    = 10;
	static public boolean	   center	            = false;
	static public boolean	   logScale	            = false;
	static public boolean	   linearScale	        = false;

	static public double	   gamma	            = 1;

	public void setLogScale() {
		logScale = true;
		linearScale = false;
	}

	public void setLinearScale() {
		logScale = false;
		linearScale = true;
	}

	public FractalGenome(final int minAffineTransforms, final int maxAffineTransforms) {

		cameraXOffset = 0;
		cameraYOffset = 0;
		cameraXShrink = 10;
		cameraYShrink = 10;
		center = false;
		logScale = false;
		linearScale = false;

		gamma = 1;

		nAffineTransformatioins = resetNAffineTransformations(minAffineTransforms, maxAffineTransforms);
		affineProbabilities = resetAffineProbabilities(nAffineTransformatioins);

		affineMatrices = resetAffineMatricies(nAffineTransformatioins);
		finalTransformMatrices = resetAffineMatricies(nAffineTransformatioins);
		affineColor = resetAffineColor(nAffineTransformatioins);
		finalColor = resetAffineColor(nAffineTransformatioins);

		variationParameters = resetVariationParameters();

		resetVariations();
	}

	private double[][] resetVariationParameters() {
		double[][] p = new double[Variation.NUMBER_OF_VARIATIONS + 1][4];
		for (double[] row : p)
			for (int i : range(row.length))
				row[i] = map(random(), 0, 1, -1, 1);
		return p;
	}

	public FractalGenome(final FractalGenome genome) {
		this.nAffineTransformatioins = genome.nAffineTransformatioins;
		this.affineProbabilities = genome.affineProbabilities;

		this.affineMatrices = genome.affineMatrices;
		this.finalTransformMatrices = genome.finalTransformMatrices;
		this.affineColor = genome.affineColor;
		this.finalColor = genome.finalColor;
		this.variations = genome.variations;
		this.variationWeights = genome.variationWeights;
		this.variationParameters = genome.variationParameters;
		this.variationToggle = genome.variationToggle;
		this.finalTransformToggle = genome.finalTransformToggle;
	}

	private void resetVariations() {
		variations = new TreeSet<Integer>();

		while (variations.size() < nVariations) {
			variations.add(ThreadLocalRandom.current().nextInt(Variation.NUMBER_OF_VARIATIONS));
		}

		variationWeights = new double[Variation.NUMBER_OF_VARIATIONS];

		for (final int i : range(variationWeights.length)) {
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

	public Variation[] getVariationObjects(final FractalGenome genome) {
		final Variation[] variations = new Variation[this.variations.size()];
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
			case 21:
				variations[i++] = new Rings21(genome);
				break;
			case 22:
				variations[i++] = new Fan22(genome);
				break;
			case 23:
				variations[i++] = new Blob23(genome);
				break;
			case 24:
				variations[i++] = new PDJ24(genome);
				break;
			case 25:
				variations[i++] = new FanTwo25(genome);
				break;
			case 26:
				variations[i++] = new RingsTwo26(genome);
				break;
			case 27:
				variations[i++] = new Eyefish27(genome);
				break;
			case 28:
				variations[i++] = new Bubble28(genome);
				break;
			case 29:
				variations[i++] = new Cylinder29(genome);
				break;
			case 30:
				variations[i++] = new Perspective30(genome);
				break;
			case 31:
				// I disabled Noise because it's always ugly
				//variations[i++] = new Noise31(genome);
				variations[i++] = new Rings21(genome);
				break;
			default:
				while (true)
					System.out.println("MISTAKE");
			}
		}
		return variations;
	}
}
