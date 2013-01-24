package fractalFlameV3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;

import com.google.gson.GsonBuilder;

import fractalFlameV3.fractalGenome.FractalGenome;
import fractalFlameV3.fractalThread.FractalThread;
import fractalFlameV3.fractalThread.ThreadSignal;

public class Main extends PApplet {
	static boolean	         fullscreen	        = false;

	int	                     swid	            = 1920;
	int	                     shei	            = 1080;
	int	                     ss	                = 1;
	int	                     SS_MAX	            = 12;

	int	                     fr	                = 60;

	Histogram	             h;
	ArrayList<FractalGenome>	genomeList;
	int	                     genomeListPosition	= 0;
	FractalGenome	         currentGenome;
	FractalThread[]	         threads;
	ThreadSignal	         threadSignal;

	final int	             SYSTEM_THREADS	    = Runtime.getRuntime().availableProcessors();

	// by running (SYSTEM_THREADS - 2) threads we can hopefully avoid making the system unusalbe
	// while the flame is generating. There is of course a tradeoff in generation speed, which
	// doesn't really matter to me as I have a 12 thread system.
	final int	             maxFlameThreads	= (SYSTEM_THREADS > 3) ? SYSTEM_THREADS - 2 : 1;

	public static final void main(final String args[]) {
		if (Main.fullscreen) {
			PApplet.main(new String[] { "--present", "fractalFlameV3.Main" });
		} else {
			PApplet.main(new String[] { "fractalFlameV3.Main" });
		}
	}

	@Override
	public void setup() {
		swid = (Main.fullscreen) ? displayWidth : swid;
		shei = (Main.fullscreen) ? displayHeight : shei;
		this.size(swid, shei);
		frameRate(fr);

		h = newHistogram();
		currentGenome = loadLastGenome();
		genomeList = new ArrayList<FractalGenome>();
		genomeList.add(currentGenome);
		threads = new FractalThread[1];
		threadSignal = new ThreadSignal();
		startThreads();

	}

	private FractalGenome loadLastGenome() {
		String fullGenomeString = "";
		final GsonBuilder gb = new GsonBuilder();
		try {
			final FileReader fileReader = new FileReader("images/last.fractalgenome");
			final BufferedReader lastGenomeReader = new BufferedReader(fileReader);

			String line;
			while ((line = lastGenomeReader.readLine()) != null) {
				fullGenomeString += line;
			}

			lastGenomeReader.close();
			fileReader.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return gb.create().fromJson(fullGenomeString, new FractalGenome(3, 3).getClass());
	}

	private FractalGenome newGenome() {
		genomeList.remove(genomeListPosition);
		genomeList.add(genomeListPosition, currentGenome);
		final FractalGenome fg = new FractalGenome(3, 10);
		fg.variationToggle = true;
		fg.finalTransformToggle = true;
		currentGenome.center = true;
		genomeList.add(fg);
		genomeListPosition = genomeList.size() - 1;
		return fg;
	}

	private Histogram newHistogram() {
		final Histogram h = new Histogram(swid, shei, ss);
		return h;
	}

	@Override
	public void keyPressed() {
		stopThreads();
		switch (keyCode) {
		case 'h':
		case 'H':
			ss = (ss == 1) ? SS_MAX : 1;
			h = null;
			System.gc();
			h = newHistogram();
			System.out.println("# SS\t|\t " + ss);
			break;

		case 'r':
		case 'R':
			currentGenome = newGenome();
			h.reset();
			break;

		case 't':
		case 'T':
			threads = new FractalThread[(threads.length == 8) ? 1 : 8];
			System.out.println("# TH\t|\t " + threads.length);
			break;

		case 'f':
		case 'F':
			currentGenome.finalTransformToggle = !currentGenome.finalTransformToggle;
			System.out.println("# FT\t|\t " + currentGenome.finalTransformToggle);
			h.reset();
			break;

		case 'v':
		case 'V':
			currentGenome.variationToggle = !currentGenome.variationToggle;
			System.out.println("# VT\t|\t " + currentGenome.variationToggle);
			h.reset();
			break;

		case 's':
		case 'S':
			final String fileName = "images/" + currentGenome.hashCode() + ".bmp";
			saveFrame(fileName);
			currentGenome.saveGsonRepresentation();
			break;

		case 'c':
		case 'C':
			currentGenome.resetColors();
			h.reset();
			break;

		case '+':
		case '=':
			currentGenome.cameraXShrink /= 1.01;
			currentGenome.cameraYShrink /= 1.01;
			h.reset();
			break;

		case '-':
		case '_':
			currentGenome.cameraXShrink *= 1.01;
			currentGenome.cameraYShrink *= 1.01;
			h.reset();
			break;
		case '>':
		case '.':
			if (genomeListPosition < (genomeList.size() - 1)) {
				genomeListPosition++;
				currentGenome = genomeList.get(genomeListPosition);
				h.reset();
			}
			break;
		case '<':
		case ',':
			if (genomeListPosition > 0) {
				genomeListPosition--;
				currentGenome = genomeList.get(genomeListPosition);
				h.reset();
			}
			break;

		case PConstants.UP:
			currentGenome.cameraYOffset += .01 * currentGenome.cameraYShrink;
			h.reset();
			break;

		case PConstants.DOWN:
			currentGenome.cameraYOffset -= .01 * currentGenome.cameraYShrink;
			h.reset();
			break;

		case PConstants.LEFT:
			currentGenome.cameraXOffset += .01 * currentGenome.cameraXShrink;
			h.reset();
			break;

		case PConstants.RIGHT:
			currentGenome.cameraXOffset -= .01 * currentGenome.cameraXShrink;
			h.reset();
			break;
		}
		startThreads();
	}

	private void stopThreads() {
		threadSignal.running = false;
		for (final Thread t : threads) {
			try {
				t.join();
			} catch (final InterruptedException e) {
				System.out.println(e.getLocalizedMessage());
			}
		}
	}

	private void startThreads() {
		threadSignal.running = true;
		for (final int i : Utils.range(threads.length)) {
			threads[i] = new FractalThread(currentGenome, threadSignal, h);
		}
		for (final Thread t : threads) {
			t.start();
		}
	}

	@Override
	public void draw() {
		if (frameCount == 1) {
			loadPixels();
		} else {
			h.updatePixels(pixels, currentGenome);
			this.updatePixels();
		}
		if ((frameCount % 10) == 0) {
			// System.out.println("#FPS: " + frameRate);
		}
	}
}
