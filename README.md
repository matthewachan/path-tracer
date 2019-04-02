# Path tracing
> Path tracer that renders photorealistic images

## Gallery

![](scene/dragon_rr.png)
![](scene/chess_ambient.png)

## Usage

To render an image, simply provide an XML file describing the scene (lighting, materials, meshes, camera, renderer, etc.) as input. There are example XML files in the `scene` directory.

```sh
// Build project
$ ant
// Render an image using an input XML file
$ java -cp carbine.jar ray.ParaRayTracer scene/cbox-global.xml
```

## Scenes

In addition to the test Cornell box scenes (and a sphere rendering test), I have created two additional *"creative"* scenes.

The first scene is `dragon.xml` which has the Cornell dragon placed in an empty Cornell box with two spheres of light for illumination. The `.obj` files (and converted `.msh` equivalents) are included in the `scene` directory.

The second scene is `chess.xml` which has a chessboard and chess pieces placed in a Cornell box. The only light in the scene is the square ceiling light.

## Report

There are four implemented renderers: ambient occlusion, direct illumination, brute force path tracing, and Russian roulette path tracing. All of the renderers have been fully-implemented.

**Note**: My implementation of `rayRadianceRecursive()` and `gatherIllumination()` for the brute force path tracer (and the Russian roulette path tracer) do not use mutual recursion. Instead, the recursive computation of reflected radiance is computed in `gatherIllumination()`.
