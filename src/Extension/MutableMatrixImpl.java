package Extension;

import java.time.ZonedDateTime;
import java.util.List;

public class MutableMatrixImpl extends AbstractMatrix implements MutableMatrix {
	// [id][time]
	double[][] mat;

	public MutableMatrixImpl(ZonedDateTime start, ZonedDateTime end, int numOfTemporalSlotsPerDay, List<Long> zoneIds) {
		super(start, end, numOfTemporalSlotsPerDay, zoneIds);
		
		mat = new double[getHeight()][getWidth()];
	}

	@Override
	public double get(int column, int row) {
		return mat[row][column];
	}

	@Override
	public void set(int column, int row, double value) {
		mat[row][column] = value;
	}

	@Override
	public void increment(int column, int row) {
		mat[row][column]++;
	}
}
