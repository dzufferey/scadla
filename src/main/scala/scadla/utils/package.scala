package scadla

import scadla.utils.Trig._
import squants.space.{Length, Angle}
import scala.language.postfixOps
import scadla.InlineOps._

package object utils {

  import scadla._
  
  def polarCoordinates(x: Length, y: Length) = (Trig.hypot(x, y), Trig.atan2(y,x))
  
  def traverse(f: Solid => Unit, s: Solid): Unit = s match {
    case t: Transform =>  traverse(f, t.child); f(t)
    case o: Operation => o.children.foreach(traverse(f, _)); f(o)
    case other => f(other)
  }
  
  def map(f: Solid => Solid, s: Solid): Solid = s match {
    case o: Operation => f(o.setChildren(o.children.map(map(f, _))))
    case t: Transform => f(t.setChild(map(f, t.child)))
    case other => f(other)
  }

  def fold[A](f: (A, Solid) => A, acc: A, s: Solid): A = s match {
    case t: Transform =>  f(fold(f, acc, t.child), t)
    case o: Operation => f(o.children.foldLeft(acc)( (acc, s2) => fold(f, acc, s2) ), s)
    case other => f(acc, other)
  }

  private val Zero = 0°
  def simplify(s: Solid): Solid = {
    def rewrite(s: Solid): Solid = s match {
      case Cube(width, depth, height) if width.value <= 0.0 || depth.value <= 0.0 || height.value <= 0.0 => Empty
      case Sphere(radius) if radius.value <= 0.0 => Empty
      case Cylinder(radiusBot, radiusTop, height) if height.value <= 0.0 => Empty
      //TODO order the points/faces to get a normal form
      case Polyhedron(triangles) if triangles.isEmpty => Empty

      case Translate(x1, y1, z1, Translate(x2, y2, z2, s2)) => Translate(x1+x2, y1+y2, z1+z2, s2)
      case Rotate(x1, Zero, Zero, Rotate(x2, Zero, Zero, s2)) => Rotate(x1+x2, Zero, Zero, s2)
      case Rotate(Zero, y1, Zero, Rotate(Zero, y2, Zero, s2)) => Rotate(Zero, y1+y2, Zero, s2)
      case Rotate(Zero, Zero, z1, Rotate(Zero, Zero, z2, s2)) => Rotate(Zero, Zero, z1+z2, s2)
      case Scale(x1, y1, z1, Scale(x2, y2, z2, s2)) => Scale(x1*x2, y1*y2, z1*z2, s2)
      
      //TODO flatten ops, reorganize according to com,assoc,...
      case Union(lst @ _*) =>
        val lst2 = lst.toSet - Empty
        if (lst2.isEmpty) Empty else Union(lst2.toSeq: _*)
      case Intersection(lst @ _*) =>
        val lst2 = lst.toSet
        if (lst2.contains(Empty) || lst2.isEmpty) Empty else Intersection(lst2.toSeq: _*)
      case Difference(s2, lst @ _*) =>
        val lst2 = lst.toSet - Empty
        if (lst2.isEmpty) s2
        else if (lst2 contains s2) Empty
        else Difference(s2, lst2.toSeq: _*)
      case Minkowski(lst @ _*) =>
        val lst2 = lst.toSet - Empty
        if (lst2.isEmpty) Empty else Minkowski(lst2.toSeq: _*)
      case Hull(lst @ _*) =>
        val lst2 = lst.toSet - Empty
        if (lst2.isEmpty) Empty else Hull(lst2.toSeq: _*)

      case s => s
    }

    var sOld = s
    var sCurr = map(rewrite, s)
    while (sOld != sCurr) {
      sOld = sCurr
      sCurr = map(rewrite, sCurr)
    }
    sCurr
  }

  /** return a value ∈ [min,max] which is a multiple of step (or min, max) */
  def round(value: Double, min: Double, max: Double, step: Double): Double = {
    val stepped = math.rint(value / step) * step
    math.min(max, math.max(min, stepped))
  }

  /** https://en.wikipedia.org/wiki/Chord_(geometry) */
  def chord(angle: Angle) = 2*sin(angle/2)

  /** https://en.wikipedia.org/wiki/Apothem */
  def apothem(nbrSides: Int, sideLength: Length) = sideLength / (2 * tan(Pi / nbrSides))
  def apothemFromR(nbrSides: Int, maxRadius: Length) = maxRadius * cos(Pi / nbrSides)

  def incribedRadius(nbrSides: Int, sideLength: Length) = sideLength / tan(Pi / nbrSides) / 2

  def circumscribedRadius(nbrSides: Int, sideLength: Length) = sideLength / sin(Pi / nbrSides) / 2

}

