package Extension;


/**
 * Matrix interface
 *
 */
public interface Matrix {
	/**
	 * Get a cell of matrix at {@code column} and {@code row}. 
	 * 
	 * @param column
	 * @param row
	 * @return
	 */
	public double get(int column, int row);

	/**
	 * Find a row corresponding {@code zoneId}.
	 * 
	 * @param zoneId
	 * @return
	 */
	public int findRow(long zoneId) ;

	/**
	 * Find a column corresponding time {@code epochSecond}.
	 * 
	 * @param epochSecond
	 * @return
	 */
	public int findColumn(long epochSecond) ;

	/**
	 * Return the height of matrix  
	 * @return
	 */
	public int getHeight();

	/**
	 * Return the width of matrix  
	 * @return
	 */
	public int getWidth();
}
