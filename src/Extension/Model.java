package Extension;

import DataParsing.Resource;

/**
 * Common interface
 * 
 *
 */
public interface Model {
	/**
	 * Get density given time
	 * 
	 * @param time
	 * @return
	 */
	double getDensity(long time);

	/**
	 * Add resource. Only used when learning
	 * 
	 * @param resource
	 */
	void addResource(Resource resource);
}
