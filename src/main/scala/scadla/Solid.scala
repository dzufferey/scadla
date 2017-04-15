package scadla

import squants.space.Length
import squants.space.Angle

sealed abstract class Solid 

//basic shapes
case class Cube(width: Length, depth: Length, height: Length) extends Solid
case class Sphere(radius: Length) extends Solid
case class Cylinder(radiusBot: Length, radiusTop: Length, height: Length) extends Solid
case class FromFile(path: String, format: String = "stl") extends Solid {
  def load: Polyhedron = format match {
    case "stl" => backends.stl.Parser(path)
    case "obj" => backends.obj.Parser(path)
    case "amf" => backends.amf.Parser(path)
    case other => sys.error("parsing " + other + " not yet supported")
  }
}
case object Empty extends Solid
case class Polyhedron(faces: Iterable[Face]) extends Solid {
  def indexed = Polyhedron.indexed(this)
}

//operations
case class Union(objs: Solid*) extends Solid
case class Intersection(objs: Solid*) extends Solid
case class Difference(pos: Solid, negs: Solid*) extends Solid
case class Minkowski(objs: Solid*) extends Solid
case class Hull(objs: Solid*) extends Solid

//transforms
case class Scale(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Rotate(x: Angle, y: Angle, z: Angle, obj: Solid) extends Solid
case class Translate(x: Length, y: Length, z: Length, obj: Solid) extends Solid
case class Mirror(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Multiply(m: Matrix, obj: Solid) extends Solid

//modifiers

/////////////////////////////
// additional constructors //
/////////////////////////////

object Cylinder {
  def apply(radius: Length, height: Length): Cylinder = Cylinder(radius, radius, height)
}

object Translate {
  def apply(v: Vector, s: Solid): Translate = Translate(v.x, v.y, v.z, s)
}

object Rotate {
  def apply(q: Quaternion, s: Solid): Rotate = {
    // TODO make sure OpendSCAD use the same sequence of roation
    val v = q.toRollPitchYaw
    Rotate(v.x, v.y, v.z, s)
  }
}

///////////
// utils //
///////////

object Polyhedron {

  def indexed(faces: Iterable[Face]): (IndexedSeq[Point], Iterable[(Int,Int,Int)]) = {
    val points = faces.foldLeft(Set[Point]())( (acc, face) => acc + face.p1 + face.p2 + face.p3 )
    val indexed = points.toIndexedSeq
    val idx: Map[Point, Int] = indexed.zipWithIndex.toMap
    (indexed, faces.map{ case Face(p1,p2,p3) => (idx(p1),idx(p2),idx(p3)) }) 
  }
  def indexed(p: Polyhedron): (IndexedSeq[Point], Iterable[(Int,Int,Int)]) = indexed(p.faces)

}
