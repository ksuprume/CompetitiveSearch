package Extension;

import java.util.List;

import COMSETsystem.Road;

/**
 * A zone is a spatial unit that contains roads.
 *
 */
public interface Zone extends Model {
	/**
	 * Returns all roads in this zone
	 * 
	 * @return
	 */
	List<Road> getRoads();

	/**
	 * Checks if this zone contains a road
	 * 
	 * @param road
	 * @return
	 */
	boolean contains(Road road);
}
