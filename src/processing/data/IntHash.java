package processing.data;

import java.io.PrintWriter;
import java.util.HashMap;

import processing.core.PApplet;

/**
 * A simple table class to use a String as a lookup for an int value.
 */
public class IntHash {

	/** Number of elements in the table */
	protected int	                       count;

	/**
	 * List of keys, available for sake of speed, but should be manipulated (consider it read-only).
	 */
	protected String[]	                   keys;

	/**
	 * List of values, available for sake of speed, but should be manipulated (consider it
	 * read-only).
	 */
	protected int[]	                       values;

	/** Internal implementation for faster lookups */
	private final HashMap<String, Integer>	indices	= new HashMap<String, Integer>();

	public IntHash() {
		count = 0;
		keys = new String[10];
		values = new int[10];
	}

	public IntHash(final int length) {
		count = 0;
		keys = new String[length];
		values = new int[length];
	}

	public IntHash(final String[] k, final int[] v) {
		count = Math.min(k.length, v.length);
		keys = new String[count];
		values = new int[count];
		System.arraycopy(k, 0, keys, 0, count);
		System.arraycopy(v, 0, values, 0, count);
	}

	static public IntHash fromTally(final String[] list) {
		final IntHash outgoing = new IntHash();
		for (final String s : list) {
			outgoing.increment(s);
		}
		outgoing.crop();
		return outgoing;
	}

	static public IntHash fromOrder(final String[] list) {
		final IntHash outgoing = new IntHash();
		for (int i = 0; i < list.length; i++) {
			outgoing.set(list[i], i);
		}
		return outgoing;
	}

	public IntHash(final PApplet parent, final String filename) {
		final String[] lines = parent.loadStrings(filename);
		keys = new String[lines.length];
		values = new int[lines.length];

		// boolean csv = (lines[0].indexOf('\t') == -1);
		for (final String line : lines) {
			// if (lines[i].trim().length() != 0) {
			// String[] pieces = csv ? Table.splitLineCSV(lines[i]) :
			// PApplet.split(lines[i], '\t');
			final String[] pieces = PApplet.split(line, '\t');
			if (pieces.length == 2) {
				keys[count] = pieces[0];
				values[count] = PApplet.parseInt(pieces[1]);
				count++;
			}
		}
	}

	public int size() {
		return count;
	}

	public String key(final int index) {
		return keys[index];
	}

	protected void crop() {
		if (count != keys.length) {
			keys = PApplet.subset(keys, 0, count);
			values = PApplet.subset(values, 0, count);
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
		final String[] outgoing = new String[count];
		System.arraycopy(keys, 0, outgoing, 0, count);
		return outgoing;
	}

	public int value(final int index) {
		return values[index];
	}

	public int[] values() {
		crop();
		return values;
	}

	public int[] valueArray() {
		final int[] outgoing = new int[count];
		System.arraycopy(values, 0, outgoing, 0, count);
		return outgoing;
	}

	public int get(final String what) {
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
			final String ktemp[] = new String[count << 1];
			System.arraycopy(keys, 0, ktemp, 0, count);
			keys = ktemp;
			final int vtemp[] = new int[count << 1];
			System.arraycopy(values, 0, vtemp, 0, count);
			values = vtemp;
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

	protected void swap(final int a, final int b) {
		final String tkey = keys[a];
		final int tvalue = values[a];
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
				IntHash.this.swap(a, b);
			}
		};
		s.run();
	}

	/**
	 * Sort by values in descending order (largest value will be at [0]).
	 */
	public void sortValues() {
		this.sortValues(true);
	}

	/**
	 * Sort by values. Identical values will use the keys as tie-breaker.
	 * 
	 * @param descending
	 *            true to put the largest value at position 0.
	 */
	public void sortValues(final boolean descending) {
		final Sort s = new Sort() {
			@Override
			public int size() {
				return count;
			}

			@Override
			public float compare(final int a, final int b) {
				int diff = values[b] - values[a];
				if (diff == 0) {
					diff = keys[a].compareToIgnoreCase(keys[b]);
				}
				return descending ? diff : -diff;
			}

			@Override
			public void swap(final int a, final int b) {
				IntHash.this.swap(a, b);
			}
		};
		s.run();
	}
}