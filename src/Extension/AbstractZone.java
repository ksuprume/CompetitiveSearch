package Extension;

import java.io.Serializable;

import DataParsing.Resource;

/**
 * Basic implementation of Zone interface
 *
 */
public abstract class AbstractZone implements Zone, Serializable {
	private static final long serialVersionUID = -3426217762832323776L;
	protected TemporalModel model;

	protected AbstractZone(TemporalModel model) {
		this.model = model;
	}

	@Override
	public double getDensity(long time) {
		return model.getDensity(time);
	}

	@Override
	public void addResource(Resource resource) {
		model.addResource(resource);
	}
}
