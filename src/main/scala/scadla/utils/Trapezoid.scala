package scadla.utils

import scadla.*
import squants.space.{Length, Angle, Millimeters, Radians}

object Trapezoid {

  def apply(x: (Length, Length, Angle) /*top, bottom, skew*/,
            y: (Length, Length, Angle) /*top, bottom, skew*/,
            z: Length): Polyhedron = {
    val (xTop, xBottom, xSkew) = x
    val xSkewOffest = z * math.tan(xSkew.toRadians)
    val dx = (xBottom-xTop)/2
    val (yTop, yBottom, ySkew) = y
    val ySkewOffest = z * math.tan(ySkew.toRadians)
    val dy = (yBottom-yTop)/2
    val O = Millimeters(0)
    val xTop0 = dx + xSkewOffest
    val xTop1 = xBottom - dx + xSkewOffest
    val yTop0 = dy + ySkewOffest
    val yTop1 = yBottom - dy + ySkewOffest
    val pts = Seq(
      Point(O,          O,          O),
      Point(xBottom,    O,          O),
      Point(xBottom,    yBottom,    O),
      Point(O,          yBottom,    O),
      Point(xTop0, yTop0, z),
      Point(xTop1, yTop0, z),
      Point(xTop1, yTop1, z),
      Point(xTop0, yTop1, z)
    )
    def face(a: Int, b: Int, c: Int) = Face(pts(a), pts(b), pts(c))
    val faces = Seq(
      face(0,3,1),
      face(1,3,2),
      face(4,5,7),
      face(5,6,7),
      face(0,1,4),
      face(1,5,4),
      face(1,2,5),
      face(2,6,5),
      face(2,3,6),
      face(3,7,6),
      face(3,0,7),
      face(0,4,7)
    )
    Polyhedron(faces)
  }

  def apply(xTop: Length, xBottom: Length, y: Length, z: Length, skew: Angle = Radians(0.0)): Polyhedron = {
    val _x = (xTop, xBottom, skew)
    val _y = (y, y, Radians(0.0))
    apply(_x, _y, z)
  }

}
