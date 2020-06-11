package Extension;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;

import COMSETsystem.CityMap;
import COMSETsystem.Link;
import MapCreation.MapCreator;

/**
 * I employ Python libraries to build a model. Therefore, you will need a Python
 * interpreter (e.g., anaconda) and required libraries, including numpy and
 * sklearn. For seamless integration, you can develop your own NMF or use other
 * java/scala library.
 * <br>
 * Learning consists of two parts: 1) generating raw matrix, 2) factorization.
 * <br>
 * In order to learn a model, first add data paths in the configuration file using "mf.learningData" property.
 * <br>
 * <code>
 * mf.learningData = NewYorkTLC/yellow_tripdata_2016-01.csv
 * <br>
 * mf.learningData = NewYorkTLC/yellow_tripdata_2016-02.csv
 * </code>
 * <br>
 * <br>
 * ModelBuilder has a main function that takes arguments as follows:
 * <br>
 * <code>[-c COMMAND] [-config ConfigFilePath] [-python PythonPath]</code>
 * <br>
 * COMMAND: generate_test_data<br>
 * COMMAND: generate_matrix<br>
 * COMMAND: factorization<br>
 * <br>
 * Examples:
 * <br>
 * <code>java -cp COMSET-1.0-jar-with-dependencies.jar Extension.ModelBuilder -c generate_matrix -config etc/config.properties</code>
 * <br>
 * <code>java -cp COMSET-1.0-jar-with-dependencies.jar Extension.ModelBuilder -c factorization -config etc/config.properties</code>
 * <br>
 * <code>java -cp COMSET-1.0-jar-with-dependencies.jar Extension.ModelBuilder -c factorization -config etc/config.properties -python C:/Python37/python</code>
 * 
 *
 */
public class ModelBuilder {
	DateTimeFormatter dtf;
	ZoneId zoneId;
	CityMap map;
	WorldParameters param;
	MutableMatrixImpl mx;
	
	public ModelBuilder(String configFile) throws IllegalArgumentException, IllegalAccessException, ConfigurationException {
		if(configFile==null)
			param = new ConfigurableWorldParameters(); // default configuration
		else
			param = new ConfigurableWorldParameters(configFile); // load from the input configuration file
		
		MapCreator creator = new MapCreator(param.map_JSON_file, param.bounding_polygon_KML_file, param.speed_reduction);
		creator.createMap();
		// Output the map
		map = creator.outputCityMap();
		dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		zoneId = ZoneId.of(param.timezone);
	}

	private Long dateConversion(String timestamp) {
		long l = 0L;
		LocalDateTime ldt = LocalDateTime.parse(timestamp, dtf);
		ZonedDateTime zdt = ZonedDateTime.of(ldt, zoneId);
		l = zdt.toEpochSecond(); //Returns Linux epoch, i.e., the number of seconds since January 1, 1970, 00:00:00 GMT until time specified in zdt
		return l;
	}

	/**
	 * Convert TLC Trip Record Data to simplified data (pickup_time, road_id)
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void convert(String inputFile, String outputFile) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile)); FileWriter writer = new FileWriter(outputFile)) {
			
			if (inputFile == null) {
				System.out.println("The resource dataset file must be specified in the configuration file.");
				System.exit(1);
			}
			String line = reader.readLine(); // skip the header
			String[] tokens; 
			writer.write("pickup_datetime\troad_id\n");
			while ((line = reader.readLine()) != null) {
				tokens = line.split(",");
				String timeStr = tokens[1];
				// long epochSecond = dateConversion(timeStr);
				double pickupLon = Double.parseDouble(tokens[5]);
				double pickupLat = Double.parseDouble(tokens[6]);
				double dropoffLon = Double.parseDouble(tokens[9]);
				double dropoffLat = Double.parseDouble(tokens[10]);
				// Only keep the resources such that both pickup location and dropoff location are within the bounding polygon.
				if (!(MapCreator.insidePolygon(pickupLon, pickupLat) && MapCreator.insidePolygon(dropoffLon, dropoffLat))) {
					continue;
				}
				Link link = map.getNearestLink(pickupLon, pickupLat);
				long roadId = link.road.id;
				writer.write(timeStr + "\t" + roadId + System.lineSeparator());
			}
		}
	}

	private void init() throws IOException {
		ZoneId zoneId = ZoneId.of(param.timezone);
		ZonedDateTime start = ZonedDateTime.of(param.temporalModelStartDatetime, zoneId);
		ZonedDateTime end = ZonedDateTime.of(param.temporalModelEndDatetime, zoneId);

		List<Long> zoneIds = new ArrayList<>();
		map.roads().forEach(m -> zoneIds.add(m.id));
		zoneIds.sort(null);
		try (FileWriter writer = new FileWriter("model/roadIds.txt")) {
			writer.write("road_id\n");
			for (Long id : zoneIds) {
				writer.write(String.valueOf(id) + "\n");
			}
		}

		mx = new MutableMatrixImpl(start, end, param.numOfTemproalSlots, zoneIds);
	}

	private void writeMatrix() throws IOException {
		try (FileWriter writer = new FileWriter("model/raw_matrix.txt")) {
			for (int i = 0; i < mx.getHeight(); i++) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < mx.getWidth(); j++) {
					int a = (int) mx.get(j, i);
					sb.append(a).append(" ");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(System.lineSeparator());
				writer.write(sb.toString());
			}
		}
	}

	private void learning(String inputFile) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			if (inputFile == null) {
				System.out.println("The resource dataset file must be specified in the configuration file.");
				System.exit(1);
			}
			String line = reader.readLine(); // skip the header
			String[] tokens; 
			while ((line = reader.readLine())!=null) {
				if(line.equals(""))
					continue;
				tokens = line.split(",");
				String timeStr = tokens[1];
				long epochSecond = dateConversion(timeStr);
				double pickupLon = Double.parseDouble(tokens[5]);
				double pickupLat = Double.parseDouble(tokens[6]);
				double dropoffLon = Double.parseDouble(tokens[9]);
				double dropoffLat = Double.parseDouble(tokens[10]);
				// Only keep the resources such that both pickup location and dropoff location are within the bounding polygon.
				if (!(MapCreator.insidePolygon(pickupLon, pickupLat) && MapCreator.insidePolygon(dropoffLon, dropoffLat))) {
					continue;
				}
				Link link = map.getNearestLink(pickupLon, pickupLat);
				long roadId = link.road.id;
				int column = mx.findColumn(epochSecond);
				int row = mx.findRow(roadId);
				if (row != -1)
					mx.increment(column, row);
			}
		}
	}

	/**
	 * New York TLC data is not sorted and it is inevitable to load all of them
	 * unless we open 31 files. It is likely to cause "java.lang.OutOfMemoryError:
	 * Java heap space" due to data size (>1GB). If it happens, increase the maximum
	 * heap space using the -Xmx option in the command line (e.g., -Xmx6g).
	 * 
	 * @param inputFile
	 * @throws IOException
	 */
	private void generateTestData(String inputFile) throws IOException {
		Map<Integer, StringBuilder> outTexts = new HashMap<Integer, StringBuilder>();
		String header = null;

		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			if (inputFile == null) {
				System.out.println("The resource dataset file must be specified in the configuration file.");
				System.exit(1);
			}
			header = reader.readLine(); // skip the header
			String line = header;
			String[] tokens; 
			while ((line = reader.readLine())!=null) {
				if(line.equals(""))
					continue;					
				tokens = line.split(",");
				String timeStr = tokens[1];
				ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.parse(timeStr, dtf), zoneId);
				double pickupLon = Double.parseDouble(tokens[5]);
				double pickupLat = Double.parseDouble(tokens[6]);
				double dropoffLon = Double.parseDouble(tokens[9]);
				double dropoffLat = Double.parseDouble(tokens[10]);
				// Only keep the resources such that both pickup location and dropoff location are within the bounding polygon.
				if (!(MapCreator.insidePolygon(pickupLon, pickupLat) && MapCreator.insidePolygon(dropoffLon, dropoffLat))) {
					continue;
				}
				if (zdt.getHour() < 8 || zdt.getHour() > 21 || (zdt.getHour() == 22 && zdt.getMinute() == 0)) {
					// Ignore data if it is out of bounds between 8:00 am and 10:00 pm.
					continue;
				}
				int day = zdt.getDayOfMonth();
				
				if(!outTexts.containsKey(day)) {
					outTexts.put(day, new StringBuilder());
				}
				StringBuilder sb = outTexts.get(day);
				sb.append(line).append("\n");
			}
		}
		
		for(int day: outTexts.keySet()) {
			StringBuilder sb = outTexts.get(day);
			String text = String.format("-%02d.csv", day);
			String outFile = inputFile.replace(".csv", text);
			try (FileWriter writer = new FileWriter(outFile)) {
				writer.write(header);
				writer.write(sb.toString());
			} 
		}
	}

	private static void printUsage() {
		System.out.println("Usage: ModelBuilder [-c COMMAND] [-config ConfigFilePath] [-python PythonPath]");
		System.out.println("\tCOMMAND: generate_test_data");
		System.out.println("\tCOMMAND: generate_matrix");
		System.out.println("\tCOMMAND: factorization");
		System.out.println();
		System.out.println("\tNote: factorization only works if raw_matrix.txt file (i.e., output file of generate_matrix) exists.");
		System.out.println("\tExample: ModelBuilder -c generate_matrix");
		System.out.println("\tExample: ModelBuilder -c generate_matrix -config etc/config.properties");
		System.out.println("\tExample: ModelBuilder -c factorization -config etc/config.properties");
		System.out.println("\tExample: ModelBuilder -c factorization -config etc/config.properties -python C:/Python37/python");
	}

	static String argumentForKey(String key, String[] args) {
		for (int x = 0; x < args.length - 1; x++)
			// if a key has an argument, it can't be the last string
			if (args[x].equalsIgnoreCase(key))
				return args[x + 1];
		return null;
	}

	public static void main(String ... args) throws IOException, IllegalArgumentException, IllegalAccessException, ConfigurationException {
		if (args.length > 1) {
			String configFile = argumentForKey("-config", args);
			String pythonPath = argumentForKey("-python", args);
			String command = argumentForKey("-c", args);
			
			ModelBuilder builder = new ModelBuilder(configFile);
			String[] inputFiles = builder.param.learningData;
			if (command != null) {
				if (command.equalsIgnoreCase("simplify")) {
					for (int i = 0; i < inputFiles.length; i++)
						builder.convert(inputFiles[i], inputFiles[i].replace("tripdata", "simplified"));
				} else if (command.equalsIgnoreCase("generate_test_data")) {
					for (int i = 0; i < inputFiles.length; i++)
						builder.generateTestData(inputFiles[i]);
				} else if (command.equalsIgnoreCase("generate_matrix")) {
					builder.init();
					for (int i = 0; i < inputFiles.length; i++)
						builder.learning(inputFiles[i]);
					builder.writeMatrix();
				} else if (command.equalsIgnoreCase("factorization")) {
					if (pythonPath == null)
						pythonPath = "python";
					Process p = Runtime.getRuntime().exec(pythonPath + " model/NMF.py");
					BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line = null;
					while ((line = in.readLine()) != null) {
						System.out.println(line);
					}
				} else
					printUsage();
			} else
				printUsage();
		}
		else printUsage();
	}
}
