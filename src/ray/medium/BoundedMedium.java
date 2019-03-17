package ray.medium;

import ray.math.Point3;
import ray.math.Vector3;
import ray.misc.Ray;

public abstract class BoundedMedium implements Medium{

	protected Point3 minPt = new Point3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	protected Point3 maxPt = new Point3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	
	public void setMinPt(Point3 minPt) { this.minPt = minPt; }
	public void setMaxPt(Point3 maxPt) { this.maxPt = maxPt; }

	/**
	 * Clip a ray to its intersection with the volume's bounding box.
	 * @param ray  the ray to be clipped; its start and end points are changed
	 * @return true if there is an intersection; if false ray is unchanged.
	 */
	boolean clipRay(Ray ray) {
	
		Point3 o = ray.origin;
		Vector3 d = ray.direction;
	
		double ox = o.x;
		double oy = o.y;
		double oz = o.z;
		double dx = d.x;
		double dy = d.y;
		double dz = d.z;
	
		double xMin = minPt.x;
		double yMin = minPt.y;
		double zMin = minPt.z;
		double xMax = maxPt.x;
		double yMax = maxPt.y;
		double zMax = maxPt.z;
	
		// a three-slab intersection test. We'll get in and out t values for
		// all three axes. For instance on the x axis:
		// o.x + t d.x = 1 => t = (1 - o.x) / d.x
		// o.x + t d.x = -1 => t = (-1 - o.x) / d.x
		// This code is straight from Shirley's section 10.9.1
	
		double tMin, tMax;
		if (dx >= 0) {
			tMin = (xMin - ox) / dx;
			tMax = (xMax - ox) / dx;
		}
		else {
			tMin = (xMax - ox) / dx;
			tMax = (xMin - ox) / dx;
		}
	
		double tyMin, tyMax;
		if (dy >= 0) {
			tyMin = (yMin - oy) / dy;
			tyMax = (yMax - oy) / dy;
		}
		else {
			tyMin = (yMax - oy) / dy;
			tyMax = (yMin - oy) / dy;
		}
		if (tMin > tyMax || tyMin > tMax)
			return false;
		if (tyMin > tMin)
			tMin = tyMin;
		if (tyMax < tMax)
			tMax = tyMax;
	
		double tzMin, tzMax;
		if (dz >= 0) {
			tzMin = (zMin - oz) / dz;
			tzMax = (zMax - oz) / dz;
		}
		else {
			tzMin = (zMax - oz) / dz;
			tzMax = (zMin - oz) / dz;
		}
		if (tMin > tzMax || tzMin > tMax)
			return false;
		if (tzMin > tMin)
			tMin = tzMin;
		if (tzMax < tMax)
			tMax = tzMax;
	
		if (tMin > ray.start) 
			ray.start = tMin;
		if (tMax < ray.end)
			ray.end = tMax;
		return true;
	}

}