package dzufferey.scadla

sealed abstract class Solid 

//basic shapes
case class Cube(width: Double, depth: Double, height: Double) extends Solid
case class Sphere(radius: Double) extends Solid
case class Cylinder(radiusBot: Double, radiusTop: Double, height: Double) extends Solid
case class Polyhedron(faces: Iterable[Face]) extends Solid
case class FromFile(path: String, format: String = "stl") extends Solid
case object Empty extends Solid

//operations
case class Union(objs: Solid*) extends Solid
case class Intersection(objs: Solid*) extends Solid
case class Difference(pos: Solid, negs: Solid*) extends Solid
case class Minkowski(objs: Solid*) extends Solid
case class Hull(obj: Solid*) extends Solid

//transforms
case class Scale(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Rotate(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Translate(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Mirror(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Multiply(m: Matrix, obj: Solid) extends Solid

//modifiers


//additional constructor for Cylinder
object Cylinder {
  def apply(radius: Double, height: Double): Cylinder = Cylinder(radius, radius, height)
}
