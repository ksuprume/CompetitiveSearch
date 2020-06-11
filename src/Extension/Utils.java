package Extension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * This is for commonly used class methods.
 *
 */
public class Utils {
	/**
	 * Convert Epoch Second to ZonedDateTime
	 * 
	 * @param epochSecond
	 * @return
	 */
	public static ZonedDateTime getTime(long epochSecond) {
		if (epochSecond != Long.MIN_VALUE) {
			ZoneId zoneId = ZoneId.of("America/New_York");
			return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond, 0), zoneId);
		}
		return ZonedDateTime.now();
	}
	
	/**
	 * Return greatest common divisor
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int gcd(int a, int b) {
		if (b == 0)
			return a;
		return gcd(b, a % b);
	}
}
