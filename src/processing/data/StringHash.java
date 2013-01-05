package processing.data;

import java.io.PrintWriter;
import java.util.HashMap;

import processing.core.PApplet;

/**
 * A simple table class to use a String as a lookup for another String value.
 */
public class StringHash {

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
	protected String[]	                   values;

	/** Internal implementation for faster lookups */
	private final HashMap<String, Integer>	indices	= new HashMap<String, Integer>();

	public StringHash() {
		count = 0;
		keys = new String[10];
		values = new String[10];
	}

	public StringHash(final int length) {
		count = 0;
		keys = new String[length];
		values = new String[length];
	}

	public StringHash(final PApplet parent, final String filename) {
		final String[] lines = parent.loadStrings(filename);
		keys = new String[lines.length];
		values = new String[lines.length];

		// boolean csv = (lines[0].indexOf('\t') == -1);

		for (final String line : lines) {
			// String[] pieces = csv ? Table.splitLineCSV(lines[i]) : PApplet.split(lines[i], '\t');
			final String[] pieces = PApplet.split(line, '\t');
			if (pieces.length == 2) {
				// keys[count] = pieces[0];
				// values[count] = pieces[1];
				// count++;
				create(pieces[0], pieces[1]);
			}
		}
	}

	public void write(final PApplet parent, final String filename) {
		final PrintWriter writer = parent.createWriter(filename);
		final boolean csv = (filename.toLowerCase().endsWith(".csv") || filename.toLowerCase().endsWith(".csv.gz"));
		for (int i = 0; i < count; i++) {
			if (csv) {
				// String k = key(i);
				// if (k.indexOf("))
			} else {
				writer.println(key(i) + "\t" + value(i));
			}
		}
	}

	public int getCount() {
		return count;
	}

	public String key(final int index) {
		return keys[index];
	}

	public void crop() {
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

	public String value(final int index) {
		return values[index];
	}

	public String[] values() {
		crop();
		return values;
	}

	public String[] valueArray() {
		final String[] outgoing = new String[count];
		System.arraycopy(values, 0, outgoing, 0, count);
		return outgoing;
	}

	public String get(final String what) {
		final int index = keyIndex(what);
		if (index == -1) { return null; }
		return values[index];
	}

	public void set(final String key, final String val) {
		final int index = keyIndex(key);
		if (index == -1) {
			create(key, val);
		} else {
			values[index] = val;
		}
	}

	public void append(final String key, final String val) {
		final int index = keyIndex(key);
		if (index == -1) {
			create(key, val);
		} else {
			values[index] += val;
		}
	}

	public int keyIndex(final String what) {
		final Integer found = indices.get(what);
		return (found == null) ? -1 : found.intValue();
	}

	public int valueIndex(final String what) {
		for (int i = 0; i < count; i++) {
			if (values[i].equals(what)) { return i; }
		}
		return -1;
	}

	protected void create(final String k, final String v) {
		if (count == keys.length) {
			keys = PApplet.expand(keys);
			values = PApplet.expand(values);
		}
		indices.put(k, new Integer(count));
		keys[count] = k;
		values[count] = v;
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
		remove(keyIndex(which));
	}

	public void remove(final int which) {
		indices.remove(keys[which]);
		for (int i = which; i < (count - 1); i++) {
			keys[i] = keys[i + 1];
			values[i] = values[i + 1];
			indices.put(keys[i], i);
		}
		count--;
		keys[count] = null;
		values[count] = null;
	}

	public void swap(final int a, final int b) {
		final String tkey = keys[a];
		final String tvalue = values[a];
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
				if (result != 0) { return result; }
				return values[a].compareToIgnoreCase(values[b]);
			}

			@Override
			public void swap(final int a, final int b) {
				StringHash.this.swap(a, b);
			}
		};
		s.run();
	}

	/**
	 * Sort by values in descending order (largest value will be at [0]).
	 */
	public void sortValues() {
		sortValues(true, true);
	}

	public void sortValues(final boolean descending) {
		sortValues(descending, true);
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
				float diff = values[a].compareToIgnoreCase(values[b]);
				if (tiebreaker) {
					if (diff == 0) {
						diff = keys[a].compareToIgnoreCase(keys[b]);
					}
				}
				return descending ? diff : -diff;
			}

			@Override
			public void swap(final int a, final int b) {
				StringHash.this.swap(a, b);
			}
		};
		s.run();
	}
}
