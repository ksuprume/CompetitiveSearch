package Extension;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Non-negative matrix X constructed by production of two non-negative matrices
 * (W * H)
 * 
 * X = W * H where W is r by k, H is l by r
 * 
 *   [W00 ... W0r]
 * W=[... ... ...]
 *   [Wk0 ... Wkr]
 * 
 *   [H00 ... H0l]
 * H=[... ... ...]
 *   [Hr0 ... Hrl]
 *
 */
public class FactorizedMatrix extends AbstractMatrix {
	// w[id][r]
	double[][] w;
	// h[r][time]
	double[][] h;

	public FactorizedMatrix(ZonedDateTime start, ZonedDateTime end, int width, List<Long> zoneIds) {
		super(start, end, width, zoneIds);
	}
	
	/**
	 * Sets Non-negative matrices. The number of columns of Matrix {@code w} should
	 * be equal to the number of rows of Matrix {@code h}.
	 * 
	 * @param w
	 * @param h
	 */
	public void setWH(double[][] w, double[][] h) {
		if (h.length != w[0].length) {
			throw new RuntimeException("The number of columns of w should be equal to the number of rows of h.");
		}
		this.w = w;
		this.h = h;
	}

	/**
	 * Get a cell of matrix at {@code column} and {@code row}. 
	 * 
	 * @param column
	 * @param row
	 * @return
	 */
	public double get(int column, int row) {
		double sum = 0.0;
		final int f = h.length;
		for (int i = 0; i < f; i++) {
			sum += w[row][i] * h[i][column];
		}
		return sum;
	}

	
}
