package ray.renderer;

import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Scene;

import ray.math.Geometry;
import ray.brdf.BRDF;
import ray.material.Material;
import ray.misc.Ray;
import ray.misc.LuminaireSamplingRecord;

/**
 * This class computes direct illumination at a surface by the simplest possible approach: it estimates
 * the integral of incident direct radiance using Monte Carlo integration with a uniform sampling
 * distribution.
 * 
 * The class has two purposes: it is an example to serve as a starting point for other methods, and it
 * is a useful base class because it contains the generally useful <incidentRadiance> function.
 * 
 * @author Changxi Zheng (at Columbia)
 */
public class ProjSolidAngleIlluminator extends DirectIlluminator {
    
    
    public void directIllumination(Scene scene, Vector3 incDir, Vector3 outDir, 
            IntersectionRecord iRec, Point2 seed, Color outColor) {
        // W4160 TODO (A)
    	// This method computes a Monte Carlo estimate of reflected radiance due to direct illumination.  It
        // generates samples uniformly wrt. the projected solid angle measure:
        //
        //    f = brdf * radiance
        //    p = 1 / pi
        //    g = f / p = brdf * radiance * pi
        //
        // The same code could be interpreted as an integration wrt. solid angle, as follows:
        //
        //    f = brdf * radiance * cos_theta
        //    p = cos_theta / pi
        //    g = f / p = brdf * radiance * pi
    	// 
    	// As a hint, here are a few steps when I code this function
    	// 1. Generate a random incident direction according to proj solid angle
        //    pdf is constant 1/pi
    	// 2. Find incident radiance from that direction
    	// 3. Estimate reflected radiance using brdf * radiance / pdf = pi * brdf * radiance

	Color brdfColor = new Color();
	Color radiance = new Color();	

	BRDF brdf = iRec.surface.getMaterial().getBRDF(iRec);

	// Sample random incident direction
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
	brdf.evaluate(iRec.frame, incDir, reflDir, brdfColor);

	// Compute the probability density function
	double pdf = brdf.pdf(iRec.frame, incDir, outDir);

	// Find surface intersections with the incident direction
	Ray incRay = new Ray(iRec.frame.o, incDir);
	incRay.makeOffsetRay();

	IntersectionRecord incRecord = new IntersectionRecord();

	// Compute the emitted radiance of the intersected surface
	if (scene.getFirstIntersection(incRecord, incRay)) {

		Material surfaceMat = incRecord.surface.getMaterial();
		if (surfaceMat.isEmitter()) {

			LuminaireSamplingRecord lRec = new LuminaireSamplingRecord();
			lRec.set(incRecord);
			lRec.emitDir.set(incDir);
			lRec.emitDir.scale(-1);


			surfaceMat.emittedRadiance(lRec, radiance);
			// Scale by the amount of emitted light projected on the surface normal
			radiance.scale(iRec.frame.w.dot(incDir));

			outColor.set(brdfColor);
			outColor.scale(radiance);
			outColor.scale(Math.PI);
			return;
		}

	}

	// If no emitter is intersected, set the color to 0
	outColor.set(0.);
    }
}
