package ray.medium;

import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;

public interface PhaseFunction {

	
	/**
	 * Evaluate the phase function.  Must be reciprocal (invariant to swapping arguments).
	 * @param incDir The incident direction
	 * @param reflDir The reflected direction
	 * @param outValue The value of the phase function
	 */
	public void evaluate(Vector3 incDir, Vector3 reflDir, Color outValue);
	
	/**
	 * Generate a sample on the hemisphere drawn from a distribution suitable for 
	 * importance sampling this phase function.  The probability density is reported with respect
	 * to the solid angle measure.
	 * @param fixedDir The fixed argument of the phase function
	 * @param outDir The random sample for the variable argument
	 * @param outWeight The weight for an unbiased estimate (value / pdf)
	 */
	public void generate(Vector3 fixedDir, Vector3 outDir, Point2 seed, Color outWeight);
	
	/**
	 * Evaluate the PDF used by generate(), with respect to the solid angle measure.
	 * @param fixedDir The fixed argument of the phase function
	 * @param dir The variable argument
	 * @return The pdf of generate() choosing <dir> if given <fixedDir>
	 */
	public double pdf(Vector3 fixedDir, Vector3 dir);

}
