package scadla.backends.renderers

import scadla.backends.renderers.BackwardCompatHelper.Solidable
import scadla.backends.renderers.Renderable._
import scadla.{Matrix, Point, Quaternion, Vector}
import scadla.backends.renderers.Solids.{Difference, Hull, Intersection, Minkowski, Mirror, Multiply, Rotate, Scale, Translate, Union}
import squants.{Angle, Length}
import squants.space.Degrees

object InlineOps {

  implicit final class AngleConversions[A](n: A)(implicit num: Numeric[A]) {
    def ° = Degrees(n)
  }

  implicit def renderableOps[A](a: RenderableForOps[A]): Ops[A] = {
    implicit val fa = a.fa
    implicit val fb = a.fb
    a.a
  }

  implicit final class Ops[A](val lhs: A)(implicit renderer: Renderable[A], ev2: Solidable[A]) {

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

    def rotateX(x: Angle) = Rotate(x, 0 °, 0 °, lhs)

    def rotateY(y: Angle) = Rotate(0 °, y, 0 °, lhs)

    def rotateZ(z: Angle) = Rotate(0 °, 0 °, z, lhs)

    def scale(x: Double, y: Double, z: Double) = Scale(x, y, z, lhs)

    def scaleX(x: Double) = Scale(x, 1, 1, lhs)

    def scaleY(y: Double) = Scale(1, y, 1, lhs)

    def scaleZ(z: Double) = Scale(1, 1, z, lhs)

    def mirror(x: Double, y: Double, z: Double) = Mirror(x, y, z, lhs)

    def multiply(m: Matrix) = Multiply(m, lhs)

    def +[B](rhs: RenderableForOps[B]) = Union(lhs, rhs)

    def ++(rhs: Iterable[RenderableForOps[_]]) = Union((lhs.toRenderableForOps :: rhs.toList): _*)

    def union[B](rhs: RenderableForOps[B]) = Union(lhs, rhs)

    def *[B](rhs: RenderableForOps[B]) = Intersection(lhs, rhs)

    def **(rhs: Iterable[RenderableForOps[_]]) = Intersection((lhs.toRenderableForOps :: rhs.toList): _*)

    def intersection[B](rhs: RenderableForOps[B]) = Intersection(lhs, rhs)

    def -[B](rhs: RenderableForOps[B]) = Difference(lhs, rhs)

    def --(rhs: Iterable[RenderableForOps[_]]) = Difference(lhs, rhs.toList: _*)

    def difference[B](rhs: RenderableForOps[B]) = Difference(lhs, rhs)

    def hull[B](rhs: RenderableForOps[B]) = Hull(lhs, rhs)

    def minkowski[B](rhs: RenderableForOps[B]) = Minkowski(lhs, rhs)

  }

}
