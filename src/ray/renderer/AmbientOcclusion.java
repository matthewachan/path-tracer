package ray.renderer;

import ray.math.Geometry;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

/**
 * 
 * @author cxz (at Columbia)
 */
public class AmbientOcclusion implements Renderer {

    private double length = 0.1;
    
    public AmbientOcclusion() { }
    
    public void setLength(double d) {
        length = d;
    }
    
    /**
     * This method must be thread-safe
     * @see ray.renderer.Renderer#rayRadiance(ray.misc.Scene, ray.misc.Ray, ray.sampling.SampleGenerator, int, ray.misc.Color)
     */
    @Override
    public void rayRadiance(Scene scene, Ray ray, SampleGenerator sampler,
            int sampleIndex, Color outColor) {
        // find if the ray intersect with any surface
        IntersectionRecord iRec = new IntersectionRecord();
        
        if (scene.getFirstIntersection(iRec, ray)) {
            
            Point2 directSeed = new Point2();
            sampler.sample(1, sampleIndex, directSeed);     // this random variable is for incident direction
            
            // Generate a random incident direction
            Vector3 incDir = new Vector3();
            Geometry.squareToHemisphere(directSeed, incDir);
            iRec.frame.frameToCanonical(incDir);
            
            Ray shadowRay = new Ray(iRec.frame.o, incDir);
            shadowRay.makeOffsetRay();
            
            if ( !scene.getFirstIntersection(iRec, shadowRay) ) {
                outColor.set(0.8);
            } else {
                // determine the length of the shadow ray
                Vector3 exts = scene.getBoundingBoxExtents();
                if ( iRec.t > length * exts.length() ) 
                    outColor.set(0.8);
                else 
                    outColor.set(0.);
            }
            return;
        }
        
        scene.getBackground().evaluate(ray.direction, outColor);
    }

}
