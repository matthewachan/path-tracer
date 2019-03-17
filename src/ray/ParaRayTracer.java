/***********************************************************************
 * File              : ParaRayTracer.java
 * Author            : Changxi Zheng <cxz@cs.columbia.edu>
 * Date              : 02/19/2017
 * Last Modified Date: 03/19/2018
 * Last Modified By  : Changxi Zheng <cxz@cs.columbia.edu>
 ***********************************************************************/
package ray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ray.camera.Camera;
import ray.io.Parser;
import ray.math.Point2;
import ray.misc.Color;
import ray.misc.Image;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.renderer.Renderer;
import ray.sampling.SampleGenerator;
import ray.viewer.QuickViewer;


/**
 * A parallel ray tracer.
 *
 */
public class ParaRayTracer {
    
    //Turn the display window on or off
    public static final boolean DISPLAY = true;
    
    //Size of image sub-blocks
    protected static int SUB_WIDTH = 32;
    protected static int SUB_HEIGHT = 32;
        
    /**
     * Widget to draw the image spiral.
     */
    private static final Spiral spiral = new Spiral();
    
    /**
     * Useful little display window that shows rendering progress.
     * The window actually take a bit of time to render itself, so
     * you can turn it on or off by setting the DISPLAY flag at the
     * top of the file.
     */
    private static QuickViewer viewer = null;
    
    private static ExecutorService executor = null;
    /**
     * The main method takes all the parameters an assumes they are input files
     * for the ray tracer. It tries to render each one and write it out to a PNG
     * file named <input_file>.png.
     *
     * @param args
     */
    public static final void main(String[] args) {
        
        Parser parser = new Parser();
        
        int ctr = 0;
        int nThreads = Runtime.getRuntime().availableProcessors();
        
        System.out.println(args[0] + "  " + args[0].equals("-s"));
        if ( args.length > 2 && args[0].equals("-s") ) {
            nThreads = Integer.parseInt(args[1]);
            ctr += 2;
        }
        
        executor = Executors.newFixedThreadPool(nThreads);
        System.out.println("Render with " + nThreads + " threads.");

        // Get the input/output filenames.
        String inputFilename = args[ctr];
        String outputFilename = inputFilename + ".png";
            
        // Parse the input file
        Scene scene = (Scene) parser.parse(inputFilename, Scene.class);
        scene.init();
            
        long startTs = System.currentTimeMillis();
        
        // Render the scene
        renderImage(scene);
        
        executor.shutdown();
        
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("Fail the awaitTermination");
            e.printStackTrace();
        }
        
        //Output time (will be longer if the viewer is on)
        System.out.println("Done.");
        System.out.println("Total rendering time: "+((System.currentTimeMillis() - startTs) / 1000.0)+" seconds");
        
        // Write the image out
        System.out.println("Save PNG file in "+outputFilename);
        scene.getImage().write(outputFilename);
    }
    
    /**
     * The renderImage method renders the entire scene.
     *
     * @param scene The scene to be rendered
     */
    public static void renderImage(Scene scene) {
        
        // Get the output image
        Image image = scene.getImage();
        Camera cam = scene.getCamera();
        
        // Setup viewer
        if(DISPLAY) {
            if(viewer == null)
                viewer = QuickViewer.createImageViewer(image);
            else 
                viewer.setImage(image);
        } //else
        System.out.print("Starting render...");
        
        // Set the camera aspect ratio to match output image
        int width  = image.getWidth();
        int height = image.getHeight();
        cam.setAspectRatioKeepYFOV(((double) width) / height);
        
        //Setup the sub-block spiral
        spiral.initSubblockSpiral(width, height);
        
        int offsetX, offsetY, sizeX, sizeY;
        for(int i = 0; i < spiral.totalSubblocks; i++) {
            
            //Increment the block counter
            spiral.incrementSublockSpiral();
            offsetX = spiral.curSubX*SUB_WIDTH;
            offsetY = spiral.curSubY*SUB_HEIGHT;
            sizeX = Math.min(width-offsetX,SUB_WIDTH);
            sizeY = Math.min(height-offsetY,SUB_HEIGHT);
            
            if (DISPLAY)
                executor.execute(new BlockRenderer(scene, image, offsetX, offsetY, sizeX, sizeY, viewer));
            else
                executor.execute(new BlockRenderer(scene, image, offsetX, offsetY, sizeX, sizeY, null));
            
        }
        
    }
    
    private static final class BlockRenderer implements Runnable {
        
        private Scene scene = null;
        private Image outImg = null;
        private int offsetX, offsetY, sizeX, sizeY;
        private QuickViewer viewer;
        
        /*
         * @param scene The scene data
         * @param outImage the output image (write the output pixels here)
         * @param offsetX the startingX value of the block
         * @param offsetY the startingY value of the block
         * @param sizeX the width of the block
         * @param sizeY the height of the block
         */
        public BlockRenderer(Scene scene, Image outImg, 
                int offsetX, int offsetY, 
                int sizeX, int sizeY, QuickViewer viewer) {
            this.scene = scene;
            this.outImg = outImg;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.sizeX   = sizeX;
            this.sizeY   = sizeY;
            this.viewer  = viewer;
        }
        
        @Override
        public void run() {
            // Do some basic setup
            Renderer renderer = scene.getRenderer();
            Camera cam = scene.getCamera();
            SampleGenerator sampler = scene.getSampler().thread_safe_clone();
            Ray ray = new Ray();
            Color pixelColor = new Color();
            Color rayColor = new Color();
            int width = outImg.getWidth();
            int height = outImg.getHeight();
            Point2 pixelSeed = new Point2();
            
            for(int x = offsetX; x < (offsetX + sizeX); x++) {
                for(int y = offsetY; y < (offsetY + sizeY); y++) {
                    
                    sampler.generate();
                    
                    //Reset the pixel color
                    pixelColor.set(0,0,0);
                    for(int sampleIndex = 0; sampleIndex < sampler.getNumSamples(); sampleIndex++) {
                            
                        sampler.sample(0, sampleIndex, pixelSeed);
                            
                        //Generate the eye ray and shade that ray
                        cam.getRay(ray, (x + pixelSeed.x) / width, (y + pixelSeed.y) / height);
                        renderer.rayRadiance(scene, ray, sampler, sampleIndex, rayColor);
                        pixelColor.add(rayColor);
                    }
                    
                    //Gamma correct and clamp pixel values
                    pixelColor.scale(1.0 / sampler.getNumSamples());
                    pixelColor.gammaCorrect(2.2);
                    pixelColor.clamp(0, 1);
                    outImg.setPixelColor(pixelColor, x, y);
                }
            }

            // now we finished the rendering of this block
            //Update display
            if( viewer!= null )
                viewer.setImage(outImg, offsetX, offsetY, offsetX+sizeX, offsetY+sizeY);
        }
    }
    
    /**
     * Class wraps code that generates the spiral of image blocks
     * for rendering.
     */
    private static final class Spiral {
        
        //variables controlling ordering of sub-blocks
        private int numSubX;             //number of subblocks in x direction
        private int numSubY;             //number of subblocks in y direction
        protected int totalSubblocks;    //total number of subblocks to be computed in all passes
        protected int curSubX, curSubY;  //current subblock to request
        private int curDir;              //direction to next subblock
        private int movesLeft;           //moves left in this direction
        private int moveLength;          //total moves before next direction change
        
        //Constants defining directions of motion
        private static final int PLUSY = 0;
        private static final int PLUSX = 1;
        private static final int MINUSY = 2;
        private static final int MINUSX = 3;
        
        /**
         * Initialize the sub-block spiral counters (must be incremented once for valid block)
         */
        protected void initSubblockSpiral(int width, int height) {
            
            numSubX = ((width - 1) / RayTracer.SUB_WIDTH) + 1;
            numSubY = ((height - 1) / RayTracer.SUB_HEIGHT) + 1;
            totalSubblocks = numSubX * numSubY;
            curSubX = (numSubX / 2) - 1;
            curSubY = (numSubY / 2) - 2;
            curDir = PLUSY;
            movesLeft = 2;
            moveLength = 1;
        }
        
        /**
         * Increment the sub-block spiral one block
         */
        protected void incrementSublockSpiral() {
            
            do {
                if (movesLeft == 0) { //time to change direction
                    curDir++;
                    if (curDir > MINUSX)
                        curDir = PLUSY;
                    if ((curDir == PLUSY) || (curDir == MINUSY))
                        moveLength++;
                    movesLeft = moveLength;
                }
                if (curDir == PLUSY)
                    curSubY++;
                else if (curDir == PLUSX)
                    curSubX++;
                else if (curDir == MINUSY)
                    curSubY--;
                else if (curDir == MINUSX)
                    curSubX--;
                movesLeft--;
            } while (curSubX < 0 || curSubY < 0 || curSubX >= numSubX || curSubY >= numSubY);
        }
    }
}
