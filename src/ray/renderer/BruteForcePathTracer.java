package ray.renderer;

import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

import ray.misc.LuminaireSamplingRecord;
import ray.material.Material;

public class BruteForcePathTracer extends PathTracer {
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
        // Find the visible surface along the ray, then add emitted and reflected radiance
        // to get the resulting color.
    	//
    	// If the ray depth is less than the limit (depthLimit), you need
    	// 1) compute the emitted light radiance from the current surface if the surface is a light surface
    	// 2) reflected radiance from other lights and objects. You need recursively compute the radiance
    	//    hint: You need to call gatherIllumination(...) method.
	

	// Hard stop on recursion depth
	if (level > depthLimit)
		return;

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

		gatherIllumination(scene, outDir, iRec, sampler, sampleIndex, level, reflectedRadiance);	

		// Set the pixel color
		outColor.set(emittedRadiance);
		outColor.add(reflectedRadiance);
		return;
	}

	// If camera ray doesn't intersect a surface, pixel color is based on the scene background
	scene.getBackground().evaluate(ray.direction, outColor);

    }

}
