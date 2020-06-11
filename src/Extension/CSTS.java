package Extension;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import COMSETsystem.BaseAgent;
import COMSETsystem.CityMap;
import COMSETsystem.Intersection;
import COMSETsystem.LocationOnRoad;

/**
 * A CSTS relies on a data model, being capable of re-routing during a trip.
 *
 */
public class CSTS extends BaseAgent {
	// All CSTS have the same data model
	static DataModel model;
	static long threshold;
	static WorldParameters params;
	Random random;
	LinkedList<Intersection> route = new LinkedList<>();
	private long rerouteAt = 0;

	public CSTS(long id, CityMap map) {
		super(id, map);
		random = new Random(id);
		if (model == null) {
			params = WorldParameters.getInstance();
			try {
				Class<?> modelClass = Class.forName(params.dataModel);
				@SuppressWarnings("unchecked")
				Constructor<? extends DataModel> cons = (Constructor<? extends DataModel>) modelClass.getConstructor(CityMap.class);
				model = cons.newInstance(map);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setThreshold();
		}
	}

	/**
	 * This function was designed to compute threshold for re-routing.
	 * Depending on a data model, re-routing can be useful.
	 *  
	 */
	private void setThreshold() {
		Map<Long, Intersection> table = map.intersections();
		long max = Long.MIN_VALUE;
		long tmp;
		for (Intersection inter1 : table.values()) {
			for (Intersection inter2 : table.values()) {
				if (inter1 == inter2) {
					continue;
				}
				tmp = map.travelTimeBetween(inter1, inter2);
				if (tmp > max)
					max = tmp;
			}
		}
		threshold = (long) (max * params.reroutingTime);
	}

	@Override
	public void planSearchRoute(LocationOnRoad currentLocation, long currentTime) {
		route.clear();
		route = model.getRoute(currentLocation, currentTime, random);
		if (route.get(0) == currentLocation.road.to) {
			route.poll(); // Ensure that route.get(0) != currentLocation.road.to.
		}
		long travelTime = map.travelTimeBetween(currentLocation.road.to, route.getLast());
		rerouteAt = currentTime + threshold;
		if (travelTime > threshold && params.rerouting) {
			// Re-routing is not efficient than I expected.
			 rerouteAt = currentTime + threshold;
			 rerouteAt = currentTime + (long) (travelTime * params.reroutingTime);
		}
	}

	@Override
	public Intersection nextIntersection(LocationOnRoad currentLocation, long currentTime) {
		if (route.size() > 0 && currentTime < rerouteAt) {
			// Route is not empty, take the next intersection.
			Intersection nextIntersection = route.poll();
			return nextIntersection;
		} else {
			// Finished the planned route. Plan a new route.
			planSearchRoute(currentLocation, currentTime);
			return route.poll();
		}
	}

	@Override
	public void assignedTo(LocationOnRoad currentLocation, long currentTime, long resourceId,
			LocationOnRoad resourcePickupLocation, LocationOnRoad resourceDropoffLocation) {
		route.clear();
	}
}
