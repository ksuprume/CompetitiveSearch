package Extension;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;

public class CustomConversionHandler extends DefaultConversionHandler {
	protected <T> T convertValue(Object src, Class<T> targetCls, ConfigurationInterpolator ci) {
		if (targetCls == Duration.class)
			return (T) Duration.parse(src.toString());
		if (targetCls == LocalDateTime.class)
			return (T) LocalDateTime.parse(src.toString());
		return super.convertValue(src, targetCls, ci);
	}
}
