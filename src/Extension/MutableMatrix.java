package Extension;

/**
 * Mutable matrix interface
 * 
 *
 */
public interface MutableMatrix extends Matrix {
	/**
	 * Set a cell of matrix at {@code column} and {@code row}. 
	 * 
	 * @param column
	 * @param row
	 * @param value
	 */
	public void set(int column, int row, double value);
	
	public void increment(int column, int row);
}
