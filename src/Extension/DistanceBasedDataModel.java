package Extension;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import COMSETsystem.CityMap;
import COMSETsystem.LocationOnRoad;
import COMSETsystem.Road;

/**
 * Data model that considers distances from an agent's current location to zones.
 *
 */
public abstract class DistanceBasedDataModel extends AbstractDataModel {
	protected DistanceBasedDataModel(CityMap map) {
		super(map);
	}

	protected Road getTargetRoad(LocationOnRoad currentLocation, long currentTime, Random rnd) {
		predicateTest(currentTime);
		List<Road> roads = null;
		
		WorldParameters params = WorldParameters.getInstance();
		// Sample multiple zones
		int[] zoneIndices = new int[params.sampleSize];
		final int len = zoneIndices.length;
		for (int i = 0; i < len; i++) {
			int zoneNumber;
			do {
				zoneNumber = sampleIndex(rnd);
				// check availability
				Zone selectedZone = zones.get(zoneNumber);
				roads = selectedZone.getRoads();
			} while (roads.size() <= 0);

			zoneIndices[i] = zoneNumber;
		}

		// distance indices
		double[] probs = new double[len];
		double[] nProbs = new double[len];
		
		double sum = 0.0;
		// Adjust cdf depends on distance
		for (int i = 0; i < len; i++) {
			Zone selectedZone = zones.get(zoneIndices[i]);
			roads = selectedZone.getRoads();
			Road road = roads.get(0);
			long distance = map.travelTimeBetween(currentLocation.road.to, road.from);
			probs[i] = (distance <= 0) ? 1 : Math.pow((double)distance, params.exponent);
			sum += probs[i];
		}
		
		if (sum > 0.0) {
			for (int i = 0; i < len; i++) {
				nProbs[i] = probs[i] * 1.0 / sum;
			}
		}
		else {
			for (int i = 0; i < len; i++) {
				nProbs[i] = 1.0 / (double)probs.length;
			}
		}

		double[] cProbs = new double[nProbs.length];
		sum = 0.0;
		for (int i = 0; i < nProbs.length; i++) {
			sum += nProbs[i];
			cProbs[i] = sum;
		}
		
		// Select one
		final double randomValue = rnd.nextDouble();
		int index = Arrays.binarySearch(cProbs, randomValue);
		if (index < 0) {
			index = -index - 1;
		}
		if (index >= 0 && index < nProbs.length && randomValue < cProbs[index]) {
			// DO NOTHING
		}
		else {
			index = len - 1;
		}

		Zone selectedZone = zones.get(zoneIndices[index]);
		roads = selectedZone.getRoads();
		int rndRoad = rnd.nextInt(roads.size());
		return roads.get(rndRoad);
	}
}
