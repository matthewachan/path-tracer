package ray.medium;

import ray.math.Geometry;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;

public class IsotropicPhaseFunction implements PhaseFunction {

	static final double inv4Pi = 1/(4 * Math.PI);

	public void evaluate(Vector3 incDir, Vector3 reflDir, Color outValue) {

		outValue.set(inv4Pi, inv4Pi, inv4Pi);
	}

	public void generate(Vector3 fixedDir, Vector3 outDir, Point2 seed,
			Color outWeight) {

		Geometry.squareToSphere(seed, outDir);
		outWeight.set(1, 1, 1);
	}

	public double pdf(Vector3 fixedDir, Vector3 dir) {

		return inv4Pi;
	}

}
