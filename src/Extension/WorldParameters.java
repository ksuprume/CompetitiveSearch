package Extension;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This class contains default settings and annotation.
 *
 */
public class WorldParameters implements Serializable {
	private static final long serialVersionUID = -4167777875952127038L;

	public static WorldParameters instance = null;

	public static final String DEFAULT_PROPERTY_FILE_NAME = "etc/config.properties";

	// COMSET
	public static final int AGENT_PLACEMENT_SEED = 1;
	public static final int NUMBER_OF_AGENTS = 5000;
	public static final String MAP_JSON_FILE = "maps/manhattan-map.json";
	public static final String DATASET_FILE = "datasets/yellow_tripdata_2016-06-01_busyhours.csv";
	public static final String BOUNDING_POLYGON_KML_FILE = "maps/manhattan-boundary.kml";
	public static final String AGENT_CLASS = "Extension.CSTS";
	public static final int RESOURCE_MAXIMUM_LIFE_TIME = 600;
	public static final int SPEED_REDUCTION = 4;
	public static final boolean LOGGING = false;

	// DEFAULT DATA MODEL
	public static final String DATA_MODEL = "Extension.MFDataModel";
	
	// RE-ROUTING
	public static final boolean REROUTING = false;
	public static final double REROUTING_TIME = 1;

	// MODEL SPECIFIC PARAMETERS

	// GRID DATA MODEL
	public static final int ONE_SIDE_SIZE = 10;

	// TEMPORAL MODEL
	public static final int NUM_OF_TEMPORAL_SLOTS = 72;
	public static final int TIME_WINDOW_SIZE_IN_MINUTE = 150;
	public static final LocalDateTime TEMPORAL_MODEL_START_DATETIME = LocalDateTime.of(2016, 1, 1, 0, 0, 0);
	public static final LocalDateTime TEMPORAL_MODEL_END_DATETIME = LocalDateTime.of(2016, 7, 1, 0, 0, 0);
	public static final String TIMEZONE = "America/New_York";

	// DISTANCE BADED MODEL
	public static final int SAMPLE_SIZE = 5;
	public static final double EXPONENT = -0.5;

	// HOME ZONE MODEL
	public static final int NUM_OF_GROUPS = 4;

	// LOGGING
	public static final String LOG_FILE_PATH = "data.csv";

	// MATRIX FACTORIZATION
	public static final int NUM_OF_FEATURES = 6;
	public static final String MF_W_MODEL = "model/W{0}.txt";
	public static final String MF_H_MODEL = "model/H{0}.txt";
	public static final boolean BUILD_MATRIX = false;
	public static final String[] LEARNING_DATA = {"datasets/yellow_tripdata_2016-01.csv"};

	// COMSET
	@ParameterGroup(group = "comset", description = "The seed of the random number generator for agents placement", lower = "1", upper = "100")
	public int agent_placement_seed;
	@ParameterGroup(group = "comset", description = "Map file path", lower = "", upper = "")
	public String map_JSON_file;
	@ParameterGroup(group = "comset", description = "Resource file path", lower = "", upper = "")
	public String dataset_file;
	@ParameterGroup(group = "comset", description = "Number of agents", lower = "5000", upper = "10000")
	public int number_of_agents;
	@ParameterGroup(group = "comset", description = "Bounding polygon KML file path", lower = "5000", upper = "10000")
	public String bounding_polygon_KML_file;
	@ParameterGroup(group = "comset", description = "The agent class name", lower = "", upper = "")
	public String agent_class;
	@ParameterGroup(group = "comset", description = "The maximum life time of a resource", lower = "600", upper = "600")
	public int resource_maximum_life_time;
	@ParameterGroup(group = "comset", description = "The speed reduction to accommodate traffic jams and turn delays", lower = "4", upper = "4")
	public int speed_reduction;
	@ParameterGroup(group = "comset", description = "Whether to enable display of logging", lower = "", upper = "")
	public boolean logging;

	// REROUTING
	@ParameterGroup(group = "rerouting", description = "Whether reroute", lower = "", upper = "")
	public boolean rerouting;
	@ParameterGroup(group = "rerouting", description = "Rerouting timing as ratio", lower = "", upper = "")
	public double reroutingTime;
	
	// MODEL SPECIFIC PARAMETERS
	@ParameterGroup(group = "grid", description = "The number of shorten axis grid sides", lower = "8", upper = "12")
	public int oneSideSize;

	@ParameterGroup(group = "temporal", description = "Number of discrete temporal slots", lower = "10", upper = "30")
	public int numOfTemproalSlots;
	@ParameterGroup(group = "temporal", description = "Time window size in minute", lower = "60", upper = "120")
	public int timeWindowSizeInMinute;
	@ParameterGroup(group = "temporal", description = "Temporal model start datetime", lower = "", upper = "")
	public LocalDateTime temporalModelStartDatetime;
	@ParameterGroup(group = "temporal", description = "Temporal model end datetime", lower = "", upper = "")
	public LocalDateTime temporalModelEndDatetime;
	@ParameterGroup(group = "temporal", description = "Temporal model timezone", lower = "", upper = "")
	public String timezone;
	
	@ParameterGroup(group = "distance", description = "Sample size", lower = "2", upper = "10")
	public int sampleSize;
	@ParameterGroup(group = "distance", description = "Distance exponent", lower = "-1.0", upper = "-0.1")
	public double exponent;

	@ParameterGroup(group = "home", description = "Number of gourps of home zone", lower = "1", upper = "6")
	public int numOfGroups;

	@ParameterGroup(group = "mf", description = "Number of features of matrix", lower = "1", upper = "10")
	public int numOfFeatures;
	@ParameterGroup(group = "mf", description = "Whether building a matrix", lower = "", upper = "")
	public boolean buildMatrix;

	@ParameterGroup(group = "", description = "Log file", lower = "", upper = "")
	public String logFilePath;

	@ParameterGroup(group = "", description = "Property file", lower = "", upper = "")
	public String propertyFile;

	@ParameterGroup(group = "", description = "Data model", lower = "", upper = "")
	public String dataModel;
	
	@ParameterGroup(group = "mf", description = "Data model", lower = "", upper = "")
	public String[] learningData;

	/**
	 * Default constructor
	 */
	public WorldParameters() {
		initializationWithDefaultValues();
	}

	/**
	 * Singleton instance
	 * @return
	 */
	public static WorldParameters getInstance() {
		if (instance != null)
			return instance;
		return new WorldParameters();
	}

	/**
	 * Initialize all variables with default values.
	 */
	protected void initializationWithDefaultValues() {
		agent_placement_seed = AGENT_PLACEMENT_SEED;
		map_JSON_file = MAP_JSON_FILE;
		dataset_file = DATASET_FILE;
		number_of_agents = NUMBER_OF_AGENTS;
		bounding_polygon_KML_file = BOUNDING_POLYGON_KML_FILE;
		agent_class = AGENT_CLASS;
		resource_maximum_life_time = RESOURCE_MAXIMUM_LIFE_TIME;
		speed_reduction = SPEED_REDUCTION;
		logging = LOGGING;
		
		rerouting = REROUTING;
		reroutingTime = REROUTING_TIME;

		// MODEL SPECIFIC PARAMETERS
		oneSideSize = ONE_SIDE_SIZE;
		numOfTemproalSlots = NUM_OF_TEMPORAL_SLOTS;
		timeWindowSizeInMinute = TIME_WINDOW_SIZE_IN_MINUTE;
		temporalModelStartDatetime = TEMPORAL_MODEL_START_DATETIME;
		temporalModelEndDatetime = TEMPORAL_MODEL_END_DATETIME;
		timezone = TIMEZONE;

		sampleSize = SAMPLE_SIZE;
		exponent = EXPONENT;
		numOfGroups = NUM_OF_GROUPS;
		numOfFeatures = NUM_OF_FEATURES;
		buildMatrix = BUILD_MATRIX;

		logFilePath = LOG_FILE_PATH;
		propertyFile = DEFAULT_PROPERTY_FILE_NAME;

		dataModel = DATA_MODEL;
		learningData = LEARNING_DATA;
	}
}
