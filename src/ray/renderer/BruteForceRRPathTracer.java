package ray.renderer;

import ray.misc.Color;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

import ray.brdf.BRDF;
import ray.material.Material;
import ray.math.Geometry;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.IntersectionRecord;

import ray.misc.LuminaireSamplingRecord;
import ray.material.Material;
import java.util.Random;

public class BruteForceRRPathTracer extends PathTracer {
	protected double survivalProbability = 0.5;
	protected Random rand = new Random();

	public void setSurvivalProbability(double val) {
		this.survivalProbability = val; 
		System.out.println("SET: " + survivalProbability);
	}

	/**
	 * @param scene
	 * @param ray
	 * @param sampler
	 * @param sampleIndex
	 * @param outColor
	 */
	protected void rayRadianceRecursive(Scene scene, Ray ray, 
			SampleGenerator sampler, int sampleIndex, int level, Color outColor) {
		// W4160 TODO (B)
		//
		// The function should be the same as BruteForcePathTracer *except* the termination 
		// condition. Here please use Russian Roulette to terminate the recursive call.
		// The survival probability of Russian Roulette is set in the XML file.

		IntersectionRecord iRec = new IntersectionRecord();

		if (scene.getFirstIntersection(iRec, ray)) {

			// Compute emitted light radiance
			Color emittedRadiance = new Color();
			Material surfaceMat = iRec.surface.getMaterial();

			if (surfaceMat.isEmitter()) {
				LuminaireSamplingRecord lRec = new LuminaireSamplingRecord();
				lRec.set(iRec);
				lRec.emitDir.set(ray.direction);
				lRec.emitDir.scale(-1);

				surfaceMat.emittedRadiance(lRec, emittedRadiance);
			}

			// Compute reflected light radiance
			Color reflectedRadiance = new Color();
			Vector3 outDir = new Vector3(ray.direction);

			gatherIlluminationRR(scene, outDir, iRec, sampler, sampleIndex, level, reflectedRadiance);	

			// Set the pixel color
			outColor.set(emittedRadiance);
			outColor.add(reflectedRadiance);
			return;
		}

		// If camera ray doesn't intersect a surface, pixel color is based on the scene background
		scene.getBackground().evaluate(ray.direction, outColor);
	}

	public void gatherIlluminationRR(Scene scene, Vector3 outDir, 
			IntersectionRecord iRec, SampleGenerator sampler, 
			int sampleIndex, int level, Color outColor) {

		// Russian roulette termination condition
		if (rand.nextDouble() < survivalProbability)
			return;

		// Sample random incident direction
		BRDF brdf = iRec.surface.getMaterial().getBRDF(iRec);
		Color brdfColor = new Color();
		Vector3 incDir = new Vector3();

		Point2 seed = new Point2();
		sampler.sample(sampleIndex, level, seed);     // this random variable is for incident direction

		// Increment the recursion level
		level += 1;

		brdf.generate(iRec.frame, outDir, incDir, seed, brdfColor);
		incDir.normalize();

		// Compute the reflection about the surface normal
		// Formula: v_ref = v_inc - 2 * (v_inc.dot(normal)) * normal
		Vector3 reflDir = new Vector3();
		reflDir.set(incDir);
		Vector3 nhat = new Vector3(iRec.frame.w);
		double dot = nhat.dot(incDir);
		nhat.scale(2 * dot);
		reflDir.sub(nhat);
		reflDir.normalize();

		// Evaluate the BRDF
		brdf.evaluate(iRec.frame, incDir, outDir, brdfColor);

		// Compute the probability density function
		double pdf = brdf.pdf(iRec.frame, incDir, outDir);

		// Find surface intersections with the incident direction
		Ray incRay = new Ray(iRec.frame.o, incDir);
		incRay.makeOffsetRay();

		IntersectionRecord incRecord = new IntersectionRecord();

		// Compute the emitted radiance of the intersected surface
		if (scene.getFirstIntersection(incRecord, incRay)) {

			Material surfaceMat = incRecord.surface.getMaterial();

			// Compute radiance directly emitted by surface
			Color directRadiance = new Color();

			if (surfaceMat.isEmitter()) {

				LuminaireSamplingRecord lRec = new LuminaireSamplingRecord();
				lRec.set(incRecord);
				lRec.emitDir.set(incDir);
				lRec.emitDir.scale(-1);


				surfaceMat.emittedRadiance(lRec, directRadiance);
			}

			// Compute radiance indirectly emitted by surface
			Color indirectRadiance = new Color();
			gatherIlluminationRR(scene, incDir, incRecord, sampler, sampleIndex, level, indirectRadiance);


			// Emitted radiance is the sum of direct and indirect radiance from incident direction
			outColor.set(directRadiance);
			outColor.add(indirectRadiance);
			// Scale by the amount of emitted light projected on the surface normal
			outColor.scale(iRec.frame.w.dot(incDir));


			outColor.scale(brdfColor);
			outColor.scale(Math.PI);
			outColor.scale(1 / (1 - survivalProbability));
		}

	}

}
