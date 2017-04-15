package scadla.utils
  
import scadla._
import squants.space.Length
import squants.space.Millimeters

object Trapezoid {

  def apply(xTop: Length, xBottom: Length, y: Length, z: Length, skew: Double = 0.0): Polyhedron = {
    val skewOffest = z * math.tan(skew)
    val d = (xBottom-xTop)/2
    val O = Millimeters(0)
    val pts = Seq(
      Point(O, O, O),
      Point(xBottom, O, O),
      Point(xBottom, y, O),
      Point(O, y, O),
      Point(d + skewOffest, O, z),
      Point(xBottom - d + skewOffest, O, z),
      Point(xBottom - d + skewOffest, y, z),
      Point(d + skewOffest, y, z)
    )
    def face(a: Int, b: Int, c: Int) = Face(pts(a), pts(b), pts(c ))
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

}
