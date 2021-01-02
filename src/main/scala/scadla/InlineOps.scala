package scadla

import squants.space.Length
import scala.language.postfixOps
import squants.space.Millimeters
import squants.space.Angle
import squants.space.Degrees

object InlineOps {

  implicit final class AngleConversions[A](n: A)(implicit num: Numeric[A]) {
    def ° = Degrees(n)
  }

  implicit final class Ops(private val lhs: Solid) extends AnyVal {
    import squants.space.LengthConversions._

    def translate(x: Length, y: Length, z: Length) = Translate(x, y, z, lhs)
    def move(x: Length, y: Length, z: Length) = Translate(x, y, z, lhs)
    def move(v: Vector) = Translate(v, lhs)
    def move(p: Point) = Translate(p.toVector, lhs)
    def moveX(x: Length) = Translate(x, 0 mm, 0 mm, lhs)
    def moveY(y: Length) = Translate(0 mm, y, 0 mm, lhs)
    def moveZ(z: Length) = Translate(0 mm, 0 mm, z, lhs)

    def rotate(x: Angle, y: Angle, z: Angle) = Rotate(x, y, z, lhs)
    def rotate(q: Quaternion) = Rotate(q, lhs)
    def rotateX(x: Angle) = Rotate(x, 0°, 0°, lhs)
    def rotateY(y: Angle) = Rotate(0°, y, 0°, lhs)
    def rotateZ(z: Angle) = Rotate(0°, 0°, z, lhs)

    def scale(x: Double, y: Double, z: Double) = Scale(x, y, z, lhs)
    def scaleX(x: Double) = Scale(x, 1, 1, lhs)
    def scaleY(y: Double) = Scale(1, y, 1, lhs)
    def scaleZ(z: Double) = Scale(1, 1, z, lhs)

    def mirror(x: Double, y: Double, z: Double) = Mirror(x, y, z, lhs)
    def multiply(m: Matrix) = Multiply(m, lhs)

    def +(rhs: Solid) = Union(lhs, rhs)
    def ++(rhs: Iterable[Solid]) = Union((lhs :: rhs.toList): _*)
    def union(rhs: Solid) = Union(lhs, rhs)

    def *(rhs: Solid) = Intersection(lhs, rhs)
    def **(rhs: Iterable[Solid]) = Intersection((lhs :: rhs.toList): _*)
    def intersection(rhs: Solid) = Intersection(lhs, rhs)

    def -(rhs: Solid) = Difference(lhs, rhs)
    def --(rhs: Iterable[Solid]) = Difference(lhs, rhs.toList: _*)
    def difference(rhs: Solid) = Difference(lhs, rhs)

    def hull(rhs: Solid) = Hull(lhs, rhs)
    def minkowski(rhs: Solid) = Minkowski(lhs, rhs)

    def toPolyhedron = backends.Renderer.default(lhs) //TODO a way of being lazy

  }

}
