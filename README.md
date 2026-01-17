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

* _type-safe units_ with the [squants](https://github.com/typelevel/squants) library. Let the compiler warns you when you try to rotate a object by 10mm. To keep a lightweight syntax `EverythingIsIn` provides implicit conversions from numbers to length and angles. For instance, add `import scadla.EverythingIsIn.{millimeters, radians}` in your file to work use millimeters and radians by default.

* a small caveat: OpenSCAD does has only floating points number but scala makes the distinction between integers and floating points. For instance, `1/2` is the integer division and returns `0`, not `0.5`. If you want floating point numbers write `1/2.0` or `1.0/2`.


## Additional Backends and Operations

Scadla core operations are based on CSG.
However, most recent CAD systems uses boundary representation which offers a wider range of operations.
There is new backend for scadla based on BREP in development at https://github.com/dzufferey/scadla-oce-backend/.


## Dependencies

For the moment, scadla uses
* required: Java 8 or above
* recommended: [OpenSCAD](http://www.openscad.org/) for the CSG operations
* optional: [Meshlab](http://meshlab.sourceforge.net/) to display the models.
  Scadla can try to display the model using [X3Dom](https://www.x3dom.org/) but it is currently quite limited and requires an internet connection.


## Examples

For examples, please look in the [example](src/main/scala/scadla/examples) folder.

For a complete example, you can head to [MecanumWheel.scala](src/main/scala/scadla/examples/MecanumWheel.scala) for a parametric design of an omnidirectional wheel.

The primitives and CSG operations are defined in [Solid.scala](src/main/scala/scadla/Solid.scala).
This gives you a basic verbose syntax.
For instance, a cube written `cube([1,2,1])` in OpenSCAD is written `Cube(1 mm, 2 mm, 1 mm)` in scadla.
Scadla has explicits units.
In that case, we use millimeters.

Let us consider the following example:
```scala
Intersection(
  Union(
    Cube(1 mm, 1 mm, 1mm),
    Translate(-0.5 mm, -0.5 mm, 0 mm, Cube(1 mm, 1 mm, 1 mm))
  ),
  Sphere(1.5 mm)
)
```
In the most verbose form, it corresponds to the full CSG tree.
However, we can make it prettier.
First, we can store `Solid`s in variables and reuse them:
```scala
val c = Cube(1 mm, 1 mm, 1 mm)
val s = Sphere(1.5 mm)
val u = Union(c, Translate(-0.5 mm, -0.5 mm, 0 mm, c))
Intersection(u, s)
```
Next, we can replace the operation with a less verbose syntax using `import InlineOps.*`
```scala
val c = Cube(1 mm, 1 mm, 1 mm)
val s = Sphere(1.5 mm)
(c + c.move(-0.5 mm, -0.5 mm, 0 mm)) * s
```
To avoid putting `mm` everywhere, it is possible to use implicit conversion with `import scadla.EverythingIsIn.{millimeters, radians}`
```scala
val c = Cube(1, 1, 1)
val s = Sphere(1.5)
(c + c.move(-0.5, -0.5, 0)) * s
```
The compiler will interpret numeric constant as millimeters when used as length and as radians for angles.
It is also possible to use inches instead of millimeters and degrees instead of radians.
Beware, when using the implicit conversions as it can be unpredictable.

Once, we have the description of the object we want to make, we need to evaluate the CSG tree to get a 3D model.
We currently use OpenSCAD for that.
See [OpenSCAD.scala](src/main/scala/scadla/backends/OpenSCAD.scala) for the details.

Assuming that we the object we are interested in is stored in the `obj` variable.
We can do a few things:
* evaluate the tree and save the result in a .stl file with `OpenSCAD.toSTL(obj, file_name)`
* view the result `OpenSCAD.view(obj)` (this requires meshlab)
* evaluate the tree and get back a Polyhedron with `OpenSCAD.getResult(obj)`

By default, scadla will run openscad with `$fa=4; $fs=0.5;`, so complex designs can take a while to render.
You can change that by giving the appropriate arguments to the different methods (see `OpenSCAD.scala` for the details)

To run this example execute `sbt run` and select `scadla.examples.MecanumWheel`.


## Compiling and Using it

You can build scadla using [sbt](http://www.scala-sbt.org/).
To install sbt follow the instructions at [http://www.scala-sbt.org/release/tutorial/Setup.html](http://www.scala-sbt.org/release/tutorial/Setup.html).

Then, in a console, execute:
```
$ sbt
> compile
```

To try some examples execute `sbt run` and select one example.

If you want to use it in another project, you need to either
* add to your `build.sbt`:
  ```
  resolvers += "jitpack" at "https://jitpack.io"

  libraryDependencies += "com.github.dzufferey" %% "scadla" % "master-SNAPSHOT"
  ```
* run `sbt publishLocal` in this folder on the same machine you are building your project.

## JupyterLab

It is possible to use scadla with [JupyterLab](https://jupyter.org/index.html).
First you need to install [Almond](https://almond.sh/).
Then, look in [sample.ipynb](doc/sample.ipynb) to see how it work.


## Contributors

* Damien Zufferey (basic infrastrucure)
* Jan Ypma (adding type-safe units)

## ToDo

Features that may (or may not) be implemented, depending on time and motivation:

* features
  - geometry shader (similar to computer graphics) to modify the surface of objects, e.g., adding a pattern to a flat surface.
  - implicit surfaces, e.g., bezier, nurbs, metaballs.
    * rendering using [marching cubes](https://en.wikipedia.org/wiki/Marching_cubes), http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.56.7139, http://users.polytech.unice.fr/~lingrand/MarchingCubes/algo.html, http://link.springer.com/article/10.1007%2FBF01900830
    * an alternative to the marching cubes is "constrained elastic surface nets" see [JSurfaceNets](https://github.com/miho/JSurfaceNets)
  - change FromFile to FromURL
  - backend specific operations: implicit surfaes, chamfer, minkowski, etc. can be easier or harder to do depending on the backend.
    We need to keep a core of operation which is supported by all backend and then other operations that may be backend specific.
    The simplest is to "unseal" the `Solid` type.
    Is it possible to do better
* implementation
  - `InBox: Polyhedron`: multiply, hull
  - object cache (speed-up recomputation)
  - built-in model viewer
    * choosing window size according to screen size, and allow resizing
    * moving the point of view

* Assemblies: group of object linked by joints (can move), the goal is to make it easier to visualized larger projects and make sure everything fits/move properly
  - better notation for the connections
  - joint: what about time transformers (for min-max travel)
  - part: rendered part (visibility and serialization)
  - GUI: sliders for time and expansion

* ongoing work in a rendering backend based on Open CASCADE Community edition: [https://github.com/dzufferey/scadla-oce-backend](https://github.com/dzufferey/scadla-oce-backend)
  - support for different operations: chamfer, fillet, and offset, but no convex hull or Minkowski sum.
  - much more ...

* Documentation and tutorials

* ...

Also, pull requests are welcome.
