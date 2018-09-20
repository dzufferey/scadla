package scadla

import squants.space.Length
import squants.space.Angle

abstract class Solid {

  /** Since we render in a second step, let's keep track of the stack trace so we can trace error to the source.
   *
   * The downside is that it might make this a bit slower and take much more memory...
   *
   * drop(4) to get rid of:
   *  java.base/java.lang.Thread.getStackTrace
   *  scadla.Solid.<init>
   *  scadla.Shape.<init>
   *  scadla.???.<init>
   */
  val trace = Thread.currentThread().getStackTrace().drop(4).toSeq

}

//basic shapes
abstract class Shape extends Solid

case class Cube(width: Length, depth: Length, height: Length) extends Shape
case class Sphere(radius: Length) extends Shape
case class Cylinder(radiusBot: Length, radiusTop: Length, height: Length) extends Shape
case class FromFile(path: String, format: String = "stl") extends Shape {
  def load: Polyhedron = format match {
    case "stl" => backends.stl.Parser(path)
    case "obj" => backends.obj.Parser(path)
    case "amf" => backends.amf.Parser(path)
    case other => sys.error("parsing " + other + " not yet supported")
  }
}
case object Empty extends Shape
case class Polyhedron(faces: Iterable[Face]) extends Shape {
  def indexed = Polyhedron.indexed(this)
}

//operations
abstract class Operation(val children: Seq[Solid]) extends Solid {
  def isCommutative = false
  def isLeftAssociative = false
  def isRightAssociative = false
  def isAssociative = isLeftAssociative && isRightAssociative
  def setChildren(c: Seq[Solid]): Operation
}

case class Union(objs: Solid*) extends Operation(objs) {
  override def isCommutative = true
  override def isLeftAssociative = true
  override def isRightAssociative = true
  def setChildren(c: Seq[Solid]) = Union(c: _*)
}
case class Intersection(objs: Solid*) extends Operation(objs) {
  override def isCommutative = true
  override def isLeftAssociative = true
  override def isRightAssociative = true
  def setChildren(c: Seq[Solid]) = Intersection(c: _*)
}
case class Difference(pos: Solid, negs: Solid*) extends Operation(pos +: negs) {
  override def isCommutative = false
  override def isLeftAssociative = true
  override def isRightAssociative = false
  def setChildren(c: Seq[Solid]) = Difference(c.head, c.tail: _*)
}
case class Minkowski(objs: Solid*) extends Operation(objs) {
  override def isCommutative = true
  override def isLeftAssociative = true
  override def isRightAssociative = true
  def setChildren(c: Seq[Solid]) = Minkowski(c: _*)
}
case class Hull(objs: Solid*) extends Operation(objs) {
  override def isCommutative = true
  override def isLeftAssociative = true
  override def isRightAssociative = true
  def setChildren(c: Seq[Solid]) = Hull(c: _*)
}

//transforms
sealed abstract class Transform(val child: Solid) extends Solid {
  def matrix: Matrix
  def asMultiply = Multiply(matrix, child)
  def setChild(c: Solid): Transform = if (c == child) this else Multiply(matrix, c)
}

case class Scale(x: Double, y: Double, z: Double, obj: Solid) extends Transform(obj) {
  def matrix = Matrix.scale(x, y, z)
  override def setChild(c: Solid) = if (c == obj) this else Scale(x, y, z, c)
}
case class Rotate(x: Angle, y: Angle, z: Angle, obj: Solid) extends Transform(obj) {
  def matrix = Matrix.rotation(x, y, z)
  override def setChild(c: Solid) = if (c == obj) this else Rotate(x, y, z, c)
}
case class Translate(x: Length, y: Length, z: Length, obj: Solid) extends Transform(obj) {
  def matrix = Matrix.translation(x, y, z)
  override def setChild(c: Solid) = if (c == obj) this else Translate(x, y, z, c)
}
case class Mirror(x: Double, y: Double, z: Double, obj: Solid) extends Transform(obj) {
  def matrix = Matrix.mirror(x, y, z)
  override def setChild(c: Solid) = if (c == obj) this else Mirror(x, y, z, c)
}
case class Multiply(m: Matrix, obj: Solid) extends Transform(obj) {
  def matrix = m
}

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
