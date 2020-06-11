package Extension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 *
 */
public abstract class AbstractMatrix implements Matrix {
	protected ZonedDateTime start;
	protected ZonedDateTime end;
	protected Map<Long, Integer> zoneToRow;
	protected int numOfTemporalSlotsPerDay;
	protected int width;

	/**
	 * Ignore time field of {@code start} and {@code end}
	 * 
	 * @param start
	 * @param end
	 * @param numOfTemporalSlotsPerDay
	 * @param zoneIds
	 */
	public AbstractMatrix(ZonedDateTime start, ZonedDateTime end, int numOfTemporalSlotsPerDay, List<Long> zoneIds) {
		if (!start.isBefore(end))
			throw new RuntimeException("Start datetime should be earlier than end datetime.");

		// positive integer
		if (numOfTemporalSlotsPerDay < 0)
			throw new RuntimeException("numOfTemporalSlotsPerDay should be greater than 0.");
		// drop time field
		this.start = ZonedDateTime.of(start.getYear(), start.getMonthValue(), start.getDayOfMonth(), 0, 0, 0, 0, start.getZone());
		this.end = ZonedDateTime.of(end.getYear(), end.getMonthValue(), end.getDayOfMonth(), 0, 0, 0, 0, end.getZone());
		this.numOfTemporalSlotsPerDay = numOfTemporalSlotsPerDay;
		double spanInMinute = (this.end.toEpochSecond() - this.start.toEpochSecond()) / TemporalModel.SECONDS_OF_MINUTE;
		int days = (int) Math.ceil(spanInMinute / (TemporalModel.HOURS_OF_DAY * TemporalModel.MINUTES_OF_HOUR));
		if (days < 7)
			throw new RuntimeException("Duration should be more than 6 days.");

		width = days * numOfTemporalSlotsPerDay;
		setZones(zoneIds);
	}

	/**
	 * Sets zones corresponding row of w
	 * 
	 * @param ids
	 */
	public void setZones(List<Long> ids) {
		zoneToRow = new TreeMap<>();
		for (int i = 0; i < ids.size(); i++) {
			zoneToRow.put(ids.get(i), i);
		}
	}

	/**
	 * Find a row corresponding {@code zoneId}.
	 * If no id exists, return -1.
	 * 
	 * @param zoneId
	 * @return
	 */
	public int findRow(long zoneId) {
		Integer ret = zoneToRow.get(zoneId);
		if(ret == null)
			return -1;
		return ret;
	}

	/**
	 * Find a column corresponding time {@code epochSecond}.
	 * 
	 * @param epochSecond
	 * @return
	 */
	public int findColumn(long epochSecond) {
		ZonedDateTime dateTime = Utils.getTime(epochSecond);
		int day = dateTime.getDayOfWeek().getValue();
		int hour = dateTime.getHour();
		int minute = dateTime.getMinute();
		int second = dateTime.getSecond();
		// for debugging
//		System.out.println("DateTime:" + dateTime + " " + dateTime.getDayOfWeek());
		
		if (!isValid(dateTime)) {
			// if out of scope, find the most similar day
			if (dateTime.isBefore(start)) {
				int gap = start.getYear() - dateTime.getYear();
				dateTime = dateTime.plusYears(gap);
			}
			if (dateTime.isAfter(end)) {
				int gap = dateTime.getYear() - end.getYear();
				dateTime = dateTime.minusYears(gap);
			}
			
			// to match the day of week
			int diffDay = day - dateTime.getDayOfWeek().getValue();
			diffDay = Math.abs(diffDay) < 4 ? diffDay : (diffDay > 0 ? diffDay - 7 : diffDay + 7);
			ZonedDateTime tmp = dateTime.plusDays(diffDay);
			if (isValid(tmp)) {
				// within scope
				dateTime = tmp;
			} else {
				// if out of scope, find the most similar day near to either start or end date
				int t = dateTime.getDayOfYear();
				int s = start.getDayOfYear();
				int e = end.getDayOfYear() - 1;

				int diffS = Math.min(365 + s - t, Math.abs(s - t));
				int diffE = Math.min(365 + e - t, Math.abs(e - t));

				if (diffS < diffE) {
					// near to start
					int plusDays = (day + 7 - start.getDayOfWeek().getValue()) % 7;
					dateTime = start.plusDays(plusDays).plusHours(hour).plusMinutes(minute).plusSeconds(second);
				} else {
					// near to end
					int minusDays = (end.getDayOfWeek().getValue() + 6 - day) % 7;
					dateTime = end.minusDays(minusDays + 1).plusHours(hour).plusMinutes(minute).plusSeconds(second);
				}
			}

		}
		// for debugging
//		System.out.println("Selected DateTime:" + dateTime + " " + dateTime.getDayOfWeek());
		return column(dateTime);
	}
		
	private boolean isValid(ZonedDateTime dateTime) {
		return (dateTime.isAfter(start) || dateTime.isEqual(start)) && dateTime.isBefore(end);
	}
	
	private int column(ZonedDateTime dateTime) {
		int days = dateTime.getDayOfYear() - start.getDayOfYear();
		int hours = dateTime.getHour() - start.getHour();
		int minutes = dateTime.getMinute() - start.getMinute();
		int seconds = dateTime.getMinute() - start.getSecond();
		
		int a = days * numOfTemporalSlotsPerDay;
		double b = (double) (hours * TemporalModel.SECONDS_OF_HOUR + minutes * TemporalModel.SECONDS_OF_MINUTE + seconds)
				/ (double) (TemporalModel.SECONDS_OF_DAY)
				* (double) numOfTemporalSlotsPerDay;
		return a + (int) b;
	}

	@Override
	public int getHeight() {
		return zoneToRow.size();
	}

	@Override
	public int getWidth() {
		return width;
	}

}
