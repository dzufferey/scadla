package scadla.utils
  
import scadla._

object Trapezoid {

  def apply(xTop: Double, xBottom: Double, y: Double, z: Double, skew: Double = 0.0): Polyhedron = {
    val skewOffest = z * math.tan(skew)
    val d = (xBottom-xTop)/2
    val pts = Seq(
      Point(0, 0, 0),
      Point(xBottom, 0, 0),
      Point(xBottom, y, 0),
      Point(0, y, 0),
      Point(d + skewOffest, 0, z),
      Point(xBottom - d + skewOffest, 0, z),
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
