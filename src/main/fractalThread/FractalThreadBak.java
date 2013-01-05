package main.fractalThread;
import java.util.concurrent.ThreadLocalRandom;

import main.ColorSet;
import main.Vec2D;

public final class FractalThreadBak extends Thread {

	final ThreadLocalRandom	r	= ThreadLocalRandom.current();

	private Vec2D	        p	= new Vec2D(r.nextDouble(-1, 1), r.nextDouble(-1, 1));
	private final ColorSet	col	= new ColorSet(0);

	private final Variation[] variations;
	
	@Override
    public final void run() {
		System.out.println("Creating thread");

		while (true) {
			if (GLB.threadStopSignal == true) { return; }

			int j = r.nextInt(0, GLB.aProbability.length);
			j = GLB.aProbability[j];

			final Vec2D pAffined = affine(p, GLB.a[j]);

			if (GLB.enableVariations) {
				Vec2D vSum = new Vec2D(0, 0);
				vSum = Vec2D.add(Vec2D.mul(v0(pAffined), GLB.vWeight[0]), vSum);
				vSum = Vec2D.add(Vec2D.mul(v1(pAffined), GLB.vWeight[1]), vSum);
				// vSum = Vec2D.add(Vec2D.mul(v2(pAffined), GLB.vWeight[2]),
				// vSum);
				// vSum = Vec2D.add(Vec2D.mul(v3(pAffined), GLB.vWeight[3]),
				// vSum);
				vSum = Vec2D.add(Vec2D.mul(v4(pAffined), GLB.vWeight[4]), vSum);
				// vSum = Vec2D.add(Vec2D.mul(v5(pAffined), GLB.vWeight[5]),
				// vSum);
				// vSum = Vec2D.add(Vec2D.mul(v6(pAffined), GLB.vWeight[6]),
				// vSum);
				vSum = Vec2D.add(Vec2D.mul(v7(pAffined), GLB.vWeight[7]), vSum);
				// vSum = Vec2D.add(Vec2D.mul(v8(pAffined), GLB.vWeight[8]),
				// vSum);
				// vSum = Vec2D.add(Vec2D.mul(v9(pAffined), GLB.vWeight[9]),
				// vSum);
				// vSum = Vec2D.add(Vec2D.mul(v10(pAffined), GLB.vWeight[10]),
				// vSum);
				// vSum = Vec2D.add(Vec2D.mul(v11(pAffined), GLB.vWeight[11]),
				// vSum);
				// vSum = Vec2D.add(Vec2D.mul(v12(pAffined), GLB.vWeight[12]),
				// vSum);
				vSum = Vec2D.add(Vec2D.mul(v13(pAffined), GLB.vWeight[13]), vSum);
				// vSum = Vec2D.add(Vec2D.mul(v16(pAffined),
				// GLB.vWeight[16]),FvSum);

				p = new Vec2D(vSum);
			} else {
				p = new Vec2D(pAffined);
			}

			col.hit(GLB.affineColor[j]);
			if (!(Double.isInfinite(p.x) || Double.isInfinite(p.y) || Double.isNaN(p.x) || Double.isNaN(p.y))) {
				GLB.h.hit(p, col);
			} else {
				p = new Vec2D(r.nextDouble(-1, 1), r.nextDouble(-1, 1));
			}
		}
	}

	private final Vec2D affine(final Vec2D p, final double[][] a) {

		final double x = (p.x * a[0][0]) + (p.y * a[0][1]) + (a[0][2]);
		final double y = (p.x * a[1][0]) + (p.y * a[1][1]) + (a[1][2]);

		return new Vec2D(x, y);
	}

	private final Vec2D v0(final Vec2D p) {
		final double x = -p.x;
		final double y = -p.y;
		return new Vec2D(x, y);
	}

	private final Vec2D v1(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		return new Vec2D(Math.cos(x), Math.sin(y));
	}

	private final Vec2D v2(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		return new Vec2D(y / rsq, x / rsq);
	}

	private final Vec2D v3(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		return new Vec2D((x * Math.sin(rsq)) - (y * Math.cos(rsq)), (x * Math.cos(rsq)) + (y * Math.sin(rsq)));
	}

	private final Vec2D v4(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		return new Vec2D((1 / r) * (x - y) * (x + y), (1 / r) * 2 * x * y);
	}

	private final Vec2D v5(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		return new Vec2D(theta / Math.PI, r - 1);
	}

	private final Vec2D v6(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		return new Vec2D(r * Math.sin(theta + r), r * Math.cos(theta - r));
	}

	private final Vec2D v7(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		return new Vec2D(r * Math.sin(theta * r), r * (-1) * Math.cos(theta * r));
	}

	private final Vec2D v8(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		return new Vec2D((theta / Math.PI) * Math.sin(Math.PI * r), (theta / Math.PI) * Math.cos(Math.PI * r));
	}

	private final Vec2D v9(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		return new Vec2D((1 / r) * (Math.cos(theta) + Math.sin(r)), (1 / r) * (Math.sin(theta) - Math.cos(r)));
	}

	private final Vec2D v10(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		return new Vec2D(Math.sin(theta) / r, r * Math.cos(theta));
	}

	private final Vec2D v11(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		return new Vec2D(Math.sin(theta) * Math.cos(r), Math.cos(theta) * Math.sin(r));
	}

	private final Vec2D v12(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		final double p0 = Math.sin(theta + r);
		final double p1 = Math.cos(theta - r);
		return new Vec2D(r * (Math.pow(p0, 3) + Math.pow(p1, 3)), r * (Math.pow(p0, 3) - Math.pow(p1, 3)));
	}

	private final Vec2D v13(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		final double omega = (this.r.nextDouble() >= .5) ? 0 : Math.PI;

		return new Vec2D(Math.sqrt(r) * Math.cos((theta / 2) + omega), Math.sqrt(r) * Math.sin((theta / 2) + omega));
	}

	private final Vec2D v16(final Vec2D p) {
		final double x = p.x;
		final double y = p.y;
		final double rsq = ((x * x) + (y * y));
		final double r = Math.sqrt(rsq);
		final double theta = Math.atan(x / y);
		final double omega = (this.r.nextDouble() >= .5) ? 0 : Math.PI;

		return new Vec2D((2 / (r + 1)) * y, (2 / (r + 1)) * x);
	}

}

