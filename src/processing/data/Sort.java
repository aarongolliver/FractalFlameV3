package processing.data;

public abstract class Sort implements Runnable {

	public Sort() {
	}

	public void run() {
		final int c = size();
		if (c > 1) {
			sort(0, c - 1);
		}
	}

	protected void sort(final int i, final int j) {
		final int pivotIndex = (i + j) / 2;
		swap(pivotIndex, j);
		final int k = partition(i - 1, j);
		swap(k, j);
		if ((k - i) > 1) {
			sort(i, k - 1);
		}
		if ((j - k) > 1) {
			sort(k + 1, j);
		}
	}

	protected int partition(int left, int right) {
		final int pivot = right;
		do {
			while (compare(++left, pivot) < 0) {
				;
			}
			while ((right != 0) && (compare(--right, pivot) > 0)) {
				;
			}
			swap(left, right);
		} while (left < right);
		swap(left, right);
		return left;
	}

	abstract public int size();

	abstract public float compare(int a, int b);

	abstract public void swap(int a, int b);
}