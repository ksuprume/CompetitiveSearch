package Extension;

import java.io.Serializable;
import java.time.ZonedDateTime;

import DataParsing.Resource;

public class DayModel implements TemporalModel, Serializable {
	private static final long serialVersionUID = 8398189712109585491L;
	private int timeWindowSizeInMinute;
	private int[] slots;

	public DayModel(int timeWindowSizeInMinute, int numOfTemporalSlots) {
		this.timeWindowSizeInMinute = timeWindowSizeInMinute;
		slots = new int[numOfTemporalSlots];
	}
	
	public void setDensity(int[] slots) {
		this.slots = slots;
	}

	@Override
	public double getDensity(long time) {
		final int slot = findSlot(time);
		int end = findSlot(time + timeWindowSizeInMinute * SECONDS_OF_MINUTE);
		if (end >= slots.length)
			end = slots.length - 1;

		double density = 0.0;
		for (int i = slot; i <= end; i++) {
			density += slots[i];
		}
		return density;
	}

	@Override
	public void addResource(Resource resource) {
		int slot = findSlot(resource.getTime());
		slots[slot]++;
	}

	public int findSlot(long epochSecond) {
		ZonedDateTime dateTime = Utils.getTime(epochSecond);
		long offsetMinutes = (dateTime.getHour() - START) * MINUTES_OF_HOUR + dateTime.getMinute();
		return (int) (offsetMinutes / slotSizeInMinute());
	}
	
	public double slotSizeInMinute() {
		return (double) SPAN_IN_MINUTE / (double) slots.length;
	}
}
