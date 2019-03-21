package ray.renderer;

import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;
import ray.misc.IntersectionRecord;

import ray.material.Material;
import ray.misc.LuminaireSamplingRecord;
import ray.renderer.ProjSolidAngleIlluminator;
import ray.renderer.DirectIlluminator;
import ray.math.Point2;

/**
 * A renderer that computes radiance due to emitted and directly reflected light only.
 * 
 * @author cxz (at Columbia)
 */
public class DirectIlluminationRenderer implements Renderer {
    
    /**
     * This is the object that is responsible for computing direct illumination.
     */
    DirectIlluminator direct = null;
        
    /**
     * The default is to compute using uninformed sampling wrt. projected solid angle over the hemisphere.
     */
    public DirectIlluminationRenderer() {
        this.direct = new ProjSolidAngleIlluminator();
    }
    
    
    /**
     * This allows the rendering algorithm to be selected from the input file by substituting an instance
     * of a different class of DirectIlluminator.
     * @param direct  the object that will be used to compute direct illumination
     */
    public void setDirectIlluminator(DirectIlluminator direct) {
        this.direct = direct;
    }

    
    public void rayRadiance(Scene scene, Ray ray, SampleGenerator sampler, int sampleIndex, Color outColor) {
        // W4160 TODO (A)
    	// In this function, you need to implement your direct illumination rendering algorithm
    	//
    	// you need:
    	// 1) compute the emitted light radiance from the current surface if the surface is a light surface
    	// 2) direct reflected radiance from other lights. This is by implementing the function
    	//    ProjSolidAngleIlluminator.directIlluminaiton(...), and call direct.directIllumination(...) in this
    	//    function here.
	
	IntersectionRecord iRec = new IntersectionRecord();
	Color emittedRadiance = new Color();
	Color reflectedRadiance = new Color();

	// Check if the camera ray intersects an object in the scene
	if (scene.getFirstIntersection(iRec, ray)) {

		// Compute emitted radiance
		emittedRadiance(iRec, ray.direction, emittedRadiance);

		// Compute reflected radiance
		Vector3 incDir = new Vector3();
		Vector3 outDir = new Vector3();

		Point2 directSeed = new Point2();
		sampler.sample(1, sampleIndex, directSeed);     // this random variable is for incident direction

		direct.directIllumination(scene, incDir, outDir, iRec, directSeed, reflectedRadiance);

		// Sum the radiances
		outColor.set(emittedRadiance);
		outColor.add(reflectedRadiance);
		return;
	}

	// If camera ray doesn't intersect, pixel color is based on the scene background
        scene.getBackground().evaluate(ray.direction, outColor);
    }

    
    /**
     * Compute the radiance emitted by a surface.
     * @param iRec      Information about the surface point being shaded
     * @param dir          The exitant direction (surface coordinates)
     * @param outColor  The emitted radiance is written to this color
     */
    protected void emittedRadiance(IntersectionRecord iRec, Vector3 dir, Color outColor) {
    	// W4160 TODO (A)
        // If material is emitting, query it for emission in the relevant direction.
        // If not, the emission is zero.
    	// This function should be called in the rayRadiance(...) method above
	
	// Check if the intersected surface emits light
	Material surfaceMat = iRec.surface.getMaterial();

	if (surfaceMat.isEmitter()) {

		// Compute the radiance of the emitted light
		LuminaireSamplingRecord lRec = new LuminaireSamplingRecord();
		lRec.set(iRec);
		lRec.emitDir.set(dir);
		lRec.emitDir.scale(-1);

		surfaceMat.emittedRadiance(lRec, outColor);
		return;
	}
	
	// Radiance is 0, if the surface is not an emitter
	outColor.set(0.);
    }
}
