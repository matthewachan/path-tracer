package ray.misc;

import ray.math.Point3;
import ray.medium.PhaseFunction;

public class MediumSamplingRecord {
	
	/** The point that was selected to compute a volume interaction. */
	public Point3 interactionPoint = new Point3();
	
	/** The attenuation from the start of the ray to that point. */
	public Color attenuation = new Color();
	
	/** The probability density with which the point was chosen. */
	public double pdf;
	
	/** The medium's absorption coefficient at the interaction point. */
	public Color absorptionCoefficient = new Color();

	/** The medium's scattering coefficient at the interaction point. */
	public Color scatteringCoefficient = new Color();

	/** The medium's phase function at the interaction point. */
	public PhaseFunction phaseFunction = null;

}
