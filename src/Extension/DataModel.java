package Extension;

import java.util.LinkedList;
import java.util.Random;

import COMSETsystem.Intersection;
import COMSETsystem.LocationOnRoad;

/**
 * Interface which is used to get a route or destination in Agent class.
 *
 */
public interface DataModel {
	/**
	 * Returns a route for probable resources from {@code currentLocation} at
	 * {@code currentTime}.
	 * 
	 * @param currentLocation
	 * @param currentTime
	 * @return
	 */
	LinkedList<Intersection> getRoute(LocationOnRoad currentLocation, long currentTime);

	/**
	 * Returns a route for probable resources from {@code currentLocation} at
	 * {@code currentTime}. The route depends on {@link Random} {@code rnd}.
	 * 
	 * @param currentLocation
	 * @param currentTime
	 * @param rnd
	 * @return
	 */
	LinkedList<Intersection> getRoute(LocationOnRoad currentLocation, long currentTime, Random rnd);

	/**
	 * Returns an {@link Intersection} as a destination for probable resources from
	 * {@code currentLocation} at {@code currentTime}.
	 * 
	 * @param currentLocation
	 * @param currentTime
	 * @return
	 */
	Intersection getDestination(LocationOnRoad currentLocation, long currentTime);

	/**
	 * Returns an {@link Intersection} as a destination for probable resources from
	 * {@code currentLocation} at {@code currentTime}. The destination depends on
	 * {@link Random} {@code rnd}.
	 * 
	 * @param currentLocation
	 * @param currentTime
	 * @param rnd
	 * @return
	 */
	Intersection getDestination(LocationOnRoad currentLocation, long currentTime, Random rnd);
}
