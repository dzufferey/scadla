package dzufferey.scadla.utils
  
import dzufferey.scadla._

object Trapezoid {

  def apply(xTop: Double, xBottom: Double, y: Double, z: Double) = {
    val x = math.max(xTop, xBottom)
    val cube = Cube(x, y, z)
    val blocking = Cube(x, y, 2*z)
    val a = math.atan2(z, (xBottom-xTop)/2)
    Difference(
      cube,
      Rotate(0, a, 0, Translate( xBottom, 0, 0, blocking)),
      Translate(-xBottom, 0, 0, Rotate(0,-a, 0, blocking))
    )
  }

}
