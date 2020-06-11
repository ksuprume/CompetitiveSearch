package Extension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * This is a class that allows to load/store configuration. It is interoperable
 * with a COMSET configuration file.
 *
 */
public class ConfigurableWorldParameters extends WorldParameters {
	private static final long serialVersionUID = 6593267387202654198L;

	public ConfigurableWorldParameters() {
		super();
	}

	/**
	 * Load configuration from a configuration file.
	 * 
	 * @param fileName
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ConfigurationException
	 */
	public ConfigurableWorldParameters(String fileName)
			throws IllegalArgumentException, IllegalAccessException, ConfigurationException {
		super();
		Parameters params = new Parameters();
		File propertiesFile = new File(fileName);
		CustomConversionHandler handler = new CustomConversionHandler();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class)
						.configure(params.fileBased().setConversionHandler(handler).setFile(propertiesFile)
								.setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
		Configuration conf = builder.getConfiguration();

		Field[] fields = WorldParameters.class.getDeclaredFields();
		int mod;
		int skipMod = Modifier.STATIC | Modifier.VOLATILE | Modifier.TRANSIENT | Modifier.FINAL;
		for (int i = 0; i < fields.length; i++) {
			mod = fields[i].getModifiers();
			if ((mod & skipMod) == 0) {
				String key = getConfigKey(fields[i]);
				if (!conf.containsKey(key))
					continue;
				Object value = conf.get((Class<?>) fields[i].getType(), key);

				fields[i].setAccessible(true);
				fields[i].set(this, value);
			}
		}
	}

	public static String getConfigKey(Field field) {
		String key = field.getName();
		if (field.isAnnotationPresent(ParameterGroup.class)) {
			ParameterGroup pg = field.getAnnotation(ParameterGroup.class);
			String group = pg.group();
			if (group != null && !group.equals(""))
				key = group + "." + key;
		}
		return key;
	}

	/**
	 * Store current configuration in a file whose name is given.
	 * 
	 * @param fileName
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public void store(String fileName)
			throws IllegalArgumentException, IllegalAccessException, ConfigurationException, IOException {
		Parameters params = new Parameters();
		File propertiesFile = new File(fileName);
		if (!propertiesFile.exists())
			propertiesFile.createNewFile();

		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class).configure(params.fileBased().setFile(propertiesFile));
		Configuration conf = builder.getConfiguration();

		Field[] fields = WorldParameters.class.getDeclaredFields();
		int mod;
		int skipMod = Modifier.STATIC | Modifier.VOLATILE | Modifier.TRANSIENT | Modifier.FINAL;
		for (int i = 0; i < fields.length; i++) {
			mod = fields[i].getModifiers();
			if ((mod & skipMod) == 0) {
				String key = getConfigKey(fields[i]);
				Object defaultValue = fields[i].get(this);

				conf.setProperty(key, defaultValue);
			}
		}
		builder.save();
	}
}
