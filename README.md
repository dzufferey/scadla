# Scadla: a scala library for constructive solid geometry

Scadla is a small library to create 3D models.
Those model can then, for instance, be 3D printed.
Scadla is very much influence by OpenSCAD and has the same goal of being a _programmer's CAD_ tool.
The name scadla is a portmanteau of scad and scala.


Why scadla? OpenSCAD is doing a pretty good job and is becoming a de facto standard in the maker community.

That is true. OpenSCAD is usually doing a pretty fine job and we currently use it in the back end.
However, there is a few things that, IMO, OpenSCAD did not get right or missing features that a good general purpose programming language, like scala, gives you for free.
So instead of developing a new language, scadla simply embed CSG into an existing language.
The main points on which we try to improve are:

* modularity/reusability, package/namespace, toolchain support, etc. IMO OpenSCAD does not scale very well to projects across multiple files. The `use` vs `include` is very coarse grained. The absence of namespace gives rise to very long variable names in order to avoid clash. Fortunately, you get those things for free in scala. Also the toolchain is more mature. For instance, one can use maven to share libraries of objects and then just specify in project configuration which library to use and sbt will download them automatically, add them to the classpath, ...

* _first class objects_ An OpenSCAD program roughly build a tree with primitive objects as leaves and CSG operations as internal nodes. However, that tree is built implicitly and one cannot access it directly. In scadla, that tree is just an algebraic datatype. This means that you can reference it, e.g., `val u = Union(Cube(...), Sphere(...))` and also inspect/modify it using pattern matching, e.g., `u match { case Union(...) => ... }`

* variables evaluation and other small quirks

* all the angles are in _radians_, not in degrees. Because ... math! If you still prefer degrees you can write `math.toRadians(α)`.

* a small caveat: OpenSCAD does has only floating points number but scala makes the distinction between integers and floating points. For instance, `1/2` is the integer division and returns `0`, not `0.5`. If you want floating point numbers write `1/2.0` or `1.0/2`.

## Dependencies

For the moment, scadla uses
* required: Java 8 with JavaFX/OpenJFX
* recommended: [OpenSCAD](http://www.openscad.org/) for the CSG operations
* optional: [Meshlab](http://meshlab.sourceforge.net/) to display the models.
    Scadla has a built-in model viewer, but it is quite limited.


## Examples

For examples, please look in the [example](src/main/scala/dzufferey/scadla/examples) folder.

For a complete example, you can head to [MecanumWheel.scala](src/main/scala/dzufferey/scadla/examples/MecanumWheel.scala) for a parametric design of an omnidirectional wheel.

The primitives and CSG operations are defined in [Solid.scala](src/main/scala/dzufferey/scadla/Solid.scala).
This gives you a basic verbose syntax.
For instance, a cube written `cube([1,2,1])` in OpenSCAD is written `Cube(1,2,1)` in scadla.

Let us consider the following example:
```scala
Intersection(
  Union(
    Cube(1,1,1),
    Translate( -0.5, -0.5, 0, Cube(1,1,1))
  ),
  Sphere(1.5)
)
```
In the most verbose form, it corresponds to the full CSG tree.
However, we can make it prettier.
First, we can store `Solid`s in variables and reuse them:
```scala
val c = Cube(1,1,1)
val s = Sphere(1.5)
val u = Union(c, Translate( -0.5, -0.5, 0, c))
Intersection(u, s)
```
Next, we can replace the operation with a less verbose syntax using `import InlineOps._`
```scala
val c = Cube(1,1,1)
val s = Sphere(1.5)
(c + c.move( -0.5, -0.5, 0)) * s
```

Once, we have the descprition of the object we want to make, we need to evaluate the CSG tree to get a 3D model.
We currently use OpenSCAD for that.
See [OpenSCAD.scala](src/main/scala/dzufferey/scadla/backends/OpenSCAD.scala) for the details.

Assuming that we the object we are interested in is stored in the `obj` variable.
We can do a few things:
* evaluate the tree and save the result in a .stl file with `OpenSCAD.toSTL(obj, file_name)`
* view the result `OpenSCAD.view(obj)` (this requires meshlab)
* evaluate the tree and get back a Polyhedron with `OpenSCAD.getResult(obj)`

By default, scadla will run openscad with `$fa=4; $fs=0.5;`, so complex designs can take a while to render.
You can change that by giving the appropriate arguments to the different methods (see `OpenSCAD.scala` for the details)

To run this example execute `sbt run` and select `dzufferey.scadla.examples.MecanumWheel`.


## Compiling and Using it

You can build scadla using [sbt](http://www.scala-sbt.org/).
To install sbt follow the instructions at [http://www.scala-sbt.org/release/tutorial/Setup.html](http://www.scala-sbt.org/release/tutorial/Setup.html).

Then, in a console, execute:
```
$ sbt
> compile
```

To try some examples execute `sbt run` and select one example.

Currently scadla is not yet published in an online maven repository.
If you want to use it in another project, run `sbt publishLocal` to make it available to other projects on the same machine. You can include it in your projects by adding `libraryDependencies += "io.github.dzufferey" %% "scadla" % "0.1-SNAPSHOT"` in your `build.sbt`.


## ToDo

Features that may (or may not) be implemented, depending on time and motivation:

* features
  - geometry shader (similar to computer graphics) to modify the surface of objects, e.g., adding a pattern to a flat surface.
  - implicit surfaces, e.g., bezier, nurbs, metaballs.
    * rendering using [marching cubes](https://en.wikipedia.org/wiki/Marching_cubes), http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.56.7139, http://users.polytech.unice.fr/~lingrand/MarchingCubes/algo.html, http://link.springer.com/article/10.1007%2FBF01900830
    * It could be fun to try to implement that in OpenCL using [ScalaCL](https://github.com/nativelibs4java/ScalaCL)
    * use adaptative sampling instead of a fix grid
    * an alternative to the marching cubes is "sonstrained elastic surface nets" see [JSurfaceNets](https://github.com/miho/JSurfaceNets)
  - modifier: `Mod(fct: Point => Point, s: Solid) extends Solid`
  - more operations
    * chamfer
    * making object smaller (negative minkowski sum): instead of adding some tolerance to all dimension, design your object at the right size, the make them a bit smaller so they fit together. The goal is to move the face parallel to their normal by some amount while keeping the mesh well-formed.
  - FromFile changed to FromURL
  - a `Solidifiable` trait that has a `def solidify: Solid` operation which can be called implicitly
  - object cache:
    * object
      - normalized non-polyhedron AST (keep only bool-ops, other operations are cheap)
      - scadla version
      - renderer
      - created
      - last accessed
      - how many times accessed
      - filename (points twoard some stl.gz/bz2/xz)
    * prefs in home as .scadla folder
      - alternate file/folder locations
      - cache size
      - eviction policy (least recently used, oldest, least frequently used)
      - index
      - dir for the cached objects

* implementation
  - file format:
    * PLY parser (this format is evil!!!)
  - try VTK as backend
    * installation [http://www.vtk.org/Wiki/VTK/Configure_and_Build]()
    * how to decide whether or not to compile VTK: it cannot be resolved as a MVN dependency but only a a local one ...
      - split the project into (1) core, (2) renderers, (3) examples
    * boolean operation [http://hdl.handle.net/10380/3262]()
    * transform [http://www.vtk.org/doc/nightly/html/classvtkTransform.html]()
    * convex hull [https://cmake.org/Wiki/VTK/Examples/Boneyard/Cxx/PolyData/ConvexHullDelaunay3D]()
    * minkowski sum ???
  - backend: decomposition as a DAG and to do the rendering in parallel.
     generalize renderer into `Renderer[A]` with
      * `render(s: Solid, renderedChildren: Seq[A]): A`
      * `postprocess(s: A): Polyhedron`
  - built-in model viewer
    * choosing window size according to screen size, and allow resizing
    * moving the point of view

* GUI
 - try to avoid the serialization when launching the GUI
 - more robust way to transfer data between the main program and the GUI

* Assemblies: group of object linked by joints (can move), the goal is to make it easier to visualized larger projects and make sure everything fits/move properly
  - better notation for the connections
  - joint: what about time transformers (for min-max travel)
  - part: rendered part (visibility and serialization)
  - GUI: sliders for time and expansion

* Documentation and tutorials

* ...

Also, pull requests are welcome.

