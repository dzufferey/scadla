# [Scadla](https://github.com/dzufferey/scadla) User Manual (Draft)

## Getting started

* Dependencies

    - Java 8 JDK

    - [sbt](http://www.scala-sbt.org/)

* Clone the scadla repository by running `git clone git@github.com:dzufferey/scadla.git` in a folder of you choice.

* Run `sbt assembly` in the scadla folder.
  This will produce the file `target/scala-2.11/scadla-assembly-0.1-SNAPSHOT.jar`.
  You can then put the jar file in the `lib` folder of your project.

  When the project will be stable enough, scadla will be available on a central repository like Maven but for the moment, it is easier to use it that way.

## Example

```scala
import scadla._     // the main package
import utils._      // some helper methods
import InlineOps._  // inline operation (more friendly syntax)
import backends._   // viewer and renderer

object MyExample {
  def main(args: Array[String]) {
    // create some simple object
    val c = Cube(1,1,1)
    val s = Sphere(1.5)
    // `+` is union, `*` is intersection
    val result = (c + c.move( -0.5, -0.5, 0)) * s
    // render the object and show it
    Renderer.default.view(result)
    // render the object and save the result in `result.stl` using the STL format
    Rendere.default.toSTL(result, "result.stl")
  }
}
```

## Scadla Constructs

### Primitives

Before going into the main part, let us take a quick look at some primitives that will be used later:

* A **Point** in 3D space is given by its X, Y, Z coordinates.
  ```scala
  Point(x: Double, y: Double, z: Double)
  ```

* A **Face**, or triangle, is defined by three points.
  ```scala
  Face(p1: Point, p2: Point, p3: Point)
  ```
  The faces are oriented.
  They have an inside and an outside.
  The orientation is computed accoring to the [right hand rule](https://en.wikipedia.org/wiki/Right_hand_rule) using the vectors `p2 - p1` and `p3 - p1`.

* A **Vector**, like a point, is given by X, Y, and Z coordinates.
  ```scala
  Vector(x: Double, y: Double, z: Double)
  ```
  However, a Vector represents a displacement, rather than a position.

* An unit **Quaternion** is a compact representation for rotations in 3D space.
  ```scala
  Quaternion(a: Double, i: Double, j: Double, k: Double)
  ```
  Roughly, the direction of (i,j,k) is the axis of rotation and a, |(i,j,k)| encodes the amplitude of the rotation.
  The [actual mathematics](https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation) are a bit more complicated.
  You don't need to understand quaternions to use scadla.
  Most likely, you will never encounter them.
  If you need to create a quaternion, you can use `Quaternion...(angle: Double, axis: Vector)` which will do the math for you.

* **4x4 Matrix** can be use to represent arbitrary affine transforms in space.
  ```scala
  Matrix(m00: Double, m01: Double, m02: Double, m03:Double,
         m10: Double, m11: Double, m12: Double, m13:Double,
         m20: Double, m21: Double, m22: Double, m23:Double,
         m30: Double, m31: Double, m32: Double, m33:Double)
  ```
  If you ask why a 4x4 matrix since we are in 3D space, time to head to wikipedia to learn about [homogeneous coordinates](https://en.wikipedia.org/wiki/Homogeneous_coordinates#Use_in_computer_graphics).

### Solids

Now we take a look at the basic solids which are then combined into more complex shapes using CSG operations.

* **Empty**
  The empty object represents nothing and is written
  ```scala
  Empty
  ```
  `Empty` is usually used as a placeholder, or as initial value of some operation, e.g., initial accumulator for a folding operation.

* **Cube** (or rectangular cuboid)
  A `Cube` is actually a rectangular cuboid, i.e., the faces can be rectangles instead of only squares.
  It is written as
  ```scala
  Cube(width: Double, depth: Double, height: Double)
  ```
  where
  `width` is the size along the X-axis,
  `depth` is the size along the Y-axis,
  `height` is the size along the Z-axis.
  The left, front, lower corner of a `Cube` is centered at (0,0,0).

* **Sphere**
  ```scala
  Sphere(radius: Double)
  ```
  generate a sphere with the given `radius` and centered at (0,0,0).

* **Cylinder** (or cones)
  A cylinder, with potentially different radii at the top and the bottom are given by
  ```scala
  Cylinder(radiusBot: Double, radiusTop: Double, height: Double)
  ```

  When the top and bottom radii are the same, one can use the following constructor with only two parameters:
  ```scala
  Cylinder(radius, height) = Cylinder(radius, radius, height)
  ```
  When using pattern matching on Cylinder, the three arguments must be used.

* **Polyhedron**
  Sometime it is easier to manually generate a solid by giving its faces.
  In that case, one can use:
  ```scala
  Polyhedron(faces: Iterable[Face])
  ```
  `Iterable[Face]` is, in scala, the supertype of any collection that contains faces.

  `Polyhedron` is also used a the result of evaluating a CSG tree.

* **Models loaded from file**
  Finally, it is possible to load existing models using
  ```scala
  FromFile(path: String, format: String = "stl")
  ```
  For the moment, the supported formats are:
    - STL (both text and binary)
    - OBJ (restricted subset)
  - AMF (restricted subset)

### Operators

Now that we have some basic solids we need to combine them to create more interesting shapes.

Before, we get into the operations let us make a quick parenthesis to explain variadic arguments in scala.
Most of the operations have a signature the looks like `Op(objs: Solid*)`.
Notice, that `Solid` is followed by a `*`.
This mean that the arguments are of type `Solid`, but the number of arguments may vary.
For instance, `Op(s0)`, `Op(s0, s1)`, `Op(s0,s1,s2)`, etc., are all valid application of the operation.
In scadla, variadic arguments are used for associative operations.
Finally, you can also apply variadic functions to collections.
You just need to tell the compiler to treat the collection are a sequence of arguments using a type annotation: `Op(coll: _*)`.

Now we are ready to go:

* **Union** takes the union of any number of objects.
  ```scala
  Union(objs: Solid*)
  ```

* **Intersection** takes the intersection of any number of objects.
  ```scala
  Intersection(objs: Solid*)
  ```

* **Difference** removes from an object, some other objects.
  ```scala
  Difference(pos: Solid, negs: Solid*)
  ```
  The first argument is the _positive_ object.
  The following arguments are the _negative_ objects.
  The negative objects are removed from the positive one.

* **Convex Hull** is doing what it names says and is simply written `Hull`.
  ```scala
  Hull(obj: Solid*)
  ```

* **Minkowski Sum** is another way of [adding](https://en.m.wikipedia.org/wiki/Minkowski_addition) objects.
  ```scala
  Minkowski(objs: Solid*)
  ```
  For the Minkowski sum, objects are interpreted as of vectors.
  A vector is in this set iff it points to a point (starting from the origin) which is inside the object.
  The faces of the object are just the boundaries of that set.
  The Minkowski addition simply adds vectors from those two sets.
  For instance, the addition of two objects _A_,_B_ is { _m_ | ∃ _a_∈_A_ ∧ _b_∈_B_. _m_ = _a_ + _b_ }.


### Transforms

Finally, we also have ways of transforming objects.

* **Scaling** streches an object in the X, Y, Z dimensions.
  ```scala
  Scale(x: Double, y: Double, z: Double, obj: Solid)
  ```

* **Rotation** around the origin.
  ```scala
  Rotate(x: Double, y: Double, z: Double, obj: Solid)
  ```
  The sequence of rotations is [X (roll), Y (pitch), and Z (yaw)](https://en.m.wikipedia.org/wiki/Euler_angles#Tait-Bryan_angles).
  Alternatively, is it possible to give a rotation using a Quaternion:
  ```scala
  Rotate(q: Quaternion, obj: Solid)
  ```

* **Translation** moves an object.
  ```scala
  Translate(x: Double, y: Double, z: Double, obj: Solid)
  ```
  A translation can also be given using a vector.
  ```scala
  Translate(v: Vector, obj: Solid)
  ```

* **Mirroring** an object around a plan going through the origin and where the normal vector is (x,y,z).
  ```scala
  Mirror(x: Double, y: Double, z: Double, obj: Solid)
  ```

* **Multiplication** by an arbitrary 4x4 matrix.
  ```scala
  Multiply(m: Matrix, obj: Solid)
  ```

### InlineOps

Sometime the above syntax can be quite verbose.
So, we also have a more concise notation where operations are either infix or postfix.
To use these operations, you must add `import InlineOps._` after the other scadla imports.

In the following, `lhs` and `rhs` are solids, `rhss` is a collection of solids, and `x`,`y`,`z` are floating point numbers.
The list is not exhaustive, please look at the API for a the full list of shorthands.

```scala
// union
lhs + rhs   = Union(lhs, rhs)
lhs ++ rhss = Union((lhs :: rhss.toList): _*)

// intersection
lhs * rhs   = Intersection(lhs, rhs)
lhs ** rhss = Intersection((lhs :: rhss.toList): _*)

// difference
lhs - rhs   = Difference(lhs, rhs)
lhs -- rhss = Difference(lhs, rhss.toList: _*)

// translation
lhs.move(x, y, z) = Translate(x, y, z, lhs)
lhs.moveX(x)      = Translate(x, 0, 0, lhs)
lhs.moveY(y)      = Translate(0, y, 0, lhs)
lhs.moveZ(z)      = Translate(0, 0, z, lhs)

// rotation
lhs.rotate(x, y, z) = Rotate(x, y, z, lhs)
lhs.rotateX(x)      = Rotate(x, 0, 0, lhs)
lhs.rotateY(y)      = Rotate(0, y, 0, lhs)
lhs.rotateZ(z)      = Rotate(0, 0, z, lhs)

//scaling
lhs.scale(x, y, z) = Scale(x, y, z, lhs)
lhs.scaleX(x)      = Scale(x, 1, 1, lhs)
lhs.scaleY(y)      = Scale(1, y, 1, lhs)
lhs.scaleZ(z)      = Scale(1, 1, z, lhs)
```

## Units

... TODO squants ...

## Renderers

When you have contructed the objet you want, the next step is to turn the CSG description into a polygonal mesh that can be used by a CAM toolchain, e.g., the slicer for a 3D printer.
A `Renderer` performs this task.

The main method of a render (the `apply` method) takes as argument a `Solid` and returns a `Polyhedron`.
Additionally, the `toSTL` method take a file name as argument and saves the mesh into the file in binary STL format.

Currently, scadla supports two renders: OpenSCAD and JCSG.

* [OpenSCAD](http://www.openscad.org/) is a CSG programming language from which scadla is greatly inspired.
  A renderer is created using `new OpenSCAD(header)` where the header is a `List[String]`.
  The header may contain instruction that will influence the rendering, usually values for the `$fa`, `$fs`, and `$fn` variables.
  The default OpenSCAD renderer is `OpenSCAD(List("$fa=4;", "$fs=0.5;"))`

  Scadla does not ship with OpenSCAD.
  To use the OpenSCAD renderer, OpenSCAD must be installed separately and the `openscad` executable must be in the path.

* [JCSG](https://github.com/miho/JCSG) is Java library for CSG.
  A render is created with `new JCSG(numSlices: Int)`.
  The number of slice is a parameter used by JCSG for teslation of spheres and cylinder.
  The higher the number of slice, the more detailed are the shapes.
  The default JCSG renderer is `JCSG(16)`,

* ParallelRenderer.
  This turn a sequential render into a parallel one by decomposing the CSG tree.
  The parallel renderer decompose a given solid and use the given renderer in parallel on the smaller parts.
  For the moment, it is _not_ recommended to use the parallel renderer with OpenSCAD.
  Due to the inner of OpenSCAD and the NEF-polyhedron in CGAL, it does not like loading the complex meshes that may be generated during the intermediate steps of the parallel decomposition.


## Viewers


Scadla offers viewers to display the generated meshes.
A viewer takes as argument a `Polyhedron` and displays it.
We currently have two viewers:

* A wrapper around [MeshLab](http://meshlab.sourceforge.net/).
  To use this viewer, MeshLab must be installed and the `meshlab` executable must be in the path.

* A built-in viewer using JavaFX.


## Assembly

Larger projects combines many different solids together.
However, these solids are independent parts that comes from different sources, are in different material, etc.
Some of those part may be 3D printed while other part may be standard and off-the-shelf such as nuts and bolts.

In Scala, these larger projects are structured as `Assembly`.
An assembly puts together a set of parts and connects them using joints.

Assemblies provide a visualization of the final project and the joint structure even provides basic animations.

... ToDo ...
