package dzufferey.scadla

sealed abstract class Solid 

//basic shapes
case class Cube(width: Double, depth: Double, height: Double) extends Solid
case class Sphere(radius: Double) extends Solid
case class Cylinder(radiusBot: Double, radiusTop: Double, height: Double) extends Solid
case class FromFile(path: String, format: String = "stl") extends Solid
case object Empty extends Solid
case class Polyhedron(faces: Iterable[Face]) extends Solid {
  def indexed = Polyhedron.indexed(this)
  def boundingBox = Polyhedron.boundingBox(this)
}

//operations
case class Union(objs: Solid*) extends Solid
case class Intersection(objs: Solid*) extends Solid
case class Difference(pos: Solid, negs: Solid*) extends Solid
case class Minkowski(objs: Solid*) extends Solid
case class Hull(objs: Solid*) extends Solid

//transforms
case class Scale(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Rotate(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Translate(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Mirror(x: Double, y: Double, z: Double, obj: Solid) extends Solid
case class Multiply(m: Matrix, obj: Solid) extends Solid

//modifiers

/////////////////////////////
// additional constructors //
/////////////////////////////

object Cylinder {
  def apply(radius: Double, height: Double): Cylinder = Cylinder(radius, radius, height)
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

  def boundingBox(faces: Iterable[Face]): ((Double,Double),(Double,Double),(Double,Double)) = {
    val init = (Double.MaxValue, Double.MinValue)
    def updt(f: Face, proj: Point => Double, acc: (Double,Double)) = {
      ( math.min(acc._1, math.min(proj(f.p1), math.min(proj(f.p2), proj(f.p3)))),
        math.max(acc._2, math.max(proj(f.p1), math.max(proj(f.p2), proj(f.p3)))) )
    }
    faces.foldLeft((init,init,init))( (acc,f) =>
      ( updt(f, p => p.x, acc._1),
        updt(f, p => p.y, acc._2),
        updt(f, p => p.z, acc._3) ) )
  }
  def boundingBox(p: Polyhedron): ((Double,Double),(Double,Double),(Double,Double)) = boundingBox(p.faces)

}
