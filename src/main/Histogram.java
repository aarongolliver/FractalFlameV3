package main;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import main.fractalGenome.FractalGenome;

public class Histogram {
	/**
	 * width of screen
	 */
	private final int	   swid;

	/**
	 * width of histogram (swid * ss)
	 */
	private final int	   hwid;

	/**
	 * height of screen
	 */
	private final int	   shei;

	/**
	 * height of histogram (shei * ss)
	 */
	private final int	   hhei;

	/**
	 * super samples
	 */
	private final int	   ss;

	/**
	 * number of samples per pixel on screen
	 */
	private final int	   ssSquared;

	/**
	 * flat array that holds the histogram, addressed using the formula: point(x, y, color) = (4 *
	 * (x + (y * hwid))) + (color) where x = [0, hwid), y = [0, hhei - 1), color = [0, 3]
	 */
	private final double[]	h;

	/**
	 * temporary array used to calculate
	 */
	private final double[]	image;

	private double	       cameraXOffset	= 0;
	private double	       cameraYOffset	= 0;
	private double	       cameraXShrink	= 10;
	private double	       cameraYShrink	= 10;
	private boolean	       center	     = true;
	private boolean	       logScale	     = false;
	private boolean	       linearScale	 = false;

	private final double	gamma	     = 1;

	/**
	 * @param swid
	 *            width of screen
	 * @param shei
	 *            height of screen
	 * @param ss
	 *            samples per pixel
	 */
	public Histogram(final int swid, final int shei, final int ss) {
		this.swid = swid;
		this.shei = shei;
		this.ss = ss;
		ssSquared = ss * ss;

		hwid = swid * ss;
		hhei = shei * ss;

		h = new double[hwid * hhei * 4];
		image = new double[swid * shei * 4];
	}

	public void updatePixels(final int[] pixels, FractalGenome genome) {
		cameraXOffset = genome.cameraXOffset;
		cameraYOffset = genome.cameraYOffset;
		cameraXShrink = genome.cameraXShrink;
		cameraYShrink = genome.cameraYShrink;
		center = genome.center;
		logScale = genome.logScale;
		linearScale = genome.linearScale;

		double gamma = genome.gamma;
		double maxA = 0;

		for (int hy = 0; hy < hhei; hy++) {
			for (int hx = 0; hx < hwid; hx++) {
				final int hi = 4 * (hx + (hy * hwid));
				final int ix = hx / ss;
				final int iy = hy / ss;
				final int ii = 4 * (ix + (iy * swid));

				final double r = h[hi + 0];
				final double g = h[hi + 1];
				final double b = h[hi + 2];
				final double a = h[hi + 3];

				image[ii + 0] += r;
				image[ii + 1] += g;
				image[ii + 2] += b;
				image[ii + 3] += a;

				final double imga = image[ii + 3];

				maxA = (maxA > imga) ? maxA : imga;
			}
		}

		final double logMaxA = Math.log(maxA / ssSquared);

		for (int iy = 0; iy < shei; iy++) {
			for (int ix = 0; ix < swid; ix++) {
				final int pixels_index = (ix + (iy * swid));
				final int index = 4 * pixels_index;
				final double aAvg = image[index + 3] / ssSquared;

				if ((aAvg > 1) || !logScale) {
					final double rAvg = image[index + 0] / ssSquared;
					final double gAvg = image[index + 1] / ssSquared;
					final double bAvg = image[index + 2] / ssSquared;
					double colorScaleFactor = 1;
					if (logScale) {
						colorScaleFactor = pow(log(aAvg) / logMaxA, 1.0 / gamma);
					} else if (linearScale) {
						colorScaleFactor = aAvg / (maxA / ss);
					}

					final int a = 0xFF;
					final int r = ((int) ((rAvg * colorScaleFactor) * 0xFF));
					final int g = ((int) ((gAvg * colorScaleFactor) * 0xFF));
					final int b = ((int) ((bAvg * colorScaleFactor) * 0xFF));

					pixels[pixels_index] = (a << 24) | (r << 16) | (g << 8) | (b << 0);
				} else {
					pixels[pixels_index] = (0xFF << 24);
				}
				image[index + 0] = 0;
				image[index + 1] = 0;
				image[index + 2] = 0;
				image[index + 3] = 0;
			}
		}
	}

	public void hit(final Vec2D p, final double r, final double g, final double b) {
		final int x = (int) (((p.x + cameraXOffset) * (hwid / cameraXShrink)) + (hwid / 2));
		final int y = (int) (((p.y + cameraYOffset) * (hhei / cameraYShrink)) + (hhei / 2));

		hit(x, y, r, g, b);

	}

	public void hit(final Vec2D p, final ColorSet c) {
		final int x = (int) (((p.x + cameraXOffset) * (hwid / cameraXShrink)) + (hwid / 2));
		final int y = (int) (((p.y + cameraYOffset) * (hhei / cameraYShrink)) + (hhei / 2));

		hit(x, y, c.r, c.g, c.b);

	}

	public void hit(final double x, final double y, final double r, final double g, final double b) {
		final int ix = (int) (((x + cameraXOffset) * (hwid / cameraXShrink)) + (center ? (hwid / 2) : 0));
		final int iy = (int) (((y + cameraYOffset) * (hhei / cameraYShrink)) + (center ? (hhei / 2) : 0));

		hit(ix, iy, r, g, b);
	}

	private void hit(final int x, final int y, final double r, final double g, final double b) {
		if ((x >= 0) && (x < hwid) && (y >= 0) && (y < hhei)) {
			final int index = 4 * (x + (y * hwid));

			h[index + 0] += r;
			h[index + 1] += g;
			h[index + 2] += b;

			h[index + 0] /= 2.0;
			h[index + 1] /= 2.0;
			h[index + 2] /= 2.0;

			h[index + 3]++;
		}
	}

	public void reset() {
		for (int i = 0; i < h.length; i++) {
			h[i] = 0;
		}
	}
}
