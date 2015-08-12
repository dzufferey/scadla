package scadla.utils
  
import scadla._

object PieSlice {

  def apply(outerRadius: Double, innerRadius: Double, angle: Double, height: Double) = {
    val o1 = outerRadius + 1
    val t = Tube(outerRadius, innerRadius, height)
    val blocking_half = Translate(-o1, -o1, -0.5, Cube(2* o1, o1, height + 1))
    val blocking_quarter = Translate(0, 0, -0.5, Cube(o1, o1, height + 1))
    if (angle <= 0) {
      Empty
    } else {
      val block =
        if (angle <= math.Pi/2) {
          Union(
            blocking_half,
            Translate(-o1, -0.5, 0, blocking_quarter),
            Rotate(0, 0, angle, blocking_quarter))
        } else if (angle <= math.Pi) {
          Union(
            blocking_half,
            Rotate(0, 0, angle, blocking_quarter))
        } else if (angle <= 3*math.Pi/2) {
          Union(
            Translate(0, -o1, 0, blocking_quarter),
            Rotate(0, 0, angle, blocking_quarter))
        } else if (angle <= 2*math.Pi) {
          Intersection(
            Translate(0, -o1, 0, blocking_quarter),
            Rotate(0, 0, angle, blocking_quarter))
        } else {
          Empty
        }
      Difference(t, block)
    }
  }

}
