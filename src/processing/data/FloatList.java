package processing.data;

import processing.core.PApplet;

public class FloatList {
	int	    count;
	float[]	data;

	public FloatList() {
		data = new float[10];
	}

	public FloatList(final float[] list) {
		count = list.length;
		data = new float[count];
		System.arraycopy(list, 0, data, 0, count);
	}

	public FloatList(final String[] list) {
		this(PApplet.parseFloat(list));
	}

	public int size() {
		return count;
	}

	public float get(final int index) {
		return data[index];
	}

	public void set(final int index, final int what) {
		if (index >= count) {
			data = (float[]) PApplet.expand(data, index + 1);
		}
		data[index] = what;
	}

	public void append(final int value) {
		if (count == data.length) {
			data = (float[]) PApplet.expand(data);
		}
		data[count++] = value;
	}

	public void clear() {
		count = 0;
	}

	public float calcMin() {
		if (data.length == 0) {
			return Float.NaN;
		}
		float m = Float.NaN;
		for (int i = 0; i < data.length; i++) {
			// find one good value to start
			if (data[i] == data[i]) {
				m = data[i];

				// calculate the rest
				for (int j = i + 1; j < data.length; j++) {
					final float d = data[j];
					if (!Float.isNaN(d) && (d < m)) {
						m = data[j];
					}
				}
				break;
			}
		}
		return m;
	}

	public float calcMax() {
		if (data.length == 0) {
			return Float.NaN;
		}
		float m = Float.NaN;
		for (int i = 0; i < data.length; i++) {
			// find one good value to start
			if (data[i] == data[i]) {
				m = data[i];

				// calculate the rest
				for (int j = i + 1; j < data.length; j++) {
					final float d = data[j];
					if (!Float.isNaN(d) && (d > m)) {
						m = data[j];
					}
				}
				break;
			}
		}
		return m;
	}

	public void removeNaN() {
		int index = 0;
		for (int i = 0; i < count; i++) {
			if (data[i] == data[i]) {
				if (i != index) {
					data[index] = data[i];
				}
				index++;
			}
		}
		count = index;
	}

	public void replaceNaN(final float replacement) {
		for (int i = 0; i < count; i++) {
			if (data[i] != data[i]) {
				data[i] = replacement;
			}
		}
	}

	public void add(final float amt) {
		for (int i = 0; i < count; i++) {
			data[i] += amt;
		}
	}

	public void sub(final float amt) {
		for (int i = 0; i < count; i++) {
			data[i] -= amt;
		}
	}

	public void mul(final float amt) {
		for (int i = 0; i < count; i++) {
			data[i] *= amt;
		}
	}

	public void div(final float amt) {
		for (int i = 0; i < count; i++) {
			data[i] /= amt;
		}
	}

	/*
	 * static public void shuffle(int[] array) { java.util.Random rng = new java.util.Random(); int
	 * n = array.length; while (n > 1) { int k = rng.nextInt(n); n--; int temp = array[n]; array[n]
	 * = array[k]; array[k] = temp; } }
	 */

	public void crop() {
		if (count != data.length) {
			data = (float[]) PApplet.subset(data, 0, count);
		}
	}

	public float[] values() {
		crop();
		return data;
	}

	public int[] toIntArray() {
		final int[] outgoing = new int[count];
		for (int i = 0; i < count; i++) {
			outgoing[i] = (int) data[i];
		}
		return outgoing;
	}

	public long[] toLongArray() {
		final long[] outgoing = new long[count];
		for (int i = 0; i < count; i++) {
			outgoing[i] = (long) data[i];
		}
		return outgoing;
	}

	public float[] toFloatArray() {
		final float[] outgoing = new float[count];
		System.arraycopy(data, 0, outgoing, 0, count);
		return outgoing;
	}

	public double[] toDoubleArray() {
		final double[] outgoing = new double[count];
		for (int i = 0; i < count; i++) {
			outgoing[i] = data[i];
		}
		return outgoing;
	}

	public String[] toStringArray() {
		final String[] outgoing = new String[count];
		for (int i = 0; i < count; i++) {
			outgoing[i] = String.valueOf(data[i]);
		}
		return outgoing;
	}

	/**
	 * Create a new array with a copy of all the values.
	 * 
	 * @return an array sized by the length of the list with each of the values.
	 */
	// public float[] toArray() {
	// return toFloatArray();
	// }

	/**
	 * Copy as many values as possible into the specified array.
	 * 
	 * @param array
	 */
	// public void toArray(float[] array) {
	// System.arraycopy(data, 0, array, 0, Math.min(count, array.length));
	// }
}