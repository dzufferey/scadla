package scadla.utils

import scadla._
import InlineOps._
import scala.math._
import squants.space.Length
import squants.space.Radians

object Hexagon {

  def maxRadius(minRadius: Length) = minRadius / math.sin(math.Pi/3)

  def minRadius(maxRadius: Length) = maxRadius * math.sin(math.Pi/3)

  /* Extrude vertically an hexagon (centered at 0,0 with z from 0 to height)
   * @param minRadius the radius of the circle inscribed in the hexagon
   * @param height
   */
  def apply(minRadius: Length, _height: Length) = {
    val unit = minRadius.unit
    val height = _height.in(unit)
    if (minRadius.value <= 0.0 || height.value <= 0.0) {
      Empty
    } else {
      import scala.math._
      val rd0 = minRadius/sin(Pi/3)

      val pts = for (i <- 0 until 6; j <- 0 to 1) yield
        Point(rd0 * cos(i * Pi/3), rd0 * sin(i * Pi/3), height * j) //linter:ignore ZeroDivideBy
      def face(a: Int, b: Int, c: Int) = Face(pts(a % 12), pts(b % 12), pts(c % 12))

      val side1 = for (i <- 0 until 6) yield face(  2*i, 2*i+2, 2*i+3) //linter:ignore ZeroDivideBy
      val side2 = for (i <- 0 until 6) yield face(2*i+1,   2*i, 2*i+3) //linter:ignore ZeroDivideBy
      val bottom = Array(
        face(0, 4, 2),
        face(4, 8, 6),
        face(8, 0, 10),
        face(0, 8, 4)
      )
      val top = Array(
        face(1, 3, 5),
        face(5, 7, 9),
        face(9, 11, 1),
        face(1, 5, 9)
      )
      val faces = side1 ++ side2 ++ bottom ++ top
      Polyhedron(faces)
    }
  }

  /* Extrude vertically a semi-regular hexagon (centered at 0,0 with z from 0 to height)
   * @param radius1 the radius of the circle inscribed in the hexagon even faces
   * @param radius2 the radius of the circle inscribed in the hexagon odd faces
   * @param height
   */
  def semiRegular(radius1: Length, radius2: Length, height: Length) = {
    val r = maxRadius(radius1) max maxRadius(radius2)
    val base = Cylinder(r,height)
    val chop = Cube(r, 2*r, height).moveY(-r)
    val neg1 = for(i <- 0 until 6 if i % 2 == 0) yield chop.moveX(radius1).rotateZ(Radians(i * Pi / 3))
    val neg2 = for(i <- 0 until 6 if i % 2 == 1) yield chop.moveX(radius2).rotateZ(Radians(i * Pi / 3))
    base -- neg1 -- neg2
  }

}
