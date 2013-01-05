package main;

import main.fractalGenome.FractalGenome;
import main.fractalThread.FractalThread;
import main.fractalThread.ThreadSignal;
import processing.core.PApplet;

public class Main extends PApplet {
	static boolean	fullscreen	= true;
	int	           swid	       = 1920;
	int	           shei	       = 1080;
	int	           ss	       = 2;
	int	           hwid	       = swid * ss;
	int	           hhei	       = shei * ss;
	int	           fr	       = 60;
	int	           bg	       = 0xFF000000;
	int	           fg	       = 0xFFFFFFFF;
	Histogram	   h;
	FractalGenome	genome;

	double	       seed;

	public static final void main(final String args[]) {
		if (fullscreen) {
			PApplet.main(new String[] { "--present", "main.Main" });
		} else {
			PApplet.main(new String[] { "main.Main" });
		}
	}

	@Override
	public void setup() {
		size(swid, shei);
		frameRate(fr);
		background(bg);
		fill(fg);
		stroke(fg);

		h = new Histogram(swid, shei, ss);
		h.setLogScale();
		h.reset();

		seed = random(0, 1);
		genome = new FractalGenome(3, 4);
		(new FractalThread(genome, new ThreadSignal(), h)).start();
		(new FractalThread(genome, new ThreadSignal(), h)).start();
		(new FractalThread(genome, new ThreadSignal(), h)).start();
		(new FractalThread(genome, new ThreadSignal(), h)).start();
		(new FractalThread(genome, new ThreadSignal(), h)).start();
		(new FractalThread(genome, new ThreadSignal(), h)).start();
	}

	@Override
	public void draw() {
		if (frameCount == 1) {
			loadPixels();
		} else {
			h.updatePixels(pixels);
			updatePixels();
		}
	}
}
