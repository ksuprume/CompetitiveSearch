package Extension;

import java.util.ArrayList;
import java.util.List;

import COMSETsystem.Road;

/**
 * 
 *
 */
public class SetZone extends AbstractZone {
	private static final long serialVersionUID = -6928569470415887626L;
	List<Road> roads;

	public SetZone(TemporalModel model) {
		super(model);
		roads = new ArrayList<Road>();
	}

	public boolean add(Road road) {
		return roads.add(road);
	}

	@Override
	public List<Road> getRoads() {
		return roads;
	}

	@Override
	public boolean contains(Road road) {
		return roads.contains(road);
	}
}
