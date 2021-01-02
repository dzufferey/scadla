package scadla.utils.box

import scadla._
import scala.math._
import squants.space.Length
import squants.space.Millimeters

object BoundingBox {

  def apply(points: Iterable[Point]): Box = {
    if (points.isEmpty) {
      Box.empty
    } else {
      val init = (Millimeters(Double.MaxValue), Millimeters(Double.MinValue))
      def updt(p: Point, proj: Point => Length, acc: (Length,Length)) = {
        ( acc._1 min proj(p), acc._2 max proj(p) )
      }
      val ((xMin, xMax), (yMin, yMax), (zMin, zMax)) =
        points.foldLeft((init,init,init))( (acc,p) =>
          ( updt(p, p => p.x, acc._1),
            updt(p, p => p.y, acc._2),
            updt(p, p => p.z, acc._3) ) )
      Box(xMin, yMin, zMin, xMax, yMax, zMax)
    }
  }

  private val O = Millimeters(0)
  def apply(s: Solid): Box = s match {
    case Cube(w, d, h) =>
      Box(O, O, O, w, d, h)
    case Empty =>
      Box.empty
    case Sphere(r) =>
      Box(-r, -r, -r, r, r, r)
    case Cylinder(radiusBot, radiusTop, height) =>
      val r = radiusBot max radiusTop
      Box(-r, -r, O, r, r, height)
    case Polyhedron(faces) =>
      apply(faces.flatMap( f => Seq(f.p1, f.p2, f.p3) ))
    case f @ FromFile(_,_) =>
      apply(f.load)

    case Translate(x, y, z, s2) =>
      apply(s2).move(x,y,z)
    case Scale(x, y, z, s2) =>
      apply(s2).scale(x,y,z)
    case Rotate(x, y, z, s2) =>
      multiply(apply(s2), Matrix.rotation(x,y,z))
    case Mirror(x, y, z, s2) =>
      multiply(apply(s2), Matrix.mirror(x,y,z))
    case Multiply(m, s2) =>
      multiply(apply(s2), m)

    case Union(lst @ _*) =>
      lst.foldLeft(Box.empty)( (b,s) => b.hull(apply(s)) )
    case Intersection(lst @ _*) =>
      if (lst.isEmpty) Box.empty
      else lst.map(apply).reduce( _ intersection _ )
    case Difference(s2, lst @ _*) =>
      val pos = apply(s2)
      val neg = InBox(Union(lst:_*))
      remove(pos, neg)
    case Minkowski(lst @ _*) =>
      val init = Box(O,O,O,O,O,O)
      if (lst.isEmpty) Box.empty
      else lst.foldLeft(init)( (b,s) => b.add(apply(s)) )
    case Hull(lst @ _*) =>
      apply(Union(lst:_*))
  }

  protected def multiply(b: Box, m: Matrix) = {
    val newCorners = b.corners.map(m * _)
    apply(newCorners)
  }

  protected def remove(a: Box, b: Box) = {
    if (a.isEmpty || b.isEmpty) a
    else {
      val overlapX = a.x overlaps b.x
      val overlapY = a.y overlaps b.y
      val overlapZ = a.z overlaps b.z
      val coverX = b.x contains a.x
      val coverY = b.y contains a.y
      val coverZ = b.z contains a.z
      if (coverX && coverY && overlapZ) {
        Box(a.x, a.y, a.z.remove(b.z))
      } else if (coverX && overlapY && coverZ) {
        Box(a.x, a.y.remove(b.y), a.z)
      } else if (overlapX && coverY && coverZ) {
        Box(a.x.remove(b.x), a.y, a.z)
      } else {
        a
      }
    }
  }

}
