package ray.renderer;

import ray.brdf.BRDF;
import ray.material.Material;
import ray.math.Geometry;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

import ray.misc.LuminaireSamplingRecord;
import ray.material.Material;

public abstract class PathTracer extends DirectIlluminationRenderer {

	protected int depthLimit = 5;
	protected int backgroundIllumination = 1;

	public void setDepthLimit(int depthLimit) { this.depthLimit = depthLimit; }
	public void setBackgroundIllumination(int backgroundIllumination) { this.backgroundIllumination = backgroundIllumination; }

	@Override
	public void rayRadiance(Scene scene, Ray ray, SampleGenerator sampler, int sampleIndex, Color outColor) {

		rayRadianceRecursive(scene, ray, sampler, sampleIndex, 0, outColor);
	}

	protected abstract void rayRadianceRecursive(Scene scene, Ray ray, SampleGenerator sampler, int sampleIndex, int level, Color outColor);

	public void gatherIllumination(Scene scene, Vector3 outDir, 
			IntersectionRecord iRec, SampleGenerator sampler, 
			int sampleIndex, int level, Color outColor) {
		// W4160 TODO (B)
		//
		// This method computes a Monte Carlo estimate of reflected radiance due to direct and/or indirect 
		// illumination.  It generates samples uniformly wrt. the projected solid angle measure:
		//
		//    f = brdf * radiance
		//    p = 1 / pi
		//    g = f / p = brdf * radiance * pi
		// You need: 
		//   1. Generate a random incident direction according to proj solid angle
		//      pdf is constant 1/pi
		//   2. Recursively find incident radiance from that direction
		//   3. Estimate the reflected radiance: brdf * radiance / pdf = pi * brdf * radiance
		//
		// Here you need to use Geometry.squareToPSAHemisphere that you implemented earlier in this function

		// Hard stop on recursion depth
		// if (level > depthLimit)
		// 	return;

		// Sample random incident direction
		BRDF brdf = iRec.surface.getMaterial().getBRDF(iRec);
		Color brdfColor = new Color();
		Vector3 incDir = new Vector3();

		Point2 seed = new Point2();
		sampler.sample(sampleIndex,level, seed);     // this random variable is for incident direction

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
			gatherIllumination(scene, incDir, incRecord, sampler, sampleIndex, level, indirectRadiance);


			// Emitted radiance is the sum of direct and indirect radiance from incident direction
			outColor.set(directRadiance);
			outColor.add(indirectRadiance);
			// Scale by the amount of emitted light projected on the surface normal
			outColor.scale(iRec.frame.w.dot(incDir));


			outColor.scale(brdfColor);
			outColor.scale(Math.PI);
		}

	}
}

