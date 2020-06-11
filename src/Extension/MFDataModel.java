package Extension;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import COMSETsystem.CityMap;
import COMSETsystem.Road;

/**
 * Data model that utilizes Matrix Factorization.
 *
 */
public class MFDataModel extends DistanceBasedDataModel {
	Map<Long, Zone> mappingTable;
	private int timeWindowSizeInMinute;
	private int numOfTemporalSlots;

	public MFDataModel(CityMap map) {
		super(map);
		int slotSizeInSecond = TemporalModel.SPAN_IN_MINUTE * TemporalModel.SECONDS_OF_MINUTE / numOfTemporalSlots;
		int timeWindowSizeInSecond = timeWindowSizeInMinute * TemporalModel.SECONDS_OF_MINUTE;
		setResetCdfPredicate(new PeriodicUpdated(Utils.gcd(slotSizeInSecond, timeWindowSizeInSecond)));
	}

	private void init() {
		WorldParameters params = WorldParameters.getInstance();
		numOfTemporalSlots = params.numOfTemproalSlots;
		timeWindowSizeInMinute = params.timeWindowSizeInMinute;
	}

	@Override
	protected List<Zone> buildZones(CityMap map) {
		init();
		mappingTable = new TreeMap<Long, Zone>();
		List<Road> roads = map.roads();
		SetZone[] zones = new SetZone[roads.size()];
		Matrix matrix = loadMatrix();
		for (int i = 0; i < zones.length; i++) {
			MFModel mf = new MFModel(matrix, timeWindowSizeInMinute);
			Road road = roads.get(i);
			mf.setRoadId(road.id);
			zones[i] = new SetZone(mf);
			zones[i].add(road);
			mappingTable.put(road.id, zones[i]);
		}
		return Arrays.asList(zones);
	}

	/**
	 * Load a matrix.
	 * 
	 * @return
	 */
	private Matrix loadMatrix() {
		WorldParameters params = WorldParameters.getInstance();
		ZoneId zoneId = ZoneId.of(params.timezone);
		ZonedDateTime start = ZonedDateTime.of(params.temporalModelStartDatetime, zoneId);
		ZonedDateTime end = ZonedDateTime.of(params.temporalModelEndDatetime, zoneId);
		List<Long> roadIds = new ArrayList<>();
		for (Road road : map.roads()) {
			roadIds.add(road.id);
		}
		roadIds.sort(null);
		
		Matrix mx = null;
		if (params.buildMatrix) {
			mx = new MutableMatrixImpl(start, end, numOfTemporalSlots, roadIds);
		} else {
			// load from file
			mx = new FactorizedMatrix(start, end, numOfTemporalSlots, roadIds);
			String wFile = MessageFormat.format(WorldParameters.MF_W_MODEL, params.numOfFeatures);
			String hFile = MessageFormat.format(WorldParameters.MF_H_MODEL, params.numOfFeatures);
			try (BufferedReader readerW = new BufferedReader(new FileReader(wFile));
					BufferedReader readerH = new BufferedReader(new FileReader(hFile))) {
				String lineW = readerW.readLine();
				String[] tokensW = lineW.split(" ");

				String lineH = readerH.readLine();
				String[] tokensH = lineH.split(" ");

				double[][] w = new double[tokensH.length][tokensW.length];
				double[][] h = new double[tokensW.length][tokensH.length];
				int r = -1;
				do {
					r++;
					tokensW = lineW.split(" ");
					for (int i = 0; i < tokensW.length; i++)
						w[r][i] = Double.valueOf(tokensW[i]);
				} while ((lineW = readerW.readLine()) != null);

				r = -1;
				do {
					r++;
					tokensH = lineH.split(" ");
					for (int i = 0; i < tokensH.length; i++)
						h[r][i] = Double.valueOf(tokensH[i]);
				} while ((lineH = readerH.readLine()) != null);

				((FactorizedMatrix)mx).setWH(w, h);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mx;
	}
}
