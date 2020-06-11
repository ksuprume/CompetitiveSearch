package Extension;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.google.common.collect.Lists;

import COMSETsystem.CityMap;
import COMSETsystem.Link;
import COMSETsystem.Road;
import DataParsing.CSVNewYorkParser;
import DataParsing.Resource;

public class LinearHomeZoneDataModel extends HomeZoneDataModel {

	Map<Long, Zone> mappingTable;
	private int timeWindowSizeInMinute;
	private int numOfTemporalSlots;

	public LinearHomeZoneDataModel(CityMap map) {
		super(map);
		int slotSizeInSecond = TemporalModel.SPAN_IN_MINUTE * 60 / numOfTemporalSlots;
		int timeWindowSizeInSecond = timeWindowSizeInMinute * 60;
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

		for (int i = 0; i < zones.length; i++) {
			DayModel dayModel = new DayModel(timeWindowSizeInMinute, numOfTemporalSlots);
			zones[i] = new SetZone(dayModel);
			zones[i].add(roads.get(i));
			mappingTable.put(roads.get(i).id, zones[i]);
		}

		List<Resource> resources = load();
		for (int i = 0; i < resources.size(); i++) {
			double longitude = resources.get(i).getPickupLon();
			double latitude = resources.get(i).getPickupLat();
			Link link = map.getNearestLink(longitude, latitude);
			Zone zone = mappingTable.get(link.road.id);
			if (zone != null)
				zone.addResource(resources.get(i));
		}
		return Arrays.asList(zones);
	}
	
	/**
	 * It is only used to compare with some baseline methods.
	 * 
	 * @return
	 */
	protected List<Resource> load() {
		String configFile = "etc/config.properties";
		try (InputStream in = new FileInputStream(configFile)) {
			Properties prop = new Properties();
			prop.load(in);

			String datasetFile = prop.getProperty("comset.dataset_file").trim();
			if (datasetFile == null) {
				System.out.println("The resource dataset file must be specified in the configuration file.");
				System.exit(1);
			}
			CSVNewYorkParser parser = new CSVNewYorkParser(datasetFile, ZoneId.of("America/New_York"));
			return parser.parse();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return Lists.newArrayList();
	}
}
