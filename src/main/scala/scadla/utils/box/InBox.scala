package scadla.utils.box

import scadla._
import scala.math._
import dzufferey.utils.Logger
import dzufferey.utils.LogLevel._
import squants.space.Millimeters

object InBox {

  private val O = Millimeters(0)
  def apply(s: Solid): Box = s match {
    case Cube(w, d, h) =>
      Box(O, O, O, w, d, h)
    case Empty =>
      Box.empty
    case Sphere(r) =>
      val s = r * sqrt(2)/2
      Box(-s, -s, -s, s, s, s)
    case Cylinder(radiusBot, radiusTop, height) =>
      val r = radiusBot min radiusTop
      val s = r * sqrt(2)/2
      Box(-s, -s, O, s, s, height)
    case Polyhedron(faces) =>
      Logger("InBox", Warning, "TODO InBox(Polyhedron)")
      Box.empty
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
      lst.foldLeft(Box.empty)( (b,s) => b.unionUnderApprox(apply(s)) )
    case Intersection(lst @ _*) =>
      if (lst.isEmpty) Box.empty
      else lst.map(apply).reduce( _ intersection _ )
    case Difference(s2, lst @ _*) =>
      val pos = apply(s2)
      val neg = BoundingBox(Union(lst:_*))
      remove(pos, neg)
    case Minkowski(lst @ _*) =>
      val init = Box(O,O,O,O,O,O)
      if (lst.isEmpty) Box.empty
      else lst.foldLeft(init)( (b,s) => b.add(apply(s)) )
    case Hull(lst @ _*) =>
      Logger("InBox", Warning, "TODO improve InBox(Hull)")
      apply(Union(lst:_*))
  }

  protected def multiply(b: Box, m: Matrix) = {
    Logger("InBox", Warning, "TODO InBox.multiply")
    Box.empty
  }

  protected def remove(a: Interval, b: Interval): Interval =
    if (a.isEmpty || b.isEmpty || a.max <= b.min || a.min >= b.max) a
    else if (b contains a) Interval.empty
    else if (b.min <= a.min) Interval(b.max, a.max)
    else if (b.max >= a.max) Interval(a.min, b.min)
    else {
      assert(b.min > a.min && b.max < a.max)
      val upper = a.max - b.max
      val lower = b.min - a.min
      if (lower > upper) Interval(a.min, b.min)
      else Interval(b.max, a.max)
    }

  protected def remove(a: Box, b: Box): Box =
    if (a.isEmpty || b.isEmpty) a
    else Box(remove(a.x, b.x),
             remove(a.y, b.y),
             remove(a.z, b.z))

}
