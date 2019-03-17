package ray.medium;

import ray.math.Point3;
import ray.misc.Color;
import ray.misc.Ray;
import ray.misc.MediumSamplingRecord;

/**
 * @author srm
 *
 * A medium in which the interaction coefficients and the phase function are constant.
 */
public class HomogeneousMedium extends BoundedMedium {
    
    Color absorptionCoefficient = new Color(0, 0, 0);
    Color scatteringCoefficient = new Color(0, 0, 0);
    PhaseFunction phaseFunction = new IsotropicPhaseFunction();

    // derived from absorption and scattering coefficients
    Color attenuationCoefficient = new Color(0, 0, 0);
    private void updateAttenuation() {
        attenuationCoefficient.set(absorptionCoefficient);
        attenuationCoefficient.add(scatteringCoefficient);        
    }

    // For the benefit of the parser
    public void setAbsorptionCoefficient(Color absorptionCoefficient) {
        this.absorptionCoefficient = absorptionCoefficient;
        updateAttenuation();
    }

    // For the benefit of the parser
    public void setScatteringCoefficient(Color scatteringCoefficient) {
        this.scatteringCoefficient = scatteringCoefficient;
        updateAttenuation();
    }

    // For the benefit of the parser
    public void setPhaseFunction(PhaseFunction phaseFunction) {
        this.phaseFunction = phaseFunction;
    }


    public void attenuation(Ray ray, Color outAttenuation) {
        
        // For a homogeneous medium the attenuation is exponential 
        // in the length of path inside the volume.
        
        Ray volRay = new Ray(ray);
        if (clipRay(volRay)) {
            assert Math.abs(volRay.direction.length()-1.) < 1e-10;
            double length = volRay.end - volRay.start;
            //assert length >= 0 : "Length = "+length;
            outAttenuation.set(
            		Math.exp(-attenuationCoefficient.r * length),
                    Math.exp(-attenuationCoefficient.g * length),
                    Math.exp(-attenuationCoefficient.b * length));
        } else {
            outAttenuation.set(1, 1, 1);
        }
    }

    
    public void properties(Point3 p, MediumSamplingRecord volRec) {
        
        // The properties don't depend on p.
        
        volRec.interactionPoint.set(p);
        volRec.attenuation.set(0, 0, 0);
        volRec.pdf = 0;
        
        volRec.absorptionCoefficient.set(absorptionCoefficient);
        volRec.scatteringCoefficient.set(scatteringCoefficient);
        volRec.phaseFunction = phaseFunction;
    }

    
    public boolean selectDistance(Ray ray, double seed, MediumSamplingRecord mRec) {
        double sigma_t = attenuationCoefficient.channelAvg();

        Ray volRay = new Ray(ray);
        if ( clipRay(volRay) ) {
            double d = -Math.log(seed) / sigma_t;
            assert Math.abs(volRay.direction.length()-1.) < 1e-10;
            double rayLength = volRay.end - volRay.start;
            //assert rayLength >= 0;

            if ( d < rayLength ) {
                volRay.evaluate(mRec.interactionPoint, volRay.start + d);
                mRec.attenuation.set(
                        Math.exp(-attenuationCoefficient.r * d),
                        Math.exp(-attenuationCoefficient.g * d),
                        Math.exp(-attenuationCoefficient.b * d));
                mRec.pdf = seed * sigma_t;
                mRec.absorptionCoefficient.set(absorptionCoefficient);
                mRec.scatteringCoefficient.set(scatteringCoefficient);
                mRec.phaseFunction = phaseFunction;
                return true;
            }
        }
        return false;
    }
    /*
    public boolean selectDistance(Ray ray, double seed, MediumSamplingRecord mRec) {
        
        double sigma_t = attenuationCoefficient.channelAvg();

        // The attenuation of the ray segment is the probability that the ray
        // passes through unimpeded.  So with probability equal to the attenuation,
        // we sample the surface behind the ray (i.e. we return false).  Otherwise
        // we generate a sample point wrt. a uniform distribution along the ray
        // segment.
        Ray volRay = new Ray(ray);
        if (clipRay(volRay)) {
            double rayLength = (volRay.end - volRay.start) * volRay.direction.length();
            double volumeProb = 1 - Math.exp(-sigma_t * rayLength); // 1 - attenuation
            if (seed < volumeProb) {
                seed = seed / volumeProb; // uniformly select a point
                double t = (1 - seed) * volRay.start + seed * volRay.end;
                double distance = (t - volRay.start) * volRay.direction.length();
                volRay.evaluate(mRec.interactionPoint, t);
                mRec.attenuation.set(Math.exp(-attenuationCoefficient.r * distance),
                        Math.exp(-attenuationCoefficient.g * distance),
                        Math.exp(-attenuationCoefficient.b * distance));
                mRec.pdf = volumeProb / rayLength;
                mRec.absorptionCoefficient.set(absorptionCoefficient);
                mRec.scatteringCoefficient.set(scatteringCoefficient);
                mRec.phaseFunction = phaseFunction;
                return true;
            } else {            
                return false;
            }
        } else {
            return false;
        }
    }
    */

}
