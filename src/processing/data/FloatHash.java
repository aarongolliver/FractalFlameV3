package processing.data;

import java.io.PrintWriter;
import java.util.HashMap;

import processing.core.PApplet;

/**
 * A simple table class to use a String as a lookup for an float value.
 */
public class FloatHash {

	/** Number of elements in the table */
	public int	                           count;

	/**
	 * List of keys, available for sake of speed, but should be manipulated (consider it read-only).
	 */
	protected String[]	                   keys;

	/**
	 * List of values, available for sake of speed, but should be manipulated (consider it
	 * read-only).
	 */
	protected float[]	                   values;

	/** Internal implementation for faster lookups */
	private final HashMap<String, Integer>	indices	= new HashMap<String, Integer>();

	public FloatHash() {
		count = 0;
		keys = new String[10];
		values = new float[10];
	}

	public FloatHash(final int length) {
		count = 0;
		keys = new String[length];
		values = new float[length];
	}

	public FloatHash(final PApplet parent, final String filename) {
		final String[] lines = parent.loadStrings(filename);
		keys = new String[lines.length];
		values = new float[lines.length];

		// boolean csv = (lines[0].indexOf('\t') == -1);

		for (final String line : lines) {
			// String[] pieces = csv ? Table.splitLineCSV(lines[i]) :
			// PApplet.split(lines[i], '\t');
			final String[] pieces = PApplet.split(line, '\t');
			if (pieces.length == 2) {
				keys[count] = pieces[0];
				values[count] = PApplet.parseFloat(pieces[1]);
				count++;
			}
		}
	}

	public int getCount() {
		return count;
	}

	public String key(final int index) {
		return keys[index];
	}

	protected void crop() {
		if (count != keys.length) {
			keys = PApplet.subset(keys, 0, count);
			values = (float[]) PApplet.subset(values, 0, count);
		}
	}

	/**
	 * Return the internal array being used to store the keys. Allocated but unused entries will be
	 * removed. This array should not be modified.
	 */
	public String[] keys() {
		crop();
		return keys;
	}

	/**
	 * Return a copy of the internal keys array. This array can be modified.
	 */
	public String[] keyArray() {
		return this.keyArray(null);
	}

	public String[] keyArray(String[] outgoing) {
		if ((outgoing == null) || (outgoing.length != count)) {
			outgoing = new String[count];
		}
		System.arraycopy(keys, 0, outgoing, 0, count);
		return outgoing;
	}

	public float value(final int index) {
		return values[index];
	}

	public float[] values() {
		crop();
		return values;
	}

	public int[] valueArray() {
		final int[] outgoing = new int[count];
		System.arraycopy(values, 0, outgoing, 0, count);
		return outgoing;
	}

	public float get(final String what) {
		final int index = index(what);
		if (index == -1) {
			return 0;
		}
		return values[index];
	}

	public void set(final String who, final int amount) {
		final int index = index(who);
		if (index == -1) {
			create(who, amount);
		} else {
			values[index] = amount;
		}
	}

	public void add(final String who, final int amount) {
		final int index = index(who);
		if (index == -1) {
			create(who, amount);
		} else {
			values[index] += amount;
		}
	}

	public void increment(final String who) {
		final int index = index(who);
		if (index == -1) {
			create(who, 1);
		} else {
			values[index]++;
		}
	}

	public int index(final String what) {
		final Integer found = indices.get(what);
		return (found == null) ? -1 : found.intValue();
	}

	protected void create(final String what, final int much) {
		if (count == keys.length) {
			keys = PApplet.expand(keys);
			// String ktemp[] = new String[count << 1];
			// System.arraycopy(keys, 0, ktemp, 0, count);
			// keys = ktemp;
			values = (float[]) PApplet.expand(values);
			// float vtemp[] = new float[count << 1];
			// System.arraycopy(values, 0, vtemp, 0, count);
			// values = vtemp;
		}
		indices.put(what, new Integer(count));
		keys[count] = what;
		values[count] = much;
		count++;
	}

	public void print() {
		write(new PrintWriter(System.out));
	}

	public void write(final PrintWriter writer) {
		for (int i = 0; i < count; i++) {
			writer.println(keys[i] + "\t" + values[i]);
		}
		writer.flush();
	}

	public void remove(final String which) {
		removeIndex(index(which));
	}

	public void removeIndex(final int which) {
		// System.out.println("index is " + which + " and " + keys[which]);
		indices.remove(keys[which]);
		for (int i = which; i < (count - 1); i++) {
			keys[i] = keys[i + 1];
			values[i] = values[i + 1];
			indices.put(keys[i], i);
		}
		count--;
		keys[count] = null;
		values[count] = 0;
	}

	public void swap(final int a, final int b) {
		final String tkey = keys[a];
		final float tvalue = values[a];
		keys[a] = keys[b];
		values[a] = values[b];
		keys[b] = tkey;
		values[b] = tvalue;

		indices.put(keys[a], new Integer(a));
		indices.put(keys[b], new Integer(b));
	}

	public void sortKeys() {
		final Sort s = new Sort() {
			@Override
			public int size() {
				return count;
			}

			@Override
			public float compare(final int a, final int b) {
				final int result = keys[a].compareToIgnoreCase(keys[b]);
				if (result != 0) {
					return result;
				}
				return values[b] - values[a];
			}

			@Override
			public void swap(final int a, final int b) {
				FloatHash.this.swap(a, b);
			}
		};
		s.run();
	}

	/**
	 * Sort by values in descending order (largest value will be at [0]).
	 */
	public void sortValues() {
		this.sortValues(true, true);
	}

	public void sortValues(final boolean descending) {
		this.sortValues(descending, true);
	}

	// ascending puts the largest value at the end
	// descending puts the largest value at 0
	public void sortValues(final boolean descending, final boolean tiebreaker) {
		final Sort s = new Sort() {
			@Override
			public int size() {
				return count;
			}

			@Override
			public float compare(final int a, final int b) {
				float diff = values[b] - values[a];
				if (tiebreaker) {
					if (diff == 0) {
						diff = keys[a].compareToIgnoreCase(keys[b]);
					}
				}
				return descending ? diff : -diff;
			}

			@Override
			public void swap(final int a, final int b) {
				FloatHash.this.swap(a, b);
			}
		};
		s.run();
	}
}
