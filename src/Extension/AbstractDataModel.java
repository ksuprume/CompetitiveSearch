package Extension;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import COMSETsystem.CityMap;
import COMSETsystem.Intersection;
import COMSETsystem.LocationOnRoad;
import COMSETsystem.Road;

/**
 * AbstractDataModel for commonly used methods
 *
 */
public abstract class AbstractDataModel implements DataModel {
	/**
	 * All zones that a data model contains.
	 */
	protected List<Zone> zones;
	/**
	 * {@link CityMap} that can be used as a reference
	 */
	protected CityMap map;
	/**
	 * Normalized probabilities
	 */
	protected double[] normalizedProbs;
	/**
	 * Cumulative probabilities
	 */
	protected double[] cumulativeProbs;
	/**
	 * Random that is used for decision making
	 */
	private Random random;
	/**
	 * Predicate that is used to check if cumulative probabilities need to be
	 * updated.
	 */
	private Predicate<Long> predicate;

	/**
	 * Default constructor
	 * 
	 * @param map
	 */
	protected AbstractDataModel(CityMap map) {
		this(map, 0);
	}

	/**
	 * Constructor with {@link Random} seed
	 * 
	 * @param map
	 * @param seed
	 */
	protected AbstractDataModel(CityMap map, long seed) {
		this(map, new Random(seed));
	}

	/**
	 * Constructor with {@link Random} {@code random}
	 * 
	 * @param map
	 * @param random
	 */
	protected AbstractDataModel(CityMap map, Random random) {
		this.map = map;
		this.random = random;
		zones = buildZones(map);
	}

	/**
	 * Provide zones depending on implementation. This method is called during
	 * construction.
	 * 
	 * @param map
	 * @return
	 */
	protected abstract List<Zone> buildZones(CityMap map);

	/**
	 * Sets a predicate to decide when cumulative density function should be updated.  
	 * 
	 * @param predicate
	 */
	public void setResetCdfPredicate(Predicate<Long> predicate) {
		this.predicate = predicate;
	}

	/**
	 * Resets cumulative density function.
	 * 
	 * @param time
	 */
	public void resetCdf(long time) {
		final double[] probs = new double[zones.size()];
		for (int i = 0; i < zones.size(); i++) {
			Zone zone = zones.get(i);
			// update probability at a zone
			probs[i] = zone.getDensity(time);
		}

		double sum = 0.0;
		final int len = probs.length;
		normalizedProbs = new double[len];
		for (int i = 0; i < len; i++) {
			sum += probs[i];
		}
		// normalize probabilities so that the sum of probabilities is equal to zero.
		if (sum > 0.0) {
			for (int i = 0; i < len; i++) {
				normalizedProbs[i] = probs[i] * 1.0 / sum;
			}
		} else {
			for (int i = 0; i < len; i++) {
				normalizedProbs[i] = 1.0 / (double) probs.length;
			}
		}

		// create cumulative density function.
		cumulativeProbs = new double[normalizedProbs.length];
		sum = 0.0;
		for (int i = 0; i < normalizedProbs.length; i++) {
			sum += normalizedProbs[i];
			cumulativeProbs[i] = sum;
		}
	}

	@Override
	public Intersection getDestination(LocationOnRoad currentLocation, long currentTime) {
		return getDestination(currentLocation, currentTime, random);
	}

	@Override
	public Intersection getDestination(LocationOnRoad currentLocation, long currentTime, Random rnd) {
		return getTargetRoad(currentLocation, currentTime, rnd).to;
	}

	@Override
	public LinkedList<Intersection> getRoute(LocationOnRoad currentLocation, long currentTime) {
		return getRoute(currentLocation, currentTime, random);
	}

	@Override
	public LinkedList<Intersection> getRoute(LocationOnRoad currentLocation, long currentTime, Random rnd) {
		Road road = null;
		do {
			road = getTargetRoad(currentLocation, currentTime, rnd);
		} while (currentLocation.road.to == road.to);

		LinkedList<Intersection> path = map.shortestTravelTimePath(currentLocation.road.to, road.from);
		if (!path.contains(road.to))
			path.addLast(road.to);

		return path;
	}

	/**
	 * Returns a road that will be considered as a target.
	 * 
	 * @param currentLocation
	 * @param currentTime
	 * @param rnd
	 * @return
	 */
	protected Road getTargetRoad(LocationOnRoad currentLocation, long currentTime, Random rnd) {
		predicateTest(currentTime);
		List<Road> roads = null;
		do {
			Zone selectedZone = sample(rnd);
			roads = selectedZone.getRoads();
		} while (roads.size() <= 0);
		int index = rnd.nextInt(roads.size());
		return roads.get(index);
	}

	/**
	 * Checks if the predicate is met.
	 * 
	 * @param currentTime
	 */
	protected void predicateTest(long currentTime) {
		if (predicate != null && predicate.test(currentTime)) {
			resetCdf(currentTime);
		}
	}

	/**
	 * Sample an index that will be used to choose a zone.
	 * 
	 * @param rnd
	 * @return
	 */
	protected int sampleIndex(Random rnd) {
		final double randomValue = rnd.nextDouble();
		int index = Arrays.binarySearch(cumulativeProbs, randomValue);
		if (index < 0) {
			index = -index - 1;
		}
		if (index >= 0 && index < normalizedProbs.length && randomValue < cumulativeProbs[index]) {
			return index;
		}
		return zones.size() - 1;
	}

	/**
	 * Sample a zone
	 * 
	 * @param rnd
	 * @return
	 */
	protected Zone sample(Random rnd) {
		return zones.get(sampleIndex(rnd));
	}

}
