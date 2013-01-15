package fractalFlameV3.fractalGenome;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fractalFlameV3.ColorSet;
import fractalFlameV3.Utils;
import fractalFlameV3.variations.Bent14;
import fractalFlameV3.variations.Blob23;
import fractalFlameV3.variations.Bubble28;
import fractalFlameV3.variations.Cosine20;
import fractalFlameV3.variations.Cylinder29;
import fractalFlameV3.variations.Diamond11;
import fractalFlameV3.variations.Disc8;
import fractalFlameV3.variations.Ex12;
import fractalFlameV3.variations.Exponential18;
import fractalFlameV3.variations.Eyefish27;
import fractalFlameV3.variations.Fan22;
import fractalFlameV3.variations.FanTwo25;
import fractalFlameV3.variations.Fisheye16;
import fractalFlameV3.variations.Handkerchief6;
import fractalFlameV3.variations.Heart7;
import fractalFlameV3.variations.Horseshoe4;
import fractalFlameV3.variations.Hyperbolic10;
import fractalFlameV3.variations.Julia13;
import fractalFlameV3.variations.Linear0;
import fractalFlameV3.variations.PDJ24;
import fractalFlameV3.variations.Perspective30;
import fractalFlameV3.variations.Polar5;
import fractalFlameV3.variations.Popcorn17;
import fractalFlameV3.variations.Power19;
import fractalFlameV3.variations.Rings21;
import fractalFlameV3.variations.RingsTwo26;
import fractalFlameV3.variations.Sinusodial1;
import fractalFlameV3.variations.Spherical2;
import fractalFlameV3.variations.Spiral9;
import fractalFlameV3.variations.Swirl3;
import fractalFlameV3.variations.Variation;
import fractalFlameV3.variations.Waves15;

public final class FractalGenome {
	final public int	       nAffineTransformatioins;
	final public int[]	       affineProbabilities;
	final public double[][][]	affineMatrices;
	final public double[][][]	finalTransformMatrices;

	final private int	       JUMP_TABLE_SIZE	    = 1000;

	public ColorSet[]	       affineColor;
	public ColorSet[]	       finalColor;
	public int	               currentMatrix	    = -1;

	public boolean	           variationToggle	    = true;
	protected TreeSet<Integer>	variations;
	public double[]	           variationWeights;
	private final int	       nVariations	        = (int) ((Math.random() * 12) + 2);

	final public double[][]	   variationParameters;
	public boolean	           finalTransformToggle	= true;

	public double	           cameraXOffset;
	public double	           cameraYOffset;
	public double	           cameraXShrink;
	public double	           cameraYShrink;
	public boolean	           center	            = false;

	static public double	   gamma	            = 1;

	public FractalGenome(final int minAffineTransforms, final int maxAffineTransforms) {
		cameraXOffset = 0;
		cameraYOffset = 0;
		cameraXShrink = 5;
		cameraYShrink = 5;
		center = true;

		FractalGenome.gamma = 1;

		nAffineTransformatioins = resetNAffineTransformations(minAffineTransforms, maxAffineTransforms);
		affineProbabilities = newAffineJumpTable(nAffineTransformatioins, JUMP_TABLE_SIZE);

		affineMatrices = newAffineMatrix(nAffineTransformatioins);
		finalTransformMatrices = newAffineMatrix(nAffineTransformatioins);
		resetColors();

		variationParameters = newVariationParamaters();

		resetVariations();
	}

	public void resetColors() {
		affineColor = newColorArray(nAffineTransformatioins);
		finalColor = newColorArray(nAffineTransformatioins);
	}

	public void saveGsonRepresentation() {
		final GsonBuilder gb = new GsonBuilder();
		gb.setPrettyPrinting();
		final String filename = "images/" + hashCode() + ".fractalgenome";

		try {
			final String gsonString = gb.create().toJson(this);
			final FileWriter genomeWriter = new FileWriter(filename);
			genomeWriter.write(gsonString);
			genomeWriter.close();

			final FileWriter previousGenomeWriter = new FileWriter("images/last.fractalgenome");
			previousGenomeWriter.write(gsonString);
			previousGenomeWriter.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	/**
	 * returns a new array of paramaters to be used by variations variations access their paramaters
	 * by using accessing the array like so: paramaters[Variation.ID][paramater number]
	 *
	 * @return paramaters for variations
	 */
	private double[][] newVariationParamaters() {
		final double[][] p = new double[Variation.NUMBER_OF_VARIATIONS + 1][4];
		for (final double[] row : p) {
			for (final int i : Utils.range(row.length)) {
				row[i] = Utils.map(Math.random(), 0, 1, -1, 1);
			}
		}
		return p;
	}

	/**
	 * constructs a copy of the input genome
	 *
	 * @param genome
	 */
	public FractalGenome(final FractalGenome genome) {
		nAffineTransformatioins = genome.nAffineTransformatioins;
		affineProbabilities = genome.affineProbabilities;

		affineMatrices = genome.affineMatrices;
		finalTransformMatrices = genome.finalTransformMatrices;
		affineColor = genome.affineColor;
		finalColor = genome.finalColor;
		variations = genome.variations;
		variationWeights = genome.variationWeights;
		variationParameters = genome.variationParameters;
		variationToggle = genome.variationToggle;
		finalTransformToggle = genome.finalTransformToggle;
	}

	private void resetVariations() {
		variations = new TreeSet<Integer>();

		while (variations.size() < nVariations) {
			variations.add(ThreadLocalRandom.current().nextInt(Variation.NUMBER_OF_VARIATIONS));
		}

		variationWeights = new double[Variation.NUMBER_OF_VARIATIONS];

		for (final int i : Utils.range(variationWeights.length)) {
			variationWeights[i] = Math.random();
		}

		double sum = 0;
		for (final int i : variations) {
			sum += variationWeights[i];
		}
		for (final int i : Utils.range(variationWeights.length)) {
			variationWeights[i] /= sum;
		}

	}

	/**
	 * returns nColorSets length array of ColorSets
	 *
	 * @param nColorSets
	 *            number of color sets
	 * @return array of ColorSets
	 */
	public ColorSet[] newColorArray(final int nColorSets) {
		final ColorSet[] cs = new ColorSet[nColorSets];

		for (final int i : Utils.range(nColorSets)) {
			cs[i] = new ColorSet(Math.random(), Math.random(), Math.random());
		}

		return cs;
	}

	/**
	 * returns a new array of affine matricies. They are indexed as such: matrix[which matrix][2][3]
	 * "which matrix" corrosponds to which affine matrix you want, so running matrix[0] will give
	 * you the 0th affine matrix. It will be in the form of a 2x3 2D array addressed as such: [ [a,
	 * b, c] [d, e, f] ]
	 *
	 * @param nAffineTransformatioins
	 * @return new array of affine matricies
	 */
	private double[][][] newAffineMatrix(final int nAffineTransformatioins) {
		final double[][][] matrices = new double[nAffineTransformatioins][2][3];
		for (int i = 0; i < matrices.length; i++) {
			for (int j = 0; j < matrices[i].length; j++) {
				for (int k = 0; k < matrices[i][j].length; k++) {
					matrices[i][j][k] = Utils.map(Math.random(), 0, 1, -1, 1);
				}
			}
		}
		return matrices;
	}

	/**
	 * picks a random between min and max. The number must be greater than 3 if max < min, max is
	 * set to min
	 *
	 * @param minAffineTransforms
	 *            minimum number of affine transformations
	 * @param maxAffineTransforms
	 *            maximum number of affine transformations
	 * @return chosen number of affine transformations
	 */
	private int resetNAffineTransformations(int minAffineTransforms, int maxAffineTransforms) {
		// there must be 3 or more transformations to work properly
		minAffineTransforms = Utils.max(minAffineTransforms, 3);

		// makes sure max is > than min
		maxAffineTransforms = Utils.max(maxAffineTransforms, minAffineTransforms);

		// picks a random number between max and min
		return (int) Utils.map(Math.random(), 0, 1, minAffineTransforms, maxAffineTransforms);
	}

	/**
	 * returns a new jump table where each index refers to a specific affine transformation. The
	 * idea is you pick a random number between (0) and (jumpTable.length) and retrive the number at
	 * that index. The retrieved number corrosponds to the affine matrix picked. By making the jump
	 * table larger, you allow for more fine-grained probabilities. Right now the least number of
	 * times a matrix can be picked is 1/1000
	 *
	 * @param nAffineTransformatioins
	 *            number of affine transformations
	 * @return the generated jumptable
	 */
	private int[] newAffineJumpTable(final int nAffineTransformatioins, final int jumpTableSize) {
		final double[] affineProbabilities = new double[nAffineTransformatioins];

		for (final int i : Utils.range(nAffineTransformatioins)) {
			affineProbabilities[i] = Math.random();
		}
		Arrays.sort(affineProbabilities);

		final int[] jumpTable = new int[jumpTableSize];
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

	/**
	 * returns a list of variation function objects based on the provided genome
	 *
	 * @param genome
	 *            genome of the variation functions
	 * @return array of Variation objects
	 */
	public Variation[] getVariationObjects(final FractalGenome genome) {
		final Variation[] variations = new Variation[this.variations.size()];
		int iv = 0;
		int previousVariationID = -1;
		Variation currentVaritionObject = null;
		for (int currentVariationID : genome.variations) {
			if (currentVariationID != previousVariationID) {
				switch (currentVariationID) {
				case 0:
					currentVaritionObject = new Linear0(genome);
					break;
				case 1:
					currentVaritionObject = new Sinusodial1(genome);
					break;
				case 2:
					currentVaritionObject = new Spherical2(genome);
					break;
				case 3:
					currentVaritionObject = new Swirl3(genome);
					break;
				case 4:
					currentVaritionObject = new Horseshoe4(genome);
					break;
				case 5:
					currentVaritionObject = new Polar5(genome);
					break;
				case 6:
					currentVaritionObject = new Handkerchief6(genome);
					break;
				case 7:
					currentVaritionObject = new Heart7(genome);
					break;
				case 8:
					currentVaritionObject = new Disc8(genome);
					break;
				case 9:
					currentVaritionObject = new Spiral9(genome);
					break;
				case 10:
					currentVaritionObject = new Hyperbolic10(genome);
					break;
				case 11:
					currentVaritionObject = new Diamond11(genome);
					break;
				case 12:
					currentVaritionObject = new Ex12(genome);
					break;
				case 13:
					currentVaritionObject = new Julia13(genome);
					break;
				case 14:
					currentVaritionObject = new Bent14(genome);
					break;
				case 15:
					currentVaritionObject = new Waves15(genome);
					break;
				case 16:
					currentVaritionObject = new Fisheye16(genome);
					break;
				case 17:
					currentVaritionObject = new Popcorn17(genome);
					break;
				case 18:
					currentVaritionObject = new Exponential18(genome);
					break;
				case 19:
					currentVaritionObject = new Power19(genome);
					break;
				case 20:
					currentVaritionObject = new Cosine20(genome);
					break;
				case 21:
					currentVaritionObject = new Rings21(genome);
					break;
				case 22:
					currentVaritionObject = new Fan22(genome);
					break;
				case 23:
					currentVaritionObject = new Blob23(genome);
					break;
				case 24:
					currentVaritionObject = new PDJ24(genome);
					break;
				case 25:
					currentVaritionObject = new FanTwo25(genome);
					break;
				case 26:
					currentVaritionObject = new RingsTwo26(genome);
					break;
				case 27:
					currentVaritionObject = new Eyefish27(genome);
					break;
				case 28:
					currentVaritionObject = new Bubble28(genome);
					break;
				case 29:
					currentVaritionObject = new Cylinder29(genome);
					break;
				case 30:
					currentVaritionObject = new Perspective30(genome);
					break;
				case 31:
					// I disabled Noise because it's always ugly
					// variations[i++] = new Noise31(genome);
					currentVaritionObject = new Rings21(genome);
					break;
				default:
					while (true) {
						// temporary "error" if you ask for a function that doesn't exist
						System.out.println("MISTAKE: variation " + currentVariationID + " does not exist!");
					}
				}
				previousVariationID = currentVariationID;
			}
			variations[iv++] = currentVaritionObject;
		}
		return variations;
	}
}
