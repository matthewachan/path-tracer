package ray.medium;

import ray.math.Point3;
import ray.misc.Color;
import ray.misc.Ray;
import ray.misc.MediumSamplingRecord;

/**
 * @author srm
 *
 */
public interface Medium {

	/**
	 * Select a point along a ray segment by importance sampling the attenuation between
	 * the ray's start point and the chosen point.
	 * @param ray  The ray along which to choose a point.
	 * @param desiredAttenuation  The importance sampling seed
	 * @param mRec  Record to store output
	 * @return  True if a point was selected; false if we went past the end point.
	 */
	boolean selectDistance(Ray ray, double desiredAttenuation, MediumSamplingRecord mRec);
	
	/**
	 * Compute the attenuation through the medium between the ray's start and end points.
	 * @param ray  The ray; the start and end times determine the relevant segment.
	 * @param outAttenuation  The attenuation.
	 */
	void attenuation(Ray ray, Color outAttenuation);
	
	/**
	 * Query the medium's properties at a particular point.
	 * @param p  The point in the medium to query.
	 * @param mRec  Record to store results; irrelevant fields are set to defaults.
	 */
	void properties(Point3 p, MediumSamplingRecord mRec);
	
}
