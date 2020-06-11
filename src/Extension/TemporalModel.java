package Extension;

/**
 * Temporal model
 * 
 *
 */
public interface TemporalModel extends Model {
	public static final int START = 0;
	public static final int END = 24;
	public static final int HOURS_OF_DAY = 24;
	public static final int MINUTES_OF_HOUR = 60;
	public static final int SECONDS_OF_MINUTE = 60;
	public static final int SECONDS_OF_HOUR = MINUTES_OF_HOUR * SECONDS_OF_MINUTE;
	public static final int SECONDS_OF_DAY = HOURS_OF_DAY * MINUTES_OF_HOUR * SECONDS_OF_MINUTE;
	public static final int SPAN_IN_MINUTE = (END - START) * SECONDS_OF_MINUTE;
}
