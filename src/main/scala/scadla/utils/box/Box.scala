package scadla.utils.box

import scadla._
import squants.space.Length

case class Box(x: Interval, y: Interval, z: Interval) {

  def isEmpty = x.isEmpty || y.isEmpty || y.isEmpty

  def contains(p: Point) =
    x.contains(p.x) &&
    y.contains(p.y) &&
    z.contains(p.z)
  
  def contains(b: Box) =
    x.contains(b.x) &&
    y.contains(b.y) &&
    z.contains(b.z)

  def overlaps(b: Box) =
    x.overlaps(b.x) &&
    y.overlaps(b.y) &&
    z.overlaps(b.z)

  def center = Point(x.center, y.center, z.center)

  def corners = Seq(
    Point(x.min, y.min, z.min),
    Point(x.min, y.min, z.max),
    Point(x.min, y.max, z.min),
    Point(x.min, y.max, z.max),
    Point(x.max, y.min, z.min),
    Point(x.max, y.min, z.max),
    Point(x.max, y.max, z.min),
    Point(x.max, y.max, z.max)
  )

  def toPolyhedron = {
    val c = corners
    Polyhedron(Seq(
      Face(c(0), c(1), c(4)), //0,1,4,5
      Face(c(1), c(5), c(4)),
      Face(c(2), c(6), c(3)), //2,3,6,7
      Face(c(3), c(6), c(7)),
      Face(c(0), c(2), c(1)), //0,1,2,3
      Face(c(1), c(2), c(3)),
      Face(c(4), c(5), c(6)), //4,5,6,7
      Face(c(5), c(7), c(6)),
      Face(c(0), c(4), c(6)), //0,2,4,6
      Face(c(0), c(6), c(2)),
      Face(c(1), c(3), c(7)), //1,3,5,7
      Face(c(1), c(5), c(5))
    ))
  }

  def move(x: Length, y: Length, z: Length) =
    if (isEmpty) Box.empty else {
      Box(this.x.move(x), this.y.move(y), this.z.move(z))
    }

  def scale(x: Double, y: Double, z: Double) =
    if (isEmpty) Box.empty else {
      Box(this.x.scale(x), this.y.scale(y), this.z.scale(z))
    }

  def intersection(b: Box) =
    if (isEmpty || b.isEmpty) Box.empty else {
      Box(x.intersection(b.x),
          y.intersection(b.y),
          z.intersection(b.z))
    }

  def add(b: Box) =
    if (isEmpty || b.isEmpty) Box.empty else {
      Box(x.add(b.x),
          y.add(b.y),
          z.add(b.z))
    }

  def hull(b: Box) =
    if (isEmpty) b
    else if (b.isEmpty) this
    else Box(x.hull(b.x),
             y.hull(b.y),
             z.hull(b.z))

  def unionUnderApprox(b: Box) =
    if (overlaps(b)) hull(b) else Box.empty

}

object Box {
  val empty = Box(Interval.empty, Interval.empty, Interval.empty)
  val unit = Box(Interval.unit, Interval.unit, Interval.unit)

  def apply(xMin: Length, yMin: Length, zMin: Length,
            xMax: Length, yMax: Length, zMax: Length): Box =
    Box(Interval(xMin, xMax),
        Interval(yMin, yMax),
        Interval(zMin, zMax))
}


