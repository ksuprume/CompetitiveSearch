package Extension;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import COMSETsystem.CityMap;
import COMSETsystem.Intersection;
import COMSETsystem.LocationOnRoad;
import COMSETsystem.Road;

public abstract class HomeZoneDataModel extends AbstractDataModel {
	protected int numOfGroups;
	protected Map<Long, Integer> roadToZoneIndexMap;

	protected HomeZoneDataModel(CityMap map) {
		super(map);
		roadToZoneIndexMap = new TreeMap<Long, Integer>();
		for (int i = 0; i < zones.size(); i++) {
			List<Road> roads = zones.get(i).getRoads();
			for (Road road : roads) {
				roadToZoneIndexMap.put(road.id, i);
			}
		}
		WorldParameters params = WorldParameters.getInstance();
		numOfGroups = params.numOfGroups;
	}

	public Zone nearestHomeZone(int belongingGroupNumber, Road road) {
		int zoneIndex = -1;
		if (roadToZoneIndexMap.containsKey(road.id))
			zoneIndex = roadToZoneIndexMap.get(road.id);

		if (zoneIndex % numOfGroups == belongingGroupNumber)
			return zones.get(zoneIndex);

		// the nearest zone that accesses to the road
		Zone nearestZone = zones.get(zoneIndex);
		Intersection origin = road.from;
		Set<Intersection> nextIntersections = origin.getAdjacentTo();
		PriorityQueue<Intersection> queue = new PriorityQueue<Intersection>(new Comparator<Intersection>() {
			@Override
			public int compare(Intersection o1, Intersection o2) {
				long d1 = map.travelTimeBetween(o1, origin);
				long d2 = map.travelTimeBetween(o2, origin);
				return Long.compare(d1, d2);
			}
		});
		queue.addAll(nextIntersections);
		Map<Intersection, Long> precedent = new TreeMap<Intersection, Long>();
		for (Intersection item : nextIntersections) {
			precedent.put(item, item.roadTo(origin).id);
		}
		Intersection currentIntersection;
		while ((currentIntersection = queue.poll()) != null) {
			// explorer
			Long roadId = precedent.get(currentIntersection);
			zoneIndex = roadToZoneIndexMap.get(roadId);
			nearestZone = zones.get(zoneIndex);
			if (zoneIndex % numOfGroups == belongingGroupNumber) {
				// we found the road which belongs to the nearest zone
				break;
			}
			// add
			nextIntersections = currentIntersection.getAdjacentTo();
			for (Intersection item : nextIntersections) {
				if (precedent.containsKey(item)) {
					// the intersection was visited already
					// therefore, we skip it
					continue;
				}
				precedent.put(item, item.roadTo(currentIntersection).id);
				queue.add(item);
			}
		}

		return nearestZone;
	}

	protected Road getTargetRoad(LocationOnRoad currentLocation, long currentTime, Random rnd) {
		Road road = super.getTargetRoad(currentLocation, currentTime, rnd);
		int belongingGroupNumber = rnd.hashCode() % numOfGroups;
		Zone homeZone = nearestHomeZone(belongingGroupNumber, road);
		List<Road> roads = homeZone.getRoads();
		int index = rnd.nextInt(roads.size());
		return roads.get(index);
	}
}
