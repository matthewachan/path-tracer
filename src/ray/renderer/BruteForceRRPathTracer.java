package ray.renderer;

import ray.misc.Color;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

public class BruteForceRRPathTracer extends PathTracer {
    protected double survivalProbability = 0.5;

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
    }

}
