package Extension;

import java.io.Serializable;

import DataParsing.Resource;

/**
 * 
 *
 */
public class MFModel implements TemporalModel, Serializable {
	private static final long serialVersionUID = 1795648067966564188L;
	Matrix mx;
	private long roadId;
	private int timeWindowSizeInMinute;

	public MFModel(Matrix matrix, int timeWindowSizeInMinute) {
		mx = matrix;
		this.timeWindowSizeInMinute = timeWindowSizeInMinute;
	}
	
	@Override
	public double getDensity(long time) {
		int row = mx.findRow(roadId);
		int column = mx.findColumn(time);
		int end = mx.findColumn(time + timeWindowSizeInMinute * TemporalModel.SECONDS_OF_MINUTE);

		double density = 0.0;
		for (int i = column; i <= end; i++) {
			density += mx.get(i, row);
		}
		return density;
	}

	@Override
	public void addResource(Resource resource) {
		if(mx instanceof MutableMatrix) {
			int column = mx.findColumn(resource.getTime());
			int row = mx.findRow(roadId);
			((MutableMatrix) mx).increment(column, row);
		}
	}

	public long getRoadId() {
		return roadId;
	}

	public void setRoadId(long roadId) {
		this.roadId = roadId;
	}

}
